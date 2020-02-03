package org.jackstaff.grpc.demo;

import java.util.List;

public class SocialInfo {

    private int id;
    private String message;
    private List<String> friends;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    @Override
    public String toString() {
        return "SocialInfo{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", friends=" + friends +
                '}';
    }
}
