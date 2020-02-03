package org.jackstaff.grpc;

import java.util.function.Consumer;

/**
 * @author reco@jackstaff.org
 */
public class PacketConsumer<T> extends MessageConsumer<Packet<T>> {

    public PacketConsumer(Consumer<Packet<T>> consumer) {
        super(consumer);
    }

    public void message(T t){
        accept(new Packet<>(Command.MESSAGE, t));
    }

    public void complete(T t){
        accept(new Packet<>(Command.COMPLETED, t));
    }

    public void messageOrComplete(boolean isMessage, T t){
        accept(new Packet<>(isMessage ? Command.MESSAGE :Command.COMPLETED, t));
    }

}
