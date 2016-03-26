package com.wm.remusic.handler;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import java.lang.ref.WeakReference;

/**
 * Created by wm on 2016/3/26.
 */
public class HandlerUtil extends Handler {
    static class CommonHandler extends Handler {
        WeakReference<Context> mActivityReference;

        CommonHandler(Context activity) {
            mActivityReference = new WeakReference<>(activity);
        }
    }
}
