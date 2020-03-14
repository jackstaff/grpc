package org.jackstaff.grpc.internal;

import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.Packet;
import org.jackstaff.grpc.internal.InternalGrpc.InternalBlockingStub;
import org.jackstaff.grpc.internal.InternalGrpc.InternalStub;

import java.util.Iterator;

/**
 * @author reco@jackstaff.org
 */
public class PacketStub<S extends AbstractStub<S>> {

    private ManagedChannel channel;
    private InternalBlockingStub blockingStub;
    private InternalStub asyncStub;
    private S stub;

    public PacketStub(String authority, ManagedChannel channel){
        this.channel = channel;
        this.blockingStub = InternalGrpc.newBlockingStub(channel).build(channel, CallOptions.DEFAULT.withAuthority(authority));
        this.asyncStub = InternalGrpc.newStub(channel).build(channel, CallOptions.DEFAULT.withAuthority(authority));
    }

    public ManagedChannel getChannel() {
        return channel;
    }

    @SuppressWarnings("unchecked")
    public PacketStub(PacketStub<?> another, boolean block) {
        this.stub = (S)(block ? another.blockingStub : another.asyncStub);
    }

    public <T> void attach(HeaderMetadata<T> metadata, T value){
        stub = metadata.attach(stub, value);
    }

    public void attach(String name, String value) {
        stub = HeaderMetadata.attachString(stub, name, value);
    }

    public void attach(String name, byte[] value) {
        stub = HeaderMetadata.attachBinary(stub, name, value);
    }

    public Packet<?> unary(Packet<?> packet) {
        return Transform.fromProto(((InternalBlockingStub)stub).unary(Transform.buildProto(packet)));
    }

    public Iterator<Packet<?>> syncServerStreaming(Packet<?> packet) {
        return Transform.fromProtoIterator(((InternalBlockingStub)stub).serverStreaming(Transform.buildProto(packet)));
    }

    public void asynchronousUnary(Packet<?> packet) {
        ((InternalStub)stub).unary(Transform.buildProto(packet), Transform.buildProtoObserver(null));
    }

    public void unaryStreaming(Packet<?> packet, StreamObserver<Packet<?>> observer) {
        ((InternalStub)stub).unary(Transform.buildProto(packet), Transform.buildProtoObserver(observer));
    }

    public void serverStreaming(Packet<?> packet, StreamObserver<Packet<?>> observer) {
        ((InternalStub)stub).serverStreaming(Transform.buildProto(packet), Transform.buildProtoObserver(observer));
    }

    public StreamObserver<Packet<?>> clientStreaming(StreamObserver<Packet<?>> observer) {
        return Transform.fromProtoObserver(((InternalStub)stub).clientStreaming(Transform.buildProtoObserver(observer)));
    }

    public StreamObserver<Packet<?>> bidiStreaming(StreamObserver<Packet<?>> observer) {
        return Transform.fromProtoObserver(((InternalStub)stub).bidiStreaming(Transform.buildProtoObserver(observer)));
    }

}
