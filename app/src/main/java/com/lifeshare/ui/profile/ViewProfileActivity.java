package com.lifeshare.ui.profile;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
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
import com.lifeshare.network.request.SaveSubscriptionRequest;
import com.lifeshare.network.request.UserProfileRequest;
import com.lifeshare.network.response.ChannelArchiveResponse;
import com.lifeshare.network.response.CheckSubscriptionResponse;
import com.lifeshare.network.response.CommonResponse;
import com.lifeshare.network.response.LoginResponse;
import com.lifeshare.network.response.MyConnectionListResponse;
import com.lifeshare.ui.ProfileActivity;
import com.lifeshare.ui.save_broadcast.ShowPreviousBroadcastAndChatActivity;
import com.lifeshare.utils.Const;
import com.lifeshare.utils.PreferenceHelper;
import com.lifeshare.utils.Security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends BaseActivity implements View.OnClickListener, MyDialogCloseListener {

    private static final String TAG = "ViewProfileActivity";
    List<SkuDetails> skuDetails = new ArrayList<>();
    BillingClient billingClient;
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
    private AppCompatButton btnSubscribe;
    private LinearLayout llEmailPhoneCity, llCATitle;
    private FilterRecyclerView rvChannelArchive;
    private ChannelArchiveAdapter channelArchiveAdapter;
    private ArrayList<ChannelArchiveResponse> channelArchiveList = new ArrayList<>();
    private ImageView addArchivesFromDialog;
    private String userId = "";
    private AppCompatTextView tvTotalViewer;
    private String base64Key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuRB1IGH8zunjzT4z3A3syZ/biyDUCfajoVQOdJYq41bXa3NI4vlcsqtH6d5+3RRqjiZd9i2ni7igrtL6LktT8a5G5m1ofwGC6Ic5a47mpPWOeu526DZpeJD4/J6dBlddbKaHgQe1Sw1PtAymjcesUQ+vqwbwanIZslgwCyoOCQqWPfLyORGWgTn80QedzB/ZMiY3Diy+73oifRPDS9vPhN0j6TMzVblmPJfU4TS/Vt4tJozpg4AMC4STIgD6LZV/p5fYXKSV83m7wzWNEtzA/Q3d2HR4fbxmCuYeaXGK2mAZgJRZDgoxeZu+a8fSQ3XliXohWXlRSlyza7jUmfVR3wIDAQAB";
    private PurchasesUpdatedListener purchaseUpdateListener = new PurchasesUpdatedListener() {

        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
            // To be implemented in a later section.
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && purchases != null) {

//                showToast("SUCCESS_1 : " + billingResult.getResponseCode() + " - " + billingResult.getDebugMessage());
                for (Purchase purchase : purchases) {
//                    insertIntoFirebase(purchase);

                    if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                        // Invalid purchase
                        // show error to user
                        Log.v(TAG, "onPurchasesUpdated: " + "Got a purchase: " + purchase + "; but signature is bad. Skipping...");
//                        showToast("Got a purchase: " + purchase + "; but signature is bad. Skipping...");
//                        Log.i(TAG, "Got a purchase: " + purchase + "; but signature is bad. Skipping...");
                        return;
                    } else {
                        PreferenceHelper.getInstance().setPurchaseData(purchase);
                        Log.v(TAG, "onPurchasesUpdated: " + "purchase is valid");
//                        showToast("purchase is valid");
                        // purchase is valid
                        // Perform actions

                    }

                    Log.v(TAG, "onPurchasesUpdated: " + purchase.getOriginalJson());
                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {

                        getAcknowdgement(purchase);
                    }

                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
//                showToast("USER_CANCELLED Billing Process");
                // Handle an error caused by a user cancelling the purchase flow.
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                showToast("ITEM_ALREADY_OWNED");
                // Handle an error caused by a user cancelling the purchase flow.
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_NOT_OWNED) {
                showToast("ITEM_NOT_OWNED");
                // Handle an error caused by a user cancelling the purchase flow.
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE) {
                showToast("ITEM_UNAVAILABLE");
                // Handle an error caused by a user cancelling the purchase flow.
            } else {
                showToast("Billing Error : " + billingResult.getResponseCode() + ":" + billingResult.getDebugMessage());
                // Handle any other error codes.
            }
        }

    };
    private boolean isSubscriptionActive = false;

    private boolean verifyValidSignature(String signedData, String signature) {
        try {
            return Security.verifyPurchase(base64Key, signedData, signature);
        } catch (IOException e) {
            Log.e(TAG, "Got an exception trying to validate a purchase: " + e);
            return false;
        }
    }

    private void insertIntoFirebase(Purchase purchase) {

        SaveSubscriptionRequest request = new SaveSubscriptionRequest();
        request.setDeveloperPayload(purchase.getDeveloperPayload());
        request.setOrderId(purchase.getOrderId());
        request.setOriginalJson(purchase.getOriginalJson());
        request.setPackageName(purchase.getPackageName());
        request.setPurchaseToken(purchase.getPurchaseToken());
        request.setSignature(purchase.getSignature());
        request.setSubscriptionId(purchase.getSku());
        request.setObfuscatedAccountId(purchase.getAccountIdentifiers().getObfuscatedAccountId());
        request.setObfuscatedProfileId(purchase.getAccountIdentifiers().getObfuscatedProfileId());
        request.setPurchaseState(String.valueOf(purchase.getPurchaseState()));
        request.setPurchaseTime(String.valueOf(purchase.getPurchaseTime()));
        request.setAutoRenewing(String.valueOf(purchase.isAutoRenewing()));
        request.setAcknowledged(String.valueOf(purchase.isAcknowledged()));
        request.setExpiryTime("");


        WebAPIManager.getInstance().saveSubscription(request, new RemoteCallback<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse response) {

            }
        });
    }

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

        setUpBillingClient();
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
        btnSubscribe = (AppCompatButton) findViewById(R.id.btnSubscribe);
        btnSubscribe.setOnClickListener(this);
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
                    if (item.getType().equals("1")) {
                        if (!item.getLink().trim().isEmpty()) {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            String url = item.getLink();
                            if (!url.startsWith("http://") && !url.startsWith("https://"))
                                url = "http://" + url;
                            i.setData(Uri.parse(url));
                            startActivity(i);
                        }
                    } else {
//                        item.setVideo_url("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
                        Intent intent = new Intent(ViewProfileActivity.this, ShowPreviousBroadcastAndChatActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(Const.CHANNAL_DATA, item);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
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
                    DialogFragment newFragment = AddChannelArchiveDialogFragment.newInstance();
                    newFragment.show(getSupportFragmentManager(), "dialog");
                }

                break;

            case R.id.btnSubscribe:
//                showToast("SKU size : " + (skuDetails.size()));
                if (skuDetails.size() > 0) {
                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetails.get(0))
                            .setObfuscatedAccountId(PreferenceHelper.getInstance().getUser().getUserId())
                            .setObfuscatedProfileId(PreferenceHelper.getInstance().getUser().getUsername())
                            .build();
                    billingClient.launchBillingFlow(ViewProfileActivity.this, billingFlowParams);
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
                checkSubscription();
            }

            @Override
            public void onEmptyResponse(String message) {
                checkSubscription();
            }
        });

    }

    private void checkSubscription() {
        WebAPIManager.getInstance().checkSubscription(new RemoteCallback<CheckSubscriptionResponse>(this) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(CheckSubscriptionResponse response) {
                hideLoading();
                if (response.getStatus().equalsIgnoreCase("1")) {
                    isSubscriptionActive = true;
                    btnSubscribe.setText(R.string.already_subscribed);
                    btnSubscribe.setEnabled(false);
                } else {
                    isSubscriptionActive = false;
                    btnSubscribe.setText(R.string.subscribe);
                    btnSubscribe.setEnabled(true);
                }
            }

            @Override
            public void onEmptyResponse(String message) {
                super.onEmptyResponse(message);
                hideLoading();
                btnSubscribe.setText(R.string.already_subscribed);
                isSubscriptionActive = false;
                btnSubscribe.setText(R.string.subscribe);
                btnSubscribe.setEnabled(true);
            }
        });
    }

    private void deleteChannelArchive(Integer id) {

        showLoading();
        DeleteArchivesRequest request = new DeleteArchivesRequest();
        request.setId(String.valueOf(id));

        WebAPIManager.getInstance().deleteChannelArchive(request, new RemoteCallback<CommonResponse>(this) {
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
/*

    private void checkUserSubScription() {
        if (billingClient != null) {
            Log.v(TAG, "checkUserSubScription: " + billingClient.queryPurchases(BillingClient.SkuType.SUBS).getPurchasesList().size());

            List<Purchase> purchasesList = billingClient.queryPurchases(BillingClient.SkuType.SUBS).getPurchasesList();
            for (int i = 0; i < purchasesList.size(); i++) {
                Log.v(TAG, "checkUserSubScription 1: " + purchasesList.get(i).getAccountIdentifiers().getObfuscatedAccountId());
                Log.v(TAG, "checkUserSubScription 2: " + purchasesList.get(i).getAccountIdentifiers().getObfuscatedProfileId());
                Log.v(TAG, "checkUserSubScription 3: " + purchasesList.get(i).isAcknowledged());
                Log.v(TAG, "checkUserSubScription 4: " + purchasesList.get(i).getOriginalJson());
                if (!purchasesList.get(i).isAutoRenewing()) {
                    // https://play.google.com/store/account/subscriptions?sku=your-sub-product-id&package=your-app-package

                    showToast("There is a problem with your subscription. Click here to go to the\n" +
                            "Google Play subscription settings to fix your payment method");
                }
                if (!purchasesList.get(i).isAcknowledged() && purchasesList.get(i).getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    getAcknowdgement(purchasesList.get(i));
                }
            }
        }
    }
*/

    private void getAcknowdgement(Purchase purchase) {

        AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();

        billingClient.acknowledgePurchase(params, new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                Log.v(TAG, "onAcknowledgePurchaseResponse: " + purchase.getOriginalJson());
                insertIntoFirebase(purchase);
                String debugMessage = billingResult.getDebugMessage();
                int responseCode = billingResult.getResponseCode();
//                showToast("SUCCESS_2 : " + responseCode + " - " + debugMessage);
            }
        });

    }

    private void setUpBillingClient() {
        billingClient = BillingClient.newBuilder(ViewProfileActivity.this)
                .setListener(purchaseUpdateListener)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
//                showToast("Status Code :" + billingResult.getResponseCode() + " - " + billingResult.getDebugMessage());
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.

                    final List<String> skuList = new ArrayList<>();
                    skuList.add("test_id_monthly_1"); // SKU Id
                    SkuDetailsParams params = SkuDetailsParams.newBuilder()
                            .setSkusList(skuList)
                            .setType(BillingClient.SkuType.SUBS)
                            .build();
                    billingClient.querySkuDetailsAsync(params,
                            new SkuDetailsResponseListener() {
                                @Override
                                public void onSkuDetailsResponse(BillingResult billingResult,
                                                                 List<SkuDetails> skuDetailsList) {
//                                    Toast.makeText(ViewProfileActivity.this, "list size - " + skuDetailsList.size(), Toast.LENGTH_SHORT).show();
                                    if (skuDetailsList.size() > 0) {
//                                        Toast.makeText(ViewProfileActivity.this, "SKU - " + skuDetailsList.get(0).getSku(), Toast.LENGTH_SHORT).show();
                                    }
                                    skuDetails = skuDetailsList;

                                }
                            });
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
//                showToast("onBillingServiceDisconnected");
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }


}
