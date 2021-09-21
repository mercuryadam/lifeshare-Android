package com.lifeshare.network.request;

public class AgoraCreateRequest {
    private String channelName;
    private String roomName;
    private String sessionId;
    private String token;
    private String roomSId;
    private boolean saveBroadcast;
    private boolean saveChat;
    private String users;
    private String isGlobal;

    public String getIsGlobal() {
        return isGlobal;
    }

    public void setIsGlobal(String isGlobal) {
        this.isGlobal = isGlobal;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRoomSId() {
        return roomSId;
    }

    public void setRoomSId(String roomSId) {
        this.roomSId = roomSId;
    }


    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }


    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public boolean getSaveBroadcast() {
        return saveBroadcast;
    }

    public void setSaveBroadcast(boolean saveBroadcast) {
        this.saveBroadcast = saveBroadcast;
    }

    public boolean getSaveChat() {
        return saveChat;
    }

    public void setSaveChat(boolean saveChat) {
        this.saveChat = saveChat;
    }
}
