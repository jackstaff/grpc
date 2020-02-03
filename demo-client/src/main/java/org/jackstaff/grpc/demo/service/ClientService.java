package org.jackstaff.grpc.demo.service;

import org.jackstaff.grpc.annotation.Client;
import org.jackstaff.grpc.demo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Consumer;

@Service
public class ClientService {

    Logger logger = LoggerFactory.getLogger(ClientService.class);

    @Client(Constant.DEMO_SERVER)
    private HelloService helloService;

//    @Client(Constant.DEMO_SERVER)
//    private PacketHelloService packetHelloService;
//
//    @Client(Constant.DEMO_SERVER)
//    private CompletableHelloService completableHelloService;
//
//    @Client(Constant.DEMO_SERVER)
//    private Application application;

    public void sayHello() {
        logger.info("........helloService..........");
        String reply = helloService.sayHello("hello world.");
        logger.info("sayHello..result {}", reply);
    }

    public void walkThrough(){
        logger.info("walkThrough....start....");
        logger.info("........helloService..........");
        String reply = helloService.sayHello("hello world.");
        logger.info("sayHello..result {}", reply);

        logger.info("start postMessage.."+now());
        helloService.postMessage("it's post message");
        logger.info("end postMessage.."+now());

        logger.info("start lotsOfReplies (server streaming)..");
        helloService.lotsOfReplies("hello friends!", helloResponse -> {
            logger.info("lotsOfReplies..{}..response.{}", now(), helloResponse);
        });

        logger.info("start lotsOfGreetings (client streaming)..");
        SocialInfo socialInfo = new SocialInfo();
        socialInfo.setId(100);
        socialInfo.setMessage("social message");
        socialInfo.setFriends(Arrays.asList("reco", "lily", "laura"));
        Consumer<HelloRequest> greetings = helloService.lotsOfGreetings(socialInfo);
        for (int i = 0; i < 3; i++) {
            greetings.accept(new HelloRequest("hi number"+(i+1)));
        }

        logger.info("start bidiHello (bidi streaming)..");
        Consumer<HelloRequest> reqs = helloService.bidiHello(socialInfo, helloResponse -> {
            logger.info("bidiHello..{}..response.{}", now(), helloResponse);
        });
        for (int i = 0; i < 3; i++) {
            reqs.accept(new HelloRequest("hi bidiHello number"+(i+1)));
        }
        logger.info("walkThrough....end.");
    }

    String now(){
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(new Date());
    }

}
