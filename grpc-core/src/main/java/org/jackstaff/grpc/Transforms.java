package org.jackstaff.grpc;

import io.grpc.ServiceDescriptor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Transforms {

    private static final Map<Class<?>, ServiceDescriptor> descriptors = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Transform<?,?>> transforms = new ConcurrentHashMap<>();

    public static String addProtocol(Class<?> protocol, ServiceDescriptor descriptor){
        descriptors.put(protocol, descriptor);
        return descriptor.getName();
    }

    public static ServiceDescriptor getServiceDescriptor(Class<?> protocol){
        return descriptors.get(protocol);
    }

    public static <Pojo, Proto> void addTransform(Class<Pojo> pojoClass, Class<Proto> protoClass,
                                                  Function<Proto, Pojo> from, Function<Pojo, Proto> build){
        Transform<Pojo, Proto> builder = new TransformBuilder<>(pojoClass, protoClass, from, build);
        transforms.put(pojoClass, builder);
        transforms.put(protoClass, builder);
    }

    @SuppressWarnings("unchecked")
    public static <Pojo, Proto> Transform<Pojo, Proto> getTransform(Class<?> type){
        Transform<Pojo, Proto> tf = (Transform<Pojo, Proto>)transforms.get(type);
        if (tf != null){
            return tf;
        }
        try {
            Class.forName(type.getName());
        }catch (Exception ignore){
        }
        return (Transform<Pojo, Proto>)transforms.computeIfAbsent(type, k->TransformBuilder.identity);
    }

    public static <T, V> void build(Supplier<V> get, Function<V, T> set) {
        Optional.ofNullable(get.get()).ifPresent(set::apply);
    }

    public static <T, V, R> void builds(Class<V> vClass, Supplier<Collection<V>> get, Function<Iterable<R>, T> set) {
        Optional.ofNullable(get.get()).ifPresent(list->{
            Transform<V, R> tf = getTransform(vClass);
            set.apply(list.stream().map(tf::build).collect(Collectors.toList()));
        });
    }

    public static <T, V, R> void build(Class<V> vClass, Supplier<V> get, Function<R, T> set) {
        Optional.ofNullable(get.get()).ifPresent(v->{
            Transform<V, R> tf = getTransform(vClass);
            set.apply(tf.build(v));
        });
    }

    public static <V, R> List<V> from(Class<R> rClass, Supplier<Collection<R>> get){
        return Optional.ofNullable(get.get()).map(list->{
            Transform<V, R> tf = getTransform(rClass);
            return list.stream().map(tf::from).collect(Collectors.toList());
        }).orElse(null);
    }

    public static <V, R> V from(Supplier<R> get){
        return Optional.ofNullable(get.get()).map(r->{
            Transform<V, R> tf = getTransform(r.getClass());
            return tf.from(r);
        }).orElse(null);
    }

}
