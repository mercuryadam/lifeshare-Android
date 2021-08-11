package com.lifeshare.network.response;

import java.util.ArrayList;
import java.util.List;

public class CommentResponse {
    private String message;
    private List<CommentData> data = new ArrayList<CommentData>();

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<CommentData> getData() {
        return data;
    }

    public void setData(List<CommentData> data) {
        this.data = data;
    }
}
