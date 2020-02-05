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

    private final Metadata.Key<T> key;
    private Context.Key<Metadata> contextKey;
    private Context context;

    HeaderMetadata(Metadata.Key<T> key) {
        this.key = key;
    }

    public static HeaderMetadata<String> ROOT = string("jackstaff-grpc");
    public static HeaderMetadata<byte[]> BINARY_ROOT = binary("jackstaff-grpc-bin");

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
        metadata.merge(headers);
        if (this.context ==null) synchronized (this) {
            this.contextKey = Context.keyWithDefault(ROOT.key.name(), new Metadata());
            this.context = Context.ROOT.withValue(contextKey, new Metadata());
        }
        return context.withValue(contextKey, metadata);
    }

}
