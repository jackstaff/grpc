package org.jackstaff.grpc;

import io.grpc.Internal;

/**
 * @author reco@jackstaff.org
 */
@Internal
enum Mode {
    Unary,
    UnaryAsynchronous,
    UnaryStreaming,
    ClientStreaming,
    ServerStreaming,
    BiStreaming,

}
