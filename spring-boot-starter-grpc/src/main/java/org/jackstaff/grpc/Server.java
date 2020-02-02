package org.jackstaff.grpc;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author reco@jackstaff.org
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Server {

    @AliasFor("value")
    Class[] service() default {};

    @AliasFor("service")
    Class[] value() default {};

    Class<? extends Interceptor>[] interceptor() default {};

}
