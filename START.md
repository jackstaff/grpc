Jackstaff gRPC framework
====

[Quick Starts](https://github.com/jackstaff/grpc/blob/master/START.md) / [Advance Usage](https://github.com/jackstaff/grpc/blob/master/ADVANCE.md) / [Smooth and friendly use of gRPC](https://github.com/jackstaff/grpc/blob/master/V2.md) / [Origin](https://github.com/jackstaff/grpc/blob/master/ORIGIN.md)


Quick Starts: 

Step 0: Import grpc-spring-boot-starter in pom.xml
```xml
<!-- https://mvnrepository.com/artifact/org.jackstaff.grpc/grpc-spring-boot-starter -->
<dependency>
    <groupId>org.jackstaff.grpc</groupId>
    <artifactId>grpc-spring-boot-starter</artifactId>
    <version>2.0.13</version>
</dependency>

```
Step 1: Define protocol interface:
```java
public interface HelloService {

    String sayHello(String greeting); //Unary RPCs

    void lotsOfReplies(String greeting, Consumer<String> replies);//Server streaming RPCs

    Consumer<String> lotsOfGreetings(List<String> friends); //Client streaming RPCs

    Consumer<String> bidiHello(List<String> friends, Consumer<String> replies); //Bidirectional streaming RPCs

}
```

Step 2: Implements protocol interface(at "server side micro service" project):
```java
import org.jackstaff.grpc.annotation.Server;
@Server(HelloService.class)
public class MyHelloService implements HelloService {

    @Override
    public String sayHello(String greeting) {
        return "reply greeting "+greeting;
    }

    @Override
    public void lotsOfReplies(String greeting, Consumer<String> replies) {
        replies.accept("reply 1");
        replies.accept("reply 2");
    }

    @Override
    public Consumer<String> lotsOfGreetings(List<String> friends) {
        System.out.println("friends "+friends);
        return greeting -> System.out.println("receive "+greeting);
    }

    @Override
    public Consumer<String> bidiHello(List<String> friends, Consumer<String> replies) {
        System.out.println("friends "+friends);
        replies.accept("hello 1");
        replies.accept("hello 2");
        return greeting -> System.out.println("receive "+greeting);
    }

}
```

Step 3: Use protocol interface (at "client side micro service" project):
```java
import org.jackstaff.grpc.annotation.Client;

@Service
public class MyClientService {

    @Client("my-server") 
    private HelloService helloService;

    public String sayHello(String greeting){
        return helloService.sayHello(greeting);
    }
    
}
```

Step 4: Config "client side " application.yml:
```yml
spring:
  grpc:
    client:
      my-server:
        host: localhost
        port: 9000
```


Step 5: Config "server side " application.yml:
```yml
spring:
  grpc:
    server:
      port: 9000
```

done.
