package com.lifeshare.utils;

import com.lifeshare.BuildConfig;

public class Const {
    public static final String NEW_INVITATION = "NEW_INVITATION";
    public static final String NEW_INVITATION_ACTION = "NEW_INVITATION_ACTION";
    public static final String INVITATION_ACCEPT = "INVITATION_ACCEPT";
    public static final String INVITATION_ACCEPT_ACTION = "INVITATION_ACCEPT_ACTION";
    public static final String STREAM_STARTED = "STREAM_STARTED";
    public static final String MY_PROFILE = "MY_PROFILE";
    public static final String PROFILE = "PROFILE";
    public static final String STREAM_DATA = "STREAM_DATA";
    public static final String USER_DATA = "USER_DATA";
    public static final String USER_ID = "USER_ID";
    public static final String OTHER_PROFILE = "OTHER_PROFILE";
    public static final String STRAM_OBJECT = "STRAM_OBJECT";
    // firebase table name
    public static final String TABLE_PUBLISHER = "PUBLISHER_" + BuildConfig.FLAVOR;
    public static final String TABLE_VIEWER = "VIEWERS_" + BuildConfig.FLAVOR;
    public static final String TABLE_CHAT_MESSAGE = "CHAT_MESSAGE_" + BuildConfig.FLAVOR;
    public static final String TABLE_COUNT_VIEWER = "COUNT_VIEWER_" + BuildConfig.FLAVOR;

    //
    public static final int GET_STREAM_USER_INTERVAL_TIME = 10000; // in millisecond
    public static final int LAST_VIEW_UPDATE_INTERVAL_TIME = 60000 * 1; // in millisecond

    public static Const INSTANCE;

    public static Const getInstance() {
        if (INSTANCE == null)
            INSTANCE = new Const();
        return INSTANCE;
    }
}
