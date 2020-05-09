package org.jackstaff.grpc.demo;

import org.jackstaff.grpc.Status;

public interface ErrorCode extends Status.Code {

    //CODE_RESERVED_RANGE_MAX==100

    int CODE_BAD_GREETING = 1001;
    int CODE_INVALID_ID = 1002;

}
