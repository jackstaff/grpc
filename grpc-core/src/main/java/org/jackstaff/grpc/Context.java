package org.jackstaff.grpc;

import org.jackstaff.grpc.internal.HeaderMetadata;
import org.jackstaff.grpc.internal.PacketStub;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author reco@jackstaff.org
 */
public class Context {

    private final Object[] arguments;
    private final Object target;
    private final Map<Object, Object> attributes;
    private final MethodDescriptor methodDescriptor;
    private PacketStub<?> stub;

    Context(MethodDescriptor methodDescriptor, Object[] arguments, Object target){
        this.attributes = new ConcurrentHashMap<>();
        this.methodDescriptor = methodDescriptor;
        this.arguments = Optional.ofNullable(arguments).map(Object[]::clone).orElse(new Object[0]);
        this.target = target;
    }

    Context stub(PacketStub<?> stub){
        this.stub = stub;
        return this;
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

    MethodDescriptor getMethodDescriptor() {
        return methodDescriptor;
    }

    void setChannel(MessageChannel<?> channel){
        methodDescriptor.setChannel(arguments, channel);
    }

}
