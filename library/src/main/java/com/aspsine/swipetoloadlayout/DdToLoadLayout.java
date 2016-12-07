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
 * Refresh and load more feature layout
 * Created by wsl on 16-11-30.
 */

public class DdToLoadLayout extends ViewGroup implements NestedScrollingChild, NestedScrollingParent {

    private static final String TAG = DdToLoadLayout.class.getSimpleName();

    private static final long DELAYED_TIME_COMPLETE = 300;
    private static final int STATE_ORIGIN = 0;

    private static final int STATE_REFRESH_PREPARE = 1;
    private static final int STATE_REFRESH_RELEASE = 2;
    private static final int STATE_REFRESH_PROGRESS = 3;
    //trigger by user and offset == progress
    private static final int STATE_REFRESH_COMPLETE = 4;

    private static final int STATE_LOAD_MORE_PREPARE = 5;
    private static final int STATE_LOAD_MORE_RELEASE = 6;
    private static final int STATE_LOAD_MORE_PROGRESS = 7;
    //trigger by user and offset == progress
    private static final int STATE_LOAD_MORE_COMPLETE = 8;

    private static final int DIRECTION_DOWN = 1;
    private static final int DIRECTION_UP = -1;
    private static final int DIRECTION_ORIGIN = 0;

    private NestedScrollingChildHelper mNestedScrollingChildHelper;
    private DdScrollHelper mDdScrollHelper;

    private int mRefreshTriggerOffset;
    private int mLoadMoreTriggerOffset;

    private boolean mRefreshEnabled;
    private boolean mLoadMoreEnabled;

    private View mHeaderView;
    private View mTargetView;
    private View mFooterView;

    private int mState;
    private boolean mWasFlung;
    private int mLastNestedDirection;
    private boolean mFirstNestedPreScroll;
    private boolean mSkipNestPreScroll;

    private OnRefreshListener mRefreshListener;
    private OnLoadMoreListener mLoadMoreListener;

    private CompleteRunnable mCompleteRunnable;


    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];

    private boolean mDebug = true;

    public void setDebug(boolean debug) {
        this.mDebug = debug;
    }

    private void printLog(String msg) {
        if (mDebug) {
            Log.d(TAG, msg);
        }
    }

    public DdToLoadLayout(Context context) {
        this(context, null);
    }

    public DdToLoadLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DdToLoadLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mDdScrollHelper = new DdScrollHelper(this);
        mDdScrollHelper.setListener(new DdScrollListener());

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DdToLoadLayout);
        mLoadMoreEnabled = a.getBoolean(R.styleable.DdToLoadLayout_dd_swipe_enabled_load_more, false);
        mRefreshEnabled = a.getBoolean(R.styleable.DdToLoadLayout_dd_swipe_enabled_refresh, false);
        a.recycle();

        setNestedScrollingEnabled(true);
    }

    private void setState(int state) {
        this.mState = state;
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {

        boolean started = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
//                && isOrigin();
//                && !isRefreshingOrLoadingMore();

//        mLastNestedDirection = DIRECTION_ORIGIN;

        //// TODO: 16-11-30  may cancel auto scroll or animation
        return started;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        //do nothing
        super.onNestedScrollAccepted(child, target, axes);

        if((axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0) {
            mFirstNestedPreScroll = true;

            //start nested to dependent view, such as DdHeaderLayout
            startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
        }
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if (dy != 0) {

            int min = 0;
            int max = 0;
            int measuredHeight = getMeasuredHeight();

            boolean handled = false;

            if (dy < 0) {
                // We're scrolling down, content scrolling up
                boolean targetCanScrollDown = ViewCompat.canScrollVertically(target, -1);
                if(!targetCanScrollDown) {
                    if (dispatchNestedPreScroll(dx, dy, mScrollConsumed, mScrollOffset)) {
                        //dispatch nested scroll to dependent view, such as DdHeaderLayout
//                        dx -= mScrollConsumed[0];
//                        dy -= mScrollConsumed[1];
                        consumed[1] = mScrollConsumed[1];
                        return;
                    }
                }

                if(mFirstNestedPreScroll && !isOrigin()) {
                    //first nested pre scroll state must be origin
                    return;
                }

                if(mRefreshEnabled && !mSkipNestPreScroll && (!targetCanScrollDown || (mLastNestedDirection == DIRECTION_UP))) {
                    handled = true;
                    switch (mLastNestedDirection) {
                        case DIRECTION_ORIGIN:
                            mLastNestedDirection = DIRECTION_DOWN;
                        case DIRECTION_DOWN:
                            min = 0;
                            max = measuredHeight;
                            break;
                        case DIRECTION_UP:
                            min = -measuredHeight;
                            max = 0;
                            break;
                    }
                }
            } else {
                // We're scrolling up, content scrolling down
                Log.d("yyy", "onNestedPreScroll scroll up before dy: " + dy);
                if (dispatchNestedPreScroll(dx, dy, mScrollConsumed, mScrollOffset)) {
                    //dispatch nested scroll to dependent view, such as DdHeaderLayout
                    consumed[1] = mScrollConsumed[1];
                    return;
                }

                if(mFirstNestedPreScroll && !isOrigin()) {
                    //first nested pre scroll state must be origin
                    return;
                }

                boolean targetCanScrollUp = ViewCompat.canScrollVertically(target, 1);
                if(mLoadMoreEnabled && !mSkipNestPreScroll && (!targetCanScrollUp ||
                        (mLastNestedDirection == DIRECTION_DOWN))) {
                    handled = true;
                    switch (mLastNestedDirection) {
                        case DIRECTION_ORIGIN:
                            mLastNestedDirection = DIRECTION_UP;
                        case DIRECTION_UP:
                            min = -measuredHeight;
                            max = 0;
                            break;
                        case DIRECTION_DOWN:
                            min = 0;
                            max = measuredHeight;
                            break;

                    }
                }
            }

            if(handled) {
                mFirstNestedPreScroll = false;
                consumed[1] = mDdScrollHelper.scroll(dy, min, max);
            }
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
//        Log.d("debug", "onNestedScroll dyUnconsumed: " + dyUnconsumed + "---dyConsumed: " + dyConsumed);
        if(dyUnconsumed < 0) {
            // If the scrolling view is scrolling down but not consuming, it's probably be at
            // the top of it's content
            Log.d("debug", "onNestedScroll before dyUnconsumed: " + dyUnconsumed);
            if (dispatchNestedPreScroll(dxUnconsumed, dyUnconsumed, mScrollConsumed, mScrollOffset)) {
//                dxUnconsumed -= mScrollConsumed[0];
                dyUnconsumed -= mScrollConsumed[1];
                Log.d("debug", "onNestedScroll after dyUnconsumed: " + dyUnconsumed);
            }

            if(dyUnconsumed < 0) {
                //still has unconsumed dy
                int min = -getMeasuredHeight();
                int max = 0;
                mDdScrollHelper.scroll(dyUnconsumed, min, max);
            }
        } else {
            Log.d("debug", "onNestedScroll scrolling up");
        }
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return true;
//        return super.onNestedPreFling(target, velocityX, velocityY);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return super.onNestedFling(target, velocityX, velocityY, consumed);
    }

    @Override
    public void onStopNestedScroll(View child) {
        //stop nested to dependent view, such as DdHeaderLayout
        stopNestedScroll();

        mFirstNestedPreScroll = true;
        mLastNestedDirection = DIRECTION_ORIGIN;
        //may auto scroll
        if (!mWasFlung) {
            if(!isOrigin()) {
                mSkipNestPreScroll = true;
            }
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
                break;
            default:
                break;
        }
    }

    // NestedScrollingChild start
    private NestedScrollingChildHelper getScrollingChildHelper() {
        if (mNestedScrollingChildHelper == null) {
            mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        }
        return mNestedScrollingChildHelper;
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        getScrollingChildHelper().setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return getScrollingChildHelper().isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return getScrollingChildHelper().startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        getScrollingChildHelper().stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return getScrollingChildHelper().hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        return getScrollingChildHelper().dispatchNestedScroll(dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return getScrollingChildHelper().dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return getScrollingChildHelper().dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return getScrollingChildHelper().dispatchNestedPreFling(velocityX, velocityY);
    }

    // NestedScrollingChild end

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mCompleteRunnable != null) {
            removeCallbacks(mCompleteRunnable);
        }
        super.onDetachedFromWindow();
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

        if (mHeaderView == null) {
            mRefreshEnabled = false;
        }

        if (mFooterView == null) {
            mLoadMoreEnabled = false;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
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
        int currOffset = mDdScrollHelper.getCurrentScrollOffset();
        mDdScrollHelper.autoScroll(this, -currOffset);
    }

    private void scrollToRefresh() {
        int currOffset = mDdScrollHelper.getCurrentScrollOffset();
        int dy;
        if (currOffset == 0) {
            dy = mRefreshTriggerOffset;
            mDdScrollHelper.autoScroll(this, dy, 2000);
        } else if (currOffset > 0 && currOffset > mRefreshTriggerOffset) {
            dy = -currOffset + mRefreshTriggerOffset;
            mDdScrollHelper.autoScroll(this, dy);
        }
    }

    private void scrollToLoadMore() {
        int currOffset = mDdScrollHelper.getCurrentScrollOffset();
        if (currOffset < 0 && -currOffset > mLoadMoreTriggerOffset) {
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

    private boolean isOrigin() {
        return mState == STATE_ORIGIN;
    }

    private boolean isLoadingMore() {
        return mState == STATE_LOAD_MORE_PROGRESS;
    }

    public void setRefresh(boolean refresh) {
        if (refresh) {
            //// TODO: 16-11-30
            if (isOrigin()) {
                scrollToRefresh();
            }
        } else {
            if (isRefreshing()) {
                setState(STATE_REFRESH_COMPLETE);
                callbackRefresh(STATE_REFRESH_COMPLETE);
                delayComplete();
            }
        }
    }

    public void setLoadMore(boolean loadMore) {
        if (loadMore) {
            //// TODO: 16-11-30
        } else {
            if (isLoadingMore()) {
                setState(STATE_LOAD_MORE_COMPLETE);
                callbackLoadMore(STATE_LOAD_MORE_COMPLETE);
                delayComplete();
            }
        }
    }

    private void delayComplete() {
        if (mCompleteRunnable == null) {
            mCompleteRunnable = new CompleteRunnable();
        } else {
            removeCallbacks(mCompleteRunnable);
        }
        postDelayed(mCompleteRunnable, DELAYED_TIME_COMPLETE);
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mRefreshListener = listener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.mLoadMoreListener = listener;
    }

    public void setRefreshEnabled(boolean enabled) {
        this.mRefreshEnabled = enabled;
    }

    public void setLoadMoreEnabled(boolean enabled) {
        this.mLoadMoreEnabled = enabled;
    }

    private void callbackRefresh(int state) {
        if (mHeaderView == null) {
            return;
        }
        if (!(mHeaderView instanceof DdSwipeTrigger)) {
            return;
        }
        DdSwipeTrigger refreshTrigger = (DdSwipeTrigger) mHeaderView;
        switch (state) {
            case STATE_REFRESH_PREPARE:
                refreshTrigger.onPrepare();
                break;
            case STATE_REFRESH_RELEASE:
                refreshTrigger.onRelease();
                break;
            case STATE_REFRESH_PROGRESS:
                refreshTrigger.onProgress();
                break;
            case STATE_REFRESH_COMPLETE:
                refreshTrigger.onComplete();
                break;
        }
    }

    private void callbackLoadMore(int state) {
        if (mFooterView == null) {
            return;
        }
        if (!(mFooterView instanceof DdSwipeTrigger)) {
            return;
        }
        DdSwipeTrigger loadMoreTrigger = (DdSwipeTrigger) mFooterView;
        switch (state) {
            case STATE_LOAD_MORE_PREPARE:
                loadMoreTrigger.onPrepare();
                break;
            case STATE_LOAD_MORE_RELEASE:
                loadMoreTrigger.onRelease();
                break;
            case STATE_LOAD_MORE_PROGRESS:
                loadMoreTrigger.onProgress();
                break;
            case STATE_LOAD_MORE_COMPLETE:
                loadMoreTrigger.onComplete();
                break;
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

    private class CompleteRunnable implements Runnable {

        @Override
        public void run() {
            scrollToOrigin();
        }
    }

    private class DdScrollListener implements DdScrollHelper.Listener {
        @Override
        public void onOffsetUpdate(int curOffset) {
            printLog(TAG + "------curOffset: " + curOffset);
            //check state and callback to header or footer that display some style
            if (curOffset < 0) {
                //load more
                if (curOffset > -mLoadMoreTriggerOffset) {
                    //load more prepare
                    setState(STATE_LOAD_MORE_PREPARE);
                    callbackLoadMore(STATE_LOAD_MORE_PREPARE);
                    printLog(TAG + "------load more prepare state");
                } else if (curOffset == -mLoadMoreTriggerOffset) {
                    //load more progress
                    setState(STATE_LOAD_MORE_PROGRESS);
                    callbackLoadMore(STATE_LOAD_MORE_PROGRESS);
                    printLog(TAG + "------load more progress state");
                    if (mLoadMoreListener != null) {
                        mLoadMoreListener.onLoadMore();
                    }
                } else {
                    //load more release
                    setState(STATE_LOAD_MORE_RELEASE);
                    callbackLoadMore(STATE_LOAD_MORE_RELEASE);
                    printLog(TAG + "------load more release state");
                }
            } else if (curOffset > 0) {
                //refresh
                if (curOffset < mRefreshTriggerOffset) {
                    //refresh prepare
                    setState(STATE_REFRESH_PREPARE);
                    callbackRefresh(STATE_REFRESH_PREPARE);
                    printLog(TAG + "------refresh prepare state");
                } else if (curOffset == mRefreshTriggerOffset) {
                    //refresh progress
                    setState(STATE_REFRESH_PROGRESS);
                    callbackRefresh(STATE_REFRESH_PROGRESS);
                    printLog(TAG + "------refresh progress state");
                    if (mRefreshListener != null) {
                        mRefreshListener.onRefresh();
                    }
                } else {
                    //refresh release
                    setState(STATE_REFRESH_RELEASE);
                    callbackRefresh(STATE_REFRESH_RELEASE);
                    printLog(TAG + "------refresh release state");
                }
            } else {
                //origin state
                setState(STATE_ORIGIN);
                printLog(TAG + "------origin state");
                mSkipNestPreScroll = false;
            }
        }
    }
}