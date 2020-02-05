package org.jackstaff.grpc;

import org.springframework.beans.BeanUtils;

import java.lang.reflect.Method;
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

    public static Packet<?> before(Context context, List<Interceptor> interceptors) {
        for (int i = 0; i < interceptors.size(); i++) {
            try {
                interceptors.get(i).before(context);
            }catch (Exception exception){
                for (int j = i-1; j >= 0 ; j--) {
                    try {
                        interceptors.get(j).recall(context, exception);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
                return Packet.throwable(exception);
            }
        }
        return Packet.NULL();
    }

    public static Packet<?> invoke(Object target, Method method, Object[] args) {
        Object result;
        try {
            if (args ==null || args.length ==0){
                result = method.invoke(target);
            }else {
                result = method.invoke(target, args);
            }
            return Packet.ok(result);
        } catch (Exception ex) {
            return Packet.throwable(ex);
        }
    }

    public static void after(Context context, List<Interceptor> interceptors, Packet<?> packet) {
        try {
            for (int i = interceptors.size()-1; i >=0 ; i--) {
                if (packet.isException()) {
                    interceptors.get(i).throwing(context, (Exception) packet.getPayload());
                }else {
                    interceptors.get(i).returning(context, packet.getPayload());
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static Packet<?> walkThrough(Context context, List<Interceptor> interceptors) {
        Packet<?> packet = before(context, interceptors);
        if (!packet.isException()){
            packet = invoke(context.getTarget(), context.getMethod(), context.getArguments());
            after(context, interceptors, packet);
        }
        return packet;
    }

}
