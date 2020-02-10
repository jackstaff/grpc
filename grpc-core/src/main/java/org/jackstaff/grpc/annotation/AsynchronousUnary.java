package org.jackstaff.grpc.annotation;

import java.lang.annotation.*;

/**
 *
 * it's a flag annotation, indicate the unary method will call as asynchronous.
 *
 * @author reco@jackstaff.org
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AsynchronousUnary {

}
