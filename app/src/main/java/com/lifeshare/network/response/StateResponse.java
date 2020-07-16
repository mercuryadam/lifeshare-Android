package com.lifeshare.network.response;

import android.os.Parcel;
import android.os.Parcelable;

public class StateResponse implements Comparable<StateResponse>, Parcelable {
    public static final Creator<StateResponse> CREATOR = new Creator<StateResponse>() {
        @Override
        public StateResponse createFromParcel(Parcel in) {
            return new StateResponse(in);
        }

        @Override
        public StateResponse[] newArray(int size) {
            return new StateResponse[size];
        }
    };
    private String id;
    private String name;

    protected StateResponse(Parcel in) {
        id = in.readString();
        name = in.readString();
    }

    public static Creator<StateResponse> getCREATOR() {
        return CREATOR;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public int compareTo(StateResponse stateResponse) {
        if (stateResponse.id.equalsIgnoreCase(id)) {
            return 0;
        } else {
            return 1;
        }
    }
}
