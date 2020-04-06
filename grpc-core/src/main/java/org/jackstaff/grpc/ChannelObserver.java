package org.jackstaff.grpc;

import io.grpc.stub.StreamObserver;
/**
 * @author reco@jackstaff.org
 */
class ChannelObserver<T> implements StreamObserver<T> {

    final MessageChannel<T> channel;

    public ChannelObserver(MessageChannel<T> channel) {
        this.channel = channel;
    }

    @Override
    public void onNext(T value) {
        channel.accept(value);
    }

    @Override
    public void onError(Throwable error) {
        if (!channel.isClosed()) {
            channel.setError(Command.UNREACHABLE, error);
        }
    }

    @Override
    public void onCompleted() {
        if (!channel.isClosed()) {
            channel.close(Command.COMPLETED);
        }
    }

}
