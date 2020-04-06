package org.jackstaff.grpc;

import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.annotation.Protocol;
import org.jackstaff.grpc.configuration.ServerConfig;
import org.jackstaff.grpc.exception.ValidationException;
import org.jackstaff.grpc.internal.HeaderMetadata;
import org.jackstaff.grpc.internal.PacketServerGrpc;
import org.jackstaff.grpc.internal.Serializer;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * the server
 * @author reco@jackstaff.org
 *
 * @see Interceptor
 * @see ServerConfig
 */
public class Server {

    private final Map<String, MethodDescriptor> methods = new ConcurrentHashMap<>();
    private final Map<String, ServerBinder<?>> binders = new ConcurrentHashMap<>();
    private io.grpc.Server server;

    /**
     * register a "protocol interface" type which implements by bean
     * @param type the "protocol interface"
     * @param bean the implements bean
     * @param interceptors the Interceptor list
     * @param <T> T
     */
    public <T> void register(Class<T> type, T bean, List<Interceptor> interceptors) {
        if (!type.isInstance(bean)){
            throw new ValidationException(bean.getClass().getName()+ " does NOT instanceof "+type.getName());
        }
        if (type.getAnnotation(Protocol.class) != null){//v2
            ServerBinder<T> binder = new ServerBinder<>(type, bean, interceptors);
            if (binders.containsKey(binder.getName())){
                throw new ValidationException(bean.getClass().getName()+ " duplicate bind "+type.getName());
            }
            binders.put(binder.getName(), binder);
            return;
        }
        List<MethodDescriptor> descriptors = Arrays.stream(type.getMethods()).
                map(method -> new MethodDescriptor(type, method, bean, interceptors)).collect(Collectors.toList());
        MethodDescriptor.validateProtocol(type, descriptors);
        descriptors.forEach(info -> methods.put(info.getSign(), info));
    }

    /**
     * start server
     * @param cfg server config
     */
    public void start(ServerConfig cfg) {
        NettyServerBuilder builder = NettyServerBuilder.forPort(cfg.getPort());
        binders.values().stream().map(this::bindService).forEach(builder::addService);
        if (!methods.isEmpty()) {
            builder.addService(bindService(new PacketServerGrpc(this::unary, this::serverStreaming, this::clientStreaming, this::bidiStreaming)));
        }
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
        try {
            if (cfg.getKyeCertChain() != null && cfg.getKyeCertChain().length()>0 && cfg.getPrivateKey() != null && cfg.getPrivateKey().length()>0) {
                SslContextBuilder sslContextBuilder = GrpcSslContexts.forServer(new File(cfg.getKyeCertChain()), new File(cfg.getPrivateKey()));
                builder.sslContext(sslContextBuilder.build());
            }
            this.server = builder.build();
            server.start();
        } catch (Exception e) {
            throw new ValidationException("Server Start fail ", e);
        }
    }

    /**
     * shutdown when application shutdown
     */
    public void shutdown() {
        Optional.ofNullable(server).ifPresent(io.grpc.Server::shutdown);
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
        String sign =HeaderMetadata.ROOT.getValue();
        if (sign == null || sign.isEmpty()){
            throw new ValidationException("method sign Not found:"+sign);
        }
        MethodDescriptor descriptor = methods.get(sign);
        if (descriptor ==null){
            throw new ValidationException("method Not found");
        }
        return new Context(descriptor, packet.unboxing(), descriptor.getBean());
    }

    private void unary(Packet<?> packet, StreamObserver<Packet<?>> observer) {
        try {
            Context context = buildContext(packet);
            MethodDescriptor descriptor = context.getMethodDescriptor();
            Packet<?> result = Utils.walkThrough(context, descriptor.getInterceptors());
            observer.onNext(result);
        }catch (Exception ex) {
            observer.onNext(Packet.throwable(ex));
        }
        observer.onCompleted();
    }

    private void serverStreaming(Packet<?> packet, StreamObserver<Packet<?>> observer) {
        try {
            Context context = buildContext(packet);
            MessageChannel<?> channel = new MessageChannel<>(observer, packet.getCommand()).ready();
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

    private StreamObserver<Packet<?>> clientStreaming(StreamObserver<Packet<?>> observer) {
        try {
            Packet<?> packet = Serializer.fromBinary(HeaderMetadata.BINARY_ROOT.getValue());
            Context context = buildContext(packet);
            MessageChannel<?> respChannel = new MessageChannel<>(observer, packet.getCommand()).unary().ready();
            context.setChannel(respChannel);
            Packet<?> result = Utils.walkThrough(context, context.getMethodDescriptor().getInterceptors());
            if (!result.isException()) {
                MessageChannel<?> reqChannel = MessageChannel.build((Consumer<?>)result.getPayload()).ready();
                return new MessageObserver(reqChannel.link(respChannel));
            }
            throw (Exception) result.getPayload();
        }catch (Exception ex){
            observer.onNext(Packet.throwable(ex));
            observer.onCompleted();
            return null;
        }
    }

    private StreamObserver<Packet<?>> bidiStreaming(StreamObserver<Packet<?>> observer) {
        try {
            Packet<?> packet =Serializer.fromBinary(HeaderMetadata.BINARY_ROOT.getValue());
            Context context = buildContext(packet);
            MessageChannel<?> respChannel = new MessageChannel<>(observer).ready();
            context.setChannel(respChannel);
            Packet<?> result = Utils.walkThrough(context, context.getMethodDescriptor().getInterceptors());
            if (!result.isException()) {
                MessageChannel<?> reqChannel = MessageChannel.build((Consumer<?>)result.getPayload()).ready();
                reqChannel.link(respChannel);
                return new MessageObserver(reqChannel);
            }
            throw (Exception) result.getPayload();
        }catch (Exception ex){
            observer.onNext(Packet.throwable(ex));
            observer.onCompleted();
            return null;
        }
    }

}
