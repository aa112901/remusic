package com.wm.remusic.uitl.Comparator;

import android.util.Log;

import com.wm.remusic.info.MusicInfo;

import java.util.Comparator;

public class MusicComparator implements Comparator<MusicInfo> {

    @Override
    public int compare(MusicInfo m1, MusicInfo m2) {
        String py1 = m1.sort;
        String py2 = m2.sort;
        // 判断是否为空""  
        if (isEmpty(py1) && isEmpty(py2))
            return 0;
        if (isEmpty(py1))
            return -1;
        if (isEmpty(py2))
            return 1;
        return py1.compareTo(py2);
    }

    private boolean isEmpty(String str) {
        return "".equals(str.trim());
    }
}  