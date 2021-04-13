package com.lifeshare.ui.save_broadcast;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lifeshare.BaseActivity;
import com.lifeshare.R;
import com.lifeshare.network.response.ChannelArchiveResponse;
import com.lifeshare.utils.Const;
import com.lifeshare.utils.PlayerManager;

public class ShowPreviousBroadcastAndChatActivity extends BaseActivity implements View.OnClickListener {


    //client ID: 186625114995-ukmqg68sjvgt5hqilao3ff9hj90tl28b.apps.googleusercontent.com
    // secret key: WW81KSd_kcjS-LQZcOozGixI
    private static final String TAG = "ShowPreviousBroadcastAn";
    PreviousChatMessageFragment messageFragment;
    PlayerManager playerManager;
    private FloatingActionButton fabMessage;
    private FrameLayout container;
    private ChannelArchiveResponse channelArchiveResponse;
    private ExoPlayer exoPlayer;
    private PlayerView exoplayer;
    private AppCompatImageView ivShareVideo, icBack;
    private LinearLayout llToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_previous_broadcast_and_chat);
        initView();
        if (getIntent() != null && getIntent().getExtras() != null) {
            channelArchiveResponse = getIntent().getExtras().getParcelable(Const.CHANNAL_DATA);

            messageFragment = PreviousChatMessageFragment.newInstance(channelArchiveResponse.getReferenceId());
            getSupportFragmentManager().beginTransaction().add(R.id.container, messageFragment).commit();
            if (channelArchiveResponse.getVideo_url().isEmpty()) {
                container.setVisibility(View.VISIBLE);
                fabMessage.hide();
            } else {
                llToolbar.setVisibility(View.VISIBLE);
                setVideoView();
            }
        }
    }

    private void setVideoView() {
        playerManager = new PlayerManager(this);
        Log.v(TAG, "setVideoView: " + channelArchiveResponse.getVideo_url());
        playerManager.init(this, exoplayer, channelArchiveResponse.getVideo_url());
//        playerManager.init(this, exoplayer, "https://lifeshare-data.s3.ap-south-1.amazonaws.com/public/uploads/channels/video/RMda48cadfd89702306dd6cf7892deea11/CJ48a4dd2e731acce7d45a6f011070cc2e.mp4");
//            playerManager.init(this, exoplayer, "https://lifeshare-data.s3.ap-south-1.amazonaws.com/public/uploads/channels/video/RMf84e6af522ee6eb2348ec10816c33ec6/RTf70429ffb9ea0006792d47f2d732811a.mkv");
        playerManager.getPlayerManagerNew().addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
                Log.v(TAG, "onTimelineChanged: ");
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                Log.v(TAG, "onTracksChanged: ");
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                Log.v(TAG, "onLoadingChanged: ");
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.v(TAG, "onPlayerStateChanged: ");
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                Log.v(TAG, "onRepeatModeChanged: ");
            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                Log.v(TAG, "onShuffleModeEnabledChanged: ");
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.v(TAG, "onPlayerError: ");
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                Log.v(TAG, "onPositionDiscontinuity: ");
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                Log.v(TAG, "onPlaybackParametersChanged: ");
            }

            @Override
            public void onSeekProcessed() {

            }
        });

    }

    private void initView() {

        fabMessage = (FloatingActionButton) findViewById(R.id.fabMessage);

        fabMessage.setOnClickListener(this);

        container = (FrameLayout) findViewById(R.id.container);
        llToolbar = (LinearLayout) findViewById(R.id.ll_toolbar);

        exoplayer = (PlayerView) findViewById(R.id.exoplayer);
        ivShareVideo = findViewById(R.id.iv_share_video);
        icBack = findViewById(R.id.ic_back);
        ivShareVideo.setOnClickListener(this);
        icBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fabMessage:
                if (container.getVisibility() == View.VISIBLE) {
                    container.setVisibility(View.GONE);
                    playerManager.resumePlaying();
                } else {
                    playerManager.paushPlaying();
                    container.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.iv_share_video:
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_video_message) + channelArchiveResponse.getVideo_url().trim());
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_video)));
                } catch (Exception e) {
                }
                break;
            case R.id.ic_back:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onBackPressed() {

        if (container.getVisibility() == View.VISIBLE && !channelArchiveResponse.getVideo_url().trim().isEmpty()) {
            container.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playerManager != null) {
            playerManager.reset();
            playerManager = null;
        }
    }
}
