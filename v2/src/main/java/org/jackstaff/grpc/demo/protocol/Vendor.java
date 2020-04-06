package org.jackstaff.grpc.demo.protocol;

import org.jackstaff.grpc.Transforms;

public class Vendor {

    private String vendor;

    public Vendor() {
    }

    public Vendor(String vendor) {
        this.vendor = vendor;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    private static Vendor from(DemoProto.Vendor vendor) {
        return new Vendor(vendor.getVendor());
    }

    private static DemoProto.Vendor build(Vendor vendor) {
        DemoProto.Vendor.Builder builder = DemoProto.Vendor.newBuilder();
        Transforms.build(vendor::getVendor, builder::setVendor);
        return builder.build();
    }

    static {
        Transforms.addTransform(Vendor.class, DemoProto.Vendor.class, Vendor::from, Vendor::build);
    }

}
