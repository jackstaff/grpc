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

package org.jackstaff.grpc.internal;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.DefaultIdStrategy;
import io.protostuff.runtime.IdStrategy;
import io.protostuff.runtime.RuntimeSchema;
import org.jackstaff.grpc.Packet;
import org.jackstaff.grpc.Status;
import org.jackstaff.grpc.TransFormRegistry;

/**
 * @author reco@jackstaff.org
 */
public class Serializer {

    private static final IdStrategy idStrategy= new DefaultIdStrategy(
            IdStrategy.DEFAULT_FLAGS | IdStrategy.ALLOW_NULL_ARRAY_ELEMENT |
                    IdStrategy.MORPH_NON_FINAL_POJOS | IdStrategy.COLLECTION_SCHEMA_ON_REPEATED_FIELDS);

    @SuppressWarnings("rawtypes")
    private static final RuntimeSchema<Packet> schema= RuntimeSchema.createFrom(Packet.class, idStrategy);

    public static IdStrategy getIdStrategy() {
        return idStrategy;
    }

    public static byte[] toBinary(Packet<?> packet){
        try {
            return ProtostuffIOUtil.toByteArray(packet, schema, LinkedBuffer.allocate());
        }catch (Exception ex){
            throw Status.DATA_LOSS.withDescription("Packet toBinary fail").asRuntimeException();
        }
    }

    public static <T> Packet<T> fromBinary(byte[] bytes){
        try {
            Packet<T> packet = new Packet<>();
            ProtostuffIOUtil.mergeFrom(bytes, packet, schema);
            return packet;
        }catch (Exception ex){
            throw Status.DATA_LOSS.withDescription("Packet fromBinary fail").asRuntimeException();
        }
    }

    @SuppressWarnings("rawtypes")
    public static void registerPacketTransForm() {
        TransFormRegistry<Packet, InternalProto.Packet, InternalProto.Packet.Builder> registry =
                new TransFormRegistry<>(Packet.class, Packet::new, InternalProto.Packet.class,
                InternalProto.Packet.Builder::build, InternalProto.Packet::newBuilder);
        registry.bytes(t->true, Serializer::toBinary,
                (packet, bytes) -> ProtostuffIOUtil.mergeFrom(bytes, packet, schema),
                InternalProto.Packet::getData, InternalProto.Packet.Builder::setData);
        registry.register();
    }

}
