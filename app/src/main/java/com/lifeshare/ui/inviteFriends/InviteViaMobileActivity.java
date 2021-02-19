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
import com.lifeshare.customview.singleChoiceBottomSheet.SingleChoiceBottomSheet;
import com.lifeshare.customview.singleChoiceBottomSheet.SingleChoiceDialogListener;
import com.lifeshare.model.ContactListModel;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.request.ContactInvitationViaMobileRequest;
import com.lifeshare.network.response.CommonResponse;
import com.lifeshare.network.response.CountryResponse;
import com.lifeshare.permission.RuntimeEasyPermission;
import com.lifeshare.ui.TwilioBroadcastActivityNew;
import com.lifeshare.utils.Const;

import java.util.ArrayList;

import pub.devrel.easypermissions.EasyPermissions;

public class InviteViaMobileActivity extends BaseActivity implements View.OnClickListener, BaseRecyclerListener<ContactListModel>, RuntimeEasyPermission.PermissionCallbacks {

    private static final int REQUEST_AUDIO_PERM_PUBLISH_BROADCAST = 1123;
    private static final String TAG = "InviteViaMobile";
    private FilterRecyclerView recyclerView;
    private AppCompatTextView tvNoData;
    private InviteViaMobileListAdapter adapter;
    private TextView tvSelectToggle;
    private String[] permissions_audio = new String[]{Manifest.permission.READ_CONTACTS};
    private AppCompatButton btnInvite;
    private AppCompatButton btnSkip;
    private boolean isFromTermAndCondition = false;
    CountryResponse selectedCountry;
    ArrayList<CountryResponse> countryList = new ArrayList<CountryResponse>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_via_mobile);
        initView();
        if (getIntent() != null && getIntent().hasExtra(Const.IS_FROM) && getIntent().getStringExtra(Const.IS_FROM).equals(Const.TERM_AND_CONDITION_SCREEN)) {
            isFromTermAndCondition = true;
            btnSkip.setVisibility(View.VISIBLE);
        } else {
            isFromTermAndCondition = false;
            btnSkip.setVisibility(View.GONE);
        }
        checkReadContactPermission();
        getCountryList();
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
        adapter = new InviteViaMobileListAdapter(this);
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
            if (model.getMobile().equals(email)) {
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
                startActivity(new Intent(InviteViaMobileActivity.this, TwilioBroadcastActivityNew.class));
                finish();
                break;
            case R.id.btn_invite:

                //here email is equals to country code

                if (adapter.getCheckedItems().size() > 0) {
                    String mobileNumbers = "";
                    for (int i = 0; i < adapter.getCheckedItems().size(); i++) {
                        if (mobileNumbers.isEmpty()) {
                            if (adapter.getCheckedItems().get(i).getEmail() != null) {
                                mobileNumbers = adapter.getCheckedItems().get(i).getEmail() + adapter.getCheckedItems().get(i).getMobile();
                            } else {
                                mobileNumbers = adapter.getCheckedItems().get(i).getMobile();
                            }
                        } else {
                            if (adapter.getCheckedItems().get(i).getEmail() != null) {
                                mobileNumbers = mobileNumbers + "," + adapter.getCheckedItems().get(i).getEmail() + adapter.getCheckedItems().get(i).getMobile();

                            } else {
                                mobileNumbers = mobileNumbers + "," + adapter.getCheckedItems().get(i).getMobile();

                            }
                        }

                        callContactInvitationViaMobile(mobileNumbers);
                    }

                } else {
                    showToast(getString(R.string.please_select_any_contact));
                }

                break;
        }
    }

    private void callContactInvitationViaMobile(String mobileNumbers) {

        Log.v(TAG, "submitMobileNOs: " + mobileNumbers);

        ContactInvitationViaMobileRequest request = new ContactInvitationViaMobileRequest();
        request.setMobile(mobileNumbers);
        showLoading();
        WebAPIManager.getInstance().contactInvitationViaMobile(request, new RemoteCallback<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse response) {
                hideLoading();
                showToast(response.getMessage());
                if (isFromTermAndCondition) {
                    startActivity(new Intent(InviteViaMobileActivity.this, TwilioBroadcastActivityNew.class));
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
        openCountryListBottomSheet(item);
    }

    private void openCountryListBottomSheet(ContactListModel item) {

        SingleChoiceBottomSheet myBottomSheet =
                new SingleChoiceBottomSheet.Builder<CountryResponse>(new SingleChoiceDialogListener<CountryResponse>() {
                    @Override
                    public void onSpinnerItemClick(int position, CountryResponse selectedItem) {
                        hideKeyboard(InviteViaMobileActivity.this);

                        //etCountry.setText(selectedItem.getName());
                        selectedCountry = selectedItem;
                        item.setEmail(selectedCountry.getPhonecode());
                        adapter.notifyDataSetChanged();
                    }

                }).addItems(countryList)
                        .setSelectedItem(selectedCountry)
                        .addDialogTitle(getString(R.string.country_code))
                        .addFieldName("phonecode")
                        .build();


        myBottomSheet.show(getSupportFragmentManager(), myBottomSheet.getTag());

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
            String[] projection = new String[]{ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};

            Cursor cur = cr.query(ContactsContract.Data.CONTENT_URI, projection, null, null, null);
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {

                    ContactListModel model = new ContactListModel();
                    //to get the contact names
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String phoneNumber = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    model.setName(name);
                    model.setMobile(phoneNumber);

                    if (phoneNumber != null && !phoneNumber.isEmpty() && Patterns.PHONE.matcher(phoneNumber).matches() && !isExist(arrayList, phoneNumber)) {
                        arrayList.add(model);
                    }
                }
            }
            cur.close();
            return arrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<ContactListModel> contactListModels) {
            super.onPostExecute(contactListModels);
            hideLoading();
            adapter.addItems(contactListModels);
        }
    }

    private void getCountryList() {

        countryList.add(new CountryResponse("1", "India", "+91"));
        countryList.add(new CountryResponse("4", "US", "+1"));
        countryList.add(new CountryResponse("3", "Uganda", "+256"));
        countryList.add(new CountryResponse("2", "Vietnam", "+84"));
    /*    WebAPIManager.getInstance().getCountry(new RemoteCallback<ArrayList<CountryResponse>>(this) {
            @Override
            public void onSuccess(ArrayList<CountryResponse> response) {
                countryList.clear();
                countryList.addAll(response);
            }

            @Override
            public void onEmptyResponse(String message) {
                super.onEmptyResponse(message);
            }
        });*/
    }
}