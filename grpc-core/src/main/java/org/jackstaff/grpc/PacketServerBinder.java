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

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.internal.HeaderMetadata;
import org.jackstaff.grpc.internal.InternalGrpc;
import org.jackstaff.grpc.internal.InternalProto;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author reco@jackstaff.org
 */
class PacketServerBinder extends InternalGrpc.InternalImplBase {

    private final Map<String, MethodDescriptor> methods = new ConcurrentHashMap<>();

    public <T> void register(Class<T> type, T bean, List<Interceptor> interceptors) {
        List<MethodDescriptor> descriptors = Arrays.stream(type.getMethods()).
                map(method -> new MethodDescriptor(type, method, bean, interceptors)).collect(Collectors.toList());
        MethodDescriptor.validateProtocol(type, descriptors);
        descriptors.forEach(info -> methods.put(info.getSign(), info));
    }

    private Context buildContext(Packet<?> packet){
        String sign = HeaderMetadata.ROOT.getValue();
        if (sign == null || sign.isEmpty()){
            throw Status.INVALID_ARGUMENT.withDescription("method sign Not found:"+sign).asRuntimeException();
        }
        MethodDescriptor descriptor = methods.get(sign);
        if (descriptor ==null){
            throw Status.INVALID_ARGUMENT.withDescription("method Not found").asRuntimeException();
        }
        return new Context(descriptor, packet.unboxing(), descriptor.getBean());
    }

    @Override
    public void unary(InternalProto.Packet request, StreamObserver<InternalProto.Packet> observer) {
        Transform<Packet<?>, InternalProto.Packet> transform= Transforms.getTransform(Packet.class);
        try {
            Context context = buildContext(transform.from(request));
            MethodDescriptor descriptor = context.getMethodDescriptor();
            Packet<?> result = Utils.walkThrough(context, descriptor.getInterceptors());
            if (result.isException()) {
                observer.onError(Utils.throwable((Exception) result.getPayload()));
                return;
            }
            observer.onNext(transform.build(result));
            observer.onCompleted();
        }catch (Throwable ex){
            observer.onError(Utils.throwable(ex));
        }
    }

    @Override
    public void serverStreaming(InternalProto.Packet request, StreamObserver<InternalProto.Packet> observer) {
        Transform<Packet<?>, InternalProto.Packet> transform= Transforms.getTransform(Packet.class);
        MessageStream<?> respStream = new MessageStream<>(new MessageObserver<>(transform.fromObserver(observer)));
        try {
            Context context = buildContext(transform.from(request)).setStream(respStream);
            MethodDescriptor descriptor = context.getMethodDescriptor();
            Packet<?> result = Utils.walkThrough(context, descriptor.getInterceptors());
            if (result.isException()) {
                respStream.error(Utils.throwable((Exception) result.getPayload()));
            }
        }catch (Throwable ex){
            respStream.error(Utils.throwable(ex));
        }
    }

    @SuppressWarnings("unchecked")
    public StreamObserver<InternalProto.Packet> clientStreaming(StreamObserver<InternalProto.Packet> observer) {
        Transform<Packet<?>, InternalProto.Packet> transform= Transforms.getTransform(Packet.class);
        MessageStream<?> respStream = new MessageStream<>(new MessageObserver<>(transform.fromObserver(observer))).unary();
        try {
            Packet<?> packet = transform.from(InternalProto.Packet.newBuilder().setData(ByteString.copyFrom(HeaderMetadata.BINARY_ROOT.getValue())).build());
            Context context = buildContext(packet).setStream(respStream);
            MethodDescriptor descriptor = context.getMethodDescriptor();
            Packet<?> result = Utils.walkThrough(context, descriptor.getInterceptors());
            if (!result.isException()) {
                MessageStream<Packet<?>> reqStream = MessageStream.build((Consumer<Packet<?>>)result.getPayload()).link(respStream);
                return transform.buildObserver(reqStream.toPacketStreamObserver());
            }
            respStream.error(Utils.throwable((Exception) result.getPayload()));
        }catch (Exception ex){
            respStream.error(Utils.throwable(ex));
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public StreamObserver<InternalProto.Packet> bidiStreaming(StreamObserver<InternalProto.Packet> observer) {
        Transform<Packet<?>, InternalProto.Packet> transform= Transforms.getTransform(Packet.class);
        MessageStream<?> respStream = new MessageStream<>(new MessageObserver<>(transform.fromObserver(observer)));
        try {
            Packet<?> packet = transform.from(InternalProto.Packet.newBuilder().setData(ByteString.copyFrom(HeaderMetadata.BINARY_ROOT.getValue())).build());
            Context context = buildContext(packet).setStream(respStream);
            MethodDescriptor descriptor = context.getMethodDescriptor();
            Packet<?> result = Utils.walkThrough(context, descriptor.getInterceptors());
            if (!result.isException()) {
                MessageStream<Packet<?>> reqStream = MessageStream.build((Consumer<Packet<?>>)result.getPayload()).link(respStream);
                return transform.buildObserver(reqStream.toPacketStreamObserver());
            }
            respStream.error(Utils.throwable((Exception) result.getPayload()));
        }catch (Exception ex){
            respStream.error(Utils.throwable(ex));
        }
        return null;
    }

}
