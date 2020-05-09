/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jackstaff.grpc;

import com.google.protobuf.ByteString;
import io.grpc.ServiceDescriptor;
import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.internal.PropertyKind;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 *
 * @author reco@jackstaff.org
 */
public class TransFormRegistry<Pojo, Proto, Builder> {

    private final Class<Pojo> pojoType;
    private final Supplier<Pojo> pojoCreator;
    private final Class<Proto> protoType;
    private final Function<Builder, Proto> protoCreator;
    private final Supplier<Builder> builderCreator;

    private final List<BiConsumer<Pojo, Proto>> froms = new ArrayList<>();
    private final List<BiConsumer<Pojo, Builder>> builds = new ArrayList<>();

    public TransFormRegistry(Class<Pojo> pojoType, Supplier<Pojo> pojoCreator,
                             Class<Proto> protoType, Function<Builder, Proto> protoCreator,
                             Supplier<Builder> builderCreator) {
        this.pojoType = pojoType;
        this.pojoCreator = pojoCreator;
        this.protoType = protoType;
        this.protoCreator = protoCreator;
        this.builderCreator = builderCreator;
    }

    public void bool(Predicate<Pojo> pojoGet, ObjBoolConsumer<Pojo> pojoSet,
                     Predicate<Proto> protoGet, ObjBoolConsumer<Builder> builderSet) {
        mapping((pojo, proto)->pojoSet.accept(pojo, protoGet.test(proto)),
                (pojo, builder)->builderSet.accept(builder, pojoGet.test(pojo)));
    }

    public void int32(ToIntFunction<Pojo> pojoGet, ObjIntConsumer<Pojo> pojoSet,
                          ToIntFunction<Proto> protoGet, ObjIntConsumer<Builder> builderSet) {
        mapping((pojo, proto)->pojoSet.accept(pojo, protoGet.applyAsInt(proto)),
                (pojo, builder)->builderSet.accept(builder, pojoGet.applyAsInt(pojo)));
    }

    public void int64(ToLongFunction<Pojo> pojoGet, ObjLongConsumer<Pojo> pojoSet,
                    ToLongFunction<Proto> protoGet, ObjLongConsumer<Builder> builderSet) {
        mapping((pojo, proto)->pojoSet.accept(pojo, protoGet.applyAsLong(proto)),
                (pojo, builder)->builderSet.accept(builder, pojoGet.applyAsLong(pojo)));
    }

    public void float32(ToFloatFunction<Pojo> pojoGet, ObjFloatConsumer<Pojo> pojoSet,
                    ToFloatFunction<Proto> protoGet, ObjFloatConsumer<Builder> builderSet) {
        mapping((pojo, proto)->pojoSet.accept(pojo, protoGet.apply(proto)),
                (pojo, builder)->builderSet.accept(builder, pojoGet.apply(pojo)));
    }

    public void float64(ToDoubleFunction<Pojo> pojoGet, ObjDoubleConsumer<Pojo> pojoSet,
                    ToDoubleFunction<Proto> protoGet, ObjDoubleConsumer<Builder> builderSet) {
        mapping((pojo, proto)->pojoSet.accept(pojo, protoGet.applyAsDouble(proto)),
                (pojo, builder)->builderSet.accept(builder, pojoGet.applyAsDouble(pojo)));
    }

    public void string(Predicate<Pojo> pojoHas, Function<Pojo, String> pojoGet, BiConsumer<Pojo, String> pojoSet,
                       Function<Proto, String> protoGet, BiConsumer<Builder, String> builderSet) {
        mapping(pojoHas, (pojo, proto)->pojoSet.accept(pojo, protoGet.apply(proto)),
                (pojo, builder)->builderSet.accept(builder, pojoGet.apply(pojo)));
    }

    public void bytes(Predicate<Pojo> pojoHas, Function<Pojo, byte[]> pojoGet, BiConsumer<Pojo, byte[]> pojoSet,
                          Function<Proto, ByteString> protoGet, BiConsumer<Builder, ByteString> builderSet) {
        mapping(pojoHas, (pojo, proto)->pojoSet.accept(pojo, protoGet.apply(proto).toByteArray()),
                (pojo, builder)->builderSet.accept(builder, ByteString.copyFrom(pojoGet.apply(pojo))));
    }

    public <T, V> void value(PropertyKind kind, Predicate<Pojo> pojoHas, Function<Pojo, T> pojoGet, BiConsumer<Pojo, T> pojoSet,
                             Predicate<Proto> protoHas, Function<Proto, V> protoGet, BiConsumer<Builder, V> builderSet){
        Transform<T, V> transform = getTransform(kind.rawType());
        mapping(pojoHas, protoHas, (pojo, proto)->pojoSet.accept(pojo, transform.from(protoGet.apply(proto))),
                (pojo, builder)->builderSet.accept(builder, transform.build(pojoGet.apply(pojo))));
    }

    public <T extends Enum<T>, P extends Enum<P>> void value(Class<T> enumType, Predicate<Pojo> pojoHas,
                                                               Function<Pojo, T> pojoGet, BiConsumer<Pojo, T> pojoSet,
                                                               Function<Proto, P> protoGet, BiConsumer<Builder, P> builderSet){
        Transform<T, P> transform = getTransform(enumType);
        mapping(pojoHas, (pojo, proto)->pojoSet.accept(pojo, transform.from(protoGet.apply(proto))),
                (pojo, builder)->builderSet.accept(builder, transform.build(pojoGet.apply(pojo))));
    }

    public <T, V> void value(Class<T> messageType,
                               Predicate<Pojo> pojoHas, Function<Pojo, T> pojoGet, BiConsumer<Pojo, T> pojoSet,
                               Predicate<Proto> protoHas, Function<Proto, V> protoGet, BiConsumer<Builder, V> builderSet){
        Transform<T, V> transform = getTransform(messageType);
        mapping(pojoHas, protoHas, (pojo, proto)->pojoSet.accept(pojo, transform.from(protoGet.apply(proto))),
                (pojo, builder)->builderSet.accept(builder, transform.build(pojoGet.apply(pojo))));
    }

    // for primitive and string list
    public <T> void list(Predicate<Pojo> pojoHas, Function<Pojo, List<T>> pojoGet, BiConsumer<Pojo, List<T>> pojoSet,
                         Function<Proto, List<T>> protoGet, BiConsumer<Builder, Iterable<T>> builderSet){
        mapping(pojoHas, (pojo, proto)->pojoSet.accept(pojo, protoGet.apply(proto)),
                (pojo, builder)->builderSet.accept(builder, pojoGet.apply(pojo)));
    }

    // for message and enum list
    public <T, V> void list(Class<T> elementType, Predicate<Pojo> pojoHas,
                               Function<Pojo, List<T>> pojoGet, BiConsumer<Pojo, List<T>> pojoSet,
                               Function<Proto, List<V>> protoGet, BiConsumer<Builder, Iterable<V>> builderSet){
        Transform<T, V> transform = getTransform(elementType);
        froms.add((pojo, proto)-> Optional.ofNullable(protoGet.apply(proto)).ifPresent(list->
                pojoSet.accept(pojo, list.stream().map(transform::from).collect(Collectors.toList()))));
        builds.add((pojo, builder)->{
            if (pojoHas.test(pojo)){
                builderSet.accept(builder, pojoGet.apply(pojo).stream().map(transform::build).collect(Collectors.toList()));
            }
        });
    }

    // for wrapper list
    public <T, V> void list(PropertyKind kind, Predicate<Pojo> pojoHas,
                            Function<Pojo, List<T>> pojoGet, BiConsumer<Pojo, List<T>> pojoSet,
                            Function<Proto, List<V>> protoGet, BiConsumer<Builder, Iterable<V>> builderSet){
        Transform<T, V> transform = getTransform(kind.rawType());
        froms.add((pojo, proto)-> Optional.ofNullable(protoGet.apply(proto)).ifPresent(list->
                pojoSet.accept(pojo, list.stream().map(transform::from).collect(Collectors.toList()))));
        builds.add((pojo, builder)->{
            if (pojoHas.test(pojo)){
                builderSet.accept(builder, pojoGet.apply(pojo).stream().map(transform::build).collect(Collectors.toList()));
            }
        });
    }

    private void mapping(BiConsumer<Pojo, Proto> from, BiConsumer<Pojo, Builder> build){
        froms.add(from);
        builds.add(build);
    }

    private void mapping(Predicate<Pojo> pojoHas, BiConsumer<Pojo, Proto> from, BiConsumer<Pojo, Builder> build){
        mapping(pojoHas, t->true, from, build);
    }

    private void mapping(Predicate<Pojo> pojoHas, Predicate<Proto> protoHas, BiConsumer<Pojo, Proto> from, BiConsumer<Pojo, Builder> build){
        froms.add((Pojo pojo, Proto proto)-> {
            if (protoHas.test(proto)){
                from.accept(pojo, proto);
            }
        });
        builds.add((Pojo pojo, Builder builder)-> {
            if (pojoHas.test(pojo)){
                build.accept(pojo, builder);
            }
        });
    }

    public <PojoE extends Enum<PojoE>, ProtoE extends Enum<ProtoE>> OneOfCase<PojoE, ProtoE> oneOf(Class<PojoE> oneOfType, Function<Pojo, PojoE> pojoGet, Function<Proto, ProtoE> protoGet){
        return new OneOfCase<>(oneOfType, pojoGet, protoGet);
    }

    private Pojo from(Proto proto){
        Pojo pojo = pojoCreator.get();
        froms.forEach(f->f.accept(pojo, proto));
        return pojo;
    }

    private Proto build(Pojo pojo){
        Builder builder = builderCreator.get();
        builds.forEach(f->f.accept(pojo, builder));
        return protoCreator.apply(builder);
    }

    public void register(){
        Transforms.addTransform(pojoType, protoType, this::from, this::build);
    }

    //register enum transform
    public static <E extends Enum<E>, P extends Enum<P>> void register(Class<E> enumType, Class<P> protoEnumType){
        Transforms.addTransform(enumType, protoEnumType,
                t -> Enum.valueOf(enumType, t.name()),
                t -> Enum.valueOf(protoEnumType, t.name()));
    }

    public static String addProtocol(Class<?> protocol, ServiceDescriptor descriptor){
        return Transforms.addProtocol(protocol, descriptor);
    }

    private static <Pojo, Proto> Transform<Pojo, Proto> getTransform(Class<?> type){
        return Transforms.hasTransform(type) ? Transforms.getTransform(type) : new DeferredTransform<>(type);
    }

    private static class DeferredTransform<Pojo, Proto> implements Transform<Pojo, Proto> {

        private final Class<?> type;
        private Transform<Pojo, Proto> transform;

        public DeferredTransform(Class<?> type) {
            this.type = type;
        }

        private Transform<Pojo, Proto> get() {
            if (transform == null){
                transform = Transforms.getTransform(type);
            }
            return transform;
        }

        public Pojo from(Proto proto) {
            return get().from(proto);
        }

        public Proto build(Pojo pojo) {
            return get().build(pojo);
        }

        @Override
        public StreamObserver<Proto> buildObserver(StreamObserver<Pojo> observer) {
            return get().buildObserver(observer);
        }

        @Override
        public StreamObserver<Pojo> fromObserver(StreamObserver<Proto> observer) {
            return get().fromObserver(observer);
        }

        @Override
        public Iterator<Proto> buildIterator(Iterator<Pojo> iterator) {
            return get().buildIterator(iterator);
        }

        @Override
        public Iterator<Pojo> fromIterator(Iterator<Proto> iterator) {
            return get().fromIterator(iterator);
        }

    }

    public class OneOfCase<PojoE extends Enum<PojoE>, ProtoE extends Enum<ProtoE>> {

        private final Class<PojoE> oneOfType;
        private final Function<Pojo, PojoE> pojoGet;
        private final Function<Proto, ProtoE> protoGet;
        private final Map<String, BiConsumer<Pojo, Proto>> fromMap = new HashMap<>();
        private final Map<String, BiConsumer<Pojo, Builder>> buildMap = new HashMap<>();

        OneOfCase(Class<PojoE> oneOfType, Function<Pojo, PojoE> pojoGet, Function<Proto, ProtoE> protoGet) {
            this.oneOfType = oneOfType;
            this.pojoGet = pojoGet;
            this.protoGet = protoGet;
        }

        //mapping for primitive and string
        public <T> OneOfCase<PojoE, ProtoE> mapping(PojoE enumerate,
                                                    Function<Pojo, T> pojoGet, BiConsumer<Pojo, T> pojoSet,
                                                    Function<Proto, T> protoGet, BiConsumer<Builder, T> builderSet){
            fromMap.put(enumerate.name(), (pojo, proto)-> pojoSet.accept(pojo, protoGet.apply(proto)));
            buildMap.put(enumerate.name(), (pojo, builder)->builderSet.accept(builder, pojoGet.apply(pojo)));
            return this;
        }

        //mapping for message and enum
        public <T, V> OneOfCase<PojoE, ProtoE> mapping(PojoE enumerate, Class<T> messageType,
                                                       Function<Pojo, T> pojoGet, BiConsumer<Pojo, T> pojoSet,
                                                       Function<Proto, V> protoGet, BiConsumer<Builder, V> builderSet){
            Transform<T, V> transform = getTransform(messageType);
            fromMap.put(enumerate.name(), (pojo, proto)-> pojoSet.accept(pojo, transform.from(protoGet.apply(proto))));
            buildMap.put(enumerate.name(), (pojo, builder)->builderSet.accept(builder, transform.build(pojoGet.apply(pojo))));
            return this;
        }

        //mapping for wrapper
        public <T, V> OneOfCase<PojoE, ProtoE> mapping(PojoE enumerate, PropertyKind kind,
                                                       Function<Pojo, T> pojoGet, BiConsumer<Pojo, T> pojoSet,
                                                       Function<Proto, V> protoGet, BiConsumer<Builder, V> builderSet){
            Transform<T, V> transform = getTransform(kind.rawType());
            fromMap.put(enumerate.name(), (pojo, proto)-> pojoSet.accept(pojo, transform.from(protoGet.apply(proto))));
            buildMap.put(enumerate.name(), (pojo, builder)->builderSet.accept(builder, transform.build(pojoGet.apply(pojo))));
            return this;
        }

        private void from(Pojo pojo, Proto proto) {
            Optional.ofNullable(protoGet.apply(proto)).map(Enum::name).map(fromMap::get).
                    ifPresent(from->from.accept(pojo, proto));
        }

        private void build(Pojo pojo, Builder builder) {
            Optional.ofNullable(pojoGet.apply(pojo)).map(Enum::name).map(buildMap::get).
                    ifPresent(build->build.accept(pojo, builder));
        }

        public void build(){
            froms.add(this::from);
            builds.add(this::build);
        }

    }

    @FunctionalInterface
    public interface ObjBoolConsumer<T> {
        void accept(T t, boolean value);
    }

    @FunctionalInterface
    public interface ToFloatFunction<T> {
        float apply(T value);
    }

    @FunctionalInterface
    public interface ObjFloatConsumer<T> {
        void accept(T t, float value);
    }

}
