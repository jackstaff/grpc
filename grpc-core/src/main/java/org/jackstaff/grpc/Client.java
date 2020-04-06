package org.jackstaff.grpc;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.configuration.ClientConfig;
import org.jackstaff.grpc.exception.StatusException;
import org.jackstaff.grpc.exception.ValidationException;
import org.jackstaff.grpc.internal.HeaderMetadata;
import org.jackstaff.grpc.internal.Serializer;
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

import static org.jackstaff.grpc.Command.*;

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

        private Class<?> type;
        private Method method;

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
            NettyChannelBuilder builder = NettyChannelBuilder.forAddress(cfg.getHost(), cfg.getPort());
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
            stubs.put(authority, new Stub<>(authority, builder.build(), Duration.ofSeconds(cfg.getDefaultUnaryTimeout())));
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

    @SuppressWarnings("unchecked, rawtypes")
    private Object walkThrough(Stub<?,?,?> prototype, MethodDescriptor descriptor, Object proxy,
                               Object[] args, List<Interceptor> interceptors) throws Exception{
        Stub stub = new Stub(prototype, descriptor);
        Context context =new Context(descriptor, args, proxy).stub(stub);
        Packet<?> packet = Utils.before(context, interceptors);
        if (!packet.isException()){
            try {
                packet = descriptor.isV2() ? v2StubCall(context, stub) : v1StubCall(context, stub);
            }catch (StatusRuntimeException ex){
                packet = Packet.throwable(new StatusException(ex));
            }catch (Exception ex){
                packet = Packet.throwable(ex);
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
            case Unary: {
                stub.attachDefaultDeadline();
                return Packet.ok(stub.blockingUnary((ReqT) args[0]));
            }
            case AsynchronousUnary: {
                MessageChannel<RespT> respChannel = MessageChannel.build((Consumer<RespT>) args[1]).unary().setV2(true).ready();
                stub.attachDeadline(respChannel.getTimeout());
                ChannelObserver<RespT> respObserver = new ChannelObserver<>(respChannel);
                stub.asyncUnary((ReqT)args[0], respObserver);
                return new Packet<>();
            }
            case UnaryServerStreaming: {
                List<RespT> list = new ArrayList<>();
                stub.attachDefaultDeadline();
                stub.blockingServerStreaming((ReqT)args[0]).forEachRemaining(list::add);
                return Packet.ok(list);
            }
            case ServerStreaming: {//timeout
                MessageChannel<RespT> respChannel = MessageChannel.build((Consumer<RespT>) args[1]).setV2(true).ready();
                stub.attachDeadline(respChannel.getTimeout());
                ChannelObserver<RespT> respObserver = new ChannelObserver<>(respChannel);
                stub.asyncServerStreaming((ReqT)args[0], respObserver);
                return new Packet<>();
            }
            case ClientStreaming: {
                MessageChannel<RespT> respChannel = MessageChannel.build((Consumer<RespT>) args[0]).setV2(true).unary();
                stub.attachDeadline(respChannel.getTimeout());
                ChannelObserver<RespT> respObserver = new ChannelObserver<>(respChannel);
                StreamObserver<ReqT> reqObserver = stub.asyncClientStreaming(respObserver);
                MessageChannel<ReqT> reqChannel = new MessageChannel<>(reqObserver, reqObserver::onNext, -1);
                respChannel.link(reqChannel);
                return Packet.ok(reqChannel);
            }
            case BidiStreaming: {
                MessageChannel<RespT> respChannel = MessageChannel.build((Consumer<RespT>) args[0]).setV2(true).ready();
                stub.attachDeadline(respChannel.getTimeout());
                ChannelObserver<RespT> respObserver = new ChannelObserver<>(respChannel);
                StreamObserver<ReqT> reqObserver = stub.asyncBidiStreaming(respObserver);
                MessageChannel<ReqT> reqChannel = new MessageChannel<>(reqObserver, reqObserver::onNext, -1);
                respChannel.link(reqChannel);
                return Packet.ok(reqChannel);
            }
            default:
                return new Packet<>();
        }
    }

    private Packet<?> v1StubCall(Context context, Stub<?,Packet<?>,Packet<?>> stub) {
        MethodDescriptor descriptor = context.getMethodDescriptor();
        switch (descriptor.getMethodType()) {
            case Unary: {
                stub.attach(HeaderMetadata.ROOT, descriptor.getSign());
                stub.attachDefaultDeadline();
                return stub.blockingUnary(Packet.boxing(context.getArguments()));
            }
            case AsynchronousUnary: {
                MethodDescriptor peer = descriptor.getPeer();
                stub.attach(HeaderMetadata.ROOT, peer.getSign());
                Object[] args = Arrays.copyOf(context.getArguments(), peer.getMethod().getParameterCount()); //-1
                MessageChannel<?> respChannel = descriptor.getChannel(context.getArguments()).unary().ready();
                MessageObserver respObserver = new MessageObserver(respChannel);
                stub.asyncUnary(Packet.boxing((int) respChannel.getTimeout().toMillis(), args), respObserver);
                return new Packet<>();
            }
            case UnaryServerStreaming: {
                MethodDescriptor peer = descriptor.getPeer();
                Object[] args = Arrays.copyOf(context.getArguments(), peer.getMethod().getParameterCount()); //+1
                stub.attach(HeaderMetadata.ROOT, peer.getSign());
                stub.attachDefaultDeadline();
                List<Packet<?>> list = new ArrayList<>();
                Iterator<Packet<?>> iter=stub.blockingServerStreaming(Packet.boxing(args));
                while (iter.hasNext()) {
                    Packet<?> pack = iter.next();
                    switch (pack.getCommand()) {
                        case MESSAGE:
                            list.add(pack);
                            break;
                        case UNREACHABLE:
                        case EXCEPTION:
                            throw (RuntimeException)pack.getPayload();
                        case TIMEOUT:
                            throw new StatusException(TIMEOUT);
                        case COMPLETED:
                            break;
                    }
                }
                return Packet.ok(list);
            }
            case ServerStreaming: {
                stub.attach(HeaderMetadata.ROOT, descriptor.getSign());
                MessageChannel<?> respChannel = descriptor.getChannel(context.getArguments());
                MessageObserver respObserver = new MessageObserver(respChannel);
                stub.asyncServerStreaming(Packet.boxing((int) respChannel.getTimeout().toMillis(), context.getArguments()), respObserver);
                return new Packet<>();
            }
            case VoidClientStreaming:
            case ClientStreaming: {
                stub.attach(HeaderMetadata.ROOT, descriptor.getSign());
                MessageChannel<?> respChannel = descriptor.getChannel(context.getArguments());
                stub.attach(HeaderMetadata.BINARY_ROOT, Serializer.toBinary(Packet.boxing((int) respChannel.getTimeout().toMillis(), context.getArguments())));
                MessageObserver respObserver = new MessageObserver(respChannel);
                MessageChannel<?> reqChannel = new MessageChannel<>(stub.asyncClientStreaming(respObserver));
                respChannel.link(reqChannel);
                return Packet.ok(reqChannel);
            }
            case BidiStreaming: {
                stub.attach(HeaderMetadata.ROOT, descriptor.getSign());
                MessageChannel<?> respChannel = descriptor.getChannel(context.getArguments());
                stub.attach(HeaderMetadata.BINARY_ROOT, Serializer.toBinary(Packet.boxing((int) respChannel.getTimeout().toMillis(), context.getArguments())));
                MessageObserver respObserver = new MessageObserver(respChannel);
                MessageChannel<?> reqChannel = new MessageChannel<>(stub.asyncBidiStreaming(respObserver));
                respChannel.link(reqChannel);
                return Packet.ok(reqChannel);
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
