package com.lifeshare.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;

import com.lifeshare.R;

import java.util.Random;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationUtil {

    private static final String TAG = "NotificationUtil";
    private Context mContext;
    private String mTitle, mDesc;
    private int mNotifId;
    private Intent mNotificationIntent;
    private boolean isAutoCancel = true;
    private PendingIntent pendingIntent;
    private TaskStackBuilder taskStackBuilder;

    public NotificationUtil(Context context, String title, String desc, Intent notificationIntent, int notifId) {
        this.mContext = context.getApplicationContext();
        this.mTitle = title;
        this.mDesc = desc;
        this.mNotificationIntent = notificationIntent;
        this.mNotifId = notifId;
    }

    public NotificationUtil(Context context, String title, String desc, TaskStackBuilder stackBuilder, int notifId) {
        this.mContext = context.getApplicationContext();
        this.mTitle = title;
        this.mDesc = desc;
        this.taskStackBuilder = stackBuilder;
        this.mNotifId = notifId;
    }

    public NotificationUtil(Context context, String title, String desc, Intent notificationIntent, int notifId, boolean isAutoCancel) {
        this.mContext = context.getApplicationContext();
        this.mTitle = title;
        this.mDesc = desc;
        this.mNotificationIntent = notificationIntent;
        this.mNotifId = notifId;
        this.isAutoCancel = isAutoCancel;
    }

    public void show() {
        if (TextUtils.isEmpty(mDesc)) {
            return;
        }
        if (mNotifId == 0) {
            Random rand = new Random();
            mNotifId = rand.nextInt(1000) + Integer.MAX_VALUE;
        }
        if (TextUtils.isEmpty(mTitle)) {
            mTitle = mContext.getString(R.string.app_name);
        }

        boolean whiteIcon = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
        String channelId = mContext.getString(R.string.app_name);
        NotificationCompat.Builder noti_builder = new NotificationCompat.Builder(mContext, channelId)
                .setPriority(NotificationCompat.PRIORITY_MAX) // it will popup on UI same as incoming default call
                .setContentTitle(mTitle)
                .setContentText(mDesc)
                .setLargeIcon(bitmap)
                .setSmallIcon(getNotificationIcon())
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mDesc));
//                .setColor(whiteIcon ? mContext.getResources().getColor(R.color.colorPrimary, null)
//                        : mContext.getResources().getColor(android.R.color.transparent, null));

        if (!isAutoCancel) {
            noti_builder.setOngoing(true);
        }

        if (pendingIntent == null) {
            if (taskStackBuilder == null) {
                Log.v(TAG, "show:1 ");
                mNotificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                pendingIntent = PendingIntent.getActivity(mContext
                        , mNotifId, mNotificationIntent
                        , PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                Log.v(TAG, "show:2 ");
                pendingIntent = taskStackBuilder.getPendingIntent(mNotifId, PendingIntent.FLAG_UPDATE_CURRENT);
            }
        }

        noti_builder.setContentIntent(pendingIntent);

        if (whiteIcon) {
            noti_builder.setColor(whiteIcon
                    ? mContext.getResources().getColor(R.color.colorPrimary)
                    : mContext.getResources().getColor(android.R.color.transparent));
        }

        Notification noti = noti_builder.build();
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);

        createNotificationChannel();

        playSound();

        noti.flags |= Notification.FLAG_AUTO_CANCEL;// hide the menu_notification after its selected
        //notificationManager.notify(mNotifId, noti);
        NotificationManagerCompat.from(mContext).notify(mNotifId, noti);

    }

    private void playSound() {
        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        switch (am.getRingerMode()) {
            case AudioManager.RINGER_MODE_NORMAL:
                final MediaPlayer mp = MediaPlayer.create(mContext, R.raw.jingle_two);
                mp.start();
                break;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            String channelId = mContext.getString(R.string.app_name);
            CharSequence name = mContext.getString(R.string.app_name);
            String description = mContext.getString(R.string.app_name);
            NotificationChannel channel = new NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            channel.setSound(null, null);
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private int getNotificationIcon() {
        boolean whiteIcon = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        return whiteIcon ? R.mipmap.ic_launcher : R.mipmap.ic_launcher;
//        return whiteIcon ? R.drawable.notification_small : R.mipmap.ic_launcher;
    }

    public static class Builder {
        private Context mContext;
        private String mTitle;
        private int mId;
        private String mDesc;
        private Intent mNotificationIntent = null;

        public Builder(Context context) {
            this.mContext = context;
        }

        public Builder setTitle(String title) {
            this.mTitle = title;
            return this;
        }

        public Builder setDesc(String desc) {
            this.mDesc = desc;
            return this;
        }

        public Builder setId(int id) {
            this.mId = id;
            return this;
        }

        public Builder setNotificationIntent(Intent notificationIntent) {
            this.mNotificationIntent = notificationIntent;
            return this;
        }

        public NotificationUtil build() {
            return new NotificationUtil(mContext, mTitle, mDesc, mNotificationIntent, mId);
        }

    }
}
