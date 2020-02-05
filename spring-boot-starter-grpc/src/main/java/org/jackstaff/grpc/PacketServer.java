package org.jackstaff.grpc;

import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.configuration.Configuration;
import org.jackstaff.grpc.exception.ValidationException;
import org.jackstaff.grpc.internal.HeaderMetadata;
import org.jackstaff.grpc.internal.PacketServerGrpc;
import org.jackstaff.grpc.internal.Transform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author reco@jackstaff.org
 */
public class PacketServer extends PacketServerGrpc {

    Logger logger = LoggerFactory.getLogger(PacketServer.class);

    private final ApplicationContext appContext;
    private final Configuration configuration;
    private final Map<String, MethodDescriptor> methods = new ConcurrentHashMap<>();

    private io.grpc.Server grpcServer;

    public PacketServer(ApplicationContext appContext, Configuration configuration) {
        this.appContext = appContext;
        this.configuration = configuration;
    }

    public void initial() {
        Optional.ofNullable(configuration.getServer()).ifPresent(cfg->{
            appContext.getBeansWithAnnotation(org.jackstaff.grpc.annotation.Server.class).values().forEach(bean->{
                org.jackstaff.grpc.annotation.Server server =bean.getClass().getAnnotation(org.jackstaff.grpc.annotation.Server.class);
                Class<?>[] services =Optional.of(server.service()).filter(a->a.length>0).orElseGet(server::value);
                if (services.length ==0){
                    throw new ValidationException(bean.getClass().getName()+ "@Server service is empty");
                }
                Arrays.stream(services).forEach(type->register((Class<Object>)type, bean, Utils.getInterceptors(server.interceptor())));
            });
            this.start(cfg);
        });
    }

    public <T> void register(Class<T> type, T bean, List<Interceptor> interceptors) {
        if (!type.isAssignableFrom(bean.getClass())){
            throw new ValidationException(bean.getClass().getName()+ "@Server service is NOT match");
        }
        logger.info("Packet Server register(@Server)..{}, {}", type.getName(), bean.getClass().getName());
        Arrays.stream(type.getMethods()).map(method -> new MethodDescriptor(bean, type, method, interceptors)).
                forEach(info -> methods.put(info.getSign(), info));
    }

    public void start(Configuration.Server cfg){
        NettyServerBuilder builder = NettyServerBuilder.forPort(cfg.getPort()).addService(bindService(this));
        this.grpcServer = builder.build();
        try {
            logger.info("Packet Server Start at port {}", cfg.getPort());
            grpcServer.start();
        } catch (IOException e) {
            logger.error(" Packet Server Start fail ", e);
            e.printStackTrace();
        }
    }

    public void shutdown() {
        Optional.ofNullable(grpcServer).ifPresent(Server::shutdown);
    }

    private ServerServiceDefinition bindService(BindableService service){

        return ServerInterceptors.intercept(service, new ServerInterceptor(){
            @Override
            public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
                                                                         final Metadata headers, ServerCallHandler<ReqT, RespT> next) {
                return Contexts.interceptCall(HeaderMetadata.ROOT.capture(call.getAuthority(), call.getAttributes(), headers), call, headers, next);
            }
        });
    }

    private Context buildContext(Packet<?> packet){
        String sign = HeaderMetadata.ROOT.getValue();
        if (sign == null || sign.isEmpty()){
            throw new ValidationException("method sign Not found");
        }
        MethodDescriptor info = methods.get(sign);
        if (info ==null){
            throw new ValidationException("method Not found");
        }
        return new Context(appContext, info, packet.unboxing(), info.getBean());
    }

    @Override
    protected void unaryImpl(Packet<?> packet, StreamObserver<Packet<?>> observer) {
        try {
            Context context = buildContext(packet);
            MethodDescriptor info = context.getMethodDescriptor();
            if (info.getMode() == Mode.UnaryStreaming) {
                serverStreamingImpl(packet, observer);
                return;
            }
            Packet<?> result = Utils.walkThrough(context, info.getInterceptors());
            observer.onNext(result);
        }catch (Exception ex){
            observer.onNext(Packet.throwable(ex));
        }
        observer.onCompleted();
    }

    @Override
    protected void serverStreamingImpl(Packet<?> packet, StreamObserver<Packet<?>> observer) {
        try {
            Context context = buildContext(packet);
            MessageChannel<?> channel = new MessageChannel<>(observer);
            context.setChannel(channel);
            Packet<?> result = Utils.walkThrough(context, context.getMethodDescriptor().getInterceptors());
            if (result.isException()) {
                observer.onNext(result);
            }
        }catch (Exception ex){
            observer.onNext(Packet.throwable(ex));
            observer.onCompleted();
        }
    }

    @Override
    protected StreamObserver<Packet<?>> clientStreamingImpl(StreamObserver<Packet<?>> observer) {
        try {
            Packet<?> packet =Transform.fromBinary(HeaderMetadata.BINARY_ROOT.getValue());
            Context context = buildContext(packet);
            Packet<?> result = Utils.walkThrough(context, context.getMethodDescriptor().getInterceptors());
            if (!result.isException()) {
                Consumer<?> origin = (Consumer<?>) result.getPayload();
                MessageObserver messageObserver = new MessageObserver(origin);
                MessageChannel<?> channel = new MessageChannel<>(observer).link(messageObserver.getChannel());
                return messageObserver;
            }
            throw (Exception) result.getPayload();
        }catch (Exception ex){
            observer.onNext(Packet.throwable(ex));
            observer.onCompleted();
            return null;
        }
    }

    @Override
    protected StreamObserver<Packet<?>> bidiStreamingImpl(StreamObserver<Packet<?>> observer) {
        try {
            Packet<?> packet =Transform.fromBinary(HeaderMetadata.BINARY_ROOT.getValue());
            Context context = buildContext(packet);
            MessageChannel<?> channel = new MessageChannel<>(observer);
            context.setChannel(channel);
            Packet<?> result = Utils.walkThrough(context, context.getMethodDescriptor().getInterceptors());
            if (!result.isException()) {
                Consumer<?> origin = (Consumer<?>) result.getPayload();
                MessageObserver messageObserver = new MessageObserver(origin);
                channel.link(messageObserver.getChannel());
                return messageObserver;
            }
            throw (Exception) result.getPayload();
        }catch (Exception ex){
            observer.onNext(Packet.throwable(ex));
            observer.onCompleted();
            return null;
        }
    }

}
