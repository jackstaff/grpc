package org.jackstaff.grpc.internal;

import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.MethodDescriptor;
import org.jackstaff.grpc.Transform;

import java.time.Duration;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static io.grpc.stub.ClientCalls.*;

/**
 * @author reco@jackstaff.org
 */
public class Stub<S extends AbstractStub<S>, ReqT, RespT> {

    private final String authority;
    private final ManagedChannel channel;
    private final Duration defaultTimeout;

    private S stub;
    private MethodDescriptor descriptor;
    private Transform<ReqT, ?> reqTransform;
    private Transform<RespT, ?> respTransform;

    public Stub(String authority, ManagedChannel channel, Duration defaultTimeout) {
        this.authority = authority;
        this.channel = channel;
        this.defaultTimeout = defaultTimeout;
    }

    @SuppressWarnings("unchecked")
    public Stub(Stub<S, ReqT, RespT> prototype, MethodDescriptor descriptor) {
        this(prototype.authority, prototype.channel, prototype.defaultTimeout);
        this.descriptor = descriptor;
        this.reqTransform = descriptor.requestTransform();
        this.respTransform = descriptor.responseTransform();
        CallOptions options = CallOptions.DEFAULT.withAuthority(authority);
        switch (descriptor.getMethodType()) {
            case Unary:
            case UnaryServerStreaming:
                this.stub =(S)new BlockingStub(channel, options);
                break;
            default:
                this.stub =(S)new AsyncStub(channel, options);
                break;
        }
    }

    public String getAuthority() {
        return authority;
    }

    public ManagedChannel getChannel() {
        return channel;
    }

    public Duration getDefaultTimeout() {
        return defaultTimeout;
    }

    public void attachDefaultDeadline() {
        attachDeadline(defaultTimeout);
    }

    public void attachDeadline(Duration duration) {
        Optional.of(Optional.ofNullable(duration).filter(d->d.getSeconds()>=1).orElse(defaultTimeout)).filter(d->d.getSeconds()>=1).
                ifPresent(t -> stub = stub.withDeadlineAfter(t.toMillis()+2000, TimeUnit.MILLISECONDS));
    }

    public <T> void attach(HeaderMetadata<T> metadata, T value) {
        this.stub = metadata.attach(this.stub, value);
    }

    public void attach(String name, String value) {
        this.stub = HeaderMetadata.attachString(this.stub, name, value);
    }

    public void attach(String name, byte[] value) {
        this.stub = HeaderMetadata.attachBinary(this.stub, name, value);
    }

    public RespT blockingUnary(ReqT request){
        return respTransform.from(blockingUnaryCall(stub.getChannel(), descriptor.grpcMethod(), stub.getCallOptions(), reqTransform.build(request)));
    }

    public Iterator<RespT> blockingServerStreaming(ReqT request){
        return respTransform.fromIterator(blockingServerStreamingCall(stub.getChannel(), descriptor.grpcMethod(), stub.getCallOptions(), reqTransform.build(request)));
    }

    public void asyncUnary(ReqT request, StreamObserver<RespT> observer) {
        asyncUnaryCall(stub.getChannel().newCall(descriptor.grpcMethod(), stub.getCallOptions()), reqTransform.build(request), respTransform.buildObserver(observer));
    }

    public void asyncServerStreaming(ReqT request, StreamObserver<RespT> observer) {
        asyncServerStreamingCall(stub.getChannel().newCall(descriptor.grpcMethod(), stub.getCallOptions()), reqTransform.build(request), respTransform.buildObserver(observer));
    }

    public StreamObserver<ReqT> asyncClientStreaming(StreamObserver<RespT> observer) {
        return reqTransform.fromObserver(asyncClientStreamingCall(stub.getChannel().newCall(descriptor.grpcMethod(), stub.getCallOptions()), respTransform.buildObserver(observer)));
    }

    public StreamObserver<ReqT> asyncBidiStreaming(StreamObserver<RespT> observer) {
        return reqTransform.fromObserver(asyncBidiStreamingCall(stub.getChannel().newCall(descriptor.grpcMethod(), stub.getCallOptions()), respTransform.buildObserver(observer)));
    }

}
