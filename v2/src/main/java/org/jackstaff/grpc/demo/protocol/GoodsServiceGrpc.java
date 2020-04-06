package org.jackstaff.grpc.demo.protocol;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.28.0)",
    comments = "Source: demo.proto")
public final class GoodsServiceGrpc {

  private GoodsServiceGrpc() {}

  public static final String SERVICE_NAME = "GoodsService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<DemoProto.Goods,
      DemoProto.Count> getCountingGoodsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CountingGoods",
      requestType = DemoProto.Goods.class,
      responseType = DemoProto.Count.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<DemoProto.Goods,
      DemoProto.Count> getCountingGoodsMethod() {
    io.grpc.MethodDescriptor<DemoProto.Goods, DemoProto.Count> getCountingGoodsMethod;
    if ((getCountingGoodsMethod = GoodsServiceGrpc.getCountingGoodsMethod) == null) {
      synchronized (GoodsServiceGrpc.class) {
        if ((getCountingGoodsMethod = GoodsServiceGrpc.getCountingGoodsMethod) == null) {
          GoodsServiceGrpc.getCountingGoodsMethod = getCountingGoodsMethod =
              io.grpc.MethodDescriptor.<DemoProto.Goods, DemoProto.Count>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CountingGoods"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  DemoProto.Goods.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  DemoProto.Count.getDefaultInstance()))
              .setSchemaDescriptor(new GoodsServiceMethodDescriptorSupplier("CountingGoods"))
              .build();
        }
      }
    }
    return getCountingGoodsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static GoodsServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GoodsServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GoodsServiceStub>() {
        @java.lang.Override
        public GoodsServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GoodsServiceStub(channel, callOptions);
        }
      };
    return GoodsServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static GoodsServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GoodsServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GoodsServiceBlockingStub>() {
        @java.lang.Override
        public GoodsServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GoodsServiceBlockingStub(channel, callOptions);
        }
      };
    return GoodsServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static GoodsServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GoodsServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GoodsServiceFutureStub>() {
        @java.lang.Override
        public GoodsServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GoodsServiceFutureStub(channel, callOptions);
        }
      };
    return GoodsServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class GoodsServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public io.grpc.stub.StreamObserver<DemoProto.Goods> countingGoods(
        io.grpc.stub.StreamObserver<DemoProto.Count> responseObserver) {
      return asyncUnimplementedStreamingCall(getCountingGoodsMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getCountingGoodsMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                DemoProto.Goods,
                DemoProto.Count>(
                  this, METHODID_COUNTING_GOODS)))
          .build();
    }
  }

  /**
   */
  public static final class GoodsServiceStub extends io.grpc.stub.AbstractAsyncStub<GoodsServiceStub> {
    private GoodsServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GoodsServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GoodsServiceStub(channel, callOptions);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<DemoProto.Goods> countingGoods(
        io.grpc.stub.StreamObserver<DemoProto.Count> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getCountingGoodsMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   */
  public static final class GoodsServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<GoodsServiceBlockingStub> {
    private GoodsServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GoodsServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GoodsServiceBlockingStub(channel, callOptions);
    }
  }

  /**
   */
  public static final class GoodsServiceFutureStub extends io.grpc.stub.AbstractFutureStub<GoodsServiceFutureStub> {
    private GoodsServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GoodsServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GoodsServiceFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_COUNTING_GOODS = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final GoodsServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(GoodsServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_COUNTING_GOODS:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.countingGoods(
              (io.grpc.stub.StreamObserver<DemoProto.Count>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class GoodsServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    GoodsServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return DemoProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("GoodsService");
    }
  }

  private static final class GoodsServiceFileDescriptorSupplier
      extends GoodsServiceBaseDescriptorSupplier {
    GoodsServiceFileDescriptorSupplier() {}
  }

  private static final class GoodsServiceMethodDescriptorSupplier
      extends GoodsServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    GoodsServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (GoodsServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new GoodsServiceFileDescriptorSupplier())
              .addMethod(getCountingGoodsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
