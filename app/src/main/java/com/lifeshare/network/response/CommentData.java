package com.lifeshare.network.response;

public class CommentData {
    private int id;
    private int channelId;
    private int userId;
    private String comment;
    private String commentDateTime;
    private int like;
    private int love;
    private User user;
    private int userLike;
    private int userLove;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserLike() {
        return userLike;
    }

    public void setUserLike(int userLike) {
        this.userLike = userLike;
    }

    public int getUserLove() {
        return userLove;
    }

    public void setUserLove(int userLike) {
        this.userLove = userLove;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommentDateTime() {
        return commentDateTime;
    }

    public void setCommentDateTime(String commentDateTime) {
        this.commentDateTime = commentDateTime;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }

    public int getLove() {
        return love;
    }

    public void setLove(int love) {
        this.love = love;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
