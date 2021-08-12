package com.lifeshare.utils;

import com.lifeshare.BuildConfig;

public class Const {
    public static final String FACEBOOK_APP_ID = "463658245014536";
    public static final String DEVICE_TYPE = "Android";
    public static final String UP_TO_DATE = "1";
    public static final String APP_UPDATE = "2";
    public static final String FORCE_UPDATE = "3";
    public static final String DEFAULT_LOG_IN = "1";
    public static final String FB_LOG_IN = "2";
    public static final String GOOGLE_LOG_IN = "3";
    public static final String INSTAGRAM_LOG_IN = "4";
    public static final String IS_FROM = "IS_FROM";
    public static final String TERM_AND_CONDITION_SCREEN = "TERM_AND_CONDITION_SCREEN";
    public static final String NEW_INVITATION = "NEW_INVITATION";
    public static final String NEW_INVITATION_ACTION = "NEW_INVITATION_ACTION";
    public static final String INVITATION_ACCEPT = "INVITATION_ACCEPT";
    public static final String LIKE_COMMENT = "LIKE_COMMENT";
    public static final String LOVE_COMMENT = "LOVE_COMMENT";
    public static final String NEW_COMMENT = "NEW_COMMENT";
    public static final String COMMENT_ADD_LIKE_LOVE = "COMMENT_ADD_LIKE_LOVE";
    public static final String INVITATION_ACCEPT_ACTION = "INVITATION_ACCEPT_ACTION";
    public static final String STREAM_STARTED = "STREAM_STARTED";
    public static final String MY_PROFILE = "MY_PROFILE";
    public static final String PROFILE = "PROFILE";
    public static final String STREAM_DATA = "STREAM_DATA";
    public static final String USER_DATA = "USER_DATA";
    public static final String USER_ID = "USER_ID";
    public static final String OTHER_PROFILE = "OTHER_PROFILE";
    public static final String STRAM_OBJECT = "STRAM_OBJECT";
    public static final String SELECTED_USERS = "SELECTED_USERS";
    public static final String CHANNAL_DATA = "CHANNAL_DATA";
    // firebase table name
    public static final String TABLE_PURCHASE = "PURCHASE" + BuildConfig.FLAVOR;
    public static final String TABLE_PUBLISHER = "PUBLISHER_" + BuildConfig.FLAVOR;
    public static final String TABLE_VIEWER = "VIEWERS_" + BuildConfig.FLAVOR;
    public static final String TABLE_CHAT_MESSAGE = "CHAT_MESSAGE_" + BuildConfig.FLAVOR;
    public static final String TABLE_COUNT_VIEWER = "COUNT_VIEWER_" + BuildConfig.FLAVOR;
    public static final String FROM_NOTIFICATION = "FROM_NOTIFICATION";
    // do not change this
    public static final String LIFESHARE_LIVE_MONTHLY_SUBSCRIPTION_ID_1 = "lifeshare_live_monthly_subscription_id_1";
//    public static final String LIFESHARE_LIVE_MONTHLY_SUBSCRIPTION_ID_1 = "test_yearly_subsciption";

    public static final String TEST_SUBSCRIPTION_ID = "test_id_monthly_1";

    //
    public static final int GET_STREAM_USER_INTERVAL_TIME = 10000; // in millisecond
    public static final int LAST_VIEW_UPDATE_INTERVAL_TIME = 60000 * 1; // in millisecond

    public static final int VIEW_TYPE_DATA = 1; // in millisecond
    public static final int VIEW_TYPE_PROGRESS = 0; // in millisecond
    public static final String UPDATE_CHANNEL_ARCHIVE = "UPDATE_CHANNEL_ARCHIVE";
    public static final String PHOTO = "Photo";
    public static final String LINK = "Link";


    public static Const INSTANCE;

    public static Const getInstance() {
        if (INSTANCE == null)
            INSTANCE = new Const();
        return INSTANCE;
    }
}
