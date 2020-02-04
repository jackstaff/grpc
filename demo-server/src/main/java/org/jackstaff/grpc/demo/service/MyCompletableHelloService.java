package org.jackstaff.grpc.demo.service;

import org.jackstaff.grpc.annotation.Server;
import org.jackstaff.grpc.demo.CompletableHelloRequest;
import org.jackstaff.grpc.demo.CompletableHelloResponse;
import org.jackstaff.grpc.demo.CompletableHelloService;
import org.jackstaff.grpc.demo.SocialInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

@Server(CompletableHelloService.class)
public class MyCompletableHelloService implements CompletableHelloService {

    Logger logger = LoggerFactory.getLogger(MyCompletableHelloService.class);

    @Override
    public void lotsOfReplies(String greeting, Consumer<CompletableHelloResponse> replies) {

    }

    @Override
    public Consumer<CompletableHelloRequest> lotsOfGreetings(SocialInfo socialInfo) {
        return null;
    }

    @Override
    public Consumer<CompletableHelloRequest> bidiHello(SocialInfo socialInfo, Consumer<CompletableHelloResponse> replies) {
        return null;
    }

}
