package com.aspsine.swipetoloadlayout.demo.wsl;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.aspsine.swipetoloadlayout.demo.R;

/**
 * Created by wsl on 16-7-19.
 */
public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initViews();
    }

    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tableLayout = (TabLayout) findViewById(R.id.tableLayout);

        TestPagerAdapter pagerAdapter = new TestPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(pagerAdapter);
        tableLayout.setupWithViewPager(viewPager);
    }
}
