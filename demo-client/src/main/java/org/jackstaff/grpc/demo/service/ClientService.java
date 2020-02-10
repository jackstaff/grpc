package org.jackstaff.grpc.demo.service;

import org.jackstaff.grpc.MessageChannel;
import org.jackstaff.grpc.annotation.Client;
import org.jackstaff.grpc.demo.*;
import org.jackstaff.grpc.demo.common.interceptor.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.stream.IntStream;

@Service
public class ClientService {

    Logger logger = LoggerFactory.getLogger(ClientService.class);
    ScheduledExecutorService schedule = Executors.newScheduledThreadPool(5);

    @Client(authority = Constant.DEMO_SERVER)
    private HelloService helloService;

    @Client(authority = Constant.DEMO_SERVER, interceptor = Credential.class)
    private AdvancedHelloService advancedHelloService;

    public void walkThrough() {
        walkThroughHelloService();
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(20));
        walkThroughAdvanceHelloService();
    }

    public void walkThroughHelloService() {
        logger.info("........helloService..........");
        String reply = helloService.sayHello("hello world.");
        logger.info("sayHello..result {}", reply);

        logger.info("idle 5 second for next step..");
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
        logger.info("start lotsOfReplies (server streaming)..");
        helloService.lotsOfReplies("hello friends!", response -> {
            logger.info("lotsOfReplies..{}..response.{}", now(), response);
        });

        logger.info("idle 5 second for next step..");
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
        logger.info("start lotsOfGreetings (client streaming)..");
        SocialInfo socialInfo = new SocialInfo();
        socialInfo.setId(100);
        socialInfo.setMessage("social message");
        socialInfo.setFriends(Arrays.asList("reco", "lily", "laura"));
        logger.info("lotsOfGreetings send friend list: "+socialInfo.getFriends());
        Consumer<String> greetings = helloService.lotsOfGreetings(socialInfo);
        for (String name: socialInfo.getFriends()) {
            greetings.accept("hi, "+name);
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
            int index = i;
            schedule.schedule(()->{
                if (((MessageChannel<HelloRequest>)reqs).isClosed()){
                    logger.info("bidiHello channel is closed, "+index);
                    return;
                }
                try {
                    reqs.accept(new HelloRequest(index+"bidiHello ClientService received reply count:"+counting.get()));
                }catch (Exception ex){
                    ex.printStackTrace();
                }
                }, (i+1)*1500, TimeUnit.MILLISECONDS);
        }

    }

    public void walkThroughAdvanceHelloService(){
        logger.info("........advancedHelloService..........");

        logger.info("sayHello reply:"+advancedHelloService.sayHello("hello"));

        logger.info("start postMessage.."+now());
        advancedHelloService.postMessage("it's post message");
        logger.info("end postMessage.."+now());

        logger.info("start lotsOfReplies (server streaming)..");
        HelloResponse ssErrorMessage = new HelloResponse("I (the client side) receive this when error");
        HelloResponse ssTimeoutMessage = new HelloResponse("I (the client side) receive this when timeout");
        Consumer<HelloResponse> ssConsumer = response -> {
            if (ssErrorMessage == response){
                logger.info("lotsOfReplies..{}..receive ERROR{}", now(), response);
                return;
            }
            logger.info("lotsOfReplies..{}..response.{}", now(), response);
        };
        int ssTimeoutSeconds = 10;
//        String ssGreeting = "hello friends!, it will timeout because this string.length() > ssTimeoutSeconds";
        String ssGreeting = "hello!";
        MessageChannel<HelloResponse> ssChannel = new MessageChannel<>(ssConsumer,
                ssErrorMessage, Duration.ofSeconds(ssTimeoutSeconds), ssTimeoutMessage);
        advancedHelloService.lotsOfReplies(ssGreeting, ssChannel);
        IntStream.range(1, 60).forEach(i->schedule.schedule(()->{
            logger.info("lotsOfReplies.(server streaming) channel "+ssChannel);
        }, i, TimeUnit.SECONDS));

        logger.info("idle 5 second for next step..");
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
        logger.info("start lotsOfGreetings (client streaming)..");
        SocialInfo csSocialInfo = new SocialInfo();
        csSocialInfo.setId(100);
        csSocialInfo.setMessage("social message");
        csSocialInfo.setFriends(Arrays.asList("reco", "lily", "laura", "yellow", "red", "black", "white"));
        logger.info("lotsOfGreetings send friend list: "+csSocialInfo.getFriends());
        Consumer<HelloRequest> csGreetings = advancedHelloService.lotsOfGreetings(csSocialInfo);
        MessageChannel<HelloRequest> csChannel = (MessageChannel<HelloRequest>) csGreetings;
        for (int i = 0; i < csSocialInfo.getFriends().size(); i++) {
            int index = i;
            HelloRequest request = new HelloRequest("hi, "+csSocialInfo.getFriends().get(index));
            schedule.schedule(()->{
                if (csChannel.isClosed()){
                    logger.info("client streaming channel closed "+csChannel+"("+csChannel.getError()+"), request NOT send "+request);
                    return;
                }
                csChannel.accept(request);
                if (index == csSocialInfo.getFriends().size() -1){
                    csChannel.done();
                    logger.info("client streaming last one, done/completed");
                }
            }, index+1, TimeUnit.SECONDS);
        }

        logger.info("idle 5 second for next step..");
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
        logger.info("start bidiHello (bidi streaming)..");
        SocialInfo biSocialInfo = new SocialInfo();
        biSocialInfo.setId(100);
        biSocialInfo.setMessage("social message");
        biSocialInfo.setFriends(Arrays.asList("reco", "lily", "laura", "yellow", "red", "black", "white"));
        logger.info("bidiHello send friend list: "+biSocialInfo.getFriends());

        HelloResponse bisErrorMessage = new HelloResponse("I (the client side) receive this when error");
        HelloResponse bisTimeoutMessage = new HelloResponse("I (the client side) receive this when timeout");
        Consumer<HelloResponse> bisConsumer = response -> {
            if (bisErrorMessage == response){
                logger.info("bidiHello..{}..receive ERROR{}", now(), response);
                return;
            }
            if (bisTimeoutMessage == response){
                logger.info("bidiHello..{}..receive TIMEOUT{}", now(), response);
                return;
            }

            logger.info("bidiHello..{}..response.{}", now(), response);
        };
        int bisTimeoutSeconds = 30;
        MessageChannel<HelloResponse> bisChannel = new MessageChannel<>(bisConsumer,
                bisErrorMessage, Duration.ofSeconds(bisTimeoutSeconds), bisTimeoutMessage);
        Consumer<HelloRequest> biGreetings = advancedHelloService.bidiHello(biSocialInfo, bisChannel);
        MessageChannel<HelloRequest> bicChannel = (MessageChannel<HelloRequest>) biGreetings;
        for (int i = 0; i < biSocialInfo.getFriends().size(); i++) {
            HelloRequest request = new HelloRequest("hi, "+biSocialInfo.getFriends().get(i));
            schedule.schedule(()->{
                if (bicChannel.isClosed()){
                    logger.info("bidi channel closed "+bicChannel+"("+bicChannel.getError()+"), request NOT send "+request);
                    return;
                }
                bicChannel.accept(request);
            }, i+1, TimeUnit.SECONDS);
        }
        IntStream.range(1, 60).forEach(i->schedule.schedule(()->{
            logger.info("bidi hello bisChannel "+bisChannel);
            logger.info("bidi hello bicChannel "+bicChannel);
        }, i, TimeUnit.SECONDS));

        logger.info("NOW I WILL CALL DENY MESSAGE, AFTER 5 SECONDS, IT WILL THROW SECURITY EXCEPTION");
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
        try {
            String s  = advancedHelloService.deny("the message");
            logger.info("never here, since deny call will throw exception.");
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }


    String now(){
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(new Date());
    }

}
