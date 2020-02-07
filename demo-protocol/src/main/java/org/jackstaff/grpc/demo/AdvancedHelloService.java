package org.jackstaff.grpc.demo;

import org.jackstaff.grpc.annotation.AsynchronousUnary;

import java.util.function.Consumer;

public interface AdvancedHelloService {

    @AsynchronousUnary
    void postMessage(String message); //Asynchronous Unary RPCs

    @AsynchronousUnary
    void postMessage(String message, Consumer<String> result); //Asynchronous Unary RPCs

    String deny(String message);

    String sayHello(String greeting); //Unary RPCs

    void lotsOfReplies(String greeting, Consumer<HelloResponse> replies);//Server streaming RPCs

    Consumer<HelloRequest> lotsOfGreetings(SocialInfo socialInfo); //Client streaming RPCs

    Consumer<HelloRequest> bidiHello(SocialInfo socialInfo, Consumer<HelloResponse> replies); //Bidirectional streaming RPCs

}
