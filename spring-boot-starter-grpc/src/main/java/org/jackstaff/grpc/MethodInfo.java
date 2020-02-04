package org.jackstaff.grpc;

import org.jackstaff.grpc.annotation.AsynchronousUnary;
import org.jackstaff.grpc.exception.ValidationException;
import org.jackstaff.grpc.interceptor.Interceptor;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * @author reco@jackstaff.org
 */
class MethodInfo {

    private Object bean;
    private Class<?> type;
    private Method method;
    private List<Interceptor> interceptors;
    private Mode mode;
    private int streamingIndex;
    private String error;
    private String sign;

    public MethodInfo(Class<?> type, Method method) {
        this(null, type, method, null);
    }

    public MethodInfo(Object bean, Class<?> type, Method method, List<Interceptor> interceptors) {
        if (!type.isInterface()){
            throw new ValidationException("invalid type "+type.getName() +" MUST be interface");
        }
        this.bean = bean;
        this.type = type;
        this.method = method;
        this.interceptors = Optional.ofNullable(interceptors).orElseGet(ArrayList::new);
        String name = type.getName()+"/" + method.toString();
        this.sign = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8))+"-"+Math.abs(name.hashCode());
        this.mode = checkMode();
        if (!isInvalid() && method.getAnnotation(AsynchronousUnary.class) != null){
//            if (mode == Mode.ServerStreaming){
//                this.mode = Mode.UnaryStreaming;
//                return;
//            }
            if (mode == Mode.Unary && method.getReturnType().equals(Void.TYPE)){
                this.mode = Mode.UnaryAsynchronous;
                return;
            }
            this.error = method +" @Asynchronous Only for Unary Call & void return";
            this.mode = Mode.Invalid;
        }
        if (isInvalid()){
            throw new ValidationException(error);
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
                streamingIndex =IntStream.range(0, types.length).filter(i->types[i].equals(Consumer.class)).sum();
                if (method.getReturnType().equals(Void.TYPE)) {
                    return Mode.ServerStreaming;
                }
                if (Consumer.class.equals(method.getReturnType())) {
                    return Mode.BiStreaming;
                }
            default:
                error = method + " invalid sign";
                return Mode.Invalid;
        }
    }

    public boolean isInvalid(){
        return mode == Mode.Invalid;
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

    public String getError() {
        return error;
    }

    public int getStreamingIndex() {
        return streamingIndex;
    }

    public Object getBean() {
        return bean;
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodInfo that = (MethodInfo) o;
        return type.equals(that.type) && method.equals(that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, method);
    }

    @Override
    public String toString() {
        return "MethodInfo{" +
                "type=" + type +
                ", method=" + method +
                '}';
    }

}
