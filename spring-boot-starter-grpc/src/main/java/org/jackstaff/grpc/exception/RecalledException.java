package org.jackstaff.grpc.exception;

/**
 * @author reco@jackstaff.org
 */
public class RecalledException extends RuntimeException {

    public RecalledException(String message) {
        super(message);
    }

    public RecalledException(String message, Throwable cause) {
        super(message, cause);
    }

}
