package org.jackstaff.grpc;

/**
 * @author reco@jackstaff.org
 */
public final class Packet<T> implements Completable, Command {

    private int command;
    private T payload;

    public Packet() {

    }

    public Packet(int command) {
        this.command = command;
    }

    public Packet(int command, T payload) {
        this.command = command;
        this.payload = payload;
    }

    @Override
    public boolean isCompleted() {
        return command >= COMPLETED_RANGE_MIN && command <= COMPLETED_RANGE_MAX;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

}