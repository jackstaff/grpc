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

import io.grpc.stub.StreamObserver;

import java.util.function.Consumer;

/**
 * @author reco@jackstaff.org
 */
class MessageObserver<T> implements StreamObserver<T>, Original<StreamObserver<?>> {

    private final Consumer<T> onNext;
    private final Consumer<Throwable> onError;
    private final Runnable onCompleted;
    private final StreamObserver<?> origin;

    public MessageObserver(Consumer<T> onNext, Consumer<Throwable> onError, Runnable onCompleted, StreamObserver<?> origin) {
        this.onNext = onNext;
        this.onError = onError;
        this.onCompleted = onCompleted;
        this.origin = origin;
    }

    public MessageObserver(StreamObserver<Packet<?>> observer) {
        this(t -> observer.onNext(Packet.ok(t)), observer::onError, observer::onCompleted, observer);
    }

    public MessageObserver(Consumer<T> onNext, Consumer<Throwable> onError, Runnable onCompleted) {
        this(onNext, onError, onCompleted, null);
    }

    @Override
    public void onNext(T value) {
        this.onNext.accept(value);
    }

    @Override
    public void onError(Throwable t) {
        this.onError.accept(Utils.throwable(t));
    }

    @Override
    public void onCompleted() {
        this.onCompleted.run();
    }

    @Override
    public StreamObserver<?> getOrigin() {
        return origin;
    }

}
