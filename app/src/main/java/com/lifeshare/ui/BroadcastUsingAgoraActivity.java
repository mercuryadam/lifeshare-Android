
package com.lifeshare.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.lifeshare.BaseActivity;
import com.lifeshare.BuildConfig;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.agorahelper.GLRender;
import com.lifeshare.agorahelper.ImgTexFrame;
import com.lifeshare.agorahelper.SinkConnector;
import com.lifeshare.agorahelper.capture.ScreenCapture;
import com.lifeshare.asyncTask.InitTrueTimeAsyncTask;
import com.lifeshare.customview.bubbleview.BubbleLayout;
import com.lifeshare.customview.bubbleview.BubblesManager;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterRecyclerView;
import com.lifeshare.model.ViewerUser;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.AgoraCreateRequest;
import com.lifeshare.network.request.DeleteStreamingTwilioRequest;
import com.lifeshare.network.request.SendNotificationRequest;
import com.lifeshare.network.request.UpdateViewerCountRequest;
import com.lifeshare.network.response.AgoraCreateResponse;
import com.lifeshare.network.response.CheckSubscriptionResponse;
import com.lifeshare.network.response.CommonResponse;
import com.lifeshare.network.response.MyConnectionListResponse;
import com.lifeshare.network.response.StreamUserListResponse;
import com.lifeshare.permission.RuntimeEasyPermission;
import com.lifeshare.ui.admin_user.ReportsUserListActivity;
import com.lifeshare.ui.invitation.MyInvitationListActivity;
import com.lifeshare.ui.inviteFriends.InviteViaMobileActivity;
import com.lifeshare.ui.my_connection.MyConnectionListActivity;
import com.lifeshare.ui.profile.ViewProfileActivity;
import com.lifeshare.ui.select_connection.SelectConnectionsActivity;
import com.lifeshare.ui.show_broadcast.AgoraShowStreamActivity;
import com.lifeshare.ui.show_broadcast.MessageFragment;
import com.lifeshare.ui.show_broadcast.TwilioStreamUserListAdapter;
import com.lifeshare.ui.show_broadcast.ViewerListAdapter;
import com.lifeshare.utils.AlarmUtils;
import com.lifeshare.utils.Const;
import com.lifeshare.utils.PreferenceHelper;
import com.lifeshare.utils.ScreenCapturerManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.models.ClientRoleOptions;
import io.agora.rtc.video.AgoraVideoFrame;
import io.agora.rtc.video.VideoEncoderConfiguration;
import pub.devrel.easypermissions.EasyPermissions;

import static com.lifeshare.utils.Const.GET_STREAM_USER_INTERVAL_TIME;

public class BroadcastUsingAgoraActivity extends BaseActivity
        implements EasyPermissions.PermissionCallbacks, View.OnClickListener, RuntimeEasyPermission.PermissionCallbacks {

    private static final int MEDIA_PROJECTION_REQUEST_CODE = 1;
    private static final int VIEW_PROFILE_REQUEST_CODE = 188;
    private static final String TAG = "BroadcastAgora";
    private static final int REQUEST_AUDIO_PERM_PUBLISH_BROADCAST = 1123;
    private static final int REQUEST_AUDIO_PERM_SHOW_BROADCAST = 1120;
    private static final int REQUEST_SELECT_CONNECTION_USERS = 159;
    private ScreenCapturerManager screenCapturerManager;
    private static final String LOG_TAG = "AgoraScreenSharing";
    private ScreenCapture mScreenCapture;
    private GLRender mScreenGLRender;
    private RtcEngine mRtcEngine;
    private final VideoEncoderConfiguration mVEC = new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_840x480,
            VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_24,
            VideoEncoderConfiguration.STANDARD_BITRATE,
            VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE);


    private boolean mIsLandSpace = false;
    BubbleLayout bubbleView;
    TextView bubbleText;
    BubbleLayout bubbleLayout;
    ProgressBar bubbleProgressBar;
    DatabaseReference viewerDatabaseReference, countViewerDatabaseReference;
    MessageFragment messageFragment;
    CountDownTimer countDownTimerGetStream;
    ValueEventListener countViewerValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            PreferenceHelper.getInstance().setCountOfViewer((int) dataSnapshot.getChildrenCount());
            tvCountViewer.setText(String.valueOf(PreferenceHelper.getInstance().getCountOfViewer()));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    AdView mAdView;
    private RelativeLayout rlChatView;
    private InterstitialAd mInterstitialAd;
    private BubblesManager bubblesManager;
    private boolean isBubbleViewVisible;
    private FilterRecyclerView rvViewer;
    private LinearLayout llCountViewer;
    private ViewerListAdapter viewerListAdapter;
    private RelativeLayout rlViewers;
    private AppCompatTextView tvNoData, tvCountViewer;
    ValueEventListener viewerValuEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            ArrayList<ViewerUser> viewerUsersList = new ArrayList<>();
            Log.v(TAG, "onDataChange: ");
            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                ViewerUser response = postSnapshot.getValue(ViewerUser.class);
                if (response.getUserId() != null && !response.getUserId().equalsIgnoreCase(PreferenceHelper.getInstance().getUser().getUserId())) {
//                    Log.v(TAG, "onDataChange: " + ((TrueTime.now().getTime()) - Long.parseLong(response.getLastViewTime())));
//                    Log.v(TAG, "onDataChange: True Time - " + ((TrueTime.now().getTime()) + " response time - " + Long.parseLong(response.getLastViewTime())));
//                    if (((TrueTime.now().getTime()) - Long.parseLong(response.getLastViewTime())) <= LAST_VIEW_UPDATE_INTERVAL_TIME) {
                    viewerUsersList.add(response);
//                    }
                }
            }
            Log.v(TAG, "onDataChange: viewerUsersList - " + viewerUsersList.size());
            viewerListAdapter.removeAllItems();
            viewerListAdapter.addItems(viewerUsersList);
            if (viewerUsersList.size() > 0) {
                rvViewer.setVisibility(View.VISIBLE);
                tvNoData.setVisibility(View.GONE);
            } else {
                rvViewer.setVisibility(View.GONE);
                tvNoData.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    private String[] permissions_audio = new String[]{Manifest.permission.RECORD_AUDIO};
    private Boolean isBroadcasting = false;
    private Boolean showErrorDialog = false;
    private TwilioStreamUserListAdapter adapter;
    private FrameLayout container;
    private FloatingActionButton fabMessage;
    private LinearLayout llFriendsBroadcast;
    private FilterRecyclerView rvFriendBroadcast;
    private AppCompatTextView tvNoFriendStreaminig;
    private ProgressBar progressBarConnectionStreaming;
    private boolean isStreamUpdating = false;
    private CircleImageView ivProfileDashBoard;
    private AppCompatTextView tvName;
    private AppCompatTextView tvChannelName;
    private RelativeLayout rlBroadcast;
    private AppCompatTextView tvBroadcast;
    private ImageView ivBroadcast;
    private RelativeLayout rlToolbar;
    private AppCompatTextView tvToolbarTitle;
    private ImageView ivMore;
    private boolean isSubscriptionActive = false;
    private String selectedUsers;
    private String opnTokID = "";
    private boolean isSaveBroadcast = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.publisher_menu, menu);
        return true;
    }

    public void disconnectSessionAndManageState() {
        llCountViewer.setVisibility(View.GONE);
        isBroadcasting = false;
        changeBroadcastButtonView();

        bubbleText.setText(getResources().getString(R.string.off));
        bubbleLayout.setBackground(getResources().getDrawable(R.drawable.gray_circle_bg));
        bubbleLayout.setEnabled(false);
        startTimer();
        removePublisherFromFirebase();
    }

    private void changeBroadcastButtonView() {
        if (isBroadcasting) {
            tvBroadcast.setText(getResources().getString(R.string.stop_broadcast));
            ivBroadcast.setImageResource(R.drawable.ic_pause);

            final int sdk = Build.VERSION.SDK_INT;
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                rlBroadcast.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dashboard_button_background_red));
            } else {
                rlBroadcast.setBackground(ContextCompat.getDrawable(this, R.drawable.dashboard_button_background_red));
            }
            llFriendsBroadcast.setVisibility(View.GONE);
            tvToolbarTitle.setText(getResources().getString(R.string.your_channel_viewer));
        } else {
            tvBroadcast.setText(getResources().getString(R.string.start_broadcast));
            ivBroadcast.setImageResource(R.drawable.ic_play);

            final int sdk = Build.VERSION.SDK_INT;
            if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                rlBroadcast.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dashboard_button_background_green));
            } else {
                rlBroadcast.setBackground(ContextCompat.getDrawable(this, R.drawable.dashboard_button_background_green));
            }

            llFriendsBroadcast.setVisibility(View.VISIBLE);
            tvToolbarTitle.setText(getResources().getString(R.string.app_name));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setProfile();
        //setTwilioCodec();
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1234);
            } else {
                if (!isBubbleViewVisible) {
                    Log.v(TAG, "onResume:1 ");
                    bubblesManager = new BubblesManager.Builder(BroadcastUsingAgoraActivity.this).setTrashLayout(R.layout.bubble_trash)
                            .build();

                    bubblesManager.initialize();
                }
            }
        } else {
            if (!isDestroyed()) {
                Log.v(TAG, "onResume:2 ");
                if (!isBubbleViewVisible) {
                    bubblesManager = new BubblesManager.Builder(BroadcastUsingAgoraActivity.this).setTrashLayout(R.layout.bubble_trash)
                            .build();

                    bubblesManager.initialize();
                }
            }

        }

    }


    private void initModules() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        if (mScreenGLRender == null) {
            mScreenGLRender = new GLRender();
        }
        if (mScreenCapture == null) {
            mScreenCapture = new ScreenCapture(getApplicationContext(), mScreenGLRender, metrics.densityDpi);
        }

        mScreenCapture.mImgTexSrcConnector.connect(new SinkConnector<ImgTexFrame>() {
            @Override
            public void onFormatChanged(Object obj) {
                Log.d(LOG_TAG, "onFormatChanged " + obj.toString());
            }

            @Override
            public void onFrameAvailable(ImgTexFrame frame) {

                if (mRtcEngine == null) {
                    return;
                }

                AgoraVideoFrame vf = new AgoraVideoFrame();
                vf.format = AgoraVideoFrame.FORMAT_TEXTURE_OES;
                vf.timeStamp = frame.pts;
                vf.stride = frame.mFormat.mWidth;
                vf.height = frame.mFormat.mHeight;
                vf.textureID = frame.mTextureId;
                vf.syncMode = true;
                vf.eglContext14 = mScreenGLRender.getEGLContext();
                vf.transform = frame.mTexMatrix;

                mRtcEngine.pushExternalVideoFrame(vf);
            }
        });

        mScreenCapture.setOnScreenCaptureListener(new ScreenCapture.OnScreenCaptureListener() {
            @Override
            public void onStarted() {
                Log.d(LOG_TAG, "Screen Record Started");
            }

            @Override
            public void onError(int err) {
                Log.d(LOG_TAG, "onError " + err);
                switch (err) {
                    case ScreenCapture.SCREEN_ERROR_SYSTEM_UNSUPPORTED:
                        break;
                    case ScreenCapture.SCREEN_ERROR_PERMISSION_DENIED:
                        break;
                }
            }
        });

        WindowManager wm = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = wm.getDefaultDisplay().getWidth();
        int screenHeight = wm.getDefaultDisplay().getHeight();
        if ((mIsLandSpace && screenWidth < screenHeight) ||
                (!mIsLandSpace) && screenWidth > screenHeight) {
            screenWidth = wm.getDefaultDisplay().getHeight();
            screenHeight = wm.getDefaultDisplay().getWidth();
        }

        setOffscreenPreview(screenWidth, screenHeight);
        if (mRtcEngine == null) {
            try {
                mRtcEngine = RtcEngine.create(getApplicationContext(), getString(R.string.agora_app_id), new IRtcEngineEventHandler() {
                    @Override
                    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                        Log.d(LOG_TAG, "onJoinChannelSuccess " + channel + " " + elapsed);
                        Log.d(LOG_TAG, "CHANNEL UID " + channel + " " + uid);


                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                showErrorDialog = true;
                                isBroadcasting = true;
                                agoraCreate(channel, isSaveBroadcast, selectedUsers, String.valueOf(uid).replace("-", ""));
                            }
                        });
                    }


                    @Override
                    public void onWarning(int warn) {

                        Log.d(LOG_TAG, "onWarning " + warn);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideLoading();
                                String errMessage = "";

                                if (showErrorDialog)
                                    if (warn == 1025 || warn == 1019 || warn == 1033 || warn == 1324 || warn == 1031) {
                                        if (warn == 1025) {
                                            errMessage = getResources().getString(R.string.recording_is_interrupted);
                                        } else if (warn == 1019) {
                                            errMessage = getResources().getString(R.string.mic_not_avail);
                                        } else if (warn == 1033) {
                                            errMessage = getResources().getString(R.string.recording_device_in_use);
                                        } else if (warn == 1324) {
                                            errMessage = getResources().getString(R.string.recording_is_released_improperly);
                                        } else if (warn == 1031) {
                                            errMessage = getResources().getString(R.string.recording_is_released_improperly);
                                        }
                                        showErrorDialog = false;
                                        otherDialog(BroadcastUsingAgoraActivity.this, errMessage, getResources().getString(R.string.okay), "", new DismissListenerWithStatus() {
                                            @Override
                                            public void onDismissed(String message) {
                                            }
                                        });
                                    }
                            }
                        });

                    }

                    @Override
                    public void onError(int err) {
                        Log.d(LOG_TAG, "onError " + err);

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                showToast(getString(R.string.err_while_joining));
                                hideLoading();
                                container.setVisibility(View.GONE);
                                fabMessage.hide();
                                removePublisherFromFirebase();

                            }
                        });
                    }

                    @Override
                    public void onAudioRouteChanged(int routing) {
                        Log.d(LOG_TAG, "onAudioRouteChanged " + routing);
                    }

                    @Override
                    // Listen for the onUserOffline callback.
                    // This callback occurs when the host leaves the channel or drops offline.
                    public void onUserOffline(final int uid, int reason) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(LOG_TAG, "User offline, uid: " + (uid & 0xFFFFFFFFL));

                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

                                        container.setVisibility(View.GONE);
                                        fabMessage.hide();
                                        removePublisherFromFirebase();

                                    }
                                });

                            }
                        });
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();


            }

            mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
            mRtcEngine.enableVideo();

            if (mRtcEngine.isTextureEncodeSupported()) {
                mRtcEngine.setExternalVideoSource(true, true, true);
            } else {
                throw new RuntimeException("Can not work on device do not supporting texture" + mRtcEngine.isTextureEncodeSupported());
            }

            mRtcEngine.setVideoEncoderConfiguration(mVEC);

            ClientRoleOptions clientRoleOptions = new ClientRoleOptions();
            clientRoleOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY;
            mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER, clientRoleOptions);
        }
    }


    private void setProfile() {

        tvName.setText(PreferenceHelper.getInstance().getUser().getFirstName() + " " + PreferenceHelper.getInstance().getUser().getLastName());
        tvChannelName.setText(PreferenceHelper.getInstance().getUser().getChannelName());

        Glide.with(LifeShare.getInstance())
                .load(PreferenceHelper.getInstance().getUser().getAvatar())
                .apply(new RequestOptions().error(R.drawable.user_placeholder).placeholder(R.drawable.user_placeholder))
                .into(ivProfileDashBoard);
    }

    private void createRoomAndGetId() {
        if (!checkInternetConnection()) {
            return;
        }
        showLoading(getString(R.string.waiting_for_connection_msg));

        if (selectedUsers != null) {
            onLiveSharingScreenClicked(true);
        }

    }

    private void setStreamingConnection() {

        progressBarConnectionStreaming.setVisibility(View.VISIBLE);
        tvNoFriendStreaminig.setVisibility(View.GONE);
        rvFriendBroadcast.setVisibility(View.GONE);
        getAgoraBroadcastList();
        startGetStreamTimer();

    }

    private void startGetStreamTimer() {
        countDownTimerGetStream = new CountDownTimer(Integer.MAX_VALUE, GET_STREAM_USER_INTERVAL_TIME) {

            @Override
            public void onTick(long l) {
                if (!isStreamUpdating) {
                    getAgoraBroadcastList();
                }
            }

            @Override
            public void onFinish() {

            }
        };
        countDownTimerGetStream.start();
    }

    private void createFirebaseData(AgoraCreateResponse agoraCreateResponse) {
        DatabaseReference databaseReference = LifeShare.getFirebaseReference().child(Const.TABLE_PUBLISHER).child(PreferenceHelper.getInstance().getUser().getUserId());
        databaseReference.removeValue();
        HashMap<String, String> startRequestMap = new HashMap<>();
        startRequestMap.put("sessionId", agoraCreateResponse.getRoomName());
        startRequestMap.put("sessionToken", agoraCreateResponse.getToken());
        startRequestMap.put("opentokId", agoraCreateResponse.getId());
        databaseReference.setValue(startRequestMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                getViewerList();
                getCountForViewers();
                fabMessage.show();
                rlChatView.setVisibility(View.VISIBLE);

                messageFragment.setCurrentStream(PreferenceHelper.getInstance().getUser().getUserId(), agoraCreateResponse.getId(), agoraCreateResponse.getsId(), isSubscriptionActive);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SELECT_CONNECTION_USERS:

                    ArrayList<MyConnectionListResponse> checkedItems = new ArrayList<MyConnectionListResponse>();
                    ArrayList<String> ids = new ArrayList<String>();
                    Bundle extras = data.getExtras();
                    checkedItems = extras.getParcelableArrayList(Const.SELECTED_USERS);
                    if (checkedItems != null) {
                        for (MyConnectionListResponse checkedItem : checkedItems) {

                            ids.add(checkedItem.getUserId());
                        }

                        selectedUsers = TextUtils.join(",", ids);
                    }

                    startBroadCast();
                    break;
            }

        } else {
            switch (requestCode) {
                case MEDIA_PROJECTION_REQUEST_CODE:
                    Toast.makeText(this, getString(R.string.screen_capture_permission_message), Toast.LENGTH_SHORT).show();
                    changeBroadcastButtonView();
                    break;
                case VIEW_PROFILE_REQUEST_CODE:
                    checkSubscription();
                    break;
                case REQUEST_SELECT_CONNECTION_USERS:
                    if (resultCode == RESULT_CANCELED) {
                        if (bubbleProgressBar != null) {
                            bubbleProgressBar.setVisibility(View.GONE);
                            bubbleText.setText(getResources().getString(R.string.start));
                            bubbleLayout.setBackground(getResources().getDrawable(R.drawable.green_circle_bg));
                            bubbleLayout.setEnabled(true);
                        }

                    }
                    break;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void removePublisherFromFirebase() {
        removeValueEventListener();
        rlViewers.setVisibility(View.GONE);
        if (PreferenceHelper.getInstance().getUser() != null) {
            DatabaseReference databaseReference = LifeShare.getFirebaseReference().child(Const.TABLE_PUBLISHER).child(PreferenceHelper.getInstance().getUser().getUserId());
            databaseReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            });
        }

    }

    private void getCountForViewers() {
        Log.v(TAG, "registerCountValueEventListener: ");
        countViewerDatabaseReference = LifeShare.getFirebaseReference()
                .child(Const.TABLE_PUBLISHER)
                .child(PreferenceHelper.getInstance().getUser().getUserId())
                .child(Const.TABLE_COUNT_VIEWER);
        countViewerDatabaseReference.addValueEventListener(countViewerValueEventListener);
    }

    private void createBubblelayout() {
        bubbleView = (BubbleLayout) LayoutInflater
                .from(BroadcastUsingAgoraActivity.this).inflate(R.layout.bubble_layout, null);
        bubbleText = bubbleView.findViewById(R.id.avatar);
        bubbleLayout = bubbleView.findViewById(R.id.bubble_main);
        bubbleProgressBar = bubbleView.findViewById(R.id.progressBar);
        bubbleText.setText(getResources().getString(R.string.stop));
        bubbleLayout.setBackground(getResources().getDrawable(R.drawable.red_circle_bg));

        bubbleView.setOnBubbleClickListener(new BubbleLayout.OnBubbleClickListener() {
            @Override
            public void onBubbleClick(BubbleLayout bubble) {
                if (!checkInternetConnection()) {
                    return;
                }
                if (bubbleText.getText().toString().trim().equalsIgnoreCase(getResources().getString(R.string.start))) {
                    Log.v(TAG, "onBubbleClick: if ");
                    bubbleProgressBar.setVisibility(View.VISIBLE);
                    bubbleLayout.setEnabled(false);
                    tvBroadcast.setText(getResources().getString(R.string.stop_broadcast));
//                    switchCompat.setChecked(true);
                    checkAudioPermissionAndStartBroadCast();

                } else {
                    Log.v(TAG, "onBubbleClick: else ");
                    bubbleText.setText(getResources().getString(R.string.off));
                    bubbleLayout.setBackground(getResources().getDrawable(R.drawable.gray_circle_bg));
                    bubbleLayout.setEnabled(false);
                    tvBroadcast.setText(getResources().getString(R.string.start_broadcast));
//                    switchCompat.setChecked(false);
                    stopBroadcast();

                }
            }
        });

        bubbleView.setOnBubbleRemoveListener(new BubbleLayout.OnBubbleRemoveListener() {
            @Override
            public void onBubbleRemoved(BubbleLayout bubble) {
                isBubbleViewVisible = false;
                getWindowManager().removeView(bubbleView);
                Log.v(TAG, "onBubbleRemoved: ");
            }
        });
        isBubbleViewVisible = true;
        bubblesManager.addBubble(bubbleView, 60, 20);
    }

    private void startTimer() {
        new CountDownTimer(3000, 1000) {

            public void onTick(long millisUntilFinished) {
                Log.v(TAG, "onTick: ");
            }

            public void onFinish() {
                Log.v(TAG, "onFinish: ");
                bubbleText.setText(getResources().getString(R.string.start));
                bubbleLayout.setBackground(getResources().getDrawable(R.drawable.green_circle_bg));
                bubbleLayout.setEnabled(true);
            }

        }.start();


    }

    @Override
    public void onBackPressed() {
        if (container.getVisibility() == View.VISIBLE) {
            container.setVisibility(View.GONE);
        } else {

            if (PreferenceHelper.getInstance().getCountOfViewer() > 0) {
                updateCountForViewerToServer();
            }

            Log.v(TAG, "onBackPressed: " + isFinishing());
            if (!isFinishing()) {
                if (isBubbleViewVisible) {

                    getWindowManager().removeView(bubbleLayout);
                    isBubbleViewVisible = false;
                }
            }
            onLiveSharingScreenClicked(false);
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (PreferenceHelper.getInstance().getCountOfViewer() > 0) {
            updateCountForViewerToServer();
        }
        AlarmUtils.getInstance().setAlarm(this);

        Log.v(TAG, "onDestroy:1 ");
        if (isBubbleViewVisible) {
            getWindowManager().removeView(bubbleLayout);
        }
        onLiveSharingScreenClicked(false);
        countDownTimerGetStream.cancel();

        deInitModules();

        if (Build.VERSION.SDK_INT >= 29) {
            screenCapturerManager.unbindService();
        }

        if (bubblesManager != null)
            bubblesManager.recycle();
        super.onDestroy();
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.v(TAG, "onPermissionsGranted: ");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fabMessage:
                if (container.getVisibility() == View.VISIBLE) {
                    container.setVisibility(View.GONE);
                } else {
                    container.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.rl_broadcast:

                if (!isBroadcasting) {
                    checkAudioPermissionAndStartBroadCast();
                } else {
                    stopBroadcast();
                }

                break;
            case R.id.iv_more:
                showDialog();
                break;
        }
    }

    private void stopBroadcast() {

        if (Build.VERSION.SDK_INT >= 29) {
            screenCapturerManager.endForeground();
        }

        onLiveSharingScreenClicked(false);

        playAudio(this, R.raw.dingdong);

        Log.v(TAG, "onCheckedChanged: false ");
        fabMessage.hide();
        rlChatView.setVisibility(View.GONE);
        container.setVisibility(View.GONE);
        if (PreferenceHelper.getInstance().getCountOfViewer() > 0) {
            updateCountForViewerToServer();
        }


        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.v(TAG, "stopBroadcast: The interstitial wasn't loaded yet");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_using_agora);

        initView();
        new InitTrueTimeAsyncTask().execute();
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(getResources().getString(R.string.app_name) + "(OFF)");
        getSupportFragmentManager().beginTransaction().add(R.id.container, messageFragment).commit();

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        setStreamingConnection();

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        //Banner ad
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //Interstitial Ad
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstial_adv_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                Log.v(TAG, "onAdClosed: ");
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.v(TAG, "onAdFailedToLoad: " + loadAdError.getMessage() + ", " + loadAdError.getDomain() + " , " + loadAdError.toString());
            }
        });


    }

    private void checkSubscription() {
        showLoading();
        WebAPIManager.getInstance().checkSubscription(new RemoteCallback<CheckSubscriptionResponse>(this) {
            @Override
            public void onSuccess(CheckSubscriptionResponse response) {
                hideLoading();
                if (response.getStatus().equalsIgnoreCase("1")) {
                    isSubscriptionActive = true;
                } else {
                    isSubscriptionActive = false;
                }
            }

            @Override
            public void onEmptyResponse(String message) {
                super.onEmptyResponse(message);
                hideLoading();
                isSubscriptionActive = false;
            }
        });
    }

    private void checkAudioPermissionAndStartBroadCast() {

        playAudio(this, R.raw.jingle_two);

        if (isSubscriptionActive) {
            otherDialog(BroadcastUsingAgoraActivity.this, getResources().getString(R.string.save_broadcast_message), getResources().getString(R.string.yes), getResources().getString(R.string.no), new DismissListenerWithStatus() {
                @Override
                public void onDismissed(String message) {
                    if (message.equalsIgnoreCase(getResources().getString(R.string.yes))) {
                        isSaveBroadcast = true;
                        RuntimeEasyPermission.newInstance(permissions_audio,
                                REQUEST_AUDIO_PERM_PUBLISH_BROADCAST, "Allow microphone permission").show(getSupportFragmentManager());

                    } else {
                        isSaveBroadcast = false;
                        RuntimeEasyPermission.newInstance(permissions_audio,
                                REQUEST_AUDIO_PERM_PUBLISH_BROADCAST, "Allow microphone permission").show(getSupportFragmentManager());

                    }
                }
            });
        } else {
            isSaveBroadcast = false;
            RuntimeEasyPermission.newInstance(permissions_audio,
                    REQUEST_AUDIO_PERM_PUBLISH_BROADCAST, "Allow microphone permission").show(getSupportFragmentManager());

        }


    }

    private void updateCountForViewerToServer() {
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

    private void initView() {
        rlChatView = (RelativeLayout) findViewById(R.id.rl_chat_message);
        rvViewer = findViewById(R.id.rv_viewer);
        llCountViewer = findViewById(R.id.llCountViewer);
        tvCountViewer = findViewById(R.id.tvCountViewer);

        if (Build.VERSION.SDK_INT >= 29) {
            screenCapturerManager = new ScreenCapturerManager(this);
        }

        rvViewer.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        viewerListAdapter = new ViewerListAdapter(new BaseRecyclerListener<ViewerUser>() {
            @Override
            public void showEmptyDataView(int resId) {

            }

            @Override
            public void onRecyclerItemClick(View view, int position, ViewerUser item) {

            }
        });
        rvViewer.setAdapter(viewerListAdapter);

        rlViewers = findViewById(R.id.rl_viewers);
        tvNoData = findViewById(R.id.tv_no_data);
        container = findViewById(R.id.container);
        fabMessage = findViewById(R.id.fabMessage);
        fabMessage.setOnClickListener(this);
        messageFragment = MessageFragment.newInstance();

        container.setVisibility(View.GONE);

        llFriendsBroadcast = (LinearLayout) findViewById(R.id.ll_friends_broadcast);
        rvFriendBroadcast = (FilterRecyclerView) findViewById(R.id.rv_friend_broadcast);
        progressBarConnectionStreaming = (ProgressBar) findViewById(R.id.progress_connection_streaming);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvFriendBroadcast.setLayoutManager(linearLayoutManager);
        adapter = new TwilioStreamUserListAdapter(new BaseRecyclerListener<StreamUserListResponse>() {
            @Override
            public void showEmptyDataView(int resId) {


            }

            @Override
            public void onRecyclerItemClick(View view, int position, StreamUserListResponse item) {
                if (RuntimeEasyPermission.newInstance(permissions_audio,
                        REQUEST_AUDIO_PERM_SHOW_BROADCAST, "Allow microphone permission").hasPermissions(permissions_audio)) {

                    if (item.getId() != null)
                        if (!item.getId().isEmpty()) {
                            LifeShare.getInstance().clearNotificationById(Integer.parseInt(item.getId()));
                        }
                    playAudio(BroadcastUsingAgoraActivity.this, R.raw.click);
                    Intent intent = new Intent(BroadcastUsingAgoraActivity.this, AgoraShowStreamActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(Const.STREAM_DATA, item);
                    intent.putExtras(bundle);
                    startActivity(intent);

                } else {
                    RuntimeEasyPermission.newInstance(permissions_audio,
                            REQUEST_AUDIO_PERM_SHOW_BROADCAST, "Allow microphone permission").show(getSupportFragmentManager());

                }
            }
        });
        rvFriendBroadcast.setEmptyMsgHolder(tvNoData);
        rvFriendBroadcast.setAdapter(adapter);

        tvNoFriendStreaminig = (AppCompatTextView) findViewById(R.id.tv_no_friend_streaminig);
        ivProfileDashBoard = (CircleImageView) findViewById(R.id.iv_profile);
        tvName = (AppCompatTextView) findViewById(R.id.tv_name);
        tvChannelName = (AppCompatTextView) findViewById(R.id.tv_channel_name);
        rlBroadcast = (RelativeLayout) findViewById(R.id.rl_broadcast);
        tvBroadcast = (AppCompatTextView) findViewById(R.id.tv_broadcast);
        ivBroadcast = (ImageView) findViewById(R.id.iv_broadcast);

        rlToolbar = (RelativeLayout) findViewById(R.id.rl_toolbar);
        tvToolbarTitle = (AppCompatTextView) findViewById(R.id.tv_toolbar_title);
        ivMore = (ImageView) findViewById(R.id.iv_more);

        changeBroadcastButtonView();
        rlBroadcast.setOnClickListener(this);
        ivMore.setOnClickListener(this);
        checkSubscription();
    }


    private void getViewerList() {
        Log.v(TAG, "registerViewerValueEventListener: ");
        rlViewers.setVisibility(View.VISIBLE);
        viewerDatabaseReference = LifeShare.getFirebaseReference()
                .child(Const.TABLE_PUBLISHER)
                .child(PreferenceHelper.getInstance().getUser().getUserId())
                .child(Const.TABLE_VIEWER);
        viewerDatabaseReference.addValueEventListener(viewerValuEventListener);
    }

    public void removeValueEventListener() {
        if (viewerDatabaseReference != null) {
            Log.v(TAG, "disconnectSession: ");
            viewerDatabaseReference.removeEventListener(viewerValuEventListener);
        }
        if (countViewerDatabaseReference != null) {
            Log.v(TAG, "disconnectSession: ");
            countViewerDatabaseReference.removeEventListener(countViewerValueEventListener);
        }
    }

    void showDialog() {

        Dialog dialog = new Dialog(BroadcastUsingAgoraActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialog.setContentView(R.layout.nav_drawer_new);

        ImageView ivClose = (AppCompatImageView) dialog.findViewById(R.id.iv_close);
        CircleImageView ivProfile = (CircleImageView) dialog.findViewById(R.id.iv_profile);
        AppCompatTextView logout = (AppCompatTextView) dialog.findViewById(R.id.logout);
        AppCompatTextView tvHome = (AppCompatTextView) dialog.findViewById(R.id.tv_home);
        AppCompatTextView tvProfile = (AppCompatTextView) dialog.findViewById(R.id.tv_profile);
        AppCompatTextView tvMyConnections = (AppCompatTextView) dialog.findViewById(R.id.tv_my_connections);
        AppCompatTextView tvInvitations = (AppCompatTextView) dialog.findViewById(R.id.tv_invitations);
        AppCompatTextView tvDialogName = (AppCompatTextView) dialog.findViewById(R.id.tv_name);
        AppCompatTextView tvAboutLifeshare = (AppCompatTextView) dialog.findViewById(R.id.tv_about_lifeshare);
        AppCompatTextView tvVersion = (AppCompatTextView) dialog.findViewById(R.id.tv_version);
        AppCompatTextView tvReports = (AppCompatTextView) dialog.findViewById(R.id.tv_report);
        AppCompatTextView tvInvite = (AppCompatTextView) dialog.findViewById(R.id.tv_invite);
        View view = (View) dialog.findViewById(R.id.view);

        tvVersion.setText("V" + BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")");
        tvDialogName.setText(PreferenceHelper.getInstance().getUser().getFirstName() + " " + PreferenceHelper.getInstance().getUser().getLastName());
        if (PreferenceHelper.getInstance().getUser().getUserType().equals("1")) {
            tvReports.setVisibility(View.VISIBLE);
        } else {
            tvReports.setVisibility(View.GONE);
        }

        Glide.with(LifeShare.getInstance())
                .load(PreferenceHelper.getInstance().getUser().getAvatar())
                .apply(new RequestOptions().error(R.drawable.user_placeholder).placeholder(R.drawable.user_placeholder))
                .into(ivProfile);

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                dialog.dismiss();
                return false;
            }
        });
        tvAboutLifeshare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://lifesharemobileapp.com"));
                startActivity(browserIntent);
            }
        });


        tvInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BroadcastUsingAgoraActivity.this, InviteViaMobileActivity.class));
                dialog.dismiss();
            }
        });

        tvReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PreferenceHelper.getInstance().getUser().getUserType().equals("1")) {
                    startActivity(new Intent(BroadcastUsingAgoraActivity.this, ReportsUserListActivity.class));
                    dialog.dismiss();
                }
            }
        });
        tvInvitations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BroadcastUsingAgoraActivity.this, MyInvitationListActivity.class));
                dialog.dismiss();
            }
        });
        tvHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otherDialog(BroadcastUsingAgoraActivity.this, getResources().getString(R.string.logout_message), getResources().getString(R.string.yes), getResources().getString(R.string.no), new DismissListenerWithStatus() {
                    @Override
                    public void onDismissed(String message) {
                        if (message.equalsIgnoreCase(getResources().getString(R.string.yes))) {
                            logoutCall();
                        }
                    }
                });

            }
        });

        tvMyConnections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BroadcastUsingAgoraActivity.this, MyConnectionListActivity.class));
                dialog.dismiss();
            }
        });

        tvProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(BroadcastUsingAgoraActivity.this, ViewProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(Const.PROFILE, Const.MY_PROFILE);
                intent.putExtras(bundle);
                startActivityForResult(intent, VIEW_PROFILE_REQUEST_CODE);

                dialog.dismiss();
            }
        });
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void getAgoraBroadcastList() {
        isStreamUpdating = true;
        WebAPIManager.getInstance().getAgoraBroadcastList(new RemoteCallback<ArrayList<StreamUserListResponse>>(this) {
            @Override
            public void onSuccess(ArrayList<StreamUserListResponse> response) {

                isStreamUpdating = false;
                progressBarConnectionStreaming.setVisibility(View.GONE);
                ArrayList<StreamUserListResponse> responseArrayList = new ArrayList<>();
                responseArrayList.clear();
                responseArrayList.addAll(response);
                if (responseArrayList.size() > 0) {
                    adapter.removeAllItems();
                    tvNoFriendStreaminig.setVisibility(View.GONE);
                    rvFriendBroadcast.setVisibility(View.VISIBLE);
                    adapter.addItems(responseArrayList);
                } else {
                    adapter.removeAllItems();
                    tvNoFriendStreaminig.setVisibility(View.VISIBLE);
                    rvFriendBroadcast.setVisibility(View.GONE);
                    fabMessage.hide();
                    rlChatView.setVisibility(View.GONE);
                    tvNoFriendStreaminig.setText(R.string.no_live_streaming_message);
                }
            }

            @Override
            public void onEmptyResponse(String message) {
                super.onEmptyResponse(message);
                progressBarConnectionStreaming.setVisibility(View.GONE);
                isStreamUpdating = false;
                adapter.removeAllItems();
                tvNoFriendStreaminig.setVisibility(View.VISIBLE);
                tvNoFriendStreaminig.setText(R.string.no_live_streaming_message);

            }

            @Override
            public void onFailed(Throwable throwable) {
                isStreamUpdating = false;
                progressBarConnectionStreaming.setVisibility(View.GONE);

            }

            @Override
            public void onInternetFailed() {
                isStreamUpdating = false;
                progressBarConnectionStreaming.setVisibility(View.GONE);
            }

            @Override
            public void onUnauthorized(Throwable throwable) {
                isStreamUpdating = false;
                progressBarConnectionStreaming.setVisibility(View.GONE);

            }
        });

    }

    private void logoutCall() {
        showLoading();
        WebAPIManager.getInstance().logout(new RemoteCallback<CommonResponse>(this) {
            @Override
            public void onSuccess(CommonResponse response) {
                hideLoading();
                LifeShare.getInstance().logout();
            }

            @Override
            public void onUnauthorized(Throwable throwable) {
                super.onUnauthorized(throwable);
                hideLoading();
                LifeShare.getInstance().logout();

            }

            @Override
            public void onFailed(Throwable throwable) {
                super.onFailed(throwable);
                hideLoading();
                LifeShare.getInstance().logout();
            }

            @Override
            public void onInternetFailed() {
                super.onInternetFailed();
                hideLoading();
                LifeShare.getInstance().logout();
            }
        });
    }

    @Override
    public void onPermissionAllow(int permissionCode) {
        if (permissionCode == REQUEST_AUDIO_PERM_PUBLISH_BROADCAST) {
            startActivityForResult(new Intent(this, SelectConnectionsActivity.class), REQUEST_SELECT_CONNECTION_USERS);
        }
    }

    private void startBroadCast() {
        if (Build.VERSION.SDK_INT >= 29) {
            screenCapturerManager.startForeground();
        }
        if (checkInternetConnection()) {
            createRoomAndGetId();
        } else {
            stopBroadcast();
        }


    }


    @Override
    public void onPermissionDeny(int permissionCode) {
        if (permissionCode == REQUEST_AUDIO_PERM_PUBLISH_BROADCAST) {
            Toast.makeText(this, R.string.msg_permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    private void notifyOther(String opentokId) {
        if (!checkInternetConnection()) {
            return;
        }
        SendNotificationRequest request = new SendNotificationRequest();
        request.setId(opentokId);
        WebAPIManager.getInstance().agoraNotifyOther(request, new RemoteCallback<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse response) {
            }

            @Override
            public void onEmptyResponse(String message) {
            }
        });
    }

    private void agoraCreate(String channelName, boolean isSaveBroadcast, String users, String randomNumber) {

        if (!checkInternetConnection()) {
            return;
        }
        AgoraCreateRequest request = new AgoraCreateRequest();
        request.setChannelName(channelName);
        request.setRoomName(channelName);
        request.setSessionId(randomNumber);
        request.setToken(randomNumber);
        request.setRoomSId(randomNumber);
        request.setSaveBroadcast(isSaveBroadcast);
        request.setSaveChat(isSaveBroadcast);
        request.setUsers(users);

        WebAPIManager.getInstance().agoraCreate(request, new RemoteCallback<AgoraCreateResponse>() {
            @Override
            public void onSuccess(AgoraCreateResponse response) {
                opnTokID = response.getId();

                notifyOther(response.getId());


                changeBroadcastButtonView();

                fabMessage.show();
                rlChatView.setVisibility(View.VISIBLE);


                if (!isBubbleViewVisible) {
                    createBubblelayout();
                } else {
                    bubbleText.setText(getResources().getString(R.string.stop));
                    bubbleLayout.setBackground(getResources().getDrawable(R.drawable.red_circle_bg));
                    bubbleLayout.setEnabled(true);
                    bubbleProgressBar.setVisibility(View.GONE);
                }
                llCountViewer.setVisibility(View.VISIBLE);
                showToast(getString(R.string.broadcasting));

                createFirebaseData(response);

                hideLoading();
            }

            @Override
            public void onEmptyResponse(String message) {
                hideLoading();
            }
        });
    }

    public void setOffscreenPreview(int width, int height) throws IllegalArgumentException {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid offscreen resolution");
        }

        mScreenGLRender.init(width, height);
    }

    public void onLiveSharingScreenClicked(boolean join) {


        if (join) {
            initModules();
            mScreenCapture.start();
            Random random = new Random();
            mRtcEngine.joinChannel(null,
                    PreferenceHelper.getInstance().getUser().getUsername() + "_" + String.format(Locale.getDefault(), "%05d", random.nextInt(100000)),
                    "", 0);
        } else {
            if (opnTokID != null) {
                if (!opnTokID.isEmpty()) {
                    deleteStreaming();
                }
            }
            if (mScreenCapture != null) {
                mScreenCapture.stop();
            }
        }
    }

    private void deInitModules() {
        if (mRtcEngine != null)
            RtcEngine.destroy();
        mRtcEngine = null;

        if (mScreenCapture != null) {
            mScreenCapture.release();
            mScreenCapture = null;
        }

        if (mScreenGLRender != null) {
            mScreenGLRender.quit();
            mScreenGLRender = null;
        }
    }

    public void deleteStreaming() {
        showLoading();
        DeleteStreamingTwilioRequest request = new DeleteStreamingTwilioRequest();
        request.setId(opnTokID);
        WebAPIManager.getInstance().deleteStreamingAgora(request, new RemoteCallback<CommonResponse>(this) {
            @Override
            public void onSuccess(CommonResponse response) {
                PreferenceHelper.getInstance().setRoomData(null);
                hideLoading();

                if (mRtcEngine != null) {
                    mRtcEngine.leaveChannel();
                }
                deInitModules();

                opnTokID = "";
                if (PreferenceHelper.getInstance().getCountOfViewer() > 0) {
                    updateCountForViewerToServer();
                }
                disconnectSessionAndManageState();

            }

            @Override
            public void onFailed(Throwable throwable) {
                if (mRtcEngine != null) {
                    mRtcEngine.leaveChannel();
                }
                deInitModules();
                hideLoading();
                disconnectSessionAndManageState();
            }

            @Override
            public void onUnauthorized(Throwable throwable) {
                if (mRtcEngine != null) {
                    mRtcEngine.leaveChannel();
                }
                deInitModules();
                hideLoading();
                disconnectSessionAndManageState();

            }

            @Override
            public void onEmptyResponse(String message) {
                if (mRtcEngine != null) {
                    mRtcEngine.leaveChannel();
                }
                deInitModules();
                hideLoading();
                disconnectSessionAndManageState();
            }
        });
    }


}
