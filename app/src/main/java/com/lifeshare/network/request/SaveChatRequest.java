package com.lifeshare.network.request;

public class SaveChatRequest {
    private String id;
    private String roomSId;
    private String message;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomSId() {
        return roomSId;
    }

    public void setRoomSId(String roomSId) {
        this.roomSId = roomSId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
