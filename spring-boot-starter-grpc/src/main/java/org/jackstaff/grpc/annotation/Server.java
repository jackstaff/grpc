package org.jackstaff.grpc.annotation;

import org.jackstaff.grpc.interceptor.Interceptor;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author reco@jackstaff.org
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Component
public @interface Server {

    @AliasFor("value")
    Class[] service() default {};

    @AliasFor("service")
    Class[] value() default {};

    Class<? extends Interceptor>[] interceptor() default {};

}
