package org.jackstaff.grpc;

import io.grpc.stub.StreamObserver;

import java.util.function.Consumer;

class MessageObserver implements StreamObserver<Packet<?>> {

    private final MessageChannel<?> channel;

    public MessageObserver(Consumer<?> origin) {
        this.channel = origin instanceof MessageChannel ? (MessageChannel<?>) origin : new MessageChannel<>(origin);
    }

    public MessageChannel<?> getChannel() {
        return channel;
    }

    @Override
    public void onNext(Packet<?> packet) {
        switch (packet.getCommand()) {
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
                break;
        }
    }

    @Override
    public void onError(Throwable t) {
        channel.close(Command.EXCEPTION);
    }

    @Override
    public void onCompleted() {
        channel.close(Command.COMPLETED);
    }

}
