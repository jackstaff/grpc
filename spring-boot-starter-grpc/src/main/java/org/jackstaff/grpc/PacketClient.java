package org.jackstaff.grpc;

import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.jackstaff.grpc.annotation.Client;
import org.jackstaff.grpc.configuration.Configuration;
import org.jackstaff.grpc.exception.ValidationException;
import org.jackstaff.grpc.internal.HeaderMetadata;
import org.jackstaff.grpc.internal.PacketStub;
import org.jackstaff.grpc.internal.Transform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.Proxy;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author reco@jackstaff.org
 */
public class PacketClient {

    Logger logger = LoggerFactory.getLogger(PacketClient.class);

    private Map<String, PacketStub<?>> stubs = new ConcurrentHashMap<>();

    private final ApplicationContext appContext;
    private final Configuration configuration;
    private final Map<String, MethodDescriptor> methods = new ConcurrentHashMap<>();

    public PacketClient(ApplicationContext appContext, Configuration configuration) {
        this.appContext = appContext;
        this.configuration = configuration;
    }

    public void initial() throws Exception{
        Optional.ofNullable(configuration.getClient()).ifPresent(c->c.forEach(this::setup));
        appContext.getBeansWithAnnotation(Component.class).forEach((name, bean)->{
            Map<Field, Client> fields = clientFields(bean);
            fields.forEach((field, client)->autowired(name, bean, field, client));
        });
    }

    private Map<Field, Client> clientFields(Object bean){
        Map<Field, Client> fields = new HashMap<>();
        Class<?> clz = bean.getClass();
        while (!clz.equals(Object.class)){
            Arrays.stream(clz.getDeclaredFields()).forEach(field ->
                    Optional.ofNullable(field.getAnnotation(Client.class)).ifPresent(client -> fields.put(field, client)));
            clz = clz.getSuperclass();
        }
        return fields;
    }

    private void autowired(String name, Object bean, Field field, Client client) {
        String authority = Optional.of(client.authority()).filter(a->!a.isEmpty()).orElseGet(client::value);
        if (authority.isEmpty()){
            throw new ValidationException(field+"@Client value/authority is empty");
        }
        if (!field.getType().isInterface()){
            throw new ValidationException(field+"@Client field MUST be interface");
        }
        field.setAccessible(true);
        try {
            if (field.get(bean) ==null){
                Object value = autowired(authority, field.getType(), client.required(), Utils.getInterceptors(client.interceptor()));
                field.set(bean, value);
            }
        }catch (Throwable ex){
            throw new ValidationException("@Client field autowired fail", ex);
        }
    }

    private void setup(String authority, Configuration.Client cfg){
        logger.info("Packet Client Setup, authority {}, host {}, port {}", authority, cfg.getHost(), cfg.getPort());
        NettyChannelBuilder builder = NettyChannelBuilder.forAddress(cfg.getHost(), cfg.getPort());
        builder.usePlaintext();
        stubs.put(authority, new PacketStub<>(authority, builder.build()));
    }

    public <T> T autowired(String authority, Class<T> type, boolean required, List<Interceptor> interceptors) {
        PacketStub<?> packetStub = stubs.get(authority);
        if (packetStub ==null){
            if (required){
                throw new ValidationException("client "+authority+" NOT found in configuration: spring.grpc.client..");
            }
            return null;
        }
        logger.info("Packet Client autowired authority: {}, type: {}, required: {}", authority, type.getName(), required);
        Object bean = Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, (proxy, method, args) -> {
            if (Object.class.equals(method.getDeclaringClass())){
                try {
                    return method.invoke(proxy, args);
                }catch (Exception ex){
                    return null;
                }
            }
            MethodDescriptor info = methods.computeIfAbsent(type+"/"+method, k-> new MethodDescriptor(type, method));
            PacketStub<?> stub = new PacketStub<>(packetStub, info.getMode()==Mode.Unary);
            stub.attach(HeaderMetadata.ROOT, info.getSign());
            Context context =new Context(appContext, stub, info, args, proxy);
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

    private Packet<?> stubCall(Context context, PacketStub<?> stub) throws Exception {
        MethodDescriptor info = context.getMethodDescriptor();
        try {
            switch (info.getMode()){
                case Unary:
                    return stub.unary(Packet.boxing(context.getArguments()));
                case UnaryAsynchronous:
                    stub.asynchronousUnary(Packet.boxing(context.getArguments()));
                    return Packet.NULL();
                case UnaryStreaming:
                    stub.unaryStreaming(Packet.boxing(context.getArguments()),
                            new MessageObserver(info.getChannel(context.getArguments())));
                    return Packet.NULL();
                case ServerStreaming:
                    stub.serverStreaming(Packet.boxing(context.getArguments()),
                            new MessageObserver(info.getChannel(context.getArguments())));
                    return Packet.NULL();
                case ClientStreaming:
                    stub.attach(HeaderMetadata.BINARY_ROOT, Transform.toBinary(Packet.boxing(context.getArguments())));
                    MessageChannel<?> channel = new MessageChannel<>(t->{});
                    return Packet.ok(new MessageChannel<>(stub.clientStreaming(new MessageObserver(channel))).link(channel));
                case BiStreaming:
                    stub.attach(HeaderMetadata.BINARY_ROOT, Transform.toBinary(Packet.boxing(context.getArguments())));
                    MessageObserver observer = new MessageObserver(info.getChannel(context.getArguments()));
                    return Packet.ok(new MessageChannel<>(stub.bidiStreaming(observer)).link(observer.getChannel()));
                default:
                    return Packet.NULL();
            }
        }catch (Exception ex){
            return Packet.throwable(ex);
        }
    }


}
