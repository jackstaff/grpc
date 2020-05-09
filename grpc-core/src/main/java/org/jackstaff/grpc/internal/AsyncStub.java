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

package org.jackstaff.grpc.internal;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.stub.AbstractAsyncStub;
/**
 * @author reco@jackstaff.org
 */
class AsyncStub extends AbstractAsyncStub<AsyncStub> {

    public AsyncStub(Channel channel, CallOptions callOptions) {
        super(channel, callOptions);
    }

    @Override
    public AsyncStub build(Channel channel, CallOptions callOptions) {
        return new AsyncStub(channel, callOptions);
    }

}
