package org.jackstaff.grpc.demo;

import org.jackstaff.grpc.annotation.AsynchronousUnary;
import org.jackstaff.grpc.annotation.UnaryServerStreaming;

import java.util.List;
import java.util.function.Consumer;

public interface AdvancedHelloService {

    int postMessage(String message); //Asynchronous Unary RPCs

    @AsynchronousUnary
    default void postMessage(String message, Consumer<Integer> result){ //Asynchronous Unary RPCs
        result.accept(postMessage(message));
    }

    String deny(String message);

    String sayHello(String greeting); //Unary RPCs

    void lotsOfReplies(String greeting, Consumer<HelloResponse> replies);//Server streaming RPCs

    @UnaryServerStreaming
    default List<HelloResponse> lotsOfReplies(String greeting){
        System.out.println("NEVER............");
        return null;
    }

    Consumer<HelloRequest> lotsOfGreetings(SocialInfo socialInfo); //Client streaming RPCs

    Consumer<HelloRequest> bidiHello(SocialInfo socialInfo, Consumer<HelloResponse> replies); //Bidirectional streaming RPCs

}
