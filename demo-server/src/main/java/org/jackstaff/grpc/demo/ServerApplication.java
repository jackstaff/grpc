package org.jackstaff.grpc.demo;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.concurrent.DelayQueue;

@ComponentScan("org.jackstaff.grpc.demo.*")
@SpringBootApplication
public class ServerApplication {

    public static void main(String[] args) throws Exception{
        SpringApplication.run(ServerApplication.class, args);
        new DelayQueue<>().take();
    }

}