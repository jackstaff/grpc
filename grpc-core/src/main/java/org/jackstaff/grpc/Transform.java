package org.jackstaff.grpc;

import io.grpc.stub.StreamObserver;

import java.util.Iterator;

/**
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
