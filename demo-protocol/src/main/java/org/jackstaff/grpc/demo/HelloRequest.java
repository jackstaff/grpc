package org.jackstaff.grpc.demo;

public class HelloRequest {

    private String greeting;

    public HelloRequest() {
    }

    public HelloRequest(String greeting) {
        this.greeting = greeting;
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    @Override
    public String toString() {
        return "HelloRequest{" +
                "greeting='" + greeting + '\'' +
                '}';
    }
}
