package org.jackstaff.grpc.demo.service;

import org.jackstaff.grpc.annotation.Server;
import org.jackstaff.grpc.demo.*;
import org.jackstaff.grpc.demo.common.interceptor.Authorization;
import org.jackstaff.grpc.demo.common.interceptor.LoggerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

@Server(service = CompletableHelloService.class, interceptor = {LoggerInfo.class, Authorization.class})
public class MyCompletableHelloService implements CompletableHelloService {

    Logger logger = LoggerFactory.getLogger(MyCompletableHelloService.class);
    ScheduledExecutorService schedule = Executors.newScheduledThreadPool(5);

    @Override
    public void lotsOfReplies(String greeting, Consumer<CompletableHelloResponse> replies) {
        logger.info("MyCompletableHelloService.lotsOfReplies receive: {}",  greeting);
        for (int i = 0; i < greeting.length(); i++) {
            int index = i;
            CompletableHelloResponse response = new CompletableHelloResponse();
            response.setCompleted(i == greeting.length()-1);
            response.setReply("lotsOfReplies(ServerStreaming)"+index+":"+greeting.charAt(index));
            schedule.schedule(()->replies.accept(response), index+1, TimeUnit.SECONDS);
        }
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
