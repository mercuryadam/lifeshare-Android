package com.lifeshare.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.lifeshare.BaseActivity;
import com.lifeshare.BuildConfig;
import com.lifeshare.R;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.CheckSocialMediaRequest;
import com.lifeshare.network.request.LoginRequest;
import com.lifeshare.network.request.SignUpRequest;
import com.lifeshare.network.response.LoginResponse;
import com.lifeshare.utils.AuthenticationDialog;
import com.lifeshare.utils.Const;
import com.lifeshare.utils.PreferenceHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends BaseActivity implements View.OnClickListener, AuthenticationDialog.AuthenticationListener {

    private static final String TAG = "LoginActivity";
    private AppCompatEditText etEmail;
    private AppCompatEditText etPassword;
    private AppCompatButton btnLogin;
    private AppCompatImageView btnInstaLogin;
    private AppCompatTextView tvForgotPassword;
    private AppCompatButton btnSignUp;
    private RelativeLayout rlMain;
    private static final Integer RC_SIGN_IN = 1;
    private GoogleSignInClient mGoogleSignInClient;
    LoginButton fbLoginButton;
    CallbackManager callbackManager;
    AlertDialog alertDialog;


    public static boolean isValidEmail(CharSequence target) {
        return (Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_login_new);

        //Google signin
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        initView();
    }

    private void initView() {
        etEmail = (AppCompatEditText) findViewById(R.id.et_email);
        etPassword = (AppCompatEditText) findViewById(R.id.et_password);
        btnLogin = (AppCompatButton) findViewById(R.id.btn_login);
        btnInstaLogin = (AppCompatImageView) findViewById(R.id.insta_login_button);
        SignInButton signInButton = findViewById(R.id.google_login_button);
        signInButton.setOnClickListener(this);


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


        btnInstaLogin.setOnClickListener(this);
        fbLoginButton = findViewById(R.id.fb_login_button);

        fbLoginButton.setReadPermissions(Arrays.asList("email", "public_profile"));
        callbackManager = CallbackManager.Factory.create();

        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                //loginResult.getAccessToken();
                //loginResult.getRecentlyDeniedPermissions()
                //loginResult.getRecentlyGrantedPermissions()
                boolean loggedOut = AccessToken.getCurrentAccessToken() == null;

                if (!loggedOut) {
                    // Picasso.with(this).load(Profile.getCurrentProfile().getProfilePictureUri(200, 200)).into(imageView);
                    Log.d("TAG", "Username is: " + Profile.getCurrentProfile().getName());

                    //Using Graph API
                    getUserProfile(AccessToken.getCurrentAccessToken());
                }

            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                showToast(exception.getMessage());

                // App code
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

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
            case R.id.insta_login_button:
                AuthenticationDialog authenticationDialog = new AuthenticationDialog(this, this);
                authenticationDialog.setCancelable(true);
                authenticationDialog.show();
                break;
            case R.id.google_login_button:
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

                if (account != null) {
                    logInWithSocialMedia(Const.GOOGLE_LOG_IN, account.getId(), account.getEmail(), account.getGivenName(), account.getFamilyName());
                } else {
                    googleSignIn();
                }
                break;
        }
    }


    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            logInWithSocialMedia(Const.GOOGLE_LOG_IN, account.getId(), account.getEmail(), account.getGivenName(), account.getFamilyName());

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
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


    //FaceBook
    private void getUserProfile(AccessToken currentAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(
                currentAccessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d("TAG", object.toString());
                        try {
                            String first_name = object.getString("first_name");
                            String last_name = object.getString("last_name");
                            String email = object.getString("email");
                            String id = object.getString("id");
                            String image_url = "https://graph.facebook.com/" + id + "/picture?type=normal";
                            logInWithSocialMedia(Const.FB_LOG_IN, id, email, first_name, last_name);
                           /* txtEmail.setText(email);
                            Glide.with(LoginActivity.this).load(image_url).into(imageView);*/

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name,last_name,email,id");
        request.setParameters(parameters);
        request.executeAsync();

    }


    @Override
    public void onTokenReceived(String auth_token) {
        if (auth_token != null) {
            Log.d("AuthenticationDialog", auth_token);
            getUserInfoByAccessToken(auth_token);
        }
    }

    private void getUserInfoByAccessToken(String token) {
        new InstaAuthCodeAPI(token).execute();
    }

    private class InstaAuthCodeAPI extends AsyncTask<Void, String, String> {
        String token;

        public InstaAuthCodeAPI(String token) {
            this.token = token;
        }

        @Override
        protected String doInBackground(Void... params) {
            showLoading();
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("client_id", getString(R.string.client_id))
                    .addFormDataPart("client_secret", getString(R.string.client_secret))
                    .addFormDataPart("grant_type", "authorization_code")
                    .addFormDataPart("redirect_uri", getString(R.string.redirect_url))
                    .addFormDataPart("code", token)
                    .build();

            Request request = new Request.Builder()
                    .url(getString(R.string.instagram_auth))
                    .post(requestBody)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                hideLoading();
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.has("user_id")) {
                        new RequestInstagramUserNameAPI(jsonObject.getString("user_id"), jsonObject.getString("access_token")).execute();
                    }else {
                        hideLoading();
                        Toast toast = Toast.makeText(getApplicationContext(), "Login error!", Toast.LENGTH_LONG);
                        toast.show();
                    }
                } catch (JSONException e) {
                    hideLoading();
                    e.printStackTrace();
                }
            }else {
                hideLoading();
                Toast toast = Toast.makeText(getApplicationContext(), "Login error!", Toast.LENGTH_LONG);
                toast.show();
            }
        }


    }

    private class RequestInstagramUserNameAPI extends AsyncTask<Void, String, String> {
        String access_token;
        String userID;

        public RequestInstagramUserNameAPI(String userID, String access_token) {
            this.access_token = access_token;
            this.userID = userID;
        }

        @Override
        protected String doInBackground(Void... params) {

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(getString(R.string.get_user_info_url, userID) + access_token)
                    .get()
                    .build();

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (IOException e) {
                hideLoading();
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.has("id") && jsonObject.has("username")) {
                        new RequestInstagramFirstNameAPI(jsonObject.getString("id"), jsonObject.getString("username")).execute();

                    }else {
                        hideLoading();
                        Toast toast = Toast.makeText(getApplicationContext(), "Login error!", Toast.LENGTH_LONG);
                        toast.show();
                    }
                } catch (JSONException e) {
                    hideLoading();
                    e.printStackTrace();
                }
            } else {
                hideLoading();
                Toast toast = Toast.makeText(getApplicationContext(), "Login error!", Toast.LENGTH_LONG);
                toast.show();
            }
        }


    }

    private class RequestInstagramFirstNameAPI extends AsyncTask<Void, String, String> {
        String username;
        String userID;

        public RequestInstagramFirstNameAPI(String userID, String username) {
            this.userID = userID;
            this.username = username;
        }

        @Override
        protected String doInBackground(Void... params) {

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("https://www.instagram.com/" + username + "/?__a=1")
                    .header("user-agent", "OkHttp Headers.java")
                    .get()
                    .build();
            try {
                Response response = client.newCall(request).execute();
                Log.d("AuthenticationDialog", "FIRSTNAME RESPONSE CODE:::" + response.code());

                return response.body().string();
            } catch (IOException e) {
                hideLoading();
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.has("graphql")) {
                        JSONObject graphql = new JSONObject(jsonObject.getString("graphql"));
                        if (graphql.has("user")) {
                            JSONObject user = new JSONObject(graphql.getString("user"));


                            if (user.has("full_name")) {
                                String fullName = user.getString("full_name");

                                String[] arrayFullName = fullName.split(" ");

                                if (arrayFullName.length == 2) {
                                    logInWithSocialMedia(Const.INSTAGRAM_LOG_IN, userID, "", arrayFullName[0], arrayFullName[1]);
                                }
                            }
                        }
                    }else {
                        hideLoading();
                        Toast toast = Toast.makeText(getApplicationContext(), "Login error!", Toast.LENGTH_LONG);
                        toast.show();
                    }
                } catch (JSONException e) {
                    hideLoading();
                    e.printStackTrace();
                }
            } else {
                hideLoading();
                Toast toast = Toast.makeText(getApplicationContext(), "Login error!", Toast.LENGTH_LONG);
                toast.show();
            }
        }


    }

    private void logInWithSocialMedia(String loginType, String socialMediaID, String email, String fName, String lName) {
        if (!checkInternetConnection()) {
            return;
        }
        showLoading();
        CheckSocialMediaRequest request = new CheckSocialMediaRequest();
        request.setDeviceId(Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID));
        request.setDeviceToken(PreferenceHelper.getInstance().getFcmToken());
        request.setLoginType(loginType);
        request.setSocialMediaId(socialMediaID);

        WebAPIManager.getInstance().checkSocialMedia(request, new RemoteCallback<LoginResponse>(this) {
            @Override
            public void onSuccess(LoginResponse response) {
                hideLoading();
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

            @Override
            public void onFailed(Throwable throwable) {
                super.onFailed(throwable);
                addChannelAndSignUpDialog(loginType, socialMediaID, email, fName, lName);
            }
        });
    }

    private void SignUp(String channelName, String loginType, String socialMediaID, String email, String fName, String lName) {
        if (!checkInternetConnection()) {
            return;
        }
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername(channelName);
        signUpRequest.setLoginType(loginType);
        signUpRequest.setFirstName(fName);
        signUpRequest.setLastName(lName);
        signUpRequest.setSocialMediaID(socialMediaID);
        signUpRequest.setEmail(email);
        signUpRequest.setDeviceId(Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID));
        signUpRequest.setDeviceToken(PreferenceHelper.getInstance().getFcmToken());

        showLoading();

        WebAPIManager.getInstance().signUp(signUpRequest, new RemoteCallback<LoginResponse>(this) {
            @Override
            public void onSuccess(LoginResponse response) {
                hideLoading();
                showToast(response.getMessage());
                PreferenceHelper.getInstance().setUser(response);
                PreferenceHelper.getInstance().setFcmTokenUpdated(false);
                Intent intent = new Intent(LoginActivity.this, TermOfServicesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }


    private void addChannelAndSignUpDialog(String loginType, String socialMediaID, String email, String fName, String lName) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.add_channel_name_doalog, null);
        AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(this, R.style.InvitationDialog);
        mAlertBuilder.setView(promptView);

        final AppCompatEditText etChannel = (AppCompatEditText) promptView.findViewById(R.id.et_username);
        final AppCompatEditText etEmail = (AppCompatEditText) promptView.findViewById(R.id.et_email);
        final AppCompatButton btnSubmit = (AppCompatButton) promptView.findViewById(R.id.btn_sign_up);
        final LinearLayout llEmail = (LinearLayout) promptView.findViewById(R.id.llEmail);

        if (loginType.equals(Const.INSTAGRAM_LOG_IN)) {
            llEmail.setVisibility(View.VISIBLE);
        } else {
            llEmail.setVisibility(View.GONE);
        }

        // create an alert dialog
        alertDialog = mAlertBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        if (alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        alertDialog.show();

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (loginType.equals(Const.INSTAGRAM_LOG_IN)) {

                    if (TextUtils.isEmpty(etChannel.getText().toString().trim())) {
                        showToast(getResources().getString(R.string.please_enter_username));
                    } else if (TextUtils.isEmpty(etEmail.getText().toString().trim())) {
                        showToast(getResources().getString(R.string.please_enter_email));
                    } else {
                        alertDialog.cancel();
                        SignUp(etChannel.getText().toString().trim(), loginType, socialMediaID, etEmail.getText().toString().trim(), fName, lName);
                    }

                } else {
                    if (TextUtils.isEmpty(etChannel.getText().toString().trim())) {
                        showToast(getResources().getString(R.string.please_enter_username));
                    } else {
                        alertDialog.cancel();
                        SignUp(etChannel.getText().toString().trim(), loginType, socialMediaID, email, fName, lName);
                    }
                }


            }
        });
    }
}
