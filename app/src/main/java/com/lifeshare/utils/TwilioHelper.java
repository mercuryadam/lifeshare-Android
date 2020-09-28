package com.lifeshare.utils;

import android.preference.PreferenceManager;

import com.lifeshare.LifeShare;
import com.twilio.video.AudioCodec;
import com.twilio.video.EncodingParameters;
import com.twilio.video.G722Codec;
import com.twilio.video.H264Codec;
import com.twilio.video.IsacCodec;
import com.twilio.video.OpusCodec;
import com.twilio.video.PcmaCodec;
import com.twilio.video.PcmuCodec;
import com.twilio.video.VideoCodec;
import com.twilio.video.Vp8Codec;
import com.twilio.video.Vp9Codec;

public class TwilioHelper {

    public static final String PREF_AUDIO_CODEC = "audio_codec";
    public static final String PREF_AUDIO_CODEC_DEFAULT = OpusCodec.NAME;
    public static final String PREF_VIDEO_CODEC = "video_codec";
    //    public static final String PREF_VIDEO_CODEC_DEFAULT = Vp8Codec.NAME;
    public static final String PREF_VIDEO_CODEC_DEFAULT = Vp9Codec.NAME;
    public static final String PREF_SENDER_MAX_AUDIO_BITRATE = "sender_max_audio_bitrate";
    public static final String PREF_SENDER_MAX_AUDIO_BITRATE_DEFAULT = "0";
    public static final String PREF_SENDER_MAX_VIDEO_BITRATE = "sender_max_video_bitrate";
    public static final String PREF_SENDER_MAX_VIDEO_BITRATE_DEFAULT = "0";
    public static final String PREF_VP8_SIMULCAST = "vp8_simulcast";
    public static final String PREF_ENABLE_AUTOMATIC_SUBSCRIPTION = "enable_automatic_subscription";
    public static final boolean PREF_ENABLE_AUTOMATIC_SUBSCRIPTION_DEFAULT = true;
    public static final boolean PREF_VP8_SIMULCAST_DEFAULT = false;

    public static TwilioHelper INSTANCE;

    public static TwilioHelper getInstance() {
        if (INSTANCE == null)
            INSTANCE = new TwilioHelper();
        return INSTANCE;
    }

    public AudioCodec getAudioCodecPreference(String key, String defaultValue) {
        final String audioCodecName = PreferenceManager.getDefaultSharedPreferences(LifeShare.getInstance()).getString(key, defaultValue);

        switch (audioCodecName) {
            case IsacCodec.NAME:
                return new IsacCodec();
            case OpusCodec.NAME:
                return new OpusCodec();
            case PcmaCodec.NAME:
                return new PcmaCodec();
            case PcmuCodec.NAME:
                return new PcmuCodec();
            case G722Codec.NAME:
                return new G722Codec();
            default:
                return new OpusCodec();
        }
    }

    /*
     * Get the preferred video codec from shared preferences
     */
    public VideoCodec getVideoCodecPreference(String key, String defaultValue) {
        final String videoCodecName = PreferenceManager.getDefaultSharedPreferences(LifeShare.getInstance()).getString(key, defaultValue);

        switch (videoCodecName) {
            case Vp8Codec.NAME:
                boolean simulcast = PreferenceManager.getDefaultSharedPreferences(LifeShare.getInstance()).getBoolean(PREF_VP8_SIMULCAST,
                        PREF_VP8_SIMULCAST_DEFAULT);
                return new Vp8Codec(simulcast);
            case H264Codec.NAME:
                return new H264Codec();
            case Vp9Codec.NAME:
                return new Vp9Codec();
            default:
                return new Vp8Codec();
        }
    }

    public boolean getAutomaticSubscriptionPreference(String key, boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(LifeShare.getInstance()).getBoolean(key, defaultValue);
    }

    public EncodingParameters getEncodingParameters() {
        final int maxAudioBitrate = Integer.parseInt(
                PreferenceManager.getDefaultSharedPreferences(LifeShare.getInstance()).getString(PREF_SENDER_MAX_AUDIO_BITRATE,
                        PREF_SENDER_MAX_AUDIO_BITRATE_DEFAULT));
        final int maxVideoBitrate = Integer.parseInt(
                PreferenceManager.getDefaultSharedPreferences(LifeShare.getInstance()).getString(PREF_SENDER_MAX_VIDEO_BITRATE,
                        PREF_SENDER_MAX_VIDEO_BITRATE_DEFAULT));

        return new EncodingParameters(maxAudioBitrate, maxVideoBitrate);
    }


}
