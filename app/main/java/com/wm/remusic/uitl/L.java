package com.wm.remusic.uitl;

import android.util.Log;

/**
 * Created by wm on 2016/8/16.
 */
public class L {
    public static void D(boolean print, String tag, String content) {
        if (print)
            Log.d(tag, content);
    }

    public static void E(boolean print, String tag, String content) {
        if (print)
            Log.e(tag, content);
    }
}
