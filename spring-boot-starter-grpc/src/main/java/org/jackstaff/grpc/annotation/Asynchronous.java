package org.jackstaff.grpc.annotation;

import java.lang.annotation.*;

/**
 * @author reco@jackstaff.org
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Asynchronous {

}
