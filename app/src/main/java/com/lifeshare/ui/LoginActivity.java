package com.lifeshare.ui;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.lifeshare.BaseActivity;
import com.lifeshare.BuildConfig;
import com.lifeshare.R;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.LoginRequest;
import com.lifeshare.network.response.LoginResponse;
import com.lifeshare.utils.PreferenceHelper;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private AppCompatEditText etEmail;
    private AppCompatEditText etPassword;
    private AppCompatButton btnLogin;
    private AppCompatTextView tvForgotPassword;
    private AppCompatButton btnSignUp;
    private RelativeLayout rlMain;


    public static boolean isValidEmail(CharSequence target) {
        return (Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login_new);

        initView();
    }

    private void initView() {
        etEmail = (AppCompatEditText) findViewById(R.id.et_email);
        etPassword = (AppCompatEditText) findViewById(R.id.et_password);
        btnLogin = (AppCompatButton) findViewById(R.id.btn_login);


        if (BuildConfig.FLAVOR.equalsIgnoreCase("Dev") && BuildConfig.BUILD_TYPE.equalsIgnoreCase("debug")) {
            etEmail.setText("kundan101");
            etPassword.setText("Test105*");
/*
            etEmail.setText("bhavy.koshti@9spl.com");
            etPassword.setText("5P20vvMAwA");
*/
        }

        tvForgotPassword = (AppCompatTextView) findViewById(R.id.tv_forgot_password);
        btnSignUp = (AppCompatButton) findViewById(R.id.btn_sign_up);

        btnLogin.setOnClickListener(this);
        tvForgotPassword.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);


        rlMain = (RelativeLayout) findViewById(R.id.rl_main);
        rlMain.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideKeyboard(LoginActivity.this);
                return false;
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                if (isValid()) {
                    if (PreferenceHelper.getInstance().getFcmToken().isEmpty()) {

                        if (!checkInternetConnection()) {
                            return;
                        }
                        showLoading();

                        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                            @Override
                            public void onSuccess(InstanceIdResult instanceIdResult) {
                                hideLoading();
                                String token = instanceIdResult.getToken();
                                PreferenceHelper.getInstance().setFcmToken(token);
                                Log.v(TAG, "onSuccess:token : " + token);
                                checkLogin();
                            }
                        });

                    } else {
                        checkLogin();
                    }
                }
                break;
            case R.id.tv_forgot_password:
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
                break;
            case R.id.btn_sign_up:
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                break;
        }
    }

    private void checkLogin() {
        if (!checkInternetConnection()) {
            return;
        }
        showLoading();
        LoginRequest request = new LoginRequest();
        request.setEmail(etEmail.getText().toString().trim());
        request.setPassword(etPassword.getText().toString().trim());
        request.setDeviceId(Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID));
        request.setDeviceToken(PreferenceHelper.getInstance().getFcmToken());
        WebAPIManager.getInstance().checkLogin(request, new RemoteCallback<LoginResponse>(this) {
            @Override
            public void onSuccess(LoginResponse response) {
                hideLoading();

                //get remaining notification
//                LifeShare.getInstance().getAllRemainingPushNotification();

                PreferenceHelper.getInstance().setUser(response);
                PreferenceHelper.getInstance().setFcmTokenUpdated(false);


                if (PreferenceHelper.getInstance().getIsAcceptTermOfService()) {
                    startActivity(new Intent(LoginActivity.this, TwilioBroadcastActivityNew.class));
                    finish();
                } else {
                    startActivity(new Intent(LoginActivity.this, TermOfServicesActivity.class));
                    finish();
                }

            }
        });
    }

    private boolean isValid() {
        if (TextUtils.isEmpty(etEmail.getText().toString().trim())) {
            Toast.makeText(this, getResources().getString(R.string.please_enter_email_or_username), Toast.LENGTH_SHORT).show();
            return false;
        }
/*
        if (!isValidEmail(etEmail.getText().toString().trim())) {
            Toast.makeText(this, getResources().getString(R.string.please_enter_valid_email), Toast.LENGTH_SHORT).show();
            return false;
        }
*/
        if (TextUtils.isEmpty(etPassword.getText().toString().trim())) {
            Toast.makeText(this, getResources().getString(R.string.please_enter_password), Toast.LENGTH_SHORT).show();
            return false;
        }
        if ((etPassword.getText().toString().trim().length() < 6)) {
            Toast.makeText(this, getResources().getString(R.string.please_enter_valid_password), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


}
