package org.jackstaff.grpc.demo;

import org.jackstaff.grpc.Completable;

public class CompletableHelloRequest implements Completable {

    private boolean completed;
    private String greeting;

    public CompletableHelloRequest() {
    }


    public CompletableHelloRequest(String greeting) {
        this.greeting = greeting;
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

}
