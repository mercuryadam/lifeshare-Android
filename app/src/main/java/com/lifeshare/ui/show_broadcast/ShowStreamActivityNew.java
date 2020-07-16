package com.lifeshare.ui.show_broadcast;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.lifeshare.utils.Const.LAST_VIEW_UPDATE_INTERVAL_TIME;

public class ShowStreamActivityNew extends BaseActivity implements Session.SessionListener, Publisher.PublisherListener, View.OnClickListener {

    private static final String TAG = "ShowStreamActivity";
    CountDownTimer countDownTimerViewerLastTime;
    MessageFragment messageFragment;
    DatabaseReference viewerDatabaseReference;
    private Subscriber mSubscriber;
    private Session mSession;
    private RelativeLayout rlReceiver;
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


    private void initView() {
        Log.v(TAG, "initView: " + LifeShare.getFirebaseReference().push().getKey());
        tvToolbarTitle = (AppCompatTextView) findViewById(R.id.tvToolbarTitle);
        rvViewer = findViewById(R.id.rv_viewer);

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
        setContentView(R.layout.activity_show_stream_new);
        initView();
        new InitTrueTimeAsyncTask().execute();
        if (getIntent() != null && getIntent().getExtras() != null) {
            currentVisibleStram = getIntent().getExtras().getParcelable(Const.STREAM_DATA);

            connectSession(currentVisibleStram.getSessionId(), currentVisibleStram.getToken());
            messageFragment.setCurrentStream(currentVisibleStram.getUserId());

            tvToolbarTitle.setText(currentVisibleStram.getChannelName());

            Glide.with(LifeShare.getInstance())
                    .load(currentVisibleStram.getAvatar())
                    .apply(new RequestOptions().error(R.drawable.user_placeholder).placeholder(R.drawable.user_placeholder))
                    .into(ivProfile);


        }

    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.v(TAG, "onStreamReceived: " + stream.getName());
        addViewerToStream();

        llStreamProgress.setVisibility(View.GONE);
        tvToolbarTitle.setText(stream.getName());
        rlReceiver.setVisibility(View.VISIBLE);
        fabMessage.show();
//        if (mSubscriber == null) {
        Log.v(TAG, "onStreamReceived: in");
        mSubscriber = new Subscriber.Builder(this, stream).build();
        mSession.subscribe(mSubscriber);
        rlReceiver.addView(mSubscriber.getView());
//        }
    }

    private void connectSession(String sessionId, String token) {

        showLoading();
        llStreamProgress.setVisibility(View.VISIBLE);
        streamProgressBar.setVisibility(View.VISIBLE);
        tvStreamMessage.setText(getString(R.string.waiting_for_connection_msg));
        mSession = new Session.Builder(ShowStreamActivityNew.this, currentVisibleStram.getOpentokApiKeyDetail().getOpentokApiKey(), sessionId).build();
        mSession.setSessionListener(this);
        mSession.connect(token);

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
        super.onBackPressed();
        removeUserFromViewer();
    }

    @Override
    public void onConnected(Session session) {
        hideLoading();
        Log.v(TAG, "onConnected: " + session.getConnection().getData());
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.v(TAG, "onError: " + opentokError.getErrorCode() + " - " + opentokError.getMessage());
        if (opentokError.getErrorCode() == OpentokError.ErrorCode.ConnectionDropped
                || opentokError.getErrorCode() == OpentokError.ErrorCode.InvalidSessionId
                || opentokError.getErrorCode() == OpentokError.ErrorCode.AuthorizationFailure) {
            deleteStreamUser(currentVisibleStram.getUserId());
        }

    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.v(TAG, "onStreamCreated: ");
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.v(TAG, "onStreamDestroyed: ");
    }


    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.v(TAG, "onError: ");


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

    @Override
    public void onDisconnected(Session session) {
        Log.v(TAG, "onDisconnected: ");
//        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();

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
    }


    @Override
    public void onStreamDropped(Session session, Stream stream) {
//        Toast.makeText(this, "onStreamDropped", Toast.LENGTH_SHORT).show();
        Log.v(TAG, "onStreamDropped: SessionId - " + session.getSessionId());
        deleteStreamUser(currentVisibleStram.getUserId());
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
}
