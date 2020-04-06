package org.jackstaff.grpc.demo.protocol;

import org.jackstaff.grpc.Transforms;

import java.util.List;
import java.util.function.Function;

public class Order {

    private int id;
    private String code;
    private Vendor vendor;
    private List<Goods> goods;

    public Order() {
    }

    public Order(int id, String code, Vendor vendor, List<Goods> goods) {
        this.id = id;
        this.code = code;
        this.vendor = vendor;
        this.goods = goods;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public List<Goods> getGoods() {
        return goods;
    }

    public void setGoods(List<Goods> goods) {
        this.goods = goods;
    }


    private static Order from(DemoProto.Order order) {
        return new Order(
                order.getId(),
                order.getCode(),
                Transforms.from(order::getVendor),
                Transforms.from(DemoProto.Goods.class, order::getGoodsList));
    }

    private static DemoProto.Order build(Order order) {
        DemoProto.Order.Builder builder = DemoProto.Order.newBuilder();
        Transforms.build(order::getId, builder::setId);
        Transforms.build(order::getCode, builder::setCode);
        Transforms.build(Vendor.class, order::getVendor, (Function<DemoProto.Vendor, ?>) builder::setVendor);
        Transforms.builds(Goods.class, order::getGoods, builder::addAllGoods);
        return builder.build();
    }

    static {
        Transforms.addTransform(Order.class, DemoProto.Order.class, Order::from, Order::build);
    }

}
