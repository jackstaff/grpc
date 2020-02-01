package org.jackstaff.grpc;

import java.lang.reflect.Method;

/**
 * @author reco@jackstaff.org
 */
public class Context {

    private final Class<?> type;
    private final Method method;
    private final Object[] arguments;
    private final Object target;
    private io.grpc.Context context;

    Context(io.grpc.Context context, Class<?> type, Method method, Object[] arguments, Object target) {
        this.context = context;
        this.type = type;
        this.method = method;
        this.arguments = arguments;
        this.target = target;
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


}
