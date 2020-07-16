package com.lifeshare.network.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.lifeshare.network.request.OpenTokDetail;

public class StreamUserResponse implements Parcelable {
    public static final Parcelable.Creator<StreamUserResponse> CREATOR = new Parcelable.Creator<StreamUserResponse>() {
        @Override
        public StreamUserResponse createFromParcel(Parcel source) {
            return new StreamUserResponse(source);
        }

        @Override
        public StreamUserResponse[] newArray(int size) {
            return new StreamUserResponse[size];
        }
    };
    private String opentokId;
    private String userName;
    private String sessionId;
    private String token;
    private String userId;
    private String firstName;
    private String lastName;
    private String dateTime;
    private String avatar;
    private boolean isSelected;
    private String channelName;
    private OpenTokDetail opentokApiKeyDetail;

    public StreamUserResponse() {
    }

    protected StreamUserResponse(Parcel in) {
        this.opentokId = in.readString();
        this.userName = in.readString();
        this.sessionId = in.readString();
        this.token = in.readString();
        this.userId = in.readString();
        this.firstName = in.readString();
        this.lastName = in.readString();
        this.dateTime = in.readString();
        this.avatar = in.readString();
        this.opentokApiKeyDetail = in.readParcelable(OpenTokDetail.class.getClassLoader());
        this.isSelected = in.readByte() != 0;
        this.channelName = in.readString();
    }

    public static Creator<StreamUserResponse> getCREATOR() {
        return CREATOR;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getOpentokId() {
        return opentokId;
    }

    public void setOpentokId(String opentokId) {
        this.opentokId = opentokId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public OpenTokDetail getOpentokApiKeyDetail() {
        return opentokApiKeyDetail;
    }

    public void setOpentokApiKeyDetail(OpenTokDetail opentokApiKeyDetail) {
        this.opentokApiKeyDetail = opentokApiKeyDetail;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.opentokId);
        dest.writeString(this.userName);
        dest.writeString(this.sessionId);
        dest.writeString(this.token);
        dest.writeString(this.userId);
        dest.writeString(this.firstName);
        dest.writeString(this.lastName);
        dest.writeString(this.dateTime);
        dest.writeString(this.avatar);
        dest.writeParcelable(this.opentokApiKeyDetail, flags);
        dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
        dest.writeString(this.channelName);
    }
}
