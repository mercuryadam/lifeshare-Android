package com.lifeshare.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.android.billingclient.api.Purchase;
import com.google.gson.Gson;
import com.lifeshare.LifeShare;
import com.lifeshare.network.response.CreateRoomResponse;
import com.lifeshare.network.response.LoginResponse;

/**
 * Created by chirag.patel on 27/11/18.
 */

public class PreferenceHelper {

    public static PreferenceHelper INSTANCE;
    private final String PREFERENCE_FILE = "LifeShare_pref";
    private final String IS_FIRST_TIME = "IS_FIRST_TIME";
    private final String IS_ACCEPT_TERM_OF_SERVICE = "IS_ACCEPT_TERM_OF_SERVICE";
    private final String USER_DATA = "USER_DATA";
    private final String SESSION_DATA = "SESSION_DATA";
    private final String PURCHASE_DATA = "PURCHASE_DATA";
    private final String IS_FCM_TOKEN_UPDATED = "IS_FCM_TOKEN_UPDATED";
    private final String KEY_FCM_TOKEN = "KEY_FCM_TOKEN";
    private final String KEY_COUNT_VIEWER = "KEY_COUNT_VIEWER";
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEdit;

    private PreferenceHelper() {
        Application application = LifeShare.getInstance();
        mPrefs = application.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
        mEdit = mPrefs.edit();
    }

    public static PreferenceHelper getInstance() {
        if (INSTANCE == null)
            INSTANCE = new PreferenceHelper();
        return INSTANCE;
    }

    private void save() {
        mEdit.apply();
    }

    public boolean getIsFirstTime() {
        return mPrefs.getBoolean(IS_FIRST_TIME, true);
    }

    public void setFirstTime(boolean b) {
        mEdit.putBoolean(IS_FIRST_TIME, b);
        save();
    }

    public boolean getIsAcceptTermOfService() {
        return mPrefs.getBoolean(IS_ACCEPT_TERM_OF_SERVICE, false);
    }

    public void setTermOfServices(boolean b) {
        mEdit.putBoolean(IS_ACCEPT_TERM_OF_SERVICE, b);
        save();
    }

    public LoginResponse getUser() {
        String userData = mPrefs.getString(USER_DATA, "");
        if (!TextUtils.isEmpty(userData)) {
            LoginResponse user = new Gson().fromJson(userData, LoginResponse.class);
            return user;
        }
        return null;
    }

    public void setUser(LoginResponse user) {
        if (user == null) {
            mEdit.putString(USER_DATA, "");
            save();
            return;
        }
        String userData = new Gson().toJson(user);
        mEdit.putString(USER_DATA, userData);
        save();
    }/*
    public CreateSessionResponse getSessionData() {
        String sessionData = mPrefs.getString(SESSION_DATA, null);
        if (!TextUtils.isEmpty(sessionData)) {
            CreateSessionResponse sessionResponse = new Gson().fromJson(sessionData, CreateSessionResponse.class);
            return sessionResponse;
        }
        return null;
    }

    public void setSessionData(CreateSessionResponse sessionData) {
        if (sessionData == null) {
            mEdit.putString(SESSION_DATA, null);
            save();
            return;
        }
        String data = new Gson().toJson(sessionData);
        mEdit.putString(SESSION_DATA, data);
        save();
    }
*/

    public CreateRoomResponse getRoomData() {
        String sessionData = mPrefs.getString(SESSION_DATA, null);
        if (!TextUtils.isEmpty(sessionData)) {
            CreateRoomResponse sessionResponse = new Gson().fromJson(sessionData, CreateRoomResponse.class);
            return sessionResponse;
        }
        return null;
    }

    public void setRoomData(CreateRoomResponse sessionData) {
        if (sessionData == null) {
            mEdit.putString(SESSION_DATA, null);
            save();
            return;
        }
        String data = new Gson().toJson(sessionData);
        mEdit.putString(SESSION_DATA, data);
        save();
    }

    public Purchase getPurchaseData() {
        String sessionData = mPrefs.getString(PURCHASE_DATA, null);
        if (!TextUtils.isEmpty(sessionData)) {
            Purchase sessionResponse = new Gson().fromJson(sessionData, Purchase.class);
            return sessionResponse;
        }
        return null;
    }

    public void setPurchaseData(Purchase sessionData) {
        if (sessionData == null) {
            mEdit.putString(PURCHASE_DATA, null);
            save();
            return;
        }
        String data = new Gson().toJson(sessionData);
        mEdit.putString(PURCHASE_DATA, data);
        save();
    }

    public boolean getFcmTokenUpdated() {
        return mPrefs.getBoolean(IS_FCM_TOKEN_UPDATED, false);
    }

    public void setFcmTokenUpdated(boolean isUpdated) {
        mEdit.putBoolean(IS_FCM_TOKEN_UPDATED, isUpdated);
        save();
    }

    public String getFcmToken() {
        return mPrefs.getString(KEY_FCM_TOKEN, "");
    }

    public void setFcmToken(String token) {
        mEdit.putString(KEY_FCM_TOKEN, token);
        save();
    }

    public Integer getCountOfViewer() {
        return mPrefs.getInt(KEY_COUNT_VIEWER, 0);
    }

    public void setCountOfViewer(Integer token) {
        mEdit.putInt(KEY_COUNT_VIEWER, token);
        save();
    }


}
