package org.jackstaff.grpc;

import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.jackstaff.grpc.exception.StatusException;
import org.jackstaff.grpc.internal.Original;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author reco@jackstaff.org
 */
public class MessageChannel<T> implements Consumer<T> {

    private volatile AtomicInteger closed;
    private Consumer<T> consumer;
    private StreamObserver<Packet<?>> observer;

    public MessageChannel(Consumer<T> consumer) {
        this.consumer = consumer;
        this.closed = new AtomicInteger(Command.OK);
    }

    public boolean isClosed() {
        return closed.get() != Command.OK;
    }

    void close(int command) {
        if (closed.get() < command) {
            closed.set(command);
        }
    }

    @Override
    public void accept(@Nonnull T t) {
        if (isClosed()) {
            throw new StatusException(closed.toString());
        }
        acceptMessage(t);
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

    MessageChannel(StreamObserver<Packet<?>> observer){
        this.closed = new AtomicInteger(Command.OK);
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
