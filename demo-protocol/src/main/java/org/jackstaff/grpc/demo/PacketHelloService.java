package org.jackstaff.grpc.demo;

import org.jackstaff.grpc.annotation.Asynchronous;
import org.jackstaff.grpc.Packet;

import java.util.function.Consumer;

public interface PacketHelloService {


    @Asynchronous
    void postMessage(String message); //Asynchronous Unary RPCs

    String sayHello(String greeting); //Unary RPCs

    void lotsOfReplies(String greeting, Consumer<Packet<HelloResponse>> replies);//Server streaming RPCs

    Consumer<Packet<HelloRequest>> lotsOfGreetings(SocialInfo socialInfo); //Client streaming RPCs

    Consumer<Packet<HelloRequest>> bidiHello(SocialInfo socialInfo, Consumer<Packet<HelloResponse>> replies); //Bidirectional streaming RPCs

}
