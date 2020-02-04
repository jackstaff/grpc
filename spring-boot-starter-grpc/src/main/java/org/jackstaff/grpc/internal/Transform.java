package org.jackstaff.grpc.internal;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.DefaultIdStrategy;
import io.protostuff.runtime.IdStrategy;
import io.protostuff.runtime.RuntimeSchema;
import org.jackstaff.grpc.Packet;
import org.jackstaff.grpc.exception.SerializationException;

import java.util.Iterator;
import java.util.Optional;

/**
 * @author reco@jackstaff.org
 */
public class Transform {

    private static final IdStrategy idStrategy= new DefaultIdStrategy(
            IdStrategy.DEFAULT_FLAGS | IdStrategy.ALLOW_NULL_ARRAY_ELEMENT |
            IdStrategy.MORPH_NON_FINAL_POJOS | IdStrategy.COLLECTION_SCHEMA_ON_REPEATED_FIELDS);

    @SuppressWarnings("rawtypes")
    private static final RuntimeSchema<Packet> schema= RuntimeSchema.createFrom(Packet.class, idStrategy);

    public static IdStrategy getIdStrategy() {
        return idStrategy;
    }

    static InternalProto.Packet buildProto(Packet<?> packet) {
        try {
            byte[] bytes = ProtostuffIOUtil.toByteArray(packet, schema, LinkedBuffer.allocate());
            return InternalProto.Packet.newBuilder().setData(ByteString.copyFrom(bytes)).build();
        }catch (Exception ex){
            throw new SerializationException("build Protobuf fail", ex);
        }
    }

    static <T> Packet<T> fromProto(InternalProto.Packet proto){
        try {
            Packet<T> packet = new Packet<>();
            ProtostuffIOUtil.mergeFrom(proto.getData().toByteArray(), packet, schema);
            return packet;
        }catch (Exception ex){
            throw new SerializationException("from Protobuf fail", ex);
        }
    }

    static StreamObserver<InternalProto.Packet> buildProtoObserver(StreamObserver<Packet<?>> observer){
        return new StreamObserver<InternalProto.Packet>(){

            @Override
            public void onNext(InternalProto.Packet value) {
                Optional.ofNullable(observer).ifPresent(o->o.onNext(fromProto(value)));
            }

            @Override
            public void onError(Throwable t) {
                Optional.ofNullable(observer).ifPresent(o->o.onError(t));
            }

            @Override
            public void onCompleted() {
                Optional.ofNullable(observer).ifPresent(StreamObserver::onCompleted);
            }

        };
    }

    static StreamObserver<Packet<?>> fromProtoObserver(StreamObserver<InternalProto.Packet> observer) {
        return new PacketObserver(observer);
    }

    static Iterator<Packet<?>> fromProtoIterator(Iterator<InternalProto.Packet> iterator){
        return new Iterator<Packet<?>>() {
            @Override
            public boolean hasNext() {
                return Optional.ofNullable(iterator).map(Iterator::hasNext).orElse(false);
            }

            @Override
            public Packet<?> next() {
                return Optional.ofNullable(iterator).map(it->fromProto(it.next())).orElse(null);
            }
        };
    }

}
