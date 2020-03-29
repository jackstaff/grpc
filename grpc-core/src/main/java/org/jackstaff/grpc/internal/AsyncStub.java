package org.jackstaff.grpc.internal;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.stub.AbstractAsyncStub;

public class AsyncStub extends AbstractAsyncStub<AsyncStub> {

    public AsyncStub(Channel channel, CallOptions callOptions) {
        super(channel, callOptions);
    }

    @Override
    public AsyncStub build(Channel channel, CallOptions callOptions) {
        return new AsyncStub(channel, callOptions);
    }

}
