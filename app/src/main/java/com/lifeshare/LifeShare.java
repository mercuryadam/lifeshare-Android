package com.lifeshare;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.JsonElement;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.UpdateDeviceTokenRequest;
import com.lifeshare.ui.LoginActivity;
import com.lifeshare.utils.PreferenceHelper;


/**
 * Created by chirag.patel on 21/11/18.
 */

public class LifeShare extends Application {
    public static LifeShare INSTANCE;
    public static DatabaseReference databaseReference;
    static Context mContext;

    public static LifeShare getInstance() {
        if (INSTANCE == null) {
            INSTANCE = (LifeShare) mContext;

        }
        return INSTANCE;
    }

    public static DatabaseReference getFirebaseReference() {
        if (databaseReference == null) {
            databaseReference = FirebaseDatabase.getInstance().getReference();
        }
        return databaseReference;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public void updateFcmTokenToServer() {

        UpdateDeviceTokenRequest request = new UpdateDeviceTokenRequest();
        request.setDeviceToken(PreferenceHelper.getInstance().getFcmToken());
        request.setDeviceId(Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID));
        WebAPIManager.getInstance().updateDeviceToken(request, new RemoteCallback<JsonElement>() {

            @Override
            public void onSuccess(JsonElement response) {
                PreferenceHelper.getInstance().setFcmTokenUpdated(false);
            }

            @Override
            public void onUnauthorized(Throwable throwable) {

            }

            @Override
            public void onFailed(Throwable throwable) {

            }

            @Override
            public void onInternetFailed() {

            }

        });
    }

    public void logout() {
        clearAllNotification();
        PreferenceHelper.getInstance().setUser(null);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void clearAllNotification() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }
}
