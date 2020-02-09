package org.jackstaff.grpc.configuration;

import org.jackstaff.grpc.Interceptor;
import org.jackstaff.grpc.annotation.Client;
import org.jackstaff.grpc.annotation.Server;
import org.jackstaff.grpc.exception.ValidationException;
import org.springframework.cglib.proxy.Proxy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Spring Configuration for Jackstaff RPC
 * @author reco@jackstaff.org
 */
public class SpringConfiguration {

    private static final Map<Class<? extends Interceptor>, Interceptor> interceptors = new ConcurrentHashMap<>();

    private final ApplicationContext appContext;
    private final Configuration configuration;

    public SpringConfiguration(ApplicationContext appContext, Configuration configuration) {
        this.appContext = appContext;
        this.configuration = configuration;
    }

    public static Interceptor getInterceptor(Class<? extends Interceptor> clazz) {
        return interceptors.computeIfAbsent(clazz, c->{
            try {
                return clazz.getDeclaredConstructor().newInstance();
            }catch (Exception ex){
                throw new ValidationException(clazz+" can't newInstance", ex);
            }
        });
    }

    public static List<Interceptor> getInterceptors(Class<? extends Interceptor>[] types){
        return Arrays.stream(types).map(SpringConfiguration::getInterceptor).collect(Collectors.toList());
    }

    public org.jackstaff.grpc.Server newServer() throws Exception {
        org.jackstaff.grpc.Server server = new org.jackstaff.grpc.Server();
        Optional.ofNullable(configuration.getServer()).ifPresent(cfg -> {
            appContext.getBeansWithAnnotation(Server.class).values().forEach(bean -> {
                Server s = bean.getClass().getAnnotation(Server.class);
                Class<?>[] services = Optional.of(s.service()).filter(a -> a.length > 0).orElseGet(s::value);
                if (services.length == 0) {
                    throw new ValidationException(bean.getClass().getName() + "@Server service is empty");
                }
                Arrays.stream(services).forEach(type->server.register((Class)type, bean, SpringConfiguration.getInterceptors(s.interceptor())));
            });
            server.start(cfg);
            if (appContext instanceof ConfigurableApplicationContext){
                ConfigurableApplicationContext ctx = ((ConfigurableApplicationContext) appContext);
                ctx.addApplicationListener((ApplicationListener<ContextClosedEvent>) evt -> server.shutdown());
            }
        });
        return server;
    }

    public org.jackstaff.grpc.Client newClient() throws Exception {
        org.jackstaff.grpc.Client client = new org.jackstaff.grpc.Client((type, handler) ->
                Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, handler::invoke));
        Optional.ofNullable(configuration.getClient()).ifPresent(client::setup);
        appContext.getBeansWithAnnotation(Component.class).forEach((name, bean) -> inject(client, name, bean));
        if (appContext instanceof ConfigurableApplicationContext){
            ConfigurableApplicationContext ctx = ((ConfigurableApplicationContext) appContext);
            ctx.addApplicationListener((ApplicationListener<ContextClosedEvent>) evt -> client.shutdown());
        }
        return client;
    }

    private void inject(org.jackstaff.grpc.Client packetClient, String name, Object bean) {
        Map<Field, Client> fields = clientFields(bean);
        fields.forEach((field, client) -> inject(packetClient, name, bean, field, client));
    }

    private Map<Field, Client> clientFields(Object bean) {
        Map<Field, Client> fields = new HashMap<>();
        Class<?> clz = bean.getClass();
        while (!clz.equals(Object.class)) {
            Arrays.stream(clz.getDeclaredFields()).forEach(field ->
                    Optional.ofNullable(field.getAnnotation(Client.class)).ifPresent(client -> fields.put(field, client)));
            clz = clz.getSuperclass();
        }
        return fields;
    }

    private void inject(org.jackstaff.grpc.Client packetClient, String name, Object bean, Field field, Client client) {
        String authority = Optional.of(client.authority()).filter(a -> !a.isEmpty()).orElseGet(client::value);
        if (authority.isEmpty()) {
            throw new ValidationException(name + ":" + field.getName() + "@Client value/authority is empty");
        }
        if (!field.getType().isInterface()) {
            throw new ValidationException(name + ":" + field.getName() + "@Client field MUST be interface");
        }
        field.setAccessible(true);
        try {
            if (field.get(bean) == null) {
                Object value = packetClient.autowired(authority, field.getType(), client.required(), SpringConfiguration.getInterceptors(client.interceptor()));
                field.set(bean, value);
            }
        } catch (Throwable ex) {
            throw new ValidationException(name + ":" + field.getName() + "@Client autowired fail", ex);
        }
    }

}
