package org.jackstaff.grpc.demo;

import org.jackstaff.grpc.demo.service.ClientService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.concurrent.DelayQueue;

@ComponentScan("org.jackstaff.grpc.demo.*")
@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) throws Exception{
        ApplicationContext appContext =SpringApplication.run(ClientApplication.class, args);
        ClientService clientService = appContext.getBean(ClientService.class);
        clientService.walkThrough();

        new DelayQueue<>().take();
    }

}
