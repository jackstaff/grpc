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
import org.jackstaff.grpc.internal.Serializer;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author reco@jackstaff.org
 */
class Transforms {

    private static final Map<Class<?>, ServiceDescriptor> descriptors = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Transform<?,?>> transforms = new ConcurrentHashMap<>();

    static {
        addProtoTransform(Double.class, DoubleValue.class, DoubleValue::getValue, v->DoubleValue.newBuilder().setValue(v).build());
        addProtoTransform(Float.class, FloatValue.class, FloatValue::getValue, v->FloatValue.newBuilder().setValue(v).build());
        addProtoTransform(Integer.class, Int32Value.class, Int32Value::getValue, v->Int32Value.newBuilder().setValue(v).build());
        addProtoTransform(Long.class, Int64Value.class, Int64Value::getValue, v->Int64Value.newBuilder().setValue(v).build());
        addProtoTransform(Integer.class, UInt32Value.class, UInt32Value::getValue, v->UInt32Value.newBuilder().setValue(v).build());
        addProtoTransform(Long.class, UInt64Value.class, UInt64Value::getValue, v->UInt64Value.newBuilder().setValue(v).build());
        addProtoTransform(Boolean.class, BoolValue.class, BoolValue::getValue, v->BoolValue.newBuilder().setValue(v).build());
        addProtoTransform(String.class, StringValue.class, StringValue::getValue, v->StringValue.newBuilder().setValue(v).build());
        addProtoTransform(byte[].class, BytesValue.class,
                BytesValue::toByteArray, v->BytesValue.newBuilder().setValue(ByteString.copyFrom(v)).build());
        addProtoTransform(byte[].class, ByteString.class, ByteString::toByteArray, ByteString::copyFrom);
        addProtoTransform(java.sql.Timestamp.class, Timestamp.class,
                v->new java.sql.Timestamp(v.getSeconds()*1000+ v.getNanos()/1000000),
                v->Timestamp.newBuilder().setSeconds(v.getTime()/1000).setNanos(v.getNanos()).build());
        addProtoTransform(java.time.Duration.class, Duration.class,
                v->java.time.Duration.ofNanos(v.getNanos()+v.getSeconds()*1000_000_000L),
                v->Duration.newBuilder().setSeconds(v.getSeconds()).setNanos(v.getNano()).build());
    }

    public static String addProtocol(Class<?> protocol, ServiceDescriptor descriptor){
        descriptors.put(protocol, descriptor);
        return descriptor.getName();
    }

    public static ServiceDescriptor getServiceDescriptor(Class<?> protocol){
        ServiceDescriptor descriptor = descriptors.get(protocol);
        if (descriptor != null){
            return descriptor;
        }
        try {
            Class.forName(protocol.getName());
        }catch (Exception ignore){
        }
        return descriptors.get(protocol);
    }

    private static <Pojo, Proto> Transform<Pojo, Proto> addProtoTransform(Class<Pojo> pojoClass, Class<Proto> protoClass,
                                                  Function<Proto, Pojo> from, Function<Pojo, Proto> build){
        Transform<Pojo, Proto> builder = new TransformWrapper<>(pojoClass, protoClass, from, build);
        transforms.put(protoClass, builder);
        return builder;
    }

    public static <Pojo, Proto> Transform<Pojo, Proto> addTransform(Class<Pojo> pojoClass, Class<Proto> protoClass,
                                                  Function<Proto, Pojo> from, Function<Pojo, Proto> build){
        Transform<Pojo, Proto> tf = addProtoTransform(pojoClass, protoClass, from, build);
        transforms.put(pojoClass, tf);
        return tf;
    }

    static boolean hasTransform(Class<?> type){
        return Optional.ofNullable(transforms.get(type)).
                map(tf->!Objects.equals(tf, TransformWrapper.identity)).orElse(false);
    }

    static Transform<?, ?> getPacketTransform(){
        if (!hasTransform(Packet.class)){
            Serializer.registerPacketTransForm();
        }
        return transforms.get(Packet.class);
    }

    @SuppressWarnings("unchecked")
    public static <Pojo, Proto> Transform<Pojo, Proto> getTransform(Class<?> type){
        return (Transform<Pojo, Proto>)transforms.computeIfAbsent(type, k-> TransformWrapper.identity);
    }


}
