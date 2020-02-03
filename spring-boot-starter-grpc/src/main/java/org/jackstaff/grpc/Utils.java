package org.jackstaff.grpc;

import org.jackstaff.grpc.exception.GrpcException;
import org.jackstaff.grpc.interceptor.Interceptor;
import org.springframework.beans.BeanUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

class Utils {

    private static final Map<Class<? extends Interceptor>, Interceptor> interceptors = new ConcurrentHashMap<>();

    public static List<Interceptor> getInterceptors(Class<? extends Interceptor>[] types){
        return Arrays.stream(types).map(BeanUtils::instantiateClass).collect(Collectors.toList());
    }

    public static Packet<Object> invoke(Context context, List<Interceptor> interceptors) throws Exception {
        Exception exception = null;
        for (int i = 0; i < interceptors.size(); i++) {
            try {
                if (!interceptors.get(i).before(context)){
                    exception = new GrpcException(interceptors.get(i).name() +" interceptor recall");
                }
            }catch (Exception ex0){
                exception = ex0;
            }
            if (exception != null){
                for (int j = i-1; j >= 0 ; j--) {
                    interceptors.get(j).recall(context);
                }
                return new Packet<>(Command.EXCEPTION, exception);
            }
        }
        Object result = null;
        try {
            if (context.getArguments() ==null || context.getArguments().length ==0){
                result = context.getMethod().invoke(context.getTarget());
            }else {
                result = context.getMethod().invoke(context.getTarget(), context.getArguments());
            }
        } catch (Exception ex0) {
            exception = ex0;
        }
        for (int i = interceptors.size()-1; i >=0 ; i--) {
            if (exception ==null) {
                interceptors.get(i).returning(context, result);
            }else {
                interceptors.get(i).throwing(context, exception);
            }
        }
        if (exception != null){
            return new Packet<>(Command.EXCEPTION, exception);
        }
        return new Packet<>(Command.MESSAGE, result);
    }

}
