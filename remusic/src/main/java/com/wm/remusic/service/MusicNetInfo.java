/**
 * Copyright (c) www.longdw.com
 */
package com.wm.remusic.service;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class MusicNetInfo implements Parcelable {


    public static final String KEY_SONG_ID = "songid";
    public static final String KEY_MUSIC_NAME = "musicname";
    public static final String KEY_ARTIST = "artist";
    public static final String KEY_ALBUM_NAME = "albumname";
    public static final String KEY_ISLOCAL = "islocal";
    public static final String KEY_URL = "url";

    /**
     * 数据库中的_id
     */
    public String musicName;
    public String artist;
    public String albumName;
    public boolean isLocal;
    public String url;

    /**
     * 0表示没有收藏 1表示收藏
     */
    public int favorite = 0;
    public static final Creator<MusicNetInfo> CREATOR = new Creator<MusicNetInfo>() {

        @Override
        public MusicNetInfo createFromParcel(Parcel source) {
            MusicNetInfo music = new MusicNetInfo();
            Bundle bundle = new Bundle();
            bundle = source.readBundle();
            music.musicName = bundle.getString(KEY_MUSIC_NAME);
            music.artist = bundle.getString(KEY_ARTIST);
            music.albumName = bundle.getString(KEY_ALBUM_NAME);
            music.isLocal = bundle.getBoolean(KEY_ISLOCAL);
            music.url = bundle.getString(KEY_URL);
            return music;
        }

        @Override
        public MusicNetInfo[] newArray(int size) {
            return new MusicNetInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();

        bundle.putString(KEY_MUSIC_NAME, musicName);
        bundle.putString(KEY_ARTIST, artist);
        bundle.putString(KEY_ALBUM_NAME, albumName);
        bundle.putBoolean(KEY_ISLOCAL, isLocal);
        bundle.putString(KEY_URL,url);
        dest.writeBundle(bundle);
    }


}