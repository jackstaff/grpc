package org.jackstaff.grpc;

/**
 * @author reco@jackstaff.org
 */
class MessageObserver extends ChannelObserver<Packet<?>> {


    public MessageObserver(MessageChannel<?> channel) {
        super((MessageChannel<Packet<?>>) channel);
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

}
