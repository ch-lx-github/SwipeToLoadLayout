package com.aspsine.swipetoloadlayout.demo.wsl;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.aspsine.swipetoloadlayout.demo.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by wsl on 16-12-1.
 */

public class TestMainActivity extends AppCompatActivity{

    @OnClick(R.id.test) void onClickTest() {
        Intent i = new Intent(this, TestActivity3.class);
        startActivity(i);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_main);
        ButterKnife.bind(this);
    }
}
