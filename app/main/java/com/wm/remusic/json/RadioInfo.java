package com.wm.remusic.json;

/**
 * Created by wm on 2016/8/3.
 */
public class RadioInfo {


    /**
     * song_id : 13015233
     * song_name : 我很好（刘若英）
     * song_duration : 273
     * songpic : {}
     */

    private String song_id;
    private String song_name;
    private String song_duration;
    private SongpicBean songpic;

    public String getSong_id() {
        return song_id;
    }

    public void setSong_id(String song_id) {
        this.song_id = song_id;
    }

    public String getSong_name() {
        return song_name;
    }

    public void setSong_name(String song_name) {
        this.song_name = song_name;
    }

    public String getSong_duration() {
        return song_duration;
    }

    public void setSong_duration(String song_duration) {
        this.song_duration = song_duration;
    }

    public SongpicBean getSongpic() {
        return songpic;
    }

    public void setSongpic(SongpicBean songpic) {
        this.songpic = songpic;
    }

    public static class SongpicBean {
    }
}
