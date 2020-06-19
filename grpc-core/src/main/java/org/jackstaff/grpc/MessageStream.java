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

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.configuration.ClientConfig;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * MessageStream implements the Consumer which for (Client/Server/Bidi/AsyncUnary) Streaming.
 * It's another style of StreamObserver
 * @see io.grpc.stub.StreamObserver
 * @see MessageStatus
 *
 * @author reco@jackstaff.org
 */
public final class MessageStream<T> implements Consumer<T> {

    private final StreamObserver<T> observer;
    private Duration timeout;

    private boolean unary;
    private AtomicReference<Status> status = new AtomicReference<>();

    /**
     * deadline not set, it will use client config's default timeout if set
     * @see ClientConfig#getDefaultTimeout()
     * @param consumer Another style of StreamObserver
     */
    public MessageStream(Consumer<MessageStatus<T>> consumer) {
        this.observer = new MessageObserver<>(
                t -> consumer.accept(new MessageStatus<>(t)),
                e -> consumer.accept(new MessageStatus<>(e)),
                ()-> consumer.accept(new MessageStatus<>()));
    }

    /**
     * @param consumer Another style of StreamObserver
     * @param timeout method deadline
     */
    public MessageStream(Consumer<MessageStatus<T>> consumer, Duration timeout) {
        this(consumer);
        this.timeout = timeout;
    }

    MessageStream(StreamObserver<T> observer){
        this.observer = observer;
        Original.accept(observer, ServerCallStreamObserver.class, o->o.setOnCancelHandler(this::cancelHandler));
    }

    static <T> MessageStream<T> build(Consumer<T> consumer) {
        if (consumer instanceof MessageStream){
            return (MessageStream<T>) consumer;
        }
        return new MessageStream<>(t->{
            if (t.isMessage()){
                consumer.accept(t.getMessage());
            }
        });
    }

    Duration timeout() {
        return Optional.ofNullable(timeout).orElse(Duration.ZERO);
    }

    private void cancelHandler(){
        if (!isClosed()){
            this.status.set(Status.CANCELLED);
        }
    }

    /**
     * send the message to another side
     * @see StreamObserver#onNext
     * @param message payload
     */
    @Override
    public void accept(T message) {
        if (isClosed()){
            throw Status.UNAVAILABLE.withDescription("status error: message stream closed").asRuntimeException();
        }
        observer.onNext(message);
        if (unary){
            done();
        }
    }

    /**
     * @see StreamObserver#onCompleted()
     * close/complete the stream if it is not closed.
     */
    public void done(){
        if (!isClosed()){
            status.set(Status.OK);
            observer.onCompleted();
        }
    }

    /**
     * @see Status#asRuntimeException()
     * @param status error status
     */
    public void error(Status status){
        if (!isClosed()){
            if (status.isOk()){
                throw Status.INVALID_ARGUMENT.asRuntimeException();
            }
            this.status.set(status);
            observer.onError(status.asRuntimeException());
        }
    }

    /**
     * @see StreamObserver#onError
     * @param t throwable
     */
    public void error(Throwable t){
        if (!isClosed()){
            status.set(Status.fromThrowable(t));
            observer.onError(t);
        }
    }

    /**
     *
     * @return indicate the stream closed status.
     */
    public boolean isClosed() {
        return status.get() != null;
    }

    /**
     *
     * @return indicate the stream status.
     */
    public Status getStatus() {
        return Optional.ofNullable(status.get()).orElse(Status.UNKNOWN);
    }

    MessageStream<T> unary() {
        this.unary = true;
        return this;
    }

    MessageStream<T> link(MessageStream<?> another) {
        this.status = another.status;
        return this;
    }

    StreamObserver<T> toStreamObserver(){
        return new MessageObserver<>(this, this::error, this::done);
    }

    @SuppressWarnings("unchecked")
    StreamObserver<Packet<?>> toPacketStreamObserver(){
        return new MessageObserver<>(t-> accept((T) t.getPayload()), this::error, this::done);
    }

    @Override
    public String toString() {
        return "MessageStream{" +
                "isClosed=" + isClosed() +
                ", status=" + getStatus().codeName() +
                '}';
    }

}
