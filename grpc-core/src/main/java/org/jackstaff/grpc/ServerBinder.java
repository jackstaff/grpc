package org.jackstaff.grpc;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServiceDescriptor;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.internal.HeaderMetadata;

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
                            observer.onError(throwable((Exception) result.getPayload()));
                            return;
                        }
                        Optional.ofNullable(result.getPayload()).map(respTransform::build).ifPresent(observer::onNext);
                        observer.onCompleted();
                    }catch (Throwable ex){
                        observer.onError(throwable(ex));
                    }
                    break;
                case ServerStreaming:
                    try {
                        StreamObserver<Object> pojoObserver= respTransform.fromObserver(observer);
                        MessageChannel<?> channel = new MessageChannel<>(observer, pojoObserver::onNext, HeaderMetadata.getTimeoutMillSeconds()).setV2(true).ready();
                        Object[] args = new Object[]{reqTransform.from(request), channel};
                        Context context = new Context(descriptor, args, descriptor.getBean());
                        Packet<?> result = Utils.walkThrough(context, descriptor.getInterceptors());
                        if (result.isException()) {
                            observer.onError(throwable((Exception) result.getPayload()));
                        }
                    }catch (Throwable ex){
                        observer.onError(throwable(ex));
                    }
                    break;
            }
        }

        @SuppressWarnings("unchecked")
        public StreamObserver<Req> invoke(StreamObserver<Resp> observer) {
            switch (descriptor.getMethodType()){
                case ClientStreaming:
                case BidiStreaming:
                    try {
                        StreamObserver<Object> pojoObserver= respTransform.fromObserver(observer);
                        MessageChannel<?> respChannel = new MessageChannel<>(observer, pojoObserver::onNext, HeaderMetadata.getTimeoutMillSeconds()).setV2(true).ready();
                        if (descriptor.getMethodType() == MethodType.ClientStreaming){
                            respChannel.unary();
                        }
                        Context context = new Context(descriptor, new Object[]{respChannel}, descriptor.getBean());
                        Packet<?> result = Utils.walkThrough(context, descriptor.getInterceptors());
                        if (result.isException()) {
                            observer.onError(throwable((Exception) result.getPayload()));
                            return null;
                        }
                        MessageChannel<Object> reqChannel = MessageChannel.build((Consumer<Object>)result.getPayload()).setV2(true);
                        reqChannel.link(respChannel);
                        return reqTransform.buildObserver(new ChannelObserver<>(reqChannel));
                    }catch (Exception ex){
                        observer.onError(throwable(ex));
                        return null;
                    }
                default:
                    throw new AssertionError("invalid method type "+descriptor.getMethod());
            }
        }
    }

    private Throwable throwable(Throwable ex){
        return Status.INTERNAL.withCause(ex).withDescription(ex.getMessage()).asRuntimeException();
    }


}
