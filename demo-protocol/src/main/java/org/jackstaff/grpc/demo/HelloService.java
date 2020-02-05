package org.jackstaff.grpc.demo;

import java.util.function.Consumer;

public interface HelloService {

    String sayHello(String greeting); //Unary RPCs

    void lotsOfReplies(String greeting, Consumer<HelloResponse> replies);//Server streaming RPCs

    Consumer<HelloRequest> lotsOfGreetings(SocialInfo socialInfo); //Client streaming RPCs

    Consumer<HelloRequest> bidiHello(SocialInfo socialInfo, Consumer<HelloResponse> replies); //Bidirectional streaming RPCs

}
