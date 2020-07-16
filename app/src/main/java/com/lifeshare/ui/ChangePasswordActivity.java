package com.lifeshare.ui;

import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.lifeshare.BaseActivity;
import com.lifeshare.R;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.ChangePasswordRequest;
import com.lifeshare.network.response.CommonResponse;

public class ChangePasswordActivity extends BaseActivity implements View.OnClickListener {


    private AppCompatEditText etOldPassword;
    private AppCompatEditText etNewPassword;
    private AppCompatEditText etConfirmPassword;
    private AppCompatButton btnChangePass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password_new);

        initView();
    }

    private void initView() {
        etOldPassword = (AppCompatEditText) findViewById(R.id.et_old_password);
        etNewPassword = (AppCompatEditText) findViewById(R.id.et_new_password);
        etConfirmPassword = (AppCompatEditText) findViewById(R.id.et_confirm_password);
        btnChangePass = (AppCompatButton) findViewById(R.id.btn_change_pass);
        btnChangePass.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_change_pass:
                if (isValid()) {
                    resetPassword();
                }
                break;
        }
    }

    private void resetPassword() {
        showLoading();
        final ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword(etOldPassword.getText().toString().trim());
        request.setNewPassword(etNewPassword.getText().toString().trim());
        request.setConfirmPassword(etConfirmPassword.getText().toString().trim());

        WebAPIManager.getInstance().changePassword(request, new RemoteCallback<CommonResponse>(this) {
            @Override
            public void onSuccess(CommonResponse response) {
                hideLoading();
                showToast(response.getMessage());
                finish();
            }
        });
    }

    private boolean isValid() {
        if (TextUtils.isEmpty(etOldPassword.getText().toString().trim())) {
            Toast.makeText(this, getString(R.string.please_enter_old_password), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(etNewPassword.getText().toString().trim())) {
            Toast.makeText(this, getString(R.string.please_enter_new_password), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(etConfirmPassword.getText().toString().trim())) {
            Toast.makeText(this, getString(R.string.please_enter_confirm_pass), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!etNewPassword.getText().toString().trim().equals(etConfirmPassword.getText().toString().trim())) {
            showToast(getResources().getString(R.string.new_pass_not_match));
            return false;
        }


        return true;
    }
}
