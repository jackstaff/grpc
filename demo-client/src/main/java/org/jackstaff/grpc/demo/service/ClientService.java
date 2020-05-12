package org.jackstaff.grpc.demo.service;

import com.google.gson.Gson;
import io.grpc.StatusRuntimeException;
import org.jackstaff.grpc.MessageStream;
import org.jackstaff.grpc.Status;
import org.jackstaff.grpc.annotation.Client;
import org.jackstaff.grpc.demo.*;
import org.jackstaff.grpc.demo.common.interceptor.Credential;
import org.jackstaff.grpc.demo.protocol.common.Id;
import org.jackstaff.grpc.demo.protocol.customer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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

    @Client(authority = Constant.DEMO_SERVER, interceptor = Credential.class)
    private CustomerService customerService;

    public void walkThrough() throws Exception{
        walkThroughCustomerService();
        walkThroughHelloService();
        walkThroughAdvanceHelloService();

     }

    public void walkThroughCustomerService(){
        logger.info("........CustomerService..........");
        DataModel dm = customerService.getDataModel(new Id(100));
        logger.info("getDataModel result:"+new Gson().toJson(dm));
        customerService.getDataModel(new Id(Constant.INVALID_ID),
                new MessageStream<>(ms -> logger.info("getDataModel with INVALID_ID, async result: errorCode:"+
                        ms.getCode()+",description:"+ms.getDescription())));
        customerService.welcomeCustomers(new Greeting("hello", null,true), new MessageStream<>(ms->{
            logger.info(now()+" welcomeCustomers response: "+ms);
        }, Constant.DEFAULT_TIMEOUT));
        Consumer<Customer> customerConsumer = customerService.sendCustomers(greeting -> logger.info(".."+greeting.getMessage()));
        for (int i = 10; i >=0; i--) {
            customerConsumer.accept(new Customer(i, "namex"+i, Level.VIP, null));
        }
        MessageStream<Greeting> greetings= (MessageStream<Greeting>)customerService.bidiGreeting(new MessageStream<>(ms->{
            logger.info("bidiGreeting response:" +ms);
        }, Duration.ofSeconds(30)));
        for (int i = 0; i < 20; i++) {
            int delay = i+1;
            schedule.schedule(()->greetings.accept(new Greeting("hello"+delay, new byte[0], delay==20)), delay, TimeUnit.SECONDS);
        }
    }

    public void walkThroughHelloService()  throws Exception{
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
                if (((MessageStream<HelloRequest>)reqs).isClosed()){
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

    public void walkThroughAdvanceHelloService() throws Exception{
        logger.info("........advancedHelloService..........");

        logger.info("sayHello reply:"+advancedHelloService.sayHello("hello"));

        logger.info("start async sayHello.."+now());
        MessageStream<String> channel =new MessageStream<>(mt->{
            logger.info("async postMessage result:"+mt+",,");
        }, Constant.DEFAULT_TIMEOUT);
        advancedHelloService.sayHello("hello", channel);

        logger.info("start lotsOfReplies (UnaryServerStreaming)..");
        List<HelloResponse> replies =advancedHelloService.lotsOfReplies("USS");
        logger.info("sync.lotsOfReplies..{}..response.list.{},{}", now(), replies.size(), replies.toString());

        logger.info("start lotsOfReplies (server streaming)..");
        Consumer<HelloResponse> ssConsumer = response -> {

            logger.info("lotsOfReplies..{}..response.{}", now(), response);
        };
        String ssGreeting = "hello friends!";
        MessageStream<HelloResponse> ssChannel = new MessageStream<>(ms->{
            logger.info(""+ms);
        }, Constant.DEFAULT_TIMEOUT);
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
        MessageStream<HelloRequest> csChannel = (MessageStream<HelloRequest>) csGreetings;
        for (int i = 0; i < csSocialInfo.getFriends().size(); i++) {
            int index = i;
            HelloRequest request = new HelloRequest("hi, "+csSocialInfo.getFriends().get(index));
            schedule.schedule(()->{
                if (csChannel.isClosed()){
                    logger.info("client streaming channel closed "+csChannel+"("+csChannel.getStatus()+"), request NOT send "+request);
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

        MessageStream<HelloResponse> bisChannel = new MessageStream<>(ms->{
            logger.info("bidiHello..{}..response.{}", now(), ms);

        },  Constant.DEFAULT_TIMEOUT);
        Consumer<HelloRequest> biGreetings = advancedHelloService.bidiHello(biSocialInfo, bisChannel);
        MessageStream<HelloRequest> bicChannel = (MessageStream<HelloRequest>) biGreetings;
        for (int i = 0; i < biSocialInfo.getFriends().size(); i++) {
            int index = i;
            HelloRequest request = new HelloRequest("hi, "+biSocialInfo.getFriends().get(i));
            schedule.schedule(()->{
                if (bicChannel.isClosed()){
                    logger.info("bidi channel closed "+bicChannel+"("+bicChannel.getStatus()+"), request NOT send "+request);
                    return;
                }
                bicChannel.accept(request);
                if (index == biSocialInfo.getFriends().size() -1){
                    bicChannel.done();
                }
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
        }catch (StatusRuntimeException ex){
            logger.info("STATUS_EXCEPTION:"+ Status.fromThrowable(ex));
            ex.printStackTrace();
        }

    }


    String now(){
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(new Date());
    }

}
