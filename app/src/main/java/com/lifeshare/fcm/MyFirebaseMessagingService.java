package com.lifeshare.fcm;


import android.app.TaskStackBuilder;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.network.response.StreamUserListResponse;
import com.lifeshare.ui.DashboardActivity;
import com.lifeshare.ui.show_broadcast.AgoraShowStreamActivity;
import com.lifeshare.ui.ui.post.CommentActivity;
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
        if (PreferenceHelper.getInstance().getIsLogIn()) {
            LifeShare.getInstance().updateFcmTokenToServer();
        }
    }// {"from_id":4,"opentokDetail":"[]","to_id":5,"type":"STREAM_STARTED","message":"Keval Garala is broadcasting now"}

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, remoteMessage.getData().toString());

        Map<String, String> notifData = remoteMessage.getData();
        String json = notifData.get("message");

        try {
            JSONObject jsonObjectMsg = new JSONObject(json);
            if (PreferenceHelper.getInstance().getIsLogIn()) {
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
            Log.e("notificationData", String.valueOf(jsonObject));
            String type = jsonObject.getString("type");
            String message = jsonObject.getString("msg");
            JSONObject channelDataJsonObject = new JSONObject(jsonObject.getString("channel_data"));
            String channelId = channelDataJsonObject.getString("id");
            String title = channelDataJsonObject.getString("title");
            String link = channelDataJsonObject.getString("link");
            String image = channelDataJsonObject.getString("image");
            String createdAt = channelDataJsonObject.getString("createdAt");
            String channelType = channelDataJsonObject.getString("type");
            String video_url = channelDataJsonObject.getString("video_url");
//            String room_s_id = channelDataJsonObject.getString("room_s_id");
//            String file_type = channelDataJsonObject.getString("file_type");
//            String save_broadcast = channelDataJsonObject.getString("save_broadcast");
//            String is_video_download = channelDataJsonObject.getString("is_video_download");
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
                    intent = new Intent(getApplicationContext(), DashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(Const.FROM_NOTIFICATION, true);
                    NotificationUtil util = new NotificationUtil(getApplicationContext(), Const.NEW_INVITATION, getString(R.string.app_name), message, intent, new Random().nextInt());

                    util.show();

                    LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Const.NEW_INVITATION_ACTION));
                    break;
                case Const.COMMENT_ADD_LIKE_LOVE:
                    intent = new Intent(getApplicationContext(), CommentActivity.class);
                    intent.putExtra(Const.FROM_NOTIFICATION, true);
                    intent.putExtra("channelId", channelId);
                    intent.putExtra("title", title);
                    intent.putExtra("link", link);
                    intent.putExtra("image", image);
                    intent.putExtra("createdAt", createdAt);
                    intent.putExtra("video_url", video_url);
                    intent.putExtra("type", channelType);
                    NotificationUtil notification =
                            new NotificationUtil(getApplicationContext(), Const.COMMENT_ADD_LIKE_LOVE,
                                    getString(R.string.app_name), message, intent, new Random().nextInt());
                    notification.show();
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

            Intent mainIntent = new Intent(this, DashboardActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(DashboardActivity.class);
            stackBuilder.addNextIntent(mainIntent);
            Intent intent = new Intent(getApplicationContext(), AgoraShowStreamActivity.class);

            PreferenceHelper.getInstance().setNotificationIntent(streamObject);

            intent.putExtra(Const.STREAM_DATA, streamObject);
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
