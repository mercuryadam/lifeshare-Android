package com.lifeshare.ui.profile;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lifeshare.BaseActivity;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterRecyclerView;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.DeleteArchivesRequest;
import com.lifeshare.network.request.GetArchiveListRequest;
import com.lifeshare.network.request.UserProfileRequest;
import com.lifeshare.network.response.ChannelArchiveResponse;
import com.lifeshare.network.response.CommonResponse;
import com.lifeshare.network.response.LoginResponse;
import com.lifeshare.network.response.MyConnectionListResponse;
import com.lifeshare.ui.ProfileActivity;
import com.lifeshare.utils.Const;
import com.lifeshare.utils.PreferenceHelper;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends BaseActivity implements View.OnClickListener, MyDialogCloseListener {

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
    private LinearLayout llEmailPhoneCity, llCATitle;
    private FilterRecyclerView rvChannelArchive;
    private ChannelArchiveAdapter channelArchiveAdapter;
    private ArrayList<ChannelArchiveResponse> channelArchiveList = new ArrayList<>();
    private ImageView addArchivesFromDialog;
    private String userId = "";
    private AppCompatTextView tvTotalViewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        initView();
        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle.getString(Const.PROFILE).equalsIgnoreCase(Const.MY_PROFILE)) {
                btnEdit.setVisibility(View.VISIBLE);
                llEmailPhoneCity.setVisibility(View.VISIBLE);
                llCATitle.setVisibility(View.VISIBLE);
                rvChannelArchive.setVisibility(View.VISIBLE);
                userId = PreferenceHelper.getInstance().getUser().getUserId();
//                setData();
                getOtherProfileData(userId);
            } else {

                btnEdit.setVisibility(View.GONE);
                llEmailPhoneCity.setVisibility(View.GONE);
                addArchivesFromDialog.setVisibility(View.GONE);
                MyConnectionListResponse data = (MyConnectionListResponse) bundle.getParcelable(Const.USER_DATA);
                userId = data.getUserId();
                getOtherProfileData(data.getUserId());

            }
            setRecyclerView();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
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
                getListChannelArchive(response.getUserId());
            }
        });

    }

    private void setOtherUserData(LoginResponse user) {
        userId = user.getUserId();
        tvName.setText(user.getFirstName() + " " + user.getLastName());
        tvNameOne.setText(user.getFirstName() + " " + user.getLastName());
        tvChannelName.setText(user.getUsername());
        tvChannelNameOne.setText(user.getUsername());
        tvTotalViewer.setText(user.getViewerCount());
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
        userId = PreferenceHelper.getInstance().getUser().getUserId();
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
        llEmailPhoneCity = findViewById(R.id.llEmailPhoneCity);
        llCATitle = findViewById(R.id.llCATitle);
        llCATitle.setOnClickListener(this);
        rvChannelArchive = findViewById(R.id.rvChannelArchive);
        addArchivesFromDialog = findViewById(R.id.addArchivesFromDialog);
        addArchivesFromDialog.setOnClickListener(this);

        tvTotalViewer = (AppCompatTextView) findViewById(R.id.tv_total_viewer);
    }

    private void setRecyclerView() {
        rvChannelArchive.setLayoutManager(new GridLayoutManager(this, 3));
        rvChannelArchive.setNestedScrollingEnabled(false);
        channelArchiveAdapter = new ChannelArchiveAdapter(userId, new BaseRecyclerListener<ChannelArchiveResponse>() {
            @Override
            public void showEmptyDataView(int resId) {

            }

            @Override
            public void onRecyclerItemClick(View view, int position, ChannelArchiveResponse item) {
                if (view.getId() == R.id.ivDeleteArchive) {
                    otherDialog(ViewProfileActivity.this, getResources().getString(R.string.delete_channel_archive_message), getResources().getString(R.string.yes), getResources().getString(R.string.no), new DismissListenerWithStatus() {
                        @Override
                        public void onDismissed(String message) {
                            if (message.equalsIgnoreCase(getResources().getString(R.string.yes))) {
                                deleteChannelArchive(item.getCAId());
                            }
                        }
                    });
                } else {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    String url = item.getLink();
                    if (!url.startsWith("http://") && !url.startsWith("https://"))
                        url = "http://" + url;
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }

            }
        });
        rvChannelArchive.setAdapter(channelArchiveAdapter);

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

            case R.id.llCATitle:
            case R.id.addArchivesFromDialog:
                if (PreferenceHelper.getInstance().getUser().getUserId().equals(userId)) {
                    // Create and show the dialog.
                    DialogFragment newFragment = AddChannelArchive.newInstance();
                    newFragment.show(getSupportFragmentManager(), "dialog");
                }

                break;

        }
    }

    private void getListChannelArchive(String userId) {

        GetArchiveListRequest request = new GetArchiveListRequest();
        request.setUserId(userId);
        showLoading();
        WebAPIManager.getInstance().listChannelArchive(request, new RemoteCallback<ArrayList<ChannelArchiveResponse>>() {
            @Override
            public void onSuccess(ArrayList<ChannelArchiveResponse> response) {
                channelArchiveList = response;
                channelArchiveAdapter.removeAllItems();
                channelArchiveAdapter.addItems(channelArchiveList);
                hideLoading();
            }

            @Override
            public void onEmptyResponse(String message) {
                hideLoading();
            }
        });

    }

    private void deleteChannelArchive(Integer id) {

        showLoading();
        DeleteArchivesRequest request = new DeleteArchivesRequest();
        request.setId(String.valueOf(id));

        WebAPIManager.getInstance().deleteChannelArchive(request, new RemoteCallback<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse response) {
                hideLoading();
                getListChannelArchive(userId);
            }

            @Override
            public void onEmptyResponse(String message) {
                hideLoading();
            }

            @Override
            public void onUnauthorized(Throwable throwable) {
                super.onUnauthorized(throwable);
            }
        });

    }

    @Override
    public void handleDialogClose(DialogInterface dialog) {
        getListChannelArchive(userId);
    }
}
