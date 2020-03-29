package org.jackstaff.grpc;

/**
 * @author reco@jackstaff.org
 */
public enum MethodType {

    Unary,
    ClientStreaming,
    ServerStreaming,
    BidiStreaming,

    AsynchronousUnary,
    UnaryServerStreaming,
    VoidClientStreaming;


}
