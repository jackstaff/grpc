package org.jackstaff.grpc.annotation;


import java.lang.annotation.*;

/**
 * it's a flag annotation, indicate the unary method will call as asynchronous.
 * it's alias method, default + overload unary method
 *  <pre> {@code
 *     public interface Hello {
 *
 *         int sayHi(String greeting);
 *
 *         \@AsynchronousUnary
 *         default void sayHi(String greeting, Consumer<Integer> reply) {
 *             //it's always equals: "reply.accept(sayHi(greeting));". AUTOMATICALLY.
 *         }
 *
 *     }
 * }</pre>
 * @author reco@jackstaff.org
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AsynchronousUnary {

}
