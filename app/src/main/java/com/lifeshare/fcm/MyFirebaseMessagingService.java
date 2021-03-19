package com.lifeshare.fcm;


import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.network.response.StreamUserListResponse;
import com.lifeshare.ui.BroadcastUsingAgoraActivity;
import com.lifeshare.ui.invitation.MyInvitationListActivity;
import com.lifeshare.ui.show_broadcast.AgoraShowStreamActivity;
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
    }// {"from_id":4,"opentokDetail":"[]","to_id":5,"type":"STREAM_STARTED","message":"Keval Garala is broadcasting now"}

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
                    NotificationUtil notificationUtil = new NotificationUtil(getApplicationContext(), Const.INVITATION_ACCEPT, getString(R.string.app_name), message, intent, 0);
                    notificationUtil.show();
                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Const.INVITATION_ACCEPT_ACTION));
                    break;
                case Const.STREAM_STARTED:

                    showStreamNotification(jsonObject, message);

                    break;
                case Const.NEW_INVITATION:
                    Intent mainIntent = new Intent(this, BroadcastUsingAgoraActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addParentStack(BroadcastUsingAgoraActivity
                            .class);
                    stackBuilder.addNextIntent(mainIntent);
                    intent = new Intent(getApplicationContext(), MyInvitationListActivity.class);
                    stackBuilder.addNextIntent(intent);


                    NotificationUtil util = new NotificationUtil(getApplicationContext(), Const.NEW_INVITATION, getString(R.string.app_name), message, stackBuilder, new Random().nextInt());

                    util.show();

                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Const.NEW_INVITATION_ACTION));
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showStreamNotification(JSONObject jsonObject, String message) {
        try {
            String opentokDetail = jsonObject.getString("opentokDetail");

            StreamUserListResponse streamObject = new Gson().fromJson(opentokDetail, StreamUserListResponse.class);

            Intent mainIntent = new Intent(this, BroadcastUsingAgoraActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(BroadcastUsingAgoraActivity.class);
            stackBuilder.addNextIntent(mainIntent);
            Intent intent = new Intent(getApplicationContext(), AgoraShowStreamActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable(Const.STREAM_DATA, streamObject);
            intent.putExtras(bundle);
            stackBuilder.addNextIntent(intent);

            NotificationUtil notifyUtil;
            if (streamObject.getId() != null)
                notifyUtil = new NotificationUtil(getApplicationContext(), Const.STREAM_STARTED, getString(R.string.app_name), message, stackBuilder, Integer.parseInt(streamObject.getId()));
            else
                notifyUtil = new NotificationUtil(getApplicationContext(), Const.STREAM_STARTED, getString(R.string.app_name), message, stackBuilder, new Random().nextInt());

            notifyUtil.show();
        } catch (Exception e) {
            Log.v(TAG, "showStreamNotification: " + e.getMessage());
        }
    }


}
