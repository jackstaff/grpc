Jackstaff gRPC framework
====

[Quick Starts](https://github.com/jackstaff/grpc/blob/master/START.md) / [Advance Usage](https://github.com/jackstaff/grpc/blob/master/ADVANCE.md) / [Road Map](https://github.com/jackstaff/grpc/blob/master/V2.md) / [Origin](https://github.com/jackstaff/grpc/blob/master/ORIGIN.md)

Advance Usage: 
1. Monitor status in ClientStreaming/ServerStreaming/BidirectionalStreaming:
```java
//client side
@Service
public class MyHelloClientService {

    @Client("my-server")
    private HelloService helloService;

    public void lotsOfReplies(){
        String greeting = "hello world.";
        //it will receive timeout message if the server side not "done()" within 30 seconds
        int timeoutSeconds = 30;
        String timeoutMessage = new String("TIMEOUT_MESSAGE");
        //it will receive error message if the network error/unreachable...
        String errorMessage = new String("ERROR/EXCEPTION_MESSAGE");
        Consumer<String> replies = message->{
            if (message == errorMessage){ // or use message.equals(errorMessage)
                System.out.println("there are error happen");
                return;
            }
            if (message == timeoutMessage){ // or use message.equals(timeoutMessage)
                System.out.println("time out");
                return;
            }
            // process message
            System.out.println("the client side receive: "+message);
        };
        MessageChannel<String> channel = new MessageChannel<>(replies,
                errorMessage, Duration.ofSeconds(timeoutSeconds), timeoutMessage);
        helloService.lotsOfReplies(greeting, channel);
    }

}


//server side
@Server(HelloService.class)
public class MyHelloService implements HelloService {

    ScheduledExecutorService schedule = Executors.newScheduledThreadPool(5);

    @Override
    public void lotsOfReplies(String greeting, Consumer<String> replies) {
        MessageChannel<String> channel = (MessageChannel<String>) replies;
        final int count =100;
        IntStream.range(1, count+1).forEach(i->{
            schedule.schedule(()->{
                if (channel.isClosed()){ //check the channel closed status
                    System.out.println("channel closed:"+channel);
                    return;
                }
                channel.accept("hi, nanoTime is: "+System.nanoTime());
                if (i == count){
                    channel.done();//the last one, complete/close the channel
                }
            }, i, TimeUnit.SECONDS);
        });
    }

    @Override
    public String sayHello(String greeting) {
        return "echo "+greeting;//todo
    }

    @Override
    public Consumer<String> lotsOfGreetings(List<String> friends) {
        return s->{}; //todo
    }

    @Override
    public Consumer<String> bidiHello(List<String> friends, Consumer<String> replies) {
        return s->{}; //todo
    }
    
}

```

2. Interceptor:
```java
public class Credential implements Interceptor {

    @Override
    public void before(Context context) throws Exception {
        context.setMetadata("Authorization", getCredential(context));
    }

    private String getCredential(Context context){
        //@TODO generate credential/token, JWT? UserPwd?
        return "theToken"+context.getMethod().getName();
    }

}

public class Authorization implements Interceptor {

    @Override
    public void before(Context context) throws Exception {
        if (!validate(context, context.getMetadata("Authorization"))){
            throw new SecurityException("NO Permission "+context.getType().getName()+":"+context.getMethod().getName());
        }
    }

    private boolean validate(Context context, String token){
        //@TODO validate the credential/token
        return ("theToken"+context.getMethod().getName()).equals(token);
    }

}


public class LoggerInfo implements Interceptor {

    @Override
    public void before(Context context) throws Exception {
        context.setAttribute(this, System.nanoTime());
    }

    @Override
    public void returning(Context context, @Nullable Object result) {
        Optional.ofNullable(LoggerFactory.getLogger(context.getType())).filter(Logger::isInfoEnabled).ifPresent(logger -> {
            logger.info("{}.{}, useTime:{}ns, result: {}",
                    context.getType().getName(), context.getMethod().getName(),
                    System.nanoTime()-(Long) context.getAttribute(this), result);
        });
    }

    @Override
    public void recall(Context context, @Nonnull Exception ex) {
        Optional.ofNullable(LoggerFactory.getLogger(context.getType())).filter(Logger::isInfoEnabled).ifPresent(logger -> {
            logger.info("{}.{}, recalled! exception: {}", context.getType().getName(), context.getMethod().getName(), ex);
        });
    }

    @Override
    public void throwing(Context context, @Nonnull Exception ex) {
        Optional.ofNullable(LoggerFactory.getLogger(context.getType())).filter(Logger::isInfoEnabled).ifPresent(logger -> {
            logger.info("{}.{}, useTime:{}ns, throw exception message: {}",
                    context.getType().getName(), context.getMethod().getName(),
                    System.nanoTime()-(Long) context.getAttribute(this), ex.getMessage());
        });
    }

}


//client side
@Service
public class MyHelloClientService {

    @Client(authority = "my-server", interceptor = {LoggerInfo.class, Credential.class})
    private HelloService helloService;
    
}

//server side
@Server(service=HelloService.class, interceptor = {LoggerInfo.class, Credential.class})
public class MyHelloService implements HelloService {

}

```

3. Asynchronous unary call:
```java
public interface HelloService {

    //it's a flag annotation, indicate the unary method will call as asynchronous.
    @AsynchronousUnary
    void postMessage(String message);
    
    //for server streaming, it indicate the Consumer will closed after call Consumer.accept() only one times
    @AsynchronousUnary
    void onlyOneReply(String greeting, Consumer<String> reply);
    
}
```

4. Config gRPC property in application.yml:
```yml
//the time unit is second.
spring:
  grpc:
    server:
      port: 9000
      keep-alive-time: 20
      keep-alive-timeout: 10
      permit-keep-alive-time: 300
      permit-keep-alive-without-calls: true
      max-inbound-message-size: 4194304
      max-inbound-metadata-size: 20480
      max-connection-idle: 0
      max-connection-age: 0
      max-connection-age-grace: 0
    client:
      my-server:
        host: localhost
        port: 9000
        keep-alive-time: 20
        keep-alive-timeout: 10
        keep-alive-without-calls: true
        max-inbound-message-size: 4194304
        max-retry-attempts: 0
        idleTimeout: 1800
```

