/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jackstaff.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.configuration.ClientConfig;
import org.jackstaff.grpc.exception.ValidationException;
import org.jackstaff.grpc.internal.HeaderMetadata;
import org.jackstaff.grpc.internal.InternalProto;
import org.jackstaff.grpc.internal.Stub;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * the client side delegate
 * @author reco@jackstaff.org
 * @see Interceptor
 * @see ClientConfig
 */
public class Client {

    public interface ProxyCreator {
        Object newProxyInstance(Class<?> type, InvocationHandler handler);
    }

    private static class MethodKey {

        private final Class<?> type;
        private final Method method;

        public MethodKey(Class<?> type, Method method) {
            this.type = type;
            this.method = method;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodKey methodKey = (MethodKey) o;
            return type.equals(methodKey.type) &&
                    method.equals(methodKey.method);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, method);
        }
    }

    private final Set<Class<?>> types = ConcurrentHashMap.newKeySet();
    private final Map<MethodKey, MethodDescriptor> methods = new ConcurrentHashMap<>();
    private final Map<String, Stub<?,?,?>> stubs = new ConcurrentHashMap<>();
    private final ProxyCreator creator;

    /**
     * default, will use jdk's Proxy
     */
    public Client() {
        this((type, handler) -> Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, handler));
    }

    /**
     * use provided ProxyCreator, like CGLib's Proxy
     *
     * @param creator the creator
     */
    public Client(ProxyCreator creator) {
        this.creator = creator;
    }

    /**
     * setup the client stub
     *
     * @param authorityClients the config
     */
    public void setup(Map<String, ClientConfig> authorityClients) {
        authorityClients.forEach((authority, cfg) -> {
            ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forAddress(cfg.getHost(), cfg.getPort());
            if (cfg.getKeepAliveTime() > 0) {
                builder.keepAliveTime(cfg.getKeepAliveTime(), TimeUnit.SECONDS);
                builder.keepAliveWithoutCalls(cfg.isKeepAliveWithoutCalls());
            }
            if (cfg.getIdleTimeout() > 300) {
                builder.idleTimeout(cfg.getIdleTimeout(), TimeUnit.SECONDS);
            }
            if (cfg.getKeepAliveTimeout() > 30) {
                builder.keepAliveTimeout(cfg.getKeepAliveTimeout(), TimeUnit.SECONDS);
            }
            if (cfg.getMaxRetryAttempts() > 0) {
                builder.enableRetry().maxRetryAttempts(cfg.getMaxRetryAttempts());
            }
            if (cfg.isPlaintext()) {
                builder.usePlaintext();
            }
            if (cfg.getMaxInboundMessageSize() > 512 * 1024) {
                builder.maxInboundMessageSize(cfg.getMaxInboundMessageSize());
            }
            stubs.put(authority, new Stub<>(authority, builder.build(), Duration.ofSeconds(cfg.getDefaultTimeout())));
        });
    }

    /**
     * delegate a "protocol interface" type which the server run at "authority" machine
     *
     * @param authority    the micro service / machine / VM name in config
     * @param type         the "protocol interface"
     * @param required     if required
     * @param interceptors the Interceptor list
     * @param <T>          T
     * @return autowired proxy instance
     */
    @SuppressWarnings("unchecked")
    public <T> T autowired(String authority, Class<T> type, boolean required, List<Interceptor> interceptors) {
        Stub<?,?,?> prototype = stubs.get(authority);
        if (prototype == null) {
            if (required) {
                throw new ValidationException("client " + authority + " NOT found in configuration: spring.grpc.client..");
            }
            return null;
        }
        if (!types.contains(type)) {
            List<MethodDescriptor> descriptors = Arrays.stream(type.getMethods()).
                    map(method -> new MethodDescriptor(type, method)).collect(Collectors.toList());
            MethodDescriptor.validateProtocol(type, descriptors);
            descriptors.forEach(desc -> methods.put(new MethodKey(type, desc.getMethod()), desc));
            types.add(type);
        }
        Object bean = creator.newProxyInstance(type, (proxy, method, args) -> {
            if (method.getDeclaringClass().equals(Object.class)) {
                try {
                    return method.invoke(proxy, args);
                } catch (Exception ex) {
                    return null;
                }
            }
            return walkThrough(prototype, methods.get(new MethodKey(type, method)), proxy, args, interceptors);
        });
        return (T) bean;
    }

    @SuppressWarnings("unchecked")
    private Object walkThrough(Stub<?,?,?> prototype, MethodDescriptor descriptor, Object proxy,
                               Object[] args, List<Interceptor> interceptors) throws Exception{
        Stub<?,?,?> stub = new Stub<>(prototype, descriptor);
        Context context =new Context(descriptor, args, proxy, stub);
        Packet<?> packet = Utils.before(context, interceptors);
        if (!packet.isException()){
            try {
                packet = descriptor.isV2() ? v2StubCall(context, stub) : v1StubCall(context, (Stub<?,Packet<?>,Packet<?>>)stub);
            }catch (Exception ex0){
                packet = Packet.throwable(ex0);
            }
            Utils.after(context, interceptors, packet);
        }
        if (packet.isException()){
            throw (Exception)packet.getPayload();
        }
        return packet.getPayload();
    }

    @SuppressWarnings("unchecked")
    private <ReqT, RespT> Packet<?> v2StubCall(Context context, Stub<?, ReqT, RespT> stub) {
        MethodDescriptor descriptor = context.getMethodDescriptor();
        Object[] args = context.getArguments();
        switch (descriptor.getMethodType()) {
            case UNARY: {
                stub.attachDefaultDeadline();
                return Packet.ok(stub.blockingUnary((ReqT) args[0]));
            }
            case ASYNCHRONOUS_UNARY: {
                MessageStream<RespT> respStream = MessageStream.build((Consumer<RespT>) args[1]).unary();
                stub.attachDeadline(respStream.timeout());
                stub.asyncUnary((ReqT)args[0], respStream.toStreamObserver());
                return new Packet<>();
            }
            case BLOCKING_SERVER_STREAMING: {
                List<RespT> list = new ArrayList<>();
                stub.attachDefaultDeadline();
                stub.blockingServerStreaming((ReqT)args[0]).forEachRemaining(list::add);
                return Packet.ok(list);
            }
            case SERVER_STREAMING: {
                MessageStream<RespT> respStream = MessageStream.build((Consumer<RespT>) args[1]);
                stub.attachDeadline(respStream.timeout());
                stub.asyncServerStreaming((ReqT)args[0], respStream.toStreamObserver());
                return new Packet<>();
            }
            case CLIENT_STREAMING: {
                MessageStream<RespT> respStream = MessageStream.build((Consumer<RespT>) args[0]).unary();
                stub.attachDeadline(respStream.timeout());
                StreamObserver<ReqT> reqObserver = stub.asyncClientStreaming(respStream.toStreamObserver());
                MessageStream<ReqT> reqStream = new MessageStream<>(reqObserver).link(respStream);
                return Packet.ok(reqStream);
            }
            case BIDI_STREAMING: {
                MessageStream<RespT> respStream = MessageStream.build((Consumer<RespT>) args[0]);
                stub.attachDeadline(respStream.timeout());
                StreamObserver<ReqT> reqObserver = stub.asyncBidiStreaming(respStream.toStreamObserver());
                MessageStream<ReqT> reqStream = new MessageStream<>(reqObserver).link(respStream);
                return Packet.ok(reqStream);
            }
            default:
                return new Packet<>();
        }
    }

    private Packet<?> v1StubCall(Context context, Stub<?,Packet<?>,Packet<?>> stub) {
        Transform<Packet<?>, InternalProto.Packet> transform= Transforms.getTransform(Packet.class);
        MethodDescriptor descriptor = context.getMethodDescriptor();
        switch (descriptor.getMethodType()) {
            case UNARY: {
                stub.attach(HeaderMetadata.ROOT, descriptor.getSign());
                stub.attachDefaultDeadline();
                return stub.blockingUnary(Packet.boxing(context.getArguments()));
            }
            case ASYNCHRONOUS_UNARY: {
                MethodDescriptor peer = descriptor.getPeer();
                stub.attach(HeaderMetadata.ROOT, peer.getSign());
                Object[] args = Arrays.copyOf(context.getArguments(), peer.getMethod().getParameterCount()); //-1
                MessageStream<?> respStream = descriptor.getStream(context.getArguments()).unary();
                stub.attachDeadline(respStream.timeout());
                stub.asyncUnary(Packet.boxing(args), respStream.toPacketStreamObserver());
                return new Packet<>();
            }
            case BLOCKING_SERVER_STREAMING: {
                MethodDescriptor peer = descriptor.getPeer();
                Object[] args = Arrays.copyOf(context.getArguments(), peer.getMethod().getParameterCount()); //+1
                stub.attach(HeaderMetadata.ROOT, peer.getSign());
                stub.attachDefaultDeadline();
                List<Object> list = new ArrayList<>();
                stub.blockingServerStreaming(Packet.boxing(args)).forEachRemaining(v->list.add(v.getPayload()));
                return Packet.ok(list);
            }
            case SERVER_STREAMING: {
                stub.attach(HeaderMetadata.ROOT, descriptor.getSign());
                MessageStream<?> respStream = descriptor.getStream(context.getArguments());
                stub.attachDeadline(respStream.timeout());
                stub.asyncServerStreaming(Packet.boxing(context.getArguments()), respStream.toPacketStreamObserver());
                return new Packet<>();
            }
            case CLIENT_STREAMING: {
                stub.attach(HeaderMetadata.ROOT, descriptor.getSign());
                MessageStream<?> respStream = descriptor.getStream(context.getArguments());
                stub.attach(HeaderMetadata.BINARY_ROOT, transform.build(Packet.boxing(context.getArguments())).getData().toByteArray());
                stub.attachDeadline(respStream.timeout());
                StreamObserver<Packet<?>> reqObserver = stub.asyncClientStreaming(respStream.toPacketStreamObserver());
                MessageStream<?> reqStream = new MessageStream<>(new MessageObserver<>(reqObserver)).link(respStream);
                return Packet.ok(reqStream);
            }
            case BIDI_STREAMING: {
                stub.attach(HeaderMetadata.ROOT, descriptor.getSign());
                MessageStream<?> respStream = descriptor.getStream(context.getArguments());
                stub.attach(HeaderMetadata.BINARY_ROOT, transform.build(Packet.boxing(context.getArguments())).getData().toByteArray());
                stub.attachDeadline(respStream.timeout());
                StreamObserver<Packet<?>> reqObserver = stub.asyncBidiStreaming(respStream.toPacketStreamObserver());
                MessageStream<?> reqStream = new MessageStream<>(new MessageObserver<>(reqObserver)).link(respStream);
                return Packet.ok(reqStream);
            }
            default:
                return new Packet<>();
        }
    }

    public void shutdown(){
        stubs.values().parallelStream().map(Stub::getChannel).
                filter(c->!c.isShutdown()).filter(c->!c.isTerminated()).
                forEach(ManagedChannel::shutdown);
    }

}
