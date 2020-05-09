/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jackstaff.grpc;

import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * @author reco@jackstaff.org
 */
class Utils {

    static @Nonnull Packet<?> before(Context context, List<Interceptor> interceptors) {
        for (int i = 0; i < interceptors.size(); i++) {
            try {
                interceptors.get(i).before(context);
            }catch (Exception exception){
                for (int j = i-1; j >= 0 ; j--) {
                    try {
                        interceptors.get(j).recall(context, exception);
                    }catch (Exception ignore){
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
        for (Interceptor interceptor: interceptors){
            try {
                if (packet.isException()) {
                    interceptor.throwing(context, (Exception) packet.getPayload());
                }else {
                    interceptor.returning(context, packet.getPayload());
                }
            }catch (Exception ignore){
            }
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

    static Throwable throwable(Throwable ex){
        if (ex instanceof StatusRuntimeException){
            return ex;
        }
        if (ex instanceof StatusException){
            return Status.fromThrowable(ex).asRuntimeException(((StatusException) ex).getTrailers());
        }
        return Status.fromThrowable(ex).asRuntimeException();
    }

    static <T> String string(String name, T value){
        return Optional.ofNullable(value).map(Object::toString).map(s->{
            StringBuilder buf = new StringBuilder().append(", ").append(name).append("=");
            if (value instanceof String){
                return buf.append('\'').append(s).append('\'').toString();
            }
            return buf.append(s).toString();
        }).orElse("");
    }

}
