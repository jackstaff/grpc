package org.jackstaff.grpc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author reco@jackstaff.org
 */
public interface Interceptor {

    default void before(Context context) throws Exception {
    }

    default void returning(Context context, @Nullable Object result) {

    }

    default void throwing(Context context, @Nonnull Exception ex) {

    }

    default void recall(Context context, @Nonnull Exception ex) {

    }


}
