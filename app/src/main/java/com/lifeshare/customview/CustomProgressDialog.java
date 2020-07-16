package com.lifeshare.customview;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.lifeshare.R;

/**
 * Created by chirag.patel on 20/11/18.
 */

public class CustomProgressDialog extends ProgressDialog {

    Context mContext;
    String message = "";

    public CustomProgressDialog(Context context) {
        super(context);
    }

    public CustomProgressDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
    }

    public CustomProgressDialog(Context context, int theme, String msg) {
        super(context, theme);
        mContext = context;
        message = msg;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams wlmp = getWindow().getAttributes();

        wlmp.gravity = Gravity.CENTER_HORIZONTAL;
        getWindow().setAttributes(wlmp);
        setTitle(null);
        setCancelable(false);
        setOnCancelListener(null);
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.progress_dialog, null);
        TextView tvMessage = view.findViewById(R.id.tv_message);
        if (TextUtils.isEmpty(message)) {
            tvMessage.setVisibility(View.GONE);
        } else {
            tvMessage.setText(message);
            tvMessage.setVisibility(View.VISIBLE);
        }
        setContentView(view);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

    }
}
