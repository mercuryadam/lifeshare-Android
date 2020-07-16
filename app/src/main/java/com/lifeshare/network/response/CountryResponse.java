package com.lifeshare.network.response;

import android.os.Parcel;
import android.os.Parcelable;

public class CountryResponse implements Parcelable, Comparable<CountryResponse> {
    public static final Creator<CountryResponse> CREATOR = new Creator<CountryResponse>() {
        @Override
        public CountryResponse createFromParcel(Parcel in) {
            return new CountryResponse(in);
        }

        @Override
        public CountryResponse[] newArray(int size) {
            return new CountryResponse[size];
        }
    };
    String id;
    String name;
    String phonecode;

    protected CountryResponse(Parcel in) {
        id = in.readString();
        name = in.readString();
        phonecode = in.readString();
    }

    public static Creator<CountryResponse> getCREATOR() {
        return CREATOR;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(phonecode);
    }

    public String getPhonecode() {
        return phonecode;
    }

    public void setPhonecode(String phonecode) {
        this.phonecode = phonecode;
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
    public int compareTo(CountryResponse countryResponse) {
        if (countryResponse.id.equalsIgnoreCase(id)) {
            return 0;
        } else {
            return 1;
        }
    }
}
