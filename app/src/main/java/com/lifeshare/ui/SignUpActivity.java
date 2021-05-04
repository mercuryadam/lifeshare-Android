package com.lifeshare.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import com.lifeshare.network.response.StateResponse;
import com.lifeshare.utils.Const;
import com.lifeshare.utils.PreferenceHelper;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends BaseActivity implements View.OnClickListener, ImagePickerFragment.ImagePickerListener {

    private static final int CAMERA_REQUEST_ID = 101;
    private static final String TAG = "SignUpActivity";
    ArrayList<CountryResponse> countryList = new ArrayList<>();
    ArrayList<StateResponse> stateList = new ArrayList<>();
    ArrayList<CityResponse> cityList = new ArrayList<>();
    CountryResponse selectedCountry;
    StateResponse selectedState;
    CityResponse selectedCity;
    private AppCompatEditText etFirstName;
    private AppCompatEditText etLastName;
    private AppCompatEditText etUsername;
    private AppCompatEditText etEmail;
    private AppCompatEditText etPassword;
    private AppCompatEditText etConfirmPassword;
    private AppCompatButton btnSignUp;
    private AppCompatEditText etShortDiscription;
    private String imagePath = "";
    private RelativeLayout rlMain;
    private AppCompatEditText etCountry;
    private AppCompatEditText etPhoneNumber;
    private AppCompatEditText etState;
    private AppCompatEditText etCity;
    private AppCompatTextView tvBack, tvTnC;
    private RelativeLayout appBar;
    private CircleImageView ivProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_up_new);

        initView();
        getCountryList();
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

    private void getStateList(CountryResponse selectedCountry) {
        showLoading();
        StateRequest request = new StateRequest();
        request.setCountryId(selectedCountry.getId());
        WebAPIManager.getInstance().getStateList(request, new RemoteCallback<ArrayList<StateResponse>>(this
        ) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        appBar = (RelativeLayout) findViewById(R.id.appbar_new);
        tvBack = (AppCompatTextView) appBar.findViewById(R.id.tvBack);
        tvBack.setVisibility(View.VISIBLE);
        tvTnC = (AppCompatTextView) findViewById(R.id.tvTnC);
        etFirstName = (AppCompatEditText) findViewById(R.id.et_first_name);
        etLastName = (AppCompatEditText) findViewById(R.id.et_last_name);
        etUsername = (AppCompatEditText) findViewById(R.id.et_username);
        etEmail = (AppCompatEditText) findViewById(R.id.et_email);
        etPassword = (AppCompatEditText) findViewById(R.id.et_password);
        etConfirmPassword = (AppCompatEditText) findViewById(R.id.et_confirm_password);
        btnSignUp = (AppCompatButton) findViewById(R.id.btn_sign_up);

        Spannable spannable = new SpannableString(getString(R.string.tnc));
    /*    spannable.setSpan(
                new ForegroundColorSpan(getResources().getColor(R.color.primary_green)), getString(R.string.tnc).indexOf("Privacy Policy"), getString(R.string.tnc).indexOf("Privacy Policy") + "Privacy Policy".length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);*/
        spannable.setSpan(
                new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View view) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.lifesharemobileapp.com/privacy"));
                        startActivity(browserIntent);
                    }
                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);
                    }
                }, getString(R.string.tnc).indexOf("Privacy Policy"), getString(R.string.tnc).indexOf("Privacy Policy") + "Privacy Policy".length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      /*  spannable.setSpan(
                new ForegroundColorSpan(getResources().getColor(R.color.primary_green)), getString(R.string.tnc).indexOf("Terms of Service"), getString(R.string.tnc).indexOf("Terms of Service") + "Terms of Service".length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);*/
        spannable.setSpan(
                new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View view) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.lifesharemobileapp.com/terms-and-conditions"));
                        startActivity(browserIntent);
                    }
                    @Override
                    public void updateDrawState(TextPaint ds) {
                        super.updateDrawState(ds);
                        ds.setUnderlineText(false);
                    }
                }, getString(R.string.tnc).indexOf("Terms of Service"), getString(R.string.tnc).indexOf("Terms of Service") + "Terms of Service".length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvTnC.setText(spannable);
        tvTnC.setMovementMethod(LinkMovementMethod.getInstance());
        tvBack.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);
        ivProfile = findViewById(R.id.iv_profile);
        ivProfile.setOnClickListener(this);
        etShortDiscription = findViewById(R.id.et_short_discription);
        rlMain = (RelativeLayout) findViewById(R.id.rl_main);
        rlMain.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideKeyboard(SignUpActivity.this);
                return false;
            }
        });
        etCountry = (AppCompatEditText) findViewById(R.id.et_country);
        etCountry.setOnClickListener(this);
        etPhoneNumber = (AppCompatEditText) findViewById(R.id.et_phone_number);
        etState = (AppCompatEditText) findViewById(R.id.et_state);
        etState.setOnClickListener(this);

        etCity = (AppCompatEditText) findViewById(R.id.et_city);
        etCity.setOnClickListener(this);

    }

    private void SignUp() {
        if (!checkInternetConnection()) {
            return;
        }
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setLoginType(Const.DEFAULT_LOG_IN);
        signUpRequest.setFirstName(etFirstName.getText().toString().trim());
        signUpRequest.setLastName(etLastName.getText().toString().trim());
        signUpRequest.setEmail(etEmail.getText().toString().trim());
        signUpRequest.setUsername(etUsername.getText().toString().trim());
        signUpRequest.setPassword(etPassword.getText().toString().trim());
        signUpRequest.setConfirmPassword(etConfirmPassword.getText().toString().trim());
        signUpRequest.setDeviceId(Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID));
        signUpRequest.setDeviceToken(PreferenceHelper.getInstance().getFcmToken());
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

        WebAPIManager.getInstance().signUp(signUpRequest, new RemoteCallback<LoginResponse>(this) {
            @Override
            public void onSuccess(LoginResponse response) {
                hideLoading();
                showToast(response.getMessage());
                PreferenceHelper.getInstance().setIsLogIn(true);
                PreferenceHelper.getInstance().setUser(response);
                PreferenceHelper.getInstance().setFcmTokenUpdated(false);
                Intent intent = new Intent(SignUpActivity.this, TermOfServicesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean isValid() {
/*
        if (TextUtils.isEmpty(imagePath)) {
            showToast(getResources().getString(R.string.please_select_profile_picture));
            return false;
        }
*/
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
        if (!Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString().trim()).matches()) {
            showToast(getResources().getString(R.string.please_enter_valid_email));
            return false;
        }
       /* if (TextUtils.isEmpty(etPhoneNumber.getText().toString().trim())) {
            showToast(getResources().getString(R.string.please_enter_mobile_number));
            return false;
        }*/

        if (TextUtils.isEmpty(etPassword.getText().toString().trim())) {
            showToast(getResources().getString(R.string.please_enter_password));
            return false;
        }
        if ((etPassword.getText().toString().trim().length() < 6)) {
            showToast(getResources().getString(R.string.please_enter_valid_password));
            return false;
        }
        if (TextUtils.isEmpty(etConfirmPassword.getText().toString().trim())) {
            showToast(getResources().getString(R.string.please_enter_confirm_pass));
            return false;
        }
        if (!etPassword.getText().toString().trim().equals(etConfirmPassword.getText().toString().trim())) {
            showToast(getResources().getString(R.string.pass_not_match));
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

        if (TextUtils.isEmpty(etShortDiscription.getText().toString().trim())) {
            showToast(getResources().getString(R.string.please_enter_short_discription));
            return false;
        }


        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_up:
                if (isValid()) {
                    SignUp();
                }
                break;
            case R.id.et_country:
                hideKeyboard(SignUpActivity.this);
                openCountryListBottomSheet();
                break;
            case R.id.et_state:
                hideKeyboard(SignUpActivity.this);
                openStateBottomSheet();
                break;
            case R.id.et_city:
                hideKeyboard(SignUpActivity.this);
                openCityBottomSheet();
                break;
            case R.id.tvBack:
                onBackPressed();
                break;
            case R.id.iv_profile:
                ImagePickerFragment
                        .newInstance(CAMERA_REQUEST_ID, "LifeShare").show(getSupportFragmentManager());

                break;

        }
    }

    private void openCityBottomSheet() {

        SingleChoiceBottomSheet myBottomSheet =
                new SingleChoiceBottomSheet.Builder<CityResponse>(new SingleChoiceDialogListener<CityResponse>() {
                    @Override
                    public void onSpinnerItemClick(int position, CityResponse selectedItem) {
                        hideKeyboard(SignUpActivity.this);
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

    @Override
    public void onImageSelected(int requestId, String uri, String imageName) {
        Log.v(TAG, "onImageSelected: " + uri);
        imagePath = uri;
        switch (requestId) {
            case CAMERA_REQUEST_ID:
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

    private void openCountryListBottomSheet() {

        SingleChoiceBottomSheet myBottomSheet =
                new SingleChoiceBottomSheet.Builder<CountryResponse>(new SingleChoiceDialogListener<CountryResponse>() {
                    @Override
                    public void onSpinnerItemClick(int position, CountryResponse selectedItem) {
                        hideKeyboard(SignUpActivity.this);
                        if (selectedCountry != null) {
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

    private void openStateBottomSheet() {

        SingleChoiceBottomSheet myBottomSheet =
                new SingleChoiceBottomSheet.Builder<StateResponse>(new SingleChoiceDialogListener<StateResponse>() {
                    @Override
                    public void onSpinnerItemClick(int position, StateResponse selectedItem) {
                        hideKeyboard(SignUpActivity.this);
                        if (selectedState != null) {
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


}
