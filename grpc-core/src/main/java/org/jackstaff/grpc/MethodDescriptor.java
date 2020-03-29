package org.jackstaff.grpc;

import org.jackstaff.grpc.annotation.*;
import org.jackstaff.grpc.exception.ValidationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author reco@jackstaff.org
 */
public class MethodDescriptor {

    private Object bean;
    private Class<?> type;
    private Method method;
    private List<Interceptor> interceptors;
    private MethodType methodType;
    private int channelIndex;
    private String sign;

    private final boolean v2;
    private Class<?> requestType;
    private Class<?> responseType;
    private MethodDescriptor peer;

    public MethodDescriptor(Class<?> type, Method method) {
        this(type, method, null, null);
    }

    public MethodDescriptor(Class<?> type, Method method, Object bean, List<Interceptor> interceptors) {
        this.v2 = type.getAnnotation(Protocol.class) != null;
        this.bean = bean;
        this.type = type;
        this.method = method;
        this.interceptors = interceptors;
        String name = type.getName()+"/" + method.toString();
        this.sign = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8))+"-"+Math.abs(name.hashCode());
        MethodType annotationType = getAnnotationMethodType(method);
        this.methodType = checkMethodType(annotationType);
        if (annotationType != null && annotationType != this.methodType){
            throw new ValidationException(method + " annotation invalid for "+ methodType);
        }
        if (isV2()){
            switch (this.methodType){
                case Unary:
                    requestType = method.getParameterTypes()[0];
                    responseType = method.getReturnType();
                    break;
                case AsynchronousUnary:
                case ServerStreaming:
                    requestType = method.getParameterTypes()[0];
                    responseType = genericParameter(method, 1);
                    break;
                case ClientStreaming:
                    requestType = method.getParameterTypes()[0];
                    responseType = genericReturnType(method);
                    break;
                case BidiStreaming:
                    requestType = genericParameter(method, 0);
                    responseType = genericReturnType(method);
                    break;
                case VoidClientStreaming:
                default:
                    break;
            }
        }
    }

    private MethodType checkMethodType(MethodType annotationType) {
        switch ((int) Arrays.stream(method.getParameterTypes()).filter(Consumer.class::equals).count()){
            case 0:
                if (Consumer.class.equals(method.getReturnType())) {
                    return MethodType.VoidClientStreaming;
                }
                return MethodType.Unary;
            case 1:
                Class<?>[] types = method.getParameterTypes();
                if (!types[types.length-1].equals(Consumer.class)){
                    throw new ValidationException(method+" Consumer must be LAST parameter");
                }
                channelIndex = types.length-1;//IntStream.range(0, types.length).filter(i->types[i].equals(Consumer.class)).sum();
                if (method.getReturnType().equals(Void.TYPE)) {
                    if (annotationType ==MethodType.AsynchronousUnary){
                        return MethodType.AsynchronousUnary;
                    }
                    return MethodType.ServerStreaming;
                }
                if (Consumer.class.equals(method.getReturnType())) {
                    return Optional.ofNullable(annotationType).map(r-> MethodType.ClientStreaming).orElse(MethodType.BidiStreaming);
                }
        }
        throw new ValidationException(method + " invalid sign");
    }

    public MethodDescriptor getPeer() {
        return peer;
    }

    void setPeer(MethodDescriptor peer) {
        this.peer = peer;
    }

    public boolean isV2(){
        return v2;
    }

    public Class<?> getRequestType() {
        return requestType;
    }

    public Class<?> getResponseType() {
        return responseType;
    }

    public MethodType getMethodType() {
        return methodType;
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

    public MessageChannel<?> getChannel(Object[] args){
        return MessageChannel.build((Consumer<?>) args[channelIndex]).setV2(v2);
    }

    public void setChannel(Object[] args, MessageChannel<?> channel){
        args[channelIndex] = channel;
    }

    public Object getBean() {
        return bean;
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }




    private static Map<Class<?>, Class<?>> PRIMITIVE = new ConcurrentHashMap<>();
    static {
        PRIMITIVE.put(Boolean.TYPE, Boolean.class);
        PRIMITIVE.put(Character.TYPE, Character.class);
        PRIMITIVE.put(Byte.TYPE, Byte.class);
        PRIMITIVE.put(Short.TYPE, Short.class);
        PRIMITIVE.put(Integer.TYPE, Integer.class);
        PRIMITIVE.put(Long.TYPE, Long.class);
        PRIMITIVE.put(Float.TYPE, Float.class);
        PRIMITIVE.put(Double.TYPE, Double.class);
//        PRIMITIVE.put(Void.TYPE, Void.class);
    }

    private static MethodDescriptor findPeer(List<MethodDescriptor> descriptors, MethodDescriptor desc, MethodType type){
        if (!desc.getMethod().isDefault()){
            throw new ValidationException(desc.getMethod()+"overload only on default method");
        }
        List<MethodDescriptor> peers =descriptors.stream().
                filter(d->d.getMethodType()==type).
                filter(d->d.getMethod().getName().equals(desc.getMethod().getName())).
                collect(Collectors.toList());
        if (peers.isEmpty()){
            throw new ValidationException(desc.getMethod()+"overload peer method not found");
        }
        if (peers.size()>1){
            throw new ValidationException(desc.getMethod()+"overload multiple peer method found");
        }
        desc.setPeer(peers.get(0));
        return desc.getPeer();
    }

    static void validateProtocol(Class<?> type, List<MethodDescriptor> descriptors){
        if (!type.isInterface()){
            throw new ValidationException("invalid type "+type.getName() +" MUST be interface");
        }
        for (MethodDescriptor desc: descriptors){
            MethodType mt = getAnnotationMethodType(desc.getMethod());
            if (mt != null && mt != desc.getMethodType()){
                throw new ValidationException(desc.getMethod()+" Conflict "+mt+"&"+desc.getMethodType());
            }
            if (desc.getMethodType() == MethodType.AsynchronousUnary){
                MethodDescriptor peer = findPeer(descriptors, desc, MethodType.Unary);
                Method unary = peer.getMethod();
                Method async = desc.getMethod();
                Class<?>[] uTypes = unary.getParameterTypes();
                Class<?>[] aTypes = async.getParameterTypes();
                if (uTypes.length != aTypes.length-1){
                    throw new ValidationException(desc.getMethod()+"@AsynchronousUnary peer method argument not match.");
                }
                for (int i = 0; i < uTypes.length; i++) {
                    if (!uTypes[i].equals(aTypes[i])){
                        throw new ValidationException(desc.getMethod()+"@AsynchronousUnary peer method argument not match.");
                    }
                }
                Class<?> c = genericParameter(async, aTypes.length-1);
                if (c != null && (c.equals(unary.getReturnType()) || c.equals(PRIMITIVE.get(unary.getReturnType())))){
                    continue;
                }
                throw new ValidationException(desc.getMethod()+"@AsynchronousUnary peer method argument not match.");
            }
            if (desc.getMethodType() == MethodType.UnaryServerStreaming){
                MethodDescriptor peer = findPeer(descriptors, desc, MethodType.ServerStreaming);
                Method ss = peer.getMethod();
                Method uss = desc.getMethod();
                Class<?>[] ssTypes = ss.getParameterTypes();
                Class<?>[] ussTypes = uss.getParameterTypes();
                if (ussTypes.length != ssTypes.length-1){
                    throw new ValidationException(desc.getMethod()+"@UnaryServerStreaming peer method argument not match.");
                }
                for (int i = 0; i < ussTypes.length; i++) {
                    if (!ussTypes[i].equals(ssTypes[i])){
                        throw new ValidationException(desc.getMethod()+"@UnaryServerStreaming peer method argument not match.");
                    }
                }
                Class<?> c = genericParameter(ss, ssTypes.length-1);
                Class<?> u = genericReturnType(uss);
                if (c != null && c.equals(u) && List.class.equals(uss.getReturnType())){
                    continue;
                }
                throw new ValidationException(desc.getMethod()+"@UnaryServerStreaming peer method argument not match.");
            }
        }
    }

    private static Class<?> genericReturnType(Method method){
        return getGenericType(method.getGenericReturnType());
    }

    private static Class<?> genericParameter(Method method, int index){
        return getGenericType(method.getGenericParameterTypes()[index]);
    }

    private static Class<?> getGenericType(Type type){
        if (type instanceof ParameterizedType){
            Type[] t=((ParameterizedType)type).getActualTypeArguments();
            if (t != null && t.length==1){
                return (Class<?>) t[0];
            }
        }
        return null;
    }

    private static MethodType getAnnotationMethodType(Method method){
        Set<Class<? extends Annotation>> all = new HashSet<>(Arrays.asList(
                Unary.class, ClientStreaming.class, ServerStreaming.class, BidiStreaming.class,
                        AsynchronousUnary.class, UnaryServerStreaming.class, VoidClientStreaming.class));
        List<Annotation> anns = Arrays.stream(method.getDeclaredAnnotations()).
                filter(a->all.contains(a.annotationType())).collect(Collectors.toList());
        switch (anns.size()){
            case 0:
                return null;
            case 1:
                return MethodType.valueOf(anns.get(0).annotationType().getSimpleName());
            default:
                throw new ValidationException(method+" duplicate methodType Annotation");
        }
    }

}
