package org.jackstaff.grpc;

import com.google.protobuf.ByteString;
import org.jackstaff.grpc.exception.SerializationException;
import org.jackstaff.grpc.internal.InternalProto;
import org.jackstaff.grpc.internal.Serializer;

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

    @Override
    public String toString() {
        return "Packet{" +
                "command=" + command +
                ", payload=" + payload +
                '}';
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

    Object[] unboxing(int length){
        return Arrays.copyOf(unboxing(), length);
    }

    static Packet<Object[]> boxing(Object[] args){
        return boxing(0, args);
    }

    static Packet<Object[]> boxing(int command, Object[] args){
        return new Packet<>(command, Arrays.stream(args).map(a-> a == null || a instanceof Consumer ? new Object() : a).toArray());
    }

    String commandName(){
        return commandName(command);
    }

    public static String commandName(int command){
        switch (command) {
            case COMPLETED: return "COMPLETED";
            case EXCEPTION: return "EXCEPTION";
            case TIMEOUT: return "TIMEOUT";
            case UNREACHABLE: return "UNREACHABLE";
            case OK: return "OK";
            default:
                return "UNKNOWN";
        }
    }

    private static Packet<?> from(InternalProto.Packet proto) {
        try {
            return Serializer.fromBinary(proto.getData().toByteArray());
        }catch (SerializationException ex){
            throw ex;
        }
        catch (Exception ex){
            throw new SerializationException("from Protobuf fail", ex);
        }
    }

    private static InternalProto.Packet build(Packet<?> packet) {
        try {
            return InternalProto.Packet.newBuilder().setData(ByteString.copyFrom(Serializer.toBinary(packet))).build();
        }catch (SerializationException ex){
            throw ex;
        }
        catch (Exception ex){
            throw new SerializationException("build Protobuf fail", ex);
        }
    }

    static {
        Transforms.addTransform(Packet.class, InternalProto.Packet.class, Packet::from, Packet::build);
    }


}
