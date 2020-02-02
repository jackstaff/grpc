package org.jackstaff.grpc;

import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.jackstaff.grpc.internal.PacketStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.Proxy;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author reco@jackstaff.org
 */
public class PacketClient {

    Logger logger = LoggerFactory.getLogger(PacketClient.class);

    private Map<String, PacketStub> stubs = new ConcurrentHashMap<>();

    private final ApplicationContext appContext;
    private final Configuration configuration;
    private final Map<String, MethodInfo> methods = new ConcurrentHashMap<>();

    public PacketClient(ApplicationContext appContext, Configuration configuration) {
        this.appContext = appContext;
        this.configuration = configuration;
    }

    public void initial() throws Exception{
        Optional.ofNullable(configuration.getClient()).ifPresent(c->c.forEach(this::setup));
        Arrays.stream(appContext.getBeanDefinitionNames()).filter(appContext::isSingleton).
                map(appContext::getBean).forEach(bean->{
            Class<?> clz = bean.getClass();
            while (!clz.equals(Object.class)){
                Arrays.stream(clz.getDeclaredFields()).forEach(field -> {
                    Client client =field.getAnnotation(Client.class);
                    if (client ==null){
                        return;
                    }
                    String authority =Optional.of(client.value()).filter(a->a.length()>0).orElse(client.authority());
                    if (authority.isEmpty()){
                        throw new RuntimeException(field+"@Client value/authority is empty");
                    }
                    if (!field.getType().isInterface()){
                        throw new RuntimeException(field+"@Client field MUST be interface");
                    }
                    field.setAccessible(true);
                    try {
                        if (field.get(bean) ==null){
                            field.set(bean, autowired(authority, field.getType(), client.required(), Utils.getInterceptors(client.interceptor())));
                        }
                    }catch (Exception ex){
                        throw new RuntimeException("@Client field autowired fail", ex);
                    }
                });
                clz = clz.getSuperclass();
            }
        });
    }

    private void setup(String authority, Configuration.Client cfg){
        logger.info("Packet Client Setup, authority {}, host {}, port {}", authority, cfg.getHost(), cfg.getPort());
        NettyChannelBuilder builder = NettyChannelBuilder.forAddress(cfg.getHost(), cfg.getPort());
        stubs.put(authority, new PacketStub(authority, builder.build()));
    }

    public <T> T autowired(String authority, Class<T> type, boolean required, List<Interceptor> interceptors) {
        PacketStub stub = stubs.get(authority);
        if (stub ==null){
            if (required){
                throw new RuntimeException("client "+authority+" NOT found in configuration: spring.grpc.client..");
            }
            return null;
        }
        logger.info("Packet Client autowired authority: {}, type: {}, required: {}", authority, type.getName(), required);
        Object bean = Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, (proxy, method, args) -> {
            if (Object.class.equals(method.getDeclaringClass())){
                return method.invoke(proxy, args);
            }
            MethodInfo info = methods.computeIfAbsent(type+"/"+method, k-> new MethodInfo(type, method));
            switch (info.getMode()){
                case Unary:
                case BiStreaming:
                case ClientStreaming:
                case ServerStreaming:
                default:
                    return null;
            }
        });
        return (T)bean;
    }

}
