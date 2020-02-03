package org.jackstaff.grpc;

import org.jackstaff.grpc.internal.HeaderMetadata;
import org.jackstaff.grpc.internal.PacketStub;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author reco@jackstaff.org
 */
public class Context {

    private static ThreadLocal<Context> local = new ThreadLocal<>();

    static void setCurrent(Context context) {
        local.set(context);
    }

    static void reset() {
        local.remove();
    }

    public static Context current() {
        return local.get();
    }

    private Class<?> type;
    private Method method;
    private Object[] arguments;
    private Object target;
    private Map<Object, Object> attributes;
    private PacketStub stub;

    Context(MethodInfo info, Object[] arguments, Object target){
        this.attributes = new ConcurrentHashMap<>();
        this.type = info.getType();
        this.method = info.getMethod();
        this.arguments = arguments;
        this.target = target;
    }

    Context(PacketStub stub, MethodInfo info, Object[] arguments, Object target) {
        this(info, arguments, target);
        this.stub = stub;
        stub.attach(HeaderMetadata.METHOD_SIGN, info.getSign());
    }

    public Class<?> getType() {
        return type;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public Object getTarget() {
        return target;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(Object key) {
        return (T) attributes.get(key);
    }

    public void setAttribute(Object key, Object value) {
        attributes.put(key, value);
    }

    public String getHeaderMetadata(String name) {
        if (stub !=null){
            throw new UnsupportedOperationException(" only support in @Server interceptor");
        }
        return HeaderMetadata.stringValue(name);
    }

    public void setHeaderMetadata(String name, String value) {
        if (stub ==null){
            throw new UnsupportedOperationException(" only support in @Client interceptor");
        }
        stub.attach(name, value);
    }

    byte[] getBinaryHeaderMetadata(String name){
        return HeaderMetadata.binaryValue(name);
    }

    void setHeaderMetadata(String name, byte[] value){
        stub.attach(name, value);
    }

}
