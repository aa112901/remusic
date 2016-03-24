package com.wm.remusic.adapter;

/**
 * Created by wm on 2016/2/21.
 */
public class OverFlowItem {

    private String title;   //信息标题
    private int avatar; //图片ID

    //标题
    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    //图片
    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }

    public int getAvatar() {
        return avatar;
    }

}
