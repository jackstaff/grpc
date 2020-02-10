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
