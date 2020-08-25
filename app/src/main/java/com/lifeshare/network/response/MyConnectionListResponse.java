package com.lifeshare.network.response;

import android.os.Parcel;
import android.os.Parcelable;


public class MyConnectionListResponse implements Parcelable {

    private String userId;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String description;
    private String avatar;
    public static final Creator<MyConnectionListResponse> CREATOR = new Creator<MyConnectionListResponse>() {
        @Override
        public MyConnectionListResponse createFromParcel(Parcel source) {
            return new MyConnectionListResponse(source);
        }

        @Override
        public MyConnectionListResponse[] newArray(int size) {
            return new MyConnectionListResponse[size];
        }
    };
    private boolean isSelected = false;

    public MyConnectionListResponse(String userId, String email, String username, String firstName, String lastName, String avatar) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatar = avatar;
    }

    protected MyConnectionListResponse(Parcel in) {
        this.userId = in.readString();
        this.email = in.readString();
        this.username = in.readString();
        this.firstName = in.readString();
        this.lastName = in.readString();
        this.description = in.readString();
        this.avatar = in.readString();
        this.isSelected = in.readByte() != 0;
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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userId);
        dest.writeString(this.email);
        dest.writeString(this.username);
        dest.writeString(this.firstName);
        dest.writeString(this.lastName);
        dest.writeString(this.description);
        dest.writeString(this.avatar);
        dest.writeByte(this.isSelected ? (byte) 1 : (byte) 0);
    }
}
