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

package org.jackstaff.grpc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Workflow interface that allows for the Client/Server execution chains.
 *  you can register any number of interceptors for any purpose,
 *  like Logging, Credential(Client Side), Authorization(Server Side) and so on.
 *  <p>flow:
 *     <p>1. call before():
 *      {@code
 *          for (int i = 0; i < interceptors.size(); i++) {
 *             try {
 *                 interceptors.get(i).before(context);
 *             }catch (Exception exception){
 *                 for (int j = i-1; j >= 0 ; j--) {
 *                     try {
 *                         interceptors.get(j).recall(context, exception);
 *                     }catch (Exception ignore){
 *                     }
 *                 }
 *                 throw exception;
 *             }
 *         }
 *      }
 *      <p>if before() throw exception, just throw it, return.
 *      <p>2. call invoke() the "target" method.
 *      <p>3. call returning() if invoke() success, OR call throwing() if invoke() throw exception.
 *
 * @author reco@jackstaff.org
 * @see Context
 * @see Client
 * @see Server
 */
public interface Interceptor {

    /**
     * @param context the Context
     * @throws Exception if don't let it go
     */
    default void before(Context context) throws Exception {
    }

    /**
     * will invoke it if call "protocol interface method" success
     * @param context the Context
     * @param result the "protocol interface method" result
     */
    default void returning(Context context, @Nullable Object result) {

    }

    /**
     * will invoke it if call "protocol interface method" throw exception
     * @param context the Context
     * @param ex the "protocol interface method" exception
     */
    default void throwing(Context context, @Nonnull Exception ex) {

    }

    /**
     * will invoke it if call "previous Interceptor .before()" throw exception
     * @param context the Context
     * @param ex the "previous Interceptor .before()" exception
     */
    default void recall(Context context, @Nonnull Exception ex) {

    }


}
