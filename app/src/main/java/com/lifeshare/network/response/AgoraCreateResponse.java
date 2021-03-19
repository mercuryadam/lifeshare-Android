package com.lifeshare.network.response;

import android.os.Parcel;
import android.os.Parcelable;

public class AgoraCreateResponse implements Parcelable {
    private String id;
    private String sId;
    private String roomSId;
    private String roomName;
    private String channelName;
    private String type;
    public static final Creator<AgoraCreateResponse> CREATOR = new Creator<AgoraCreateResponse>() {
        @Override
        public AgoraCreateResponse createFromParcel(Parcel in) {
            return new AgoraCreateResponse(in);
        }

        @Override
        public AgoraCreateResponse[] newArray(int size) {
            return new AgoraCreateResponse[size];
        }
    };
    private String token;

    protected AgoraCreateResponse(Parcel in) {
        id = in.readString();
        sId = in.readString();
        roomSId = in.readString();
        roomName = in.readString();
        channelName = in.readString();
        type = in.readString();
        token = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(sId);
        dest.writeString(roomSId);
        dest.writeString(roomName);
        dest.writeString(channelName);
        dest.writeString(type);
        dest.writeString(token);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRoomSId() {
        return roomSId;
    }

    public void setRoomSId(String roomSId) {
        this.roomSId = roomSId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getsId() {
        return sId;
    }


    public void setsId(String sId) {
        this.sId = sId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
