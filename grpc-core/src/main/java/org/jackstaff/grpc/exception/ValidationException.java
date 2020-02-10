package org.jackstaff.grpc.exception;

/**
 *
 * @author reco@jackstaff.org
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
