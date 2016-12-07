package com.aspsine.swipetoloadlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by wsl on 16-11-30.
 */

public class DdOffsetLayout extends ViewGroup implements NestedScrollingChild, NestedScrollingParent {

    public interface Listener {
        void onRefresh();
    }

    private static final String TAG = DdOffsetLayout.class.getSimpleName();
    private static final int STATE_ORIGIN = 0;

    private static final int STATE_REFRESH_PREPARE = 1;
    private static final int STATE_REFRESH_RELEASE = 2;
    private static final int STATE_REFRESH_PROGRESS = 3;

    private static final int STATE_LOAD_MORE_PREPARE = 4;
    private static final int STATE_LOAD_MORE_RELEASE = 5;
    private static final int STATE_LOAD_MORE_PROGRESS = 6;

    private NestedScrollingChildHelper mNestedScrollingChildHelper;
    private DdOffsetHelper mDdScrollHelper;

    private int mRefreshTriggerOffset;
    private int mLoadMoreTriggerOffset;

    private View mHeaderView;
    private View mTargetView;
    private View mFooterView;

    private int mState;
    private boolean mWasFlung;

    private Listener mListener;

    private boolean mDebug = true;

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public void setDebug(boolean debug) {
        this.mDebug = debug;
    }

    private void printLog(String msg) {
        if(mDebug) {
            Log.d(TAG, msg);
        }
    }

    public DdOffsetLayout(Context context) {
        this(context, null);
    }

    public DdOffsetLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DdOffsetLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mDdScrollHelper = new DdOffsetHelper(this);
        mDdScrollHelper.setListener(new DdOffsetHelper.Listener() {
            @Override
            public void onOffsetUpdate(int curOffset) {
                printLog(TAG + "------curOffset: " + curOffset);
                //check state and callback to header or footer that display some style
                if (curOffset < 0) {
                    //load more
                    if (curOffset > -mLoadMoreTriggerOffset) {
                        //load more prepare
                        setState(STATE_LOAD_MORE_PREPARE);
                        printLog(TAG + "------load more prepare state");
                    } else if(curOffset == -mLoadMoreTriggerOffset) {
                        //load more progress
                        setState(STATE_LOAD_MORE_PROGRESS);
                        printLog(TAG + "------load more progress state");

                    } else {
                        //load more release
                        setState(STATE_LOAD_MORE_RELEASE);
                        printLog(TAG + "------load more release state");
                    }
                } else if (curOffset > 0) {
                    //refresh
                    if (curOffset < mRefreshTriggerOffset) {
                        //refresh prepare
                        setState(STATE_REFRESH_PREPARE);
                        printLog(TAG + "------refresh prepare state");
                    } else if(curOffset == mRefreshTriggerOffset) {
                        //refresh progress
                        setState(STATE_REFRESH_PROGRESS);
                        printLog(TAG + "------refresh progress state");
                        if(mListener != null) {
                            mListener.onRefresh();
                        }
                    } else {
                        //refresh release
                        setState(STATE_REFRESH_RELEASE);
                        printLog(TAG + "------refresh release state");
                    }
                } else {
                    //origin state
                    setState(STATE_ORIGIN);
                    printLog(TAG + "------origin state");
                }
            }
        });

        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        mNestedScrollingChildHelper.setNestedScrollingEnabled(true);

//        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DdOffsetLayout);
//        a.recycle();

    }

    private void setState(int state) {
        this.mState = state;
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        Log.d("test", "onStartNestedScroll ---------------");
        boolean started = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0
                && !isRefreshingOrLoadingMore();
        //// TODO: 16-11-30  may cancel auto scroll or animation
        return started;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        //do nothing
        super.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        Log.d("debug3", "onNestedPreScroll ---------------dy: " + dy
                + "---consumed0: " + consumed[0]
                + "---consumed1: " + consumed[1]);
        if (dy != 0) {
            int min;
            int max;
            if (dy < 0) {
                // We're scrolling down, content scrolling up
                //Target
                boolean targetCanScrollDown = ViewCompat.canScrollVertically(target, -1);
//                Log.d("test", "We're scrolling down and targetCanScrollDown = " + targetCanScrollDown);
                if(!targetCanScrollDown) {
                    //Now offset down based on dy
                    min = 0;
                    max = getMeasuredHeight();
                    consumed[1] = mDdScrollHelper.scroll(dy, min, max);
                }
            } else {
                // We're scrolling up, content scrolling down
                boolean targetCanScrollUp = ViewCompat.canScrollVertically(target, 1);
                if(!targetCanScrollUp) {
                    min = -getMeasuredHeight();
                    max = 0;
                    consumed[1] = mDdScrollHelper.scroll(dy, min, max);
                }
            }
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        Log.d("test", "onNestedScroll ---------------");
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return super.onNestedPreFling(target, velocityX, velocityY);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return super.onNestedFling(target, velocityX, velocityY, consumed);
    }

    @Override
    public void onStopNestedScroll(View child) {
        //may auto scroll
        Log.d("test", "onStopNestedScroll ---------------");
        if(!mWasFlung) {
            snapIfNeeded();
        }
        super.onStopNestedScroll(child);
    }

    private void snapIfNeeded() {
        switch (mState) {
            case STATE_REFRESH_PREPARE:
                scrollToOrigin();
                break;
            case STATE_REFRESH_RELEASE:
                scrollToRefresh();
                break;
            case STATE_LOAD_MORE_PREPARE:
                scrollToOrigin();
                break;
            case STATE_LOAD_MORE_RELEASE:
                scrollToLoadMore();
                setState(STATE_LOAD_MORE_PROGRESS);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mHeaderView = findViewById(R.id.swipe_refresh_header);
        mTargetView = findViewById(R.id.swipe_target);
        mFooterView = findViewById(R.id.swipe_load_more_footer);

        if (mTargetView == null) {
            throw new IllegalStateException("Target view must not null and id equal swipe_target");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // header
        if (mHeaderView != null) {
            final View headerView = mHeaderView;
            measureChildWithMargins(headerView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            int headerHeight = headerView.getMeasuredHeight();
            if (mRefreshTriggerOffset < headerHeight) {
                mRefreshTriggerOffset = headerHeight;
            }
        }

        // target
        if (mTargetView != null) {
            final View targetView = mTargetView;
            measureChildWithMargins(targetView, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }

        // footer
        if (mFooterView != null) {
            final View footerView = mFooterView;
            measureChildWithMargins(footerView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            int footerHeight = footerView.getMeasuredHeight();
            if (mLoadMoreTriggerOffset < footerHeight) {
                mLoadMoreTriggerOffset = footerHeight;
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutChildren();

        mDdScrollHelper.onViewLayout();
    }

    private void layoutChildren() {
        int measureHeight = getMeasuredHeight();
        int measureWidth = getMeasuredWidth();

        int parentLeft = this.getPaddingLeft();
        int parentRight = measureWidth - this.getPaddingRight();

        int left;
        int top;
        int right;
        int bottom;
        //layout header
        if (mHeaderView != null) {
            View headerView = mHeaderView;
            int headerWidth = headerView.getMeasuredWidth();
            int headerHeight = headerView.getMeasuredHeight();
            MarginLayoutParams lp = (MarginLayoutParams) headerView.getLayoutParams();
            left = parentLeft + lp.leftMargin;
            top = -headerHeight;
            right = Math.min(left + headerWidth, parentRight - lp.rightMargin);
            bottom = 0;

            headerView.layout(left, top, right, bottom);
        }

        //layout target
        if (mTargetView != null) {
            View targetView = mTargetView;
            int headerWidth = targetView.getMeasuredWidth();
            MarginLayoutParams lp = (MarginLayoutParams) targetView.getLayoutParams();
            left = parentLeft + lp.leftMargin;
            right = Math.min(left + headerWidth, parentRight - lp.rightMargin);
            top = 0;
            bottom = measureHeight;
            targetView.layout(left, top, right, bottom);
        }

        //layout footer
        if (mFooterView != null) {
            View footerView = mFooterView;
            int footerWidth = footerView.getMeasuredWidth();
            int footerHeight = footerView.getMeasuredHeight();
            MarginLayoutParams lp = (MarginLayoutParams) footerView.getLayoutParams();
            left = parentLeft + lp.leftMargin;
            top = measureHeight;
            right = Math.min(left + footerWidth, parentRight - lp.rightMargin);
            bottom = top + footerHeight;

            footerView.layout(left, top, right, bottom);
        }
    }

    private void scrollToOrigin() {
        int currOffset = mDdScrollHelper.getTopAndBottomOffset();
        mDdScrollHelper.autoScroll(this, -currOffset);
    }

    private void scrollToRefresh() {
        int currOffset = mDdScrollHelper.getTopAndBottomOffset();
        if(currOffset > 0 && currOffset > mRefreshTriggerOffset) {
            int dy = -currOffset + mRefreshTriggerOffset;
            mDdScrollHelper.autoScroll(this, dy);
        }
    }

    private void scrollToLoadMore() {
        int currOffset = mDdScrollHelper.getTopAndBottomOffset();
        if(currOffset < 0 && -currOffset > mLoadMoreTriggerOffset) {
            int dy = -currOffset - mLoadMoreTriggerOffset;
            mDdScrollHelper.autoScroll(this, dy);
        }
    }

    private boolean isRefreshingOrLoadingMore() {
        return mState == STATE_LOAD_MORE_PROGRESS ||
                mState == STATE_REFRESH_PROGRESS;
    }

    private boolean isRefreshing() {
        return mState == STATE_REFRESH_PROGRESS;
    }

    private boolean isLoadingMore() {
        return mState == STATE_LOAD_MORE_PROGRESS;
    }

    public void setRefresh(boolean refresh) {
        if(refresh) {
            //// TODO: 16-11-30
        } else {
            if(isRefreshing()) {
                scrollToOrigin();
            }
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}