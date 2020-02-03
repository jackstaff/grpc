package org.jackstaff.grpc.interceptor;


import org.jackstaff.grpc.Context;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author reco@jackstaff.org
 */
public interface Interceptor {

    default String name(){
        return "";
    }

    default boolean before(Context context) throws Exception {
        return true;
    }

    default void returning(Context context, @Nullable Object result) throws Exception {

    }

    default void throwing(Context context, @Nonnull Exception ex) throws Exception {

    }

    default void recall(Context context) throws Exception {

    }


}
