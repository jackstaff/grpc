package org.jackstaff.grpc.demo;

import org.jackstaff.grpc.MessageChannel;
import org.jackstaff.grpc.annotation.Client;
import org.jackstaff.grpc.demo.protocol.Id;
import org.jackstaff.grpc.demo.protocol.Order;
import org.jackstaff.grpc.demo.protocol.OrderService;
import org.jackstaff.grpc.demo.protocol.Vendor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderClient {

    Logger logger = LoggerFactory.getLogger(OrderClient.class);

    @Client("v2-server")
    OrderService orderService;

    public void go(){
        Order order = orderService.getOrderById(new Id(100));
        logger.info("getOrderById..code.."+order.getCode());
        MessageChannel<Order> channel = new MessageChannel<>(t->{
            logger.info("listOrdersByVendor"+t.getCode()+","+t.getId()+","+t.getVendor().getVendor());
        });
        orderService.listOrdersByVendor(new Vendor("VV"), channel);
    }

}
