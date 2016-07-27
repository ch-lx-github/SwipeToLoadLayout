package com.aspsine.swipetoloadlayout.demo.wsl;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.aspsine.swipetoloadlayout.demo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wsl on 16-7-20.
 */
public class TestFragment extends Fragment implements OnLoadMoreListener {

    private static final String KEY_INDEX = "index";

    private int index;
    private RecyclerView recyclerView;
    private TestAdapter adapter;
    private SwipeToLoadLayout swipeToLoadLayout;

    public static TestFragment newInstance(int index) {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_INDEX, index);

        TestFragment fragment = new TestFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        index = bundle.getInt(KEY_INDEX);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_test, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void initViews(View view) {
        swipeToLoadLayout = (SwipeToLoadLayout) view.findViewById(R.id.swipeToLoadLayout);
        swipeToLoadLayout.setOnLoadMoreListener(this);

        adapter = new TestAdapter(mockData());

        recyclerView = (RecyclerView) view.findViewById(R.id.swipe_target);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }

    private List<String> mockData() {
        List<String> data = new ArrayList<>();
        switch (index) {
            case 0:
                for (int i = 0; i < 5; i++) {
                    data.add("index=" + index + " and value=" + i);
                }
                break;
            case 1:
                for (int i = 0; i < 6; i++) {
                    data.add("index=" + index + " and value=" + i);
                }
                break;
            case 2:
                for (int i = 0; i < 20; i++) {
                    data.add("index=" + index + " and value=" + i);
                }
                break;
        }
        return data;
    }

    @Override
    public void onLoadMore() {
        swipeToLoadLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeToLoadLayout.setLoadingMore(false);
                adapter.insert("loading more value");
            }
        }, 5000);
    }
}