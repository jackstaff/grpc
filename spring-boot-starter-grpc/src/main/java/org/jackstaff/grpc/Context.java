package org.jackstaff.grpc;

import org.jackstaff.grpc.internal.HeaderMetadata;
import org.jackstaff.grpc.internal.PacketStub;
import org.springframework.context.ApplicationContext;

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

    static void remove(Context context) {
        if (local.get() == context) {
            local.remove();
        }
    }

    public static Context current() {
        return local.get();
    }

    private final ApplicationContext appContext;
    private final Object[] arguments;
    private final Object target;
    private final Map<Object, Object> attributes;
    private final PacketStub<?> stub;
    private final MethodInfo info;

    Context(ApplicationContext appContext, MethodInfo info, Object[] arguments, Object target){
        this(appContext,null, info, arguments, target);
    }

    Context(ApplicationContext appContext, PacketStub<?> stub, MethodInfo info, Object[] arguments, Object target) {
        this.attributes = new ConcurrentHashMap<>();
        this.appContext = appContext;
        this.info = info;
        this.arguments = arguments;
        this.target = target;
        this.stub = stub;
    }

    public ApplicationContext getAppContext() {
        return appContext;
    }

    public Class<?> getType() {
        return info.getType();
    }

    public Method getMethod() {
        return info.getMethod();
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

    public String getMetadata(String name) {
        if (stub !=null) {
            return null;
//            throw new ValidationException(" only support in @Server interceptor");
        }
        return HeaderMetadata.stringValue(name);
    }

    public void setMetadata(String name, String value) {
        if (stub !=null){
            stub.attach(name, value);
        }
        //throw new ValidationException(" only support in @Client interceptor");
    }

    byte[] getBinaryMetadata(String name){
        return HeaderMetadata.binaryValue(name);
    }

    void setMetadata(String name, byte[] value){
        stub.attach(name, value);
    }

    MethodInfo getMethodInfo() {
        return info;
    }

}
