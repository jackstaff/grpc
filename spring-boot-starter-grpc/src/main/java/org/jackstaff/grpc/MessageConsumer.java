package org.jackstaff.grpc;

import org.jackstaff.grpc.exception.GrpcException;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author reco@jackstaff.org
 */
public class MessageConsumer<T> implements Consumer<T> {

    private AtomicInteger closed;
    private final Consumer<T> consumer;

    public MessageConsumer(Consumer<T> consumer) {
        this.consumer = consumer;
        this.closed = new AtomicInteger(0);
    }

    public boolean isClosed() {
        return closed.get() !=0;
    }

    void close(int status) {
        if (closed.get() < status) {
            closed.set(status);
        }
    }

    @Override
    public void accept(T t) {
        if (isClosed()) {
            throw new GrpcException("streaming closed");
        }
        consumer.accept(t);
        if (t instanceof Completable && ((Completable) t).isCompleted()) {
            this.close(1);
        }
    }

}
