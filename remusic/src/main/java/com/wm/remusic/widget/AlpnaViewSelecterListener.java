package com.wm.remusic.widget;

/**
 * 准备滑动选择接口
 */
public interface AlpnaViewSelecterListener {
    public void down(String key);

    public void up();

    public void move(String key);
}
