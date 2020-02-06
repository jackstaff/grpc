package org.jackstaff.grpc;

import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.exception.StatusException;
import org.jackstaff.grpc.internal.Original;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author reco@jackstaff.org
 */
public class MessageChannel<T> implements Consumer<T> {

    private volatile AtomicInteger closed;
    private Consumer<T> consumer;
    private StreamObserver<Packet<?>> observer;
    private boolean ready;
    private T errorMessage;
    private Map<Integer, Runnable> notify = new ConcurrentHashMap<>();

    MessageChannel() {
        this(t -> {});
    }

    public MessageChannel(Consumer<T> consumer) {
        this.consumer = consumer;
        this.closed = new AtomicInteger(Command.OK);
    }

    MessageChannel(StreamObserver<Packet<?>> observer){
        this.closed = new AtomicInteger(Command.OK);
        setObserver(observer);
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

    public MessageChannel<T> onError(@Nonnull T errorMessage){
        if (ready){
            throw new StatusException("please call before ready");
        }
        this.errorMessage = errorMessage;
        Optional.ofNullable(notify.get(Command.ERROR_MESSAGE)).ifPresent(Runnable::run);
        return this;
    }

    MessageChannel<T> addNotify(int command, Runnable runnable) {
        this.notify.put(command, runnable);
        return this;
    }

    T getErrorMessage() {
        return errorMessage;
    }

    public MessageChannel<T> ready() {
        this.ready = true;
        return this;
    }

    void close(int command) {
        if (closed.get() < command) {
            closed.set(command);
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
            this.close(Command.EXCEPTION);
            throw ex;
        }
    }

}
