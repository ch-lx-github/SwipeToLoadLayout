package com.aspsine.swipetoloadlayout.demo.wsl;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.aspsine.swipetoloadlayout.DdToLoadLayout;
import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.demo.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wsl on 16-12-2.
 */

public class TestActivity3 extends AppCompatActivity{

    @BindView(R.id.swipe_target)
    RecyclerView recyclerView;

    @BindView(R.id.ddToLoadLayout)
    DdToLoadLayout ddToLoadLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test3);
        ButterKnife.bind(this);
        initViews();
    }

    private void initViews() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new TestAdapter(mockData()));

        ddToLoadLayout.setOnRefreshListener(new OnRefreshListener() {
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
        ddToLoadLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                ddToLoadLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ddToLoadLayout.setLoadMore(false);
                    }
                }, 2000);
            }
        });

    }

    private List<String> mockData() {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            data.add("index=" + i + " and value=" + i);
        }
        return data;
    }
}
