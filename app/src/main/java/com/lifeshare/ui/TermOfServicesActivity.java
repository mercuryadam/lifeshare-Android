package com.lifeshare.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;

import com.lifeshare.BaseActivity;
import com.lifeshare.R;
import com.lifeshare.utils.PreferenceHelper;

public class TermOfServicesActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout llButton;
    private AppCompatButton btnReject;
    private AppCompatButton btnAccept;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term_of_services);
        initView();
        getSupportActionBar().setTitle(R.string.term_of_services);
        getSupportActionBar().hide();
    }

    private void initView() {
        llButton = (LinearLayout) findViewById(R.id.ll_button);
        btnReject = (AppCompatButton) findViewById(R.id.btn_reject);
        btnAccept = (AppCompatButton) findViewById(R.id.btn_accept);
        btnAccept.setOnClickListener(this);
        btnReject.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_accept:

                PreferenceHelper.getInstance().setTermOfServices(true);
                startActivity(new Intent(TermOfServicesActivity.this, BroadcastActivityNew.class));
                finish();


/*
                User user = PreferenceHelper.getInstance().getUser();
                user.setTermOfService("1");
                PreferenceHelper.getInstance().setUser(user);
*/

             /*   new UserViewModel.InsertUpdateUserData(user, new DatabaseCallbackListener<User>() {
                    @Override
                    public void onSuccess(User user) {
                        startActivity(new Intent(TermOfServicesActivity.this, MainActivity.class));
                        finish();
                    }

                    @Override
                    public void onFailed(Exception e) {

                    }
                }).execute();*/
                break;
            case R.id.btn_reject:
                Toast.makeText(this, getResources().getString(R.string.please_accept_term_of_services), Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
