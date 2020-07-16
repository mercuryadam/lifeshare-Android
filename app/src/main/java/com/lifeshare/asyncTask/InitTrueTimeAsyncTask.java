package com.lifeshare.asyncTask;

import android.os.AsyncTask;

import com.instacart.library.truetime.TrueTime;
import com.lifeshare.LifeShare;

import java.io.IOException;

public class InitTrueTimeAsyncTask extends AsyncTask<Void, Void, Void> {

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
    }
}