package org.jackstaff.grpc.demo;

import org.jackstaff.grpc.demo.protocol.Id;
import org.jackstaff.grpc.demo.protocol.OrderServiceGrpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;
import java.util.concurrent.DelayQueue;
import java.util.stream.Collectors;

@ComponentScan("org.jackstaff.grpc.*")
@SpringBootApplication
public class Version2Application {

    public static void main(String[] args) throws Exception{
        ApplicationContext appContext = SpringApplication.run(Version2Application.class, args);
        appContext.getBean(OrderClient.class).go();
        new DelayQueue<>().take();
    }

}
