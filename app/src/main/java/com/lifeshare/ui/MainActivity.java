package com.lifeshare.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lifeshare.BaseActivity;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.response.CommonResponse;
import com.lifeshare.ui.invitation.MyInvitationListActivity;
import com.lifeshare.ui.my_connection.MyConnectionListActivity;
import com.lifeshare.ui.profile.ViewProfileActivity;
import com.lifeshare.utils.Const;
import com.lifeshare.utils.PreferenceHelper;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private AppCompatButton btnStart;
    private AppCompatButton btnShow;
    private AppCompatTextView tvName;
    private AppCompatImageView ivMore;
    private AppCompatTextView tvInvitations;
    private AppCompatTextView tvMyConnections;
    private AppCompatTextView tvProfile;
    private AppCompatTextView tvHome;
    private AppCompatTextView logout;
    private AppCompatTextView tvDialogName;
    private AppCompatTextView tvInvite;
    private CircleImageView ivProfile;
    private AppCompatImageView ivClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        initView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                otherDialog(this, getResources().getString(R.string.logout_message), getResources().getString(R.string.yes), getResources().getString(R.string.no), new DismissListenerWithStatus() {
                    @Override
                    public void onDismissed(String message) {
                        if (message.equalsIgnoreCase(getResources().getString(R.string.yes))) {
                            logoutCall();
                        }
                    }
                });
                break;
            case R.id.my_connection:
                startActivity(new Intent(MainActivity.this, MyConnectionListActivity.class));
                break;
            case R.id.invitation:
                startActivity(new Intent(MainActivity.this, MyInvitationListActivity.class));
                break;
            case R.id.profile:
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(Const.PROFILE, Const.MY_PROFILE);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logoutCall() {
        showLoading();
        WebAPIManager.getInstance().logout(new RemoteCallback<CommonResponse>(this) {
            @Override
            public void onSuccess(CommonResponse response) {
                hideLoading();
                logout();
            }

            @Override
            public void onUnauthorized(Throwable throwable) {
                super.onUnauthorized(throwable);
                hideLoading();
                logout();

            }

            @Override
            public void onFailed(Throwable throwable) {
                super.onFailed(throwable);
                hideLoading();
                logout();
            }

            @Override
            public void onInternetFailed() {
                super.onInternetFailed();
                hideLoading();
                logout();
            }
        });
    }

    private void logout() {
        clearAllNotification();
        PreferenceHelper.getInstance().setIsLogIn(false);
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void initView() {

        btnStart = (AppCompatButton) findViewById(R.id.btn_start);
        btnShow = (AppCompatButton) findViewById(R.id.btn_show);
        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkInternetConnection()) {
//                    startActivity(new Intent(MainActivity.this, ShowStreamActivityNew.class));
//                    startActivity(new Intent(MainActivity.this, TestActivity.class));
                }
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, BroadcastActivity.class));
//                startActivity(new Intent(MainActivity.this, BroadcastActivityNew.class));
            }
        });
        if (PreferenceHelper.getInstance().getFcmTokenUpdated()) {
            LifeShare.getInstance().updateFcmTokenToServer();
        }
        tvName = findViewById(R.id.tv_name);
        tvName.setText("Welcome to LifeShare " + PreferenceHelper.getInstance().getUser().getFirstName()
                + " " + PreferenceHelper.getInstance().getUser().getLastName());

        ivMore = findViewById(R.id.iv_more);

        ivMore.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
    }

    void showDialog() {

        Dialog dialog = new Dialog(MainActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.nav_drawer_new);

        ivClose = (AppCompatImageView) dialog.findViewById(R.id.iv_close);
        ivProfile = (CircleImageView) dialog.findViewById(R.id.iv_profile);
        logout = (AppCompatTextView) dialog.findViewById(R.id.logout);
        tvHome = (AppCompatTextView) dialog.findViewById(R.id.tv_home);
        tvProfile = (AppCompatTextView) dialog.findViewById(R.id.tv_profile);
        tvMyConnections = (AppCompatTextView) dialog.findViewById(R.id.tv_my_connections);
        tvInvitations = (AppCompatTextView) dialog.findViewById(R.id.tv_invitations);
        tvDialogName = (AppCompatTextView) dialog.findViewById(R.id.tv_name);
        tvInvite = (AppCompatTextView) dialog.findViewById(R.id.tv_invite);

        tvDialogName.setText(PreferenceHelper.getInstance().getUser().getFirstName() + " " + PreferenceHelper.getInstance().getUser().getLastName());

        Glide.with(LifeShare.getInstance())
                .load(PreferenceHelper.getInstance().getUser().getAvatar())
                .apply(new RequestOptions().error(R.drawable.user_placeholder).placeholder(R.drawable.user_placeholder))
                .into(ivProfile);


        tvInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MyInvitationListActivity.class));
                dialog.dismiss();
            }
        });

        tvInvitations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MyInvitationListActivity.class));
                dialog.dismiss();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otherDialog(MainActivity.this, getResources().getString(R.string.logout_message), getResources().getString(R.string.yes), getResources().getString(R.string.no), new DismissListenerWithStatus() {
                    @Override
                    public void onDismissed(String message) {
                        if (message.equalsIgnoreCase(getResources().getString(R.string.yes))) {
                            logoutCall();
                        }
                    }
                });

            }
        });

        tvMyConnections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MyConnectionListActivity.class));
                dialog.dismiss();
            }
        });

        tvProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewProfileActivity.class);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
