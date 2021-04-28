package com.lifeshare.ui.ui;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.lifeshare.BaseFragment;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.asyncTask.InitTrueTimeAsyncTask;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterRecyclerView;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.GetArchiveListRequest;
import com.lifeshare.network.response.ChannelArchiveResponse;
import com.lifeshare.network.response.StreamUserListResponse;
import com.lifeshare.permission.RuntimeEasyPermission;
import com.lifeshare.ui.DashboardActivity;
import com.lifeshare.ui.ImageFullScreenDialogFragment;
import com.lifeshare.ui.profile.PostAdapter;
import com.lifeshare.ui.save_broadcast.ShowPreviousBroadcastAndChatActivity;
import com.lifeshare.ui.show_broadcast.AgoraShowStreamActivity;
import com.lifeshare.ui.show_broadcast.TwilioStreamUserListAdapter;
import com.lifeshare.utils.Const;
import com.lifeshare.utils.PreferenceHelper;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import io.agora.rtc.video.VideoEncoderConfiguration;

import static com.lifeshare.utils.Const.GET_STREAM_USER_INTERVAL_TIME;

public class HomeFragment extends BaseFragment
        implements View.OnClickListener, RuntimeEasyPermission.PermissionCallbacks {

    View rootView;
    private static final String TAG = "BroadcastAgora";
    private static final int REQUEST_AUDIO_PERM_SHOW_BROADCAST = 1120;
    private final VideoEncoderConfiguration mVEC = new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_840x480,
            VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_24,
            VideoEncoderConfiguration.STANDARD_BITRATE,
            VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE);
    CountDownTimer countDownTimerGetStream;
    AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private String[] permissions_audio = new String[]{Manifest.permission.RECORD_AUDIO};
    private TwilioStreamUserListAdapter adapter;
    private FilterRecyclerView rvFriendBroadcast;
    private AppCompatTextView tvNoFriendStreaminig;
    private ProgressBar progressBarConnectionStreaming;
    private boolean isStreamUpdating = false;
    private RelativeLayout rlToolbar;
    private AppCompatTextView tvToolbarTitle;
    private CircleImageView ivAdd;
    private PostAdapter postAdapter;
    private FilterRecyclerView rvPosts;
    private ArrayList<ChannelArchiveResponse> channelArchiveList = new ArrayList<>();


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ivAdd:
                DashboardActivity activity = (DashboardActivity) getActivity();
                activity.viewProfile();
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
        new InitTrueTimeAsyncTask().execute();
        setStreamingConnection();
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(requireContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        //Banner ad
        mAdView = rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //Interstitial Ad
        mInterstitialAd = new InterstitialAd(requireContext());
        mInterstitialAd.setAdUnitId(getString(R.string.interstial_adv_id));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                Log.v(TAG, "onAdClosed: ");
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.v(TAG, "onAdFailedToLoad: " + loadAdError.getMessage() + ", " + loadAdError.getDomain() + " , " + loadAdError.toString());
            }
        });

        setRecyclerView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void initView() {
        rvPosts = (FilterRecyclerView) rootView.findViewById(R.id.rvPosts);
        tvNoFriendStreaminig = (AppCompatTextView) rootView.findViewById(R.id.tv_no_friend_streaminig);
        rlToolbar = (RelativeLayout) rootView.findViewById(R.id.appbar_new);
        tvToolbarTitle = (AppCompatTextView) rlToolbar.findViewById(R.id.tvToolbarTitle);
        ivAdd = (CircleImageView) rlToolbar.findViewById(R.id.ivAdd);
        tvToolbarTitle.setText(getResources().getString(R.string.app_name));
        tvToolbarTitle.setVisibility(View.VISIBLE);
        ivAdd.setVisibility(View.VISIBLE);
        Glide.with(LifeShare.getInstance())
                .load(PreferenceHelper.getInstance().getUser().getAvatar())
                .apply(new RequestOptions().error(R.drawable.user_placeholder).placeholder(R.drawable.user_placeholder))
                .into(ivAdd);
        ivAdd.setOnClickListener(this);
        rvFriendBroadcast = (FilterRecyclerView) rootView.findViewById(R.id.rv_friend_broadcast);
        progressBarConnectionStreaming = (ProgressBar) rootView.findViewById(R.id.progress_connection_streaming);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        rvFriendBroadcast.setLayoutManager(linearLayoutManager);
        adapter = new TwilioStreamUserListAdapter(new BaseRecyclerListener<StreamUserListResponse>() {
            @Override
            public void showEmptyDataView(int resId) {


            }

            @Override
            public void onRecyclerItemClick(View view, int position, StreamUserListResponse item) {
                if (RuntimeEasyPermission.newInstance(permissions_audio,
                        REQUEST_AUDIO_PERM_SHOW_BROADCAST, "Allow microphone permission").hasPermissions(permissions_audio)) {

                    if (item.getId() != null)
                        if (!item.getId().isEmpty()) {
                            LifeShare.getInstance().clearNotificationById(Integer.parseInt(item.getId()));
                        }
                    playAudio(requireContext(), R.raw.click);
                    Intent intent = new Intent(requireContext(), AgoraShowStreamActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(Const.STREAM_DATA, item);
                    intent.putExtras(bundle);
                    startActivity(intent);

                } else {
                    RuntimeEasyPermission.newInstance(permissions_audio,
                            REQUEST_AUDIO_PERM_SHOW_BROADCAST, "Allow microphone permission").show(getChildFragmentManager());

                }
            }
        });
        rvFriendBroadcast.setEmptyMsgHolder(tvNoFriendStreaminig);
        rvFriendBroadcast.setAdapter(adapter);

    }

    private void setRecyclerView() {
        rvPosts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvPosts.setNestedScrollingEnabled(false);
        postAdapter = new PostAdapter(new BaseRecyclerListener<ChannelArchiveResponse>() {
            @Override
            public void showEmptyDataView(int resId) {

            }

            @Override
            public void onRecyclerItemClick(View view, int position, ChannelArchiveResponse item) {

                if (item.getType().equals("1")) {
                    if (view.getId() == R.id.tvChannelName) {
                        if (!item.getLink().trim().isEmpty()) {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            String url = item.getLink();
                            if (!url.startsWith("http://") && !url.startsWith("https://"))
                                url = "http://" + url;
                            i.setData(Uri.parse(url));
                            startActivity(i);
                        }
                    } else if (!item.getImage().isEmpty()) {
                        DialogFragment dialogFragment = ImageFullScreenDialogFragment.newInstance(item.getImage());
                        dialogFragment.show(getChildFragmentManager(), "ImageFullScreenDialogFragment");
                    }
                } else {
                    Intent intent = new Intent(requireContext(), ShowPreviousBroadcastAndChatActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(Const.CHANNAL_DATA, item);
                    intent.putExtras(bundle);
                    startActivity(intent);

                }

            }
        });
        rvPosts.setAdapter(postAdapter);
        getListChannelArchive();
    }


    private void setStreamingConnection() {

        progressBarConnectionStreaming.setVisibility(View.VISIBLE);
        tvNoFriendStreaminig.setVisibility(View.GONE);
        rvFriendBroadcast.setVisibility(View.GONE);
        getAgoraBroadcastList();
        startGetStreamTimer();
    }

    private void startGetStreamTimer() {
        countDownTimerGetStream = new CountDownTimer(Integer.MAX_VALUE, GET_STREAM_USER_INTERVAL_TIME) {

            @Override
            public void onTick(long l) {
                if (!isStreamUpdating) {
                    getAgoraBroadcastList();
                }
            }

            @Override
            public void onFinish() {

            }
        };
        countDownTimerGetStream.start();
    }

    private void getAgoraBroadcastList() {
        isStreamUpdating = true;
        WebAPIManager.getInstance().getAgoraBroadcastList(new RemoteCallback<ArrayList<StreamUserListResponse>>(this) {
            @Override
            public void onSuccess(ArrayList<StreamUserListResponse> response) {

                isStreamUpdating = false;
                progressBarConnectionStreaming.setVisibility(View.GONE);
                ArrayList<StreamUserListResponse> responseArrayList = new ArrayList<>();
                responseArrayList.clear();
                responseArrayList.addAll(response);
                if (responseArrayList.size() > 0) {
                    adapter.removeAllItems();
                    tvNoFriendStreaminig.setVisibility(View.GONE);
                    rvFriendBroadcast.setVisibility(View.VISIBLE);
                    adapter.addItems(responseArrayList);
                } else {
                    adapter.removeAllItems();
                    tvNoFriendStreaminig.setVisibility(View.VISIBLE);
                    rvFriendBroadcast.setVisibility(View.GONE);
                    tvNoFriendStreaminig.setText(R.string.no_live_streaming_message);
                }
            }

            @Override
            public void onEmptyResponse(String message) {
                super.onEmptyResponse(message);
                progressBarConnectionStreaming.setVisibility(View.GONE);
                isStreamUpdating = false;
                adapter.removeAllItems();
                tvNoFriendStreaminig.setVisibility(View.VISIBLE);
                tvNoFriendStreaminig.setText(R.string.no_live_streaming_message);

            }

            @Override
            public void onFailed(Throwable throwable) {
                isStreamUpdating = false;
                progressBarConnectionStreaming.setVisibility(View.GONE);

            }

            @Override
            public void onInternetFailed() {
                isStreamUpdating = false;
                progressBarConnectionStreaming.setVisibility(View.GONE);
            }

            @Override
            public void onUnauthorized(Throwable throwable) {
                isStreamUpdating = false;
                progressBarConnectionStreaming.setVisibility(View.GONE);

            }
        });

    }

    private void getListChannelArchive() {
        showLoading();
        GetArchiveListRequest request = new GetArchiveListRequest();
        request.setUserId(PreferenceHelper.getInstance().getUser().getUserId());
        WebAPIManager.getInstance().listChannelArchive(request, new RemoteCallback<ArrayList<ChannelArchiveResponse>>() {
            @Override
            public void onSuccess(ArrayList<ChannelArchiveResponse> response) {
                channelArchiveList = response;
                postAdapter.removeAllItems();
                postAdapter.addItems(channelArchiveList);
                hideLoading();
            }

            @Override
            public void onEmptyResponse(String message) {
                hideLoading();
            }
        });

    }


    @Override
    public void onPermissionAllow(int permissionCode) {

    }

    @Override
    public void onPermissionDeny(int permissionCode) {

    }
}

