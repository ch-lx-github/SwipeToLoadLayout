package com.aspsine.swipetoloadlayout.demo.wsl;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.aspsine.swipetoloadlayout.DdToLoadLayout;
import com.aspsine.swipetoloadlayout.demo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wsl on 16-11-30.
 */

public class TestActivity2 extends AppCompatActivity {

    DdToLoadLayout ddToLoadLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test2);

        initViews();
    }

    private void initViews() {
        ddToLoadLayout = (DdToLoadLayout) findViewById(R.id.ddToLoadLayout);
        ddToLoadLayout.setListener(new DdToLoadLayout.Listener() {
            @Override
            public void onRefresh() {
                ddToLoadLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ddToLoadLayout.setRefresh(false);
                    }
                }, 2000);
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.swipe_target);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new TestAdapter(mockData()));
    }

    private List<String> mockData() {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            data.add("index=" + i + " and value=" + i);
        }
        return data;
    }
}
