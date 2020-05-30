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

import org.jackstaff.grpc.annotation.RpcMethod;
import org.jackstaff.grpc.exception.ValidationException;
import org.jackstaff.grpc.internal.InternalGrpc;

import javax.annotation.Nonnull;
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
    private final MethodType annotationType;
    private final int streamIndex;
    private MethodDescriptor peer;
    private Transform requestTransform;
    private Transform responseTransform;
    private io.grpc.MethodDescriptor grpcMethod;
    private String serviceName;

    public MethodDescriptor(Class<?> type, Method method) {
        this(type, method, null, null);
    }

    public MethodDescriptor(Class<?> type, Method method, Object bean, List<Interceptor> interceptors) {
        this.v2 = Utils.isV2(type);
        this.type = type;
        this.method = method;
        this.bean = bean;
        this.interceptors = interceptors;
        this.annotationType = Optional.ofNullable(method.getAnnotation(RpcMethod.class)).map(RpcMethod::methodType).orElse(null);
        this.methodType = checkMethodType(annotationType);
        if (annotationType != null && annotationType != this.methodType){
            throw new ValidationException(method + " annotation invalid for "+ methodType+"/"+annotationType);
        }
        this.streamIndex = findChannelIndex();
        this.grpcMethod = findGrpcMethod();
        this.getTransform();
        this.sign = buildSign();
    }

    private String buildSign(){
        try {
            this.serviceName = (String) type.getField("SERVICE_NAME").get(null);
        }catch (Throwable ignore){
            this.serviceName = type.getSimpleName();
        }
        String name = type.getName()+"/" + method.toString();
        String id= UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)).toString();
        return String.format("/%s/%s/%s", serviceName, method.getName(), id);
    }

    private void getTransform(){
        if (!v2){
            this.requestTransform = Transforms.getPacketTransform();
            this.responseTransform = this.requestTransform;
            return;
        }
        switch (this.methodType){
            case UNARY:
                this.requestTransform = Transforms.getTransform(method.getParameterTypes()[0]);
                this.responseTransform = Transforms.getTransform(method.getReturnType());
                break;
            case BLOCKING_SERVER_STREAMING:
                //it's default + overload
                this.requestTransform = Transforms.getTransform(method.getParameterTypes()[0]);
                this.responseTransform = Transforms.getTransform(genericReturnType(method));
                break;
            case ASYNCHRONOUS_UNARY:
                //it's default,will call peer method (unary), transform same as server streaming.
            case SERVER_STREAMING:
                this.requestTransform = Transforms.getTransform(method.getParameterTypes()[0]);
                this.responseTransform = Transforms.getTransform(genericParameter(method, 1));
                break;
            case CLIENT_STREAMING:
            case BIDI_STREAMING:
                this.requestTransform = Transforms.getTransform(genericReturnType(method));
                this.responseTransform = Transforms.getTransform(genericParameter(method, 0));
                break;
        }
    }

    private io.grpc.MethodDescriptor<?, ?> findGrpcMethod(){
        if (v2){
            return Transforms.getServiceDescriptor(type).getMethods().stream().
                    filter(desc->desc.getFullMethodName().toUpperCase().endsWith("/"+method.getName().toUpperCase())).
                    findAny().orElse(null);
        }
        switch (this.methodType){
            case ASYNCHRONOUS_UNARY:
            case UNARY:
                return InternalGrpc.getUnaryMethod();
            case BLOCKING_SERVER_STREAMING:
            case SERVER_STREAMING:
                return InternalGrpc.getServerStreamingMethod();
            case CLIENT_STREAMING:
                return InternalGrpc.getClientStreamingMethod();
            case BIDI_STREAMING:
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
                    return MethodType.CLIENT_STREAMING;
                }
                if (annotationType == MethodType.BLOCKING_SERVER_STREAMING || (v2 && method.isDefault())){
                    return MethodType.BLOCKING_SERVER_STREAMING;
                }
                return MethodType.UNARY;
            case 1:
                Class<?>[] types = method.getParameterTypes();
                if (!types[types.length-1].equals(Consumer.class)){
                    throw new ValidationException(method+" Consumer must be LAST parameter");
                }
                if (method.getReturnType().equals(Void.TYPE)) {
                    if (annotationType ==MethodType.ASYNCHRONOUS_UNARY || (v2 && method.isDefault())){
                        return MethodType.ASYNCHRONOUS_UNARY;
                    }
                    return MethodType.SERVER_STREAMING;
                }
                if (Consumer.class.equals(method.getReturnType())) {
                    if (annotationType != null){
                        if (annotationType != MethodType.CLIENT_STREAMING && annotationType != MethodType.BIDI_STREAMING){
                            throw new ValidationException(method + " invalid annotation type");
                        }
                        return annotationType;
                    }
                    return MethodType.BIDI_STREAMING;

                    //return Optional.ofNullable(annotationType).map(r-> MethodType.ClientStreaming).orElse(MethodType.BidiStreaming);
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


    MethodType getAnnotationType() {
        return annotationType;
    }

    public MethodType getMethodType() {
        return methodType;
    }

    public boolean isBlockingMethod(){
        switch (methodType) {
            case UNARY:
            case BLOCKING_SERVER_STREAMING:
                return true;
        }
        return false;
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

    public @Nonnull MessageStream<?> getStream(Object[] args){
        Consumer<?> consumer = streamIndex >=0 ? (Consumer<?>) args[streamIndex] : t->{};
        return MessageStream.build(consumer);
    }

    public void setStream(Object[] args, MessageStream<?> stream){
        if (streamIndex >=0) {
            args[streamIndex] = stream;
        }
    }

    public Object getBean() {
        return bean;
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    private static final Map<Class<?>, Class<?>> PRIMITIVE = new ConcurrentHashMap<>();
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
            if (desc.getMethodType() == MethodType.ASYNCHRONOUS_UNARY){
                MethodDescriptor peer = findPeer(descriptors, desc, MethodType.UNARY);
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
            if (desc.getMethodType() == MethodType.BLOCKING_SERVER_STREAMING){
                MethodDescriptor peer = findPeer(descriptors, desc, MethodType.SERVER_STREAMING);
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
            Set<MethodType> types = new HashSet<>(Arrays.asList(MethodType.UNARY, MethodType.CLIENT_STREAMING,
                    MethodType.SERVER_STREAMING, MethodType.BIDI_STREAMING));
            if (descriptors.stream().anyMatch(d->d != desc && types.contains(d.methodType) &&
                    d.getMethod().getName().equalsIgnoreCase(desc.getMethod().getName()))){
                throw new ValidationException(desc.getMethod()+" duplicate name conflict.");
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

}
