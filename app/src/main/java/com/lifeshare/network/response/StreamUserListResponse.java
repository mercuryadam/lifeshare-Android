package com.lifeshare.network.response;

import android.os.Parcel;
import android.os.Parcelable;

public class StreamUserListResponse implements Parcelable {
    private String id;
    private String opentokId;
    private String token;
    private String sId;
    private String userId;
    private String userName;
    private String avatar;
    private String firstName;
    private String lastName;
    private String isGlobal;

    public String getIsGlobal() {
        return isGlobal;
    }

    public void setIsGlobal(String isGlobal) {
        this.isGlobal = isGlobal;
    }

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
    private String channelName;
    private String roomName;

    protected StreamUserListResponse(Parcel in) {
        id = in.readString();
        opentokId = in.readString();
        token = in.readString();
        sId = in.readString();
        userId = in.readString();
        userName = in.readString();
        avatar = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        roomName = in.readString();
        channelName = in.readString();
        isGlobal = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(opentokId);
        dest.writeString(token);
        dest.writeString(sId);
        dest.writeString(userId);
        dest.writeString(userName);
        dest.writeString(avatar);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(roomName);
        dest.writeString(channelName);
        dest.writeString(isGlobal);
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOpentokId() {
        return opentokId;
    }

    public void setOpentokId(String opentokId) {
        this.opentokId = opentokId;
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
}

//result = {StreamUserListResponse@17274}
// avatar = "http://3.136.176.13/public/qa/public/uploads/users/4749181646acbea87962762b78f2d4da."
// channelName = "sydney_61_devd2"
// firstName = "Divyesh"
// id = null
// lastName = "D"
// roomName = null
// sId = null
// token = "2032311023"
// userId = "128"
// userName = "devd2"
// shadow$_klass_ = {Class@12204} "class com.lifeshare.network.response.StreamUserListResponse"
// shadow$_monitor_ = 0
