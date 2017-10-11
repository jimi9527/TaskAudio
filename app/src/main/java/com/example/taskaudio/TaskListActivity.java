package com.example.taskaudio;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * author: daxiong9527
 * mail : 15570350453@163.com
 */

public class TaskListActivity extends AppCompatActivity {

    Button mBtnFirst , mBtnTwo,mBtnThree,mBtnFour ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasklist);

        initView();

    }

    private void initView() {

        mBtnFirst = (Button) findViewById(R.id.first);
        mBtnTwo = (Button) findViewById(R.id.two);
        mBtnThree = (Button) findViewById(R.id.three);
        mBtnFour = (Button) findViewById(R.id.four);

        mBtnFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskListActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        mBtnTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskListActivity.this,TaskActivity.class);
                startActivity(intent);
            }
        });

        mBtnThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskListActivity.this,TaskTwoActivity.class);
                startActivity(intent);
            }
        });

        mBtnFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskListActivity.this,TaskThreeActivity.class);
                startActivity(intent);
            }
        });

    }
}
