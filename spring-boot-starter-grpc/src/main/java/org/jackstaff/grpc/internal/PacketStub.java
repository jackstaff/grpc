package org.jackstaff.grpc.internal;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.Packet;

import java.util.Iterator;

/**
 * @author reco@jackstaff.org
 */
public class PacketStub {

    private final String authority;
    private final Channel channel;
    private final InternalGrpc.InternalBlockingStub blockingStub;
    private final InternalGrpc.InternalStub asyncStub;

    public PacketStub(String authority, Channel channel){
        this.authority = authority;
        this.channel = channel;
        this.blockingStub = InternalGrpc.newBlockingStub(channel).build(channel, CallOptions.DEFAULT.withAuthority(authority));
        this.asyncStub = InternalGrpc.newStub(channel).build(channel, CallOptions.DEFAULT.withAuthority(authority));
    }

    public String getAuthority() {
        return authority;
    }

    public Channel getChannel() {
        return channel;
    }

    public Packet<?> syncUnary(Packet<?> packet) {
        return Transform.fromProto(blockingStub.unary(Transform.buildProto(packet)));
    }

    public Iterator<Packet<?>> syncServerStreaming(Packet<?> packet) {
        return Transform.fromProtoIterator(blockingStub.serverStreaming(Transform.buildProto(packet)));
    }

    public void unary(Packet<?> packet, StreamObserver<Packet<?>> observer) {
        asyncStub.unary(Transform.buildProto(packet), Transform.buildProtoObserver(observer));
    }

    public void serverStreaming(Packet<?> packet, StreamObserver<Packet<?>> observer) {
        asyncStub.serverStreaming(Transform.buildProto(packet), Transform.buildProtoObserver(observer));
    }

    public StreamObserver<Packet<?>> clientStreaming(StreamObserver<Packet<?>> observer) {
        return Transform.fromProtoObserver(asyncStub.clientStreaming(Transform.buildProtoObserver(observer)));
    }

    public StreamObserver<Packet<?>> bidiStreaming(StreamObserver<Packet<?>> observer) {
        return Transform.fromProtoObserver(asyncStub.bidiStreaming(Transform.buildProtoObserver(observer)));
    }

}
