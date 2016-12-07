package com.aspsine.swipetoloadlayout.demo.wsl;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.DdSwipeTrigger;


/**
 * Created by wsl on 16-7-20.
 */
public class LoadMoreFooterView extends TextView implements DdSwipeTrigger {
    public LoadMoreFooterView(Context context) {
        super(context);
    }

    public LoadMoreFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onPrepare() {
        setText("上拉加载");
    }

    @Override
    public void onRelease() {
        setText("释放加载");
    }

    @Override
    public void onComplete() {
        setText("加载完成");
    }

    @Override
    public void onProgress() {
        setText("正在加载");
    }
}