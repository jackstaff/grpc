package org.jackstaff.grpc.internal;

import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.AbstractStub;
import org.jackstaff.grpc.MethodDescriptor;
import org.jackstaff.grpc.MethodType;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author reco@jackstaff.org
 */
public class Stub<S extends AbstractStub<S>> {

    private BlockingStub blockingStub;
    private AsyncStub asyncStub;
    private S stub;
    private Duration defaultTimeout;
//    private Transform<Pojo, Proto> transform;

    public Stub(String authority, ManagedChannel channel) {
        CallOptions options = CallOptions.DEFAULT.withAuthority(authority);
        this.blockingStub = new BlockingStub(channel, options);
        this.asyncStub = new AsyncStub(channel, options);
    }

    @SuppressWarnings("unchecked")
    public Stub(Stub<S> another, MethodDescriptor descriptor) {
        this.stub =(S)(descriptor.getMethodType() == MethodType.Unary ? another.blockingStub : another.asyncStub);
    }

    public AbstractStub<?> getStub() {
        return stub;
    }

    public void attachDeadline(@Nullable Duration duration) {
        Optional.of(Optional.ofNullable(duration).orElse(defaultTimeout)).filter(d->d.getSeconds()>0).
                ifPresent(t -> stub = stub.withDeadlineAfter(t.toMillis()+5000, TimeUnit.MILLISECONDS));
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

//
//    public Pojo unary(Pojo packet) {
//        throw new UnsupportedOperationException("");
//    }
//
//    public Iterator<Pojo> syncServerStreaming(Pojo packet) {
//        throw new UnsupportedOperationException("");
//    }
//
//    public void asynchronousUnary(Pojo packet) {
//        throw new UnsupportedOperationException("");
//    }
//
//    public void unaryStreaming(Pojo packet, StreamObserver<Pojo> observer) {
//        throw new UnsupportedOperationException("");
//    }
//
//    public void serverStreaming(Pojo packet, StreamObserver<Pojo> observer) {
//        throw new UnsupportedOperationException("");
//    }
//
//    public StreamObserver<Pojo> clientStreaming(StreamObserver<Pojo> observer) {
//        throw new UnsupportedOperationException("");
//    }
//
//    public StreamObserver<Pojo> bidiStreaming(StreamObserver<Pojo> observer) {
//        throw new UnsupportedOperationException("");
//    }

}
