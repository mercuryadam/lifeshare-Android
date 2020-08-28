package com.lifeshare.receiver;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.lifeshare.LifeShare;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.DeleteStreamingTwilioRequest;
import com.lifeshare.network.request.UpdateViewerCountRequest;
import com.lifeshare.network.response.CommonResponse;
import com.lifeshare.utils.Const;
import com.lifeshare.utils.PreferenceHelper;

public class StreamingIntentService extends IntentService {
    private static final String TAG = "StreamingIntentService";

    public StreamingIntentService() {
        super("StreamingIntentService");
    }

    public StreamingIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        deleteStreaming();
        updateCountForViewerToServer();

    }

    public void deleteStreaming() {
        Log.v(TAG, "deleteStreaming: ");
        if (PreferenceHelper.getInstance().getRoomData()
                == null) {
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

    private void updateCountForViewerToServer() {
        Log.v(TAG, "updateCountForViewerToServer: " + PreferenceHelper.getInstance().getCountOfViewer());
        if (PreferenceHelper.getInstance().getCountOfViewer() == 0) {
            return;
        }
        UpdateViewerCountRequest request = new UpdateViewerCountRequest();
        request.setCount(PreferenceHelper.getInstance().getCountOfViewer());
        WebAPIManager.getInstance().updateViewerCount(request, new RemoteCallback<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse response) {
                PreferenceHelper.getInstance().setCountOfViewer(0);
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

            @Override
            public void onEmptyResponse(String message) {

            }
        });
    }

}
