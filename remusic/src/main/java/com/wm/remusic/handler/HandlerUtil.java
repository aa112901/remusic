package com.wm.remusic.handler;

import android.content.Context;
import android.os.Handler;

import java.lang.ref.WeakReference;

/**
 * Created by wm on 2016/3/26.
 */
public class HandlerUtil extends Handler {

    private static HandlerUtil instance = null;
    WeakReference<Context> mActivityReference;

    public static HandlerUtil getInstance(Context context) {
        if (instance == null) {
            instance = new HandlerUtil(context.getApplicationContext());
        }
        return instance;
    }

    HandlerUtil(Context context) {
        mActivityReference = new WeakReference<>(context);
    }
}
