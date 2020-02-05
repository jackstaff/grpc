package org.jackstaff.grpc.demo.service;

import org.jackstaff.grpc.annotation.Server;
import org.jackstaff.grpc.demo.CompletableHelloRequest;
import org.jackstaff.grpc.demo.CompletableHelloResponse;
import org.jackstaff.grpc.demo.CompletableHelloService;
import org.jackstaff.grpc.demo.SocialInfo;
import org.jackstaff.grpc.demo.common.interceptor.Authorization;
import org.jackstaff.grpc.demo.common.interceptor.LoggerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

@Server(service = CompletableHelloService.class, interceptor = {LoggerInfo.class, Authorization.class})
public class MyCompletableHelloService implements CompletableHelloService {

    Logger logger = LoggerFactory.getLogger(MyCompletableHelloService.class);

    @Override
    public void postMessage(String message) {
        logger.info("MyCompletableHelloService.postMessage receive: {}", message);
        logger.info("for test @Asynchronous, i will sleep 30 seconds.....start...");
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(30));
        logger.info("...end...MyCompletableHelloService.postMessage wake up..");
    }

    @Override
    public void postMessage(String message, Consumer<Boolean> result) {
        logger.info("MyCompletableHelloService.postMessage receive: {}", message);
        result.accept(message.length()>5);
    }

    @Override
    public String deny(String message) {
        logger.info("never here, since recalled by Authorization Interceptor");
        return "it never happen";
    }

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
