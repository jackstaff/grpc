package org.jackstaff.grpc;

import org.jackstaff.grpc.annotation.*;
import org.jackstaff.grpc.exception.ValidationException;
import org.jackstaff.grpc.internal.InternalGrpc;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author reco@jackstaff.org
 */
@SuppressWarnings("rawtypes")
public class MethodDescriptor {

    private final boolean v2;
    private final Class<?> type;
    private final Method method;
    private final Object bean;
    private final List<Interceptor> interceptors;
    private final String sign;
    private final MethodType methodType;
    private final int channelIndex;
    private MethodDescriptor peer;
    private Transform requestTransform;
    private Transform responseTransform;
    private io.grpc.MethodDescriptor grpcMethod;

    public MethodDescriptor(Class<?> type, Method method) {
        this(type, method, null, null);
    }

    public MethodDescriptor(Class<?> type, Method method, Object bean, List<Interceptor> interceptors) {
        this.v2 = type.getAnnotation(Protocol.class) != null;
        this.type = type;
        this.method = method;
        this.bean = bean;
        this.interceptors = interceptors;
        MethodType annotationType = getAnnotationMethodType(method);
        this.methodType = checkMethodType(annotationType);
        if (annotationType != null && annotationType != this.methodType){
            throw new ValidationException(method + " annotation invalid for "+ methodType);
        }
        this.channelIndex = findChannelIndex();
        this.grpcMethod = findGrpcMethod();
        this.getTransform();
        this.sign = buildSign();
    }

    private String buildSign(){
        String name = type.getName()+"/" + method.toString();
        return UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8))+"-"+Math.abs(name.hashCode());
    }

    private void getTransform(){
        if (!v2){
            this.requestTransform = Transforms.getTransform(Packet.class);
            this.responseTransform = this.requestTransform;
            return;
        }
        switch (this.methodType){
            case Unary:
                this.requestTransform = Transforms.getTransform(method.getParameterTypes()[0]);
                this.responseTransform = Transforms.getTransform(method.getReturnType());
                break;
            case UnaryServerStreaming:
                //it's default + overload
                this.requestTransform = Transforms.getTransform(method.getParameterTypes()[0]);
                this.responseTransform = Transforms.getTransform(genericReturnType(method));
                break;
            case AsynchronousUnary:
                //it's default,will call peer method (unary), transform same as server streaming.
            case ServerStreaming:
                this.requestTransform = Transforms.getTransform(method.getParameterTypes()[0]);
                this.responseTransform = Transforms.getTransform(genericParameter(method, 1));
                break;
            case ClientStreaming:
            case BidiStreaming:
                this.requestTransform = Transforms.getTransform(genericReturnType(method));
                this.responseTransform = Transforms.getTransform(genericParameter(method, 0));
                break;
            case VoidClientStreaming:
                //NEVER HAPPEN
                this.requestTransform = Transforms.getTransform(genericReturnType(method));
                this.responseTransform = Transforms.getTransform(Void.TYPE);
                break;
            default:
                break;
        }
    }

    private io.grpc.MethodDescriptor<?, ?> findGrpcMethod(){
        if (v2){
            String end = Optional.of(method.getName()).map(s->"/"+s.substring(0,1).toUpperCase()+s.substring(1)).get();
            return Transforms.getServiceDescriptor(type).getMethods().stream().
                    filter(desc->desc.getFullMethodName().endsWith(end)).findAny().orElse(null);
        }
        switch (this.methodType){
            case AsynchronousUnary:
            case Unary:
                return InternalGrpc.getUnaryMethod();
            case UnaryServerStreaming:
            case ServerStreaming:
                return InternalGrpc.getServerStreamingMethod();
            case VoidClientStreaming:
            case ClientStreaming:
                return InternalGrpc.getClientStreamingMethod();
            case BidiStreaming:
                return InternalGrpc.getBidiStreamingMethod();
            default:
                return null;
        }
    }

    private int findChannelIndex(){
        Class<?>[] types =method.getParameterTypes();
        return IntStream.range(0, types.length).filter(i->types[i].equals(Consumer.class)).findAny().orElse(-1);
    }

    private MethodType checkMethodType(MethodType annotationType) {
        switch ((int) Arrays.stream(method.getParameterTypes()).filter(Consumer.class::equals).count()){
            case 0:
                if (Consumer.class.equals(method.getReturnType())) {
                    return MethodType.VoidClientStreaming;
                }
                if (annotationType == MethodType.UnaryServerStreaming){
                    return MethodType.UnaryServerStreaming;
                }
                return MethodType.Unary;
            case 1:
                Class<?>[] types = method.getParameterTypes();
                if (!types[types.length-1].equals(Consumer.class)){
                    throw new ValidationException(method+" Consumer must be LAST parameter");
                }
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

    @SuppressWarnings("unchecked")
    public <ReqT, RespT> io.grpc.MethodDescriptor<ReqT, RespT> grpcMethod() {
        return grpcMethod;
    }

    @SuppressWarnings("unchecked")
    public <Pojo, Proto> Transform<Pojo, Proto> requestTransform(){
        return requestTransform;
    }

    @SuppressWarnings("unchecked")
    public <Pojo, Proto> Transform<Pojo, Proto> responseTransform(){
        return responseTransform;
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

    public @Nonnull MessageChannel<?> getChannel(Object[] args){
        Consumer<?> consumer = channelIndex>=0 ? (Consumer<?>) args[channelIndex] : t->{};
        return MessageChannel.build(consumer).setV2(v2);
    }

    public void setChannel(Object[] args, MessageChannel<?> channel){
        if (channelIndex >=0) {
            args[channelIndex] = channel;
        }
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
        PRIMITIVE.put(Void.TYPE, Void.class);
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
            Set<MethodType> types = new HashSet<>(Arrays.asList(MethodType.Unary, MethodType.ClientStreaming,
                    MethodType.ServerStreaming, MethodType.BidiStreaming, MethodType.VoidClientStreaming));
            if (descriptors.stream().anyMatch(d->d != desc && types.contains(d.methodType) &&
                    d.getMethod().getName().equals(desc.getMethod().getName()))){
                throw new ValidationException(desc.getMethod()+" overload NOT supported.");
            }
        }
    }

    public static Class<?> genericReturnType(Method method){
        return getGenericType(method.getGenericReturnType());
    }

    public static Class<?> genericParameter(Method method, int index){
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

    private static Set<Class<? extends Annotation>> ANNOTATIONS = new HashSet<>(Arrays.asList(
            Unary.class, ClientStreaming.class, ServerStreaming.class, BidiStreaming.class,
            AsynchronousUnary.class, UnaryServerStreaming.class, VoidClientStreaming.class));

    private static MethodType getAnnotationMethodType(Method method){
        List<Annotation> anns = Arrays.stream(method.getDeclaredAnnotations()).
                filter(a->ANNOTATIONS.contains(a.annotationType())).collect(Collectors.toList());
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
