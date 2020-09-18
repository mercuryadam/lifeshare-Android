package com.lifeshare;

import android.text.TextUtils;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.lifeshare.customview.CustomProgressDialog;
import com.lifeshare.network.BaseRemoteCallback;

public class BaseFragment extends Fragment implements BaseRemoteCallback {
    public CustomProgressDialog mProgressDialog;
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
            mProgressDialog = new CustomProgressDialog(getContext(), R.style.progress_dialog_text_style);
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


}
