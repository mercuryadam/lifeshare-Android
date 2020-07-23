package com.lifeshare.permission;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.lifeshare.R;

import java.util.ArrayList;

public class RuntimeEasyPermission extends DialogFragment {

    private static final String TAG = "RuntimeEasyPermission";
    private static final int REQUEST_PERMISSION = 1;

    private static final String PERMISSION = "PERMISSION";
    private static final String REQUEST_CODE = "REQUEST_CODE";
    private static final String RATIONAL_MESSAGE = "RATIONAL_MESSAGE";

    private PermissionCallbacks mAppPermissionListener;

    private String[] permissions;
    private int permissionCode;
    private String rationalMessage;

    public static RuntimeEasyPermission newInstance(String permission, int permissionCode, String rationalMessage) {

        String[] permissions = new String[]{permission};

        Bundle args = new Bundle();
        args.putStringArray(REQUEST_CODE, permissions);
        args.putInt(PERMISSION, permissionCode);
        args.putString(RATIONAL_MESSAGE, rationalMessage);

        RuntimeEasyPermission fragment = new RuntimeEasyPermission();
        fragment.setArguments(args);
        return fragment;
    }

    public static RuntimeEasyPermission newInstance(String[] permissions, int permissionCode, String rationalMessage) {

        Bundle args = new Bundle();
        args.putStringArray(REQUEST_CODE, permissions);
        args.putInt(PERMISSION, permissionCode);
        args.putString(RATIONAL_MESSAGE, rationalMessage);

        RuntimeEasyPermission fragment = new RuntimeEasyPermission();
        fragment.setArguments(args);
        return fragment;
    }

    public void setAppPermissionListener(PermissionCallbacks mAppPermissionListener) {
        this.mAppPermissionListener = mAppPermissionListener;
    }

    public boolean hasPermissions(String[] permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (permissions == null) {
            return false;
        }
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void show(FragmentManager fragmentManager) {
        if (fragmentManager.findFragmentByTag(TAG) == null) {
            fragmentManager.beginTransaction().add(this, TAG).commit();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getTargetFragment() instanceof PermissionCallbacks) {
            mAppPermissionListener = (PermissionCallbacks) getTargetFragment();
        }

        if (getArguments() != null) {
            permissions = getArguments().getStringArray(REQUEST_CODE);
            permissionCode = getArguments().getInt(PERMISSION);
            rationalMessage = getArguments().getString(RATIONAL_MESSAGE);
        }
        checkPermission();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PermissionCallbacks) {
            mAppPermissionListener = (PermissionCallbacks) context;
        }

        if (getParentFragment() != null && getParentFragment() instanceof PermissionCallbacks) {
            mAppPermissionListener = (PermissionCallbacks) getParentFragment();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mAppPermissionListener = null;
    }

    private String[] getNonGrantedPermissions(String[] permissions) {
        ArrayList<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }
        if (permissionList.size() > 0) {
            String[] permissionArray = new String[permissionList.size()];
            permissionList.toArray(permissionArray);
            return permissionArray;
        }
        return new String[]{};
    }


    public void checkPermission() {

        String[] nonGrantedPermissions = getNonGrantedPermissions(permissions);
        if (nonGrantedPermissions.length > 0 && !hasPermissions(nonGrantedPermissions)) {
            requestPermissions(nonGrantedPermissions, REQUEST_PERMISSION);
            return;
        }

        mAppPermissionListener.onPermissionAllow(permissionCode);
        closeDialog();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (verifyPermissions(grantResults)) {
                mAppPermissionListener.onPermissionAllow(permissionCode);
                closeDialog();
            } else {
                for (String permission : permissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission)) {
                        showRationaleDialog();
                        break;
                    } else {
                        showSettingsDialog();
                        break;
                    }
                }
            }
        }
    }

    private void showRationaleDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.declined))
                .setMessage(rationalMessage)
                .setPositiveButton(getString(R.string.retry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checkPermission();
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAppPermissionListener.onPermissionDeny(permissionCode);
                        closeDialog();
                        dialogInterface.dismiss();
                    }
                }).create().show();
    }

    private void closeDialog() {
        dismissAllowingStateLoss();
    }

    private void showSettingsDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.title))
                .setMessage(rationalMessage)
                .setPositiveButton(getString(R.string.settings), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getContext().getApplicationContext().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        dialogInterface.dismiss();
                        closeDialog();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAppPermissionListener.onPermissionDeny(permissionCode);
                        dialogInterface.dismiss();
                        closeDialog();
                    }
                }).create().show();
    }

    public interface PermissionCallbacks {
        void onPermissionAllow(int permissionCode);

        void onPermissionDeny(int permissionCode);
    }


}
