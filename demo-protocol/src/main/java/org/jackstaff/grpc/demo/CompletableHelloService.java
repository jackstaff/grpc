package org.jackstaff.grpc.demo;

import java.util.function.Consumer;

public interface CompletableHelloService {

    void lotsOfReplies(String greeting, Consumer<CompletableHelloResponse> replies);//Server streaming RPCs

    Consumer<CompletableHelloRequest> lotsOfGreetings(SocialInfo socialInfo); //Client streaming RPCs

    Consumer<CompletableHelloRequest> bidiHello(SocialInfo socialInfo, Consumer<CompletableHelloResponse> replies); //Bidirectional streaming RPCs

}
