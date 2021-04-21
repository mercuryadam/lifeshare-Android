package com.lifeshare.ui.profile;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
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
import com.lifeshare.ui.ImageFullScreenDialogFragment;
import com.lifeshare.ui.ProfileActivity;
import com.lifeshare.ui.save_broadcast.ShowPreviousBroadcastAndChatActivity;
import com.lifeshare.utils.Const;
import com.lifeshare.utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends BaseActivity implements View.OnClickListener, MyDialogCloseListener {

    private static final String TAG = "ViewProfileActivity";
    List<SkuDetails> skuDetails = new ArrayList<>();
    BillingClient billingClient;
    private LinearLayout llMain;
    private RelativeLayout appBar;
    private CircleImageView ivAdd, ivProfile;
    private AppCompatTextView tvChannelName;
    private AppCompatTextView tvEmail;
    private AppCompatTextView tvShortDescription;
    private AppCompatImageView ivBack;
    private AppCompatTextView btnEdit, tvToolbarTitle;
    private AppCompatTextView tvNameOne;
    private AppCompatTextView tvCityName, tvAddress;
    private AppCompatTextView tvStateName;
    private AppCompatTextView tvCountryName;
    private AppCompatTextView tvPhoneNumber;
    private AppCompatButton btnSubscribe;
    private LinearLayout llEmailPhoneCity, llCATitle;
    private FilterRecyclerView rvChannelArchive;
    private ChannelArchiveAdapter channelArchiveAdapter;
    private ArrayList<ChannelArchiveResponse> channelArchiveList = new ArrayList<>();
    private String userId = "";
    private AppCompatTextView tvTotalViewer;
    private boolean isSubscriptionActive = false;
    private PurchasesUpdatedListener purchaseUpdateListener = new PurchasesUpdatedListener() {

        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
            // To be implemented in a later section.
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && purchases != null) {

//                showToast("SUCCESS_1 : " + billingResult.getResponseCode() + " - " + billingResult.getDebugMessage());
                for (Purchase purchase : purchases) {
                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                        if (purchase.getSku().equals(Const.LIFESHARE_LIVE_MONTHLY_SUBSCRIPTION_ID_1)) {
                            getAcknowdgement(purchase);
                        }
                    }
                }

            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
//                showToast("USER_CANCELLED Billing Process");
                // Handle an error caused by a user cancelling the purchase flow.
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                showToast("ITEM ALREADY OWNED");
                // Handle an error caused by a user cancelling the purchase flow.
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_NOT_OWNED) {
                showToast("ITEM NOT OWNED");
                // Handle an error caused by a user cancelling the purchase flow.
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE) {
                showToast("ITEM UNAVAILABLE");
                // Handle an error caused by a user cancelling the purchase flow.
            } else {
                showToast("Billing Error : " + billingResult.getResponseCode() + ":" + billingResult.getDebugMessage());
                // Handle any other error codes.
            }
        }

    };

    private void updatePurchaseToServer(Purchase purchase) {

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
                hideLoading();
                showToast(response.getMessage());
                manageViewForSubscription("1");
                getListChannelArchive();
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
                ivBack.setVisibility(View.GONE);
                btnEdit.setVisibility(View.VISIBLE);
                llEmailPhoneCity.setVisibility(View.VISIBLE);
                llCATitle.setVisibility(View.VISIBLE);
                rvChannelArchive.setVisibility(View.VISIBLE);
                btnSubscribe.setVisibility(View.VISIBLE);
                userId = PreferenceHelper.getInstance().getUser().getUserId();
                checkSubscription();

            } else {

                ivBack.setVisibility(View.VISIBLE);
                btnEdit.setVisibility(View.GONE);
                llEmailPhoneCity.setVisibility(View.GONE);
                ivAdd.setVisibility(View.GONE);
                MyConnectionListResponse data = (MyConnectionListResponse) bundle.getParcelable(Const.USER_DATA);
                btnSubscribe.setVisibility(View.GONE);
                userId = data.getUserId();
                checkSubscription();
            }
            setRecyclerView();
        }

        setUpBillingClient();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void getOtherProfileData() {
        showLoading();
        UserProfileRequest request = new UserProfileRequest();
        request.setUserId(userId);
        WebAPIManager.getInstance().getUserProfile(request, new RemoteCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse response) {
                setOtherUserData(response);
                getListChannelArchive();
                hideLoading();
            }
        });

    }

    private void setOtherUserData(LoginResponse user) {
        userId = user.getUserId();
        tvNameOne.setText(user.getFirstName() + " " + user.getLastName());
        tvChannelName.setText(user.getUsername());
        tvToolbarTitle.setText("@" + user.getUsername());
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
        if (user.getCountry() != null && user.getCountry().getName() != null) {
            if (user.getState() != null && user.getState().getName() != null) {
                if (user.getCity() != null && user.getCity().getName() != null) {
                    tvAddress.setText(user.getCity().getName() + ", " + user.getState().getName() + ", " + user.getCountry().getName());
                }
            }
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
        tvNameOne.setText(user.getFirstName() + " " + user.getLastName());
        tvChannelName.setText(user.getUsername());
        tvToolbarTitle.setText("@" + user.getUsername());
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

        if (user.getCountry() != null && user.getCountry().getName() != null) {
            if (user.getState() != null && user.getState().getName() != null) {
                if (user.getCity() != null && user.getCity().getName() != null) {
                    tvAddress.setText(user.getCity().getName() + "," + user.getState().getName() + "," + user.getCountry().getName());
                }
            }
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
        appBar = (RelativeLayout) findViewById(R.id.appbar_new);
        ivAdd = (CircleImageView) appBar.findViewById(R.id.ivAdd);
        tvToolbarTitle = (AppCompatTextView) appBar.findViewById(R.id.tvToolbarTitle);
        ivAdd.setVisibility(View.VISIBLE);
        tvToolbarTitle.setVisibility(View.VISIBLE);
        ivAdd.setOnClickListener(this);

        llMain = (LinearLayout) findViewById(R.id.ll_main);
        ivProfile = (CircleImageView) findViewById(R.id.iv_profile);
        tvChannelName = (AppCompatTextView) findViewById(R.id.tv_channel_name);
        tvEmail = (AppCompatTextView) findViewById(R.id.tv_email);
        tvShortDescription = (AppCompatTextView) findViewById(R.id.tv_short_description);
        btnEdit = (AppCompatTextView) appBar.findViewById(R.id.tvBack);
        ivBack = (AppCompatImageView) appBar.findViewById(R.id.ivBack);
        btnEdit.setTextAppearance(this, R.style.TextRegular_colorBlack_size18);
        btnEdit.setText(R.string.edit);

        btnEdit.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        tvNameOne = (AppCompatTextView) findViewById(R.id.tv_name_one);
        tvAddress = (AppCompatTextView) findViewById(R.id.tvAddress);
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


        tvTotalViewer = (AppCompatTextView) findViewById(R.id.tv_total_viewer);
    }

    private void setRecyclerView() {
        rvChannelArchive.setLayoutManager(new GridLayoutManager(this, 1));
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

                        if (view.getId() == R.id.ivBackGround && !item.getImage().isEmpty()) {

                            DialogFragment dialogFragment = ImageFullScreenDialogFragment.newInstance(item.getImage());
                            dialogFragment.show(getSupportFragmentManager(), "ImageFullScreenDialogFragment");

                        } else if (view.getId() == R.id.tvChannelName) {

                            if (!item.getLink().trim().isEmpty()) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                String url = item.getLink();
                                if (!url.startsWith("http://") && !url.startsWith("https://"))
                                    url = "http://" + url;
                                i.setData(Uri.parse(url));
                                startActivity(i);
                            }

                        }
                    } else {

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
            case R.id.ivBack:
                onBackPressed();
                break;
            case R.id.tvBack:

                Intent intent = new Intent(ViewProfileActivity.this, ProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(Const.PROFILE, Const.MY_PROFILE);
                intent.putExtras(bundle);
                startActivity(intent);
                break;

            case R.id.llCATitle:
            case R.id.ivAdd:
                if (PreferenceHelper.getInstance().getUser().getUserId().equals(userId)) {
                    // Create and show the dialog.
                    DialogFragment newFragment = AddChannelArchiveDialogFragment.newInstance(this);
                    newFragment.show(getSupportFragmentManager(), "dialog");
                }

                break;

            case R.id.btnSubscribe:
//                showToast("SKU size : " + (skuDetails.size()));
                if (skuDetails.size() > 0) {
                    for (int i = 0; i < skuDetails.size(); i++) {
                        if (skuDetails.get(i).getSku().equals(Const.LIFESHARE_LIVE_MONTHLY_SUBSCRIPTION_ID_1)) {
                            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                    .setSkuDetails(skuDetails.get(i))
                                    .setObfuscatedAccountId(PreferenceHelper.getInstance().getUser().getUserId())
                                    .setObfuscatedProfileId(PreferenceHelper.getInstance().getUser().getUsername())
                                    .build();
                            billingClient.launchBillingFlow(ViewProfileActivity.this, billingFlowParams);
                            break;
                        }
                    }
                }
                break;
        }
    }

    private void getListChannelArchive() {
        if (!userId.equals(PreferenceHelper.getInstance().getUser().getUserId()) && !isSubscriptionActive) {
            llCATitle.setVisibility(View.GONE);
            return;
        }

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

    private void checkSubscription() {
        showLoading();
        WebAPIManager.getInstance().checkSubscription(new RemoteCallback<CheckSubscriptionResponse>(this) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(CheckSubscriptionResponse response) {
                manageViewForSubscription(response.getStatus());
                hideLoading();
                getOtherProfileData();
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

    private void manageViewForSubscription(String status) {
        if (status.equalsIgnoreCase("1")) {
            isSubscriptionActive = true;
            btnSubscribe.setText(R.string.already_subscribed);
            btnSubscribe.setEnabled(false);
        } else {
            isSubscriptionActive = false;
            btnSubscribe.setText(R.string.subscribe);
            btnSubscribe.setEnabled(true);
        }
        if (!userId.equals(PreferenceHelper.getInstance().getUser().getUserId())) {
            btnSubscribe.setVisibility(View.GONE);
        } else {
            if (isSubscriptionActive) {
                btnSubscribe.setVisibility(View.GONE);
            } else {
                btnSubscribe.setText(R.string.subscribe);
                btnSubscribe.setVisibility(View.VISIBLE);
            }
        }
    }

    private void deleteChannelArchive(Integer id) {

        showLoading();
        DeleteArchivesRequest request = new DeleteArchivesRequest();
        request.setId(String.valueOf(id));

        WebAPIManager.getInstance().deleteChannelArchive(request, new RemoteCallback<CommonResponse>(this) {
            @Override
            public void onSuccess(CommonResponse response) {
                hideLoading();
                getListChannelArchive();
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
        getListChannelArchive();
    }

    private void getAcknowdgement(Purchase purchase) {
        showLoading();
        AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();

        billingClient.acknowledgePurchase(params, new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                Log.v(TAG, "onAcknowledgePurchaseResponse: " + purchase.getOriginalJson());
                updatePurchaseToServer(purchase);
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
                    skuList.add(Const.LIFESHARE_LIVE_MONTHLY_SUBSCRIPTION_ID_1); // SKU Id
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
