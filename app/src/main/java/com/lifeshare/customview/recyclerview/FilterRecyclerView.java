package com.lifeshare.customview.recyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class FilterRecyclerView extends RecyclerView {

    private TextView tvEmptyMsgHolder;

    public FilterRecyclerView(Context context) {
        super(context);
    }

    public FilterRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FilterRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public View getEmptyMsgHolder() {
        return tvEmptyMsgHolder;
    }

    public void setEmptyMsgHolder(TextView tvEmptyMsgHolder) {
        this.tvEmptyMsgHolder = tvEmptyMsgHolder;
    }

    public void showEmptyDataView(String errorMessage) {


        if (tvEmptyMsgHolder != null) {
            if (getAdapter() != null && getAdapter().getItemCount() == 0) {

                tvEmptyMsgHolder.setVisibility(View.VISIBLE);
                tvEmptyMsgHolder.setText(errorMessage);

            } else {
                tvEmptyMsgHolder.setVisibility(View.GONE);
            }
        }
    }

}
