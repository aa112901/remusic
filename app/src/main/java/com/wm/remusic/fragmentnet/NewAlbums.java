package com.wm.remusic.fragmentnet;

/**
 * Created by wm on 2016/4/10.
 */
public class NewAlbums {
    public String coverImgUrl;
    public final long id;
    public final String albumName;
    public final String artistName;
    public int publishTime;

    public NewAlbums() {
        this.coverImgUrl = "";
        this.id = -1;
        this.albumName = "";
        this.artistName = "";
        this.publishTime = -1;

    }

    public NewAlbums(String _coverImg, long _id, String _album_Name, String _artistName,
                     int _publishTime) {
        this.coverImgUrl = _coverImg;
        this.id = _id;
        this.albumName = _album_Name;
        this.artistName = _artistName;
        this.publishTime = _publishTime;
    }

}
