package org.jackstaff.grpc;

import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.internal.PacketServerGrpc;

/**
 * @author reco@jackstaff.org
 */
public class PacketServer extends PacketServerGrpc {

    @Override
    protected void unaryImpl(Packet<?> packet, StreamObserver<Packet<?>> observer) {

    }

    @Override
    protected void serverStreamingImpl(Packet<?> packet, StreamObserver<Packet<?>> observer) {

    }

    @Override
    protected StreamObserver<Packet<?>> clientStreamingImpl(StreamObserver<Packet<?>> observer) {
        return null;
    }

    @Override
    protected StreamObserver<Packet<?>> bidiStreamingImpl(StreamObserver<Packet<?>> observer) {
        return null;
    }

}
