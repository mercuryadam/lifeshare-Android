package com.lifeshare.network.response;

import android.os.Parcel;
import android.os.Parcelable;

public class ChannelArchiveResponse implements Parcelable {

    private Integer id;
    private String title;
    private String link;
    private String image;
    private String createdAt;
    public static final Creator<ChannelArchiveResponse> CREATOR = new Creator<ChannelArchiveResponse>() {
        @Override
        public ChannelArchiveResponse createFromParcel(Parcel in) {
            return new ChannelArchiveResponse(in);
        }

        @Override
        public ChannelArchiveResponse[] newArray(int size) {
            return new ChannelArchiveResponse[size];
        }
    };
    private String type; // 1- normal channel, 2- chat or broadcast
    private String video_url;
    private String referenceId;

    public ChannelArchiveResponse(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        title = in.readString();
        link = in.readString();
        image = in.readString();
        createdAt = in.readString();
        type = in.readString();
        video_url = in.readString();
        referenceId = in.readString();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public Integer getCAId() {
        return id;
    }

    public void setCAId(Integer id) {
        this.id = id;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (id == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(id);
        }
        parcel.writeString(title);
        parcel.writeString(link);
        parcel.writeString(image);
        parcel.writeString(createdAt);
        parcel.writeString(type);
        parcel.writeString(video_url);
        parcel.writeString(referenceId);
    }
}
