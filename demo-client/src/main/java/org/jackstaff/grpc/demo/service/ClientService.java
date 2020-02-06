package org.jackstaff.grpc.demo.service;

import org.jackstaff.grpc.MessageChannel;
import org.jackstaff.grpc.PacketClient;
import org.jackstaff.grpc.annotation.Client;
import org.jackstaff.grpc.demo.*;
import org.jackstaff.grpc.demo.common.interceptor.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

@Service
public class ClientService {

    Logger logger = LoggerFactory.getLogger(ClientService.class);
    ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();

    @Client(authority = Constant.DEMO_SERVER, interceptor = Credential.class)
    private HelloService helloService;

    @Client(Constant.DEMO_SERVER)
    private PacketHelloService packetHelloService;

    @Client(authority=Constant.DEMO_SERVER, interceptor = Credential.class)
    private CompletableHelloService completableHelloService;

    public void walkThrough() {
        walkThroughHelloService();
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));
        walkThroughPacketHelloService();
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(10));
        walkThroughCompletableHelloService();
    }

    public void walkThroughHelloService() {
        logger.info("........helloService..........");
        String reply = helloService.sayHello("hello world.");
        logger.info("sayHello..result {}", reply);

        logger.info("idle 5 second for next step..");
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
        logger.info("start lotsOfReplies (server streaming)..");
        helloService.lotsOfReplies("hello friends!", helloResponse -> {
            logger.info("lotsOfReplies..{}..response.{}", now(), helloResponse);
        });

        logger.info("idle 5 second for next step..");
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
        logger.info("start lotsOfGreetings (client streaming)..");
        SocialInfo socialInfo = new SocialInfo();
        socialInfo.setId(100);
        socialInfo.setMessage("social message");
        socialInfo.setFriends(Arrays.asList("reco", "lily", "laura"));
        logger.info("lotsOfGreetings send friend list: "+socialInfo.getFriends());
        Consumer<HelloRequest> greetings = helloService.lotsOfGreetings(socialInfo);
        for (String name: socialInfo.getFriends()) {
            greetings.accept(new HelloRequest("hi, "+name));
        }

        logger.info("idle 5 second for next step..");
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));

        logger.info("start bidiHello (bidi streaming)..");
        socialInfo.setFriends(Arrays.asList("reco", "lily", "laura", "mike", "emily"));
        logger.info("bidiHello send friend list: "+socialInfo.getFriends());
        AtomicInteger counting = new AtomicInteger(0);
        Consumer<HelloRequest> reqs = helloService.bidiHello(socialInfo, helloResponse -> {
            logger.info("bidiHello..{}..response.{}", now(), helloResponse);
            counting.incrementAndGet();
        });
        for (int i = 0; i < 10; i++) {
            schedule.schedule(()->{
                if (((MessageChannel<HelloRequest>)reqs).isClosed()){
                    logger.info("channel is closed, network error");
                    return;
                }
                try {
                    reqs.accept(new HelloRequest("ClientService received reply count:"+counting.get()));
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                }, (i+1)*1500, TimeUnit.MILLISECONDS);
        }

    }

    public void walkThroughPacketHelloService(){
        logger.info("........packetHelloService..........");

    }

    public void walkThroughCompletableHelloService(){
        logger.info("........completableHelloService..........");

        logger.info("start postMessage.."+now());
        completableHelloService.postMessage("it's post message");
        logger.info("end postMessage.."+now());


        logger.info("NOW I WILL CALL DENY MESSAGE, AFTER 5 SECONDS, IT WILL THROW SECURITY EXCEPTION");
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
        try {
            String s  = completableHelloService.deny("the message");
            logger.info("never here, since deny call will throw exception.");
        }catch (Exception ex){
            ex.printStackTrace();
        }
        logger.info("completableHelloService..end..");

    }

    String now(){
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(new Date());
    }

}
