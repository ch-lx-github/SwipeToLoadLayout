package com.aspsine.swipetoloadlayout;

/**
 * Created by wsl on 16-11-30.
 */

public class DdMathUtils {

    static int constrain(int amount, int low, int high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

    static float constrain(float amount, float low, float high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

}
