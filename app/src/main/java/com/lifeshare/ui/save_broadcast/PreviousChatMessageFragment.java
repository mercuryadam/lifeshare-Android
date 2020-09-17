package com.lifeshare.ui.save_broadcast;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lifeshare.R;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterRecyclerView;
import com.lifeshare.model.ChatMessage;
import com.lifeshare.network.BaseRemoteCallback;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.ChatHistoryRequest;

import java.util.ArrayList;

public class PreviousChatMessageFragment extends Fragment implements View.OnClickListener, BaseRecyclerListener<ChatMessage> {

    private static final String TAG = "PreviousChatMessageFrag";
    private static final String REFERENCE_ID = "REFERENCE_ID";
    int pageNo = 1;
    String roomId = "";
    boolean isLoading = false;
    int ITEM_OFFSET = 5;
    String referenceId = "";
    private FilterRecyclerView rvMessage;
    private PreviousChatMessageListAdapter adapter;

    public PreviousChatMessageFragment() {
        // Required empty public constructor
    }

    public static PreviousChatMessageFragment newInstance(String referenceId) {
        PreviousChatMessageFragment fragment = new PreviousChatMessageFragment();
        Bundle bundle = new Bundle();
        bundle.putString(REFERENCE_ID, referenceId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_show_chat_history, container, false);
        initView(view);
        referenceId = getArguments().getString(REFERENCE_ID);
        if (!referenceId.isEmpty()) {
            getData();
        }
        return view;
    }

    private void initView(View view) {
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        linearLayoutManager.setReverseLayout(true);
        rvMessage = (FilterRecyclerView) view.findViewById(R.id.rv_message);
        rvMessage.setLayoutManager(linearLayoutManager);
        adapter = new PreviousChatMessageListAdapter(this);
        rvMessage.setAdapter(adapter);
        rvMessage.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();
                Log.v(TAG, "onScrolled: " + lastVisibleItemPosition);
                if (lastVisibleItemPosition >= adapter.getItemCount() - 5 && !isLoading && pageNo != (-1)) {
                    isLoading = true;
                    adapter.addItem(null);
                    getData();
                }
            }
        });

    }

    private void getData() {
        Log.v(TAG, "getData: " + pageNo);

        ChatHistoryRequest request = new ChatHistoryRequest();
        request.setPageNo(String.valueOf(pageNo));
        request.setId(String.valueOf(referenceId));

        WebAPIManager.getInstance().getSaveChatHistory(request, new RemoteCallback<ArrayList<ChatMessage>>(new BaseRemoteCallback() {
            @Override
            public void onUnauthorized(Throwable throwable) {

            }

            @Override
            public void onFailed(Throwable throwable) {

            }

            @Override
            public void onInternetFailed() {

            }

            @Override
            public void onEmptyResponse(String message) {
                isLoading = false;
                if (pageNo > 1) {
                    adapter.removeLastItem();
                }
                pageNo = -1;

            }
        }) {
            @Override
            public void onSuccess(ArrayList<ChatMessage> response) {
                isLoading = false;
                if (pageNo > 1) {
                    adapter.removeLastItem();
                }
                if (response.size() > 0) {
                    pageNo++;
                } else {
                    pageNo = -1;
                }
                adapter.addItems(response);
            }
        });
/*

        ArrayList<ChatMessage> messageArrayList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            ChatMessage message = new ChatMessage();
            message.setFirstName(pageNo + "AA" + (i + 1));
            message.setLastName(pageNo + "AA" + (i + 1));
            message.setKey(pageNo + "AA" + (i + 1));
            message.setMessage(pageNo + "AA" + (i + 1));
            message.setProfileUrl(pageNo + "AA" + (i + 1));
            message.setUserId(pageNo + "AA" + (i + 1));
            message.setUsername(pageNo + "AA" + (i + 1));
            messageArrayList.add(message);
        }
        isLoading = false;
        if (pageNo > 1) {
            adapter.removeLastItem();
        }
        adapter.addItems(messageArrayList);
*/

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

        }
    }

    @Override
    public void showEmptyDataView(int resId) {

    }

    @Override
    public void onRecyclerItemClick(View view, int position, ChatMessage item) {

    }


}
