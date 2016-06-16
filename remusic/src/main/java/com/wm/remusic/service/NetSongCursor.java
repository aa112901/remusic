package com.wm.remusic.service;

import android.database.AbstractCursor;
import android.database.CursorWindow;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by wm on 2016/6/14.
 */
public class NetSongCursor extends AbstractCursor {

    //构建cursor时必须先传入列明数组以规定列数
    private String[] columnNames = new String[]{
            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.SIZE};
    private int allDataCnt = 0;//总记录行数
    private int columnNum = 0;
    private int logicNum = 0;
    private int currentPosition = 0;
    private final int MAX_SHOW_NUM = 10;
    private String TAG = " ";

    /**
     * 数据区域
     */
    private ArrayList<ArrayList<String>> allDatas = new ArrayList<ArrayList<String>>();//在构造的时候填充数据，里层数据的size=columnNames.leng
    private ArrayList<ArrayList<String>> currentDatas = null;//在fillwindow时填充
    private ArrayList<String> oneLineData = null;//onMove时填充
    ArrayList<String> list = new ArrayList<>();

    //父类帮我们维护了一个nPose变量，即游标当前所在行数，我们需要在父类构造的时候知道我们要让他知道我们需要他帮我们维护多少列，这个需要重写getColumnNames()方法:
    public NetSongCursor(ArrayList<String> all) {
       list = all;
    }

    /**
     * 获取列名称
     */
    @Override
    public String[] getColumnNames() {
        return columnNames;//此变量必须保证在cursor构造时已经赋值
    }


    //  cursor构造首先要执行fillwindow,在fillwindow中规定cursor一次取的数据，如果不需要规定每次取值多少可以不实现此方法，在onMove中用总数据返回。在此我们是在fillwindow中规定了每次返回多少数据，交给onMove来取值返回,fillwindow会在页面刷新的时候被cursor调用,如光标移动到列表最后一行往下移动或移动到最上一行往上移动。
    @Override
    public void fillWindow(int position, CursorWindow window) {
        if (position < 0 || position >= allDataCnt) {
            return;
        }
        if (position > 0) {
            position -= 1;
        }
        currentPosition = position;
        int currentShowCnt = MAX_SHOW_NUM;
        if (allDataCnt - position < MAX_SHOW_NUM) {
            currentShowCnt = allDataCnt - position;
        }
        if (currentDatas == null) {
            currentDatas = new ArrayList<ArrayList<String>>();
        } else {
            currentDatas.clear();
        }
        for (int i = 0; i < currentShowCnt; i++) {
            currentDatas.add(allDatas.get(position + i));
        }
        Log.d(TAG, "fillWindowout end position=" + (position + currentShowCnt - 1));
        super.fillWindow(position, window);
    }

//onMove方法是cursor构造时会执行的

    /**
     * 获取当前行对象，为一个oneLineDatastring[]
     */
    @Override
    public boolean onMove(int oldPosition, int newPosition) {
        if (newPosition < 0 || newPosition >= getCount()) {
            oneLineData = null;
            return false;
        }
        int index = newPosition - currentPosition;
        if (index < 0 || index >= currentDatas.size()) {
            return false;
        }
        oneLineData = currentDatas.get(index);
        return super.onMove(oldPosition, newPosition);
    }


// 取值方式：

// 获取int数据方法为例子，我们可以根据自己取值类型实现其get方法.

    @Override
    public int getInt(int column) {
        Object value = getString(column);
        try {
            return value != null ? ((Number) value).intValue() : null;
        } catch (ClassCastException e) {
            if (value instanceof CharSequence) {
                try {
                    return Integer.valueOf(value.toString());
                } catch (NumberFormatException e2) {
                    Log.e(TAG, "Cannotparse int value for " + value + "at key " + column);
                    return 0;
                }
            } else {
                Log.e(TAG, "Cannotcast value for " + column + "to a int: " + value, e);
                return 0;
            }
        }
    }

    @Override
    public long getLong(int column) {
        return Long.parseLong(list.get(column));
    }

    @Override
    public float getFloat(int column) {
        return 0;
    }

    @Override
    public double getDouble(int column) {
        return 0;
    }

    @Override
    public boolean isNull(int column) {
        return false;
    }


    /**
     * 获取游标行数
     */
    @Override
    public int getCount() {
        return allDataCnt;
    }


    @Override
    public String getString(int column) {
        return list.get(column);
    }

    @Override
    public short getShort(int column) {
        return 0;
    }


}
