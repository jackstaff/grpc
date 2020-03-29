package org.jackstaff.grpc;

import io.grpc.stub.StreamObserver;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author reco@jackstaff.org
 */
class TransformBuilder<Pojo, Proto> implements Transform<Pojo, Proto> {

    private final Class<Pojo> pojoType;
    private final Class<Proto> protoType;
    private final Function<Proto, Pojo> from;
    private final Function<Pojo, Proto> build;

    public TransformBuilder(Class<Pojo> pojoType, Class<Proto> protoType, Function<Proto, Pojo> from, Function<Pojo, Proto> build) {
        this.pojoType = pojoType;
        this.protoType = protoType;
        this.from = from;
        this.build = build;
    }

    public Class<Pojo> getPojoType() {
        return pojoType;
    }

    public Class<Proto> getProtoType() {
        return protoType;
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
        abstract class X implements StreamObserver<Proto>, Original<StreamObserver<Pojo>> {}
        return new X(){

            @Override
            public void onNext(Proto value) {
                Optional.ofNullable(observer).ifPresent(o->o.onNext(from.apply(value)));
            }

            @Override
            public void onError(Throwable t) {
                Optional.ofNullable(observer).ifPresent(o->o.onError(t));
            }

            @Override
            public void onCompleted() {
                Optional.ofNullable(observer).ifPresent(StreamObserver::onCompleted);
            }

            @Override
            public StreamObserver<Pojo> getOrigin() {
                return observer;
            }
        };
    }

    @Override
    public StreamObserver<Pojo> fromObserver(StreamObserver<Proto> observer) {
        abstract class X implements StreamObserver<Pojo>, Original<StreamObserver<Proto>> {}
        return new X(){
            @Override
            public void onNext(Pojo value) {
                Optional.ofNullable(observer).ifPresent(o->o.onNext(build.apply(value)));
            }

            @Override
            public void onError(Throwable t) {
                Optional.ofNullable(observer).ifPresent(o->o.onError(t));
            }

            @Override
            public void onCompleted() {
                Optional.ofNullable(observer).ifPresent(StreamObserver::onCompleted);
            }

            @Override
            public StreamObserver<Proto> getOrigin() {
                return observer;
            }
        };
    }

    @Override
    public Iterator<Proto> buildIterator(Iterator<Pojo> iterator) {
        abstract class X implements Iterator<Proto>, Original<Iterator<Pojo>>{}
        return new X() {
            @Override
            public boolean hasNext() {
                return Optional.ofNullable(iterator).map(Iterator::hasNext).orElse(false);
            }

            @Override
            public Proto next() {
                return Optional.ofNullable(iterator).map(it->build.apply(it.next())).orElse(null);
            }

            @Override
            public Iterator<Pojo> getOrigin() {
                return iterator;
            }
        };
    }

    @Override
    public Iterator<Pojo> fromIterator(Iterator<Proto> iterator) {
        abstract class X implements Iterator<Pojo>, Original<Iterator<Proto>>{}
        return new X() {

            @Override
            public boolean hasNext() {
                return Optional.ofNullable(iterator).map(Iterator::hasNext).orElse(false);
            }

            @Override
            public Pojo next() {
                return Optional.ofNullable(iterator).map(it->from.apply(it.next())).orElse(null);
            }

            @Override
            public Iterator<Proto> getOrigin() {
                return iterator;
            }

        };
    }

    static Transform<?, ?> identity = new Transform<Object, Object>() {

        @Override
        public Object from(Object object) {
            return object;
        }

        @Override
        public Object build(Object object) {
            return object;
        }

        @Override
        public StreamObserver<Object> buildObserver(StreamObserver<Object> observer) {
            return observer;
        }

        @Override
        public StreamObserver<Object> fromObserver(StreamObserver<Object> observer) {
            return observer;
        }

        @Override
        public Iterator<Object> buildIterator(Iterator<Object> iterator) {
            return iterator;
        }

        @Override
        public Iterator<Object> fromIterator(Iterator<Object> iterator) {
            return iterator;
        }
    };

}
