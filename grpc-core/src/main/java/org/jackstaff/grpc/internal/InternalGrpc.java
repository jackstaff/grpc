package org.jackstaff.grpc.internal;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.*;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.*;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.26.0)",
    comments = "Source: internal.proto")
public final class InternalGrpc {

  private InternalGrpc() {}

  public static final String SERVICE_NAME = "Internal";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.jackstaff.grpc.internal.InternalProto.Packet,
      org.jackstaff.grpc.internal.InternalProto.Packet> getUnaryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Unary",
      requestType = org.jackstaff.grpc.internal.InternalProto.Packet.class,
      responseType = org.jackstaff.grpc.internal.InternalProto.Packet.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.jackstaff.grpc.internal.InternalProto.Packet,
      org.jackstaff.grpc.internal.InternalProto.Packet> getUnaryMethod() {
    io.grpc.MethodDescriptor<org.jackstaff.grpc.internal.InternalProto.Packet, org.jackstaff.grpc.internal.InternalProto.Packet> getUnaryMethod;
    if ((getUnaryMethod = InternalGrpc.getUnaryMethod) == null) {
      synchronized (InternalGrpc.class) {
        if ((getUnaryMethod = InternalGrpc.getUnaryMethod) == null) {
          InternalGrpc.getUnaryMethod = getUnaryMethod =
              io.grpc.MethodDescriptor.<org.jackstaff.grpc.internal.InternalProto.Packet, org.jackstaff.grpc.internal.InternalProto.Packet>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Unary"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.jackstaff.grpc.internal.InternalProto.Packet.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.jackstaff.grpc.internal.InternalProto.Packet.getDefaultInstance()))
              .setSchemaDescriptor(new InternalMethodDescriptorSupplier("Unary"))
              .build();
        }
      }
    }
    return getUnaryMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.jackstaff.grpc.internal.InternalProto.Packet,
      org.jackstaff.grpc.internal.InternalProto.Packet> getServerStreamingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ServerStreaming",
      requestType = org.jackstaff.grpc.internal.InternalProto.Packet.class,
      responseType = org.jackstaff.grpc.internal.InternalProto.Packet.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<org.jackstaff.grpc.internal.InternalProto.Packet,
      org.jackstaff.grpc.internal.InternalProto.Packet> getServerStreamingMethod() {
    io.grpc.MethodDescriptor<org.jackstaff.grpc.internal.InternalProto.Packet, org.jackstaff.grpc.internal.InternalProto.Packet> getServerStreamingMethod;
    if ((getServerStreamingMethod = InternalGrpc.getServerStreamingMethod) == null) {
      synchronized (InternalGrpc.class) {
        if ((getServerStreamingMethod = InternalGrpc.getServerStreamingMethod) == null) {
          InternalGrpc.getServerStreamingMethod = getServerStreamingMethod =
              io.grpc.MethodDescriptor.<org.jackstaff.grpc.internal.InternalProto.Packet, org.jackstaff.grpc.internal.InternalProto.Packet>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ServerStreaming"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.jackstaff.grpc.internal.InternalProto.Packet.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.jackstaff.grpc.internal.InternalProto.Packet.getDefaultInstance()))
              .setSchemaDescriptor(new InternalMethodDescriptorSupplier("ServerStreaming"))
              .build();
        }
      }
    }
    return getServerStreamingMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.jackstaff.grpc.internal.InternalProto.Packet,
      org.jackstaff.grpc.internal.InternalProto.Packet> getClientStreamingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ClientStreaming",
      requestType = org.jackstaff.grpc.internal.InternalProto.Packet.class,
      responseType = org.jackstaff.grpc.internal.InternalProto.Packet.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<org.jackstaff.grpc.internal.InternalProto.Packet,
      org.jackstaff.grpc.internal.InternalProto.Packet> getClientStreamingMethod() {
    io.grpc.MethodDescriptor<org.jackstaff.grpc.internal.InternalProto.Packet, org.jackstaff.grpc.internal.InternalProto.Packet> getClientStreamingMethod;
    if ((getClientStreamingMethod = InternalGrpc.getClientStreamingMethod) == null) {
      synchronized (InternalGrpc.class) {
        if ((getClientStreamingMethod = InternalGrpc.getClientStreamingMethod) == null) {
          InternalGrpc.getClientStreamingMethod = getClientStreamingMethod =
              io.grpc.MethodDescriptor.<org.jackstaff.grpc.internal.InternalProto.Packet, org.jackstaff.grpc.internal.InternalProto.Packet>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ClientStreaming"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.jackstaff.grpc.internal.InternalProto.Packet.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.jackstaff.grpc.internal.InternalProto.Packet.getDefaultInstance()))
              .setSchemaDescriptor(new InternalMethodDescriptorSupplier("ClientStreaming"))
              .build();
        }
      }
    }
    return getClientStreamingMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.jackstaff.grpc.internal.InternalProto.Packet,
      org.jackstaff.grpc.internal.InternalProto.Packet> getBidiStreamingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "BidiStreaming",
      requestType = org.jackstaff.grpc.internal.InternalProto.Packet.class,
      responseType = org.jackstaff.grpc.internal.InternalProto.Packet.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<org.jackstaff.grpc.internal.InternalProto.Packet,
      org.jackstaff.grpc.internal.InternalProto.Packet> getBidiStreamingMethod() {
    io.grpc.MethodDescriptor<org.jackstaff.grpc.internal.InternalProto.Packet, org.jackstaff.grpc.internal.InternalProto.Packet> getBidiStreamingMethod;
    if ((getBidiStreamingMethod = InternalGrpc.getBidiStreamingMethod) == null) {
      synchronized (InternalGrpc.class) {
        if ((getBidiStreamingMethod = InternalGrpc.getBidiStreamingMethod) == null) {
          InternalGrpc.getBidiStreamingMethod = getBidiStreamingMethod =
              io.grpc.MethodDescriptor.<org.jackstaff.grpc.internal.InternalProto.Packet, org.jackstaff.grpc.internal.InternalProto.Packet>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "BidiStreaming"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.jackstaff.grpc.internal.InternalProto.Packet.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.jackstaff.grpc.internal.InternalProto.Packet.getDefaultInstance()))
              .setSchemaDescriptor(new InternalMethodDescriptorSupplier("BidiStreaming"))
              .build();
        }
      }
    }
    return getBidiStreamingMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static InternalStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<InternalStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<InternalStub>() {
        @java.lang.Override
        public InternalStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new InternalStub(channel, callOptions);
        }
      };
    return InternalStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static InternalBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<InternalBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<InternalBlockingStub>() {
        @java.lang.Override
        public InternalBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new InternalBlockingStub(channel, callOptions);
        }
      };
    return InternalBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static InternalFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<InternalFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<InternalFutureStub>() {
        @java.lang.Override
        public InternalFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new InternalFutureStub(channel, callOptions);
        }
      };
    return InternalFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class InternalImplBase implements io.grpc.BindableService {

    /**
     */
    public void unary(org.jackstaff.grpc.internal.InternalProto.Packet request,
        io.grpc.stub.StreamObserver<org.jackstaff.grpc.internal.InternalProto.Packet> responseObserver) {
      asyncUnimplementedUnaryCall(getUnaryMethod(), responseObserver);
    }

    /**
     */
    public void serverStreaming(org.jackstaff.grpc.internal.InternalProto.Packet request,
        io.grpc.stub.StreamObserver<org.jackstaff.grpc.internal.InternalProto.Packet> responseObserver) {
      asyncUnimplementedUnaryCall(getServerStreamingMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<org.jackstaff.grpc.internal.InternalProto.Packet> clientStreaming(
        io.grpc.stub.StreamObserver<org.jackstaff.grpc.internal.InternalProto.Packet> responseObserver) {
      return asyncUnimplementedStreamingCall(getClientStreamingMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<org.jackstaff.grpc.internal.InternalProto.Packet> bidiStreaming(
        io.grpc.stub.StreamObserver<org.jackstaff.grpc.internal.InternalProto.Packet> responseObserver) {
      return asyncUnimplementedStreamingCall(getBidiStreamingMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getUnaryMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.jackstaff.grpc.internal.InternalProto.Packet,
                org.jackstaff.grpc.internal.InternalProto.Packet>(
                  this, METHODID_UNARY)))
          .addMethod(
            getServerStreamingMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                org.jackstaff.grpc.internal.InternalProto.Packet,
                org.jackstaff.grpc.internal.InternalProto.Packet>(
                  this, METHODID_SERVER_STREAMING)))
          .addMethod(
            getClientStreamingMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                org.jackstaff.grpc.internal.InternalProto.Packet,
                org.jackstaff.grpc.internal.InternalProto.Packet>(
                  this, METHODID_CLIENT_STREAMING)))
          .addMethod(
            getBidiStreamingMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                org.jackstaff.grpc.internal.InternalProto.Packet,
                org.jackstaff.grpc.internal.InternalProto.Packet>(
                  this, METHODID_BIDI_STREAMING)))
          .build();
    }
  }

  /**
   */
  public static final class InternalStub extends io.grpc.stub.AbstractAsyncStub<InternalStub> {
    private InternalStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected InternalStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new InternalStub(channel, callOptions);
    }

    /**
     */
    public void unary(org.jackstaff.grpc.internal.InternalProto.Packet request,
        io.grpc.stub.StreamObserver<org.jackstaff.grpc.internal.InternalProto.Packet> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getUnaryMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void serverStreaming(org.jackstaff.grpc.internal.InternalProto.Packet request,
        io.grpc.stub.StreamObserver<org.jackstaff.grpc.internal.InternalProto.Packet> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getServerStreamingMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<org.jackstaff.grpc.internal.InternalProto.Packet> clientStreaming(
        io.grpc.stub.StreamObserver<org.jackstaff.grpc.internal.InternalProto.Packet> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(getClientStreamingMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<org.jackstaff.grpc.internal.InternalProto.Packet> bidiStreaming(
        io.grpc.stub.StreamObserver<org.jackstaff.grpc.internal.InternalProto.Packet> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getBidiStreamingMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   */
  public static final class InternalBlockingStub extends io.grpc.stub.AbstractBlockingStub<InternalBlockingStub> {
    private InternalBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected InternalBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new InternalBlockingStub(channel, callOptions);
    }

    /**
     */
    public org.jackstaff.grpc.internal.InternalProto.Packet unary(org.jackstaff.grpc.internal.InternalProto.Packet request) {
      return blockingUnaryCall(
          getChannel(), getUnaryMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<org.jackstaff.grpc.internal.InternalProto.Packet> serverStreaming(
        org.jackstaff.grpc.internal.InternalProto.Packet request) {
      return blockingServerStreamingCall(
          getChannel(), getServerStreamingMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class InternalFutureStub extends io.grpc.stub.AbstractFutureStub<InternalFutureStub> {
    private InternalFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected InternalFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new InternalFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.jackstaff.grpc.internal.InternalProto.Packet> unary(
        org.jackstaff.grpc.internal.InternalProto.Packet request) {
      return futureUnaryCall(
          getChannel().newCall(getUnaryMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_UNARY = 0;
  private static final int METHODID_SERVER_STREAMING = 1;
  private static final int METHODID_CLIENT_STREAMING = 2;
  private static final int METHODID_BIDI_STREAMING = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final InternalImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(InternalImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_UNARY:
          serviceImpl.unary((org.jackstaff.grpc.internal.InternalProto.Packet) request,
              (io.grpc.stub.StreamObserver<org.jackstaff.grpc.internal.InternalProto.Packet>) responseObserver);
          break;
        case METHODID_SERVER_STREAMING:
          serviceImpl.serverStreaming((org.jackstaff.grpc.internal.InternalProto.Packet) request,
              (io.grpc.stub.StreamObserver<org.jackstaff.grpc.internal.InternalProto.Packet>) responseObserver);
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
        case METHODID_CLIENT_STREAMING:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.clientStreaming(
              (io.grpc.stub.StreamObserver<org.jackstaff.grpc.internal.InternalProto.Packet>) responseObserver);
        case METHODID_BIDI_STREAMING:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.bidiStreaming(
              (io.grpc.stub.StreamObserver<org.jackstaff.grpc.internal.InternalProto.Packet>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class InternalBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    InternalBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.jackstaff.grpc.internal.InternalProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Internal");
    }
  }

  private static final class InternalFileDescriptorSupplier
      extends InternalBaseDescriptorSupplier {
    InternalFileDescriptorSupplier() {}
  }

  private static final class InternalMethodDescriptorSupplier
      extends InternalBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    InternalMethodDescriptorSupplier(String methodName) {
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
      synchronized (InternalGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new InternalFileDescriptorSupplier())
              .addMethod(getUnaryMethod())
              .addMethod(getServerStreamingMethod())
              .addMethod(getClientStreamingMethod())
              .addMethod(getBidiStreamingMethod())
              .build();
        }
      }
    }
    return result;
  }
}
