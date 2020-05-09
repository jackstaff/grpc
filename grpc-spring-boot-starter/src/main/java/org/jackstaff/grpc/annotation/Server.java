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
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 *
 *
 *
 * use it like Spring annotation "@Service",
 * and need set "service" for indicate which "you implements protocol interface" you will publish
 *
 * @author reco@jackstaff.org
 * @see Client
 * @see Interceptor
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Component
public @interface Server {

    /**
     *
     * it's protocol interface type(s), implements by this server component
     */
    @AliasFor("value")
    Class[] service() default {};

    @AliasFor("service")
    Class[] value() default {};

    /**
     * server side interceptor list
     * ps. the Interceptor creator will get/create it from factory
     */
    Class<? extends Interceptor>[] interceptor() default {};

}
