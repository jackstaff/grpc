package org.jackstaff.grpc.demo;

public class HelloResponse {
    private String reply;

    public HelloResponse() {
    }

    public HelloResponse(String reply) {
        this.reply = reply;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    @Override
    public String toString() {
        return "HelloResponse{" +
                "reply='" + reply + '\'' +
                '}';
    }
}
