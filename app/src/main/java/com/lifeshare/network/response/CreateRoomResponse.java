package com.lifeshare.network.response;

import android.os.Parcel;
import android.os.Parcelable;

public class CreateRoomResponse implements Parcelable {
    public static final Creator<CreateRoomResponse> CREATOR = new Creator<CreateRoomResponse>() {
        @Override
        public CreateRoomResponse createFromParcel(Parcel in) {
            return new CreateRoomResponse(in);
        }

        @Override
        public CreateRoomResponse[] newArray(int size) {
            return new CreateRoomResponse[size];
        }
    };
    private String id;
    private String sID;
    private String roomName;
    private String type;

    protected CreateRoomResponse(Parcel in) {
        id = in.readString();
        sID = in.readString();
        roomName = in.readString();
        type = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(sID);
        dest.writeString(roomName);
        dest.writeString(type);
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

    public String getsID() {
        return sID;
    }

    public void setsID(String sID) {
        this.sID = sID;
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
