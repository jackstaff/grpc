package org.jackstaff.grpc.internal;

import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.Packet;
import org.jackstaff.grpc.Transform;
import org.jackstaff.grpc.Transforms;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author reco@jackstaff.org
 */
public class PacketServerGrpc extends InternalGrpc.InternalImplBase {

    private final BiConsumer<Packet<?>, StreamObserver<Packet<?>>> unary;
    private final BiConsumer<Packet<?>, StreamObserver<Packet<?>>> serverStreaming;
    private final Function<StreamObserver<Packet<?>>, StreamObserver<Packet<?>>> clientStreaming;
    private final Function<StreamObserver<Packet<?>>, StreamObserver<Packet<?>>> bidiStreaming;
    private final Transform<Packet<?>, InternalProto.Packet> transform;

    public PacketServerGrpc(BiConsumer<Packet<?>, StreamObserver<Packet<?>>> unary,
                            BiConsumer<Packet<?>, StreamObserver<Packet<?>>> serverStreaming,
                            Function<StreamObserver<Packet<?>>, StreamObserver<Packet<?>>> clientStreaming,
                            Function<StreamObserver<Packet<?>>, StreamObserver<Packet<?>>> bidiStreaming) {
        this.unary = unary;
        this.serverStreaming = serverStreaming;
        this.clientStreaming = clientStreaming;
        this.bidiStreaming = bidiStreaming;
        this.transform = Transforms.getTransform(Packet.class);
    }

    @Override
    public final void unary(InternalProto.Packet packet, StreamObserver<InternalProto.Packet> observer) {
        this.unary.accept(transform.from(packet), transform.fromObserver(observer));
    }

    @Override
    public final void serverStreaming(InternalProto.Packet packet, StreamObserver<InternalProto.Packet> observer) {
        this.serverStreaming.accept(transform.from(packet), transform.fromObserver(observer));
    }

    @Override
    public final StreamObserver<InternalProto.Packet> clientStreaming(StreamObserver<InternalProto.Packet> observer) {
        return transform.buildObserver(this.clientStreaming.apply(transform.fromObserver(observer)));
    }

    @Override
    public final StreamObserver<InternalProto.Packet> bidiStreaming(StreamObserver<InternalProto.Packet> observer) {
        return transform.buildObserver(this.bidiStreaming.apply(transform.fromObserver(observer)));
    }

}
