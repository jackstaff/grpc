package org.jackstaff.grpc.internal;

import java.util.Optional;
import java.util.function.Consumer;

@FunctionalInterface
public interface Original<T> {

    T getOrigin();

    static <T> T from(Object source, Class<T> clazz) {
        return Optional.ofNullable(source instanceof Original ? ((Original<?>) source).getOrigin() : source).
                filter(clazz::isInstance).map(clazz::cast).orElse(null);
    }

    static <T> void accept(Object source, Class<T> clazz, Consumer<T> consumer) {
        Optional.ofNullable(from(source, clazz)).ifPresent(consumer);
    }

}
