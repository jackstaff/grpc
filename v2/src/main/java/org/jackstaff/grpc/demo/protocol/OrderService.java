package org.jackstaff.grpc.demo.protocol;

import org.jackstaff.grpc.Transforms;
import org.jackstaff.grpc.annotation.*;

import java.util.function.Consumer;

@Protocol
public interface OrderService {

    @Unary
    Order getOrderById(Id id);

    @AsynchronousUnary
    default void getOrderById(Id id, Consumer<Order> result){
        //it's alias method, server don't implement,
    }

    @ServerStreaming
    void listOrdersByVendor(Vendor vendor, Consumer<Order> orderConsumer);

    /**
     *
     //StreamObserver<DemoProto.Order> createOrders(StreamObserver<DemoProto.Count> responseObserver)
     rpc CreateOrders (stream Order) returns ( Count ) {}

     */
    @ClientStreaming
    Consumer<Order> createOrders(Consumer<Count> result);

    String SERVICE_NAME = Transforms.addProtocol(OrderService.class, OrderServiceGrpc.getServiceDescriptor());

}
