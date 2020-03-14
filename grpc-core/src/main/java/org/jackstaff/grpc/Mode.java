package org.jackstaff.grpc;

/**
 * @author reco@jackstaff.org
 */
enum Mode {
    Unary,
    UnaryAsynchronous,
    UnaryStreaming,
    ClientStreaming,
    ServerStreaming,
    BidiStreaming,

}
