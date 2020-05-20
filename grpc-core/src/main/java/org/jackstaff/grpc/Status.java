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

import com.google.common.base.Objects;
import io.grpc.Metadata;
import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.internal.HeaderMetadata;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * refactor io.grpc.Status, Code replace with interface for application extends.
 * @see io.grpc.Status
 * @see io.grpc.StatusRuntimeException
 * @see io.grpc.StatusException
 * @see StatusRuntimeException
 * @see StatusException
 * @author reco@jackstaff.org
 */
public class Status {

    /**
     * @see io.grpc.Status.Code
     */
    public interface Code {

        /**
         * it's mapping for StreamObserver.onCompleted
         * @see StreamObserver#onCompleted()
         * @see MessageStatus#getCode()
         */
        int OK = 0;//io.grpc.Status.Code.OK
        int CANCELLED = 1;//io.grpc.Status.Code.CANCELLED
        int UNKNOWN = 2;//io.grpc.Status.Code.UNKNOWN
        int INVALID_ARGUMENT = 3;//io.grpc.Status.Code.INVALID_ARGUMENT
        int DEADLINE_EXCEEDED = 4;//io.grpc.Status.Code.DEADLINE_EXCEEDED
        int NOT_FOUND = 5;//io.grpc.Status.Code.NOT_FOUND
        int ALREADY_EXISTS = 6;//io.grpc.Status.Code.ALREADY_EXISTS
        int PERMISSION_DENIED = 7;//io.grpc.Status.Code.PERMISSION_DENIED
        int RESOURCE_EXHAUSTED = 8;//io.grpc.Status.Code.RESOURCE_EXHAUSTED
        int FAILED_PRECONDITION = 9;//io.grpc.Status.Code.FAILED_PRECONDITION
        int ABORTED = 10;//io.grpc.Status.Code.ABORTED
        int OUT_OF_RANGE = 11;//io.grpc.Status.Code.OUT_OF_RANGE
        int UNIMPLEMENTED = 12;//io.grpc.Status.Code.UNIMPLEMENTED
        int INTERNAL = 13;//io.grpc.Status.Code.INTERNAL;
        int UNAVAILABLE = 14;//io.grpc.Status.Code.UNAVAILABLE
        int DATA_LOSS = 15;//io.grpc.Status.Code.DATA_LOSS;
        int UNAUTHENTICATED = 16;//io.grpc.Status.Code.UNAUTHENTICATED

        /**
         * it's mapping for StreamObserver.onNext
         * @see StreamObserver#onNext
         * @see MessageStatus#getMessage()
         */
        int MESSAGE = 99;

        /**
         * 0..100 is reserved by rpc framework.
         * The application will use values outside this range
         */
        int CODE_RESERVED_RANGE_MIN = 0;
        int CODE_RESERVED_RANGE_MAX = 100;

    }

    private final int code;
    private final String description;
    private final Throwable cause;

    Status(int code) {
        this(code, null, null);
    }

    Status(int code, String description, Throwable cause) {
        this.code = code;
        this.description = description;
        this.cause = cause;
    }

    Status(Status another){
        this.code = another.code;
        this.description = another.description;
        this.cause = another.cause;
    }

    private static final Metadata.Key<String> KEY = HeaderMetadata.stringKey("grpc-status-ext");

    public static final Status OK = new Status(Code.OK);
    public static final Status CANCELLED = new Status(Code.CANCELLED);
    public static final Status UNKNOWN = new Status(Code.UNKNOWN);
    public static final Status INVALID_ARGUMENT = new Status(Code.INVALID_ARGUMENT);
    public static final Status DEADLINE_EXCEEDED = new Status(Code.DEADLINE_EXCEEDED);
    public static final Status NOT_FOUND = new Status(Code.NOT_FOUND);
    public static final Status ALREADY_EXISTS = new Status(Code.ALREADY_EXISTS);
    public static final Status PERMISSION_DENIED = new Status(Code.PERMISSION_DENIED);
    public static final Status UNAUTHENTICATED = new Status(Code.UNAUTHENTICATED);
    public static final Status RESOURCE_EXHAUSTED = new Status(Code.RESOURCE_EXHAUSTED);
    public static final Status FAILED_PRECONDITION =new Status(Code.FAILED_PRECONDITION);
    public static final Status ABORTED = new Status(Code.ABORTED);
    public static final Status OUT_OF_RANGE = new Status(Code.OUT_OF_RANGE);
    public static final Status UNIMPLEMENTED = new Status(Code.UNIMPLEMENTED);
    public static final Status INTERNAL = new Status(Code.INTERNAL);
    public static final Status UNAVAILABLE = new Status(Code.UNAVAILABLE);
    public static final Status DATA_LOSS = new Status(Code.DATA_LOSS);

    public static final Status MESSAGE = new Status(Code.MESSAGE);

    public Status withCause(Throwable cause) {
        if (Objects.equal(this.cause, cause)) {
            return this;
        }
        return new Status(this.code, this.description, cause);
    }

    public Status withDescription(String description) {
        if (Objects.equal(this.description, description)) {
            return this;
        }
        return new Status(this.code, description, this.cause);
    }

    public Status augmentDescription(String additionalDetail) {
        if (additionalDetail == null) {
            return this;
        } else if (this.description == null) {
            return new Status(this.code, additionalDetail, this.cause);
        } else {
            return new Status(this.code, this.description + "\n" + additionalDetail, this.cause);
        }
    }

    /**
     * application customize support
     * @return error code
     */
    public int getCode() {
        return code;
    }

    /**
     * @return error description
     */
    @Nullable
    public String getDescription() {
        return description;
    }

    /**
     *
     * @return error cause
     */
    @Nullable
    public Throwable getCause() {
        return cause;
    }

    public boolean isOk() {
        return Code.OK == code;
    }

    static io.grpc.Status buildStatus(int code, String description, Throwable cause, Metadata trailers){
        io.grpc.Status status = io.grpc.Status.fromCodeValue(code).withDescription(description).withCause(cause);
        if (status.getCode().value() != code) {
            trailers.removeAll(KEY);
            trailers.put(KEY, Integer.toString(code));
        }
        return status;
    }

    public StatusRuntimeException asRuntimeException() {
        return asRuntimeException(null);
    }

    public StatusRuntimeException asRuntimeException(@Nullable Metadata trailers) {
        return new StatusRuntimeException(this, trailers);
    }

    public StatusException asException() {
        return asException(null);
    }

    public StatusException asException(@Nullable Metadata trailers) {
        return new StatusException(this, trailers);
    }

    String codeName(){
        switch (code) {
            case Code.OK: return "OK";
            case Code.CANCELLED: return "CANCELLED";
            case Code.UNKNOWN: return "UNKNOWN";
            case Code.INVALID_ARGUMENT: return "INVALID_ARGUMENT";
            case Code.DEADLINE_EXCEEDED: return "DEADLINE_EXCEEDED";
            case Code.NOT_FOUND: return "NOT_FOUND";
            case Code.ALREADY_EXISTS: return "ALREADY_EXISTS";
            case Code.PERMISSION_DENIED: return "PERMISSION_DENIED";
            case Code.RESOURCE_EXHAUSTED: return "RESOURCE_EXHAUSTED";
            case Code.FAILED_PRECONDITION: return "FAILED_PRECONDITION";
            case Code.ABORTED: return "ABORTED";
            case Code.OUT_OF_RANGE: return "OUT_OF_RANGE";
            case Code.UNIMPLEMENTED: return "UNIMPLEMENTED";
            case Code.INTERNAL: return "INTERNAL";
            case Code.UNAVAILABLE: return "UNAVAILABLE";
            case Code.DATA_LOSS: return "DATA_LOSS";
            case Code.UNAUTHENTICATED: return "UNAUTHENTICATED";
            case Code.MESSAGE: return "MESSAGE";
            default:
                return String.valueOf(code);
        }
    }

    private static Status from(@Nonnull io.grpc.Status status, @Nullable Metadata trailers) {
        int code = Optional.ofNullable(trailers).map(t->t.get(KEY)).
                filter(s->!s.isEmpty()).map(Integer::parseInt).orElseGet(status.getCode()::value);
        return new Status(code, status.getDescription(), status.getCause());
    }

    public static Status fromThrowable(@Nonnull Throwable t) {
        Throwable cause = t;
        while (cause != null) {
            if (cause instanceof StatusException) {
                return from(((StatusException) cause).getStatus(), ((StatusException) cause).getTrailers());
            } else if (cause instanceof StatusRuntimeException) {
                return from(((StatusRuntimeException) cause).getStatus(), ((StatusRuntimeException) cause).getTrailers());
            }
            cause = cause.getCause();
        }
        return UNKNOWN.withCause(t);
    }

    public static Metadata trailersFromThrowable(Throwable t) {
        Metadata trailers = io.grpc.Status.trailersFromThrowable(t);
        if (trailers != null && trailers.containsKey(KEY)){
            Metadata metadata = new Metadata();
            metadata.merge(trailers);
            metadata.removeAll(KEY);
            return metadata;
        }
        return trailers;
    }

    public static Status fromCodeValue(int codeValue) {
        return new Status(codeValue);
    }

    @Override
    public String toString() {
        return "Status{" +
                "code=" + codeName() +
                Utils.string("description", description)+
                Utils.string("cause", cause)+
                '}';
    }

}
