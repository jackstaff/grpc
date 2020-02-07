package org.jackstaff.grpc;

import io.grpc.stub.StreamObserver;

import java.util.Optional;

class MessageObserver implements StreamObserver<Packet<?>> {

    private final MessageChannel<?> channel;

    public MessageObserver(MessageChannel<?> channel) {
        this.channel = channel;
    }

    @Override
    public void onNext(Packet<?> packet) {
        switch (packet.getCommand()) {
            case Command.COMPLETED:
                channel.close(Command.COMPLETED);
                break;
            case Command.MESSAGE:
                channel.acceptMessage(packet.getPayload());
                break;
            case Command.EXCEPTION:
                channel.close(Command.EXCEPTION);
                break;
            case Command.TIMEOUT:
                channel.acceptMessage(packet.getPayload());
                channel.close(Command.TIMEOUT);
                break;
            case Command.ERROR_MESSAGE:
                Optional.ofNullable(packet.getPayload()).ifPresent(channel::setErrorMessage);
                break;
        }
    }

    @Override
    public void onError(Throwable t) {
        channel.close(Command.UNREACHABLE);
    }

    @Override
    public void onCompleted() {
        channel.close(Command.COMPLETED);
    }

}
