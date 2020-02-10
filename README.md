A java RPC framework, gRPC based and compatible with gRPC
====
Step 0: [Initial a spring project](https://start.spring.io/) and import grpc-spring-boot-starter
```xml
<!-- https://mvnrepository.com/artifact/org.jackstaff.grpc/grpc-spring-boot-starter -->
<dependency>
    <groupId>org.jackstaff.grpc</groupId>
    <artifactId>grpc-spring-boot-starter</artifactId>
    <version>1.0.1</version>
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
//todo implements
}
```

Step 3: use protocol interface(at "client side micro service" project):
```java
import org.jackstaff.grpc.annotation.Client;

@Service
public class MyClientService {

    @Client(authority = "my-server") //mapping to config file.
    private HelloService helloService;

    public String sayHello(String greeting){
        return helloService.sayHello(greeting);
    }
    
}
```

Step 4:  config "server side " application.yml:
```yml
spring:
  grpc:
    client:
      my-server:
        host: localhost
        port: 9000
```


Step 5:  config "client side " application.yml:
```yml
spring:
  grpc:
    server:
      port: 9000
```

done.


