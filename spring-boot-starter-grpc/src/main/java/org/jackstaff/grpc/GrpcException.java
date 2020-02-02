package org.jackstaff.grpc;

/**
 * @author reco@jackstaff.org
 */
public class GrpcException extends RuntimeException {

    public GrpcException(String message) {
        super(message);
    }

}
