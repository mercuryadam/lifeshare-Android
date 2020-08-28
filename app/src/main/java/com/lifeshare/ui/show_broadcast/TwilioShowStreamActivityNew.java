package com.lifeshare.ui.show_broadcast;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
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
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.DeleteStreamingRequest;
import com.lifeshare.network.response.CommonResponse;
import com.lifeshare.network.response.LoginResponse;
import com.lifeshare.network.response.StreamUserResponse;
import com.lifeshare.utils.Const;
import com.lifeshare.utils.PreferenceHelper;
import com.lifeshare.utils.TwilioHelper;
import com.opentok.android.Session;
import com.opentok.android.Subscriber;
import com.twilio.video.AudioCodec;
import com.twilio.video.ConnectOptions;
import com.twilio.video.EncodingParameters;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.Room;
import com.twilio.video.TwilioException;
import com.twilio.video.Video;
import com.twilio.video.VideoCodec;
import com.twilio.video.VideoView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.lifeshare.utils.Const.LAST_VIEW_UPDATE_INTERVAL_TIME;

public class TwilioShowStreamActivityNew extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "ShowStreamActivity";
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
    String accessToken = TwilioHelper.TWILIO_ACCESS_TOKEN_SUBSCRIBER;
    private Subscriber mSubscriber;
    private Session mSession;
    private LinearLayout rlReceiver;
    private StreamUserResponse currentVisibleStram;
    //    private AppBarLayout receiverAppbar;
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
    private VideoView primaryVideoView;
    private LocalAudioTrack localAudioTrack;
    private LocalVideoTrack localVideoTrack;
    private AudioCodec audioCodec;
    private VideoCodec videoCodec;
    private boolean enableAutomaticSubscription;
    private EncodingParameters encodingParameters;
    private LocalVideoTrack screenVideoTrack;
    private Room room;
    private LocalParticipant localParticipant;

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


        rlReceiver = findViewById(R.id.rl_receiver);
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twilio_activity_show_stream_new);
        initView();
        new InitTrueTimeAsyncTask().execute();
        if (getIntent() != null && getIntent().getExtras() != null) {
            currentVisibleStram = getIntent().getExtras().getParcelable(Const.STREAM_DATA);

//            connectSession(currentVisibleStram.getSessionId(), currentVisibleStram.getToken());

            connectToRoom("XYZ");
            messageFragment.setCurrentStream(currentVisibleStram.getUserId());

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
        setTwilioCodec();
    }

    private void setTwilioCodec() {
        audioCodec = TwilioHelper.getInstance().getAudioCodecPreference(TwilioHelper.PREF_AUDIO_CODEC,
                TwilioHelper.PREF_AUDIO_CODEC_DEFAULT);
        videoCodec = TwilioHelper.getInstance().getVideoCodecPreference(TwilioHelper.PREF_VIDEO_CODEC,
                TwilioHelper.PREF_VIDEO_CODEC_DEFAULT);
        enableAutomaticSubscription = TwilioHelper.getInstance().getAutomaticSubscriptionPreference(TwilioHelper.PREF_ENABLE_AUTOMATIC_SUBSCRIPTION,
                TwilioHelper.PREF_ENABLE_AUTOMATIC_SUBSCRIPTION_DEFAULT);
        final EncodingParameters newEncodingParameters = TwilioHelper.getInstance().getEncodingParameters();

        if (localVideoTrack == null) {

            /*
             * If connected to a Room then share the local video track.
             */
            if (localParticipant != null) {
                localParticipant.publishTrack(localVideoTrack);

                /*
                 * Update encoding parameters if they have changed.
                 */
                if (!newEncodingParameters.equals(encodingParameters)) {
                    localParticipant.setEncodingParameters(newEncodingParameters);
                }
            }
        }


        this.encodingParameters = newEncodingParameters;

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
            super.onBackPressed();
            removeUserFromViewer();
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

    private void disconnectSession() {
        removeValueEventListener();
        if (mSession == null) {
            return;
        }

        mSession.disconnect();
        rlReceiver.removeAllViews();

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

    private void deleteStreamUser(String userId) {

        if (!checkInternetConnection()) {
            return;
        }

        DeleteStreamingRequest request = new DeleteStreamingRequest();
        request.setOpentokId(currentVisibleStram.getOpentokId());
        WebAPIManager.getInstance().deleteStreaming(request, new RemoteCallback<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse response) {
                removePublisherFromFirebase(userId);
            }

            @Override
            public void onUnauthorized(Throwable throwable) {
                removePublisherFromFirebase(userId);
            }

            @Override
            public void onFailed(Throwable throwable) {
                removePublisherFromFirebase(userId);

            }

            @Override
            public void onInternetFailed() {
                removePublisherFromFirebase(userId);
            }

            @Override
            public void onEmptyResponse(String message) {
                removePublisherFromFirebase(userId);
            }
        });

    }

    private void removePublisherFromFirebase(String userId) {
        removeValueEventListener();
        DatabaseReference databaseReference = LifeShare.getFirebaseReference().child(Const.TABLE_PUBLISHER).child(userId);
        databaseReference.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });

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
        mSession.disconnect();
        removeUserFromViewer();
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

    private void connectToRoom(String roomName) {

        showLoading();
        llStreamProgress.setVisibility(View.VISIBLE);
        streamProgressBar.setVisibility(View.VISIBLE);
        tvStreamMessage.setText(getString(R.string.waiting_for_connection_msg));


        ConnectOptions.Builder connectOptionsBuilder = new ConnectOptions.Builder(accessToken)
                .roomName(roomName);

        /*
         * Add local audio track to connect options to share with participants.
         */
        if (localAudioTrack != null) {
      /*      connectOptionsBuilder
                    .audioTracks(Collections.singletonList(localAudioTrack));
      */
        }

        /*
         * Add local video track to connect options to share with participants.
         */
        if (localVideoTrack != null) {
            connectOptionsBuilder.videoTracks(Collections.singletonList(localVideoTrack));
        }

        /*
         * Set the preferred audio and video codec for media.
         */
        connectOptionsBuilder.preferAudioCodecs(Collections.singletonList(audioCodec));
        connectOptionsBuilder.preferVideoCodecs(Collections.singletonList(videoCodec));

        /*
         * Set the sender side encoding parameters.
         */
        connectOptionsBuilder.encodingParameters(encodingParameters);

        connectOptionsBuilder.enableAutomaticSubscription(enableAutomaticSubscription);

        room = Video.connect(this, connectOptionsBuilder.build(), roomListener());
    }

    @SuppressLint("SetTextI18n")
    private Room.Listener roomListener() {
        return new Room.Listener() {
            @Override
            public void onConnected(Room room) {
                hideLoading();
                showToast("onConnected");
                localParticipant = room.getLocalParticipant();
                setTitle(room.getName());

                addViewerToStream();
                updateCountForViewer();

                llStreamProgress.setVisibility(View.GONE);
                tvToolbarTitle.setText(room.getName());
                rlReceiver.setVisibility(View.VISIBLE);
                fabMessage.show();


      /*          for (RemoteParticipant remoteParticipant : room.getRemoteParticipants()) {
                    addRemoteParticipant(remoteParticipant);
                    break;
                }
      */
            }

            @Override
            public void onReconnecting(@NonNull Room room, @NonNull TwilioException twilioException) {
                showToast("onReconnecting");
                streamProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReconnected(@NonNull Room room) {
                showToast("onReconnected");
                streamProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onConnectFailure(Room room, TwilioException e) {
                showToast("onConnectFailure");
            }

            @Override
            public void onDisconnected(Room room, TwilioException e) {
                showToast("onDisconnected");
                localParticipant = null;
                TwilioShowStreamActivityNew.this.room = null;

            }

            @Override
            public void onParticipantConnected(Room room, RemoteParticipant remoteParticipant) {
                showToast("onParticipantDisconnected");
//                addRemoteParticipant(remoteParticipant);
            }

            @Override
            public void onParticipantDisconnected(Room room, RemoteParticipant remoteParticipant) {
                showToast("onParticipantDisconnected");

                // TODO: 27-08-2020 check for publisher then do following

                currentVisibleStram = null;
                llStreamProgress.setVisibility(View.VISIBLE);
                rlReceiver.setVisibility(View.GONE);
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

            @Override
            public void onRecordingStarted(Room room) {
                showToast("onRecordingStarted");
                /*
                 * Indicates when media shared to a Room is being recorded. Note that
                 * recording is only available in our Group Rooms developer preview.
                 */
                Log.d(TAG, "onRecordingStarted");
            }

            @Override
            public void onRecordingStopped(Room room) {
                showToast("onRecordingStarted");
                /*
                 * Indicates when media shared to a Room is no longer being recorded. Note that
                 * recording is only available in our Group Rooms developer preview.
                 */
                Log.d(TAG, "onRecordingStopped");
            }
        };
    }


}
