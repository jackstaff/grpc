package org.jackstaff.grpc.demo.protocol;

import org.jackstaff.grpc.Transforms;

public class Id {

    private int id;

    public Id() {
    }

    public Id(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private static Id from(DemoProto.Id id) {
        return new Id(id.getId());
    }

    private static DemoProto.Id build(Id id) {
        DemoProto.Id.Builder builder = DemoProto.Id.newBuilder();
        Transforms.build(id::getId, builder::setId);
        return builder.build();
    }

    static {
        Transforms.addTransform(Id.class, DemoProto.Id.class, Id::from, Id::build);
    }

}
