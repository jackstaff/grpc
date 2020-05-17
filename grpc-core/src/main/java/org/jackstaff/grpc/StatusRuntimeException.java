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

/**
 * refactor io.grpc.StatusRuntimeException
 * @see Status
 * @see io.grpc.StatusRuntimeException
 * @author reco@jackstaff.org
 */
import io.grpc.Metadata;
import org.jackstaff.grpc.Status;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class StatusRuntimeException extends io.grpc.StatusRuntimeException {

    public StatusRuntimeException(@Nonnull Throwable t) {
        this(Status.fromThrowable(t));
    }

    public StatusRuntimeException(Status status) {
        this(status.getCode(), status.getDescription());
    }

    public StatusRuntimeException(Status status, @Nullable Metadata trailers) {
        this(status.getCode(), status.getDescription(), status.getCause(), Optional.ofNullable(trailers).orElseGet(Metadata::new));
    }

    public StatusRuntimeException(int code) {
        this(code, null);
    }

    public StatusRuntimeException(int code, String message) {
        this(code, message, null);
    }

    public StatusRuntimeException(int code, String message, Throwable cause){
        this(code, message, cause, new Metadata());
    }

    StatusRuntimeException(int code, String message, Throwable cause, @Nonnull Metadata trailers) {
        super(Status.buildStatus(code, message, cause, trailers), trailers);
    }

}
