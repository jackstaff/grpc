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

package org.jackstaff.grpc.internal;

import io.grpc.Attributes;
import io.grpc.Context;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.MetadataUtils;

import java.util.Optional;

/**
 * @author reco@jackstaff.org
 */
public class HeaderMetadata<T> {

    public static final String AUTHORITY = "authority";
    public static final String REMOTE_ADDR = "remote-addr";
    public static final String LOCAL_ADDR = "local-addr";

    private static final String JACKSTAFF = "grpc-jackstaff";

    private final Metadata.Key<T> key;
    private final Context.Key<Metadata> contextKey;
    private final Context context;

    HeaderMetadata(Metadata.Key<T> key) {
        this.key = key;
        this.contextKey = Context.key(JACKSTAFF);
        this.context = Context.ROOT.withValue(contextKey, new Metadata());
    }

    public static HeaderMetadata<String> ROOT = string(JACKSTAFF);
    public static HeaderMetadata<byte[]> BINARY_ROOT = binary(JACKSTAFF+Metadata.BINARY_HEADER_SUFFIX);

    public static HeaderMetadata<String> string(String name) {
        return new HeaderMetadata<>(stringKey(name));
    }

    public static HeaderMetadata<byte[]> binary(String name) {
        return new HeaderMetadata<>(binaryKey(name));
    }

    public static Metadata.Key<String> stringKey(String name){
        return Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER);
    }

    public static Metadata.Key<byte[]> binaryKey(String name){
        return Metadata.Key.of(name, Metadata.BINARY_BYTE_MARSHALLER);
    }

    static <T, S extends AbstractStub<S>> S attach(S stub, Metadata.Key<T> key, T value){
        Metadata headers = new Metadata();
        headers.put(key, value);
        return MetadataUtils.attachHeaders(stub, headers);
    }

    public static <S extends AbstractStub<S>> S attachString(S stub, String name, String value){
        return attach(stub, stringKey(name), value);
    }

    public static <S extends AbstractStub<S>> S attachBinary(S stub, String name, byte[] value){
        return attach(stub, binaryKey(name), value);
    }

    static <T> T getValue(Metadata.Key<T> key){
        return Optional.ofNullable(ROOT.contextKey.get()).map(ctx->ctx.get(key)).orElse(null);
    }

    public static byte[] binaryValue(String name){
        return getValue(binaryKey(name));
    }

    public static String stringValue(String name){
        return getValue(stringKey(name));
    }

    <S extends AbstractStub<S>> S attach(S stub, T value){
        Metadata headers = new Metadata();
        headers.put(key, value);
        return MetadataUtils.attachHeaders(stub, headers);
    }

    public T getValue(){
        return getValue(key);
    }

    public Context capture(String authority, Attributes attributes, Metadata headers){
        Metadata metadata = new Metadata();
        Optional.ofNullable(authority).filter(a->!a.isEmpty()).
                ifPresent(a->metadata.put(stringKey(AUTHORITY), authority));
        Optional.ofNullable(attributes.get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR)).map(Object::toString).
                ifPresent(addr->metadata.put(stringKey(REMOTE_ADDR), addr));
        Optional.ofNullable(attributes.get(Grpc.TRANSPORT_ATTR_LOCAL_ADDR)).map(Object::toString).
                ifPresent(addr->metadata.put(stringKey(LOCAL_ADDR), addr));
        metadata.merge(headers);
        return context.withValue(contextKey, metadata);
    }

}
