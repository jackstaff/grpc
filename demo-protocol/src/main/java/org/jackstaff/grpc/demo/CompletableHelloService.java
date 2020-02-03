package org.jackstaff.grpc.demo;

import org.jackstaff.grpc.annotation.Asynchronous;

import java.util.function.Consumer;

public interface CompletableHelloService {

    @Asynchronous
    void postMessage(String message); //Asynchronous Unary RPCs

    String sayHello(String greeting); //Unary RPCs

    void lotsOfReplies(String greeting, Consumer<CompletableHelloResponse> replies);//Server streaming RPCs

    Consumer<CompletableHelloRequest> lotsOfGreetings(SocialInfo socialInfo); //Client streaming RPCs

    Consumer<CompletableHelloRequest> bidiHello(SocialInfo socialInfo, Consumer<CompletableHelloResponse> replies); //Bidirectional streaming RPCs

}
