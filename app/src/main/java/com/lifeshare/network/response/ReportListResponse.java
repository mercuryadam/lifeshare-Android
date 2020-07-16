package com.lifeshare.network.response;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class ReportListResponse implements Serializable, Parcelable {
    public static final Creator<ReportListResponse> CREATOR = new Creator<ReportListResponse>() {
        @Override
        public ReportListResponse createFromParcel(Parcel in) {
            return new ReportListResponse(in);
        }

        @Override
        public ReportListResponse[] newArray(int size) {
            return new ReportListResponse[size];
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

    protected ReportListResponse(Parcel in) {
        channelName = in.readString();
        userId = in.readString();
        email = in.readString();
        username = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        description = in.readString();
        avatar = in.readString();
        totalAbuse = in.readString();
    }

    public ReportListResponse() {
    }

    public static Creator<ReportListResponse> getCREATOR() {
        return CREATOR;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(channelName);
        dest.writeString(userId);
        dest.writeString(email);
        dest.writeString(username);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(description);
        dest.writeString(avatar);
        dest.writeString(totalAbuse);
    }

    @Override
    public int describeContents() {
        return 0;
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

}
