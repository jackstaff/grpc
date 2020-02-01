package org.jackstaff.grpc;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * @author reco@jackstaff.org
 */
class MethodInfo {

    private Object handler;
    private Class<?> type;
    private Method method;
    private Interceptor[] interceptors;
    private Mode mode;
    private int streamingIndex;
    private String error;
    private String sign;

    public MethodInfo(Class<?> type, Method method, Interceptor[] interceptors) {
        this(null, type, method, interceptors);
    }

    public MethodInfo(Object handler, Class<?> type, Method method, Interceptor[] interceptors) {
        this.handler = handler;
        this.type = type;
        this.method = method;
        this.interceptors = interceptors;
        String name = type.getName()+"/" + method.toString();
        this.sign = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8))+":"+name.hashCode();
        this.mode = checkMode();
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

    public Object getHandler() {
        return handler;
    }

    public List<Interceptor> getInterceptors() {
        return interceptors != null ? Arrays.asList(interceptors) : new ArrayList<>();
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

}
