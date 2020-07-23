package com.lifeshare.customview.recyclerview;

import android.view.View;

import androidx.annotation.StringRes;

public interface BaseRecyclerListener<T> {
    void showEmptyDataView(@StringRes int resId);

    void onRecyclerItemClick(View view, int position, T item);
}
