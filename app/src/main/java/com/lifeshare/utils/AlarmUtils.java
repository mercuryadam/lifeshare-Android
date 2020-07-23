package com.lifeshare.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.lifeshare.receiver.AlarmReceiver;

import java.util.Calendar;
import java.util.Date;

import static android.content.Context.ALARM_SERVICE;

public class AlarmUtils {
    private static final String TAG = "AlarmUtils";
    public static AlarmUtils alarmManager;
    private int INTERVAL_TIME_IN_HOUR = 1;
    private int INTERNET_CHECK_IN_TIME_IN_MILLIS = 1000 * 10;

    public static AlarmUtils getInstance() {
        if (alarmManager == null) {
            alarmManager = new AlarmUtils();
        }
        return alarmManager;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void setAlarm(Context context) {
        Log.v(TAG, "setAlarm: " + new Date().toString());
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 101, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + INTERNET_CHECK_IN_TIME_IN_MILLIS, pendingIntent);

    }
}
