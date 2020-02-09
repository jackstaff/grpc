package org.jackstaff.grpc;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

class Utils {

    static @Nonnull Packet<?> before(Context context, List<Interceptor> interceptors) {
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
        return new Packet<>();
    }

    static Packet<?> invoke(Object target, Method method, Object[] args) {
        Object result;
        try {
            if (args ==null || args.length ==0){
                result = method.invoke(target);
            }else {
                result = method.invoke(target, args);
            }
            return Packet.ok(result);
        }catch (InvocationTargetException ex){
            return Packet.throwable(ex.getTargetException());
        }catch (Exception ex) {
            return Packet.throwable(ex);
        }
    }

    static void after(Context context, List<Interceptor> interceptors, Packet<?> packet) {
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

    static Packet<?> walkThrough(Context context, List<Interceptor> interceptors) {
        Packet<?> packet = before(context, interceptors);
        if (!packet.isException()){
            packet = invoke(context.getTarget(), context.getMethod(), context.getArguments());
            after(context, interceptors, packet);
        }
        return packet;
    }

}
