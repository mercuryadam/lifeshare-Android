package com.lifeshare.network.response;

public class ListOfPostResponse {
    public int id;
    public String title;
    public String link;
    public String image;
    public String createdAt;
    public int referenceId;
    public String video_url;
    public int type;
    public String room_s_id;
    public String file_type;
    public int save_broadcast;
    public int is_video_download;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Integer referenceId) {
        this.referenceId = referenceId;
    }

    public String getVideoUrl() {
        return video_url;
    }

    public void setVideoUrl(String video_url) {
        this.video_url = video_url;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getRoomsId() {
        return room_s_id;
    }

    public void setRoomsId(String room_s_id) {
        this.room_s_id = room_s_id;
    }

    public String getFileType() {
        return file_type;
    }

    public void setFileType(String file_type) {
        this.file_type = file_type;
    }

    public Integer getIsVideoDownload() {
        return is_video_download;
    }

    public void setIsVideoDownload(Integer is_video_download) {
        this.is_video_download = is_video_download;
    }

}