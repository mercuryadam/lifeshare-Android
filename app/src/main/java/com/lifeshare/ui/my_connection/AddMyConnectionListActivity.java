package com.lifeshare.ui.my_connection;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lifeshare.BaseActivity;
import com.lifeshare.R;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.CustomSearchView;
import com.lifeshare.customview.recyclerview.FilterRecyclerView;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.BlockUnblockRequest;
import com.lifeshare.network.request.InvitationRequest;
import com.lifeshare.network.request.SearchUserRequest;
import com.lifeshare.network.response.CommonResponse;
import com.lifeshare.network.response.SearchUserResponse;

import java.util.ArrayList;

public class AddMyConnectionListActivity extends BaseActivity implements BaseRecyclerListener<SearchUserResponse> {

    private FilterRecyclerView recyclerView;
    private AppCompatTextView tvNoData;
    private AddMyConnectionListAdapter adapter;
    private String searchtext = "";
    private CustomSearchView searchView;
    private RelativeLayout appBar;
    private AppCompatTextView tvToolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_my_connection);
        initView();
    }

    private void initView() {

        appBar = (RelativeLayout) findViewById(R.id.appbar_new);
        tvToolbarTitle = (AppCompatTextView) appBar.findViewById(R.id.tvToolbarTitle);
        tvToolbarTitle.setVisibility(View.VISIBLE);
        tvToolbarTitle.setText(R.string.search_connection);

        recyclerView = (FilterRecyclerView) findViewById(R.id.recyclerView);
        tvNoData = (AppCompatTextView) findViewById(R.id.tv_no_data);
        searchView = (CustomSearchView) findViewById(R.id.searchView);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new AddMyConnectionListAdapter(this);
        recyclerView.setEmptyMsgHolder(tvNoData);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.trim().length() > 0) {
                    searchtext = query;
                    getdata();
                } else {
                    showToast(getResources().getString(R.string.search_text_blank_message));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.requestFocus();

    }

    private void getdata() {
        if (!checkInternetConnection()) {
            return;
        }

        searchView.clearFocus();
        showLoading();
        SearchUserRequest request = new SearchUserRequest();
        request.setSearch(searchtext);
        WebAPIManager.getInstance().searchUser(request, new RemoteCallback<ArrayList<SearchUserResponse>>(this) {
            @Override
            public void onSuccess(ArrayList<SearchUserResponse> response) {
                hideLoading();
                adapter.removeAllItems();
                adapter.addItems(response);
            }

            @Override
            public void onEmptyResponse(String message) {
                hideLoading();
                adapter.removeAllItems();
                super.onEmptyResponse(message);
            }
        });

    }

    @Override
    public void showEmptyDataView(int resId) {
        recyclerView.showEmptyDataView(getString(resId));
    }

    @Override
    public void onRecyclerItemClick(View view, int position, SearchUserResponse item) {
        if (view.getId() == R.id.tv_block_user) {
            callBlockUser(item);
        } else if (view.getId() == R.id.tv_unblock_user) {
            callUnBlockUser(item);
        } else if (view.getId() == R.id.tv_send_request) {
            otherDialog(AddMyConnectionListActivity.this
                    , getResources().getString(R.string.send_request_message, item.getFirstName() + " " + item.getLastName())
                    , getResources().getString(R.string.yes)
                    , getResources().getString(R.string.no), new DismissListenerWithStatus() {
                        @Override
                        public void onDismissed(String message) {
                            if (message.equalsIgnoreCase(getResources().getString(R.string.yes))) {
                                sendInvitation(item);
                            }
                        }
                    });
        }
    }

    private void callBlockUser(SearchUserResponse item) {

        if (!checkInternetConnection()) {
            return;
        }

        showLoading();
        BlockUnblockRequest request = new BlockUnblockRequest();
        request.setUserId(item.getUserId());
        WebAPIManager.getInstance().blockUser(request, new RemoteCallback<CommonResponse>(this) {
            @Override
            public void onSuccess(CommonResponse response) {
                hideLoading();
                showToast(response.getMessage());
                getdata();
            }
        });


    }

    private void callUnBlockUser(SearchUserResponse item) {

        if (!checkInternetConnection()) {
            return;
        }

        showLoading();
        BlockUnblockRequest request = new BlockUnblockRequest();
        request.setUserId(item.getUserId());
        WebAPIManager.getInstance().unblockUser(request, new RemoteCallback<CommonResponse>(this) {
            @Override
            public void onSuccess(CommonResponse response) {
                hideLoading();
                showToast(response.getMessage());
                getdata();
            }
        });


    }

    private void sendInvitation(SearchUserResponse item) {
        if (!checkInternetConnection()) {
            return;
        }

        showLoading();
        InvitationRequest request = new InvitationRequest();
        request.setToId(item.getUserId());
        WebAPIManager.getInstance().sendInvitaion(request, new RemoteCallback<CommonResponse>(this) {
            @Override
            public void onSuccess(CommonResponse response) {
                hideLoading();
                showToast(response.getMessage());
            }
        });
    }
}
