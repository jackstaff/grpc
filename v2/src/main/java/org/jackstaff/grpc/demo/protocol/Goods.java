package org.jackstaff.grpc.demo.protocol;

import org.jackstaff.grpc.Transforms;

public class Goods {

    private int id;
    private String name;
    private double price;

    public Goods() {
    }

    public Goods(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    private static Goods from(DemoProto.Goods goods) {
        return new Goods(goods.getId(), goods.getName(), goods.getPrice());
    }

    private static DemoProto.Goods build(Goods goods) {
        DemoProto.Goods.Builder builder = DemoProto.Goods.newBuilder();
        Transforms.build(goods::getId, builder::setId);
        Transforms.build(goods::getName, builder::setName);
        Transforms.build(goods::getPrice, builder::setPrice);
        return builder.build();
    }

    static {
        Transforms.addTransform(Goods.class, DemoProto.Goods.class, Goods::from, Goods::build);
    }

}
