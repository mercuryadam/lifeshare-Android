package com.lifeshare.network.response;

public class CheckSubscriptionResponse {
    private String status; // 0 - DeActive , 1 - Active

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
