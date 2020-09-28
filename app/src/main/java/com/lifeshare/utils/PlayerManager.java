package com.lifeshare.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;
import com.lifeshare.R;

public class PlayerManager implements BandwidthMeter {

    private static final String TAG = "PlayerManager";
    private DataSource.Factory dataSourceFactory;
    private SimpleExoPlayer player;
    private long contentPosition;
    private MediaSource contentMediaSource;

    public PlayerManager(Context context) {
        dataSourceFactory =
                new DefaultDataSourceFactory(
                        context, Util.getUserAgent(context, context.getString(R.string.app_name)));
        Log.v(TAG, "PlayerManager: " + dataSourceFactory);
    }

    public void init(Context context, PlayerView playerView, String url) {
        // Create a default track selector.
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(new DefaultBandwidthMeter());
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        DefaultLoadControl loadControl = new DefaultLoadControl(
                new DefaultAllocator(true, 16),
                VideoPlayerConfig.MIN_BUFFER_DURATION,
                VideoPlayerConfig.MAX_BUFFER_DURATION,
                VideoPlayerConfig.MIN_PLAYBACK_START_BUFFER,
                VideoPlayerConfig.MIN_PLAYBACK_RESUME_BUFFER, -1, true);
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl);

        // Create a player instance.

//        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        // Bind the player to the view.
        playerView.setPlayer(player);
        player.setVolume(1f);
        // This is the MediaSource representing the content media (i.e. not the ad).
        contentMediaSource = buildMediaSource(Uri.parse(url));


        // Prepare the player with the source.
        player.prepare(contentMediaSource);
        resetAndPlay();
    }

    public SimpleExoPlayer getPlayerManagerNew() {
        if (player != null) {
            Log.v(TAG, "getPlayerManagerNew: "
                    + player);
            return player;
        } else {
            return null;
        }
    }

    public void reset() {
        if (player != null) {
            contentPosition = player.getContentPosition();
            player.seekTo(0);
            player.release();
            player = null;
        }
    }

    public void release() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    public void setSound(boolean sound) {
        Log.v(TAG, "setSound 1: ");
        if (player != null) {
            Log.v(TAG, "setSound 2: " + sound);
            if (sound) {
                player.setVolume(10);
            } else {
                player.setVolume(0f);
            }
        }
    }

    // Internal methods.

    private MediaSource buildMediaSource(Uri uri) {
        @C.ContentType int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
/*

                DataSource.Factory manifestDataSourceFactory =
                        new DefaultHttpDataSourceFactory("ua");
                DashChunkSource.Factory dashChunkSourceFactory =
                        new DefaultDashChunkSource.Factory(
                                new DefaultHttpDataSourceFactory("ua",  new DefaultBandwidthMeter()));
                return new DashMediaSource.Factory(dashChunkSourceFactory,
                        manifestDataSourceFactory).createMediaSource(uri);
*/
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    public void resetAndPlay() {
        if (player != null) {
            player.seekTo(0);
            player.setPlayWhenReady(true);

        }
    }

    public void resetAndStop() {
        try {
            if (player != null) {
                player.seekTo(0);
                player.setPlayWhenReady(false);
            }

        } catch (Exception e) {
            Log.v(TAG, "resetAndStop: ");
        }
    }

    public void paushPlaying() {
        try {
            if (player != null) {
                Log.v(TAG, "stopCurentPlaying: ");
                player.stop();
            }
        } catch (Exception e) {
            Log.v(TAG, "paushPlaying: ");
        }
    }

    public void resumePlaying() {
        if (player != null) {
            if (player.getContentPosition() > 0) {
                Log.v(TAG, "resumePlaying: if");
                player.seekTo(player.getContentPosition());
                player.setPlayWhenReady(true);
                player.retry();
            } else {
//                player.prepare(contentMediaSource);
                Log.v(TAG, "resumePlaying: else");
                player.seekTo(player.getContentPosition());
                player.setPlayWhenReady(true);
            }
        }
    }

    public void resumePlaying(int contentPosition) {
        if (player != null) {
            if (contentPosition > 0) {
                Log.v(TAG, "resumePlaying: if");
                player.seekTo(contentPosition);
                player.setPlayWhenReady(true);
                player.retry();
            } else {
                Log.v(TAG, "resumePlaying: else");
                player.seekTo(player.getContentPosition());
                player.setPlayWhenReady(true);
            }
        }
    }

    @Override
    public long getBitrateEstimate() {
        return 0;
    }

    @Nullable
    @Override
    public TransferListener getTransferListener() {
        return null;
    }

    @Override
    public void addEventListener(Handler eventHandler, EventListener eventListener) {

    }

    @Override
    public void removeEventListener(EventListener eventListener) {

    }

    public class VideoPlayerConfig {

        //Minimum Video you want to buffer while Playing
        public static final int
                MIN_BUFFER_DURATION = 6000;
        //Max Video you want to buffer during PlayBack
        public static final int MAX_BUFFER_DURATION = 50000;
        //Min Video you want to buffer before getIntent Playing it
        public static final int MIN_PLAYBACK_START_BUFFER = 1500;
        //Min video You want to buffer when user resumes video
        public static final int MIN_PLAYBACK_RESUME_BUFFER = 5000;

    }

}

