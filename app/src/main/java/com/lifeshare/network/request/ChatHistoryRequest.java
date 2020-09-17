package com.lifeshare.network.request;

public class ChatHistoryRequest {
    private String pageNo;
    private String id;

    public String getPageNo() {
        return pageNo;
    }

    public void setPageNo(String pageNo) {
        this.pageNo = pageNo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
