package com.lifeshare.imagepicker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.lifeshare.R;
import com.lifeshare.permission.RuntimeEasyPermission;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ImagePickerListener} interface
 * to handle interaction events.
 */
public class ImagePickerFragment extends DialogFragment implements View.OnClickListener, RuntimeEasyPermission.PermissionCallbacks {


    public static final String TAG = "ImagePickerDialog";
    private static final int ACTION_CHOOSE_IMAGE = 873;
    private static final int ACTION_CAPTURE_IMAGE = 922;

    private static final int REQUEST_GALLARY_PERM = 998;
    private static final int REQUEST_CAPTURE_PERM = 736;
    private static final String KEY_PICKER_REQUEST_CODE = "picker_request_code";
    private static final String KEY_PICKER_TYPE = "picker_type";//0 - camera only, 1 - gallary only , 2 - both
    private static final String KEY_INITIAL_NAME = "intial_name";
    TextView tvGallery, tvCamera;
    private String[] permissions_storage = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private String[] permissions_camera = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private ImagePickerListener mListener;

    public ImagePickerFragment() {
        // Required empty public constructor
    }

    public static ImagePickerFragment newInstance(int requestId, String initialName) {

        Bundle args = new Bundle();
        args.putInt(KEY_PICKER_REQUEST_CODE, requestId);
        args.putString(KEY_INITIAL_NAME, initialName);
        ImagePickerFragment fragment = new ImagePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ImagePickerFragment newInstance(int requestId, int pickerType, String initialName) {

        Bundle args = new Bundle();
        args.putInt(KEY_PICKER_REQUEST_CODE, requestId);
        args.putInt(KEY_PICKER_TYPE, pickerType);
        args.putString(KEY_INITIAL_NAME, initialName);
        ImagePickerFragment fragment = new ImagePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissions_storage = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
            permissions_camera = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA};
        }

        if (getArguments() != null && getArguments().containsKey(KEY_PICKER_TYPE)) {
            if (getArguments().getInt(KEY_PICKER_TYPE) == 0) {
                RuntimeEasyPermission.newInstance(permissions_camera,
                        REQUEST_CAPTURE_PERM, "Allow camera permission")
                        .show(getChildFragmentManager());
                return null;
            } else if (getArguments().getInt(KEY_PICKER_TYPE) == 1) {
                RuntimeEasyPermission.newInstance(permissions_storage,
                        REQUEST_GALLARY_PERM, "Allow storage permission")
                        .show(getChildFragmentManager());
                return null;
            }
            return null;
        } else {
            return inflater.inflate(R.layout.capture_dialogue, container);
        }
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvGallery = (TextView) view.findViewById(R.id.tv_gallery);
        tvCamera = (TextView) view.findViewById(R.id.tv_camera);

        tvGallery.setOnClickListener(this);
        tvCamera.setOnClickListener(this);

    }

    public void show(FragmentManager fragmentManager) {
        //show(fragmentManager, TAG);
        if (fragmentManager.findFragmentByTag(TAG) == null) {
            fragmentManager.beginTransaction().add(this, TAG).commitAllowingStateLoss();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ImagePickerListener) {
            mListener = (ImagePickerListener) context;
        }
        if (getParentFragment() instanceof ImagePickerListener) {
            mListener = (ImagePickerListener) getParentFragment();
        }
        if (mListener == null) {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.onImagePickerClose();
        mListener = null;
    }

    private void selectImage() {
        MediaHelper.createMediaDirectory();
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, ACTION_CHOOSE_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ACTION_CHOOSE_IMAGE:

                    String selectedImageName = MediaHelper.createMediaFileName(getArguments().getString(KEY_INITIAL_NAME, ""));

                    String realPathPhotoId = MediaHelper.getInstance()
                            .getRealPathFromURI(getContext(),
                                    data.getData());


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        try {
                            if (realPathPhotoId != null) {
                                MediaHelper.getInstance().copyFile(realPathPhotoId
                                        , requireContext().getExternalFilesDir(null) + "/" + selectedImageName);

                                mListener.onImageSelected(getArguments().getInt(KEY_PICKER_REQUEST_CODE, 0)
                                        , requireContext().getExternalFilesDir(null) + "/" + selectedImageName
                                        , selectedImageName);

                            } else {
                                Toast.makeText(getContext(), R.string.err_image_not_exist_in_device, Toast.LENGTH_SHORT).show();
                            }
                            closeImagePicker();

                        } catch (IOException e) {
                            closeImagePicker();
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            if (realPathPhotoId != null) {
                                MediaHelper.getInstance().copyFile(realPathPhotoId
                                        , ImageConst.getInstance().IMAGE_DIRECTORY_PATH + "/" + selectedImageName);

                                mListener.onImageSelected(getArguments().getInt(KEY_PICKER_REQUEST_CODE, 0)
                                        , ImageConst.getInstance().IMAGE_DIRECTORY_PATH + "/" + selectedImageName
                                        , selectedImageName);

                            } else {
                                Toast.makeText(getContext(), R.string.err_image_not_exist_in_device, Toast.LENGTH_SHORT).show();
                            }
                            closeImagePicker();

                        } catch (IOException e) {
                            closeImagePicker();
                            e.printStackTrace();
                        }
                    }
                    break;
                case ACTION_CAPTURE_IMAGE:
                    String captureImageName = "";
                    captureImageName = MediaHelper.getInstance()
                            .getImageCapturePath().substring(MediaHelper.getInstance()
                                    .getImageCapturePath().lastIndexOf("/") + 1);
                    MediaHelper.getInstance().singleMediaScanner(requireActivity());

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        try {
                            MediaHelper.getInstance().copyFile(MediaHelper.getInstance()
                                            .getImageCapturePath()
                                    , requireContext().getExternalFilesDir(null) + "/" + captureImageName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mListener.onImageSelected(getArguments().getInt(KEY_PICKER_REQUEST_CODE, 0)
                                , requireContext().getExternalFilesDir(null) + "/" + captureImageName
                                , captureImageName);
                    } else {
                        try {
                            MediaHelper.getInstance().copyFile(MediaHelper.getInstance()
                                            .getImageCapturePath()
                                    , ImageConst.getInstance().IMAGE_DIRECTORY_PATH + "/" + captureImageName);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        mListener.onImageSelected(getArguments().getInt(KEY_PICKER_REQUEST_CODE, 0)
                                , ImageConst.getInstance().IMAGE_DIRECTORY_PATH + "/" + captureImageName
                                , captureImageName);
                    }
                    closeImagePicker();
                    break;
            }
        } else {
            closeImagePicker();
        }
    }

    private void closeImagePicker() {
        dismissAllowingStateLoss();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_gallery:


                RuntimeEasyPermission.newInstance(permissions_storage,
                        REQUEST_GALLARY_PERM, "Allow storage permission")
                        .show(getChildFragmentManager());


                break;
            case R.id.tv_camera:

                RuntimeEasyPermission.newInstance(permissions_camera,
                        REQUEST_CAPTURE_PERM, "Allow camera permission")
                        .show(getChildFragmentManager());

                break;
            default:
                break;
        }
    }

    //capture image and save into default gallary path
    private void captureImage() {
        File mediaStorageDir;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mediaStorageDir = new File(requireContext().getExternalFilesDir(null).toString());
        } else {
            mediaStorageDir = new File(ImageConst.getInstance().IMAGE_DIRECTORY_PATH);

        }
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                MediaHelper.createMediaDirectory();
            }
        }
        // Create a media file name
        String mImageName = MediaHelper.createMediaFileName(getArguments().getString(KEY_INITIAL_NAME, ""));

        File mediaFile;
        String path = mediaStorageDir.getPath() + File.separator + mImageName;
        mediaFile = new File(path);

        MediaHelper.getInstance()
                .setImageCapturePath(mediaFile.getAbsolutePath());

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT
                    , FileProvider.getUriForFile(getActivity()
                            , getActivity().getPackageName() + ".provider", mediaFile));
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mediaFile));
        }
        startActivityForResult(intent, ACTION_CAPTURE_IMAGE);

    }

    @Override
    public void onPermissionAllow(int permissionCode) {
        switch (permissionCode) {
            case REQUEST_GALLARY_PERM:
                selectImage();
                break;
            case REQUEST_CAPTURE_PERM:
                captureImage();
                break;

        }
    }

    @Override
    public void onPermissionDeny(int permissionCode) {
        switch (permissionCode) {
            case REQUEST_GALLARY_PERM:
                Toast.makeText(getActivity(), R.string.msg_permission_denied, Toast.LENGTH_SHORT).show();
                closeImagePicker();

                break;
            case REQUEST_CAPTURE_PERM:
                Toast.makeText(getActivity(), R.string.msg_permission_denied, Toast.LENGTH_SHORT).show();
                closeImagePicker();

                break;

        }

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface ImagePickerListener {
        void onImageSelected(int requestId, String uri, String imageName);

        void onImagePickerClose();
    }

}
