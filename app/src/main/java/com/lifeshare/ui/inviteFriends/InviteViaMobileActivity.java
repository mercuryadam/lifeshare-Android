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
import com.lifeshare.ui.BroadcastUsingAgoraActivity;
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
                startActivity(new Intent(InviteViaMobileActivity.this, BroadcastUsingAgoraActivity.class));
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
                    startActivity(new Intent(InviteViaMobileActivity.this, BroadcastUsingAgoraActivity.class));
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

        countryList.add(new CountryResponse("1", "Afghanistan", "+93"));
        countryList.add(new CountryResponse("1", "Armenia", "+374"));
        countryList.add(new CountryResponse("1", "Azerbaijan", "+994"));
        countryList.add(new CountryResponse("1", "Bahrain", "+973"));
        countryList.add(new CountryResponse("1", "Bangladesh", "+880"));
        countryList.add(new CountryResponse("1", "Bhutan", "+975"));
        countryList.add(new CountryResponse("1", "Brunei", "+673"));
        countryList.add(new CountryResponse("1", "Cambodia", "+855"));
        countryList.add(new CountryResponse("1", "China", "+86"));
        countryList.add(new CountryResponse("1", "East Timor", "+670"));
        countryList.add(new CountryResponse("1", "Georgia", "+995"));
        countryList.add(new CountryResponse("1", "Hong Kong", "+852"));
        countryList.add(new CountryResponse("1", "India", "+91"));
        countryList.add(new CountryResponse("1", "Indonesia", "+62"));
        countryList.add(new CountryResponse("1", "Iran", "+98"));
        countryList.add(new CountryResponse("1", "Iraq", "+964"));
        countryList.add(new CountryResponse("1", "Israel", "+972"));
        countryList.add(new CountryResponse("1", "Japan", "+81"));
        countryList.add(new CountryResponse("1", "Jordan", "+962"));
        countryList.add(new CountryResponse("1", "Korea Dem People's Rep", "+850"));
        countryList.add(new CountryResponse("1", "Korea Republic of", "+82"));
        countryList.add(new CountryResponse("1", "Kuwait", "+965"));
        countryList.add(new CountryResponse("1", "Kyrgyzstan", "+996"));
        countryList.add(new CountryResponse("1", "Laos PDR", "+856"));
        countryList.add(new CountryResponse("1", "Lebanon", "+961"));
        countryList.add(new CountryResponse("1", "Macau", "+853"));
        countryList.add(new CountryResponse("1", "Malaysia", "+60"));
        countryList.add(new CountryResponse("1", "Maldives", "+960"));
        countryList.add(new CountryResponse("1", "Mongolia", "+976"));
        countryList.add(new CountryResponse("1", "Myanmar", "+95"));
        countryList.add(new CountryResponse("1", "Nepal", "+977"));
        countryList.add(new CountryResponse("1", "Oman", "+968"));
        countryList.add(new CountryResponse("1", "Pakistan", "+92"));
        countryList.add(new CountryResponse("1", "Palestinian Territory", "+970"));
        countryList.add(new CountryResponse("1", "Philippines", "+63"));
        countryList.add(new CountryResponse("1", "Qatar", "+974"));
        countryList.add(new CountryResponse("1", "Russia/Kazakhstan", "+7"));
        countryList.add(new CountryResponse("1", "Saudi Arabia", "+966"));
        countryList.add(new CountryResponse("1", "Singapore", "+65"));
        countryList.add(new CountryResponse("1", "Sri Lanka", "+94"));
        countryList.add(new CountryResponse("1", "Syria", "+963"));
        countryList.add(new CountryResponse("1", "Taiwan", "+886"));
        countryList.add(new CountryResponse("1", "Tajikistan", "+992"));
        countryList.add(new CountryResponse("1", "Thailand", "+66"));
        countryList.add(new CountryResponse("1", "Turkey", "+90"));
        countryList.add(new CountryResponse("1", "Turkish Republic of Northern Cyprus", "+90"));
        countryList.add(new CountryResponse("1", "Turkmenistan", "+993"));
        countryList.add(new CountryResponse("1", "United Arab Emirates", "+971"));
        countryList.add(new CountryResponse("1", "Uzbekistan", "+998"));
        countryList.add(new CountryResponse("1", "Vietnam", "+84"));
        countryList.add(new CountryResponse("1", "Yemen", "+967"));
        countryList.add(new CountryResponse("1", "Albania", "+355"));
        countryList.add(new CountryResponse("1", "Andorra", "+376"));
        countryList.add(new CountryResponse("1", "Austria", "+43"));
        countryList.add(new CountryResponse("1", "Belarus", "+375"));
        countryList.add(new CountryResponse("1", "Belgium", "+32"));
        countryList.add(new CountryResponse("1", "Bosnia and Herzegovina", "+387"));
        countryList.add(new CountryResponse("1", "Bulgaria", "+359"));
        countryList.add(new CountryResponse("1", "Canary Islands", "+3491"));
        countryList.add(new CountryResponse("1", "Croatia", "+385"));
        countryList.add(new CountryResponse("1", "Cyprus", "+357"));
        countryList.add(new CountryResponse("1", "Czech Republic", "+420"));
        countryList.add(new CountryResponse("1", "Denmark", "+45"));
        countryList.add(new CountryResponse("1", "Estonia", "+372"));
        countryList.add(new CountryResponse("1", "Faroe Islands", "+298"));
        countryList.add(new CountryResponse("1", "Finland/Aland Islands", "+358"));
        countryList.add(new CountryResponse("1", "France", "+33"));
        countryList.add(new CountryResponse("1", "Germany", "+49"));
        countryList.add(new CountryResponse("1", "Gibraltar", "+350"));
        countryList.add(new CountryResponse("1", "Greece", "+30"));
        countryList.add(new CountryResponse("1", "Hungary", "+36"));
        countryList.add(new CountryResponse("1", "Iceland", "+354"));
        countryList.add(new CountryResponse("1", "Ireland", "+353"));
        countryList.add(new CountryResponse("1", "Italy", "+39"));
        countryList.add(new CountryResponse("1", "Kosovo", "+383"));
        countryList.add(new CountryResponse("1", "Latvia", "+371"));
        countryList.add(new CountryResponse("1", "Liechtenstein", "+423"));
        countryList.add(new CountryResponse("1", "Lithuania", "+370"));
        countryList.add(new CountryResponse("1", "Luxembourg", "+352"));
        countryList.add(new CountryResponse("1", "Macedonia", "+389"));
        countryList.add(new CountryResponse("1", "Malta", "+356"));
        countryList.add(new CountryResponse("1", "Moldova", "+373"));
        countryList.add(new CountryResponse("1", "Monaco", "+377"));
        countryList.add(new CountryResponse("1", "Montenegro", "+382"));
        countryList.add(new CountryResponse("1", "Netherlands", "+31"));
        countryList.add(new CountryResponse("1", "Norway", "+47"));
        countryList.add(new CountryResponse("1", "Poland", "+48"));
        countryList.add(new CountryResponse("1", "Portugal", "+351"));
        countryList.add(new CountryResponse("1", "Romania", "+40"));
        countryList.add(new CountryResponse("1", "San Marino", "+378"));
        countryList.add(new CountryResponse("1", "Serbia", "+381"));
        countryList.add(new CountryResponse("1", "Slovakia", "+421"));
        countryList.add(new CountryResponse("1", "Slovenia", "+386"));
        countryList.add(new CountryResponse("1", "Spain", "+34"));
        countryList.add(new CountryResponse("1", "Sweden", "+46"));
        countryList.add(new CountryResponse("1", "Switzerland", "+41"));
        countryList.add(new CountryResponse("1", "Ukraine", "+380"));
        countryList.add(new CountryResponse("1", "United Kingdom", "+44"));
        countryList.add(new CountryResponse("1", "Vatican City", "+379"));
        countryList.add(new CountryResponse("1", "Algeria", "+213"));
        countryList.add(new CountryResponse("1", "Angola", "+244"));
        countryList.add(new CountryResponse("1", "Benin", "+229"));
        countryList.add(new CountryResponse("1", "Botswana", "+267"));
        countryList.add(new CountryResponse("1", "Burkina Faso", "+226"));
        countryList.add(new CountryResponse("1", "Burundi", "+257"));
        countryList.add(new CountryResponse("1", "Cameroon", "+237"));
        countryList.add(new CountryResponse("1", "Canary Islands", "+3491"));
        countryList.add(new CountryResponse("1", "Cape Verde", "+238"));
        countryList.add(new CountryResponse("1", "Central Africa", "+236"));
        countryList.add(new CountryResponse("1", "Chad", "+235"));
        countryList.add(new CountryResponse("1", "Comoros", "+269"));
        countryList.add(new CountryResponse("1", "Congo", "+242"));
        countryList.add(new CountryResponse("1", "Congo, Dem Rep", "+243"));
        countryList.add(new CountryResponse("1", "Djibouti", "+253"));
        countryList.add(new CountryResponse("1", "Egypt", "+2020"));
        countryList.add(new CountryResponse("1", "Equatorial Guinea", "+240"));
        countryList.add(new CountryResponse("1", "Eritrea", "+291"));
        countryList.add(new CountryResponse("1", "Ethiopia", "+251"));
        countryList.add(new CountryResponse("1", "Gabon", "+241"));
        countryList.add(new CountryResponse("1", "Gambia", "+220"));
        countryList.add(new CountryResponse("1", "Ghana", "+233"));
        countryList.add(new CountryResponse("1", "Guinea", "+224"));
        countryList.add(new CountryResponse("1", "Guinea-Bissau", "+245"));
        countryList.add(new CountryResponse("1", "Ivory Coast", "+225"));
        countryList.add(new CountryResponse("1", "Kenya", "+254"));
        countryList.add(new CountryResponse("1", "Lesotho", "+266"));
        countryList.add(new CountryResponse("1", "Liberia", "+231"));
        countryList.add(new CountryResponse("1", "Libya", "+218"));
        countryList.add(new CountryResponse("1", "Madagascar", "+261"));
        countryList.add(new CountryResponse("1", "Malawi", "+265"));
        countryList.add(new CountryResponse("1", "Mali", "+223"));
        countryList.add(new CountryResponse("1", "Mauritania", "+222"));
        countryList.add(new CountryResponse("1", "Mauritius", "+230"));
        countryList.add(new CountryResponse("1", "Morocco/Western Sahara", "+212"));
        countryList.add(new CountryResponse("1", "Mozambique", "+258"));
        countryList.add(new CountryResponse("1", "Namibia", "+264"));
        countryList.add(new CountryResponse("1", "Niger", "+227"));
        countryList.add(new CountryResponse("1", "Nigeria", "+234"));
        countryList.add(new CountryResponse("1", "Reunion/Mayotte", "+262"));
        countryList.add(new CountryResponse("1", "Rwanda", "+250"));
        countryList.add(new CountryResponse("1", "Sao Tome and Principe", "+239"));
        countryList.add(new CountryResponse("1", "Senegal", "+221"));
        countryList.add(new CountryResponse("1", "Seychelles", "+248"));
        countryList.add(new CountryResponse("1", "Sierra Leone", "+232"));
        countryList.add(new CountryResponse("1", "Somalia", "+252"));
        countryList.add(new CountryResponse("1", "South Africa", "+27"));
        countryList.add(new CountryResponse("1", "South Sudan", "+211"));
        countryList.add(new CountryResponse("1", "Spain", "+34"));
        countryList.add(new CountryResponse("1", "Sudan", "+249"));
        countryList.add(new CountryResponse("1", "Swaziland", "+268"));
        countryList.add(new CountryResponse("1", "Tanzania", "+255"));
        countryList.add(new CountryResponse("1", "Togo", "+228"));
        countryList.add(new CountryResponse("1", "Tunisia", "+216"));
        countryList.add(new CountryResponse("1", "Uganda", "+256"));
        countryList.add(new CountryResponse("1", "Zambia", "+260"));
        countryList.add(new CountryResponse("1", "Zimbabwe", "+263"));
        countryList.add(new CountryResponse("1", "American Samoa", "+1684"));
        countryList.add(new CountryResponse("1", "Australia/Cocos/Christmas Island", "+61"));
        countryList.add(new CountryResponse("1", "Cook Islands", "+682"));
        countryList.add(new CountryResponse("1", "Fiji", "+679"));
        countryList.add(new CountryResponse("1", "French Polynesia", "+689"));
        countryList.add(new CountryResponse("1", "Guam", "+1671"));
        countryList.add(new CountryResponse("1", "Kiribati", "+686"));
        countryList.add(new CountryResponse("1", "Marshall Islands", "+692"));
        countryList.add(new CountryResponse("1", "Micronesia", "+691"));
        countryList.add(new CountryResponse("1", "New Caledonia", "+687"));
        countryList.add(new CountryResponse("1", "New Zealand", "+64"));
        countryList.add(new CountryResponse("1", "Niue", "+683"));
        countryList.add(new CountryResponse("1", "Norfolk Island", "+672"));
        countryList.add(new CountryResponse("1", "Northern Mariana Islands", "+1670"));
        countryList.add(new CountryResponse("1", "Palau", "+680"));
        countryList.add(new CountryResponse("1", "Papua New Guinea", "+675"));
        countryList.add(new CountryResponse("1", "Samoa", "+685"));
        countryList.add(new CountryResponse("1", "Solomon Islands", "+677"));
        countryList.add(new CountryResponse("1", "Tonga", "+676"));
        countryList.add(new CountryResponse("1", "Tuvalu", "+688"));
        countryList.add(new CountryResponse("1", "Vanuatu", "+678"));
        countryList.add(new CountryResponse("1", "American Samoa", "+1684"));
        countryList.add(new CountryResponse("1", "Anguilla", "+1264"));
        countryList.add(new CountryResponse("1", "Antigua and Barbuda", "+1268"));
        countryList.add(new CountryResponse("1", "Aruba", "+297"));
        countryList.add(new CountryResponse("1", "Ascension", "+247"));
        countryList.add(new CountryResponse("1", "Bahamas", "+1242"));
        countryList.add(new CountryResponse("1", "Barbados", "+1246"));
        countryList.add(new CountryResponse("1", "Belize", "+501"));
        countryList.add(new CountryResponse("1", "Bermuda", "+1441"));
        countryList.add(new CountryResponse("1", "Canada", "+1"));
        countryList.add(new CountryResponse("1", "Cayman Islands", "+1345"));
        countryList.add(new CountryResponse("1", "Costa Rica", "+506"));
        countryList.add(new CountryResponse("1", "Cuba", "+53"));
        countryList.add(new CountryResponse("1", "Dominica", "+1767"));
        countryList.add(new CountryResponse("1", "Dominican Republic", "+1809201"));
        countryList.add(new CountryResponse("1", "Dominican Republic", "+1809"));
        countryList.add(new CountryResponse("1", "Dominican Republic", "+1849"));
        countryList.add(new CountryResponse("1", "Dominican Republic", "+1829"));
        countryList.add(new CountryResponse("1", "El Salvador", "+503"));
        countryList.add(new CountryResponse("1", "Greenland", "+299"));
        countryList.add(new CountryResponse("1", "Grenada", "+1473"));
        countryList.add(new CountryResponse("1", "Guadeloupe", "+590"));
        countryList.add(new CountryResponse("1", "Guam", "+1671"));
        countryList.add(new CountryResponse("1", "Guatemala", "+502"));
        countryList.add(new CountryResponse("1", "Haiti", "+509"));
        countryList.add(new CountryResponse("1", "Honduras", "+504"));
        countryList.add(new CountryResponse("1", "Jamaica", "+1876"));
        countryList.add(new CountryResponse("1", "Martinique", "+596"));
        countryList.add(new CountryResponse("1", "Mexico", "+52"));
        countryList.add(new CountryResponse("1", "Montserrat", "+1664"));
        countryList.add(new CountryResponse("1", "Netherlands Antilles", "+599"));
        countryList.add(new CountryResponse("1", "Nicaragua", "+505"));
        countryList.add(new CountryResponse("1", "Northern Mariana Islands", "+1670"));
        countryList.add(new CountryResponse("1", "Panama", "+507"));
        countryList.add(new CountryResponse("1", "Puerto Rico", "+1787"));
        countryList.add(new CountryResponse("1", "St Kitts and Nevis", "+1869"));
        countryList.add(new CountryResponse("1", "St Lucia", "+1758"));
        countryList.add(new CountryResponse("1", "St Pierre and Miquelon", "+508"));
        countryList.add(new CountryResponse("1", "St Vincent Grenadines", "+1784"));
        countryList.add(new CountryResponse("1", "Trinidad and Tobago", "+1868"));
        countryList.add(new CountryResponse("1", "Turks and Caicos Islands", "+1649"));
        countryList.add(new CountryResponse("1", "United States", "+1"));
        countryList.add(new CountryResponse("1", "Virgin Islands, British", "+1284"));
        countryList.add(new CountryResponse("1", "Virgin Islands, U.S.", "+1340"));
        countryList.add(new CountryResponse("1", "Argentina", "+54"));
        countryList.add(new CountryResponse("1", "Bolivia", "+591"));
        countryList.add(new CountryResponse("1", "Brazil", "+55"));
        countryList.add(new CountryResponse("1", "Chile", "+56"));
        countryList.add(new CountryResponse("1", "Colombia", "+57"));
        countryList.add(new CountryResponse("1", "Ecuador", "+593"));
        countryList.add(new CountryResponse("1", "Falkland Islands", "+500"));
        countryList.add(new CountryResponse("1", "French Guiana", "+594"));
        countryList.add(new CountryResponse("1", "Guyana", "+592"));
        countryList.add(new CountryResponse("1", "Paraguay", "+595"));
        countryList.add(new CountryResponse("1", "Peru", "+51"));
        countryList.add(new CountryResponse("1", "Suriname", "+597"));
        countryList.add(new CountryResponse("1", "Uruguay", "+598"));
        countryList.add(new CountryResponse("1", "Venezuela", "+58"));
    }
}