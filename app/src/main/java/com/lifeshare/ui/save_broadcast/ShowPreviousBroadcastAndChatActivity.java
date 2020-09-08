package com.lifeshare.ui.save_broadcast;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lifeshare.BaseActivity;
import com.lifeshare.R;

public class ShowPreviousBroadcastAndChatActivity extends BaseActivity implements View.OnClickListener {

    PreviousChatMessageFragment messageFragment;
    private VideoView videoView;
    private FloatingActionButton fabMessage;
    private FrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_previous_broadcast_and_chat);
        initView();
    }

    private void initView() {
        videoView = (VideoView) findViewById(R.id.videoView);
        fabMessage = (FloatingActionButton) findViewById(R.id.fabMessage);

        videoView.setVideoPath("https://www.radiantmediaplayer.com/media/big-buck-bunny-360p.mp4");
        videoView.start();
        MediaController ctlr = new MediaController(this);
        ctlr.setMediaPlayer(videoView);
        videoView.setMediaController(ctlr);
        videoView.requestFocus();
        fabMessage.setOnClickListener(this);

        messageFragment = PreviousChatMessageFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.container, messageFragment).commit();

        container = (FrameLayout) findViewById(R.id.container);
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
        }
    }
}
