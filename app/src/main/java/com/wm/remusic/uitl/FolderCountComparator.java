package com.wm.remusic.uitl;

import android.util.Log;

import com.wm.remusic.info.FolderInfo;

import java.util.Comparator;

public class FolderCountComparator implements Comparator<FolderInfo> {

    @Override
    public int compare(FolderInfo a1, FolderInfo a2) {
        Integer py1 = a1.folder_count;
        Integer py2 = a2.folder_count;
        Log.e("compare", "py1 =  " + py1 + "   py2  = " + py2);
        return py2.compareTo(py1);
    }

    private boolean isEmpty(String str) {
        return "".equals(str.trim());
    }
}  