package com.lifeshare;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.JsonElement;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.UpdateDeviceTokenRequest;
import com.lifeshare.ui.LoginActivity;
import com.lifeshare.utils.PreferenceHelper;

//import com.google.firebase.crashlytics.FirebaseCrashlytics;

//import com.google.firebase.crashlytics.FirebaseCrashlytics;



/**
 * Created by chirag.patel on 21/11/18.
 */

public class LifeShare extends MultiDexApplication {
    public static LifeShare INSTANCE;
    public static DatabaseReference databaseReference;
    static Context mContext;

    public static LifeShare getInstance() {
        if (INSTANCE == null) {
            INSTANCE = (LifeShare) mContext;

        }
        return INSTANCE;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
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
   /*     FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG);
        if (BuildConfig.BUILD_TYPE.equalsIgnoreCase("release")) {
            FirebaseApp.initializeApp(getApplicationContext());
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
            FirebaseCrashlytics.getInstance().sendUnsentReports();
        }
   */
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

    public void clearNotificationById(int notificationId) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }
}
