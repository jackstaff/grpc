package org.jackstaff.grpc;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * the package for interact client side and server side
 * @author reco@jackstaff.org
 */
public class Packet<T> implements Command {

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

    boolean isOk(){
        return command == OK;
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

    Object[] unboxing(){
        return Arrays.stream((Object[])payload).map(a->a ==null || a.getClass().equals(Object.class) ? null : a).toArray();
    }

    static Packet<Object[]> boxing(Object[] args){
        return boxing(0, args);
    }

    static Packet<Object[]> boxing(int command, Object[] args){
        return new Packet<>(command, Arrays.stream(args).map(a-> a == null || a instanceof Consumer ? new Object() : a).toArray());
    }

    String commandName(){
        switch (command) {
            case COMPLETED: return "COMPLETED";
            case EXCEPTION: return "EXCEPTION";
            case TIMEOUT: return "TIMEOUT";
            case UNREACHABLE: return "UNREACHABLE";
            case OK:
            default:
                return "OK";
        }
    }


}
