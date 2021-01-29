package com.lifeshare.network.response;

public class CheckVersionResponse {

    //1 = up-to-date
    //2 = update available but user can skip
    //3 = force update
    private String status;
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
