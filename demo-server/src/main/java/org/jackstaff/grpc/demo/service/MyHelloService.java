package org.jackstaff.grpc.demo.service;

import org.jackstaff.grpc.annotation.Server;
import org.jackstaff.grpc.demo.HelloRequest;
import org.jackstaff.grpc.demo.HelloResponse;
import org.jackstaff.grpc.demo.HelloService;
import org.jackstaff.grpc.demo.SocialInfo;
import org.jackstaff.grpc.demo.common.interceptor.Authorization;
import org.jackstaff.grpc.demo.common.interceptor.LoggerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Server(service = HelloService.class, interceptor = {LoggerInfo.class, Authorization.class})
public class MyHelloService implements HelloService {

    Logger logger = LoggerFactory.getLogger(MyHelloService.class);
    ScheduledExecutorService schedule =Executors.newSingleThreadScheduledExecutor();

    public MyHelloService() {
        logger.info("MyHelloService..........CREATE......."+System.identityHashCode(this));
    }

    @Override
    public String sayHello(String greeting) {
        logger.info("MyHelloService.sayHello receive: {}", greeting);
        return "ok";
    }

    @Override
    public void lotsOfReplies(String greeting, Consumer<HelloResponse> replies) {
        logger.info("MyHelloService.lotsOfReplies receive: {}",  greeting);
        for (int i = 0; i < greeting.length(); i++) {
            replies.accept(new HelloResponse(i+":"+greeting.charAt(i)));
        }
    }

    @Override
    public Consumer<HelloRequest> lotsOfGreetings(SocialInfo socialInfo) {
        logger.info("MyHelloService.lotsOfGreetings receive: {}", socialInfo);
        return helloRequest -> {
            logger.info("MyHelloService.lotsOfGreetings receive client streaming, helloRequest:{}",helloRequest);
        };
    }

    @Override
    public Consumer<HelloRequest> bidiHello(SocialInfo socialInfo, Consumer<HelloResponse> replies) {
        logger.info("MyHelloService.bidiHello receive: {}", socialInfo);
        List<String> friends = socialInfo.getFriends();
        if (friends != null){
            for (int i = 0; i < friends.size(); i++) {
                int index = i;
                schedule.schedule(()->replies.accept(new HelloResponse("hi,"+friends.get(index))), index+1, TimeUnit.SECONDS);
            }
        }
        return helloRequest -> {
            logger.info("MyHelloService.bidiHello receive bidi streaming, helloRequest:{}",helloRequest);
        };
    }


}
