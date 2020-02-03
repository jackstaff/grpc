package org.jackstaff.grpc.demo;

import org.jackstaff.grpc.Completable;

public class CompletableHelloResponse implements Completable {

    private boolean completed;
    private String reply;

    public CompletableHelloResponse() {
    }

    public CompletableHelloResponse(String reply) {
        this.reply = reply;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
