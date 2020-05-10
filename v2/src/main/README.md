#Version 2, Compatible with gRPC

Generator Jackstaff style Protocol interface and POJO java bean when "option java_multiple_files = false" and "option java_outer_classname" end with "Proto" in .proto;
 (you can set compilerArgument with "-AJackstaffProto=${Proto}" at maven-compiler-plugin.configuration)

1. route_guide.proto, copy from [grpc-java](https://github.com/grpc/grpc-java/blob/master/examples/src/main/proto/route_guide.proto)
2. Auto Generate Protocol interface.
3. Set java_multiple_files = false in route_guide.proto, will Auto generate POJO java file
