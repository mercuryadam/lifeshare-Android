package com.lifeshare.network.response;

import android.os.Parcel;
import android.os.Parcelable;

public class StreamUserListResponse implements Parcelable {
    public static final Creator<StreamUserListResponse> CREATOR = new Creator<StreamUserListResponse>() {
        @Override
        public StreamUserListResponse createFromParcel(Parcel in) {
            return new StreamUserListResponse(in);
        }

        @Override
        public StreamUserListResponse[] newArray(int size) {
            return new StreamUserListResponse[size];
        }
    };
    private String id;
    private String token;
    private String sId;
    private String userId;
    private String userName;
    private String avatar;
    private String firstName;
    private String lastName;
    private String channelName;

    protected StreamUserListResponse(Parcel in) {
        id = in.readString();
        token = in.readString();
        sId = in.readString();
        userId = in.readString();
        userName = in.readString();
        avatar = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        channelName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(token);
        dest.writeString(sId);
        dest.writeString(userId);
        dest.writeString(userName);
        dest.writeString(avatar);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(channelName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getsId() {
        return sId;
    }

    public void setsId(String sId) {
        this.sId = sId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }
}
