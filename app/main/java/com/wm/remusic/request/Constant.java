package com.wm.remusic.request;

import android.os.Environment;

import java.io.File;

/**
 * Created by Administrator on 2016/7/8.
 */
public class Constant {

    public static final String SAVE_PATH = Environment.getExternalStorageDirectory().getPath() + File.separatorChar + "uiMonitor" + File.separator;
}
