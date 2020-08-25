package com.lifeshare.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
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
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
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
import com.lifeshare.BuildConfig;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.ScreensharingCapturer;
import com.lifeshare.asyncTask.InitTrueTimeAsyncTask;
import com.lifeshare.customview.bubbleview.BubbleLayout;
import com.lifeshare.customview.bubbleview.BubblesManager;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterRecyclerView;
import com.lifeshare.model.ViewerUser;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.DeleteStreamingRequest;
import com.lifeshare.network.request.SendNotificationRequest;
import com.lifeshare.network.request.UpdateViewerCountRequest;
import com.lifeshare.network.response.CommonResponse;
import com.lifeshare.network.response.CreateSessionResponse;
import com.lifeshare.network.response.MyConnectionListResponse;
import com.lifeshare.network.response.StreamUserResponse;
import com.lifeshare.permission.RuntimeEasyPermission;
import com.lifeshare.receiver.ForegroundService;
import com.lifeshare.ui.admin_user.ReportsUserListActivity;
import com.lifeshare.ui.invitation.MyInvitationListActivity;
import com.lifeshare.ui.my_connection.MyConnectionListActivity;
import com.lifeshare.ui.profile.ViewProfileActivity;
import com.lifeshare.ui.select_connection.SelectConnectionsActivity;
import com.lifeshare.ui.show_broadcast.MessageFragment;
import com.lifeshare.ui.show_broadcast.ShowStreamActivityNew;
import com.lifeshare.ui.show_broadcast.StreamUserListAdapter;
import com.lifeshare.ui.show_broadcast.ViewerListAdapter;
import com.lifeshare.utils.AlarmUtils;
import com.lifeshare.utils.Const;
import com.lifeshare.utils.PreferenceHelper;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.EasyPermissions;

import static com.lifeshare.utils.Const.GET_STREAM_USER_INTERVAL_TIME;
import static com.lifeshare.utils.Const.LAST_VIEW_UPDATE_INTERVAL_TIME;

public class BroadcastActivityNew extends BaseActivity
        implements Publisher.PublisherListener,
        Session.SessionListener, EasyPermissions.PermissionCallbacks, View.OnClickListener, RuntimeEasyPermission.PermissionCallbacks {

    private static final int MEDIA_PROJECTION_REQUEST_CODE = 1;
    private static final String TAG = "BroadcastActivity";
    private static final int RC_VIDEO_APP_PERM = 124;
    private static final int REQUEST_AUDIO_PERM = 1123;
    BubbleLayout bubbleView;
    TextView bubbleText;
    BubbleLayout bubbleLayout;
    ProgressBar bubbleProgressBar;
    CreateSessionResponse sessionData;
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
    private MediaProjectionManager projectionManager;
    private ImageReader imageReader;
    private MediaProjection mediaProjection;
    private Handler handler;
    private int displayWidth;
    private int displayHeight;
    private int imagesProduced;
    private boolean projectionStarted;
    private ImageView ivImage;
    private Session mSession;
    private Publisher mPublisher;
    private RelativeLayout mPublisherViewContainer;
    private RelativeLayout rlChatView;
    private TextView tvText;
    private RelativeLayout rlReceiver;
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
    private String[] permissions_audio = new String[]{Manifest.permission.RECORD_AUDIO};
    private Boolean isBroadcasting = false;
    private StreamUserListAdapter adapter;
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
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1234);
            } else {
                if (!isBubbleViewVisible) {
                    Log.v(TAG, "onResume:1 ");
                    bubblesManager = new BubblesManager.Builder(BroadcastActivityNew.this).setTrashLayout(R.layout.bubble_trash)
                            .build();

                    bubblesManager.initialize();
                }
            }
        } else {
            if (!isDestroyed()) {
                Log.v(TAG, "onResume:2 ");
                if (!isBubbleViewVisible) {
                    bubblesManager = new BubblesManager.Builder(BroadcastActivityNew.this).setTrashLayout(R.layout.bubble_trash)
                            .build();

                    bubblesManager.initialize();
                }
            }

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

    private void connectWithSession() {
        if (!checkInternetConnection()) {
            return;
        }
        showLoading(getString(R.string.waiting_for_connection_msg));
        WebAPIManager.getInstance().createSession(new RemoteCallback<CreateSessionResponse>() {
            @Override
            public void onSuccess(CreateSessionResponse response) {
                sessionData = response;
                PreferenceHelper.getInstance().setSessionData(sessionData);
                if (response != null
                        && !TextUtils.isEmpty(response.getSessionId())
                        && !TextUtils.isEmpty(response.getToken())) {
                    mSession = new Session.Builder(BroadcastActivityNew.this, response.getOpentokApiKeyDetail().getOpentokApiKey(), response.getSessionId()).build();
                    mSession.setSessionListener(BroadcastActivityNew.this);
                    mSession.connect(response.getToken());
                    createFirebaseData(response);
//                    switchCompat.setText(getResources().getString(R.string.stop));
                } else {
                    showToast(getString(R.string.message_invalid_session));
                    hideLoading();
                }

            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        setContentView(R.layout.activity_broadcast_new);

        initView();
        new InitTrueTimeAsyncTask().execute();
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name) + "(OFF)");
        getSupportFragmentManager().beginTransaction().add(R.id.container, messageFragment).commit();

        getSupportActionBar().hide();
        setStreamingConnection();

    }

    private void startForGroundService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Start Foreground Service");
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void stopForgroundService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }

    private void setStreamingConnection() {

        progressBarConnectionStreaming.setVisibility(View.VISIBLE);
        tvNoFriendStreaminig.setVisibility(View.GONE);
        rvFriendBroadcast.setVisibility(View.GONE);
        getCurrentStreamingConnection();
        startGetStreamTimer();

    }

    private void startGetStreamTimer() {
        countDownTimerGetStream = new CountDownTimer(Integer.MAX_VALUE, GET_STREAM_USER_INTERVAL_TIME) {

            @Override
            public void onTick(long l) {
                if (!isStreamUpdating) {
                    getCurrentStreamingConnection();
                }
            }

            @Override
            public void onFinish() {

            }
        };
        countDownTimerGetStream.start();
    }

    private void createFirebaseData(CreateSessionResponse response) {
        DatabaseReference databaseReference = LifeShare.getFirebaseReference().child(Const.TABLE_PUBLISHER).child(PreferenceHelper.getInstance().getUser().getUserId());
        databaseReference.removeValue();
        HashMap<String, String> startRequestMap = new HashMap<>();
        startRequestMap.put("sessionId", response.getSessionId());
        startRequestMap.put("sessionToken", response.getToken());
        startRequestMap.put("opentokId", response.getOpentokId());
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

    public void startProjection() {
        startActivityForResult(projectionManager.createScreenCaptureIntent(), MEDIA_PROJECTION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case MEDIA_PROJECTION_REQUEST_CODE:


                    mediaProjection = projectionManager.getMediaProjection(resultCode, data);
                    if (mediaProjection != null) {

                        projectionStarted = true;

                        // Initialize the media projection
                        DisplayMetrics metrics = getResources().getDisplayMetrics();
                        int density = metrics.densityDpi;
                        int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY
                                | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

                        Display display = getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);

                        displayWidth = size.x;
                        displayHeight = size.y;

                        imageReader = ImageReader.newInstance(displayWidth, displayHeight
                                , PixelFormat.RGBA_8888, 2);
                        mediaProjection.createVirtualDisplay("screencap",
                                displayWidth, displayHeight, density,
                                flags, imageReader.getSurface(), null, handler);
                        imageReader.setOnImageAvailableListener(new ImageAvailableListener(), handler);

                    }
                    if (checkInternetConnection()) {
                        connectWithSession();
                    } else {
//                        switchCompat.setChecked(false);
                        changeBroadcastButtonView();
                    }

                    break;
                case 1024:
                    ArrayList<MyConnectionListResponse> checkedItems = new ArrayList();
                    Bundle extras = data.getExtras();
                    checkedItems = extras.getParcelableArrayList(Const.SELECTED_USERS);
                    for (MyConnectionListResponse checkedItem : checkedItems) {
                        Log.e("Bhavy", checkedItem.getFirstName());
                    }
                    Toast.makeText(this, "Got Here", Toast.LENGTH_SHORT).show();
                    break;
            }

        } else {
            switch (requestCode) {
                case MEDIA_PROJECTION_REQUEST_CODE:
                    Toast.makeText(this, getString(R.string.screen_capture_permission_message), Toast.LENGTH_SHORT).show();
                    changeBroadcastButtonView();
     /*               if (switchCompat != null) {
                        switchCompat.setChecked(false);
                    }
     */
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

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Toast.makeText(this, opentokError.getMessage(), Toast.LENGTH_SHORT).show();
        removePublisherFromFirebase();
    }

    @Override
    public void onConnected(Session session) {
        if (BuildConfig.FLAVOR.equals("Dev")) {
            Toast.makeText(this, "onConnected", Toast.LENGTH_SHORT).show();
        }
        hideLoading();
        ScreensharingCapturer screenCapturer =
                new ScreensharingCapturer(BroadcastActivityNew.this, ivImage);

        mPublisher = new Publisher.Builder(BroadcastActivityNew.this)
                .name(PreferenceHelper.getInstance().getUser().getFirstName() + " " + PreferenceHelper.getInstance().getUser().getLastName())
                .capturer(screenCapturer)
                .frameRate(Publisher.CameraCaptureFrameRate.FPS_30)
                .build();
        mPublisher.setPublishAudio(true);
        mPublisher.setPublishVideo(true);
        mPublisher.setPublisherListener(this);

        mPublisherViewContainer.addView(mPublisher.getView());
        mSession.publish(mPublisher);

        isBroadcasting = true;

        notifyOther(sessionData.getOpentokId());
        changeBroadcastButtonView();
//        switchCompat.setText(getResources().getString(R.string.stop));
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
        Toast.makeText(this, getString(R.string.broadcasting), Toast.LENGTH_SHORT).show();

    }

    private void createBubblelayout() {
        bubbleView = (BubbleLayout) LayoutInflater
                .from(BroadcastActivityNew.this).inflate(R.layout.bubble_layout, null);
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
                    startBroadCast();

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
//                switchCompat.setEnabled(true);
//                switchCompat.setText(getResources().getString(R.string.start));
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
        if (mediaProjection != null) {
            mediaProjection.stop();
        }
        disconnectSession();
        countDownTimerGetStream.cancel();
        super.onDestroy();
    }

    @Override
    public void onDisconnected(Session session) {
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {

    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Toast.makeText(this, "onStreamDropped", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.v(TAG, "onError: " + opentokError.getMessage());
        Log.v(TAG, "onError: " + opentokError.getException());
        container.setVisibility(View.GONE);
        fabMessage.hide();
//        switchCompat.setChecked(false);
        removePublisherFromFirebase();
    }

    private void disconnectSession() {
        if (mSession == null) {
            return;
        }

        if (mPublisher != null) {
            mPublisherViewContainer.removeView(mPublisher.getView());
            mSession.unpublish(mPublisher);
            mPublisher.destroy();
            mPublisher = null;
        }
        mSession.disconnect();
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
                startActivityForResult(new Intent(this, SelectConnectionsActivity.class), 1024);
                /*if (!isBroadcasting) {
                    startBroadCast();
                } else {
                    stopBroadcast();
                }*/

                break;
            case R.id.iv_more:
                showDialog();
                break;
        }
    }


    private void stopBroadcast() {
        stopForgroundService();
        playAudio(this, R.raw.dingdong);

        Log.v(TAG, "onCheckedChanged: false ");
        fabMessage.hide();
        rlChatView.setVisibility(View.GONE);
        container.setVisibility(View.GONE);
        deleteStreaming();
    }

    private void startBroadCast() {
        startForGroundService();
        playAudio(this, R.raw.jingle_two);

        RuntimeEasyPermission.newInstance(permissions_audio,
                REQUEST_AUDIO_PERM, "Allow microphone permission").show(getSupportFragmentManager());

    }

    public void deleteStreaming() {
        if (sessionData == null) {
            return;
        }
        showLoading();
        DeleteStreamingRequest request = new DeleteStreamingRequest();
        request.setOpentokId(sessionData.getOpentokId());
        WebAPIManager.getInstance().deleteStreaming(request, new RemoteCallback<CommonResponse>(this) {
            @Override
            public void onSuccess(CommonResponse response) {
                PreferenceHelper.getInstance().setSessionData(null);
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
        ivImage = (ImageView) findViewById(R.id.iv_image);
        tvText = (TextView) findViewById(R.id.tv_text);
        rlReceiver = (RelativeLayout) findViewById(R.id.rl_receiver);
        mPublisherViewContainer = (RelativeLayout) findViewById(R.id.publisherview);
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
        adapter = new StreamUserListAdapter(new BaseRecyclerListener<StreamUserResponse>() {
            @Override
            public void showEmptyDataView(int resId) {


            }

            @Override
            public void onRecyclerItemClick(View view, int position, StreamUserResponse item) {
                if (!item.getOpentokId().isEmpty()) {
                    LifeShare.getInstance().clearNotificationById(Integer.parseInt(item.getOpentokId()));
                }
                playAudio(BroadcastActivityNew.this, R.raw.click);
                Intent intent = new Intent(BroadcastActivityNew.this, ShowStreamActivityNew.class);
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

        Dialog dialog = new Dialog(BroadcastActivityNew.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
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
                    startActivity(new Intent(BroadcastActivityNew.this, ReportsUserListActivity.class));
                    dialog.dismiss();
                }
            }
        });
        tvInvitations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BroadcastActivityNew.this, MyInvitationListActivity.class));
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
                otherDialog(BroadcastActivityNew.this, getResources().getString(R.string.logout_message), getResources().getString(R.string.yes), getResources().getString(R.string.no), new DismissListenerWithStatus() {
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
                startActivity(new Intent(BroadcastActivityNew.this, MyConnectionListActivity.class));
                dialog.dismiss();
            }
        });

        tvProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(BroadcastActivityNew.this, ViewProfileActivity.class);
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

    private void getCurrentStreamingConnection() {
        isStreamUpdating = true;
        WebAPIManager.getInstance().getCurrentConnectionStreaming(new RemoteCallback<ArrayList<StreamUserResponse>>(this) {
            @Override
            public void onSuccess(ArrayList<StreamUserResponse> response) {
                isStreamUpdating = false;
                progressBarConnectionStreaming.setVisibility(View.GONE);
                ArrayList<StreamUserResponse> responseArrayList = new ArrayList<>();
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
            if (!projectionStarted) {
                projectionManager = (MediaProjectionManager)
                        getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                startProjection();
            } else {

                if (checkInternetConnection()) {
                    connectWithSession();

                } else {
                    stopBroadcast();
//                switchCompat.setChecked(false);
                }
            }
        }
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

    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = null;
            FileOutputStream fos = null;
            Bitmap bitmap = null;

            ByteArrayOutputStream stream = null;

            try {
                image = imageReader.acquireLatestImage();
                if (image != null) {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * displayWidth;

                    // create bitmap
                    bitmap = Bitmap.createBitmap(displayWidth + rowPadding / pixelStride,
                            displayHeight, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);
                    stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 5, stream);

//                    Log.v(TAG, "onImageAvailable: " + stream.toByteArray());
                    byte[] data = stream.toByteArray();
                    if (data != null && data.length != 0) {
                        Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
//                        Log.d(TAG, "Set Image : " + bm.toString());
                        ivImage.setImageBitmap(bm);
                    }


                    imagesProduced++;
//                    Log.e(TAG, "captured image: " + imagesProduced);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }

                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }

                if (bitmap != null) {
                    bitmap.recycle();
                }

                if (image != null) {
                    image.close();
                }
            }
        }
    }
}
