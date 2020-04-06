package org.jackstaff.grpc.demo.protocol;

import org.jackstaff.grpc.Transforms;

public class Count {

    private int count;

    public Count() {
    }

    public Count(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    private static Count from(DemoProto.Count count) {
        return new Count();
    }

    private static DemoProto.Count build(Count count) {
        DemoProto.Count.Builder builder = DemoProto.Count.newBuilder();
        Transforms.build(count::getCount, builder::setCount);
        return builder.build();
    }

    static {
        Transforms.addTransform(Count.class, DemoProto.Count.class, Count::from, Count::build);
    }

}
