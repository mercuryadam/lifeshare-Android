package com.lifeshare.network.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.lifeshare.network.request.OpenTokDetail;

public class CreateSessionResponse implements Parcelable {
    public static final Creator<CreateSessionResponse> CREATOR = new Creator<CreateSessionResponse>() {
        @Override
        public CreateSessionResponse createFromParcel(Parcel in) {
            return new CreateSessionResponse(in);
        }

        @Override
        public CreateSessionResponse[] newArray(int size) {
            return new CreateSessionResponse[size];
        }
    };
    private String sessionId;
    private String token;
    private String opentokId;
    private OpenTokDetail opentokApiKeyDetail;

    protected CreateSessionResponse(Parcel in) {
        sessionId = in.readString();
        token = in.readString();
        opentokId = in.readString();
        opentokApiKeyDetail = in.readParcelable(OpenTokDetail.class.getClassLoader());
    }

    public OpenTokDetail getOpentokApiKeyDetail() {
        return opentokApiKeyDetail;
    }

    public void setOpentokApiKeyDetail(OpenTokDetail opentokApiKeyDetail) {
        this.opentokApiKeyDetail = opentokApiKeyDetail;
    }

    public String getOpentokId() {
        return opentokId;
    }

    public void setOpentokId(String opentokId) {
        this.opentokId = opentokId;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(sessionId);
        parcel.writeString(token);
        parcel.writeString(opentokId);
        parcel.writeParcelable(opentokApiKeyDetail, i);
    }
}
