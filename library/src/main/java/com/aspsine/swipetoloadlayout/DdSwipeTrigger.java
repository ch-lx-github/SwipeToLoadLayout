package com.aspsine.swipetoloadlayout;

/**
 * Created by wsl on 16-12-1.
 */

public interface DdSwipeTrigger {
    void onPrepare();
    void onRelease();
    void onProgress();
    void onComplete();
}
