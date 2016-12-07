package com.aspsine.swipetoloadlayout.demo.wsl;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.DdSwipeTrigger;

/**
 * Created by wsl on 16-11-30.
 */

public class RefreshHeaderView extends TextView implements DdSwipeTrigger {

    public RefreshHeaderView(Context context) {
        super(context);
    }

    public RefreshHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RefreshHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onPrepare() {
        setText("下拉刷新");
    }

    @Override
    public void onRelease() {
        setText("释放刷新");
    }

    @Override
    public void onComplete() {
        setText("刷新完成");
    }

    @Override
    public void onProgress() {
        setText("正在刷新");
    }
}
