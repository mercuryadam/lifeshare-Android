package com.lifeshare.network.request;

import android.os.Parcel;
import android.os.Parcelable;

public class SaveSubscriptionRequest implements Parcelable {
    public static final Creator<SaveSubscriptionRequest> CREATOR = new Creator<SaveSubscriptionRequest>() {
        @Override
        public SaveSubscriptionRequest createFromParcel(Parcel in) {
            return new SaveSubscriptionRequest(in);
        }

        @Override
        public SaveSubscriptionRequest[] newArray(int size) {
            return new SaveSubscriptionRequest[size];
        }
    };
    private String acknowledged;
    private String autoRenewing;
    private String subscriptionId;
    private String signature;
    private String purchaseToken;
    private String purchaseTime;
    private String purchaseState;
    private String orderId;
    private String developerPayload;
    private String obfuscatedAccountId;
    private String obfuscatedProfileId;
    private String originalJson;
    private String packageName;
    private String expriryTime;

    public SaveSubscriptionRequest() {
    }

    protected SaveSubscriptionRequest(Parcel in) {
        acknowledged = in.readString();
        autoRenewing = in.readString();
        subscriptionId = in.readString();
        signature = in.readString();
        purchaseToken = in.readString();
        purchaseTime = in.readString();
        purchaseState = in.readString();
        orderId = in.readString();
        developerPayload = in.readString();
        obfuscatedAccountId = in.readString();
        obfuscatedProfileId = in.readString();
        originalJson = in.readString();
        packageName = in.readString();
        expriryTime = in.readString();
    }

    public String getAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(String acknowledged) {
        this.acknowledged = acknowledged;
    }

    public String getAutoRenewing() {
        return autoRenewing;
    }

    public void setAutoRenewing(String autoRenewing) {
        this.autoRenewing = autoRenewing;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getPurchaseToken() {
        return purchaseToken;
    }

    public void setPurchaseToken(String purchaseToken) {
        this.purchaseToken = purchaseToken;
    }

    public String getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(String purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public String getPurchaseState() {
        return purchaseState;
    }

    public void setPurchaseState(String purchaseState) {
        this.purchaseState = purchaseState;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getDeveloperPayload() {
        return developerPayload;
    }

    public void setDeveloperPayload(String developerPayload) {
        this.developerPayload = developerPayload;
    }

    public String getObfuscatedAccountId() {
        return obfuscatedAccountId;
    }

    public void setObfuscatedAccountId(String obfuscatedAccountId) {
        this.obfuscatedAccountId = obfuscatedAccountId;
    }

    public String getObfuscatedProfileId() {
        return obfuscatedProfileId;
    }

    public void setObfuscatedProfileId(String obfuscatedProfileId) {
        this.obfuscatedProfileId = obfuscatedProfileId;
    }

    public String getOriginalJson() {
        return originalJson;
    }

    public void setOriginalJson(String originalJson) {
        this.originalJson = originalJson;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getExpriryTime() {
        return expriryTime;
    }

    public void setExpriryTime(String expriryTime) {
        this.expriryTime = expriryTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(acknowledged);
        parcel.writeString(autoRenewing);
        parcel.writeString(subscriptionId);
        parcel.writeString(signature);
        parcel.writeString(purchaseToken);
        parcel.writeString(purchaseTime);
        parcel.writeString(purchaseState);
        parcel.writeString(orderId);
        parcel.writeString(developerPayload);
        parcel.writeString(obfuscatedAccountId);
        parcel.writeString(obfuscatedProfileId);
        parcel.writeString(originalJson);
        parcel.writeString(packageName);
        parcel.writeString(expriryTime);
    }
}
