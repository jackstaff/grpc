package org.jackstaff.grpc;

import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.configuration.ServerConfig;
import org.jackstaff.grpc.exception.ValidationException;
import org.jackstaff.grpc.internal.HeaderMetadata;
import org.jackstaff.grpc.internal.PacketServerGrpc;
import org.jackstaff.grpc.internal.Transform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author reco@jackstaff.org
 */
public class PacketServer {

    static final Logger logger = LoggerFactory.getLogger(PacketServer.class);

    private final Map<String, MethodDescriptor> methods = new ConcurrentHashMap<>();
    private Server server;

    public <T> void register(Class<T> type, T bean, List<Interceptor> interceptors) {
        if (!type.isInstance(bean)){
            throw new ValidationException(bean.getClass().getName()+ " does NOT instanceof "+type.getName());
        }
        logger.info("Packet Server register..{}, {}", type.getName(), bean.getClass().getName());
        Arrays.stream(type.getMethods()).map(method -> new MethodDescriptor(type, method, bean, interceptors)).
                forEach(info -> methods.put(info.getSign(), info));
    }

    public void start(ServerConfig cfg) {
        NettyServerBuilder builder = NettyServerBuilder.forPort(cfg.getPort()).addService(bindService(
                new PacketServerGrpc(this::unary, this::serverStreaming, this::clientStreaming, this::bidiStreaming)));
        if (cfg.getMaxInboundMessageSize() >512*1024){
            builder.maxInboundMessageSize(cfg.getMaxInboundMessageSize());
        }
        if (cfg.getKeepAliveTimeout() >30) {
            builder.keepAliveTimeout(cfg.getKeepAliveTimeout(), TimeUnit.SECONDS);
        }
        if (cfg.getKeepAliveTime() > 0){
            builder.keepAliveTime(cfg.getKeepAliveTime(), TimeUnit.SECONDS);
        }
        if (cfg.getPermitKeepAliveTime() > 30) {
            builder.permitKeepAliveTime(cfg.getPermitKeepAliveTime(), TimeUnit.SECONDS);
            builder.permitKeepAliveWithoutCalls(cfg.isPermitKeepAliveWithoutCalls());
        }
        if (cfg.getMaxInboundMetadataSize() > 10*1024){
            builder.maxInboundMetadataSize(cfg.getMaxInboundMetadataSize());
        }
        if (cfg.getMaxConnectionIdle() >0){
            builder.maxConnectionIdle(cfg.getMaxConnectionIdle(), TimeUnit.SECONDS);
        }
        if (cfg.getMaxConnectionAge() >0) {
            builder.maxConnectionAge(cfg.getMaxConnectionAge(), TimeUnit.SECONDS);
        }
        if (cfg.getMaxConnectionAgeGrace() >0) {
            builder.maxConnectionAgeGrace(cfg.getMaxConnectionAgeGrace(), TimeUnit.SECONDS);
        }
        this.server = builder.build();
        try {
            logger.info("Packet Server Start at port {}", cfg.getPort());
            server.start();
        } catch (Exception e) {
            logger.error("Packet Server Start fail ", e);
            e.printStackTrace();
        }
    }

    public void shutdown() {
        Optional.ofNullable(server).ifPresent(Server::shutdown);
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
        return new Context(info, packet.unboxing(), info.getBean());
    }

    void unary(Packet<?> packet, StreamObserver<Packet<?>> observer) {
        try {
            Context context = buildContext(packet);
            MethodDescriptor info = context.getMethodDescriptor();
            if (info.getMode() == Mode.UnaryStreaming) {
                MessageChannel<?> channel = new MessageChannel<>(observer);
                context.setChannel(channel);
            }
            Packet<?> result = Utils.walkThrough(context, info.getInterceptors());
            observer.onNext(result);
            if (info.getMode() == Mode.UnaryStreaming && !result.isException()) {
                return;
            }
        }catch (Exception ex){
            observer.onNext(Packet.throwable(ex));
        }
        observer.onCompleted();
    }

    void serverStreaming(Packet<?> packet, StreamObserver<Packet<?>> observer) {
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

    StreamObserver<Packet<?>> clientStreaming(StreamObserver<Packet<?>> observer) {
        try {
            Packet<?> packet =Transform.fromBinary(HeaderMetadata.BINARY_ROOT.getValue());
            Context context = buildContext(packet);
            Packet<?> result = Utils.walkThrough(context, context.getMethodDescriptor().getInterceptors());
            if (!result.isException()) {
                Consumer<?> origin = (Consumer<?>) result.getPayload();
                MessageObserver messageObserver = new MessageObserver(origin);
                messageObserver.getChannel().setObserver(observer);
                //MessageChannel<?> channel = new MessageChannel<>(observer).link(messageObserver.getChannel());
                return messageObserver;
            }
            throw (Exception) result.getPayload();
        }catch (Exception ex){
            observer.onNext(Packet.throwable(ex));
            observer.onCompleted();
            return null;
        }
    }

    StreamObserver<Packet<?>> bidiStreaming(StreamObserver<Packet<?>> observer) {
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
