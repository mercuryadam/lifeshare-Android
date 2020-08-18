package com.lifeshare.network.response;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by chirag.patel on 17/12/18.
 */

public class LoginResponse extends CommonResponse implements Parcelable {
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String token;
    private String username;
    private String description;
    private String avatar;
    private String mobile;
    public static final Parcelable.Creator<LoginResponse> CREATOR = new Parcelable.Creator<LoginResponse>() {
        @Override
        public LoginResponse createFromParcel(Parcel source) {
            return new LoginResponse(source);
        }

        @Override
        public LoginResponse[] newArray(int size) {
            return new LoginResponse[size];
        }
    };
    private String channelName;
    private String userType;//1 - Admin , 2 - User
    private CountryResponse country;
    private StateResponse state;
    private CityResponse city;
    private String viewerCount;

    public LoginResponse() {
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public CountryResponse getCountry() {
        return country;
    }

    public void setCountry(CountryResponse country) {
        this.country = country;
    }

    public StateResponse getState() {
        return state;
    }

    public void setState(StateResponse state) {
        this.state = state;
    }

    public CityResponse getCity() {
        return city;
    }

    public void setCity(CityResponse city) {
        this.city = city;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    protected LoginResponse(Parcel in) {
        this.userId = in.readString();
        this.email = in.readString();
        this.firstName = in.readString();
        this.lastName = in.readString();
        this.token = in.readString();
        this.username = in.readString();
        this.description = in.readString();
        this.avatar = in.readString();
        this.mobile = in.readString();
        this.viewerCount = in.readString();
        this.channelName = in.readString();
        this.userType = in.readString();
        this.country = in.readParcelable(CountryResponse.class.getClassLoader());
        this.state = in.readParcelable(StateResponse.class.getClassLoader());
        this.city = in.readParcelable(CityResponse.class.getClassLoader());
    }

    public String getViewerCount() {
        return viewerCount;
    }

    public void setViewerCount(String viewerCount) {
        this.viewerCount = viewerCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userId);
        dest.writeString(this.email);
        dest.writeString(this.firstName);
        dest.writeString(this.lastName);
        dest.writeString(this.token);
        dest.writeString(this.username);
        dest.writeString(this.description);
        dest.writeString(this.avatar);
        dest.writeString(this.mobile);
        dest.writeString(this.viewerCount);
        dest.writeString(this.channelName);
        dest.writeString(this.userType);
        dest.writeParcelable(this.country, flags);
        dest.writeParcelable(this.state, flags);
        dest.writeParcelable(this.city, flags);
    }
}
