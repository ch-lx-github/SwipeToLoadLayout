package com.aspsine.swipetoloadlayout.demo.wsl;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by wsl on 16-12-2.
 */

public class TestRecyclerView extends RecyclerView {

    public TestRecyclerView(Context context) {
        super(context);
    }

    public TestRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TestRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        Log.d("xxx", "dispatchNestedPreScroll before dy: " + dy
                + "---consumed: " + dumpArray(consumed)
                + "---offsetInWindow: " + dumpArray(offsetInWindow));
        boolean res = super.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
        Log.d("xxx", "dispatchNestedPreScroll after dy: " + dy
                + "---consumed: " + dumpArray(consumed)
                + "---offsetInWindow: " + dumpArray(offsetInWindow));
        return res;
    }

    private String dumpArray(int[] temp) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(temp[0]);
        sb.append(",");
        sb.append(temp[1]);
        sb.append("]");
        return sb.toString();
    }
}
