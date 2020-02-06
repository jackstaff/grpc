package org.jackstaff.grpc;

import org.jackstaff.grpc.annotation.AsynchronousUnary;
import org.jackstaff.grpc.exception.ValidationException;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * @author reco@jackstaff.org
 */
class MethodDescriptor {

    private Object bean;
    private Class<?> type;
    private Method method;
    private List<Interceptor> interceptors;
    private Mode mode;
    private int channelIndex;
    private String sign;

    public MethodDescriptor(Class<?> type, Method method) {
        this(type, method, null, null);
    }

    public MethodDescriptor(Class<?> type, Method method, Object bean, List<Interceptor> interceptors) {
        if (!type.isInterface()){
            throw new ValidationException("invalid type "+type.getName() +" MUST be interface");
        }
        this.bean = bean;
        this.type = type;
        this.method = method;
        this.interceptors = interceptors;
        String name = type.getName()+"/" + method.toString();
        this.sign = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8))+"-"+Math.abs(name.hashCode());
        this.mode = checkMode();
        if (method.getAnnotation(AsynchronousUnary.class) != null){
            if (mode == Mode.ServerStreaming){
                this.mode = Mode.UnaryStreaming;
                return;
            }
            if (mode == Mode.Unary && method.getReturnType().equals(Void.TYPE)){
                this.mode = Mode.UnaryAsynchronous;
                return;
            }
            throw new ValidationException(method + " @Asynchronous Only for Unary Call & void return");
        }
    }

    private Mode checkMode() {
        switch ((int) Arrays.stream(method.getParameterTypes()).filter(Consumer.class::equals).count()){
            case 0:
                if (Consumer.class.equals(method.getReturnType())) {
                    return Mode.ClientStreaming;
                }
                return Mode.Unary;
            case 1:
                Class<?>[] types = method.getParameterTypes();
                channelIndex =IntStream.range(0, types.length).filter(i->types[i].equals(Consumer.class)).sum();
                if (method.getReturnType().equals(Void.TYPE)) {
                    return Mode.ServerStreaming;
                }
                if (Consumer.class.equals(method.getReturnType())) {
                    return Mode.BiStreaming;
                }
        }
        throw new ValidationException(method + " invalid sign");
    }

    public Mode getMode() {
        return mode;
    }

    public Class<?> getType() {
        return type;
    }

    public Method getMethod() {
        return method;
    }

    public String getSign() {
        return sign;
    }

    public Consumer<?> getChannel(Object[] args){
        return (Consumer<?>) args[channelIndex];
    }

    public void setChannel(Object[] args, Consumer<?> channel){
        args[channelIndex] = channel;
    }

    public Object getBean() {
        return bean;
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

}
