package com.lifeshare.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lifeshare.BaseActivity;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.UserProfileRequest;
import com.lifeshare.network.response.LoginResponse;
import com.lifeshare.network.response.MyConnectionListResponse;
import com.lifeshare.ui.ProfileActivity;
import com.lifeshare.utils.Const;
import com.lifeshare.utils.PreferenceHelper;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout llMain;
    private CircleImageView ivProfile;
    private AppCompatTextView tvName;
    private AppCompatTextView tvChannelNameOne;
    private AppCompatTextView tvChannelName;
    private AppCompatTextView tvEmail;
    private AppCompatTextView tvShortDescription;
    private AppCompatButton btnEdit;
    private AppCompatTextView tvNameOne;
    private AppCompatTextView tvCityName;
    private AppCompatTextView tvStateName;
    private AppCompatTextView tvCountryName;
    private AppCompatTextView tvPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_profile);
        initView();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle.getString(Const.PROFILE).equalsIgnoreCase(Const.MY_PROFILE)) {
                btnEdit.setVisibility(View.VISIBLE);
                setData();
            } else {

                btnEdit.setVisibility(View.GONE);
                MyConnectionListResponse data = (MyConnectionListResponse) bundle.getSerializable(Const.USER_DATA);

                getOtherProfileData(data.getUserId());

            }
        }
    }

    private void getOtherProfileData(String userId) {
        showLoading();
        UserProfileRequest request = new UserProfileRequest();
        request.setUserId(userId);
        WebAPIManager.getInstance().getUserProfile(request, new RemoteCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse response) {
                hideLoading();
                setOtherUserData(response);
            }
        });

    }

    private void setOtherUserData(LoginResponse user) {
        tvName.setText(user.getFirstName() + " " + user.getLastName());
        tvNameOne.setText(user.getFirstName() + " " + user.getLastName());
        tvChannelName.setText(user.getUsername());
        tvChannelNameOne.setText(user.getUsername());
        tvEmail.setText(user.getEmail());
        tvShortDescription.setText(user.getDescription());
        if (user.getCountry() != null && user.getCountry().getName() != null) {
            tvCountryName.setText(user.getCountry().getName());
        }
        if (user.getState() != null && user.getState().getName() != null) {
            tvStateName.setText(user.getState().getName());
        }
        if (user.getCity() != null && user.getCity().getName() != null) {
            tvCityName.setText(user.getCity().getName());
        }
        if (user.getMobile() != null) {
            tvPhoneNumber.setText(user.getMobile());
        }


        Glide.with(LifeShare.getInstance())
                .load(user.getAvatar())
                .apply(new RequestOptions().error(R.drawable.user_placeholder).placeholder(R.drawable.user_placeholder))
                .into(ivProfile);

    }

    private void setData() {
        LoginResponse user = PreferenceHelper.getInstance().getUser();
        tvName.setText(user.getFirstName() + " " + user.getLastName());
        tvNameOne.setText(user.getFirstName() + " " + user.getLastName());
        tvChannelName.setText(user.getUsername());
        tvChannelNameOne.setText(user.getUsername());
        tvEmail.setText(user.getEmail());
        tvShortDescription.setText(user.getDescription());
        if (user.getCountry() != null && user.getCountry().getName() != null) {
            tvCountryName.setText(user.getCountry().getName());
        }
        if (user.getState() != null && user.getState().getName() != null) {
            tvStateName.setText(user.getState().getName());
        }
        if (user.getCity() != null && user.getCity().getName() != null) {
            tvCityName.setText(user.getCity().getName());
        }
        if (user.getMobile() != null) {
            tvPhoneNumber.setText(user.getMobile());
        }

        Glide.with(LifeShare.getInstance())
                .load(user.getAvatar())
                .apply(new RequestOptions().error(R.drawable.user_placeholder).placeholder(R.drawable.user_placeholder))
                .into(ivProfile);

    }

    private void initView() {
        getSupportActionBar().hide();
        llMain = (LinearLayout) findViewById(R.id.ll_main);
        ivProfile = (CircleImageView) findViewById(R.id.iv_profile);
        tvName = (AppCompatTextView) findViewById(R.id.tv_name);
        tvChannelNameOne = (AppCompatTextView) findViewById(R.id.tv_channel_name_one);
        tvChannelName = (AppCompatTextView) findViewById(R.id.tv_channel_name);
        tvEmail = (AppCompatTextView) findViewById(R.id.tv_email);
        tvShortDescription = (AppCompatTextView) findViewById(R.id.tv_short_description);
        btnEdit = (AppCompatButton) findViewById(R.id.btn_edit);


        btnEdit.setOnClickListener(this);
        tvNameOne = (AppCompatTextView) findViewById(R.id.tv_name_one);
        tvCityName = (AppCompatTextView) findViewById(R.id.tv_city_name);
        tvStateName = (AppCompatTextView) findViewById(R.id.tv_state_name);
        tvCountryName = (AppCompatTextView) findViewById(R.id.tv_country_name);
        tvPhoneNumber = (AppCompatTextView) findViewById(R.id.tv_phone_number);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_edit:

                Intent intent = new Intent(ViewProfileActivity.this, ProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(Const.PROFILE, Const.MY_PROFILE);
                intent.putExtras(bundle);
                startActivity(intent);


                break;
        }
    }
}
