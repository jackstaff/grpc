package org.jackstaff.grpc.demo;

import org.jackstaff.grpc.MethodType;
import org.jackstaff.grpc.annotation.RpcMethod;

import java.util.List;
import java.util.function.Consumer;

public interface AdvancedHelloService {

    void procedure(String msg);

    String deny(String message);

    String sayHello(String greeting); //Unary RPCs

    @RpcMethod(methodType = MethodType.ASYNCHRONOUS_UNARY)
    default void sayHello(String greeting, Consumer<String> reply){
        //reply.accept(sayHello(greeting));
    }

    void lotsOfReplies(String greeting, Consumer<HelloResponse> replies);//Server streaming RPCs

    @RpcMethod(methodType = MethodType.BLOCKING_SERVER_STREAMING)
    default List<HelloResponse> lotsOfReplies(String greeting){
        System.out.println("NEVER............");
        return null;
    }

    Consumer<HelloRequest> lotsOfGreetings(SocialInfo socialInfo); //Client streaming RPCs

    Consumer<HelloRequest> bidiHello(SocialInfo socialInfo, Consumer<HelloResponse> replies); //Bidirectional streaming RPCs

}
