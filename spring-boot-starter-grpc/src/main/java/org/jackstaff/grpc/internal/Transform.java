package org.jackstaff.grpc.internal;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.DefaultIdStrategy;
import io.protostuff.runtime.IdStrategy;
import io.protostuff.runtime.RuntimeSchema;
import org.jackstaff.grpc.Packet;

import java.util.Iterator;
import java.util.Optional;

/**
 * @author reco@jackstaff.org
 */
public class Transform {

    private static final DefaultIdStrategy idStrategy= new DefaultIdStrategy(
            IdStrategy.DEFAULT_FLAGS | IdStrategy.ALLOW_NULL_ARRAY_ELEMENT |
            IdStrategy.MORPH_NON_FINAL_POJOS | IdStrategy.COLLECTION_SCHEMA_ON_REPEATED_FIELDS);

    @SuppressWarnings("rawtypes")
    private static final RuntimeSchema<Packet> schema= RuntimeSchema.createFrom(Packet.class, idStrategy);

    public static IdStrategy getIdStrategy() {
        return idStrategy;
    }

    static InternalProto.Packet buildProto(Packet<?> packet) {
        byte[] bytes = ProtostuffIOUtil.toByteArray(packet, schema, LinkedBuffer.allocate());
        return InternalProto.Packet.newBuilder().setData(ByteString.copyFrom(bytes)).build();
    }

    static <T> Packet<T> fromProto(InternalProto.Packet proto){
        Packet<T> packet = new Packet<>();
        ProtostuffIOUtil.mergeFrom(proto.getData().toByteArray(), packet, schema);
        return packet;
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
        return new StreamObserver<Packet<?>>(){

            @Override
            public void onNext(Packet<?> value) {
                observer.onNext(buildProto(value));
            }

            @Override
            public void onError(Throwable t) {
                observer.onError(t);
            }

            @Override
            public void onCompleted() {
                observer.onCompleted();
            }
        };
    }

    static Iterator<Packet<?>> fromProtoIterator(Iterator<InternalProto.Packet> iterator){
        return new Iterator<Packet<?>>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Packet<?> next() {
                return fromProto(iterator.next());
            }
        };
    }

}
