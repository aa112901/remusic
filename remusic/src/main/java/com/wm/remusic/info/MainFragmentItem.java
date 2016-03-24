package com.wm.remusic.info;

/**
 * Created by wm on 2016/3/10.
 */
public class MainFragmentItem {
    private String title;   //信息标题
    private int count;
    private int avatar; //图片ID

    //标题
    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    //图片
    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }

    public int getAvatar() {
        return avatar;
    }
}
