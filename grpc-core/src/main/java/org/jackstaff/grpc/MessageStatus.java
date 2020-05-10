/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jackstaff.grpc;

/**
 *
 * @see io.grpc.stub.StreamObserver
 * @see Status
 * @see MessageStream
 * @author reco@jackstaff.org
 */
public class MessageStatus<T> extends Status {

    private T message;

    MessageStatus() {
        super(Code.COMPLETED);
    }

    MessageStatus(T message) {
        super(Code.MESSAGE);
        this.message = message;
    }

    MessageStatus(Throwable throwable) {
        super(Status.fromThrowable(throwable));
    }

    /**
     *
     * @return the payload when code == Status.Code.MESSAGE
     */
    public T getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "MessageStatus{" +
                "code=" + codeName() +
                Utils.string("message", message)+
                Utils.string("description", getDescription())+
                Utils.string("cause", getCause())+
                '}';
    }

}
