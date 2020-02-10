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
