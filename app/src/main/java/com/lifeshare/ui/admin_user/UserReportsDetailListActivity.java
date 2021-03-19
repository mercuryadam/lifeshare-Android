package com.lifeshare.ui.admin_user;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.lifeshare.network.response.ReportDetailListResponse;
import com.lifeshare.network.response.ReportListResponse;
import com.lifeshare.utils.Const;

import java.util.ArrayList;

public class UserReportsDetailListActivity extends BaseActivity implements View.OnClickListener, BaseRecyclerListener<ReportDetailListResponse> {

    private FilterRecyclerView recyclerView;
    private AppCompatTextView tvNoData;
    private AppCompatTextView tvTitle;
    private UserReportsDetailListAdapter adapter;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_user_detail_list);
        initView();
        if (getIntent() != null && getIntent().getExtras() != null) {
            ReportListResponse userData = getIntent().getExtras().getParcelable(Const.USER_DATA);
            if (userData.getUserId() != null && !TextUtils.isEmpty(userData.getUserId())) {
                String toolbarTitle = userData.getFirstName() + " " + userData.getLastName() + " ( Complains : " + userData.getTotalAbuse() + " ) ";
                tvTitle.setText(toolbarTitle);
                getdata(userData);
            }
        }

    }

    private void initView() {

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.reports));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = (FilterRecyclerView) findViewById(R.id.recyclerView);
        tvNoData = (AppCompatTextView) findViewById(R.id.tv_no_data);
        tvTitle = (AppCompatTextView) findViewById(R.id.tvTitle);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new UserReportsDetailListAdapter(this);
        recyclerView.setEmptyMsgHolder(tvNoData);
        recyclerView.setAdapter(adapter);

    }

    private void getdata(ReportListResponse user) {
        if (!checkInternetConnection()) {
            return;
        }

        DeleteUserRequest request = new DeleteUserRequest();
        request.setUserId(user.getUserId());
        showLoading();
        WebAPIManager.getInstance().getAllReportForUserAgora(request, new RemoteCallback<ArrayList<ReportDetailListResponse>>(this) {
            @Override
            public void onSuccess(ArrayList<ReportDetailListResponse> response) {
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
        recyclerView.showEmptyDataView(getString(R.string.no_connection_available));
    }

    @Override
    public void onRecyclerItemClick(View view, int position, ReportDetailListResponse item) {

    }


}
