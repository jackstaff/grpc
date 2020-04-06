package org.jackstaff.grpc;

import org.jackstaff.grpc.internal.HeaderMetadata;
import org.jackstaff.grpc.internal.Stub;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Context for Interceptor
 * @author reco@jackstaff.org
 * @see Interceptor
 */
public class Context {

    private final Object[] arguments;
    private final Object target;
    private final Map<Object, Object> attributes;
    private final MethodDescriptor methodDescriptor;
    private Stub<?,?,?> stub;

    Context(MethodDescriptor methodDescriptor, Object[] arguments, Object target){
        this.attributes = new ConcurrentHashMap<>();
        this.methodDescriptor = methodDescriptor;
        this.arguments = Optional.ofNullable(arguments).orElse(new Object[0]);
        this.target = target;
    }

    Context stub(Stub<?,?,?> stub){
        this.stub = stub;
        return this;
    }

    /**
     *
     * @return the protocol interface
     */
    public Class<?> getType() {
        return methodDescriptor.getType();
    }

    /**
     *
     * @return the protocol interface's method
     */
    public Method getMethod() {
        return methodDescriptor.getMethod();
    }

    /**
     *
     * @return call method's arguments
     */
    public Object[] getArguments() {
        return arguments.clone();
    }

    /**
     *
     * @return the target bean
     */
    public Object getTarget() {
        return target;
    }

    /**
     * get the local value by key
     * eg.
     * in UseTimeLoggerInterceptor.before(), set the time stamp: setAttribute(this, System.nanoTime()),
     * and in UseTimeLoggerInterceptor.returning, get it: getAttribute(this).
     * @param key key
     * @param <T> value type
     * @return value
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(Object key) {
        return (T) attributes.get(key);
    }

    /**
     * save the local key/value
     * eg.
     * in UseTimeLoggerInterceptor.before(), set the time stamp: setAttribute(this, System.nanoTime()),
     * and in UseTimeLoggerInterceptor.returning, get it: getAttribute(this).
     * @param key key
     * @param value value
     */
    public void setAttribute(Object key, Object value) {
        attributes.put(key, value);
    }

    /**
     * get the metadata from gRPC http header
     * only valid in server side
     * @param name name in header, ignore case
     * @return value
     */
    public String getMetadata(String name) {
        return HeaderMetadata.stringValue(name);
    }

    /**
     * set the metadata into gRPC http header
     * only valid in client side
     * @param name name in header, ignore case
     * @param value value
     */
    public void setMetadata(String name, @Nonnull String value) {
        if (stub !=null){
            stub.attach(name, value);
        }
    }

    MethodDescriptor getMethodDescriptor() {
        return methodDescriptor;
    }

    void setChannel(MessageChannel<?> channel){
        methodDescriptor.setChannel(arguments, channel);
    }

}
