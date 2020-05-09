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

import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author reco@jackstaff.org
 */
@FunctionalInterface
interface Original<T> {

    T getOrigin();

    static Object from(Object source) {
        Object obj =null;
        if (source instanceof Original){
            obj = ((Original<?>) source).getOrigin();
            while (obj instanceof Original){
                obj = ((Original<?>) obj).getOrigin();
            }
        }
        return obj;
    }

    @SuppressWarnings("unchecked")
    static <T> void accept(Object source, Class<T> clazz, Consumer<T> consumer) {
        if (clazz.isInstance(source)){
            consumer.accept((T)source);
            return;
        }
        Optional.ofNullable(from(source)).filter(clazz::isInstance).map(clazz::cast).ifPresent(consumer);
    }

}
