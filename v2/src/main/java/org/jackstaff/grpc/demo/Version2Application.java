package org.jackstaff.grpc.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.concurrent.DelayQueue;

@ComponentScan("org.jackstaff.grpc.demo.*")
@SpringBootApplication
public class Version2Application {

    public static void main(String[] args) throws Exception{
        ApplicationContext appContext = SpringApplication.run(Version2Application.class, args);
        new DelayQueue<>().take();
    }

}
