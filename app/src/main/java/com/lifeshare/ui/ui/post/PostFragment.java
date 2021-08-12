package com.lifeshare.ui.ui.post;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

    View rootView;
    RecyclerView rvAllPost;
    AllPostAdapter allPostAdapter;
    private ArrayList<ChannelArchiveResponse> channelArchiveList = new ArrayList<>();
    private final int totalPages = 10;
    private int pageNo = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private SwipeRefreshLayout swipeRefreshLayoutRegister;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        swipeRefreshLayoutRegister = rootView.findViewById(R.id.swipeRefreshLayoutRegister);
        LinearLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        rvAllPost.setLayoutManager(layoutManager);
        allPostAdapter = new AllPostAdapter();
        rvAllPost.setAdapter(allPostAdapter);

        rvAllPost.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                if (!isLoading && !isLastPage) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= 10) {
                        pageNo++;
                        isLoading = true;
                        getPostList(false);
                    }
                }
            }
        });
        getPostList(false);
        swipeRefreshLayoutRegister.setOnRefreshListener(() -> {
            pageNo = 0;
            getPostList(true);
            swipeRefreshLayoutRegister.setRefreshing(false);
        });
    }

    private void getPostList(boolean isRefresh) {
        showLoading();
        AllPostRequest request = new AllPostRequest();
        request.setUserId(PreferenceHelper.getInstance().getUser().getUserId());
        request.setAll("1");
        request.setPageNo(String.valueOf(pageNo));
        WebAPIManager.getInstance().allPostList(request, new RemoteCallback<ArrayList<ChannelArchiveResponse>>() {
            @Override
            public void onSuccess(ArrayList<ChannelArchiveResponse> response) {
                channelArchiveList = response;
                allPostAdapter.addItems(channelArchiveList, isRefresh);
                if (channelArchiveList.isEmpty()) {
                    isLastPage = true;
                } else {
                    isLastPage = false;
                }
                isLoading = false;
                hideLoading();
            }

            @Override
            public void onEmptyResponse(String message) {
                hideLoading();
                isLastPage = false;
            }
        });

    }
}