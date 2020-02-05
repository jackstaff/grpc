package org.jackstaff.grpc.internal;

import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.Packet;

/**
 * @author reco@jackstaff.org
 */
public abstract class PacketServerGrpc extends InternalGrpc.InternalImplBase {

    @Override
    public final void unary(InternalProto.Packet packet, StreamObserver<InternalProto.Packet> observer) {
        unaryImpl(Transform.fromProto(packet), Transform.fromProtoObserver(observer));
    }

    @Override
    public final void serverStreaming(InternalProto.Packet packet, StreamObserver<InternalProto.Packet> observer) {
        serverStreamingImpl(Transform.fromProto(packet), Transform.fromProtoObserver(observer));
    }

    @Override
    public final StreamObserver<InternalProto.Packet> clientStreaming(StreamObserver<InternalProto.Packet> observer) {
        return Transform.buildProtoObserver(clientStreamingImpl(Transform.fromProtoObserver(observer)));
    }

    @Override
    public final StreamObserver<InternalProto.Packet> bidiStreaming(StreamObserver<InternalProto.Packet> observer) {
        return Transform.buildProtoObserver(bidiStreamingImpl(Transform.fromProtoObserver(observer)));
    }

    protected abstract void unaryImpl(Packet<?> packet, StreamObserver<Packet<?>> observer);

    protected abstract void serverStreamingImpl(Packet<?> packet, StreamObserver<Packet<?>> observer);

    protected abstract StreamObserver<Packet<?>> clientStreamingImpl(StreamObserver<Packet<?>> observer);

    protected abstract StreamObserver<Packet<?>> bidiStreamingImpl(StreamObserver<Packet<?>> observer);

}
