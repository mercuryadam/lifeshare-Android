package com.lifeshare.ui.show_broadcast;

import android.app.Activity;
import android.app.Dialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.instacart.library.truetime.TrueTime;
import com.lifeshare.BaseFragment;
import com.lifeshare.LifeShare;
import com.lifeshare.R;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterRecyclerView;
import com.lifeshare.model.ChatMessage;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.ReportUserRequest;
import com.lifeshare.network.request.SaveChatRequest;
import com.lifeshare.network.request.UpdateSaveChatFlag;
import com.lifeshare.network.response.CommonResponse;
import com.lifeshare.network.response.LoginResponse;
import com.lifeshare.utils.Const;
import com.lifeshare.utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageFragment extends BaseFragment implements View.OnClickListener, BaseRecyclerListener<ChatMessage> {

    private static final String TAG = "MessageFragment";
    DatabaseReference databaseReference;
    private FilterRecyclerView rvMessage;
    private LinearLayout llMessage;
    private AppCompatButton btnSaveChat;
    private AppCompatEditText etMessage;
    private ImageView ivSendMessage;
    private ImageView ivFlag;
    private MessageListAdapterNew adapter;
    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            ArrayList<ChatMessage> messageArrayList = new ArrayList<>();
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                messageArrayList.add(chatMessage);

            }
            if (messageArrayList.size() > adapter.getAllItems().size()) {
                if (messageArrayList.get(messageArrayList.size() - 1).getUserId().equals(PreferenceHelper.getInstance().getUser().getUserId())) {
                    playAudio(R.raw.jingle);
                } else {
                    playAudio(R.raw.tap_1);
                }
            }
            adapter.setItems(messageArrayList);
            rvMessage.scrollToPosition(messageArrayList.size() - 1);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
    private String publisherUserId;
    private String roomId = "";
    private String roomSid = "";
    private Boolean isSubscriptionActive = false;

    public MessageFragment() {
        // Required empty public constructor
    }

    public static MessageFragment newInstance() {
        MessageFragment fragment = new MessageFragment();
        return fragment;
    }

    public static void hideKeyboard(Activity activity) {
        if (activity == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        initView(view);
        return view;
    }

    private void initView(View view) {
        rvMessage = view.findViewById(R.id.rv_message);
        llMessage = view.findViewById(R.id.ll_message);
        etMessage = view.findViewById(R.id.et_message);
        btnSaveChat = view.findViewById(R.id.btn_save_chat);
        btnSaveChat.setOnClickListener(this);
        ivFlag = view.findViewById(R.id.iv_flag);
        etMessage.setImeOptions(EditorInfo.IME_ACTION_SEND);
        etMessage.setRawInputType(InputType.TYPE_CLASS_TEXT);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rvMessage.setLayoutManager(linearLayoutManager);
        adapter = new MessageListAdapterNew(this);
        rvMessage.setAdapter(adapter);


        ivSendMessage = view.findViewById(R.id.iv_send_message);
        ivSendMessage.setOnClickListener(this);
        ivFlag.setOnClickListener(this);
        etMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    hideKeyboard(getActivity());
                    sendMessage();
                    return true;
                }
                return false;
            }
        });
        manageButtons();

    }

    private void getData() {
        if (TextUtils.isEmpty(publisherUserId)) {
            return;
        }
        databaseReference = LifeShare.getFirebaseReference().child(Const.TABLE_PUBLISHER)
                .child(publisherUserId).child(Const.TABLE_CHAT_MESSAGE);

        databaseReference.addValueEventListener(valueEventListener);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_send_message:
                hideKeyboard(getActivity());
                sendMessage();
                break;
            case R.id.iv_flag:
                openDialog();
                break;
            case R.id.btn_save_chat:
                saveChatHistory();
                break;
        }
    }

    private void saveChatHistory() {
        UpdateSaveChatFlag request = new UpdateSaveChatFlag();
        request.setId(roomId);
        request.setSaveChat("1");
        showLoading();
        WebAPIManager.getInstance().updateSaveChatFlag(request, new RemoteCallback<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse response) {
//                btnSaveChat.setEnabled(false);
                hideLoading();
                Toast.makeText(getContext(), "Your chat is being saved.", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void openDialog() {


        Dialog dialog = new Dialog(getContext(), R.style.WideDialog);

        dialog.setContentView(R.layout.dialog_objectional);
        AppCompatEditText etShortDiscription = (AppCompatEditText) dialog.findViewById(R.id.et_short_discription);
        AppCompatTextView tvCancel = (AppCompatTextView) dialog.findViewById(R.id.tv_cancel);
        AppCompatTextView tvSend = (AppCompatTextView) dialog.findViewById(R.id.tv_send);

        tvSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitReport(etShortDiscription.getText().toString().trim());
                dialog.dismiss();
            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void submitReport(String message) {

        ReportUserRequest request = new ReportUserRequest();
        request.setMessage(message);
        request.setUserId(publisherUserId);
        WebAPIManager.getInstance().submitReportUser(request, new RemoteCallback<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse response) {

            }
        });
    }

    private void sendMessage() {
        if (!isValid()) {
            return;
        }
        LoginResponse user = PreferenceHelper.getInstance().getUser();
        DatabaseReference databaseReference = LifeShare.getFirebaseReference()
                .child(Const.TABLE_PUBLISHER)
                .child(publisherUserId)
                .child(Const.TABLE_CHAT_MESSAGE)
                .child(LifeShare.getFirebaseReference().push().getKey());
        HashMap<String, String> startRequestMap = new HashMap<>();
        startRequestMap.put("userId", user.getUserId());
        startRequestMap.put("firstName", user.getFirstName());
        startRequestMap.put("lastName", user.getLastName());
        startRequestMap.put("username", user.getUsername());
        startRequestMap.put("profileUrl", user.getAvatar());
        startRequestMap.put("time", String.valueOf(TrueTime.now().getTime()));
        startRequestMap.put("message", etMessage.getText().toString().trim());

        sendMessageToAPI(etMessage.getText().toString().trim());

        databaseReference.setValue(startRequestMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                etMessage.setText("");
            }
        });
    }

    private void sendMessageToAPI(String message) {
        SaveChatRequest request = new SaveChatRequest();
        request.setMessage(message);
        request.setId(roomId);
        request.setRoomSId(roomSid);

        showLoading();

        WebAPIManager.getInstance().saveChatMessage(request, new RemoteCallback<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse response) {
                hideLoading();
            }

            @Override
            public void onFailed(Throwable throwable) {
                super.onFailed(throwable);
                hideLoading();
            }

            @Override
            public void onUnauthorized(Throwable throwable) {
                super.onUnauthorized(throwable);
                hideLoading();
            }

            @Override
            public void onInternetFailed() {
                super.onInternetFailed();
                hideLoading();
            }

            @Override
            public void onEmptyResponse(String message) {
                super.onEmptyResponse(message);
                hideLoading();
            }
        });
    }

    public void setCurrentStream(String publisherId, String roomId, String roomSid, Boolean isSubActive) {
        publisherUserId = publisherId;
        this.roomId = roomId;
        this.roomSid = roomSid;
        this.isSubscriptionActive = isSubActive;
        if (adapter != null) {
            adapter.removeAllItems();
        }
        if (databaseReference != null && valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }
        getData();
        Log.v(TAG, "setCurrentStream: " + isAdded());
        manageButtons();
    }

    private void manageButtons() {
        if (ivFlag != null && btnSaveChat != null) {
            if (publisherUserId != null && Integer.parseInt(publisherUserId) != Integer.parseInt(PreferenceHelper.getInstance().getUser().getUserId())) {
                ivFlag.setVisibility(View.VISIBLE);
                btnSaveChat.setVisibility(View.GONE);
            } else {
                ivFlag.setVisibility(View.GONE);
                if (isSubscriptionActive) {
                    btnSaveChat.setVisibility(View.VISIBLE);
                } else {
                    btnSaveChat.setVisibility(View.GONE);
                }
            }
        }
    }

    private boolean isValid() {
        if (TextUtils.isEmpty(etMessage.getText().toString().trim())) {
            Toast.makeText(getActivity(), getString(R.string.please_enter_comment), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public void showEmptyDataView(int resId) {

    }

    @Override
    public void onRecyclerItemClick(View view, int position, ChatMessage item) {

    }

    public void playAudio(Integer audio) {
        if (getActivity() != null) {
            final MediaPlayer mp = MediaPlayer.create(getActivity(), audio);
            mp.start();
        }
    }

}
