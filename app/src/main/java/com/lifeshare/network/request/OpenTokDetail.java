package com.lifeshare.network.request;

import android.os.Parcel;
import android.os.Parcelable;

public class OpenTokDetail implements Parcelable {
    public static final Creator<OpenTokDetail> CREATOR = new Creator<OpenTokDetail>() {
        @Override
        public OpenTokDetail createFromParcel(Parcel in) {
            return new OpenTokDetail(in);
        }

        @Override
        public OpenTokDetail[] newArray(int size) {
            return new OpenTokDetail[size];
        }
    };
    private String opentokApiKey;
    private String opentokApiSecret;

    protected OpenTokDetail(Parcel in) {
        opentokApiKey = in.readString();
        opentokApiSecret = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(opentokApiKey);
        dest.writeString(opentokApiSecret);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getOpentokApiKey() {
        return opentokApiKey;
    }

    public void setOpentokApiKey(String opentokApiKey) {
        this.opentokApiKey = opentokApiKey;
    }

    public String getOpentokApiSecret() {
        return opentokApiSecret;
    }

    public void setOpentokApiSecret(String opentokApiSecret) {
        this.opentokApiSecret = opentokApiSecret;
    }

}

