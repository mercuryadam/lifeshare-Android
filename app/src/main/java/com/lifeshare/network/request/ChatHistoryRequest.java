package com.lifeshare.network.request;

public class ChatHistoryRequest {
    private String pageNo;
    private String opentokSessionsId;

    public String getPageNo() {
        return pageNo;
    }

    public void setPageNo(String pageNo) {
        this.pageNo = pageNo;
    }

    public String getOpentokSessionsId() {
        return opentokSessionsId;
    }

    public void setOpentokSessionsId(String opentokSessionsId) {
        this.opentokSessionsId = opentokSessionsId;
    }
}
