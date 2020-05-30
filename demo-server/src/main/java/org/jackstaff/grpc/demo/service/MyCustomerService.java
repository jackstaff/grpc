package org.jackstaff.grpc.demo.service;

import org.jackstaff.grpc.MessageStream;
import org.jackstaff.grpc.Status;
import org.jackstaff.grpc.StatusRuntimeException;
import org.jackstaff.grpc.annotation.Server;
import org.jackstaff.grpc.demo.ErrorCode;
import org.jackstaff.grpc.demo.common.interceptor.Authorization;
import org.jackstaff.grpc.demo.common.interceptor.LoggerInfo;
import org.jackstaff.grpc.demo.protocol.*;
import org.jackstaff.grpc.demo.protocol.common.*;
import org.jackstaff.grpc.demo.protocol.customer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Server(service = CustomerService.class, interceptor = {LoggerInfo.class, Authorization.class})
public class MyCustomerService implements CustomerService {

    Logger logger = LoggerFactory.getLogger(MyCustomerService.class);
    ScheduledExecutorService schedule = Executors.newScheduledThreadPool(2);

    @Override
    public Customer findCustomer(Id id) {
        Customer customer = new Customer();
        customer.setId(id.getId());
        customer.setLevel(Level.VIP);
        customer.setName("my customer name");
        return customer;
    }

    @Override
    public void welcomeCustomers(Greeting greeting, Consumer<Customer> customerResponse) {
        if (greeting.hasMessage()){
            for (int i = 0; i < greeting.getMessage().length(); i++) {
                customerResponse.accept(new Customer(i, "name"+i, Level.NORMAL, null));
                if (i == greeting.getMessage().length()-1){
                    ((MessageStream<Customer>)customerResponse).done();
                    return;
                }
            }
        }
        throw new StatusRuntimeException(ErrorCode.CODE_BAD_GREETING,"greeting no message");
        //same as ((MessageStream<Customer>)customerResponse).error(new StatusRuntimeException(ErrorCode.CODE_BAD_GREETING,"greeting no message"));
    }

    @Override
    public Consumer<Customer> sendCustomers(Consumer<Greeting> greetingResponse) {
        return customer->{
            logger.info("receive customer:"+customer.getName());
            if (customer.getId() <=0) {
                logger.info("receive customer.id<=0, done");
                greetingResponse.accept(new Greeting("completed", new byte[]{1,2,3,4,5}, true));
            }
        };
    }

    @Override
    public Consumer<Greeting> bidiGreeting(Consumer<Greeting> greetingResponse) {
        MessageStream<Greeting> requests = new MessageStream<>(ms -> {
            logger.info("bidiGreeting receive "+ms);
            Optional.ofNullable(ms.getMessage()).filter(Greeting::getSmile).ifPresent(greeting -> {
                logger.info("receive smile, done");
                ((MessageStream<Greeting>)greetingResponse).done();
            });
        });
        MessageStream<Greeting> responses = (MessageStream<Greeting>) greetingResponse;
        for (int i = 0; i < 30; i++) {
            int delay =i+1;
            schedule.schedule(()->{
                if (responses.isClosed()) {
                    logger.info("greetings closed "+responses.getStatus());
                }else {
                    responses.accept(new Greeting("completed"+delay, new byte[]{1, 2, 3, 4, 5}, true));
                }
            }, delay, TimeUnit.SECONDS);
        }
        return requests;
    }

    @Override
    public Customer.Office getLevel(Credential credential) {
        return new Customer.Office(credential.getUser(), new Address("my country", "street 100",1));
    }

    @Override
    public DataModel getDataModel(Id id) {
        if (id.getId() < 0){
            logger.info("getDataModel id.getId() < 0  will throw exception: CODE_INVALID_ID");
            throw new StatusRuntimeException(ErrorCode.CODE_INVALID_ID,"invalid id:"+id.getId());
        }
        DataModel dm = new DataModel();

        dm.setOoEnum(Level.VIP);
        dm.setOoInt32(id.getId());

        dm.setTheBool(true);
        dm.setTheInt32(id.getId());
        dm.setTheDouble(1.23456);
        dm.setTheString("Duration.ofSeconds:"+id.getId());
        dm.setTheBytes(new byte[]{0,1,2,3,4,5,6,7,8,9});

        dm.setTheDuration(Duration.ofSeconds(id.getId()));
        dm.setTheTimestamp(new Timestamp(System.currentTimeMillis()));
        dm.setTheInt32Value(id.getId());
        dm.setTheBoolValue(Boolean.FALSE);
        dm.setTheStringValue("the string value");
        dm.setTheBytesValue(new byte[]{1,2,3,4,5,6,7,8,9});

        dm.setTheEnum(DataModel.TheEmbedEnum.SUCCESS);
        dm.setTheMessage(new DataModel.TheEmbedMessage(id.getId(), DataModel.TheEmbedEnum.SUCCESS));

        dm.setTheRepeatedString(Arrays.asList("a","b","c","d"));
        dm.setTheRepeatedBytes(Arrays.asList(new byte[]{1,2,3}, new byte[]{4,5,6}));
        dm.setTheRepeatedBytesValue(Arrays.asList(new byte[]{1,2,3,4,5}, new byte[]{4,5,6,7,8}));
        dm.setTheRepeatedDoubleValue(Arrays.asList(1.2,2.3,3.4));
        dm.setTheRepeatedEnum(Arrays.asList(Level.VIP, Level.NORMAL));
        return dm;
    }

    public static void main(String[] args) {
        DataModel dm = new MyCustomerService().getDataModel(new Id(100));

    }
}
