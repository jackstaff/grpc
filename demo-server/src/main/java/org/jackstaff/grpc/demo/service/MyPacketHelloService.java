package org.jackstaff.grpc.demo.service;

import org.jackstaff.grpc.Packet;
import org.jackstaff.grpc.MessageChannel;
import org.jackstaff.grpc.annotation.Server;
import org.jackstaff.grpc.demo.HelloRequest;
import org.jackstaff.grpc.demo.HelloResponse;
import org.jackstaff.grpc.demo.PacketHelloService;
import org.jackstaff.grpc.demo.SocialInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Server(service = PacketHelloService.class)
public class MyPacketHelloService implements PacketHelloService {
    Logger logger = LoggerFactory.getLogger(MyPacketHelloService.class);
    ScheduledExecutorService schedule = Executors.newScheduledThreadPool(5);

    @Autowired
    private MyHelloService myHelloService;

    @Override
    public String sayHello(String greeting) {
        return myHelloService.sayHello(greeting);
    }

    @Override
    public void lotsOfReplies(String greeting, Consumer<Packet<HelloResponse>> replies) {
        logger.info("PacketHelloService.lotsOfReplies receive: {}",  greeting);
        for (int i = 0; i < greeting.length(); i++) {
            int index = i;
            replies.accept(new Packet<>(i< greeting.length()-1 ? Packet.MESSAGE : Packet.COMPLETED, new HelloResponse(i+":"+greeting.charAt(i))));
        }
    }

    @Override
    public Consumer<Packet<HelloRequest>> lotsOfGreetings(SocialInfo socialInfo) {
        logger.info("MyPacketHelloService.lotsOfGreetings receive: {}", socialInfo);
        return packet -> {
            logger.info("MyPacketHelloService.lotsOfGreetings receive client streaming, command:{} helloRequest:{}",packet.getCommand(), packet.getPayload());
        };
    }

    @Override
    public Consumer<Packet<HelloRequest>> bidiHello(SocialInfo socialInfo, Consumer<Packet<HelloResponse>> replies) {
        logger.info("MyPacketHelloService.bidiHello receive: {}", socialInfo);
        MessageChannel<Packet<HelloResponse>> messageChannel = (MessageChannel<Packet<HelloResponse>>) replies;
        List<String> friends = socialInfo.getFriends();
        if (friends != null){
            ScheduledExecutorService schedule = Executors.newSingleThreadScheduledExecutor();
            for (int i = 0; i < friends.size(); i++) {
                int index = i;
                Packet<HelloResponse> packet = new Packet<>();
                packet.setPayload(new HelloResponse("hi,"+friends.get(index)));
                packet.setCommand(i < friends.size()-1 ? Packet.MESSAGE : Packet.COMPLETED);
                schedule.schedule(()->{
                    if (messageChannel.isClosed()){
                        logger.info("bidiHello replies consumer closed "+index);
                        return;
                    }
                    messageChannel.accept(packet);
                }, index+1, TimeUnit.SECONDS);
            }
        }
        return packet -> {
            logger.info("MyPacketHelloService.bidiHello receive bidi streaming, command:{} helloRequest:{}",packet.getCommand(), packet.getPayload());
        };

    }
}
