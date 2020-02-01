package org.jackstaff.grpc;

import org.springframework.core.annotation.AliasFor;

public @interface Server {

    @AliasFor("value")
    Class[] service() default {};

    @AliasFor("service")
    Class[] value() default {};

    Class<? extends Interceptor>[] interceptor() default {};

}
