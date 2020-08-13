package com.lifeshare.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.lifeshare.R;
import com.lifeshare.imagepicker.ImagePickerFragment;
import com.lifeshare.model.ChannelArchive;
import com.lifeshare.network.RemoteCallback;
import com.lifeshare.network.WebAPIManager;
import com.lifeshare.network.response.CommonResponse;


public class AddChannelArchive extends DialogFragment implements View.OnClickListener, ImagePickerFragment.ImagePickerListener {

    private static final int CAMERA_REQUEST_ID = 101;
    private ImageView ivAddImage;
    private EditText etAddTitle, etAddLink;
    private Button btnAddChannelArchive;
    private String imagePath;

    static AddChannelArchive newInstance() {
        AddChannelArchive f = new AddChannelArchive();

        // Supply num input as an argument.
       /* Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);
*/
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_archive_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        etAddTitle = (EditText) view.findViewById(R.id.etAddTitle);
        etAddLink = (EditText) view.findViewById(R.id.etAddLink);
        ivAddImage = view.findViewById(R.id.ivAddImage);
        btnAddChannelArchive = view.findViewById(R.id.btnAddChannelArchive);

        ivAddImage.setOnClickListener(this);
        btnAddChannelArchive.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivAddImage:
                ImagePickerFragment
                        .newInstance(CAMERA_REQUEST_ID, "LifeShare").show(getChildFragmentManager());
                break;
            case R.id.btnAddChannelArchive:
                if (isValid()) {
                    createChannelArchive(etAddTitle.getText().toString(), etAddLink.getText().toString(), imagePath);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onImageSelected(int requestId, String uri, String imageName) {
        switch (requestId) {
            case CAMERA_REQUEST_ID:
                imagePath = uri;
                Glide.with(getContext())
                        .load(uri)
                        .apply(new RequestOptions().error(R.drawable.user_placeholder).placeholder(R.drawable.user_placeholder))
                        .into(ivAddImage);
                break;
        }
    }

    @Override
    public void onImagePickerClose() {

    }

    private boolean isValid() {
        if (TextUtils.isEmpty(etAddTitle.getText().toString().trim())) {
            Toast.makeText(getActivity(), getString(R.string.please_add_title), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(etAddLink.getText().toString().trim())) {
            Toast.makeText(getActivity(), getString(R.string.please_add_link), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (imagePath == null) {
            Toast.makeText(getActivity(), getString(R.string.please_add_image), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void createChannelArchive(String title, String link, String path) {

        ChannelArchive channelArchive = new ChannelArchive(title, link, path);

        WebAPIManager.getInstance().createChannelArchive(channelArchive, new RemoteCallback<CommonResponse>() {
            @Override
            public void onSuccess(CommonResponse response) {
                Toast.makeText(getActivity(), response.getMessage(), Toast.LENGTH_SHORT).show();
                dismissAllowingStateLoss();
            }

            @Override
            public void onEmptyResponse(String message) {
                Toast.makeText(getActivity(), getString(R.string.failed_to_create), Toast.LENGTH_SHORT).show();
                dismissAllowingStateLoss();
            }

            @Override
            public void onUnauthorized(Throwable throwable) {
                super.onUnauthorized(throwable);
            }
        });

    }
}