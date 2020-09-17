package com.lifeshare;

import android.text.TextUtils;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.lifeshare.network.BaseRemoteCallback;

public class BaseFragment extends Fragment implements BaseRemoteCallback {

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

}
