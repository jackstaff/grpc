package org.jackstaff.grpc.internal;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.stub.AbstractBlockingStub;

public class BlockingStub extends AbstractBlockingStub<BlockingStub> {

    public BlockingStub(Channel channel, CallOptions callOptions) {
        super(channel, callOptions);
    }

    @Override
    protected BlockingStub build(Channel channel, CallOptions callOptions) {
        return new BlockingStub(channel, callOptions);
    }

}
