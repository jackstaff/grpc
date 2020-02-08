package org.jackstaff.grpc.demo;

import java.util.function.Consumer;

public interface HelloService {

    String sayHello(String greeting); //Unary RPCs

    void lotsOfReplies(String greeting, Consumer<String> replies);//Server streaming RPCs

    Consumer<String> lotsOfGreetings(SocialInfo socialInfo); //Client streaming RPCs

    Consumer<HelloRequest> bidiHello(SocialInfo socialInfo, Consumer<HelloResponse> replies); //Bidirectional streaming RPCs

}
