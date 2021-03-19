package com.lifeshare.ui.inviteFriends;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lifeshare.BaseActivity;
import com.lifeshare.R;
import com.lifeshare.customview.recyclerview.BaseRecyclerListener;
import com.lifeshare.customview.recyclerview.FilterRecyclerView;
import com.lifeshare.model.ContactListModel;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.ContactInvitationRequest;
import com.lifeshare.network.response.CommonResponse;
import com.lifeshare.permission.RuntimeEasyPermission;
import com.lifeshare.ui.BroadcastUsingAgoraActivity;
import com.lifeshare.ui.TwilioBroadcastActivityNew;
import com.lifeshare.utils.Const;

import java.util.ArrayList;

import pub.devrel.easypermissions.EasyPermissions;

public class ContactListActivity extends BaseActivity implements View.OnClickListener, BaseRecyclerListener<ContactListModel>, RuntimeEasyPermission.PermissionCallbacks {

    private static final int REQUEST_AUDIO_PERM_PUBLISH_BROADCAST = 1123;
    private static final String TAG = "ContactListActivity";
    private FilterRecyclerView recyclerView;
    private AppCompatTextView tvNoData;
    private ContactListAdapter adapter;
    private TextView tvSelectToggle;
    private String[] permissions_audio = new String[]{Manifest.permission.READ_CONTACTS};
    private AppCompatButton btnInvite;
    private AppCompatButton btnSkip;
    private boolean isFromTermAndCondition = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        initView();
        if (getIntent() != null && getIntent().hasExtra(Const.IS_FROM) && getIntent().getStringExtra(Const.IS_FROM).equals(Const.TERM_AND_CONDITION_SCREEN)) {
            isFromTermAndCondition = true;
            btnSkip.setVisibility(View.VISIBLE);
        } else {
            isFromTermAndCondition = false;
            btnSkip.setVisibility(View.GONE);
        }
        checkReadContactPermission();
    }

    private void checkReadContactPermission() {
        RuntimeEasyPermission.newInstance(permissions_audio,
                REQUEST_AUDIO_PERM_PUBLISH_BROADCAST, "Allow contact read permission").show(getSupportFragmentManager());
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
        adapter = new ContactListAdapter(this);
        recyclerView.setEmptyMsgHolder(tvNoData);
        recyclerView.setAdapter(adapter);

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
        btnInvite = (AppCompatButton) findViewById(R.id.btn_invite);
        btnSkip = (AppCompatButton) findViewById(R.id.btn_skip);

        btnInvite.setOnClickListener(this);
        btnSkip.setOnClickListener(this);
    }

    private boolean isExist(ArrayList<ContactListModel> arrayList, String email) {
        for (ContactListModel model : arrayList) {
            if (model.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_skip:
                startActivity(new Intent(ContactListActivity.this, BroadcastUsingAgoraActivity.class));
                finish();
                break;
            case R.id.btn_invite:

                if (adapter.getCheckedItems().size() > 0) {
                    String email = "";
                    for (int i = 0; i < adapter.getCheckedItems().size(); i++) {
                        if (email.isEmpty()) {
                            email = adapter.getCheckedItems().get(i).getEmail();
                        } else {
                            email = email + "," + adapter.getCheckedItems().get(i).getEmail();
                        }

                        submitEmailAddress(email);
                    }

                } else {
                    showToast(getString(R.string.please_select_any_contact));
                }

                break;
        }
    }

    private void submitEmailAddress(String email) {

        Log.v(TAG, "submitEmailAddress: " + email);

        ContactInvitationRequest request = new ContactInvitationRequest();
        request.setEmail(email);
        showLoading();
        WebAPIManager.getInstance().contactInvitation(request, new RemoteCallback<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse response) {
                hideLoading();
                showToast(response.getMessage());
                if (isFromTermAndCondition) {
                    startActivity(new Intent(ContactListActivity.this, BroadcastUsingAgoraActivity.class));
                    finish();
                } else {
                    finish();
                }
            }
        });
    }

    @Override
    public void showEmptyDataView(int resId) {
        recyclerView.showEmptyDataView(getString(R.string.no_connection_available));
    }

    @Override
    public void onRecyclerItemClick(View view, int position, ContactListModel item) {

    }

    @Override
    public void onPermissionAllow(int permissionCode) {
        new GetContactAsyncTask().execute();
    }

    @Override
    public void onPermissionDeny(int permissionCode) {

    }

    class GetContactAsyncTask extends AsyncTask<Void, Void, ArrayList<ContactListModel>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading();
        }

        @Override
        protected ArrayList<ContactListModel> doInBackground(Void... voids) {


            ArrayList<ContactListModel> arrayList = new ArrayList<>();
            ContentResolver cr = getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    Cursor cur1 = cr.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (cur1.moveToNext()) {
                        ContactListModel model = new ContactListModel();
                        //to get the contact names
                        String name = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                        String email = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));

                        Log.v(TAG, "getdata: " + name + "- " + email + "");

                        model.setName(name);
                        model.setEmail(email);

                        if (email != null && !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches() && !isExist(arrayList, email)) {
                            arrayList.add(model);
                        }
                    }

                    cur1.close();
                }
            }
            return arrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<ContactListModel> contactListModels) {
            super.onPostExecute(contactListModels);
            hideLoading();
            adapter.addItems(contactListModels);
        }
    }
}