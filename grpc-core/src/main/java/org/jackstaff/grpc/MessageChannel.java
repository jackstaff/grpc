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

    @Override
    public void accept(@Nonnull T t) {
        if (isClosed()) {
            throw new StatusException(""+status.getCommand());
        }
        acceptMessage(t);
    }

    public void done() {
        if (!isClosed()){
            if (observer != null){
                onNext(new Packet<>(Command.COMPLETED));
            }
            close(Command.COMPLETED);
        }
    }

    public boolean isClosed() {
        return status.getCommand() != Command.OK;
    }

    public Throwable getError() {
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
        if (status.getCommand() < command){
            status.setCommand(command);
        }
    }

    void acceptMessage(Object t) {
        if (t==null || isClosed()) {
            return;
        }
        directAccept(t);
        if (t instanceof Completable && ((Completable) t).isCompleted()) {
            this.close(Command.COMPLETED);
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
        return "MessageChannel{status='" + status.commandName() + "'}";
    }

}
