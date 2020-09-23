package com.lifeshare.ui.save_broadcast;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lifeshare.BaseActivity;
import com.lifeshare.R;
import com.lifeshare.network.response.ChannelArchiveResponse;
import com.lifeshare.utils.Const;

public class ShowPreviousBroadcastAndChatActivity extends BaseActivity implements View.OnClickListener {

    PreviousChatMessageFragment messageFragment;
    private VideoView videoView;
    private FloatingActionButton fabMessage;
    private FrameLayout container;
    private ChannelArchiveResponse channelArchiveResponse;
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

    private static final String TAG = "ShowPreviousBroadcastAn";
    private void setVideoView() {

        try {

        MediaController mediaController = new MediaController(this);
//        mediaController.setMediaPlayer(videoView);
            mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
//        videoView.setVideoURI(Uri.parse("https://upload-poc-demo.s3.ap-south-1.amazonaws.com/public/uploads/channels/video/RM4650f18d921c5930c24b802314ec58c7/RTb5515e1dbbc675e9f2b53bcb23549602.mp4"));
            videoView.setVideoURI(Uri.parse("https://lifeshare-data.s3.ap-south-1.amazonaws.com/public/test.mp4"));
            videoView.start();
        videoView.requestFocus();
        showLoading();
        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
                if (i == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    hideLoading();
                }
                return false;
            }
        });
        } catch (Exception e) {
            Log.v(TAG, "setVideoView: " + e);
        }
    }

    private void initView() {
        videoView = (VideoView) findViewById(R.id.videoView);
        fabMessage = (FloatingActionButton) findViewById(R.id.fabMessage);

        fabMessage.setOnClickListener(this);

        container = (FrameLayout) findViewById(R.id.container);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fabMessage:
                if (container.getVisibility() == View.VISIBLE) {
                    container.setVisibility(View.GONE);
                } else {
                    videoView.pause();
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
}
