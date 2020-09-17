package com.lifeshare.network.request;

public class UpdateSaveChatFlag {
    private String id;
    private String saveChat;

    public String getSaveChat() {
        return saveChat;
    }

    public void setSaveChat(String saveChat) {
        this.saveChat = saveChat;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
