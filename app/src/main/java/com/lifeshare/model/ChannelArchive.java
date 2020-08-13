package com.lifeshare.model;

public class ChannelArchive {

    private String title;
    private String link;
    private String image;

    public ChannelArchive(String title, String link, String image) {
        this.title = title;
        this.link = link;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getPicture() {
        return image;
    }

    public void setPicture(String picture) {
        this.image = picture;
    }
}
