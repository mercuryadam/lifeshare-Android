package com.lifeshare.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

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
    private AppCompatImageView ivBack;
    private RelativeLayout appBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_new);

        initView();
    }

    private void initView() {
        etEmail = (AppCompatEditText) findViewById(R.id.et_email);
        btnReset = (AppCompatButton) findViewById(R.id.btn_reset);
        appBar = (RelativeLayout) findViewById(R.id.appbar_new);
        ivBack = (AppCompatImageView) appBar.findViewById(R.id.ivBack);
        ivBack.setVisibility(View.VISIBLE);
        btnReset.setOnClickListener(this);
        ivBack.setOnClickListener(this);

        if (!BuildConfig.BUILD_TYPE.equalsIgnoreCase("release")) {
            etEmail.setText("chirag.patel@9spl.com");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivBack:
                onBackPressed();
                break;
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
