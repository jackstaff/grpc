package org.jackstaff.grpc;

import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.exception.StatusException;
import org.jackstaff.grpc.exception.ValidationException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * MessageChannel implements the Consumer which for (Client/Server/Bidi) Streaming
 * @author reco@jackstaff.org
 *
 */
public class MessageChannel<T> implements Consumer<T> {

    private static final ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();

    private boolean v2;
    private boolean unary;
    private Consumer<T> consumer;
    private T errorMessage;
    private T completeMessage;
    private Duration timeout;
    private T timeoutMessage;
    private Packet<Throwable> status;
    private StreamObserver observer;

    public MessageChannel(Consumer<T> consumer) {
        this.consumer = Optional.ofNullable(consumer).orElse(t->{});
        this.status = new Packet<>();
    }

    public MessageChannel(Consumer<T> consumer, @Nonnull T errorMessage) {
        this(consumer);
        this.errorMessage = errorMessage;
    }

    public MessageChannel(Consumer<T> consumer, @Nonnull T errorMessage, @Nonnull T completeMessage) {
        this(consumer, errorMessage);
        this.completeMessage = completeMessage;
    }

    public MessageChannel(Consumer<T> consumer, @Nonnull T errorMessage, @Nonnull Duration timeout, @Nonnull T timeoutMessage) {
        this(consumer, errorMessage);
        if (timeout.toDays() > 365 || timeout.getSeconds() <1){
            throw new ValidationException("timeout out range "+timeout);
        }
        this.timeout = timeout;
        this.timeoutMessage = timeoutMessage;
    }

    /**
     * prepare the errorMessage and timeoutMessage for message channel,
     * 1. the consumer will receive this errorMessage if there is an error in the channel (like network error).
     * 2. the consumer will receive this timeoutMessage if the "done()" is not called in time-out.
     * 3. the consumer will receive this completeMessage if call "done()"
     * @param consumer the consumer
     * @param errorMessage the error Message
     * @param completeMessage the complete message
     * @param timeout the timeout
     * @param timeoutMessage the timeout Message
     */
    public MessageChannel(Consumer<T> consumer, @Nonnull T errorMessage, @Nonnull T completeMessage, @Nonnull Duration timeout, @Nonnull T timeoutMessage) {
        this(consumer, errorMessage, timeout, timeoutMessage);
        this.completeMessage = completeMessage;
    }

    MessageChannel(StreamObserver<Packet<?>> observer){
        this(t->{});
        this.observer = observer;
        Original.accept(observer, ServerCallStreamObserver.class, o->o.setOnCancelHandler(() -> setError(Command.UNREACHABLE, new StatusException(Command.UNREACHABLE))));
        this.consumer = info->onNext(Packet.message(info));
    }

    MessageChannel(StreamObserver<?> observer, Consumer<T> consumer, long timeoutMillSeconds){
        this(t->{});
        this.observer = observer;
        Original.accept(observer, ServerCallStreamObserver.class, o->o.setOnCancelHandler(() -> setError(Command.UNREACHABLE, new StatusException(Command.UNREACHABLE))));
        this.consumer = consumer;
        if (timeoutMillSeconds >=1000){
            this.timeout = Duration.ofMillis(timeoutMillSeconds-100);
        }
    }

    MessageChannel(StreamObserver<Packet<?>> observer, int timeoutMillSeconds){
        this(observer);
        if (timeoutMillSeconds >=1000){
            this.timeout = Duration.ofMillis(timeoutMillSeconds);
        }
    }

    static <T> MessageChannel<T> build(Consumer<T> consumer) {
        return consumer instanceof MessageChannel ? (MessageChannel<T>) consumer : new MessageChannel<>(consumer);
    }

    /**
     * send the message to another side
     * @param message payload
     */
    @Override
    public void accept(@Nonnull T message) {
        if (isClosed()) {
            throw new StatusException(status.getCommand());
        }
        acceptMessage(message);
    }

    /**
     * close/complete the channel if the channel is not closed.
     * the client will receive "completeMessage" if it's exist
     */
    public void done() {
        if (!isClosed()){
            if (observer != null){
                if (!v2) {
                    onNext(new Packet<>(Command.COMPLETED));
                }
                observer.onCompleted();
            }
            close(Command.COMPLETED);
        }
    }

    /**
     *
     * @return indicate the channel closed status.
     */
    public boolean isClosed() {
        return !status.isOk();
    }

    /**
     *
     * @return exception if channel close and receive error.
     */
    public @Nullable Throwable getError() {
        return status.getPayload();
    }

    void setError(int errorCode, Throwable error) {
        this.status.setPayload(error);
        this.close(errorCode);
    }

    Duration getTimeout() {
        return Optional.ofNullable(timeout).orElse(Duration.ZERO);
    }

    private boolean isV2(){
        return this.v2;
    }

    MessageChannel<T> setV2(boolean v2) {
        this.v2 = true;
        return this;
    }

    MessageChannel<T> ready() {
        if (this.timeout != null && !isClosed()){
            schedule.schedule(()-> {
                if (!isClosed()){
                    this.close(Command.TIMEOUT);
                    if (observer != null){
                        try {
                            if (v2) {
                                this.observer.onNext(new Packet<>(Command.TIMEOUT));
                            }
                        }catch (Exception ignore) {
                        }
                        observer.onCompleted();
                    }
                }
            }, timeout.toMillis(), TimeUnit.MILLISECONDS);
        }
        return this;
    }

    void close(int command) {
        if (!isClosed()) {
            status.setCommand(command);
            switch (command) {
                case Command.COMPLETED:
                    Optional.ofNullable(completeMessage).ifPresent(consumer);
                    break;
                case Command.EXCEPTION:
                case Command.UNREACHABLE:
                    Optional.ofNullable(errorMessage).ifPresent(consumer);
                    break;
                case Command.TIMEOUT:
                    Optional.ofNullable(timeoutMessage).ifPresent(consumer);
                    break;
                default:
                    break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    void acceptMessage(Object t) {
        if (t !=null && !isClosed()) {
            consumer.accept((T)t);
            if (unary){
                done();
            }
        }
    }

    MessageChannel<T> unary() {
        this.unary = true;
        this.completeMessage = null; //for unary, not complete message
        return this;
    }

    MessageChannel<T> link(MessageChannel<?> another) {
        this.status = another.status;
        return this;
    }

    private void onNext(Packet<?> packet){
        if (isClosed()) {
            return;
        }
        try {
            this.observer.onNext(packet);
        }catch (StatusRuntimeException ex){
            StatusException se = new StatusException(ex);
            this.setError(Command.UNREACHABLE, se);
            throw se;
        }
    }

    @Override
    public String toString() {
        return "MessageChannel{" +
                "closed=" + isClosed() +
                ", status=" + status.commandName() +
                Optional.ofNullable(status.getPayload()).map(e->",error="+e).orElse("")+
                '}';
    }


}
