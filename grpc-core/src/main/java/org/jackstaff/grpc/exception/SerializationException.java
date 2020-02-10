package org.jackstaff.grpc.exception;

/**
 *
 * @author reco@jackstaff.org
 */
public class SerializationException extends RuntimeException {

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

}
