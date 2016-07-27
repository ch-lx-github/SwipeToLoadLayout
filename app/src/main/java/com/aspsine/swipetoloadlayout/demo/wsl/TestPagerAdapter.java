package com.aspsine.swipetoloadlayout.demo.wsl;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by wsl on 16-7-20.
 */
public class TestPagerAdapter extends FragmentPagerAdapter {

    private static final int COUNT = 3;
    private static final String[] TITLES = {
            "tab0",
            "tab1",
            "tab2"
    };

    public TestPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return TestFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }
}
