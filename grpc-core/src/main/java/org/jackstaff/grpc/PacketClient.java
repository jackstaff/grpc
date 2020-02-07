package org.jackstaff.grpc;

import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.jackstaff.grpc.configuration.ClientConfig;
import org.jackstaff.grpc.exception.ValidationException;
import org.jackstaff.grpc.internal.HeaderMetadata;
import org.jackstaff.grpc.internal.PacketStub;
import org.jackstaff.grpc.internal.Transform;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author reco@jackstaff.org
 */
public class PacketClient {

    public interface ProxyCreator {
        Object newProxyInstance(Class<?> type, InvocationHandler handler);
    }

    private final Map<String, MethodDescriptor> methods = new ConcurrentHashMap<>();
    private final Map<String, PacketStub<?>> stubs = new ConcurrentHashMap<>();
    private final ProxyCreator creator;

    public PacketClient() {
        this((type, handler) -> Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, handler));
    }

    public PacketClient(ProxyCreator creator){
        this.creator = creator;
    }

    public void setup(Map<String, ClientConfig> authorityClients){
        authorityClients.forEach((authority, cfg)->{
            NettyChannelBuilder builder = NettyChannelBuilder.forAddress(cfg.getHost(), cfg.getPort());
            if (cfg.getKeepAliveTime() > 0){
                builder.keepAliveTime(cfg.getKeepAliveTime(), TimeUnit.SECONDS);
                builder.keepAliveWithoutCalls(cfg.isKeepAliveWithoutCalls());
            }
            if (cfg.getIdleTimeout() > 30) {
                builder.idleTimeout(cfg.getIdleTimeout(), TimeUnit.SECONDS);
            }
            if (cfg.getKeepAliveTimeout() >30) {
                builder.keepAliveTimeout(cfg.getKeepAliveTimeout(), TimeUnit.SECONDS);
            }
            if (cfg.getMaxRetryAttempts()>0){
                builder.enableRetry().maxRetryAttempts(cfg.getMaxRetryAttempts());
            }
            if (cfg.isPlaintext()){
                builder.usePlaintext();
            }
            if (cfg.getMaxInboundMessageSize() >512*1024){
                builder.maxInboundMessageSize(cfg.getMaxInboundMessageSize());
            }
            stubs.put(authority, new PacketStub<>(authority, builder.build()));
        });
    }

    public <T> T autowired(String authority, Class<T> type, boolean required, List<Interceptor> interceptors) {
        PacketStub<?> packetStub = stubs.get(authority);
        if (packetStub ==null){
            if (required){
                throw new ValidationException("client "+authority+" NOT found in configuration: spring.grpc.client..");
            }
            return null;
        }
        Object bean = creator.newProxyInstance(type, (proxy, method, args) -> {
            if (method.getDeclaringClass().getName().equals("java.lang.Object")) {
                try {
                    return method.invoke(proxy, args);
                }catch (Exception ex){
                    return null;
                }
            }
            MethodDescriptor info = methods.computeIfAbsent(type+"/"+method, k-> new MethodDescriptor(type, method));
            PacketStub<?> stub = new PacketStub<>(packetStub, info.getMode()==Mode.Unary);
            stub.attach(HeaderMetadata.ROOT, info.getSign());
            Context context =new Context(info, args, proxy).stub(stub);
            Packet<?> packet = Utils.before(context, interceptors);
            if (!packet.isException()){
                packet = stubCall(context, stub);
                Utils.after(context, interceptors, packet);
            }
            if (packet.isException()){
                throw (Exception)packet.getPayload();
            }
            return packet.getPayload();
        });
        return (T)bean;
    }

    private Packet<?> stubCall(Context context, PacketStub<?> stub) {
        MethodDescriptor info = context.getMethodDescriptor();
        try {
            switch (info.getMode()){
                case Unary:
                    return stub.unary(Packet.boxing(context.getArguments()));
                case UnaryAsynchronous:
                    stub.asynchronousUnary(Packet.boxing(context.getArguments()));
                    return Packet.NULL();
                case UnaryStreaming:
                    MessageChannel<?> usChannel = info.getChannel(context.getArguments()).ready();
                    MessageObserver usObserver = new MessageObserver(usChannel);
                    stub.unaryStreaming(Packet.boxing(usChannel.getTimeoutSeconds(), context.getArguments()), usObserver);
                    return Packet.NULL();
                case ServerStreaming:
                    MessageChannel<?> ssChannel = info.getChannel(context.getArguments()).ready();
                    MessageObserver ssObserver = new MessageObserver(ssChannel);
                    stub.serverStreaming(Packet.boxing(ssChannel.getTimeoutSeconds(), context.getArguments()), ssObserver);
                    return Packet.NULL();
                case ClientStreaming:
                    stub.attach(HeaderMetadata.BINARY_ROOT, Transform.toBinary(Packet.boxing(context.getArguments())));
                    MessageChannel<?> channel = new MessageChannel<>(t->{});
                    return Packet.ok(new MessageChannel<>(stub.clientStreaming(new MessageObserver(channel))).link(channel));
                case BiStreaming:
                    MessageChannel<?> bsChannel = info.getChannel(context.getArguments()).ready();
                    stub.attach(HeaderMetadata.BINARY_ROOT, Transform.toBinary(Packet.boxing(bsChannel.getTimeoutSeconds(), context.getArguments())));
                    MessageObserver bsObserver = new MessageObserver(bsChannel);
                    return Packet.ok(new MessageChannel<>(stub.bidiStreaming(bsObserver)).link(bsChannel));
                default:
                    return Packet.NULL();
            }
        }catch (Exception ex){
            return Packet.throwable(ex);
        }
    }


}
