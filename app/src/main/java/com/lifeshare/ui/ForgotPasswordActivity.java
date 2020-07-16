package com.lifeshare.ui;

import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.lifeshare.BaseActivity;
import com.lifeshare.BuildConfig;
import com.lifeshare.R;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.ForgotPasswordRequest;
import com.lifeshare.network.response.CommonResponse;

import static com.lifeshare.ui.LoginActivity.isValidEmail;

public class ForgotPasswordActivity extends BaseActivity implements View.OnClickListener {

    private AppCompatEditText etEmail;
    private AppCompatButton btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_new);

        initView();
    }

    private void initView() {
        etEmail = (AppCompatEditText) findViewById(R.id.et_email);
        btnReset = (AppCompatButton) findViewById(R.id.btn_reset);
        btnReset.setOnClickListener(this);

        if (BuildConfig.FLAVOR.equalsIgnoreCase("Dev")) {
            etEmail.setText("chirag.patel@9spl.com");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reset:
                if (isValid()) {
                    resetPassword();
                }
                break;
        }
    }

    private void resetPassword() {
        showLoading();
        final ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail(etEmail.getText().toString().trim());
        WebAPIManager.getInstance().forgotPassword(request, new RemoteCallback<CommonResponse>(this) {
            @Override
            public void onSuccess(CommonResponse response) {
                hideLoading();
                showToast(response.getMessage());
                finish();
            }
        });
    }

    private boolean isValid() {
        if (TextUtils.isEmpty(etEmail.getText().toString().trim())) {
            Toast.makeText(this, getResources().getString(R.string.please_enter_email), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isValidEmail(etEmail.getText().toString().trim())) {
            Toast.makeText(this, getResources().getString(R.string.please_enter_valid_email), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
