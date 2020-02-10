package org.jackstaff.grpc.internal;

import io.grpc.Internal;
import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.Packet;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author reco@jackstaff.org
 */
@Internal
public class PacketServerGrpc extends InternalGrpc.InternalImplBase {

    private final BiConsumer<Packet<?>, StreamObserver<Packet<?>>> unary;
    private final BiConsumer<Packet<?>, StreamObserver<Packet<?>>> serverStreaming;
    private final Function<StreamObserver<Packet<?>>, StreamObserver<Packet<?>>> clientStreaming;
    private final Function<StreamObserver<Packet<?>>, StreamObserver<Packet<?>>> bidiStreaming;

    public PacketServerGrpc(BiConsumer<Packet<?>, StreamObserver<Packet<?>>> unary,
                            BiConsumer<Packet<?>, StreamObserver<Packet<?>>> serverStreaming,
                            Function<StreamObserver<Packet<?>>, StreamObserver<Packet<?>>> clientStreaming,
                            Function<StreamObserver<Packet<?>>, StreamObserver<Packet<?>>> bidiStreaming) {
        this.unary = unary;
        this.serverStreaming = serverStreaming;
        this.clientStreaming = clientStreaming;
        this.bidiStreaming = bidiStreaming;
    }

    @Override
    public final void unary(InternalProto.Packet packet, StreamObserver<InternalProto.Packet> observer) {
        this.unary.accept(Transform.fromProto(packet), Transform.fromProtoObserver(observer));
    }

    @Override
    public final void serverStreaming(InternalProto.Packet packet, StreamObserver<InternalProto.Packet> observer) {
        this.serverStreaming.accept(Transform.fromProto(packet), Transform.fromProtoObserver(observer));
    }

    @Override
    public final StreamObserver<InternalProto.Packet> clientStreaming(StreamObserver<InternalProto.Packet> observer) {
        return Transform.buildProtoObserver(this.clientStreaming.apply(Transform.fromProtoObserver(observer)));
    }

    @Override
    public final StreamObserver<InternalProto.Packet> bidiStreaming(StreamObserver<InternalProto.Packet> observer) {
        return Transform.buildProtoObserver(this.bidiStreaming.apply(Transform.fromProtoObserver(observer)));
    }

}
