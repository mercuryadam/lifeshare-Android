package com.lifeshare.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.instacart.library.truetime.TrueTime;
import com.lifeshare.BaseActivity;
import com.lifeshare.BuildConfig;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.CheckVersionRequest;
import com.lifeshare.network.response.CheckVersionResponse;
import com.lifeshare.receiver.StreamingIntentService;
import com.lifeshare.utils.Const;
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
        if (!PreferenceHelper.getInstance().getIsLogIn()) {
            PreferenceHelper.getInstance().setUser(null);
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        } else {
            if (PreferenceHelper.getInstance().getIsAcceptTermOfService()) {
                deleteStreamingIfAvailable();
                startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
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
        checkVersion();

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

    private void checkVersion() {
        if (!checkInternetConnection()) {
            return;
        }
        showLoading();
        CheckVersionRequest request = new CheckVersionRequest();
        request.setVersion(BuildConfig.VERSION_NAME);
        request.setDeviceType(Const.DEVICE_TYPE);

        WebAPIManager.getInstance().checkVersion(request, new RemoteCallback<CheckVersionResponse>(this) {
            @Override
            public void onSuccess(CheckVersionResponse response) {
                hideLoading();

                if (response.getStatus().equals(Const.UP_TO_DATE)) {
                    handler.postDelayed(runnable, SPLASH_TIME_OUT);
                } else
                    openUpdateDialog(response.getMessage(), response.getStatus().equals(Const.FORCE_UPDATE));
            }
        });
    }

    private void openUpdateDialog(String message, Boolean forceUpdate) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.app_name))
                .setMessage(message)
                .setPositiveButton(getString(R.string.update), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                        dialogInterface.dismiss();
                    }
                });

        if (!forceUpdate) {
            dialog.setNegativeButton(getString(R.string.skip), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    handler.postDelayed(runnable, SPLASH_TIME_OUT);
                    dialogInterface.dismiss();
                }
            });
        }
        dialog.setCancelable(false).create().show();
    }

}
