package org.jackstaff.grpc.annotation;


import java.lang.annotation.*;

/**
 * it's a flag annotation, indicate the ServerStreaming method will call as unary and synchronous.
 * it's alias method,  default + overload ServerStreaming method
 * @author reco@jackstaff.org
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UnaryServerStreaming {

}
