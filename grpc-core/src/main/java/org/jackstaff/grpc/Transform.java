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

import io.grpc.stub.StreamObserver;

import java.util.Iterator;

/**
 * Transform for POJO java bean and Protobuf java bean.
 *
 * @author reco@jackstaff.org
 */
public interface Transform<Pojo, Proto> {

    Pojo from(Proto proto);

    Proto build(Pojo pojo);

    StreamObserver<Proto> buildObserver(StreamObserver<Pojo> observer);

    StreamObserver<Pojo> fromObserver(StreamObserver<Proto> observer);

    Iterator<Proto> buildIterator(Iterator<Pojo> iterator);

    Iterator<Pojo> fromIterator(Iterator<Proto> iterator);


}
