package org.jackstaff.grpc.demo.service;

import org.jackstaff.grpc.MessageStream;
import org.jackstaff.grpc.annotation.Server;
import org.jackstaff.grpc.demo.AdvancedHelloService;
import org.jackstaff.grpc.demo.HelloRequest;
import org.jackstaff.grpc.demo.HelloResponse;
import org.jackstaff.grpc.demo.SocialInfo;
import org.jackstaff.grpc.demo.common.interceptor.Authorization;
import org.jackstaff.grpc.demo.common.interceptor.LoggerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.IntStream;

@Server(service = AdvancedHelloService.class, interceptor = {LoggerInfo.class, Authorization.class})
public class MyAdvancedHelloService implements AdvancedHelloService {

    Logger logger = LoggerFactory.getLogger(MyAdvancedHelloService.class);
    ScheduledExecutorService schedule = Executors.newScheduledThreadPool(2);

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
        MessageStream<HelloResponse> channel = (MessageStream<HelloResponse>) replies;
        logger.info("if you shutdown the client in {}seconds, channell.isClosed()=true", greeting.length());
        for (int i = 0; i < greeting.length(); i++) {
            int index = i;
            schedule.schedule(()->{
                if (index >3){
                    logger.info("timmmmmmmm"+channel);
                }
                HelloResponse response =new HelloResponse("lotsOfReplies(ServerStreaming)"+index+":"+greeting.charAt(index));
                if (channel.isClosed()){
                    logger.info("server streaming channel closed，{}, response NOT sent {}", channel, response);
                    return;
                }
                channel.accept(response);
                if (index == greeting.length() -1){
                    channel.done();
                    logger.info("server streaming channel done/completed");
                }

            }, index+1, TimeUnit.SECONDS);
        }
    }

    @Override
    public Consumer<HelloRequest> lotsOfGreetings(SocialInfo socialInfo) {
        logger.info("MyAdvancedHelloService.lotsOfGreetings receive: {}", socialInfo);
        int timeoutSeconds = 60;

        return new MessageStream<HelloRequest>(ms->{
            logger.info("MyAdvancedHelloService.lotsOfGreetings receive client streaming, code:"+ms.getCode()+","+ms.getMessage());
        });
    }

    @Override
    public Consumer<HelloRequest> bidiHello(SocialInfo socialInfo, Consumer<HelloResponse> replies) {
        logger.info("MyAdvancedHelloService.bidiHello receive: {}", socialInfo);
        int timeoutSeconds = 30;
        MessageStream<HelloResponse> resp = (MessageStream<HelloResponse>) replies;
        logger.info("if you shutdown the client in {}seconds, channell.isClosed()=closed", timeoutSeconds);
        for (int i = 0; i < socialInfo.getFriends().size(); i++) {
            int index = i;
            schedule.schedule(()->{
                HelloResponse response =new HelloResponse("bidiHello(BidiStreaming), hello, "+socialInfo.getFriends().get(index));
                if (resp.isClosed()){
                    logger.info("bidi channel closed，{}, response NOT sent {}", resp, response);
                    return;
                }
                resp.accept(response);
                if (index == socialInfo.getFriends().size()-1){
                    resp.done();
                    logger.info("bidi channel done/completed");
                }
            }, index+1, TimeUnit.SECONDS);
        }
        MessageStream<HelloRequest> req= new MessageStream<>(ms->{
            logger.info("MyAdvancedHelloService.bidiHello receive client streaming, code:"+ms.getCode()+","+ms.getMessage());
        });
        IntStream.range(1, 60).forEach(i->schedule.schedule(()->{
            logger.info("bidi hello resp "+resp);
            logger.info("bidi hello req "+req);
        }, i, TimeUnit.SECONDS));
        return req;

    }


}
