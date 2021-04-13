package com.lifeshare;

import android.app.AlertDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.lifeshare.customview.CustomProgressDialog;
import com.lifeshare.network.BaseRemoteCallback;

public class BaseFragment extends Fragment implements BaseRemoteCallback {
    public CustomProgressDialog mProgressDialog;
    private AlertDialog alertDialog;

    @Override
    public void onUnauthorized(Throwable throwable) {
        showToast(throwable.getMessage());
    }

    @Override
    public void onFailed(Throwable throwable) {
        showToast(throwable.getMessage());
    }

    @Override
    public void onInternetFailed() {
        showToast(getString(R.string.no_internet_connection_available));
    }

    @Override
    public void onEmptyResponse(String message) {
        showToast(message);
    }

    public void showToast(String message) {
        if (!TextUtils.isEmpty(message)) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    public void showLoading(String message) {
        try {
            hideLoading();
            mProgressDialog = new CustomProgressDialog(getContext(), R.style.progress_dialog_text_style, message);
            mProgressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void showLoading() {
        try {
            hideLoading();
            mProgressDialog = new CustomProgressDialog(requireContext(), R.style.progress_dialog_text_style);
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

    private boolean isInternetAvailable() {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE));
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
                final Snackbar snackbar = Snackbar.make(requireActivity().findViewById(android.R.id.content),
                        R.string.no_internet_connection_available, Snackbar.LENGTH_INDEFINITE);
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView
                        .findViewById(com.google.android.material.R.id.snackbar_text);
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                snackbar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark));
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
                final Snackbar snackbar = Snackbar.make(requireActivity().findViewById(android.R.id.content),
                        R.string.no_internet_connection_available, Snackbar.LENGTH_LONG);
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView
                        .findViewById(com.google.android.material.R.id.snackbar_text);
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                snackbar.show();

            }
            return false;
        }
        return true;
    }

    public void onInternetRetry(int uniqueNumber) {

    }

    public void playAudio(Context mContext, Integer audio) {
        final MediaPlayer mp = MediaPlayer.create(mContext, audio);
        mp.start();
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

        alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
                if (mDismissListenerWithStatus != null) {
                    mDismissListenerWithStatus.onDismissed(possitiveBtnName);
                }
            }
        });
        alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
                if (mDismissListenerWithStatus != null) {
                    mDismissListenerWithStatus.onDismissed(nagativeBtnName);
                }
            }
        });
    }


    public interface DismissListenerWithStatus {
        public void onDismissed(String message);
    }


}
