package com.lifeshare.network.response;

import android.os.Parcel;
import android.os.Parcelable;

public class CityResponse implements Comparable<CityResponse>, Parcelable {

    public static final Parcelable.Creator<CityResponse> CREATOR = new Parcelable.Creator<CityResponse>() {
        @Override
        public CityResponse createFromParcel(Parcel source) {
            return new CityResponse(source);
        }

        @Override
        public CityResponse[] newArray(int size) {
            return new CityResponse[size];
        }
    };
    String id;
    String name;

    public CityResponse() {
    }

    protected CityResponse(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
    }

    public static Creator<CityResponse> getCREATOR() {
        return CREATOR;
    }

    @Override
    public int compareTo(CityResponse cityResponse) {
        if (cityResponse.id.equalsIgnoreCase(id)) {
            return 0;
        } else {
            return 1;
        }

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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
    }
}
