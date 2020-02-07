package org.jackstaff.grpc.demo.service;

import org.jackstaff.grpc.MessageChannel;
import org.jackstaff.grpc.annotation.Server;
import org.jackstaff.grpc.demo.*;
import org.jackstaff.grpc.demo.common.interceptor.Authorization;
import org.jackstaff.grpc.demo.common.interceptor.LoggerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

@Server(service = AdvancedHelloService.class, interceptor = {LoggerInfo.class, Authorization.class})
public class MyAdvancedHelloService implements AdvancedHelloService {

    Logger logger = LoggerFactory.getLogger(MyAdvancedHelloService.class);
    ScheduledExecutorService schedule = Executors.newScheduledThreadPool(5);

    @Override
    public void postMessage(String message) {
        logger.info("MyAdvancedHelloService.postMessage receive: {}", message);
        logger.info("for test @Asynchronous, i will sleep {} seconds(=message.length()). and i'm not block client side....start...", message.length());
        LockSupport.parkNanos(TimeUnit.SECONDS.toSeconds(message.length()));
        logger.info("...end...MyAdvancedHelloService.postMessage wake up..");
    }

    @Override
    public void postMessage(String message, Consumer<String> result) {
        logger.info("MyAdvancedHelloService.postMessage receive: {}, will Consumer.accept() after {} seconds", message, message.length());
        LockSupport.parkNanos(TimeUnit.SECONDS.toSeconds(message.length()));
        result.accept("AdvancedHelloService echo:"+message);
    }

    @Override
    public String deny(String message) {
        logger.info("never here, since recalled by Authorization Interceptor");
        return "it never happen";
    }

    @Override
    public String sayHello(String greeting) {
        logger.info("MyAdvancedHelloService.sayHello receive: {}", greeting);
        return "MyAdvancedHelloService.ok";
    }

    @Override
    public void lotsOfReplies(String greeting, Consumer<HelloResponse> replies) {
        logger.info("MyAdvancedHelloService.lotsOfReplies receive: {}",  greeting);
        MessageChannel<HelloResponse> channel = (MessageChannel<HelloResponse>) replies;
        logger.info("if you shutdown this client in {}seconds, channell.isClosed()=closed", greeting.length());
        for (int i = 0; i < greeting.length(); i++) {
            int index = i;
            schedule.schedule(()->{
                HelloResponse response =new HelloResponse("lotsOfReplies(ServerStreaming)"+index+":"+greeting.charAt(index));
                if (channel.isClosed()){
                    logger.info("channel closed，{}, response NOT sent {}", channel, response);
                    return;
                }
                channel.accept(response);
            }, index+1, TimeUnit.SECONDS);
        }
    }

    @Override
    public Consumer<HelloRequest> lotsOfGreetings(SocialInfo socialInfo) {
        logger.info("MyAdvancedHelloService.lotsOfGreetings receive: {}", socialInfo);
        int timeoutSeconds = 500;
        HelloRequest timeoutMessage = new HelloRequest("the client side will receive this when timeout ");
        HelloRequest errorMessage = new HelloRequest("the client side will receive this when network error");
        Consumer<HelloRequest> consumer = request -> {
            logger.info("MyAdvancedHelloService.lotsOfGreetings receive client streaming, helloRequest:{}", request);
        };
        return new MessageChannel<>(consumer, errorMessage, Duration.ofSeconds(timeoutSeconds), timeoutMessage);
    }

    @Override
    public Consumer<HelloRequest> bidiHello(SocialInfo socialInfo, Consumer<HelloResponse> replies) {
        return null;
    }


}
