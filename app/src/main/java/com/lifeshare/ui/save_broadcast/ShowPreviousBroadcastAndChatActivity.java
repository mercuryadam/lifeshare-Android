package com.lifeshare.ui.save_broadcast;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_previous_broadcast_and_chat);
        initView();
        if (getIntent() != null && getIntent().getExtras() != null) {
            ChannelArchiveResponse response = getIntent().getExtras().getParcelable(Const.CHANNAL_DATA);

            messageFragment = PreviousChatMessageFragment.newInstance(response.getReferenceId());
            getSupportFragmentManager().beginTransaction().add(R.id.container, messageFragment).commit();
        }
    }

    private void initView() {
        videoView = (VideoView) findViewById(R.id.videoView);
        fabMessage = (FloatingActionButton) findViewById(R.id.fabMessage);

   /*     videoView.setVideoPath("https://www.radiantmediaplayer.com/media/big-buck-bunny-360p.mp4");
        videoView.start();
        MediaController ctlr = new MediaController(this);
        ctlr.setMediaPlayer(videoView);
        videoView.setMediaController(ctlr);
        videoView.requestFocus();
   */
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
                    container.setVisibility(View.VISIBLE);
                }
                break;
        }
    }
}
