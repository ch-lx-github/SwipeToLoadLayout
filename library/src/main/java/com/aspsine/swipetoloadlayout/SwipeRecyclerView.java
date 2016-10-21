package com.aspsine.swipetoloadlayout;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 *
 * Created by wsl on 16-10-21.
 */

public class SwipeRecyclerView extends RecyclerView{

    interface Listener {
        void onActionSetAdapter(Adapter adapter);
    }

    private Listener listener;

    public SwipeRecyclerView(Context context) {
        super(context);
    }

    public SwipeRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if(listener != null) {
            listener.onActionSetAdapter(adapter);
        }
    }
}
