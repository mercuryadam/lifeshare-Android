package com.lifeshare.customview.recyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SearchView;
import android.widget.TextView;

import com.lifeshare.R;


public class CustomSearchView extends SearchView {

    private int typefaceCode;
    private int color;

    public CustomSearchView(Context context) {
        super(context);
    }

    public CustomSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            int id = getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
            TextView textView = (TextView) findViewById(id);

            textView.setTextAppearance(context, R.style.TextRegular_Black_size16);
            //auto focus on start is removed don't remove following line
            setFocusable(false);

            if (isInEditMode()) {
                return;
            }
        }

    }
}
