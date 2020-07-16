package com.lifeshare;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.lifeshare.customview.CustomProgressDialog;
import com.lifeshare.network.BaseRemoteCallback;

/**
 * Created by chirag.patel on 20/11/18.
 */

public class BaseActivity extends AppCompatActivity implements BaseRemoteCallback {
    public CustomProgressDialog mProgressDialog;
    private AlertDialog alertDialog;

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void showLoading(String message) {
        try {
            hideLoading();
            mProgressDialog = new CustomProgressDialog(this, R.style.progress_dialog_text_style, message);
            mProgressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void showLoading() {
        try {
            hideLoading();
            mProgressDialog = new CustomProgressDialog(this, R.style.progress_dialog_text_style);
            mProgressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void hideLoading() {
        try {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        } catch (Exception e) {

        }
    }

    public void otherDialog(Context mContext, String mMessage, final String possitiveBtnName,
                            final String nagativeBtnName,
                            final DismissListenerWithStatus mDismissListenerWithStatus) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View promptView = layoutInflater.inflate(R.layout.alert_dialog_program, null);
        AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(mContext, R.style.InvitationDialog);
        mAlertBuilder.setView(promptView);

        final TextView mMessageTextView = (TextView) promptView.findViewById(R.id.mMessageText);
        mMessageTextView.setText(mMessage);
        mAlertBuilder.setPositiveButton(possitiveBtnName, null);
        mAlertBuilder.setNegativeButton(nagativeBtnName, null);

        // create an alert dialog
        alertDialog = mAlertBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        if (alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        alertDialog.show();

        alertDialog.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
                if (mDismissListenerWithStatus != null) {
                    mDismissListenerWithStatus.onDismissed(possitiveBtnName);
                }
            }
        });
        alertDialog.getButton(android.support.v7.app.AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
                if (mDismissListenerWithStatus != null) {
                    mDismissListenerWithStatus.onDismissed(nagativeBtnName);
                }
            }
        });
    }

    private boolean isInternetAvailable() {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public boolean checkInternetConnection() {
        if (checkInternetConnection(false, 1010)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkInternetConnection(boolean isRetry, final int uniqueNumber) {
        if (!isInternetAvailable()) {
            if (isRetry) {
                final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                        R.string.no_internet_connection_available, Snackbar.LENGTH_INDEFINITE);
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView
                        .findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(ContextCompat.getColor(this, R.color.white));
                snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
                snackbar.setAction(R.string.retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                snackbar.dismiss();
                                onInternetRetry(uniqueNumber);
                            }
                        }
                );
                snackbar.show();
            } else {
                final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                        R.string.no_internet_connection_available, Snackbar.LENGTH_LONG);
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView
                        .findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(ContextCompat.getColor(this, R.color.white));
                snackbar.show();

            }
            return false;
        }
        return true;
    }

    public void onInternetRetry(int uniqueNumber) {

    }

    @Override
    public void onUnauthorized(Throwable throwable) {
        hideLoading();
        showToast(throwable.getMessage());
    }

    @Override
    public void onFailed(Throwable throwable) {
        hideLoading();
        showToast(throwable.getMessage());
    }

    @Override
    public void onInternetFailed() {
        hideLoading();
        showToast(getString(R.string.no_internet_connection_available));
    }

    @Override
    public void onEmptyResponse(String message) {
        hideLoading();
        showToast(message);
    }

    public void showToast(String message) {
        if (!TextUtils.isEmpty(message)) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void clearAllNotification() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    public interface DismissListenerWithStatus {
        public void onDismissed(String message);
    }

}
