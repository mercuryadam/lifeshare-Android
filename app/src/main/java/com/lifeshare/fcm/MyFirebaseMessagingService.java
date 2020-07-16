package com.lifeshare.fcm;


import android.app.TaskStackBuilder;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.ui.BroadcastActivityNew;
import com.lifeshare.ui.invitation.MyInvitationListActivity;
import com.lifeshare.utils.Const;
import com.lifeshare.utils.PreferenceHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Random;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private String TAG = "MyFirebaseMessagingService";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.v(TAG, "onNewToken: " + s);
        PreferenceHelper.getInstance().setFcmToken(s);
        PreferenceHelper.getInstance().setFcmTokenUpdated(true);
        if (PreferenceHelper.getInstance().getUser() != null) {
            LifeShare.getInstance().updateFcmTokenToServer();
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.v(TAG, String.valueOf(remoteMessage));

        Map<String, String> notifData = remoteMessage.getData();
        String json = notifData.get("message");

        try {
            JSONObject jsonObjectMsg = new JSONObject(json);
            if (PreferenceHelper.getInstance().getUser() != null) {
                String strNotificationObject = jsonObjectMsg.getJSONObject("data").toString();
                setNotificationIntentAndView(strNotificationObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void setNotificationIntentAndView(String object) {

        try {
            JSONObject jsonObject = new JSONObject(object);
            Log.v("notificationData", String.valueOf(jsonObject));
            String type = jsonObject.getString("type");
            String message = jsonObject.getString("message");
            Random rand = new Random();
            int mNotifId = rand.nextInt(1000) + Integer.MAX_VALUE;
            Intent intent;
            switch (type) {
                case Const.INVITATION_ACCEPT:
                    intent = new Intent();
                    NotificationUtil notificationUtil = new NotificationUtil(getApplicationContext(), getString(R.string.app_name), message, intent, 0);
                    notificationUtil.show();
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Const.INVITATION_ACCEPT_ACTION));
                    break;
                case Const.NEW_INVITATION:
                    Intent mainIntent = new Intent(this, BroadcastActivityNew.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addParentStack(BroadcastActivityNew
                            .class);
                    stackBuilder.addNextIntent(mainIntent);
                    intent = new Intent(getApplicationContext(), MyInvitationListActivity.class);
                    stackBuilder.addNextIntent(intent);


                    NotificationUtil util = new NotificationUtil(getApplicationContext(), getString(R.string.app_name), message, stackBuilder, new Random().nextInt());

                    util.show();

                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Const.NEW_INVITATION_ACTION));
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
