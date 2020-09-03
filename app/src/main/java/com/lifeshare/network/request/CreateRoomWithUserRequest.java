package com.lifeshare.network.request;

public class CreateRoomWithUserRequest {

    private String users;
    private Boolean saveBroadcast;

    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }

    public Boolean getSaveBroadCast() {
        return saveBroadcast;
    }

    public void setSaveBroadCast(Boolean saveBroadcast) {
        this.saveBroadcast = saveBroadcast;
    }
}
