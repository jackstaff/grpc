package org.jackstaff.grpc.demo;

import org.jackstaff.grpc.MessageChannel;
import org.jackstaff.grpc.annotation.Server;
import org.jackstaff.grpc.demo.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Server(OrderService.class)
public class OrderServer implements OrderService {

    Logger logger = LoggerFactory.getLogger(OrderServer.class);
    ScheduledExecutorService scheduled =Executors.newSingleThreadScheduledExecutor();

    @Override
    public Order getOrderById(Id id) {
        logger.info("OrderServer.getOrderById.id"+id.getId());
        return new Order(id.getId(), UUID.randomUUID().toString(), new Vendor("vendor x"), new ArrayList<>());
    }

    @Override
    public void listOrdersByVendor(Vendor vendor, Consumer<Order> orders) {
        logger.info("OrderServer.listOrdersByVendor.vendor"+vendor.getVendor());
        int count = 5;
        for (int i = 0; i < count; i++) {
            int k = i;
            scheduled.schedule(()->orders.accept(
                    new Order(k, UUID.randomUUID().toString(),
                            new Vendor("X"+vendor.getVendor()),
                            Arrays.asList(new Goods(k, "nam", k*2.5),
                                    new Goods(k+100, "nam100", k+100)))),
                    i+1, TimeUnit.SECONDS);
        }
        scheduled.schedule(((MessageChannel<?>) orders)::done, count+1, TimeUnit.SECONDS);
    }

    @Override
    public Consumer<Order> createOrders(Consumer<Count> result) {
        logger.info("OrderServer.createOrders");
        scheduled.schedule(()->{
            result.accept(new Count(100));
            ((MessageChannel<?>)result).done();
            logger.info("OrderServer.createOrders.done 100");
        }, 10, TimeUnit.SECONDS);
        return order -> {
            logger.info("OrderServer.createOrders.receive "+order.getCode());
        };
    }


}
