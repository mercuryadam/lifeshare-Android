package com.lifeshare.ui.invitation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lifeshare.BaseActivity;
import com.lifeshare.R;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterRecyclerView;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.AcceptInvitation;
import com.lifeshare.network.request.RejectInvitationRequest;
import com.lifeshare.network.response.CommonResponse;
import com.lifeshare.network.response.InvitationListResponse;
import com.lifeshare.utils.Const;

import java.util.ArrayList;

public class MyInvitationListActivity extends BaseActivity implements BaseRecyclerListener<InvitationListResponse> {

    private static final String TAG = "MyInvitationListActivit";
    private FilterRecyclerView recyclerView;
    private AppCompatTextView tvNoData;
    private MyInvitationListAdapter adapter;
    private RelativeLayout appBar;
    private AppCompatTextView tvToolbarTitle;


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getdata();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ivitation_list_new);
        initView();
    }

    private void initView() {
        appBar = (RelativeLayout) findViewById(R.id.appbar_new);
        tvToolbarTitle = (AppCompatTextView) appBar.findViewById(R.id.tvToolbarTitle);
        tvToolbarTitle.setVisibility(View.VISIBLE);
        tvToolbarTitle.setText(R.string.pending_requests);

        recyclerView = (FilterRecyclerView) findViewById(R.id.recyclerView);
        tvNoData = (AppCompatTextView) findViewById(R.id.tv_no_data);


        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new MyInvitationListAdapter(this);
        recyclerView.setEmptyMsgHolder(tvNoData);
        recyclerView.setAdapter(adapter);
        getdata();
    }

    private void getdata() {
        if (!checkInternetConnection()) {
            return;
        }
        showLoading();
        WebAPIManager.getInstance().getInvitationList(new RemoteCallback<ArrayList<InvitationListResponse>>(this) {
            @Override
            public void onSuccess(ArrayList<InvitationListResponse> response) {
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
        Log.v(TAG, "showEmptyDataView: ");
        recyclerView.showEmptyDataView(getString(R.string.no_invitaion_available));
    }

    @Override
    public void onRecyclerItemClick(View view, int position, InvitationListResponse item) {
        if (view.getId() == R.id.tv_accept) {
            otherDialog(MyInvitationListActivity.this
                    , getResources().getString(R.string.add_in_connection_message, item.getFirstName() + " " + item.getLastName())
                    , getResources().getString(R.string.yes)
                    , getResources().getString(R.string.no), new DismissListenerWithStatus() {
                        @Override
                        public void onDismissed(String message) {
                            if (message.equalsIgnoreCase(getResources().getString(R.string.yes))) {
                                acceptInvitation(item, position);
                            }
                        }
                    });
        }
        if (view.getId() == R.id.tv_decline) {
            otherDialog(MyInvitationListActivity.this
                    , getResources().getString(R.string.reject_connection_message, item.getFirstName() + " " + item.getLastName())
                    , getResources().getString(R.string.yes)
                    , getResources().getString(R.string.no), new DismissListenerWithStatus() {
                        @Override
                        public void onDismissed(String message) {
                            if (message.equalsIgnoreCase(getResources().getString(R.string.yes))) {
                                rejectInvitation(item, position);
                            }
                        }
                    });
        }
    }

    private void acceptInvitation(InvitationListResponse item, int position) {
        if (!checkInternetConnection()) {
            return;
        }

        showLoading();
        AcceptInvitation invitation = new AcceptInvitation();
        invitation.setFromId(item.getUserId());
        WebAPIManager.getInstance().acceptInvitaion(invitation, new RemoteCallback<CommonResponse>(this) {
            @Override
            public void onSuccess(CommonResponse response) {
                hideLoading();
                showToast(response.getMessage());
                adapter.removeItemAt(position);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(Const.NEW_INVITATION_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void rejectInvitation(InvitationListResponse item, int position) {
        if (!checkInternetConnection()) {
            return;
        }

        showLoading();
        RejectInvitationRequest invitation = new RejectInvitationRequest();
        invitation.setToId(item.getUserId());
        WebAPIManager.getInstance().rejectInvitation(invitation, new RemoteCallback<CommonResponse>(this) {
            @Override
            public void onSuccess(CommonResponse response) {
                hideLoading();
                showToast(response.getMessage());
                adapter.removeItemAt(position);
            }
        });
    }
}
