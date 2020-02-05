package org.jackstaff.grpc.internal;

import java.util.Optional;
import java.util.function.Consumer;

@FunctionalInterface
public interface Original<T> {

    T getOrigin();

    @SuppressWarnings("rawtypes, unchecked")
    static <T> T from(Object source, Class<T> clazz) {
        return (T)Optional.ofNullable(source instanceof Original ? ((Original) source).getOrigin() : source).
                filter(result -> clazz.isAssignableFrom(result.getClass())).orElse(null);
    }

    static <T> void accept(Object source, Class<T> clazz, Consumer<T> consumer) {
        Optional.ofNullable(from(source, clazz)).ifPresent(consumer);
    }

}
