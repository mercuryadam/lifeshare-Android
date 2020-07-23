package com.lifeshare.customview.recyclerview;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.IntDef;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    public static final int ListView = 1;
    public static final int GridView = 2;
    private int space, listorGrid;//1 -List , 2 -Grid


    public SpacesItemDecoration(int space, @ViewType int listorGrid) {
        this.space = space;
        this.listorGrid = listorGrid;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {


        if (listorGrid == 2) {
            outRect.left = space;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildLayoutPosition(view) == 0 || parent.getChildLayoutPosition(view) == 1) {
                outRect.top = space;
            } else {
                outRect.top = 0;
            }
            if (parent.getChildLayoutPosition(view) % 2 == 0) {

                outRect.right = 0;

            } else {
                outRect.right = space;

            }
        } else {
            outRect.left = space;
            outRect.bottom = space;
            outRect.right = space;
            if (parent.getChildLayoutPosition(view) == 0) {
                outRect.top = space;
            } else {
                outRect.top = 0;
            }
        }


    }

    // Bundling them under one definition
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ListView, GridView})
    public @interface ViewType {
    }
}
