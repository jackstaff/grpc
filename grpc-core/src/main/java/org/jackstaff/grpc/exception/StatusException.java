package org.jackstaff.grpc.exception;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.jackstaff.grpc.Packet;

/**
 *
 * @author reco@jackstaff.org
 */
public class StatusException extends RuntimeException {

    public StatusException(int status) {
        super(Packet.commandName(status));
    }

    public StatusException(String message, Throwable cause) {
        super(message, cause);
    }

    public StatusException(StatusRuntimeException sre){
        super(sre.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED ?
                Packet.commandName(Packet.TIMEOUT) : sre.getMessage());
    }

}
