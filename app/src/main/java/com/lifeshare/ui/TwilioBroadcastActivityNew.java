package com.lifeshare.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.instacart.library.truetime.TrueTime;
import com.lifeshare.BaseActivity;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.asyncTask.InitTrueTimeAsyncTask;
import com.lifeshare.customview.bubbleview.BubbleLayout;
import com.lifeshare.customview.bubbleview.BubblesManager;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterRecyclerView;
import com.lifeshare.model.ViewerUser;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.CreateRoomWithUserRequest;
import com.lifeshare.network.request.DeleteStreamingTwilioRequest;
import com.lifeshare.network.request.SendNotificationRequest;
import com.lifeshare.network.request.UpdateViewerCountRequest;
import com.lifeshare.network.response.CommonResponse;
import com.lifeshare.network.response.CreateRoomResponse;
import com.lifeshare.network.response.MyConnectionListResponse;
import com.lifeshare.network.response.StreamUserListResponse;
import com.lifeshare.permission.RuntimeEasyPermission;
import com.lifeshare.ui.admin_user.ReportsUserListActivity;
import com.lifeshare.ui.invitation.MyInvitationListActivity;
import com.lifeshare.ui.my_connection.MyConnectionListActivity;
import com.lifeshare.ui.profile.ViewProfileActivity;
import com.lifeshare.ui.select_connection.SelectConnectionsActivity;
import com.lifeshare.ui.show_broadcast.MessageFragment;
import com.lifeshare.ui.show_broadcast.TwilioShowStreamActivityNew;
import com.lifeshare.ui.show_broadcast.TwilioStreamUserListAdapter;
import com.lifeshare.ui.show_broadcast.ViewerListAdapter;
import com.lifeshare.utils.AlarmUtils;
import com.lifeshare.utils.Const;
import com.lifeshare.utils.PreferenceHelper;
import com.lifeshare.utils.ScreenCapturerManager;
import com.lifeshare.utils.TwilioHelper;
import com.twilio.audioswitch.AudioDevice;
import com.twilio.audioswitch.AudioSwitch;
import com.twilio.video.AspectRatio;
import com.twilio.video.AudioCodec;
import com.twilio.video.ConnectOptions;
import com.twilio.video.EncodingParameters;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.Room;
import com.twilio.video.ScreenCapturer;
import com.twilio.video.TwilioException;
import com.twilio.video.Video;
import com.twilio.video.VideoCodec;
import com.twilio.video.VideoConstraints;
import com.twilio.video.VideoDimensions;
import com.twilio.video.VideoView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import pub.devrel.easypermissions.EasyPermissions;

import static com.lifeshare.utils.Const.GET_STREAM_USER_INTERVAL_TIME;
import static com.lifeshare.utils.Const.LAST_VIEW_UPDATE_INTERVAL_TIME;

public class TwilioBroadcastActivityNew extends BaseActivity
        implements EasyPermissions.PermissionCallbacks, View.OnClickListener, RuntimeEasyPermission.PermissionCallbacks {

    private static final int MEDIA_PROJECTION_REQUEST_CODE = 1;
    private static final String TAG = "BroadcastActivity";
    private static final int RC_VIDEO_APP_PERM = 124;
    private static final int REQUEST_AUDIO_PERM = 1123;
    private static final int REQUEST_MEDIA_PROJECTION = 100;
    private static final int REQUEST_SELECT_CONNECTION_USERS = 159;
    private static final String LOCAL_AUDIO_TRACK_NAME = "mic";
    BubbleLayout bubbleView;
    TextView bubbleText;
    BubbleLayout bubbleLayout;
    ProgressBar bubbleProgressBar;
    CreateRoomResponse sessionData;
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
    private RelativeLayout mPublisherViewContainer;
    private RelativeLayout rlChatView;
    private TextView tvText;
    private RelativeLayout rlReceiver;
    private InterstitialAd mInterstitialAd;
    private CountDownTimer timer;
    //    private SwitchCompat switchCompat;
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
                    Log.v(TAG, "onDataChange: " + ((TrueTime.now().getTime()) - Long.parseLong(response.getLastViewTime())));
                    Log.v(TAG, "onDataChange: True Time - " + ((TrueTime.now().getTime()) + " response time - " + Long.parseLong(response.getLastViewTime())));
                    if (((TrueTime.now().getTime()) - Long.parseLong(response.getLastViewTime())) <= LAST_VIEW_UPDATE_INTERVAL_TIME) {
                        viewerUsersList.add(response);
                    }
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
    private ScreenCapturerManager screenCapturerManager;
    private String[] permissions_audio = new String[]{Manifest.permission.RECORD_AUDIO};
    private Boolean isBroadcasting = false;
    private TwilioStreamUserListAdapter adapter;
    private ImageView ivLogo;
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
    private VideoView localVideoView;
    private AudioSwitch audioSwitch;
    private int savedVolumeControlStream;
    private ScreenCapturer screenCapturer;
    private AudioCodec audioCodec;
    private VideoCodec videoCodec;
    private boolean enableAutomaticSubscription;
    private EncodingParameters encodingParameters;
    private LocalVideoTrack screenVideoTrack;
    private LocalAudioTrack localAudioTrack;
    private Room room;
    private final ScreenCapturer.Listener screenCapturerListener = new ScreenCapturer.Listener() {
        @Override
        public void onScreenCaptureError(String errorDescription) {
            Log.e(TAG, "Screen capturer error: " + errorDescription);
            stopScreenCapture();
            Toast.makeText(TwilioBroadcastActivityNew.this, "Error in screen capture",
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFirstFrameAvailable() {
            Log.d(TAG, "First frame from screen capturer available");
        }
    };
    private String selectedUsers;
    private LocalParticipant localParticipant;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.publisher_menu, menu);
        return true;
    }

    public void disconnectSessionAndManageState() {
        disconnectSession();
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
        setTwilioCodec();
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1234);
            } else {
                if (!isBubbleViewVisible) {
                    Log.v(TAG, "onResume:1 ");
                    bubblesManager = new BubblesManager.Builder(TwilioBroadcastActivityNew.this).setTrashLayout(R.layout.bubble_trash)
                            .build();

                    bubblesManager.initialize();
                }
            }
        } else {
            if (!isDestroyed()) {
                Log.v(TAG, "onResume:2 ");
                if (!isBubbleViewVisible) {
                    bubblesManager = new BubblesManager.Builder(TwilioBroadcastActivityNew.this).setTrashLayout(R.layout.bubble_trash)
                            .build();

                    bubblesManager.initialize();
                }
            }

        }

    }

    private void setTwilioCodec() {
        audioCodec = TwilioHelper.getInstance().getAudioCodecPreference(TwilioHelper.PREF_AUDIO_CODEC,
                TwilioHelper.PREF_AUDIO_CODEC_DEFAULT);
        videoCodec = TwilioHelper.getInstance().getVideoCodecPreference(TwilioHelper.PREF_VIDEO_CODEC,
                TwilioHelper.PREF_VIDEO_CODEC_DEFAULT);
        enableAutomaticSubscription = TwilioHelper.getInstance().getAutomaticSubscriptionPreference(TwilioHelper.PREF_ENABLE_AUTOMATIC_SUBSCRIPTION,
                TwilioHelper.PREF_ENABLE_AUTOMATIC_SUBSCRIPTION_DEFAULT);
        final EncodingParameters newEncodingParameters = TwilioHelper.getInstance().getEncodingParameters();
        this.encodingParameters = newEncodingParameters;

    }

    private void connectToRoom(String roomName, String token) {
        Log.v(TAG, "connectToRoom: roomName : " + roomName + " - Token:" + token);
        audioSwitch.activate();
        ConnectOptions.Builder connectOptionsBuilder = new ConnectOptions.Builder(token)
                .roomName(roomName);
        if (localAudioTrack != null) {
            connectOptionsBuilder
                    .audioTracks(Collections.singletonList(localAudioTrack));
        }
        if (screenVideoTrack != null) {
            connectOptionsBuilder.videoTracks(Collections.singletonList(screenVideoTrack));
        }

        connectOptionsBuilder.preferAudioCodecs(Collections.singletonList(audioCodec));
        connectOptionsBuilder.preferVideoCodecs(Collections.singletonList(videoCodec));
        connectOptionsBuilder.encodingParameters(encodingParameters);

        connectOptionsBuilder.enableAutomaticSubscription(enableAutomaticSubscription);

        room = Video.connect(this, connectOptionsBuilder.build(), new Room.Listener() {
            @Override
            public void onConnected(@NonNull Room room) {
                hideLoading();
//                showToast("onConnected");
                localParticipant = room.getLocalParticipant();

                isBroadcasting = true;

                notifyOther(sessionData.getId());
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

            }

            @Override
            public void onConnectFailure(@NonNull Room room, @NonNull TwilioException twilioException) {
//                showToast("onConnectFailure");
                audioSwitch.deactivate();
                container.setVisibility(View.GONE);
                fabMessage.hide();
                removePublisherFromFirebase();

            }

            @Override
            public void onReconnecting(@NonNull Room room, @NonNull TwilioException twilioException) {
//                showToast("onReconnecting");
                showLoading();
            }

            @Override
            public void onReconnected(@NonNull Room room) {
//                showToast("onReconnected");
                hideLoading();
            }

            @Override
            public void onDisconnected(@NonNull Room room, @Nullable TwilioException twilioException) {
//                showToast("onDisconnected");
                localParticipant = null;
                hideLoading();
                TwilioBroadcastActivityNew.this.room = null;
                audioSwitch.deactivate();

                container.setVisibility(View.GONE);
                fabMessage.hide();
                removePublisherFromFirebase();
            }

            @Override
            public void onParticipantConnected(@NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {
//                showToast("onParticipantConnected");
            }

            @Override
            public void onParticipantDisconnected(@NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {
                Log.v(TAG, "onParticipantDisconnected: " + remoteParticipant.getIdentity());
//                showToast("onParticipantDisconnected: " + remoteParticipant.getIdentity());
            }

            @Override
            public void onRecordingStarted(@NonNull Room room) {
//                showToast("onRecordingStarted");
                Log.v(TAG, "onRecordingStarted: ");
            }

            @Override
            public void onRecordingStopped(@NonNull Room room) {
//                showToast("onRecordingStopped");
                Log.v(TAG, "onRecordingStopped: ");
            }
        });

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
            CreateRoomWithUserRequest createRoomWithUserRequest = new CreateRoomWithUserRequest();
            createRoomWithUserRequest.setSaveBroadCast(false);
            createRoomWithUserRequest.setUsers(selectedUsers);
            WebAPIManager.getInstance().createRoom(createRoomWithUserRequest, new RemoteCallback<CreateRoomResponse>() {
                @Override
                public void onSuccess(CreateRoomResponse response) {
                    sessionData = response;
                    PreferenceHelper.getInstance().setRoomData(sessionData);
                    if (response != null
                            && !TextUtils.isEmpty(response.getRoomName())
                            && !TextUtils.isEmpty(response.getToken())) {

                        startScreenCapture();
                        createFirebaseData(response);
                    } else {
                        showToast(getString(R.string.message_invalid_session));
                        hideLoading();
                    }

                }
            });
        }

    }

    private void startScreenCapture() {
        VideoConstraints videoConstraints = new VideoConstraints.Builder()
                .aspectRatio(AspectRatio.ASPECT_RATIO_4_3)
                .minVideoDimensions(VideoDimensions.CIF_VIDEO_DIMENSIONS)
                .maxVideoDimensions(VideoDimensions.CIF_VIDEO_DIMENSIONS)
                .minFps(5).maxFps(15)
                .build();


        screenVideoTrack = LocalVideoTrack.create(this, true, screenCapturer, videoConstraints);
        localAudioTrack = LocalAudioTrack.create(this, true, LOCAL_AUDIO_TRACK_NAME);
        localAudioTrack.enable(true);
        localVideoView.setVisibility(View.VISIBLE);
        screenVideoTrack.addRenderer(localVideoView);

        connectToRoom(sessionData.getRoomName(), sessionData.getToken());
    }

    private void stopScreenCapture() {
        if (screenVideoTrack != null) {
            screenVideoTrack.removeRenderer(localVideoView);
            screenVideoTrack.release();
            screenVideoTrack = null;
            localVideoView.setVisibility(View.INVISIBLE);
        }

        if (room != null) {
            room.disconnect();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        setContentView(R.layout.twilio_activity_broadcast);

        initView();
        new InitTrueTimeAsyncTask().execute();
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name) + "(OFF)");
        getSupportFragmentManager().beginTransaction().add(R.id.container, messageFragment).commit();

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
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });

    }

    private void setStreamingConnection() {

        progressBarConnectionStreaming.setVisibility(View.VISIBLE);
        tvNoFriendStreaminig.setVisibility(View.GONE);
        rvFriendBroadcast.setVisibility(View.GONE);
//        getCurrentStreamingConnection();
        getCurrentStreamingConnectionTwilio();
        startGetStreamTimer();

    }

    private void startGetStreamTimer() {
        countDownTimerGetStream = new CountDownTimer(Integer.MAX_VALUE, GET_STREAM_USER_INTERVAL_TIME) {

            @Override
            public void onTick(long l) {
                if (!isStreamUpdating) {
//                    getCurrentStreamingConnection();
                    getCurrentStreamingConnectionTwilio();
                }
            }

            @Override
            public void onFinish() {

            }
        };
        countDownTimerGetStream.start();
    }

    private void createFirebaseData(CreateRoomResponse response) {
        DatabaseReference databaseReference = LifeShare.getFirebaseReference().child(Const.TABLE_PUBLISHER).child(PreferenceHelper.getInstance().getUser().getUserId());
        databaseReference.removeValue();
        HashMap<String, String> startRequestMap = new HashMap<>();
        startRequestMap.put("sessionId", response.getRoomName());
        startRequestMap.put("sessionToken", response.getToken());
        startRequestMap.put("opentokId", response.getId());
        databaseReference.setValue(startRequestMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                getViewerList();
                getCountForViewers();
                fabMessage.show();
                rlChatView.setVisibility(View.VISIBLE);
                messageFragment.setCurrentStream(PreferenceHelper.getInstance().getUser().getUserId());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_MEDIA_PROJECTION: {

                    Toast.makeText(this, R.string.screen_capture_permission_message,
                            Toast.LENGTH_LONG).show();

                    screenCapturer = new ScreenCapturer(this, resultCode, data, screenCapturerListener);

                    startBroadCast();
                }
                break;
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
                case REQUEST_SELECT_CONNECTION_USERS:
                    if (resultCode == RESULT_CANCELED) {
                        bubbleProgressBar.setVisibility(View.GONE);
                        bubbleText.setText(getResources().getString(R.string.start));
                        bubbleLayout.setBackground(getResources().getDrawable(R.drawable.green_circle_bg));
                        bubbleLayout.setEnabled(true);

                    }
                    break;
            }
        }
    }

    private void removePublisherFromFirebase() {
        removeValueEventListener();
        rlViewers.setVisibility(View.GONE);
        DatabaseReference databaseReference = LifeShare.getFirebaseReference().child(Const.TABLE_PUBLISHER).child(PreferenceHelper.getInstance().getUser().getUserId());
        databaseReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });

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
                .from(TwilioBroadcastActivityNew.this).inflate(R.layout.bubble_layout, null);
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

            if (sessionData != null) {
                deleteStreaming();
            }

            Log.v(TAG, "onBackPressed: " + isFinishing());
            if (!isFinishing()) {
                if (isBubbleViewVisible) {

                    getWindowManager().removeView(bubbleLayout);
                    isBubbleViewVisible = false;
                }
            }
            disconnectSession();
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (sessionData != null) {
            deleteStreaming();
        }
        AlarmUtils.getInstance().setAlarm(this);

        Log.v(TAG, "onDestroy:1 ");
        if (isBubbleViewVisible) {
            getWindowManager().removeView(bubbleLayout);
        }
        disconnectSession();
        countDownTimerGetStream.cancel();
        super.onDestroy();
    }

    private void disconnectSession() {
        stopScreenCapture();
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
        stopScreenCapture();

        playAudio(this, R.raw.dingdong);

        Log.v(TAG, "onCheckedChanged: false ");
        fabMessage.hide();
        rlChatView.setVisibility(View.GONE);
        container.setVisibility(View.GONE);
        deleteStreaming();

        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("TAG", "The interstitial wasn't loaded yet.");
        }
    }

    private void checkAudioPermissionAndStartBroadCast() {

//        startForGroundService();
        playAudio(this, R.raw.jingle_two);

        RuntimeEasyPermission.newInstance(permissions_audio,
                REQUEST_AUDIO_PERM, "Allow microphone permission").show(getSupportFragmentManager());


    }

    public void deleteStreaming() {
        if (sessionData == null) {
            return;
        }
        showLoading();
        DeleteStreamingTwilioRequest request = new DeleteStreamingTwilioRequest();
        request.setId(sessionData.getId());
        WebAPIManager.getInstance().deleteStreamingTwilio(request, new RemoteCallback<CommonResponse>(this) {
            @Override
            public void onSuccess(CommonResponse response) {
                PreferenceHelper.getInstance().setRoomData(null);
                hideLoading();
                if (PreferenceHelper.getInstance().getCountOfViewer() > 0) {
                    updateCountForViewerToServer();
                }
                disconnectSessionAndManageState();

            }

            @Override
            public void onFailed(Throwable throwable) {
                hideLoading();
                disconnectSessionAndManageState();
            }

            @Override
            public void onUnauthorized(Throwable throwable) {
                hideLoading();
                disconnectSessionAndManageState();

            }

            @Override
            public void onEmptyResponse(String message) {
                hideLoading();
                disconnectSessionAndManageState();
            }
        });
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
        tvText = (TextView) findViewById(R.id.tv_text);
        rlReceiver = (RelativeLayout) findViewById(R.id.rl_receiver);
        mPublisherViewContainer = (RelativeLayout) findViewById(R.id.publisherview);
        localVideoView = (VideoView) findViewById(R.id.local_video);
        rlChatView = (RelativeLayout) findViewById(R.id.rl_chat_message);
        rvViewer = findViewById(R.id.rv_viewer);
        llCountViewer = findViewById(R.id.llCountViewer);
        tvCountViewer = findViewById(R.id.tvCountViewer);

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
        ivLogo = findViewById(R.id.iv_logo);
        ivLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
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
                if (!item.getId().isEmpty()) {
                    LifeShare.getInstance().clearNotificationById(Integer.parseInt(item.getId()));
                }
                playAudio(TwilioBroadcastActivityNew.this, R.raw.click);
                Intent intent = new Intent(TwilioBroadcastActivityNew.this, TwilioShowStreamActivityNew.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(Const.STREAM_DATA, item);
                intent.putExtras(bundle);
                startActivity(intent);
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

        initializeTwilioComponent();

    }

    private void initializeTwilioComponent() {
        if (Build.VERSION.SDK_INT >= 29) {
            screenCapturerManager = new ScreenCapturerManager(this);
        }

        audioSwitch = new AudioSwitch(getApplicationContext());
        savedVolumeControlStream = getVolumeControlStream();
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        audioSwitch.start((audioDevices, audioDevice) -> {
//            updateAudioDeviceIcon(audioDevice);
            return Unit.INSTANCE;
        });

        List<AudioDevice> availableAudioDevices = audioSwitch.getAvailableAudioDevices();
        audioSwitch.selectDevice(availableAudioDevices.get(availableAudioDevices.size() - 1));

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

        Dialog dialog = new Dialog(TwilioBroadcastActivityNew.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
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
        AppCompatTextView tvReports = (AppCompatTextView) dialog.findViewById(R.id.tv_report);
        View view = (View) dialog.findViewById(R.id.view);

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
        tvReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PreferenceHelper.getInstance().getUser().getUserType().equals("1")) {
                    startActivity(new Intent(TwilioBroadcastActivityNew.this, ReportsUserListActivity.class));
                    dialog.dismiss();
                }
            }
        });
        tvInvitations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TwilioBroadcastActivityNew.this, MyInvitationListActivity.class));
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
                otherDialog(TwilioBroadcastActivityNew.this, getResources().getString(R.string.logout_message), getResources().getString(R.string.yes), getResources().getString(R.string.no), new DismissListenerWithStatus() {
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
                startActivity(new Intent(TwilioBroadcastActivityNew.this, MyConnectionListActivity.class));
                dialog.dismiss();
            }
        });

        tvProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(TwilioBroadcastActivityNew.this, ViewProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(Const.PROFILE, Const.MY_PROFILE);
                intent.putExtras(bundle);
                startActivity(intent);

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

    private void getCurrentStreamingConnectionTwilio() {
        isStreamUpdating = true;
        WebAPIManager.getInstance().getCurrentConnectionStreamingListTwilio(new RemoteCallback<ArrayList<StreamUserListResponse>>(this) {
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
        if (permissionCode == REQUEST_AUDIO_PERM) {


            startActivityForResult(new Intent(this, SelectConnectionsActivity.class), REQUEST_SELECT_CONNECTION_USERS);

        }
    }

    private void startBroadCast() {
        if (screenCapturer == null) {
            requestScreenCapturePermission();
        } else {

            if (Build.VERSION.SDK_INT >= 29) {
                screenCapturerManager.startForeground();
            }
            if (checkInternetConnection()) {
                createRoomAndGetId();
            } else {
                stopBroadcast();
            }
        }

    }

    private void requestScreenCapturePermission() {
        Log.d(TAG, "Requesting permission to capture screen");
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        // This initiates a prompt dialog for the user to confirm screen projection.
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION);
    }

    @Override
    public void onPermissionDeny(int permissionCode) {
        if (permissionCode == REQUEST_AUDIO_PERM) {
            Toast.makeText(this, R.string.msg_permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    private void notifyOther(String opentokId) {
        if (!checkInternetConnection()) {
            return;
        }
        SendNotificationRequest request = new SendNotificationRequest();
        request.setId(opentokId);
        WebAPIManager.getInstance().notifyOther(request, new RemoteCallback<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse response) {
            }

            @Override
            public void onEmptyResponse(String message) {
            }
        });
    }

}
