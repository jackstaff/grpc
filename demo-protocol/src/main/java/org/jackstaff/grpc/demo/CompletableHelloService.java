package org.jackstaff.grpc.demo;

import org.jackstaff.grpc.annotation.AsynchronousUnary;

import java.util.function.Consumer;

public interface CompletableHelloService {

    @AsynchronousUnary
    void postMessage(String message); //Asynchronous Unary RPCs

    @AsynchronousUnary
    void postMessage(String message, Consumer<Boolean> result); //Asynchronous Unary RPCs

    String deny(String message);

    void lotsOfReplies(String greeting, Consumer<CompletableHelloResponse> replies);//Server streaming RPCs

    Consumer<CompletableHelloRequest> lotsOfGreetings(SocialInfo socialInfo); //Client streaming RPCs

    Consumer<CompletableHelloRequest> bidiHello(SocialInfo socialInfo, Consumer<CompletableHelloResponse> replies); //Bidirectional streaming RPCs

}
