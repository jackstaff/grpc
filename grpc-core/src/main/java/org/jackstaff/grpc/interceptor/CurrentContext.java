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

/**
 * for server side get current thread local context.
 * For example:
 * <pre>
 *   \@Server(service=MyProtocol.class, interceptor={CurrentContext.class})
 *   public class MyProtocolService implements MyProtocol {
 *
 *   public Response sayHello(Request req){
 *       Context context = CurrentContext.get();
 *       String data = context.getMetadata("my_metadata_key");
 *       System.out.println("the meta data from client is: "+data);
 *   }
 *
 *   }
 * </pre>
 * @author reco@jackstaff.org
 * @see Interceptor
 * @see Context
 * @see org.jackstaff.grpc.Server#register
 */
public final class CurrentContext implements Interceptor {

    private static final ThreadLocal<Context> local = new ThreadLocal<>();

    /**
     * @return current thread local Context
     */
    public static Context get(){
        return local.get();
    }

    @Override
    public void before(Context context) {
        local.set(context);
    }

    @Override
    public void returning(Context context, @Nullable Object result) {
        local.set(null);
    }

    @Override
    public void throwing(Context context, @Nonnull Exception ex) {
        local.set(null);
    }

    @Override
    public void recall(Context context, @Nonnull Exception ex) {
        local.set(null);
    }

}
