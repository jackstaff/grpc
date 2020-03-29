package org.jackstaff.grpc.internal;

import com.google.protobuf.ByteString;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.DefaultIdStrategy;
import io.protostuff.runtime.IdStrategy;
import io.protostuff.runtime.RuntimeSchema;
import org.jackstaff.grpc.Packet;
import org.jackstaff.grpc.Transform;
import org.jackstaff.grpc.Transforms;
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


    private static Packet<?> from(InternalProto.Packet proto) {
        try {
            return fromBinary(proto.getData().toByteArray());
        }catch (SerializationException ex){
            throw ex;
        }
        catch (Exception ex){
            throw new SerializationException("from Protobuf fail", ex);
        }
    }

    private static InternalProto.Packet build(Packet<?> packet) {
        try {
            return InternalProto.Packet.newBuilder().setData(ByteString.copyFrom(toBinary(packet))).build();
        }catch (SerializationException ex){
            throw ex;
        }
        catch (Exception ex){
            throw new SerializationException("build Protobuf fail", ex);
        }
    }

    static Transform<Packet<?>, InternalProto.Packet> getTransform(){
        return Transforms.getTransform(Packet.class);
    }

    static {
        Transforms.addTransform(Packet.class, InternalProto.Packet.class, Serializer::from, Serializer::build);
    }

}
