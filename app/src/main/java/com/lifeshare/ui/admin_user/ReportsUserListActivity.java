package com.lifeshare.ui.admin_user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lifeshare.BaseActivity;
import com.lifeshare.R;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterRecyclerView;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.DeleteUserRequest;
import com.lifeshare.network.response.CommonResponse;
import com.lifeshare.network.response.ReportListResponse;
import com.lifeshare.utils.Const;

import java.util.ArrayList;

public class ReportsUserListActivity extends BaseActivity implements View.OnClickListener, BaseRecyclerListener<ReportListResponse> {

    private FilterRecyclerView recyclerView;
    private AppCompatTextView tvNoData;
    private ReportsUserListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_user_list);
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

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ReportsUserListAdapter(this);
        recyclerView.setEmptyMsgHolder(tvNoData);
        recyclerView.setAdapter(adapter);

    }

    private void getdata() {
        if (!checkInternetConnection()) {
            return;
        }

        showLoading();
        WebAPIManager.getInstance().getReportedUserList(new RemoteCallback<ArrayList<ReportListResponse>>(this) {
            @Override
            public void onSuccess(ArrayList<ReportListResponse> response) {
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
        }
    }

    @Override
    public void showEmptyDataView(int resId) {
        recyclerView.showEmptyDataView(getString(R.string.no_report_available));
    }

    @Override
    public void onRecyclerItemClick(View view, int position, ReportListResponse item) {
        if (view.getId() == R.id.tv_delete) {
            otherDialog(ReportsUserListActivity.this
                    , getResources().getString(R.string.delete_user_message, item.getFirstName() + " " + item.getLastName())
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

            Intent intent = new Intent(this, UserReportsDetailListActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable(Const.USER_DATA, item);
            intent.putExtras(bundle);
            startActivity(intent);

        }
    }

    private void removeMyConnection(ReportListResponse item, int position) {
        if (!checkInternetConnection()) {
            return;
        }

        showLoading();
        DeleteUserRequest request = new DeleteUserRequest();
        request.setUserId(item.getUserId());
        WebAPIManager.getInstance().deleteUser(request, new RemoteCallback<CommonResponse>(this) {
            @Override
            public void onSuccess(CommonResponse response) {
                hideLoading();
                showToast(response.getMessage());
                adapter.removeItemAt(position);
            }
        });
    }


}
