package org.jackstaff.grpc;

import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.internal.PacketServerGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author reco@jackstaff.org
 */
public class PacketServer extends PacketServerGrpc {

    Logger logger = LoggerFactory.getLogger(PacketServer.class);

    private final ApplicationContext appContext;
    private final Configuration configuration;
    private final Map<String, MethodInfo> methods = new ConcurrentHashMap<>();

    private io.grpc.Server grpcServer;

    public PacketServer(ApplicationContext appContext, Configuration configuration) {
        this.appContext = appContext;
        this.configuration = configuration;
    }

    public void initial() throws Exception {
        Optional.ofNullable(configuration.getServer()).ifPresent(cfg->{
            appContext.getBeansWithAnnotation(Server.class).values().forEach(bean->{
                Server server =bean.getClass().getAnnotation(Server.class);
                Class<?>[] services =Optional.of(server.value()).filter(a->a.length>0).orElse(server.service());
                if (services.length ==0){
                    throw new RuntimeException(bean.getClass().getName()+ "@Server service is empty");
                }
                Arrays.stream(services).forEach(type->register((Class<Object>)type, bean, Utils.getInterceptors(server.interceptor())));
            });
            this.start(cfg);
        });
    }

    <T> void register(Class<T> type, T bean, List<Interceptor> interceptors) {
        Arrays.stream(type.getMethods()).map(method -> new MethodInfo(bean, type, method, interceptors)).
                forEach(info -> {
                    if (methods.containsKey(info.getSign())){
                        throw new RuntimeException(info+" @Server duplicate ");
                    }
                    methods.put(info.getSign(), info);
                });
    }

    void start(Configuration.Server cfg){
        NettyServerBuilder builder = NettyServerBuilder.forPort(cfg.getPort()).addService(this);
        this.grpcServer = builder.build();
        try {
            logger.info("Packet Server Start at port {}", cfg.getPort());
            grpcServer.start();
        } catch (IOException e) {
            logger.error( " Packet Server Start fail ", e);
            e.printStackTrace();
        }
    }

    @Override
    protected void unaryImpl(Packet<?> packet, StreamObserver<Packet<?>> observer) {

    }

    @Override
    protected void serverStreamingImpl(Packet<?> packet, StreamObserver<Packet<?>> observer) {

    }

    @Override
    protected StreamObserver<Packet<?>> clientStreamingImpl(StreamObserver<Packet<?>> observer) {
        return null;
    }

    @Override
    protected StreamObserver<Packet<?>> bidiStreamingImpl(StreamObserver<Packet<?>> observer) {
        return null;
    }

}
