package org.jackstaff.grpc;

import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.exception.StatusException;
import org.jackstaff.grpc.internal.Original;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author reco@jackstaff.org
 */
public class MessageChannel<T> implements Consumer<T> {

    private static final ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();

    private Consumer<T> consumer;
    private T errorMessage;
    private Duration timeout;
    private T timeoutMessage;
    private AtomicInteger closed;

    private StreamObserver<Packet<?>> observer;

    public MessageChannel(Consumer<T> consumer) {
        this.consumer = consumer;
        this.closed = new AtomicInteger(Command.OK);
    }

    public MessageChannel(Consumer<T> consumer, @Nonnull T errorMessage) {
        this(consumer);
        this.setErrorMessage(errorMessage);
    }

    public MessageChannel(Consumer<T> consumer, @Nonnull Duration timeout, @Nonnull T timeoutMessage) {
        this(consumer);
        this.timeout = timeout;
        this.timeoutMessage = timeoutMessage;
    }

    public MessageChannel(Consumer<T> consumer, @Nonnull T errorMessage, @Nonnull Duration timeout, @Nonnull T timeoutMessage) {
        this(consumer, timeout, timeoutMessage);
        this.setErrorMessage(errorMessage);
    }

    MessageChannel(StreamObserver<Packet<?>> observer, int timeoutSeconds){
        this(observer);
        if (timeoutSeconds >0){
            this.timeout = Duration.ofSeconds(timeoutSeconds);
        }
    }

    MessageChannel(StreamObserver<Packet<?>> observer){
        this.closed = new AtomicInteger(Command.OK);
        setObserver(observer);
    }

    public static MessageChannel<?> build(Consumer<?> consumer){
        return consumer instanceof MessageChannel ? (MessageChannel<?>) consumer : new MessageChannel<>(consumer);
    }

    @Override
    public void accept(@Nonnull T t) {
        if (isClosed()) {
            throw new StatusException(closed.toString());
        }
        acceptMessage(t);
    }

    public boolean isClosed() {
        return closed.get() != Command.OK;
    }

    void setErrorMessage(Object errorMessage) {
        this.errorMessage = (T)errorMessage;
    }

    T getErrorMessage() {
        return errorMessage;
    }

    int getTimeoutSeconds() {
        return Optional.ofNullable(timeout).map(Duration::getSeconds).map(Long::intValue).orElse(0);
    }

    MessageChannel<T> ready() {
        if (this.timeout != null && !isClosed()){
            schedule.schedule(()-> {
                if (!isClosed()){
                    this.closed.set(Command.TIMEOUT);
                    Optional.ofNullable(timeoutMessage).ifPresent(msg-> CompletableFuture.runAsync(()->consumer.accept(msg)));
                }
            }, timeout.toMillis(), TimeUnit.MILLISECONDS);
        }
        return this;
    }

    void close(int command) {
        if (!isClosed()) {
            closed.set(command);
            switch (command) {
                case Command.UNREACHABLE:
                    Optional.ofNullable(errorMessage).ifPresent(consumer);
                    break;
                case Command.EXCEPTION:
                case Command.TIMEOUT:
                default:
                    break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    void acceptMessage(Object t) {
        if (t==null || isClosed()) {
            return;
        }
        consumer.accept((T)t);
        if (t instanceof Completable && ((Completable) t).isCompleted()) {
            this.close(Command.COMPLETED);
        }
    }

    MessageChannel<T> link(MessageChannel<?> another) {
        this.closed = another.closed;
        return this;
    }

    void setObserver(StreamObserver<Packet<?>> observer) {
        this.observer = observer;
        Original.accept(observer, ServerCallStreamObserver.class, o->o.setOnCancelHandler(() -> close(Command.UNREACHABLE)));
        this.consumer = info->onNext(Packet.message(info));
    }

    private void onNext(Packet<?> packet){
        if (isClosed()) {
            return;
        }
        try {
            this.observer.onNext(packet);
        }catch (StatusRuntimeException ex){
            this.close(Command.UNREACHABLE);
            throw ex;
        }
    }

    String status() {
        switch (closed.get()) {
            case Command.EXCEPTION: return "EXCEPTION";
            case Command.TIMEOUT: return "TIMEOUT";
            case Command.UNREACHABLE: return "UNREACHABLE";
            case Command.OK:
            default:
                return "OK";
        }
    }

    @Override
    public String toString() {
        return "MessageChannel{" +
                "status='" + status() + '\'' +
                '}';
    }

}
