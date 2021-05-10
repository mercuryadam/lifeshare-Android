package com.lifeshare.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.lifeshare.BaseActivity;
import com.lifeshare.R;
import com.lifeshare.customview.singleChoiceBottomSheet.SingleChoiceBottomSheet;
import com.lifeshare.customview.singleChoiceBottomSheet.SingleChoiceDialogListener;
import com.lifeshare.imagepicker.ImagePickerFragment;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.CityRequest;
import com.lifeshare.network.request.SignUpRequest;
import com.lifeshare.network.request.StateRequest;
import com.lifeshare.network.response.CityResponse;
import com.lifeshare.network.response.CountryResponse;
import com.lifeshare.network.response.LoginResponse;
import com.lifeshare.network.response.MyConnectionListResponse;
import com.lifeshare.network.response.StateResponse;
import com.lifeshare.utils.Const;
import com.lifeshare.utils.PreferenceHelper;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends BaseActivity implements View.OnClickListener, ImagePickerFragment.ImagePickerListener {

    private static final int CAMERA_REQUEST_ID = 101;
    ArrayList<CountryResponse> countryList = new ArrayList<>();
    ArrayList<StateResponse> stateList = new ArrayList<>();
    ArrayList<CityResponse> cityList = new ArrayList<>();
    CountryResponse selectedCountry;
    StateResponse selectedState;
    CityResponse selectedCity;
    private AppBarLayout appBar;
    private Toolbar toolbar;
    private CircleImageView ivProfile;
    private AppCompatEditText etFirstName;
    private AppCompatEditText etLastName;
    private AppCompatEditText etUsername;
    private AppCompatEditText etEmail;
    private AppCompatEditText etShortDiscription;
    private String imagePath = "";
    private AppCompatTextView tvName, tvToolbarTitle, tvBack, tvDone;
    private RelativeLayout rlToolbar;
    private AppCompatTextView tvChannelName;
    private AppCompatButton btnChangePass;
    private AppCompatEditText etPhoneNumber;
    private AppCompatEditText etCountry;
    private AppCompatEditText etState;
    private AppCompatEditText etCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_new);

        initView();
        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle.getString(Const.PROFILE).equalsIgnoreCase(Const.MY_PROFILE)) {
                btnChangePass.setVisibility(View.VISIBLE);
                setData();
            } else {
                btnChangePass.setVisibility(View.GONE);
                MyConnectionListResponse data = (MyConnectionListResponse) bundle.getSerializable(Const.USER_DATA);

                setOtherUserData(data);
            }
        }
    }

    private void setOtherUserData(MyConnectionListResponse data) {
//        getSupportActionBar().setTitle(data.getFirstName() + " " + data.getLastName());
        tvDone.setVisibility(View.GONE);
        etFirstName.setText(data.getFirstName());
        etLastName.setText(data.getLastName());
        etEmail.setText(data.getEmail());
        etUsername.setText(data.getUsername());
        etShortDiscription.setText(data.getDescription());
        etUsername.setEnabled(false);
        imagePath = data.getAvatar();
        Glide.with(this)
                .load(data.getAvatar())
                .apply(new RequestOptions().error(R.drawable.user_placeholder).placeholder(R.drawable.user_placeholder))
                .into(ivProfile);

        etFirstName.setEnabled(false);
        etLastName.setEnabled(false);
        etEmail.setEnabled(false);
        etUsername.setEnabled(false);
        etShortDiscription.setEnabled(false);
        etUsername.setEnabled(false);
        ivProfile.setClickable(false);


    }

    private void setData() {
        etFirstName.setText(PreferenceHelper.getInstance().getUser().getFirstName());
        etLastName.setText(PreferenceHelper.getInstance().getUser().getLastName());
        etEmail.setText(PreferenceHelper.getInstance().getUser().getEmail());
        etUsername.setText(PreferenceHelper.getInstance().getUser().getUsername());
        etShortDiscription.setText(PreferenceHelper.getInstance().getUser().getDescription());
        etUsername.setEnabled(false);
        imagePath = PreferenceHelper.getInstance().getUser().getAvatar();

        Glide.with(this)
                .load(PreferenceHelper.getInstance().getUser().getAvatar())
                .apply(new RequestOptions().error(R.drawable.user_placeholder).placeholder(R.drawable.user_placeholder))
                .into(ivProfile);

        tvName.setText(PreferenceHelper.getInstance().getUser().getFirstName() + " " + PreferenceHelper.getInstance().getUser().getLastName());
        tvChannelName.setText(PreferenceHelper.getInstance().getUser().getUsername());

        etPhoneNumber.setText(PreferenceHelper.getInstance().getUser().getMobile());
        getCountryList();
        selectedCountry = PreferenceHelper.getInstance().getUser().getCountry();
        if (selectedCountry != null && selectedCountry.getId() != null) {
            etCountry.setText(selectedCountry.getName());
            getStateList(selectedCountry);
            selectedState = PreferenceHelper.getInstance().getUser().getState();
            if (selectedState != null && selectedState.getId() != null) {
                etState.setText(selectedState.getName());
                getCityList(selectedState);
            }
            selectedCity = PreferenceHelper.getInstance().getUser().getCity();
            if (selectedCity != null && selectedCity.getName() != null) {
                etCity.setText(selectedCity.getName());
            }
        }
    }

    private void initView() {

        rlToolbar = (RelativeLayout) findViewById(R.id.appbar_new);
        tvToolbarTitle = (AppCompatTextView) rlToolbar.findViewById(R.id.tvToolbarTitle);
        tvBack = (AppCompatTextView) rlToolbar.findViewById(R.id.tvBack);
        tvBack.setTextAppearance(this, R.style.TextRegular_colorBlack_size18);
        tvDone = (AppCompatTextView) rlToolbar.findViewById(R.id.tvDone);
        tvToolbarTitle.setVisibility(View.VISIBLE);
        tvBack.setVisibility(View.VISIBLE);
        tvDone.setVisibility(View.VISIBLE);
        tvToolbarTitle.setText(R.string.edit_profile);

        appBar = findViewById(R.id.appBar);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.profile);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ivProfile = findViewById(R.id.iv_profile);
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etShortDiscription = findViewById(R.id.et_short_discription);
        tvDone.setOnClickListener(this);
        ivProfile.setOnClickListener(this);
        tvName = (AppCompatTextView) findViewById(R.id.tv_name);
        tvChannelName = (AppCompatTextView) findViewById(R.id.tv_channel_name);
        btnChangePass = (AppCompatButton) findViewById(R.id.btn_change_pass);

        btnChangePass.setOnClickListener(this);
        tvBack.setOnClickListener(this);

        etPhoneNumber = (AppCompatEditText) findViewById(R.id.et_phone_number);
        etCountry = (AppCompatEditText) findViewById(R.id.et_country);
        etState = (AppCompatEditText) findViewById(R.id.et_state);
        etCity = (AppCompatEditText) findViewById(R.id.et_city);

        etCountry.setOnClickListener(this);
        etState.setOnClickListener(this);
        etCity.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvDone:
                if (isValid()) {
                    updateProfile();
                }
                break;
            case R.id.tvBack:
                onBackPressed();
                break;
            case R.id.btn_change_pass:
                startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));
                break;
            case R.id.iv_profile:
                new ImagePickerFragment()
                        .newInstance(CAMERA_REQUEST_ID, "LifeShare").show(getSupportFragmentManager());

                break;
            case R.id.et_country:
                hideKeyboard(ProfileActivity.this);
                openCountryListBottomSheet();
                break;
            case R.id.et_state:
                hideKeyboard(ProfileActivity.this);
                openStateBottomSheet();
                break;
            case R.id.et_city:
                hideKeyboard(ProfileActivity.this);
                openCityBottomSheet();
                break;


        }
    }

    private void updateProfile() {

        if (!checkInternetConnection()) {
            return;
        }
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setFirstName(etFirstName.getText().toString().trim());
        signUpRequest.setLastName(etLastName.getText().toString().trim());
        signUpRequest.setEmail(etEmail.getText().toString().trim());
        signUpRequest.setDescription(etShortDiscription.getText().toString().trim());
        signUpRequest.setAvatar(imagePath);

        if (selectedCity != null) {
            signUpRequest.setCity(selectedCity.getId());
        } else signUpRequest.setCity("");

        if (selectedState != null) {
            signUpRequest.setState(selectedState.getId());
        } else signUpRequest.setState("");

        if (selectedCountry != null) {
            signUpRequest.setCountry(selectedCountry.getId());
        } else signUpRequest.setCountry("");

        signUpRequest.setMobile(etPhoneNumber.getText().toString().trim());

        showLoading();

        WebAPIManager.getInstance().updateProfile(signUpRequest, new RemoteCallback<LoginResponse>(this) {
            @Override
            public void onSuccess(LoginResponse response) {
                hideLoading();
                setResult(RESULT_OK);
                PreferenceHelper.getInstance().setUser(response);
                showToast(getString(R.string.profile_update_message));
            }
        });

    }

    private boolean isValid() {
        /*if (TextUtils.isEmpty(imagePath)) {
            showToast(getResources().getString(R.string.please_select_profile_picture));
            return false;
        }*/
        if (TextUtils.isEmpty(etFirstName.getText().toString().trim())) {
            showToast(getResources().getString(R.string.please_enter_first_name));
            return false;
        }
        if (TextUtils.isEmpty(etLastName.getText().toString().trim())) {
            showToast(getResources().getString(R.string.please_enter_last_name));
            return false;
        }
        if (TextUtils.isEmpty(etUsername.getText().toString().trim())) {
            showToast(getResources().getString(R.string.please_enter_username));
            return false;
        }
        if (TextUtils.isEmpty(etEmail.getText().toString().trim())) {
            showToast(getResources().getString(R.string.please_enter_email));
            return false;
        }
       /* if (TextUtils.isEmpty(etPhoneNumber.getText().toString().trim())) {
            showToast(getResources().getString(R.string.please_enter_mobile_number));
            return false;
        }*/

        if (!Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString().trim()).matches()) {
            showToast(getResources().getString(R.string.please_enter_valid_email));
            return false;
        }
        if (TextUtils.isEmpty(etShortDiscription.getText().toString().trim())) {
            showToast(getResources().getString(R.string.please_enter_short_discription));
            return false;
        }

       /* if (TextUtils.isEmpty(etCountry.getText().toString().trim())) {
            showToast(getResources().getString(R.string.please_enter_country));
            return false;
        }
        if (TextUtils.isEmpty(etState.getText().toString().trim())) {
            showToast(getResources().getString(R.string.please_enter_state));
            return false;
        }
        if (TextUtils.isEmpty(etCity.getText().toString().trim())) {
            showToast(getResources().getString(R.string.please_enter_city));
            return false;
        }*/


        return true;

    }

    private void openStateBottomSheet() {

        SingleChoiceBottomSheet myBottomSheet =
                new SingleChoiceBottomSheet.Builder<StateResponse>(new SingleChoiceDialogListener<StateResponse>() {
                    @Override
                    public void onSpinnerItemClick(int position, StateResponse selectedItem) {
                        hideKeyboard(ProfileActivity.this);
                        if (selectedState != null && selectedState.getId() != null) {
                            if (!selectedState.getId().equals(selectedItem.getId())) {
                                etCity.setText("");
                                cityList.clear();
                                selectedCity = null;

                                etState.setText(selectedItem.getName());
                                selectedState = selectedItem;
                                getCityList(selectedState);

                            }
                        } else {

                            etState.setText(selectedItem.getName());
                            selectedState = selectedItem;
                            getCityList(selectedState);
                        }

                    }
                }).addItems(stateList)
                        .setSelectedItem(selectedState)
                        .addDialogTitle(getString(R.string.State))
                        .addFieldName("name")
                        .build();

        myBottomSheet.show(getSupportFragmentManager(), myBottomSheet.getTag());

    }

    private void openCityBottomSheet() {

        SingleChoiceBottomSheet myBottomSheet =
                new SingleChoiceBottomSheet.Builder<CityResponse>(new SingleChoiceDialogListener<CityResponse>() {
                    @Override
                    public void onSpinnerItemClick(int position, CityResponse selectedItem) {
                        hideKeyboard(ProfileActivity.this);
                        etCity.setText(selectedItem.getName());
                        selectedCity = selectedItem;
                    }
                }).addItems(cityList)
                        .setSelectedItem(selectedCity)
                        .addDialogTitle(getString(R.string.City))
                        .addFieldName("name")
                        .build();

        myBottomSheet.show(getSupportFragmentManager(), myBottomSheet.getTag());

    }

    private void openCountryListBottomSheet() {

        SingleChoiceBottomSheet myBottomSheet =
                new SingleChoiceBottomSheet.Builder<CountryResponse>(new SingleChoiceDialogListener<CountryResponse>() {
                    @Override
                    public void onSpinnerItemClick(int position, CountryResponse selectedItem) {
                        hideKeyboard(ProfileActivity.this);
                        if (selectedCountry != null && selectedCountry.getId() != null) {
                            if (!selectedCountry.getId().equals(selectedItem.getId())) {
                                etState.setText("");
                                etCity.setText("");
                                stateList.clear();
                                cityList.clear();
                                selectedState = null;
                                selectedCity = null;

                                etCountry.setText(selectedItem.getName());
                                selectedCountry = selectedItem;
                                getStateList(selectedCountry);
                            }
                        } else {
                            etCountry.setText(selectedItem.getName());
                            selectedCountry = selectedItem;
                            getStateList(selectedCountry);
                        }
                    }
                }).addItems(countryList)
                        .setSelectedItem(selectedCountry)
                        .addDialogTitle(getString(R.string.country))
                        .addFieldName("name")
                        .build();

        myBottomSheet.show(getSupportFragmentManager(), myBottomSheet.getTag());

    }

    @Override
    public void onImageSelected(int requestId, String uri, String imageName) {

        switch (requestId) {
            case CAMERA_REQUEST_ID:
                imagePath = uri;
                Glide.with(this)
                        .load(uri)
                        .apply(new RequestOptions().error(R.drawable.user_placeholder).placeholder(R.drawable.user_placeholder))
                        .into(ivProfile);
                break;
        }

    }

    @Override
    public void onImagePickerClose() {

    }

    private void getCityList(StateResponse selectedState) {
        showLoading();
        CityRequest request = new CityRequest();
        request.setCountryId(selectedCountry.getId());
        request.setStateId(selectedState.getId());
        WebAPIManager.getInstance().getCityList(request, new RemoteCallback<ArrayList<CityResponse>>(this) {
            @Override
            public void onSuccess(ArrayList<CityResponse> response) {
                hideLoading();
                cityList.clear();
                cityList.addAll(response);
            }

            @Override
            public void onEmptyResponse(String message) {
                hideLoading();
                cityList.clear();
            }
        });

    }

    private void getStateList(CountryResponse selectedCountry) {
        showLoading();
        StateRequest request = new StateRequest();
        request.setCountryId(selectedCountry.getId());
        WebAPIManager.getInstance().getStateList(request, new RemoteCallback<ArrayList<StateResponse>>(this) {
            @Override
            public void onSuccess(ArrayList<StateResponse> response) {
                hideLoading();
                stateList.clear();
                stateList.addAll(response);
            }

            @Override
            public void onEmptyResponse(String message) {
                hideLoading();
                stateList.clear();
            }
        });
    }

    private void getCountryList() {
        showLoading();
        WebAPIManager.getInstance().getCountry(new RemoteCallback<ArrayList<CountryResponse>>(this) {
            @Override
            public void onSuccess(ArrayList<CountryResponse> response) {
                hideLoading();
                countryList.clear();
                countryList.addAll(response);
            }

            @Override
            public void onEmptyResponse(String message) {
                hideLoading();
                super.onEmptyResponse(message);
            }
        });
    }
}
