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

import io.grpc.Metadata;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * refactor io.grpc.StatusException
 * @see Status
 * @see io.grpc.StatusException
 * @author reco@jackstaff.org
 */
public final class StatusException extends io.grpc.StatusException {

    public StatusException(@Nonnull Throwable t) {
        this(Status.fromThrowable(t));
    }

    public StatusException(Status status) {
        this(status.getCode(), status.getDescription());
    }

    public StatusException(Status status, @Nullable Metadata trailers) {
        this(status.getCode(), status.getDescription(), status.getCause(), Optional.ofNullable(trailers).orElseGet(Metadata::new));
    }

    public StatusException(int code) {
        this(code, null);
    }

    public StatusException(int code, String message) {
        this(code, message, null);
    }

    public StatusException(int code, String message, Throwable cause){
        this(code, message, cause, new Metadata());
    }

    StatusException(int code, String message, Throwable cause, @Nonnull Metadata trailers) {
        super(Status.buildStatus(code, message, cause, trailers), trailers);
    }

    @Override
    public String getMessage() {
        Status status = Status.fromThrowable(this);
        return Optional.ofNullable(status.getDescription()).map(desc->status.codeName() + ": " + desc).orElseGet(status::codeName);
    }

}
