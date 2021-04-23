package com.lifeshare.ui.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lifeshare.BaseFragment;
import com.lifeshare.R;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterRecyclerView;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.DeleteConnectionRequest;
import com.lifeshare.network.response.CommonResponse;
import com.lifeshare.network.response.MyConnectionListResponse;
import com.lifeshare.ui.my_connection.MyConnectionListAdapter;
import com.lifeshare.ui.profile.ViewProfileActivity;
import com.lifeshare.utils.Const;

import java.util.ArrayList;

public class ConnectionFragment extends BaseFragment implements BaseRecyclerListener<MyConnectionListResponse> {

    private FilterRecyclerView recyclerView;
    private AppCompatTextView tvNoData;
    private MyConnectionListAdapter adapter;

    private View rootView;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getdata();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_connection, container, false);
        initView();
        getdata();
        return rootView;
    }

    private void initView() {

        recyclerView = (FilterRecyclerView) rootView.findViewById(R.id.recyclerView);
        tvNoData = (AppCompatTextView) rootView.findViewById(R.id.tv_no_data);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new MyConnectionListAdapter(this);
        recyclerView.setEmptyMsgHolder(tvNoData);
        recyclerView.setAdapter(adapter);

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
    public void showEmptyDataView(int resId) {
        if (isAdded()) {
            recyclerView.showEmptyDataView(getString(R.string.no_connection_available));
        }
    }

    @Override
    public void onRecyclerItemClick(View view, int position, MyConnectionListResponse item) {
        if (view.getId() == R.id.iv_delete) {
            otherDialog(requireContext()
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
            Intent intent = new Intent(requireContext(), ViewProfileActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(Const.PROFILE, Const.OTHER_PROFILE);
            bundle.putParcelable(Const.USER_DATA, item);
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
        WebAPIManager.getInstance().deleteMyConnection(request, new RemoteCallback<CommonResponse>(this) {
            @Override
            public void onSuccess(CommonResponse response) {
                hideLoading();
                showToast(response.getMessage());
                adapter.removeItemAt(position);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver, new IntentFilter(Const.INVITATION_ACCEPT_ACTION));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver);
    }
}


