package org.jackstaff.grpc;

/**
 * @author reco@jackstaff.org
 */
public interface Command {

    int MESSAGE = 0;
    int ERROR_MESSAGE = -1;

    int COMPLETED_RANGE_MIN =1;

    int COMPLETED = 1;
    int TIMEOUT = 2;
    int ERROR = 3;

    int COMPLETED_RANGE_MAX =100;


}
