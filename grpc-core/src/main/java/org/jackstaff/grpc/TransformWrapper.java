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
import java.util.function.Function;

/**
 * @author reco@jackstaff.org
 */
class TransformWrapper<Pojo, Proto> implements Transform<Pojo, Proto> {

    private final Class<Pojo> pojoType;
    private final Class<Proto> protoType;
    private final Function<Proto, Pojo> from;
    private final Function<Pojo, Proto> build;

    public TransformWrapper(Class<Pojo> pojoType, Class<Proto> protoType,
                            Function<Proto, Pojo> from, Function<Pojo, Proto> build) {
        this.pojoType = pojoType;
        this.protoType = protoType;
        this.from = from;
        this.build = build;
    }

    @Override
    public Pojo from(Proto proto) {
        return from.apply(proto);
    }

    @Override
    public Proto build(Pojo pojo) {
        return build.apply(pojo);
    }

    @Override
    public StreamObserver<Proto> buildObserver(StreamObserver<Pojo> observer){
        return new MessageObserver<>(v->observer.onNext(from.apply(v)), observer::onError, observer::onCompleted, observer);
    }

    @Override
    public StreamObserver<Pojo> fromObserver(StreamObserver<Proto> observer) {
        return new MessageObserver<>(v->observer.onNext(build.apply(v)), observer::onError, observer::onCompleted, observer);
    }

    @Override
    public Iterator<Proto> buildIterator(Iterator<Pojo> iterator) {
        return new MessageIterator<>(iterator::hasNext, ()->build.apply(iterator.next()), iterator);
    }

    @Override
    public Iterator<Pojo> fromIterator(Iterator<Proto> iterator) {
        return new MessageIterator<>(iterator::hasNext, ()->from.apply(iterator.next()), iterator);
    }

}
