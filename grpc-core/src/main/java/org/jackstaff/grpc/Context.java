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

import org.jackstaff.grpc.internal.HeaderMetadata;
import org.jackstaff.grpc.internal.Stub;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Context for Interceptor
 * @author reco@jackstaff.org
 * @see Interceptor
 */
public final class Context {

    private final Object[] arguments;
    private final Object target;
    private final Map<Object, Object> attributes;
    private final MethodDescriptor methodDescriptor;
    private final Stub<?,?,?> stub;
    private final io.grpc.Context context;

    private Context(MethodDescriptor methodDescriptor, Object[] arguments, Object target, io.grpc.Context context, Stub<?,?,?> stub){
        this.attributes = new ConcurrentHashMap<>();
        this.methodDescriptor = methodDescriptor;
        this.arguments = Optional.ofNullable(arguments).orElse(new Object[0]);
        this.target = target;
        this.stub = stub;
        this.context = context;
        //java.util.stream.Collector
//        com.google.common.collect.ImmutableSet.
    }

    Context(MethodDescriptor methodDescriptor, Object[] arguments, Object target, Stub<?,?,?> stub){
        this(methodDescriptor, arguments, target, null, stub);
    }

    Context(MethodDescriptor methodDescriptor, Object[] arguments, Object target){
        this(methodDescriptor, arguments, target, io.grpc.Context.current(), null);
    }

    /**
     *
     * @return the protocol interface
     */
    public Class<?> getType() {
        return methodDescriptor.getType();
    }

    /**
     *
     * @return the protocol interface's method
     */
    public Method getMethod() {
        return methodDescriptor.getMethod();
    }

    /**
     *
     * @return call method's arguments
     */
    public Object[] getArguments() {
        return arguments.clone();
    }

    /**
     *
     * @return the target bean
     */
    public Object getTarget() {
        return target;
    }

    /**
     * get the local value by key
     * eg.
     * in UseTimeLoggerInterceptor.before(), set the time stamp: setAttribute(this, System.nanoTime()),
     * and in UseTimeLoggerInterceptor.returning, get it: getAttribute(this).
     * @param key key
     * @param <T> value type
     * @return value
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(Object key) {
        return (T) attributes.get(key);
    }

    /**
     * save the local key/value
     * eg.
     * in UseTimeLoggerInterceptor.before(), set the time stamp: setAttribute(this, System.nanoTime()),
     * and in UseTimeLoggerInterceptor.returning, get it: getAttribute(this).
     * @param key key
     * @param value value
     */
    public void setAttribute(Object key, Object value) {
        attributes.put(key, value);
    }

    /**
     * get the metadata from gRPC http header
     * only valid in server side
     * @param name name in header, ignore case
     * @return value
     */
    public String getMetadata(String name) {
        return Optional.ofNullable(context).map(ctx-> HeaderMetadata.stringValue(ctx, name)).orElse(null);
    }

    /**
     * set the metadata into gRPC http header
     * only valid in client side
     * @param name name in header, ignore case
     * @param value value
     */
    public void setMetadata(String name, @Nonnull String value) {
        if (stub !=null){
            stub.attach(name, value);
        }
    }

    MethodDescriptor getMethodDescriptor() {
        return methodDescriptor;
    }

    Context setStream(MessageStream<?> stream){
        methodDescriptor.setStream(arguments, stream);
        return this;
    }

}
