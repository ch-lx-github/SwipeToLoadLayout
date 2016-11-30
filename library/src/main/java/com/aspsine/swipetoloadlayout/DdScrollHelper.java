package com.aspsine.swipetoloadlayout;

import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.view.View;

/**
 * DdToLoadLayout scroll helper
 * Created by wsl on 16-11-30.
 */

final class DdScrollHelper {

    private static final int AUTO_SCROLL_DURATION = 500;

    interface Listener {
        void onOffsetUpdate(int offset);
    }

    private ViewOffsetHelper mViewOffsetHelper;

    private Listener mListener;

    private ScrollerCompat mAutoScroller;
    private AutoScrollRunnable mAutoScrollRunnable;

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    /**
     * View must be DdToLoadLayout
     * @param view
     */
    public DdScrollHelper(View view) {
        mViewOffsetHelper = new ViewOffsetHelper(view);
    }

    public void onViewLayout() {
        mViewOffsetHelper.onViewLayout();
    }

    final int scroll(int dy, int minOffset, int maxOffset) {
        return setHeaderTopBottomOffset(getTopAndBottomOffset() - dy, minOffset, maxOffset);
    }

    private int setHeaderTopBottomOffset(int newOffset) {
        return setHeaderTopBottomOffset(newOffset,
                Integer.MIN_VALUE, Integer.MAX_VALUE);
    }


    private int setHeaderTopBottomOffset(int newOffset, int minOffset, int maxOffset) {
        final int curOffset = getTopAndBottomOffset();
        int consumed = 0;

//        if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
        if (curOffset >= minOffset && curOffset <= maxOffset) {
            // If we have some scrolling range, and we're currently within the min and max
            // offsets, calculate a new offset
            newOffset = DdMathUtils.constrain(newOffset, minOffset, maxOffset);

            if (curOffset != newOffset) {
                mViewOffsetHelper.setTopAndBottomOffset(newOffset);
                // Update how much dy we have consumed
                consumed = curOffset - newOffset;
            }

            dispatchOffsetUpdates();
        }

        return consumed;
    }

    final int getTopAndBottomOffset() {
        return mViewOffsetHelper.getTopAndBottomOffset();
    }

    private void dispatchOffsetUpdates() {
        int curOffset = getTopAndBottomOffset();
        if(mListener != null) {
            mListener.onOffsetUpdate(curOffset);
        }
    }

    final void abortAutoScroll(View layout) {
        if (mAutoScrollRunnable != null) {
            layout.removeCallbacks(mAutoScrollRunnable);
            mAutoScrollRunnable = null;
        }
        if(mAutoScroller != null) {
            if(!mAutoScroller.isFinished()) {
                mAutoScroller.abortAnimation();
            }
        }
    }

    /**
     *
     * return to origin or REFRESHING or LOAD_MORE state
     * @param layout DdToLoadLayout
     * @param dy offset
     * @return true means can auto scroll
     */
    final boolean autoScroll(View layout, int dy) {
        if (mAutoScrollRunnable != null) {
            layout.removeCallbacks(mAutoScrollRunnable);
            mAutoScrollRunnable = null;
        }

        if (mAutoScroller == null) {
            mAutoScroller = ScrollerCompat.create(layout.getContext());
        }
        mAutoScroller.startScroll(0, getTopAndBottomOffset(), 0, dy, AUTO_SCROLL_DURATION);
        if (mAutoScroller.computeScrollOffset()) {
            mAutoScrollRunnable = new AutoScrollRunnable(layout);
            ViewCompat.postOnAnimation(layout, mAutoScrollRunnable);
            return true;
        }
        return false;
    }

    private class AutoScrollRunnable implements Runnable {
        private final View mLayout;

        AutoScrollRunnable(View layout) {
            mLayout = layout;
        }

        @Override
        public void run() {
            if (mLayout != null && mAutoScroller != null && mAutoScroller.computeScrollOffset()) {
                setHeaderTopBottomOffset(mAutoScroller.getCurrY());

                // Post ourselves so that we run on the next animation
                ViewCompat.postOnAnimation(mLayout, this);
            }
        }
    }
}