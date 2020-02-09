package org.jackstaff.grpc;

/**
 * @author reco@jackstaff.org
 */
interface Command {

    int OK = 0;
    int MESSAGE = OK;
    int COMPLETED = 1;
    int TIMEOUT = 2;
    int UNREACHABLE = 3;
    int EXCEPTION = 4;

}
