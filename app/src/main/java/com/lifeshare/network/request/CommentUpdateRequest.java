package com.lifeshare.network.request;

public class CommentUpdateRequest {
    private Integer channelCommentId;
    private String comment;

    public Integer getChannelId() {
        return channelCommentId;
    }

    public void setChannelId(Integer channelCommentId) {
        this.channelCommentId = channelCommentId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
