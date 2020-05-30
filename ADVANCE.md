Jackstaff gRPC framework
====

[Quick Starts](https://github.com/jackstaff/grpc/blob/master/START.md) / [Advance Usage](https://github.com/jackstaff/grpc/blob/master/ADVANCE.md) / [Smooth and friendly use of gRPC](https://github.com/jackstaff/grpc/blob/master/V2.md) / [Origin](https://github.com/jackstaff/grpc/blob/master/ORIGIN.md)

Advance Usage: 
```java

```

1. Monitor status in ClientStreaming/ServerStreaming/BidirectionalStreaming:
```java
//MessageStream&MessageStatus, It's another style of io.grpc.stub.StreamObserver
//MessageStream implement Consumer,and MessageStatus adapter to StreamObserver
//client side
@Service
public class MyHelloClientService {

    @Client("my-server")
    private HelloService helloService;

    public void lotsOfReplies(){
        String greeting = "hello world.";
        //it will receive timeout message if the server side not "done()" within 30 seconds
        MessageStream<String> replies = new MessageStream<>(messageStatus->{
            switch (messageStatus.getCode()) {
                case Status.Code.MESSAGE:
                    System.out.println("the client side receive: "+messageStatus.getMessage());
                    break;
                case Status.Code.OK:
                     System.out.println("completed");
                    break;
                case Status.Code.DEADLINE_EXCEEDED: 
                     System.out.println("timeout");
                    break;
                case Status.Code.CANCELLED: //...
                case MY_BUSINESS_ERROR_CODE://application customize error code
                default:
                     System.out.println("errorCode:"+messageStatus.getCode()+", error:"+messageStatus.getCause());
                    break;
                    
            }
        }, Duration.ofSeconds(30));//timeout 30 seconds
        helloService.lotsOfReplies(greeting, replies);
    }

}


//server side
@Server(HelloService.class)
public class MyHelloService implements HelloService {

    ScheduledExecutorService schedule = Executors.newScheduledThreadPool(5);

    @Override
    public void lotsOfReplies(String greeting, Consumer<String> replies) {
        MessageStream<String> stream = (MessageStream<String>) replies;
        final int count =100;
        IntStream.range(1, count+1).forEach(i->{
            schedule.schedule(()->{
                if (stream.isClosed()){ //check the stream  status
                    System.out.println("stream closed:"+stream);
                    return;
                }
                stream.accept("hi, nanoTime is: "+System.nanoTime());
                if (i == count){
                    stream.done();//the last one, complete/close the stream
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

    static final int MY_BUSINESS_ERROR_CODE = 10001;
    @Override
    public Consumer<String> bidiHello(List<String> friends, Consumer<String> replies) {
        //pass/throw exception to client side with customize error code;
        ((MessageStream<String>)replies).error(new org.jackstaff.grpc.StatusRuntimeException(MY_BUSINESS_ERROR_CODE));
        return s->{}; //todo
    }
    
}

```

2. Interceptor & Context:
```java
import org.jackstaff.grpc.Context;
import org.jackstaff.grpc.Interceptor;

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
            throw new org.jackstaff.grpc.StatusRuntimeException(ErrorCode.PERMISSION_DENIED, context.getMethod().getName()+" No Permission");
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

3. Asynchronous unary & BlockingServerStreaming call:
```java
public interface HelloService {

    String sayHello(String greeting); //Unary RPCs

    @RpcMethod(methodType = MethodType.ASYNCHRONOUS_UNARY)
    default void sayHello(String greeting, Consumer<String> reply){
        //reply.accept(sayHello(greeting));
            //it indicate the channel will closed after call Consumer.accept() only one times
       //it's alias method, default + overload unary method. NOT need implements.

    }

    void lotsOfReplies(String greeting, Consumer<HelloResponse> replies);//Server streaming RPCs

    @RpcMethod(methodType = MethodType.BLOCKING_SERVER_STREAMING)
    default List<HelloResponse> lotsOfReplies(String greeting){
    //sync call server streaming,it's alias method,  default + overload ServerStreaming method. NOT need implements.
        return null;
    }
}

```

4. Config gRPC property in application.yml:
```yml
//the time unit is second. defaultTimeout is mean method default grpc-timeout
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
        idle-timeout: 1800
        defaultTimeout: 60
```

