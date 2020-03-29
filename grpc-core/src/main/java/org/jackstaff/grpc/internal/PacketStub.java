package org.jackstaff.grpc.internal;

import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.Packet;
import org.jackstaff.grpc.Transform;
import org.jackstaff.grpc.internal.InternalGrpc.InternalBlockingStub;
import org.jackstaff.grpc.internal.InternalGrpc.InternalStub;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author reco@jackstaff.org
 */
public class PacketStub<S extends AbstractStub<S>> {

    private ManagedChannel channel;
    private InternalBlockingStub blockingStub;
    private InternalStub asyncStub;
    private S stub;
    private Duration defaultTimeout;
    private Transform<Packet<?>, InternalProto.Packet> transform;

    public PacketStub(String authority, int defaultTimeoutSeconds, ManagedChannel channel){
        this.channel = channel;
        this.blockingStub = InternalGrpc.newBlockingStub(channel).build(channel, CallOptions.DEFAULT.withAuthority(authority));
        this.asyncStub = InternalGrpc.newStub(channel).build(channel, CallOptions.DEFAULT.withAuthority(authority));
        this.transform = Serializer.getTransform();
        this.defaultTimeout = defaultTimeoutSeconds >5 ? Duration.ofSeconds(defaultTimeoutSeconds) : Duration.ZERO;
    }

    public ManagedChannel getChannel() {
        return channel;
    }

    @SuppressWarnings("unchecked")
    public PacketStub(PacketStub<?> another, boolean block) {
        this.stub = (S)(block ? another.blockingStub : another.asyncStub);
        this.transform = another.transform;
        this.defaultTimeout = another.defaultTimeout;
    }

    public PacketStub<S> timeout(@Nullable Duration duration) {
        Optional.of(Optional.ofNullable(duration).orElse(defaultTimeout)).filter(d->d.getSeconds()>0).
                ifPresent(t -> stub = stub.withDeadlineAfter(t.toMillis()+5000, TimeUnit.MILLISECONDS));
        return this;
    }

    public <T> void attach(HeaderMetadata<T> metadata, T value) {
        stub = metadata.attach(stub, value);
    }

    public void attach(String name, String value) {
        stub = HeaderMetadata.attachString(stub, name, value);
    }

    public void attach(String name, byte[] value) {
        stub = HeaderMetadata.attachBinary(stub, name, value);
    }

    public Packet<?> unary(Packet<?> packet) {
        return transform.from(((InternalBlockingStub)stub).unary(transform.build(packet)));
    }

    public Iterator<Packet<?>> syncServerStreaming(Packet<?> packet) {
        return transform.fromIterator(((InternalBlockingStub)stub).serverStreaming(transform.build(packet)));
    }

    public void asynchronousUnary(Packet<?> packet) {
        ((InternalStub)stub).unary(transform.build(packet), transform.buildObserver(null));
    }

    public void unaryStreaming(Packet<?> packet, StreamObserver<Packet<?>> observer) {
        ((InternalStub)stub).unary(transform.build(packet), transform.buildObserver(observer));
    }

    public void serverStreaming(Packet<?> packet, StreamObserver<Packet<?>> observer) {
        ((InternalStub)stub).serverStreaming(transform.build(packet), transform.buildObserver(observer));
    }

    public StreamObserver<Packet<?>> clientStreaming(StreamObserver<Packet<?>> observer) {
        return transform.fromObserver(((InternalStub)stub).clientStreaming(transform.buildObserver(observer)));
    }

    public StreamObserver<Packet<?>> bidiStreaming(StreamObserver<Packet<?>> observer) {
        return transform.fromObserver(((InternalStub)stub).bidiStreaming(transform.buildObserver(observer)));
    }

}
