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

/**
 * @author reco@jackstaff.org
 */
public enum MethodType {

    UNARY,
    CLIENT_STREAMING,
    SERVER_STREAMING,
    BIDI_STREAMING,

    /**
     * it indicate the unary method will call as asynchronous.
     * it's alias method, default + overload unary method
     *  <pre> {@code
     *     public interface Hello {
     *
     *         int sayHi(String greeting);
     *
     *         //ASYNCHRONOUS_UNARY
     *         default void sayHi(String greeting, Consumer<Integer> reply) {
     *             //it's always equals: "reply.accept(sayHi(greeting));". AUTOMATICALLY.
     *         }
     *
     *     }
     * }</pre>
     *
     */
    ASYNCHRONOUS_UNARY,
    /**
     * it indicate the ServerStreaming method will call as unary and synchronous.
     * it's alias method,  default + overload ServerStreaming method
     */
    BLOCKING_SERVER_STREAMING;

}
