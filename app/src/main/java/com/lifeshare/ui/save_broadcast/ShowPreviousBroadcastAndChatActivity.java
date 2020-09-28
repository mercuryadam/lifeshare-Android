package com.lifeshare.ui.save_broadcast;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

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

    private static final String TAG = "ShowPreviousBroadcastAn";
    PreviousChatMessageFragment messageFragment;
    PlayerManager playerManager;
    private FloatingActionButton fabMessage;
    private FrameLayout container;
    private ChannelArchiveResponse channelArchiveResponse;
    private ExoPlayer exoPlayer;
    private PlayerView exoplayer;

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
                setVideoView();
            }
        }
    }

    private void setVideoView() {
        playerManager = new PlayerManager(this);
        Log.v(TAG, "setVideoView: " + channelArchiveResponse.getVideo_url());
        playerManager.init(this, exoplayer, "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
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

        exoplayer = (PlayerView) findViewById(R.id.exoplayer);
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
        playerManager.reset();
        playerManager = null;
    }
}
