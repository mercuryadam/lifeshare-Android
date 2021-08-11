package com.lifeshare.ui.ui.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lifeshare.BaseFragment;
import com.lifeshare.R;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.AllPostRequest;
import com.lifeshare.network.response.ChannelArchiveResponse;
import com.lifeshare.utils.PreferenceHelper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PostFragment extends BaseFragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    View rootView;
    RecyclerView rvAllPost;
    AllPostAdapter allPostAdapter;
    private String mParam1;
    private String mParam2;
    private ArrayList<ChannelArchiveResponse> channelArchiveList = new ArrayList<>();

    public static PostFragment newInstance(String param1, String param2) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_post, container, false);
        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    private void initView() {
        rvAllPost = rootView.findViewById(R.id.rvAllPost);
        rvAllPost.setLayoutManager(new GridLayoutManager(getContext(), 2));
        allPostAdapter = new AllPostAdapter();
        rvAllPost.setAdapter(allPostAdapter);
        getPostList();
    }

    private void getPostList() {
        showLoading();
        AllPostRequest request = new AllPostRequest();
        request.setUserId(PreferenceHelper.getInstance().getUser().getUserId());
        request.setAll("1");
        request.setPageNo("0");
        WebAPIManager.getInstance().allPostList(request, new RemoteCallback<ArrayList<ChannelArchiveResponse>>() {
            @Override
            public void onSuccess(ArrayList<ChannelArchiveResponse> response) {
                channelArchiveList = response;
                allPostAdapter.addItems(channelArchiveList);
                hideLoading();
            }

            @Override
            public void onEmptyResponse(String message) {
                hideLoading();
            }
        });

    }
}