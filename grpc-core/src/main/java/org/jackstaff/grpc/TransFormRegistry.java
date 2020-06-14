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

import com.google.protobuf.*;
import io.grpc.ServiceDescriptor;
import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.exception.ValidationException;
import org.jackstaff.grpc.internal.PropertyKind;

import java.lang.Enum;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * for code generator build/register POJO and Protocol java bean.
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

    public void doubleValue(Predicate<Pojo> pojoHas, Function<Pojo, Double> pojoGet, BiConsumer<Pojo, Double> pojoSet,
                                                  Predicate<Proto> protoHas, Function<Proto, DoubleValue> protoGet, BiConsumer<Builder, DoubleValue> builderSet){
        value(PropertyKind.DOUBLE_VALUE, pojoHas, pojoGet, pojoSet, protoHas, protoGet, builderSet);
    }

    public void floatValue(Predicate<Pojo> pojoHas, Function<Pojo, Float> pojoGet, BiConsumer<Pojo, Float> pojoSet,
                                               Predicate<Proto> protoHas, Function<Proto, FloatValue> protoGet, BiConsumer<Builder, FloatValue> builderSet){
        value(PropertyKind.FLOAT_VALUE, pojoHas, pojoGet, pojoSet, protoHas, protoGet, builderSet);
    }

    public void int32Value(Predicate<Pojo> pojoHas, Function<Pojo, Integer> pojoGet, BiConsumer<Pojo, Integer> pojoSet,
                           Predicate<Proto> protoHas, Function<Proto, Int32Value> protoGet, BiConsumer<Builder, Int32Value> builderSet){
        value(PropertyKind.INT_VALUE, pojoHas, pojoGet, pojoSet, protoHas, protoGet, builderSet);
    }

    public void int64Value(Predicate<Pojo> pojoHas, Function<Pojo, Long> pojoGet, BiConsumer<Pojo, Long> pojoSet,
                           Predicate<Proto> protoHas, Function<Proto, Int64Value> protoGet, BiConsumer<Builder, Int64Value> builderSet){
        value(PropertyKind.LONG_VALUE, pojoHas, pojoGet, pojoSet, protoHas, protoGet, builderSet);
    }

    public void uint32Value(Predicate<Pojo> pojoHas, Function<Pojo, Integer> pojoGet, BiConsumer<Pojo, Integer> pojoSet,
                                                   Predicate<Proto> protoHas, Function<Proto, UInt32Value> protoGet, BiConsumer<Builder, UInt32Value> builderSet){
        value(PropertyKind.U_INT_VALUE, pojoHas, pojoGet, pojoSet, protoHas, protoGet, builderSet);
    }

    public void uint64Value(Predicate<Pojo> pojoHas, Function<Pojo, Long> pojoGet, BiConsumer<Pojo, Long> pojoSet,
                                                Predicate<Proto> protoHas, Function<Proto, UInt64Value> protoGet, BiConsumer<Builder, UInt64Value> builderSet){
        value(PropertyKind.U_LONG_VALUE, pojoHas, pojoGet, pojoSet, protoHas, protoGet, builderSet);
   }

    public void boolValue(Predicate<Pojo> pojoHas, Function<Pojo, Boolean> pojoGet, BiConsumer<Pojo, Boolean> pojoSet,
                                               Predicate<Proto> protoHas, Function<Proto, BoolValue> protoGet, BiConsumer<Builder, BoolValue> builderSet){
        value(PropertyKind.BOOL_VALUE, pojoHas, pojoGet, pojoSet, protoHas, protoGet, builderSet);
    }

    public void duration(Predicate<Pojo> pojoHas, Function<Pojo, java.time.Duration> pojoGet, BiConsumer<Pojo, java.time.Duration> pojoSet,
                                       Predicate<Proto> protoHas, Function<Proto, Duration> protoGet, BiConsumer<Builder, Duration> builderSet){
        value(PropertyKind.DURATION, pojoHas, pojoGet, pojoSet, protoHas, protoGet, builderSet);
    }

    public void timestamp(Predicate<Pojo> pojoHas, Function<Pojo, java.sql.Timestamp> pojoGet, BiConsumer<Pojo, java.sql.Timestamp> pojoSet,
                                         Predicate<Proto> protoHas, Function<Proto, Timestamp> protoGet, BiConsumer<Builder, Timestamp> builderSet){
        value(PropertyKind.TIMESTAMP, pojoHas, pojoGet, pojoSet, protoHas, protoGet, builderSet);
    }

    public void stringValue(Predicate<Pojo> pojoHas, Function<Pojo, String> pojoGet, BiConsumer<Pojo, String> pojoSet,
                                                  Predicate<Proto> protoHas, Function<Proto, StringValue> protoGet, BiConsumer<Builder, StringValue> builderSet){
        value(PropertyKind.STRING_VALUE, pojoHas, pojoGet, pojoSet, protoHas, protoGet, builderSet);
    }

    public void bytesValue(Predicate<Pojo> pojoHas, Function<Pojo, byte[]> pojoGet, BiConsumer<Pojo, byte[]> pojoSet,
                                           Predicate<Proto> protoHas, Function<Proto, BytesValue> protoGet, BiConsumer<Builder, BytesValue> builderSet){
        value(PropertyKind.BYTES_VALUE, pojoHas, pojoGet, pojoSet, protoHas, protoGet, builderSet);
    }

    private <T, V> void value(PropertyKind kind, Predicate<Pojo> pojoHas, Function<Pojo, T> pojoGet, BiConsumer<Pojo, T> pojoSet,
                              Predicate<Proto> protoHas, Function<Proto, V> protoGet, BiConsumer<Builder, V> builderSet){
        Transform<T, V> transform = hookTransform(kind.rawType());
        mapping(pojoHas, protoHas, (pojo, proto)->pojoSet.accept(pojo, transform.from(protoGet.apply(proto))),
                (pojo, builder)->builderSet.accept(builder, transform.build(pojoGet.apply(pojo))));
    }

    public <T extends Enum<T>, P extends Enum<P>> void value(Class<T> enumType, Predicate<Pojo> pojoHas,
                                                               Function<Pojo, T> pojoGet, BiConsumer<Pojo, T> pojoSet,
                                                               Function<Proto, P> protoGet, BiConsumer<Builder, P> builderSet){
        Transform<T, P> transform = hookTransform(enumType);
        mapping(pojoHas, (pojo, proto)->pojoSet.accept(pojo, transform.from(protoGet.apply(proto))),
                (pojo, builder)->builderSet.accept(builder, transform.build(pojoGet.apply(pojo))));
    }

    public <T, V> void value(Class<T> messageType,
                               Predicate<Pojo> pojoHas, Function<Pojo, T> pojoGet, BiConsumer<Pojo, T> pojoSet,
                               Predicate<Proto> protoHas, Function<Proto, V> protoGet, BiConsumer<Builder, V> builderSet){
        Transform<T, V> transform = hookTransform(messageType);
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
        Transform<T, V> transform = hookTransform(elementType);
        froms.add((pojo, proto)-> Optional.ofNullable(protoGet.apply(proto)).ifPresent(list->
                pojoSet.accept(pojo, list.stream().map(transform::from).collect(Collectors.toList()))));
        builds.add((pojo, builder)->{
            if (pojoHas.test(pojo)){
                builderSet.accept(builder, pojoGet.apply(pojo).stream().map(transform::build).collect(Collectors.toList()));
            }
        });
    }


    public void doubleValues(Predicate<Pojo> pojoHas, Function<Pojo, List<Double>> pojoGet, BiConsumer<Pojo, List<Double>> pojoSet,
                            Function<Proto, List<DoubleValue>> protoGet, BiConsumer<Builder, Iterable<DoubleValue>> builderSet){
        list(PropertyKind.DOUBLE_VALUE, pojoHas, pojoGet, pojoSet, protoGet, builderSet);
    }

    public void floatValues(Predicate<Pojo> pojoHas, Function<Pojo, List<Float>> pojoGet, BiConsumer<Pojo, List<Float>> pojoSet,
                           Function<Proto, List<FloatValue>> protoGet, BiConsumer<Builder, Iterable<FloatValue>> builderSet){
        list(PropertyKind.FLOAT_VALUE, pojoHas, pojoGet, pojoSet, protoGet, builderSet);
    }

    public void int32Values(Predicate<Pojo> pojoHas, Function<Pojo, List<Integer>> pojoGet, BiConsumer<Pojo, List<Integer>> pojoSet,
                           Function<Proto, List<Int32Value>> protoGet, BiConsumer<Builder, Iterable<Int32Value>> builderSet){
        list(PropertyKind.INT_VALUE, pojoHas, pojoGet, pojoSet, protoGet, builderSet);
    }

    public void int64Values(Predicate<Pojo> pojoHas, Function<Pojo, List<Long>> pojoGet, BiConsumer<Pojo, List<Long>> pojoSet,
                           Function<Proto, List<Int64Value>> protoGet, BiConsumer<Builder, Iterable<Int64Value>> builderSet){
        list(PropertyKind.LONG_VALUE, pojoHas, pojoGet, pojoSet, protoGet, builderSet);
    }

    public void uint32Values(Predicate<Pojo> pojoHas, Function<Pojo, List<Integer>> pojoGet, BiConsumer<Pojo, List<Integer>> pojoSet,
                            Function<Proto, List<UInt32Value>> protoGet, BiConsumer<Builder, Iterable<UInt32Value>> builderSet){
        list(PropertyKind.U_INT_VALUE, pojoHas, pojoGet, pojoSet, protoGet, builderSet);
    }

    public void uint64Values(Predicate<Pojo> pojoHas, Function<Pojo, List<Long>> pojoGet, BiConsumer<Pojo, List<Long>> pojoSet,
                            Function<Proto, List<UInt64Value>> protoGet, BiConsumer<Builder, Iterable<UInt64Value>> builderSet){
        list(PropertyKind.U_LONG_VALUE, pojoHas, pojoGet, pojoSet, protoGet, builderSet);
    }

    public void boolValues(Predicate<Pojo> pojoHas, Function<Pojo, List<Boolean>> pojoGet, BiConsumer<Pojo, List<Boolean>> pojoSet,
                          Function<Proto, List<BoolValue>> protoGet, BiConsumer<Builder, Iterable<BoolValue>> builderSet){
        list(PropertyKind.BOOL_VALUE, pojoHas, pojoGet, pojoSet, protoGet, builderSet);
    }

    public void durations(Predicate<Pojo> pojoHas, Function<Pojo, List<java.time.Duration>> pojoGet, BiConsumer<Pojo, List<java.time.Duration>> pojoSet,
                         Function<Proto, List<Duration>> protoGet, BiConsumer<Builder, Iterable<Duration>> builderSet){
        list(PropertyKind.DURATION, pojoHas, pojoGet, pojoSet, protoGet, builderSet);
    }

    public void timestamps(Predicate<Pojo> pojoHas, Function<Pojo, List<java.sql.Timestamp>> pojoGet, BiConsumer<Pojo, List<java.sql.Timestamp>> pojoSet,
                          Function<Proto, List<Timestamp>> protoGet, BiConsumer<Builder, Iterable<Timestamp>> builderSet){
        list(PropertyKind.TIMESTAMP, pojoHas, pojoGet, pojoSet, protoGet, builderSet);
    }

    public void stringValues(Predicate<Pojo> pojoHas, Function<Pojo, List<String>> pojoGet, BiConsumer<Pojo, List<String>> pojoSet,
                            Function<Proto, List<StringValue>> protoGet, BiConsumer<Builder, Iterable<StringValue>> builderSet){
        list(PropertyKind.STRING_VALUE, pojoHas, pojoGet, pojoSet, protoGet, builderSet);
    }

    public void bytesValues(Predicate<Pojo> pojoHas,
                            Function<Pojo, List<byte[]>> pojoGet, BiConsumer<Pojo, List<byte[]>> pojoSet,
                            Function<Proto, List<BytesValue>> protoGet, BiConsumer<Builder, Iterable<BytesValue>> builderSet) {
        list(PropertyKind.BYTES_VALUE, pojoHas, pojoGet, pojoSet, protoGet, builderSet);
    }

    public void bytess(Predicate<Pojo> pojoHas,
                            Function<Pojo, List<byte[]>> pojoGet, BiConsumer<Pojo, List<byte[]>> pojoSet,
                            Function<Proto, List<ByteString>> protoGet, BiConsumer<Builder, Iterable<ByteString>> builderSet) {
        list(PropertyKind.BYTES, pojoHas, pojoGet, pojoSet, protoGet, builderSet);
    }

    private <T, V> void list(PropertyKind kind, Predicate<Pojo> pojoHas,
                             Function<Pojo, List<T>> pojoGet, BiConsumer<Pojo, List<T>> pojoSet,
                             Function<Proto, List<V>> protoGet, BiConsumer<Builder, Iterable<V>> builderSet){
        Transform<T, V> transform = hookTransform(kind.rawType());
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

    public static void dependency(String registryName){
        try {
            Class.forName(registryName);
        } catch (ClassNotFoundException e) {
            throw new ValidationException("dependency registry not found.", e);
        }
    }

    public static <Pojo, Proto> Transform<Pojo, Proto> getTransform(Class<?> type){
        return Transforms.getTransform(type);
    }

    private static <Pojo, Proto> Transform<Pojo, Proto> hookTransform(Class<?> type){
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
                transform = Transforms.getOrIdentityTransform(type);
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

        private OneOfCase<PojoE, ProtoE> mapping(PojoE enumerate, BiConsumer<Pojo, Proto> from, BiConsumer<Pojo, Builder> build){
            fromMap.put(enumerate.name(), from);
            buildMap.put(enumerate.name(), build);
            return this;
        }

        public OneOfCase<PojoE, ProtoE> bool(PojoE enumerate, Predicate<Pojo> pojoGet, ObjBoolConsumer<Pojo> pojoSet,
                         Predicate<Proto> protoGet, ObjBoolConsumer<Builder> builderSet) {
            return mapping(enumerate, (pojo, proto)-> pojoSet.accept(pojo, protoGet.test(proto)),
                    (pojo, builder)->builderSet.accept(builder, pojoGet.test(pojo)));
        }

        public OneOfCase<PojoE, ProtoE> int32(PojoE enumerate, ToIntFunction<Pojo> pojoGet, ObjIntConsumer<Pojo> pojoSet,
                          ToIntFunction<Proto> protoGet, ObjIntConsumer<Builder> builderSet) {
            return mapping(enumerate, (pojo, proto)->pojoSet.accept(pojo, protoGet.applyAsInt(proto)),
                    (pojo, builder)->builderSet.accept(builder, pojoGet.applyAsInt(pojo)));
        }

        public OneOfCase<PojoE, ProtoE> int64(PojoE enumerate, ToLongFunction<Pojo> pojoGet, ObjLongConsumer<Pojo> pojoSet,
                          ToLongFunction<Proto> protoGet, ObjLongConsumer<Builder> builderSet) {
            return mapping(enumerate, (pojo, proto)->pojoSet.accept(pojo, protoGet.applyAsLong(proto)),
                    (pojo, builder)->builderSet.accept(builder, pojoGet.applyAsLong(pojo)));
        }

        public OneOfCase<PojoE, ProtoE> float32(PojoE enumerate, ToFloatFunction<Pojo> pojoGet, ObjFloatConsumer<Pojo> pojoSet,
                            ToFloatFunction<Proto> protoGet, ObjFloatConsumer<Builder> builderSet) {
            return mapping(enumerate, (pojo, proto)->pojoSet.accept(pojo, protoGet.apply(proto)),
                    (pojo, builder)->builderSet.accept(builder, pojoGet.apply(pojo)));
        }

        public OneOfCase<PojoE, ProtoE> float64(PojoE enumerate, ToDoubleFunction<Pojo> pojoGet, ObjDoubleConsumer<Pojo> pojoSet,
                            ToDoubleFunction<Proto> protoGet, ObjDoubleConsumer<Builder> builderSet) {
            return mapping(enumerate, (pojo, proto)->pojoSet.accept(pojo, protoGet.applyAsDouble(proto)),
                    (pojo, builder)->builderSet.accept(builder, pojoGet.applyAsDouble(pojo)));
        }

        public OneOfCase<PojoE, ProtoE> string(PojoE enumerate, Function<Pojo, String> pojoGet, BiConsumer<Pojo, String> pojoSet,
                           Function<Proto, String> protoGet, BiConsumer<Builder, String> builderSet) {
            return mapping(enumerate, (pojo, proto)->pojoSet.accept(pojo, protoGet.apply(proto)),
                    (pojo, builder)->builderSet.accept(builder, pojoGet.apply(pojo)));
        }

        public OneOfCase<PojoE, ProtoE> bytes(PojoE enumerate, Function<Pojo, byte[]> pojoGet, BiConsumer<Pojo, byte[]> pojoSet,
                          Function<Proto, ByteString> protoGet, BiConsumer<Builder, ByteString> builderSet) {
            return mapping(enumerate, (pojo, proto)->pojoSet.accept(pojo, protoGet.apply(proto).toByteArray()),
                    (pojo, builder)->builderSet.accept(builder, ByteString.copyFrom(pojoGet.apply(pojo))));
        }


        public OneOfCase<PojoE, ProtoE> doubleValue(PojoE enumerate, Function<Pojo, Double> pojoGet, BiConsumer<Pojo, Double> pojoSet,
                                Function<Proto, DoubleValue> protoGet, BiConsumer<Builder, DoubleValue> builderSet){
            Transform<Double, DoubleValue> transform = hookTransform(DoubleValue.class);
            return mapping(enumerate, (pojo, proto)-> pojoSet.accept(pojo, transform.from(protoGet.apply(proto))),
                    (pojo, builder)->builderSet.accept(builder, transform.build(pojoGet.apply(pojo))));
        }

        public OneOfCase<PojoE, ProtoE> floatValue(PojoE enumerate, Function<Pojo, Float> pojoGet, BiConsumer<Pojo, Float> pojoSet,
                               Function<Proto, FloatValue> protoGet, BiConsumer<Builder, FloatValue> builderSet){
            Transform<Float, FloatValue> transform = hookTransform(FloatValue.class);
            return mapping(enumerate, (pojo, proto)-> pojoSet.accept(pojo, transform.from(protoGet.apply(proto))),
                    (pojo, builder)->builderSet.accept(builder, transform.build(pojoGet.apply(pojo))));
        }

        public OneOfCase<PojoE, ProtoE> int32Value(PojoE enumerate, Function<Pojo, Integer> pojoGet, BiConsumer<Pojo, Integer> pojoSet,
                               Function<Proto, Int32Value> protoGet, BiConsumer<Builder, Int32Value> builderSet){
            Transform<Integer, Int32Value> transform = hookTransform(Int32Value.class);
            return mapping(enumerate, (pojo, proto)-> pojoSet.accept(pojo, transform.from(protoGet.apply(proto))),
                    (pojo, builder)->builderSet.accept(builder, transform.build(pojoGet.apply(pojo))));
        }

        public OneOfCase<PojoE, ProtoE> int64Value(PojoE enumerate, Function<Pojo, Long> pojoGet, BiConsumer<Pojo, Long> pojoSet,
                               Function<Proto, Int64Value> protoGet, BiConsumer<Builder, Int64Value> builderSet){
            Transform<Long, Int64Value> transform = hookTransform(Int64Value.class);
            return mapping(enumerate, (pojo, proto)-> pojoSet.accept(pojo, transform.from(protoGet.apply(proto))),
                    (pojo, builder)->builderSet.accept(builder, transform.build(pojoGet.apply(pojo))));
        }

        public OneOfCase<PojoE, ProtoE> uint32Value(PojoE enumerate, Function<Pojo, Integer> pojoGet, BiConsumer<Pojo, Integer> pojoSet,
                                Function<Proto, UInt32Value> protoGet, BiConsumer<Builder, UInt32Value> builderSet){
            Transform<Integer, UInt32Value> transform = hookTransform(UInt32Value.class);
            return mapping(enumerate, (pojo, proto)-> pojoSet.accept(pojo, transform.from(protoGet.apply(proto))),
                    (pojo, builder)->builderSet.accept(builder, transform.build(pojoGet.apply(pojo))));
        }

        public OneOfCase<PojoE, ProtoE> uint64Value(PojoE enumerate, Function<Pojo, Long> pojoGet, BiConsumer<Pojo, Long> pojoSet,
                                Function<Proto, UInt64Value> protoGet, BiConsumer<Builder, UInt64Value> builderSet){
            Transform<Long, UInt64Value> transform = hookTransform(UInt64Value.class);
            return mapping(enumerate, (pojo, proto)-> pojoSet.accept(pojo, transform.from(protoGet.apply(proto))),
                    (pojo, builder)->builderSet.accept(builder, transform.build(pojoGet.apply(pojo))));
        }

        public OneOfCase<PojoE, ProtoE> boolValue(PojoE enumerate, Function<Pojo, Boolean> pojoGet, BiConsumer<Pojo, Boolean> pojoSet,
                              Function<Proto, BoolValue> protoGet, BiConsumer<Builder, BoolValue> builderSet){
            Transform<Boolean, BoolValue> transform = hookTransform(BoolValue.class);
            return mapping(enumerate, (pojo, proto)-> pojoSet.accept(pojo, transform.from(protoGet.apply(proto))),
                    (pojo, builder)->builderSet.accept(builder, transform.build(pojoGet.apply(pojo))));
        }

        public OneOfCase<PojoE, ProtoE> duration(PojoE enumerate, Function<Pojo, java.time.Duration> pojoGet, BiConsumer<Pojo, java.time.Duration> pojoSet,
                             Function<Proto, Duration> protoGet, BiConsumer<Builder, Duration> builderSet){
            Transform<java.time.Duration, Duration> transform = hookTransform(Duration.class);
            return mapping(enumerate, (pojo, proto)-> pojoSet.accept(pojo, transform.from(protoGet.apply(proto))),
                    (pojo, builder)->builderSet.accept(builder, transform.build(pojoGet.apply(pojo))));
        }

        public OneOfCase<PojoE, ProtoE> timestamp(PojoE enumerate, Function<Pojo, java.sql.Timestamp> pojoGet, BiConsumer<Pojo, java.sql.Timestamp> pojoSet,
                              Function<Proto, Timestamp> protoGet, BiConsumer<Builder, Timestamp> builderSet){
            Transform<java.sql.Timestamp, Timestamp> transform = hookTransform(Timestamp.class);
            return mapping(enumerate, (pojo, proto)-> pojoSet.accept(pojo, transform.from(protoGet.apply(proto))),
                    (pojo, builder)->builderSet.accept(builder, transform.build(pojoGet.apply(pojo))));
        }

        public OneOfCase<PojoE, ProtoE> stringValue(PojoE enumerate, Function<Pojo, String> pojoGet, BiConsumer<Pojo, String> pojoSet,
                                Function<Proto, StringValue> protoGet, BiConsumer<Builder, StringValue> builderSet){
            Transform<String, StringValue> transform = hookTransform(StringValue.class);
            return mapping(enumerate, (pojo, proto)-> pojoSet.accept(pojo, transform.from(protoGet.apply(proto))),
                    (pojo, builder)->builderSet.accept(builder, transform.build(pojoGet.apply(pojo))));
        }

        public OneOfCase<PojoE, ProtoE> bytesValue(PojoE enumerate, Function<Pojo, byte[]> pojoGet, BiConsumer<Pojo, byte[]> pojoSet,
                               Function<Proto, BytesValue> protoGet, BiConsumer<Builder, BytesValue> builderSet){
            Transform<byte[], BytesValue> transform = hookTransform(BytesValue.class);
            return mapping(enumerate, (pojo, proto)-> pojoSet.accept(pojo, transform.from(protoGet.apply(proto))),
                    (pojo, builder)->builderSet.accept(builder, transform.build(pojoGet.apply(pojo))));
        }

        //mapping for message and enum
        public <T, V> OneOfCase<PojoE, ProtoE> mapping(PojoE enumerate, Class<T> messageType,
                                                       Function<Pojo, T> pojoGet, BiConsumer<Pojo, T> pojoSet,
                                                       Function<Proto, V> protoGet, BiConsumer<Builder, V> builderSet){
            Transform<T, V> transform = hookTransform(messageType);
            return mapping(enumerate, (pojo, proto)-> pojoSet.accept(pojo, transform.from(protoGet.apply(proto))),
                    (pojo, builder)->builderSet.accept(builder, transform.build(pojoGet.apply(pojo))));
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
