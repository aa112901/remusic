package com.wm.remusic.lrc;

import android.content.Context;

import java.util.List;


public interface ILrcView {
    /**
     * 初始化画笔，颜色，字体大小等设置
     */
    void init(Context context);

    /***
     * 设置数据源
     *
     * @param lrcRows
     */
    void setLrcRows(List<LrcRow> lrcRows);

    /**
     * 指定时间
     *
     * @param progress          时间进度
     * @param fromSeekBarByUser 是否由用户触摸Seekbar触发
     */
    void seekTo(int progress, boolean fromSeekBar, boolean fromSeekBarByUser);

    /***
     * 设置歌词文字的缩放比例
     *
     * @param scalingFactor
     */
    void setLrcScalingFactor(float scalingFactor);

    /**
     * 重置
     */
    void reset();
}
