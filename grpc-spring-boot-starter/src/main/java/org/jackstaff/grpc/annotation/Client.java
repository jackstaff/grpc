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

package org.jackstaff.grpc.annotation;

import org.jackstaff.grpc.Interceptor;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 *
 * use it like Spring annotation "@Autowired"
 * @author reco@jackstaff.org
 * @see Server Server.service
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Client {

    /**
     *
     * the micro service / machine /vm name, in configuration
     */
    @AliasFor("value")
    String authority() default "";

    @AliasFor("authority")
    String value() default "";

    /**
     * Declares whether the annotated dependency is required.
     * <p>Defaults to {@code true}.
     */
    boolean required() default true;

    /**
     *the interceptor list for this client component
     * ps. the Interceptor creator will get/create it from factory
     */
    Class<? extends Interceptor>[] interceptor() default {};

}
