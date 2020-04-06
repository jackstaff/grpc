package org.jackstaff.grpc.internal;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.DefaultIdStrategy;
import io.protostuff.runtime.IdStrategy;
import io.protostuff.runtime.RuntimeSchema;
import org.jackstaff.grpc.Packet;
import org.jackstaff.grpc.exception.SerializationException;

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
            throw new SerializationException("Packet toBinary fail", ex);
        }
    }

    public static <T> Packet<T> fromBinary(byte[] bytes){
        try {
            Packet<T> packet = new Packet<>();
            ProtostuffIOUtil.mergeFrom(bytes, packet, schema);
            return packet;
        }catch (Exception ex){
            throw new SerializationException("Packet fromBinary fail", ex);
        }
    }

}
