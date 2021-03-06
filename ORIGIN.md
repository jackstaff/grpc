
2020年这个春节过得真是...
因为武汉肺炎，天天呆在房间里刷抖音逛油管，无聊得要死，而且，居然，假期延长到2月9号...也就是说还有10天...

为了避免脑袋发霉，我决定做点事情，一直以来都用各种开源软件，但我其实从来没有为社区做过任何事情，
虽然github不会玩，连markdown都不会，不过，这些不重要...


为什么选择grpc呢？
因为我对 https://github.com/grpc/grpc-java 生成的代码，提供使用的接口不满意，不是我喜欢的样子，

so, road map：
version 1 (客户端和服务器端均为java)：
开发人员只需要使用java的interface来定义客户端和服务器端接口，使用springboot来自动注入客户端接口代理以及服务器端实现bean，
完全不需要使用protobuff定义文件(当然也就完全不需要使用官方grpc-java来生成代码).

底层利用java特性，反射代理动态对象序列化+Consumer，来对接客户端和服务器端的rpc调用及grpc中的streaming实现.
希望在这10天把这个版本实现掉.

这个版本完成后，开发人员使用@Server和@Client即可，就像spring的@Service和@Autowired一样方便，大致可以如下使用：

import java.util.function.Consumer;

public class MyPojo {
  int id;
  String message;
  List<Date> times;
}

public interface HelloService {

  String sayHello(String greeting); //Unary RPCs 

  void lotsOfReplies(String greeting, Consumer<String> replies);//Server streaming RPCs 
  
  Consumer<String> lotsOfGreetings(MyPojo pojo); //Client streaming RPCs
  
  Consumer<String> bidiHello(String greeting, Consumer<MyPojo> pojos); //Bidirectional streaming RPCs
  
}

@Server
public MyHelloService implements HelloService {

   @Override
   public String sayHello(String greeting, MyPojo pojo){
    return "hello "+ greeting;
   }
    ...
}

public MyHelloClientService {

  @Client
  private HelloService helloService;
    ...
}



version 2 (不限客户端和服务器端语言，只要支持grpc即可)：
使用protobuff定义文件，在现有官方grpc-java生成的代码的基础上，再次生成新的接口/POJO文件，使其和version1风格接口一致.

这个版本完成后，可以和非java语言开发service通过grpc交互，大致开发流程如下：

1. 定义protobuff接口文件 （ref https://grpc.io/docs/quickstart/）
syntax = "proto3";
package helloworld;
service Hello {
  rpc sayHello (HelloRequest) returns (HelloReply) {}
}
message HelloRequest {
  string name =1;
}
message HelloReply {
  int32 code =1;
  string message = 2;
}

2. 生成proto/stub/grpc基础类 （ref https://grpc.io/docs/quickstart/）

3. 使用插件或工具，利用2中生成的基础类，生成如下代码

public class HelloReply {
  int code;
  String message;
}

public interface HelloService {

  HelloReply sayHello(String name); 
  
}
以及其他的和2中基础类的桥接代码

4. 至此，使用方式及其他，和version 1种就一样的了.


嗯，就这样,

google翻译 :D
--------------------google translate------------------------------------------------------------------------------------------
This Spring Festival in 2020 is really ...
Because of pneumonia in Wuhan, I stayed in the room every day and went to the tubing with vibrato, and I was bored to death, and, actually, the holiday was extended to February 9 ... that is, there are 10 days ...

In order to avoid moldy heads, I decided to do something. I have always used various open source software, but I have never done anything for the community.
Although github will not play, not even markdown, but these are not important ...


Why choose grpc?
Because I'm not satisfied with the code generated by https://github.com/grpc/grpc-java, the interface used is not the way I like,

so, road map:
version 1 (both client and server are java):
Developers only need to use the java interface to define the client and server interfaces, and use springboot to automatically inject the client interface proxy and server-side implementation beans.
There is no need to use protobuff definition files at all (of course, there is no need to use official grpc-java to generate code at all).

The bottom layer uses java features, reflection proxy dynamic object serialization + Consumer, to connect client and server rpc calls and streaming implementation in grpc.
Hope to implement this version in these 10 days.

After this version is completed, developers can use @Server and @Client, just as convenient as spring's @Service and @Autowired.

import java.util.function.Consumer;

public class MyPojo {
  int id;
  String message;
  List <Date> times;
}

public interface HelloService {

  String sayHello (String greeting); // Unary RPCs

  void lotsOfReplies (String greeting, Consumer <String> replies); // Server streaming RPCs
  
  Consumer <String> lotsOfGreetings (MyPojo pojo); // Client streaming RPCs
  
  Consumer <String> bidiHello (String greeting, Consumer <MyPojo> pojos); // Bidirectional streaming RPCs
  
}

@Server
public MyHelloService implements HelloService {

   @Override
   public String sayHello (String greeting, MyPojo pojo) {
    return "hello" + greeting;
   }
    ...
}

public MyHelloClientService {

  @Client
  private HelloService helloService;
    ...
}



version 2 (unlimited client and server language, as long as it supports grpc):
Using the protobuff definition file, based on the code generated by the existing official grpc-java, generate a new interface / POJO file again to make it consistent with the version1 style interface.

After this version is completed, you can interact with non-Java language development services through grpc. The general development process is as follows:

1. Define the protobuff interface file (ref https://grpc.io/docs/quickstart/)
syntax = "proto3";
package helloworld;
service Hello {
  rpc sayHello (HelloRequest) returns (HelloReply) {}
}
message HelloRequest {
  string name = 1;
}
message HelloReply {
  int32 code = 1;
  string message = 2;
}

2. Generate proto / stub / grpc base class (ref https://grpc.io/docs/quickstart/)

3. Use the plugin or tool to generate the following code by using the base class generated in 2.

public class HelloReply {
  int code;
  String message;
}

public interface HelloService {

  HelloReply sayHello (String name);
  
}
And other bridging code with the 2 base classes

4. So far, the usage and other methods are the same as version 1.


that is it,
