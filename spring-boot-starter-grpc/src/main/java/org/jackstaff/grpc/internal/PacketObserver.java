package org.jackstaff.grpc.internal;

import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.Packet;

import java.util.Optional;

import static org.jackstaff.grpc.internal.Transform.buildProto;

public class PacketObserver implements StreamObserver<Packet<?>> {

    private final StreamObserver<InternalProto.Packet> observer;

    public PacketObserver() {
        this.observer = null;
    }

    public PacketObserver(StreamObserver<InternalProto.Packet> observer) {
        this.observer = observer;
    }

    public StreamObserver<InternalProto.Packet> getObserver() {
        return observer;
    }

    @Override
    public void onNext(Packet<?> value) {
        Optional.ofNullable(observer).ifPresent(o->o.onNext(buildProto(value)));
    }

    @Override
    public void onError(Throwable t) {
        Optional.ofNullable(observer).ifPresent(o->o.onError(t));
    }

    @Override
    public void onCompleted() {
        Optional.ofNullable(observer).ifPresent(StreamObserver::onCompleted);
    }

}
