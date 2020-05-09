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

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServiceDescriptor;
import io.grpc.stub.StreamObserver;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.grpc.stub.ServerCalls.*;

/**
 * @author reco@jackstaff.org
 */
class ServerBinder<T> implements BindableService {

    private Class<T> type;
    private T target;
    private Map<String, MethodDescriptor> descriptors;
    private ServiceDescriptor serviceDescriptor;

    public ServerBinder(Class<T> type, T target, List<Interceptor> interceptors) {
        this.type = type;
        this.target = target;
        this.serviceDescriptor = Transforms.getServiceDescriptor(type);
        List<MethodDescriptor> methods = Arrays.stream(type.getMethods()).map(method -> new MethodDescriptor(type, method, target, interceptors)).collect(Collectors.toList());
        MethodDescriptor.validateProtocol(type, methods);
        Set<MethodType> types = new HashSet<>(Arrays.asList(MethodType.Unary, MethodType.ClientStreaming, MethodType.ServerStreaming, MethodType.BidiStreaming));
        this.descriptors = methods.stream().filter(desc->types.contains(desc.getMethodType())).
                collect(HashMap::new, (m,d)->m.put(d.getMethod().getName(), d), HashMap::putAll);
    }

    public String getName() {
        return serviceDescriptor.getName();
    }

    @SuppressWarnings("unchecked,rawtypes")
    public ServerServiceDefinition bindService() {
        ServerServiceDefinition.Builder builder =ServerServiceDefinition.builder(serviceDescriptor);
        for (io.grpc.MethodDescriptor<?, ?> desc: serviceDescriptor.getMethods()){
            String name = Optional.of(desc.getFullMethodName()).map(s->s.substring(s.lastIndexOf("/")+1)).get();
            String methodName = name.substring(0,1).toLowerCase()+ name.substring(1);
            MethodDescriptor info = descriptors.get(methodName);
            switch (info.getMethodType()){
                case Unary:
                    builder.addMethod(desc, asyncUnaryCall(new MethodHandler<>(info)));
                    break;
                case ServerStreaming:
                    builder.addMethod(desc, asyncServerStreamingCall(new MethodHandler<>(info)));
                    break;
                case ClientStreaming:
                    builder.addMethod(desc, asyncClientStreamingCall(new MethodHandler<>(info)));
                    break;
                case BidiStreaming:
                    builder.addMethod(desc, asyncBidiStreamingCall(new MethodHandler<>(info)));
                    break;
            }
        }
        return builder.build();
    }

    private class MethodHandler<Req, Resp> implements
            UnaryMethod<Req, Resp>,
            ServerStreamingMethod<Req, Resp>,
            ClientStreamingMethod<Req, Resp>,
            BidiStreamingMethod<Req, Resp> {

        private final MethodDescriptor descriptor;
        private final Transform<Object, Req> reqTransform;
        private final Transform<Object, Resp> respTransform;

        public MethodHandler(MethodDescriptor descriptor) {
            this.descriptor = descriptor;
            this.reqTransform = descriptor.requestTransform();
            this.respTransform = descriptor.responseTransform();
        }

        @SuppressWarnings("unchecked")
        public void invoke(Req request, StreamObserver<Resp> observer) {
            switch (descriptor.getMethodType()){
                case Unary:
                    try {
                        Object[] args = new Object[]{reqTransform.from(request)};
                        Context context = new Context(descriptor, args, descriptor.getBean());
                        Packet<?> result = Utils.walkThrough(context, descriptor.getInterceptors());
                        if (result.isException()) {
                            observer.onError(Utils.throwable((Exception) result.getPayload()));
                            return;
                        }
                        Optional.ofNullable(result.getPayload()).map(respTransform::build).ifPresent(observer::onNext);
                        observer.onCompleted();
                    }catch (Throwable ex){
                        observer.onError(Utils.throwable(ex));
                    }
                    break;
                case ServerStreaming:
                    MessageStream<?> respStream = new MessageStream<>(respTransform.fromObserver(observer));
                    try {
                        Object[] args = new Object[]{reqTransform.from(request), respStream};
                        Context context = new Context(descriptor, args, descriptor.getBean());
                        Packet<?> result = Utils.walkThrough(context, descriptor.getInterceptors());
                        if (result.isException()) {
                            respStream.error(Utils.throwable((Exception) result.getPayload()));
                        }
                    }catch (Throwable ex){
                        respStream.error(Utils.throwable(ex));
                    }
                    break;
            }
        }

        @SuppressWarnings("unchecked")
        public StreamObserver<Req> invoke(StreamObserver<Resp> observer) {
            switch (descriptor.getMethodType()){
                case ClientStreaming:
                case BidiStreaming:
                    MessageStream<?> respStream = new MessageStream<>(respTransform.fromObserver(observer));
                    try {
                        if (descriptor.getMethodType() == MethodType.ClientStreaming){
                            respStream.unary();
                        }
                        Context context = new Context(descriptor, new Object[]{respStream}, descriptor.getBean());
                        Packet<?> result = Utils.walkThrough(context, descriptor.getInterceptors());
                        if (!result.isException()) {
                            MessageStream<Object> reqStream = MessageStream.build((Consumer<Object>)result.getPayload()).link(respStream);
                            return reqTransform.buildObserver(reqStream.toStreamObserver());
                        }
                        respStream.error(Utils.throwable((Exception) result.getPayload()));
                    }catch (Exception ex){
                        respStream.error(Utils.throwable(ex));
                    }
                    return null;
                default:
                    throw new AssertionError("invalid method type "+descriptor.getMethod());
            }
        }
    }


}
