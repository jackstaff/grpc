package org.jackstaff.grpc.exception;

/**
 * @author reco@jackstaff.org
 */
public class StatusException extends RuntimeException {

    public StatusException(String message) {
        super(message);
    }

    public StatusException(String message, Throwable cause) {
        super(message, cause);
    }

}
