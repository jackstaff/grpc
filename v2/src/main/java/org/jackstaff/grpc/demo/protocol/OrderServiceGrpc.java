package org.jackstaff.grpc.demo.protocol;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.28.0)",
    comments = "Source: demo.proto")
public final class OrderServiceGrpc {

  private OrderServiceGrpc() {}

  public static final String SERVICE_NAME = "OrderService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<DemoProto.Id,
      DemoProto.Order> getGetOrderByIdMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetOrderById",
      requestType = DemoProto.Id.class,
      responseType = DemoProto.Order.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<DemoProto.Id,
      DemoProto.Order> getGetOrderByIdMethod() {
    io.grpc.MethodDescriptor<DemoProto.Id, DemoProto.Order> getGetOrderByIdMethod;
    if ((getGetOrderByIdMethod = OrderServiceGrpc.getGetOrderByIdMethod) == null) {
      synchronized (OrderServiceGrpc.class) {
        if ((getGetOrderByIdMethod = OrderServiceGrpc.getGetOrderByIdMethod) == null) {
          OrderServiceGrpc.getGetOrderByIdMethod = getGetOrderByIdMethod =
              io.grpc.MethodDescriptor.<DemoProto.Id, DemoProto.Order>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetOrderById"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  DemoProto.Id.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  DemoProto.Order.getDefaultInstance()))
              .setSchemaDescriptor(new OrderServiceMethodDescriptorSupplier("GetOrderById"))
              .build();
        }
      }
    }
    return getGetOrderByIdMethod;
  }

  private static volatile io.grpc.MethodDescriptor<DemoProto.Vendor,
      DemoProto.Order> getListOrdersByVendorMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListOrdersByVendor",
      requestType = DemoProto.Vendor.class,
      responseType = DemoProto.Order.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<DemoProto.Vendor,
      DemoProto.Order> getListOrdersByVendorMethod() {
    io.grpc.MethodDescriptor<DemoProto.Vendor, DemoProto.Order> getListOrdersByVendorMethod;
    if ((getListOrdersByVendorMethod = OrderServiceGrpc.getListOrdersByVendorMethod) == null) {
      synchronized (OrderServiceGrpc.class) {
        if ((getListOrdersByVendorMethod = OrderServiceGrpc.getListOrdersByVendorMethod) == null) {
          OrderServiceGrpc.getListOrdersByVendorMethod = getListOrdersByVendorMethod =
              io.grpc.MethodDescriptor.<DemoProto.Vendor, DemoProto.Order>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListOrdersByVendor"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  DemoProto.Vendor.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  DemoProto.Order.getDefaultInstance()))
              .setSchemaDescriptor(new OrderServiceMethodDescriptorSupplier("ListOrdersByVendor"))
              .build();
        }
      }
    }
    return getListOrdersByVendorMethod;
  }

  private static volatile io.grpc.MethodDescriptor<DemoProto.Order,
      DemoProto.Count> getCreateOrdersMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateOrders",
      requestType = DemoProto.Order.class,
      responseType = DemoProto.Count.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<DemoProto.Order,
      DemoProto.Count> getCreateOrdersMethod() {
    io.grpc.MethodDescriptor<DemoProto.Order, DemoProto.Count> getCreateOrdersMethod;
    if ((getCreateOrdersMethod = OrderServiceGrpc.getCreateOrdersMethod) == null) {
      synchronized (OrderServiceGrpc.class) {
        if ((getCreateOrdersMethod = OrderServiceGrpc.getCreateOrdersMethod) == null) {
          OrderServiceGrpc.getCreateOrdersMethod = getCreateOrdersMethod =
              io.grpc.MethodDescriptor.<DemoProto.Order, DemoProto.Count>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CreateOrders"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  DemoProto.Order.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  DemoProto.Count.getDefaultInstance()))
              .setSchemaDescriptor(new OrderServiceMethodDescriptorSupplier("CreateOrders"))
              .build();
        }
      }
    }
    return getCreateOrdersMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static OrderServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<OrderServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<OrderServiceStub>() {
        @java.lang.Override
        public OrderServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new OrderServiceStub(channel, callOptions);
        }
      };
    return OrderServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static OrderServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<OrderServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<OrderServiceBlockingStub>() {
        @java.lang.Override
        public OrderServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new OrderServiceBlockingStub(channel, callOptions);
        }
      };
    return OrderServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static OrderServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<OrderServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<OrderServiceFutureStub>() {
        @java.lang.Override
        public OrderServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new OrderServiceFutureStub(channel, callOptions);
        }
      };
    return OrderServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class OrderServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void getOrderById(DemoProto.Id request,
                             io.grpc.stub.StreamObserver<DemoProto.Order> responseObserver) {
      asyncUnimplementedUnaryCall(getGetOrderByIdMethod(), responseObserver);
    }

    /**
     */
    public void listOrdersByVendor(DemoProto.Vendor request,
                                   io.grpc.stub.StreamObserver<DemoProto.Order> responseObserver) {
      asyncUnimplementedUnaryCall(getListOrdersByVendorMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<DemoProto.Order> createOrders(
        io.grpc.stub.StreamObserver<DemoProto.Count> responseObserver) {
      return asyncUnimplementedStreamingCall(getCreateOrdersMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGetOrderByIdMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                DemoProto.Id,
                DemoProto.Order>(
                  this, METHODID_GET_ORDER_BY_ID)))
          .addMethod(
            getListOrdersByVendorMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                DemoProto.Vendor,
                DemoProto.Order>(
                  this, METHODID_LIST_ORDERS_BY_VENDOR)))
          .addMethod(
            getCreateOrdersMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                DemoProto.Order,
                DemoProto.Count>(
                  this, METHODID_CREATE_ORDERS)))
          .build();
    }
  }

  /**
   */
  public static final class OrderServiceStub extends io.grpc.stub.AbstractAsyncStub<OrderServiceStub> {
    private OrderServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OrderServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new OrderServiceStub(channel, callOptions);
    }

    /**
     */
    public void getOrderById(DemoProto.Id request,
                             io.grpc.stub.StreamObserver<DemoProto.Order> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetOrderByIdMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listOrdersByVendor(DemoProto.Vendor request,
                                   io.grpc.stub.StreamObserver<DemoProto.Order> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getListOrdersByVendorMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<DemoProto.Order> createOrders(
        io.grpc.stub.StreamObserver<DemoProto.Count> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(getCreateOrdersMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   */
  public static final class OrderServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<OrderServiceBlockingStub> {
    private OrderServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OrderServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new OrderServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public DemoProto.Order getOrderById(DemoProto.Id request) {
      return blockingUnaryCall(
          getChannel(), getGetOrderByIdMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<DemoProto.Order> listOrdersByVendor(
        DemoProto.Vendor request) {
      return blockingServerStreamingCall(
          getChannel(), getListOrdersByVendorMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class OrderServiceFutureStub extends io.grpc.stub.AbstractFutureStub<OrderServiceFutureStub> {
    private OrderServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OrderServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new OrderServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<DemoProto.Order> getOrderById(
        DemoProto.Id request) {
      return futureUnaryCall(
          getChannel().newCall(getGetOrderByIdMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_ORDER_BY_ID = 0;
  private static final int METHODID_LIST_ORDERS_BY_VENDOR = 1;
  private static final int METHODID_CREATE_ORDERS = 2;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final OrderServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(OrderServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_ORDER_BY_ID:
          serviceImpl.getOrderById((DemoProto.Id) request,
              (io.grpc.stub.StreamObserver<DemoProto.Order>) responseObserver);
          break;
        case METHODID_LIST_ORDERS_BY_VENDOR:
          serviceImpl.listOrdersByVendor((DemoProto.Vendor) request,
              (io.grpc.stub.StreamObserver<DemoProto.Order>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CREATE_ORDERS:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.createOrders(
              (io.grpc.stub.StreamObserver<DemoProto.Count>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class OrderServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    OrderServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return DemoProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("OrderService");
    }
  }

  private static final class OrderServiceFileDescriptorSupplier
      extends OrderServiceBaseDescriptorSupplier {
    OrderServiceFileDescriptorSupplier() {}
  }

  private static final class OrderServiceMethodDescriptorSupplier
      extends OrderServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    OrderServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (OrderServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new OrderServiceFileDescriptorSupplier())
              .addMethod(getGetOrderByIdMethod())
              .addMethod(getListOrdersByVendorMethod())
              .addMethod(getCreateOrdersMethod())
              .build();
        }
      }
    }
    return result;
  }
}
