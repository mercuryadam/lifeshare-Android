package com.lifeshare.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.instacart.library.truetime.TrueTime;
import com.lifeshare.BaseActivity;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.receiver.StreamingIntentService;
import com.lifeshare.utils.PreferenceHelper;

import java.io.IOException;
import java.util.Date;

public class SplashActivity extends BaseActivity {

    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            moveToNextScreen();
        }
    };
    Handler handler = new Handler();
    private String TAG = SplashActivity.class.getSimpleName();
    private long SPLASH_TIME_OUT = 2000;

    private void moveToNextScreen() {
//        new InitTrueTimeAsyncTask().execute();
        if (PreferenceHelper.getInstance().getUser() == null) {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        } else {
            if (PreferenceHelper.getInstance().getIsAcceptTermOfService()) {
                deleteStreamingIfAvailable();
                startActivity(new Intent(SplashActivity.this, TwilioBroadcastActivityNew.class));
            } else {
                startActivity(new Intent(SplashActivity.this, TermOfServicesActivity.class));
            }
        }
        finish();
    }

    private void deleteStreamingIfAvailable() {
        if (checkInternetConnection()) {
            Intent intent = new Intent(this, StreamingIntentService.class);
            startService(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new InitTrueTimeAsyncTask().execute();
/*
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    */
        setContentView(R.layout.activity_splash);

    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable, SPLASH_TIME_OUT);

    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    private class InitTrueTimeAsyncTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... params) {
            try {
                TrueTime.build()
                        //.withSharedPreferences(SampleActivity.this)
                        .withNtpHost("time.google.com")
                        .withLoggingEnabled(false)
                        .withSharedPreferencesCache(LifeShare.getInstance())
                        .withConnectionTimeout(3_1428)
                        .initialize();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (TrueTime.isInitialized()) {
                Date time = TrueTime.now();
                Log.v(TAG, "onPostExecute :" + time + " : " + time.getTime());
            } else {
                Log.v(TAG, "TrueTime not initialized");
            }

        }
    }
}
