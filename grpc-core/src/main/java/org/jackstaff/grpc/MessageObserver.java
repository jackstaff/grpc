package org.jackstaff.grpc;

import io.grpc.stub.StreamObserver;

/**
 * @author reco@jackstaff.org
 */
class MessageObserver implements StreamObserver<Packet<?>> {

    private final MessageChannel<?> channel;

    public MessageObserver(MessageChannel<?> channel) {
        this.channel = channel;
    }

    public MessageObserver link(MessageChannel<?> channel) {
        this.channel.link(channel);
        return this;
    }

    @Override
    public void onNext(Packet<?> packet) {
        switch (packet.getCommand()) {
            case Command.COMPLETED:
                channel.acceptMessage(packet.getPayload());
                channel.close(Command.COMPLETED);
                break;
            case Command.MESSAGE:
                channel.acceptMessage(packet.getPayload());
                break;
            case Command.TIMEOUT:
                channel.acceptMessage(packet.getPayload());
                channel.close(Command.TIMEOUT);
                break;
            case Command.EXCEPTION:
                channel.setError(Command.EXCEPTION, (Exception)packet.getPayload());
                break;
        }
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
