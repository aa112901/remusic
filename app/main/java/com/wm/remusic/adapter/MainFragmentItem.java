package com.wm.remusic.adapter;

/**
 * Created by wm on 2016/3/10.
 */
public class MainFragmentItem {
    public String title;   //信息标题
    public int count;
    public int avatar; //图片ID
    public boolean countChanged = true;

    public String getTitle() {
        return title;
    }

    //标题
    public void setTitle(String title) {
        this.title = title;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getAvatar() {
        return avatar;
    }

    //图片
    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }
}
