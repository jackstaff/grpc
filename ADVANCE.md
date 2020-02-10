Jackstaff gRPC framework
====

[Quick Starts](https://github.com/jackstaff/grpc/blob/master/START.md) / [Advance Usage](https://github.com/jackstaff/grpc/blob/master/ADVANCE.md) / [Road Map](https://github.com/jackstaff/grpc/blob/master/V2.md) / [Origin](https://github.com/jackstaff/grpc/blob/master/ORIGIN.md)

Advance Usage: 
1. Monitor status in ClientStreaming/ServerStreaming/BidirectionalStreaming:
```java
//isClosed() and getError(), done()

```

2. Set timeout
```java

```

3: Interceptor:
```java

```

4. Asynchronous unary call:
```java

```

5. Config gRPC property in application.yml:
```yml
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

