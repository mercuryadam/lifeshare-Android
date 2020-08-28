package com.lifeshare.network.response;

import android.os.Parcel;
import android.os.Parcelable;

public class NewTwilioTokenResponse implements Parcelable {
    public static final Creator<NewTwilioTokenResponse> CREATOR = new Creator<NewTwilioTokenResponse>() {
        @Override
        public NewTwilioTokenResponse createFromParcel(Parcel in) {
            return new NewTwilioTokenResponse(in);
        }

        @Override
        public NewTwilioTokenResponse[] newArray(int size) {
            return new NewTwilioTokenResponse[size];
        }
    };
    private String token;

    protected NewTwilioTokenResponse(Parcel in) {
        token = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(token);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
