package org.jackstaff.grpc;

/**
 * @author reco@jackstaff.org
 */
public class GrpcException extends RuntimeException {

    private int errorCode;

    public GrpcException(String message) {
        super(message);
        errorCode = ErrorCodes.STUB_ERROR;
    }

    public GrpcException(int errorCode, String message){
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

}
