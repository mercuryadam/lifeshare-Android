package com.lifeshare.ui.my_connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lifeshare.BaseActivity;
import com.lifeshare.R;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterRecyclerView;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.DeleteConnectionRequest;
import com.lifeshare.network.response.CommonResponse;
import com.lifeshare.network.response.MyConnectionListResponse;
import com.lifeshare.ui.profile.ViewProfileActivity;
import com.lifeshare.utils.Const;

import java.util.ArrayList;

public class MyConnectionListActivity extends BaseActivity implements View.OnClickListener, BaseRecyclerListener<MyConnectionListResponse> {

    private FilterRecyclerView recyclerView;
    private AppCompatTextView tvNoData;
    private FloatingActionButton fabAdd;
    private MyConnectionListAdapter adapter;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getdata();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_connection_list_new);
        initView();
        getdata();
    }

    private void initView() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.my_connection);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = (FilterRecyclerView) findViewById(R.id.recyclerView);
        tvNoData = (AppCompatTextView) findViewById(R.id.tv_no_data);
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new MyConnectionListAdapter(this);
        recyclerView.setEmptyMsgHolder(tvNoData);
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(this);
    }

    private void getdata() {
        if (!checkInternetConnection()) {
            return;
        }

        showLoading();
        WebAPIManager.getInstance().getMyConnectionList(new RemoteCallback<ArrayList<MyConnectionListResponse>>(this) {
            @Override
            public void onSuccess(ArrayList<MyConnectionListResponse> response) {
                adapter.removeAllItems();
                adapter.addItems(response);
                hideLoading();
            }

            @Override
            public void onEmptyResponse(String message) {
                super.onEmptyResponse(message);
                hideLoading();
                adapter.removeAllItems();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabAdd:
                startActivity(new Intent(MyConnectionListActivity.this, AddMyConnectionListActivity.class));
                break;
        }
    }

    @Override
    public void showEmptyDataView(int resId) {
        recyclerView.showEmptyDataView(getString(R.string.no_connection_available));
    }

    @Override
    public void onRecyclerItemClick(View view, int position, MyConnectionListResponse item) {
        if (view.getId() == R.id.iv_delete) {
            otherDialog(MyConnectionListActivity.this
                    , getResources().getString(R.string.remove_from_connection_message, item.getFirstName() + " " + item.getLastName())
                    , getResources().getString(R.string.yes)
                    , getResources().getString(R.string.no), new DismissListenerWithStatus() {
                        @Override
                        public void onDismissed(String message) {
                            if (message.equalsIgnoreCase(getResources().getString(R.string.yes))) {
                                removeMyConnection(item, position);
                            }
                        }
                    });
        } else {
            Intent intent = new Intent(MyConnectionListActivity.this, ViewProfileActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(Const.PROFILE, Const.OTHER_PROFILE);
            bundle.putSerializable(Const.USER_DATA, item);
            intent.putExtras(bundle);
            startActivity(intent);

        }
    }

    private void removeMyConnection(MyConnectionListResponse item, int position) {
        if (!checkInternetConnection()) {
            return;
        }

        showLoading();
        DeleteConnectionRequest request = new DeleteConnectionRequest();
        request.setToId(item.getUserId());
        WebAPIManager.getInstance().deleteMyConnection(request, new RemoteCallback<CommonResponse>() {
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
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(Const.INVITATION_ACCEPT_ACTION));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }
}
