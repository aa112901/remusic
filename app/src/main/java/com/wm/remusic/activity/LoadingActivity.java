package com.wm.remusic.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.wm.remusic.R;

/**
 * Created by wm on 2016/3/19.
 */
public class LoadingActivity extends Activity {

    //延迟3秒
    private static final long SPLASH_DELAY_MILLIS = 1600;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // 使用Handler的postDelayed方法，3秒后执行跳转到MainActivity
        new Handler().postDelayed(new Runnable() {
            public void run() {
                goHome();
            }
        }, SPLASH_DELAY_MILLIS);
    }

    private void goHome() {
        Intent intent = new Intent(LoadingActivity.this, MainActivity.class);

        LoadingActivity.this.startActivity(intent);
        LoadingActivity.this.finish();
    }
}
