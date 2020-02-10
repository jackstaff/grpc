package org.jackstaff.grpc;

import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.exception.StatusException;
import org.jackstaff.grpc.internal.Original;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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

    private Consumer<T> consumer;
    private T errorMessage;
    private Duration timeout;
    private T timeoutMessage;
    private Packet<Throwable> status;
    private StreamObserver<Packet<?>> observer;

    public MessageChannel(Consumer<T> consumer) {
        this.consumer = consumer;
        this.status = new Packet<>();
    }

    public MessageChannel(Consumer<T> consumer, @Nonnull T errorMessage) {
        this(consumer);
        this.errorMessage = errorMessage;
    }

    public MessageChannel(Consumer<T> consumer, @Nonnull Duration timeout, @Nonnull T timeoutMessage) {
        this(consumer);
        this.timeout = timeout;
        this.timeoutMessage = timeoutMessage;
    }

    /**
     * prepare the errorMessage and timeoutMessage for message channel,
     * the consumer will receive this errorMessage if there is an error in the channel (like network error).
     * and the consumer will receive this timeoutMessage if the "done()" is not called in time-out.
     *
     * @param consumer the consumer
     * @param errorMessage the error Message
     * @param timeout the timeout
     * @param timeoutMessage the timeout Message
     */
    public MessageChannel(Consumer<T> consumer, @Nonnull T errorMessage, @Nonnull Duration timeout, @Nonnull T timeoutMessage) {
        this(consumer, timeout, timeoutMessage);
        this.errorMessage = errorMessage;
    }

    MessageChannel(StreamObserver<Packet<?>> observer){
        this(t->{});
        setObserver(observer);
    }

    MessageChannel(StreamObserver<Packet<?>> observer, int timeoutSeconds){
        this(observer);
        if (timeoutSeconds >0){
            this.timeout = Duration.ofSeconds(timeoutSeconds);
        }
    }

    static MessageChannel<?> build(Consumer<?> consumer) {
        return consumer instanceof MessageChannel ? (MessageChannel<?>) consumer : new MessageChannel<>(consumer);
    }

    /**
     * send the message to another side
     * @param message payload
     */
    @Override
    public void accept(@Nonnull T message) {
        if (isClosed()) {
            throw new StatusException(""+status.getCommand());
        }
        acceptMessage(message);
    }

    /**
     * close/complete the channel if the channel is not closed
     */
    public void done() {
        if (!isClosed()){
            if (observer != null){
                onNext(new Packet<>(Command.COMPLETED));
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

    int getTimeoutSeconds() {
        return Optional.ofNullable(timeout).map(Duration::getSeconds).map(Long::intValue).orElse(0);
    }

    MessageChannel<T> ready() {
        if (this.timeout != null && !isClosed()){
            schedule.schedule(()-> {
                if (!isClosed()){
                    this.status.setCommand(Command.TIMEOUT);
                    Optional.ofNullable(timeoutMessage).ifPresent(msg-> CompletableFuture.runAsync(()->consumer.accept(msg)));
                }
            }, timeout.toMillis(), TimeUnit.MILLISECONDS);
        }
        return this;
    }

    void close(int command) {
        if (!isClosed()) {
            status.setCommand(command);
            switch (command) {
                case Command.EXCEPTION:
                case Command.UNREACHABLE:
                    Optional.ofNullable(errorMessage).ifPresent(consumer);
                    break;
                case Command.TIMEOUT:
                default:
                    break;
            }
        }
    }

    void acceptMessage(Object t) {
        if (t !=null && !isClosed()) {
            directAccept(t);
        }
    }

    @SuppressWarnings("unchecked")
    void directAccept(Object t) {
        consumer.accept((T)t);
    }

    MessageChannel<T> link(MessageChannel<?> another) {
        this.status = another.status;
        return this;
    }

    void setObserver(StreamObserver<Packet<?>> observer) {
        this.observer = observer;
        Original.accept(observer, ServerCallStreamObserver.class, o->o.setOnCancelHandler(() -> setError(Command.UNREACHABLE, new StatusException("canceled"))));
        this.consumer = info->onNext(Packet.message(info));
    }

    private void onNext(Packet<?> packet){
        if (isClosed()) {
            return;
        }
        try {
            this.observer.onNext(packet);
        }catch (StatusRuntimeException ex){
            StatusException se = new StatusException("canceled", ex);
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
