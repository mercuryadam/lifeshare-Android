package com.lifeshare.customview.bubbleview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * Created by chirag.patel on 21/11/18.
 */

public class BubbleBaseLayout extends FrameLayout {
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private BubblesLayoutCoordinator layoutCoordinator;

    public BubbleBaseLayout(Context context) {
        super(context);
    }

    public BubbleBaseLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BubbleBaseLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    BubblesLayoutCoordinator getLayoutCoordinator() {
        return layoutCoordinator;
    }

    void setLayoutCoordinator(BubblesLayoutCoordinator layoutCoordinator) {
        this.layoutCoordinator = layoutCoordinator;
    }

    WindowManager getWindowManager() {
        return this.windowManager;
    }

    void setWindowManager(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    WindowManager.LayoutParams getViewParams() {
        return this.params;
    }

    void setViewParams(WindowManager.LayoutParams params) {
        this.params = params;
    }
}