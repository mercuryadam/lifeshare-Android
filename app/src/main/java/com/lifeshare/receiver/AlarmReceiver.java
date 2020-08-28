package com.lifeshare.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.lifeshare.LifeShare;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.DeleteStreamingTwilioRequest;
import com.lifeshare.network.response.CommonResponse;
import com.lifeshare.utils.AlarmUtils;
import com.lifeshare.utils.Const;
import com.lifeshare.utils.PreferenceHelper;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (PreferenceHelper.getInstance().getRoomData() != null) {
            if (isInternetAvailable(context)) {
                deleteStreaming();
            } else {
                setAlarm(context);
            }
        }

    }

    public void deleteStreaming() {
        if (PreferenceHelper.getInstance().getRoomData() == null) {
            return;
        }
        DeleteStreamingTwilioRequest request = new DeleteStreamingTwilioRequest();
        request.setId(PreferenceHelper.getInstance().getRoomData().getId());
        WebAPIManager.getInstance().deleteStreamingTwilio(request, new RemoteCallback<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse response) {
                PreferenceHelper.getInstance().setRoomData(null);
                removePublisherFromFirebase();
            }

            @Override
            public void onFailed(Throwable throwable) {
            }

            @Override
            public void onUnauthorized(Throwable throwable) {
            }

            @Override
            public void onEmptyResponse(String message) {
            }
        });
    }

    private void removePublisherFromFirebase() {
        DatabaseReference databaseReference = LifeShare.getFirebaseReference().child(Const.TABLE_PUBLISHER).child(PreferenceHelper.getInstance().getUser().getUserId());
        databaseReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
//                showNotification("Delete From Firebase");
            }
        });

    }

    private boolean isInternetAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }


    private void setAlarm(Context context) {

        showNotification("Alarm Set");
        AlarmUtils.getInstance().setAlarm(context);
    }



    public void showNotification(String message) {
        /*Calendar calendar = Calendar.getInstance();
        String time = calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);

        NotificationUtil notificationUtil = new NotificationUtil(LifeShare.getInstance(), "LifeShare", message + " - " + time, new Intent(), new Random().nextInt());
        notificationUtil.show();
*/
    }
}