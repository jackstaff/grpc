package org.jackstaff.grpc;


import java.util.function.Consumer;

/**
 * @author reco@jackstaff.org
 */
public class PacketConsumer<T> extends MessageConsumer<Packet<T>> {

    public PacketConsumer(Consumer<Packet<T>> consumer) {
        super(consumer);
    }

}
