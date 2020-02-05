package org.jackstaff.grpc;

import org.jackstaff.grpc.internal.HeaderMetadata;
import org.jackstaff.grpc.internal.PacketStub;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author reco@jackstaff.org
 */
public class Context {

    private final ApplicationContext appContext;
    private final Object[] arguments;
    private final Object target;
    private final Map<Object, Object> attributes;
    private final PacketStub<?> stub;
    private final MethodDescriptor methodDescriptor;

    Context(ApplicationContext appContext, MethodDescriptor methodDescriptor, Object[] arguments, Object target){
        this(appContext,null, methodDescriptor, arguments, target);
    }

    Context(ApplicationContext appContext, PacketStub<?> stub, MethodDescriptor methodDescriptor, Object[] arguments, Object target) {
        this.attributes = new ConcurrentHashMap<>();
        this.appContext = appContext;
        this.methodDescriptor = methodDescriptor;
        this.arguments = arguments;
        this.target = target;
        this.stub = stub;
    }

    public ApplicationContext getAppContext() {
        return appContext;
    }

    public Class<?> getType() {
        return methodDescriptor.getType();
    }

    public Method getMethod() {
        return methodDescriptor.getMethod();
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

    MethodDescriptor getMethodDescriptor() {
        return methodDescriptor;
    }

    void setChannel(Consumer<?> channel){
        arguments[methodDescriptor.getChannelIndex()] = channel;
    }

}
