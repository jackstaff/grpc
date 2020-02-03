package org.jackstaff.grpc.annotation;

import org.jackstaff.grpc.interceptor.Interceptor;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author reco@jackstaff.org
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Client {

    @AliasFor("value")
    String authority() default "";

    @AliasFor("authority")
    String value() default "";

    boolean required() default true;

    Class<? extends Interceptor>[] interceptor() default {};

}
