package com.lifeshare.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.lifeshare.R;

public class AuthenticationDialog extends Dialog {

    private final String redirect_url;
    private final String request_url;
    private AuthenticationListener listener;
    private final Activity activity;

    public AuthenticationDialog(@NonNull Context context, Activity activity, AuthenticationListener listener) {
        super(context);
        this.listener = listener;
        this.activity = activity;
        this.redirect_url = context.getResources().getString(R.string.redirect_url);
        this.request_url = context.getResources().getString(R.string.base_url) +
                "oauth/authorize/?client_id=" +
                context.getResources().getString(R.string.client_id) +
                "&redirect_uri=" + redirect_url +
                "&response_type=code&display=touch&scope=user_profile";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.auth_dialog);

        WebView webView = findViewById(R.id.webView);

        try {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
            webView.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        } catch (Exception e) {
            e.printStackTrace();
        }


        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(request_url);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                Log.d("AuthenticationDialog", url);
                if (url.startsWith(redirect_url)) {
                    AuthenticationDialog.this.dismiss();
                    if (url.contains("code=")) {
                        Log.d("AuthenticationDialog", "Contain -- " + url);
                        Uri uri = Uri.EMPTY.parse(url);
                        String access_token = uri.toString();
                        access_token = access_token.substring(access_token.lastIndexOf("=") + 1);
                        access_token = access_token.replace("#_", "");
                        Log.d("AuthenticationDialog", "String -- " + access_token);
                        listener.onTokenReceived(access_token);
                    }
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

            }
        });

    }


    public interface AuthenticationListener {
        void onTokenReceived(String auth_token);
    }
}


