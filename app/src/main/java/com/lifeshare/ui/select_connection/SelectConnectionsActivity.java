package com.lifeshare.ui.select_connection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import com.lifeshare.utils.Const;

import java.util.ArrayList;

public class SelectConnectionsActivity extends BaseActivity implements View.OnClickListener, BaseRecyclerListener<MyConnectionListResponse> {

    private FilterRecyclerView recyclerView;
    private AppCompatTextView tvNoData;
    private FloatingActionButton fabAdd;
    private SelectConnectionListAdapter adapter;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getdata();
        }
    };
    private TextView tvSelectToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_connections);
        initView();
        getdata();
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.select_connection_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.select_all:
                if (item.getTitle().equals(getString(R.string.select_all))) {
                    adapter.selectAll();
                    item.setTitle(getString(R.string.un_select_all));
                } else {
                    adapter.unSelectAll();
                    item.setTitle(getString(R.string.select_all));
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }*/

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
        adapter = new SelectConnectionListAdapter(this);
        recyclerView.setEmptyMsgHolder(tvNoData);
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(this);

        tvSelectToggle = findViewById(R.id.tv_selectAll);
        tvSelectToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tvSelectToggle.getText().equals(getString(R.string.select_all))) {
                    adapter.selectAll();
                    tvSelectToggle.setText(getString(R.string.un_select_all));
                } else {
                    adapter.unSelectAll();
                    tvSelectToggle.setText(getString(R.string.select_all));
                }
            }
        });
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
//                setDummyDAta();
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

    private void setDummyDAta() {
        adapter.addItem(new MyConnectionListResponse("1", "bhavy.koshti", "name", "bhavy", "Koshti", ""));
        adapter.addItem(new MyConnectionListResponse("1", "bhavy.koshti", "name", "bhavy", "Koshti", ""));
        adapter.addItem(new MyConnectionListResponse("1", "bhavy.koshti", "name", "bhavy", "Koshti", ""));
        adapter.addItem(new MyConnectionListResponse("1", "bhavy.koshti", "name", "bhavy", "Koshti", ""));
        adapter.addItem(new MyConnectionListResponse("1", "bhavy.koshti", "name", "bhavy", "Koshti", ""));
        adapter.addItem(new MyConnectionListResponse("1", "bhavy.koshti", "name", "bhavy", "Koshti", ""));
        adapter.addItem(new MyConnectionListResponse("1", "bhavy.koshti", "name", "bhavy", "Koshti", ""));
        adapter.addItem(new MyConnectionListResponse("1", "bhavy.koshti", "name", "bhavy", "Koshti", ""));
        adapter.addItem(new MyConnectionListResponse("1", "bhavy.koshti", "name", "bhavy", "Koshti", ""));
        adapter.addItem(new MyConnectionListResponse("1", "bhavy.koshti", "name", "bhavy", "Koshti", ""));
        adapter.addItem(new MyConnectionListResponse("1", "bhavy.koshti", "name", "bhavy", "Koshti", ""));
        adapter.addItem(new MyConnectionListResponse("1", "bhavy.koshti", "name", "bhavy", "Koshti", ""));
        adapter.addItem(new MyConnectionListResponse("1", "bhavy.koshti", "name", "bhavy", "Koshti", ""));
        adapter.addItem(new MyConnectionListResponse("1", "bhavy.koshti", "name", "bhavy", "Koshti", ""));
        adapter.addItem(new MyConnectionListResponse("1", "bhavy.koshti", "name", "bhavy", "Koshti", ""));
        adapter.addItem(new MyConnectionListResponse("1", "bhavy.koshti", "name", "bhavy", "Koshti", ""));
        adapter.addItem(new MyConnectionListResponse("1", "bhavy.koshti", "name", "bhavy", "Koshti", ""));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabAdd:

                if (adapter.getCheckedItems().size() == 0) {
                    Toast.makeText(this, "Please Select Any item", Toast.LENGTH_SHORT).show();
                } else {
                    for (MyConnectionListResponse checkedItem : adapter.getCheckedItems()) {
//                        Log.e("Bhavy", checkedItem.getUserId());
                    }
                    Intent resultIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Const.SELECTED_USERS, adapter.getCheckedItems());
                    resultIntent.putExtras(bundle);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }

                break;
        }
    }

    @Override
    public void showEmptyDataView(int resId) {
        recyclerView.showEmptyDataView(getString(R.string.no_connection_available));
    }

    @Override
    public void onRecyclerItemClick(View view, int position, MyConnectionListResponse item) {

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