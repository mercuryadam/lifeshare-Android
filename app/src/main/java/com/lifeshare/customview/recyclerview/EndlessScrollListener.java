package com.lifeshare.customview.recyclerview;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class EndlessScrollListener extends RecyclerView.OnScrollListener {
//    public static String TAG = EndlessScrollListener.class.getSimpleName();

    public int previousTotal = 0; // The total number of items in the dataset after the last load
    int firstVisibleItem;
    int visibleItemCount;
    int totalItemCount;
    private boolean loading = true; // True if we are still waiting for the last set of data to load.
    private int visibleThreshold = 1; // The minimum amount of items to have below your current scroll position before loading more.
    private int current_page = 0;

    private LinearLayoutManager mLinearLayoutManager;
    private GridLayoutManager mGridLayoutManager;

    public EndlessScrollListener(LinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
    }

    public EndlessScrollListener(GridLayoutManager gridLayoutManager) {
        this.mGridLayoutManager = gridLayoutManager;
    }

    public EndlessScrollListener() {

    }

    public void resetScrollData() {
        previousTotal = 0; // The total number of items in the dataset after the last load
        loading = true; // True if we are still waiting for the last set of data to load.
        visibleThreshold = 1; // The minimum amount of items to have below your current scroll position before loading more.
        firstVisibleItem = 0;
        visibleItemCount = 0;
        totalItemCount = 0;
        current_page = 0;

    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        visibleItemCount = recyclerView.getChildCount();
        if (mGridLayoutManager == null) {
            totalItemCount = mLinearLayoutManager.getItemCount();
            firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount)
                    <= (firstVisibleItem + visibleThreshold)) {
                current_page++;

                onLoadMore(current_page);

                loading = true;
            }
        } else {
            totalItemCount = mGridLayoutManager.getItemCount();
            firstVisibleItem = mGridLayoutManager.findFirstVisibleItemPosition();

            if (loading) {
                if (totalItemCount > previousTotal + 1) {
                    loading = false;
                    previousTotal = totalItemCount;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                // End has been reached
                // Do something
                current_page++;
                onLoadMore(current_page);
                loading = true;
            }
        }


    }

    public abstract void onLoadMore(int current_page);


}
