package org.jackstaff.grpc;

import java.util.Arrays;
import java.util.function.Consumer;

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


    boolean isException(){
        return command == EXCEPTION;
    }

    static Packet<?> throwable(Throwable ex){
        return new Packet<>(EXCEPTION, ex);
    }

    static Packet<?> ok(Object value){
        return new Packet<>(OK, value);
    }

    static Packet<?> message(Object value){
        return new Packet<>(MESSAGE, value);
    }

    static Packet<?> NULL() {
        return new Packet<>();
    }

    Object[] unboxing(){
        return Arrays.stream((Object[])payload).map(a->a ==null || a.getClass().equals(Object.class) ? null : a).toArray();
    }

    static Packet<Object[]> boxing(Object[] args){
        return boxing(0, args);
    }

    static Packet<Object[]> boxing(int command, Object[] args){
        return new Packet<>(command, Arrays.stream(args).map(a-> a == null || a instanceof Consumer ? new Object() : a).toArray());
    }

}
