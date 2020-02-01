package org.jackstaff.grpc;

import org.springframework.core.annotation.AliasFor;

public @interface Client {

    @AliasFor("value")
    String authority() default "";

    @AliasFor("authority")
    String value() default "";

    boolean required() default true;

    Class<? extends Interceptor>[] interceptor() default {};

}
