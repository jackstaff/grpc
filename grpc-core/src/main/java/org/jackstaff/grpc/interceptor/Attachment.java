/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jackstaff.grpc.interceptor;

import org.jackstaff.grpc.Context;
import org.jackstaff.grpc.Interceptor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * for client side attach temp interceptor.
 * For example:
 * <pre>
 *   \@Client("my-server", interceptor={Attachment.class})
 *   private MyProtocol protocol;
 *
 *   Attachment.attach(context-context.setMetadata("my_metadata_key", "my some data special"));
 *   try {
 *       protocol.doSomething();
 *   } finally {
 *      Attachment.detach();
 *   }
 * </pre>
 *
 * @author reco@jackstaff.org
 * @see Interceptor
 * @see Context
 * @see org.jackstaff.grpc.Client#autowired
 */
public final class Attachment implements Interceptor {

    private static final ThreadLocal<Interceptor> attachment = new ThreadLocal<>();

    public static void attach(Consumer<Context> before) {
        attach(new Interceptor() {
            @Override
            public void before(Context context) {
                before.accept(context);
            }
        });
    }

    public static void attach(@Nonnull Interceptor interceptor) {
        attachment.set(interceptor);
    }

    public static void detach(){
        attachment.remove();
    }

    @Override
    public void before(Context context) {
        Optional.ofNullable(attachment.get()).ifPresent(it -> it.before(context));
    }

    @Override
    public void returning(Context context, @Nullable Object result) {
        Optional.ofNullable(attachment.get()).ifPresent(it -> it.returning(context, result));
    }

    @Override
    public void throwing(Context context, @Nonnull Exception ex) {
        Optional.ofNullable(attachment.get()).ifPresent(it -> it.throwing(context, ex));
    }

    @Override
    public void recall(Context context, @Nonnull Exception ex) {
        Optional.ofNullable(attachment.get()).ifPresent(it -> it.recall(context, ex));
    }

}
