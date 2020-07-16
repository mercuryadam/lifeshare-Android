package com.lifeshare.network.response;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class ReportDetailListResponse implements Serializable, Parcelable {
    public static final Creator<ReportDetailListResponse> CREATOR = new Creator<ReportDetailListResponse>() {
        @Override
        public ReportDetailListResponse createFromParcel(Parcel source) {
            return new ReportDetailListResponse(source);
        }

        @Override
        public ReportDetailListResponse[] newArray(int size) {
            return new ReportDetailListResponse[size];
        }
    };
    private String channelName;
    private String userId;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String description;
    private String avatar;
    private String totalAbuse;
    private String message;

    public ReportDetailListResponse() {
    }

    protected ReportDetailListResponse(Parcel in) {
        this.channelName = in.readString();
        this.userId = in.readString();
        this.email = in.readString();
        this.username = in.readString();
        this.firstName = in.readString();
        this.lastName = in.readString();
        this.description = in.readString();
        this.avatar = in.readString();
        this.totalAbuse = in.readString();
        this.message = in.readString();
    }

    public static Creator<ReportDetailListResponse> getCREATOR() {
        return CREATOR;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getTotalAbuse() {
        return totalAbuse;
    }

    public void setTotalAbuse(String totalAbuse) {
        this.totalAbuse = totalAbuse;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.channelName);
        dest.writeString(this.userId);
        dest.writeString(this.email);
        dest.writeString(this.username);
        dest.writeString(this.firstName);
        dest.writeString(this.lastName);
        dest.writeString(this.description);
        dest.writeString(this.avatar);
        dest.writeString(this.totalAbuse);
        dest.writeString(this.message);
    }
}
