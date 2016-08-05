package com.wm.remusic.json;

/**
 * Created by wm on 2016/4/16.
 */
public class MusicFileDownInfo {

    /**
     * show_link : http://zhangmenshiting.baidu.com/data2/music/66764024/66764024.mp3?xcode=7c8f1aaf10a3704c350a5c038dd045b5
     * down_type : 0
     * original : 0
     * free : 1
     * replay_gain : 0.000000
     * song_file_id : 66764024
     * file_size : 1775862
     * file_extension : mp3
     * file_duration : 221
     * can_see : 1
     * can_load : true
     * preload : 40
     * file_bitrate : 64
     * file_link : http://yinyueshiting.baidu.com/data2/music/66764024/66764024.mp3?xcode=7c8f1aaf10a3704c350a5c038dd045b5
     * is_udition_url : 1
     * hash : d8bc896f901186562c200a9e18a96a5429c59a82
     */

    private String show_link;
    private int down_type;
    private int original;
    private int free;
    private String replay_gain;
    private int song_file_id;
    private int file_size;
    private String file_extension;
    private int file_duration;
    private int can_see;
    private boolean can_load;
    private int preload;
    private int file_bitrate;
    private String file_link;
    private int is_udition_url;
    private String hash;

    public String getShow_link() {
        return show_link;
    }

    public void setShow_link(String show_link) {
        this.show_link = show_link;
    }

    public int getDown_type() {
        return down_type;
    }

    public void setDown_type(int down_type) {
        this.down_type = down_type;
    }

    public int getOriginal() {
        return original;
    }

    public void setOriginal(int original) {
        this.original = original;
    }

    public int getFree() {
        return free;
    }

    public void setFree(int free) {
        this.free = free;
    }

    public String getReplay_gain() {
        return replay_gain;
    }

    public void setReplay_gain(String replay_gain) {
        this.replay_gain = replay_gain;
    }

    public int getSong_file_id() {
        return song_file_id;
    }

    public void setSong_file_id(int song_file_id) {
        this.song_file_id = song_file_id;
    }

    public int getFile_size() {
        return file_size;
    }

    public void setFile_size(int file_size) {
        this.file_size = file_size;
    }

    public String getFile_extension() {
        return file_extension;
    }

    public void setFile_extension(String file_extension) {
        this.file_extension = file_extension;
    }

    public int getFile_duration() {
        return file_duration;
    }

    public void setFile_duration(int file_duration) {
        this.file_duration = file_duration;
    }

    public int getCan_see() {
        return can_see;
    }

    public void setCan_see(int can_see) {
        this.can_see = can_see;
    }

    public boolean isCan_load() {
        return can_load;
    }

    public void setCan_load(boolean can_load) {
        this.can_load = can_load;
    }

    public int getPreload() {
        return preload;
    }

    public void setPreload(int preload) {
        this.preload = preload;
    }

    public int getFile_bitrate() {
        return file_bitrate;
    }

    public void setFile_bitrate(int file_bitrate) {
        this.file_bitrate = file_bitrate;
    }

    public String getFile_link() {
        return file_link;
    }

    public void setFile_link(String file_link) {
        this.file_link = file_link;
    }

    public int getIs_udition_url() {
        return is_udition_url;
    }

    public void setIs_udition_url(int is_udition_url) {
        this.is_udition_url = is_udition_url;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
