package com.lifeshare.ui.show_broadcast;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterRecyclerView;
import com.lifeshare.model.ViewerUser;
import com.lifeshare.network.response.LoginResponse;
import com.lifeshare.network.response.StreamUserListResponse;
import com.lifeshare.utils.Const;
import com.lifeshare.utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.models.ClientRoleOptions;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

import static com.lifeshare.utils.Const.LAST_VIEW_UPDATE_INTERVAL_TIME;

public class AgoraShowStreamActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "ShowStreamActivity";
    private static final String LOG_TAG = "AgoraScreenSharing";
    private RtcEngine mRtcEngine;
    private final VideoEncoderConfiguration mVEC = new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_840x480,
            VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_24,
            VideoEncoderConfiguration.STANDARD_BITRATE,
            VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE);

    CountDownTimer countDownTimerViewerLastTime;
    MessageFragment messageFragment;
    DatabaseReference viewerDatabaseReference, countViewerDatabaseReference;
    ValueEventListener countViewerValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    AudioManager audioManager;
    private StreamUserListResponse currentVisibleStram;
    private ProgressBar streamProgressBar;
    private LinearLayout llStreamProgress;
    private AppCompatTextView tvStreamMessage;
    private FilterRecyclerView rvViewer;
    private FrameLayout container;
    private FloatingActionButton fabMessage;
    private ImageView icBack;
    private AppCompatTextView tvToolbarTitle;
    private CircleImageView ivProfile;
    private ViewerListAdapter viewerListAdapter;
    ValueEventListener viewerValuEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            ArrayList<ViewerUser> viewerUsersList = new ArrayList<>();
            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                ViewerUser response = postSnapshot.getValue(ViewerUser.class);
                if (response.getUserId() != null && !response.getUserId().equalsIgnoreCase(PreferenceHelper.getInstance().getUser().getUserId())) {
                    if (((TrueTime.now().getTime()) - Long.parseLong(response.getLastViewTime())) < LAST_VIEW_UPDATE_INTERVAL_TIME) {
                        viewerUsersList.add(response);
                    }
                }
            }

            viewerListAdapter.removeAllItems();
            viewerListAdapter.addItems(viewerUsersList);
            if (viewerUsersList.size() > 0) {
                rvViewer.setVisibility(View.VISIBLE);
            } else {
                rvViewer.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    private FrameLayout primaryVideoView;
    private int savedVolumeControlStream;

    private void initView() {
        Log.v(TAG, "initView: " + LifeShare.getFirebaseReference().push().getKey());
        tvToolbarTitle = (AppCompatTextView) findViewById(R.id.tvToolbarTitle);
        rvViewer = findViewById(R.id.rv_viewer);

        primaryVideoView = findViewById(R.id.primary_video_view);
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

        streamProgressBar = findViewById(R.id.streamProgressBar);
        llStreamProgress = findViewById(R.id.ll_stream_progress);
        tvStreamMessage = findViewById(R.id.tv_stream_message);

        container = findViewById(R.id.container);
        fabMessage = findViewById(R.id.fabMessage);
        fabMessage.setOnClickListener(this);
        messageFragment = MessageFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.container, messageFragment).commit();

        container.setVisibility(View.GONE);
        icBack = (ImageView) findViewById(R.id.ic_back);
        tvToolbarTitle = (AppCompatTextView) findViewById(R.id.tvToolbarTitle);
        ivProfile = (CircleImageView) findViewById(R.id.ivProfile);
        icBack.setOnClickListener(this);

        initializeTwiloCompoenent();
    }

    private void initializeTwiloCompoenent() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);
        audioManager.setMicrophoneMute(true);

        savedVolumeControlStream = getVolumeControlStream();
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agora_show_stream);
        initView();
        new InitTrueTimeAsyncTask().execute();
        if (getIntent() != null && getIntent().getExtras() != null) {
            currentVisibleStram = getIntent().getParcelableExtra(Const.STREAM_DATA);
        }

        if (currentVisibleStram != null) {
            onLiveSharingScreenClicked(true, currentVisibleStram.getChannelName());
            messageFragment.setCurrentStream(currentVisibleStram.getUserId(), currentVisibleStram.getId(), currentVisibleStram.getsId(), false);
            tvToolbarTitle.setText(currentVisibleStram.getChannelName());

            Glide.with(LifeShare.getInstance())
                    .load(currentVisibleStram.getAvatar())
                    .apply(new RequestOptions().error(R.drawable.user_placeholder).placeholder(R.drawable.user_placeholder))
                    .into(ivProfile);
        }

    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    private void addViewerToStream() {
        Log.v(TAG, "addViewerToStream: ");
        LoginResponse user = PreferenceHelper.getInstance().getUser();
        DatabaseReference databaseReference = LifeShare.getFirebaseReference()
                .child(Const.TABLE_PUBLISHER)
                .child(currentVisibleStram.getUserId())
                .child(Const.TABLE_VIEWER)
                .child(user.getUserId());
        HashMap<String, String> startRequestMap = new HashMap<>();
        startRequestMap.put("userId", user.getUserId());
        startRequestMap.put("firstName", user.getFirstName());
        startRequestMap.put("lastName", user.getLastName());
        startRequestMap.put("username", user.getUsername());
        startRequestMap.put("profileUrl", user.getAvatar());
        startRequestMap.put("lastViewTime", String.valueOf(TrueTime.now().getTime()));
        databaseReference.setValue(startRequestMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.v(TAG, "onSuccess:addViewerToStream ");
                registerViewerValueEventListener();
                startViewerLastTimeUpdateHandler();
            }
        });
    }

    private void updateCountForViewer() {
        Log.v(TAG, "updateCountForViewer: ");
        LoginResponse user = PreferenceHelper.getInstance().getUser();
        DatabaseReference databaseReference = LifeShare.getFirebaseReference()
                .child(Const.TABLE_PUBLISHER)
                .child(currentVisibleStram.getUserId())
                .child(Const.TABLE_COUNT_VIEWER).push();
        databaseReference.setValue("").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.v(TAG, "onSuccess:updateCountForViewer ");
                countViewerDatabaseReference = LifeShare.getFirebaseReference()
                        .child(Const.TABLE_PUBLISHER)
                        .child(currentVisibleStram.getUserId())
                        .child(Const.TABLE_COUNT_VIEWER);
                countViewerDatabaseReference.addValueEventListener(countViewerValueEventListener);
            }
        });

    }

    private void registerViewerValueEventListener() {
        Log.v(TAG, "registerViewerValueEventListener: ");
        viewerDatabaseReference = LifeShare.getFirebaseReference()
                .child(Const.TABLE_PUBLISHER)
                .child(currentVisibleStram.getUserId())
                .child(Const.TABLE_VIEWER);
        viewerDatabaseReference.addValueEventListener(viewerValuEventListener);
    }

    private void startLastViewTimeUpdate() {
        Log.v(TAG, "startLastViewTimeUpdate: " + String.valueOf(TrueTime.now().getTime()));
        LoginResponse user = PreferenceHelper.getInstance().getUser();
        DatabaseReference databaseReference = LifeShare.getFirebaseReference()
                .child(Const.TABLE_PUBLISHER)
                .child(currentVisibleStram.getUserId())
                .child(Const.TABLE_VIEWER)
                .child(user.getUserId());

        databaseReference.child("lastViewTime").setValue(String.valueOf(TrueTime.now().getTime()));
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (container.getVisibility() == View.VISIBLE) {
            container.setVisibility(View.GONE);
        } else {
            onLiveSharingScreenClicked(false, "");
            super.onBackPressed();

        }
    }

    private void removeUserFromViewer() {
        Log.v(TAG, "removeUserFromViewer: ");
        if (countDownTimerViewerLastTime != null) {
            countDownTimerViewerLastTime.cancel();
        }
        if (currentVisibleStram != null) {
            DatabaseReference databaseReference = LifeShare.getFirebaseReference()
                    .child(Const.TABLE_PUBLISHER)
                    .child(currentVisibleStram.getUserId())
                    .child(Const.TABLE_VIEWER)
                    .child(PreferenceHelper.getInstance().getUser().getUserId());
            databaseReference.removeValue();
        }
        rvViewer.setVisibility(View.INVISIBLE);

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

    private void startViewerLastTimeUpdateHandler() {

        countDownTimerViewerLastTime = new CountDownTimer(Integer.MAX_VALUE, LAST_VIEW_UPDATE_INTERVAL_TIME) {

            @Override
            public void onTick(long l) {
                Log.v(TAG, "onTick: ");
                startLastViewTimeUpdate();
            }

            @Override
            public void onFinish() {

            }
        };
        countDownTimerViewerLastTime.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimerViewerLastTime != null) {
            countDownTimerViewerLastTime.cancel();
        }
        removeUserFromViewer();

        /*
         * Tear down audio management and restore previous volume stream
         */
        setVolumeControlStream(savedVolumeControlStream);

        /*
         * Always disconnect from the room before leaving the Activity to
         * ensure any memory allocated to the Room resource is freed.
         */
        RtcEngine.destroy();
        mRtcEngine = null;


        finish();

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
            case R.id.ic_back:
                onBackPressed();
                break;
        }
    }


    private void initModules() {
        if (mRtcEngine == null) {
            try {
                mRtcEngine = RtcEngine.create(getApplicationContext(), getString(R.string.agora_app_id), new IRtcEngineEventHandler() {
                    @Override
                    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                        Log.d(LOG_TAG, "onJoinChannelSuccess " + channel + " " + elapsed);
                        Log.d(LOG_TAG, "CHANNEL UID " + channel + " " + uid);

                      /*  localParticipant = room.getLocalParticipant();
                        setTitle(room.getName());*/

                        addViewerToStream();
                        updateCountForViewer();

                        llStreamProgress.setVisibility(View.GONE);
                        fabMessage.show();
                    }


                    @Override
                    public void onWarning(int warn) {
                        Log.d(LOG_TAG, "onWarning " + warn);
                        showToast(getString(R.string.err_while_joining));
                    }

                    @Override
                    public void onError(int err) {
                        Log.d(LOG_TAG, "onError " + err);
                        showToast(getString(R.string.err_while_joining));
                    }

                    @Override
                    public void onAudioRouteChanged(int routing) {
                        Log.d(LOG_TAG, "onAudioRouteChanged " + routing);
                    }

                    @Override
                    // Listen for the onFirstRemoteVideoDecoded callback.
                    // This callback occurs when the first video frame of the host is received and decoded after the host successfully joins the channel.
                    // You can call the setupRemoteVideo method in this callback to set up the remote video view.
                    public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("agora", "First remote video decoded, uid: " + (uid & 0xFFFFFFFFL));
                                setupVideoView(uid);
                            }
                        });
                    }

                    @Override
                    // Listen for the onUserOffline callback.
                    // This callback occurs when the host leaves the channel or drops offline.
                    public void onUserOffline(final int uid, int reason) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("agora", "User offline, uid: " + (uid & 0xFFFFFFFFL));

                                if (container.getVisibility() == View.VISIBLE) {
                                    container.setVisibility(View.GONE);
                                }
                                onLiveSharingScreenClicked(false, "");

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
            mRtcEngine.setClientRole(IRtcEngineEventHandler.ClientRole.CLIENT_ROLE_AUDIENCE, clientRoleOptions);

        }
    }

    private void setupVideoView(int uid) {
        // Create a SurfaceView object.
        SurfaceView mLocalView;

        mLocalView = RtcEngine.CreateRendererView(getBaseContext());
        mLocalView.setZOrderOnTop(true);
        mLocalView.setZOrderMediaOverlay(true);
        primaryVideoView.addView(mLocalView);
        // Set the local video view.
        VideoCanvas localVideoCanvas = new VideoCanvas(mLocalView, VideoCanvas.RENDER_MODE_FIT, uid);
        mRtcEngine.setupRemoteVideo(localVideoCanvas);

        //  primaryVideoView.setVisibility(View.VISIBLE);
        //                audioManager.setMicrophoneMute(true);


    }


    public void onLiveSharingScreenClicked(boolean join, String channelName) {
        if (join) {
            initModules();
            mRtcEngine.joinChannel(null, channelName, "", 0);
        } else {
            mRtcEngine.leaveChannel();
            //on leave
            removeUserFromViewer();
            currentVisibleStram = null;
            llStreamProgress.setVisibility(View.VISIBLE);
            primaryVideoView.setVisibility(View.GONE);
            streamProgressBar.setVisibility(View.GONE);
            tvStreamMessage.setText(R.string.stream_drop_message);
            container.setVisibility(View.GONE);
            fabMessage.hide();
            tvStreamMessage.setVisibility(View.VISIBLE);
            if (countDownTimerViewerLastTime != null) {
                countDownTimerViewerLastTime.cancel();
            }
            rvViewer.setVisibility(View.INVISIBLE);
            removeValueEventListener();
        }
    }

}
