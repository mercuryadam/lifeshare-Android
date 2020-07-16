package com.lifeshare.customview.recyclerview;

import android.support.annotation.StringRes;
import android.view.View;

public interface BaseRecyclerListener<T> {
    void showEmptyDataView(@StringRes int resId);

    void onRecyclerItemClick(View view, int position, T item);
}
