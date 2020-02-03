package org.jackstaff.grpc.demo;

import org.jackstaff.grpc.annotation.Asynchronous;

import java.util.function.Consumer;

public interface HelloService {

    @Asynchronous
    void postMessage(String message); //Asynchronous Unary RPCs

    String sayHello(String greeting); //Unary RPCs

    void lotsOfReplies(String greeting, Consumer<HelloResponse> replies);//Server streaming RPCs

    Consumer<HelloRequest> lotsOfGreetings(SocialInfo socialInfo); //Client streaming RPCs

    Consumer<HelloRequest> bidiHello(SocialInfo socialInfo, Consumer<HelloResponse> replies); //Bidirectional streaming RPCs

}
