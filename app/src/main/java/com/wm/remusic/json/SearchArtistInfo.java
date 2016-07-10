package com.wm.remusic.json;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wm on 2016/5/18.
 */
public class SearchArtistInfo implements Parcelable {


    /**
     * artist_id : 2027570
     * author : Maroon
     * ting_uid : 87966713
     * avatar_middle : http://c.hiphotos.baidu.com/ting/pic/item/80cb39dbb6fd526674d53b05ad18972bd4073620.jpg
     * album_num : 0
     * song_num : 3
     * country :
     * artist_desc :
     * artist_source : web
     */

    private String artist_id;
    private String author;
    private String ting_uid;
    private String avatar_middle;
    private int album_num;
    private int song_num;
    private String country;
    private String artist_desc;
    private String artist_source;

    public String getArtist_id() {
        return artist_id;
    }

    public void setArtist_id(String artist_id) {
        this.artist_id = artist_id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTing_uid() {
        return ting_uid;
    }

    public void setTing_uid(String ting_uid) {
        this.ting_uid = ting_uid;
    }

    public String getAvatar_middle() {
        return avatar_middle;
    }

    public void setAvatar_middle(String avatar_middle) {
        this.avatar_middle = avatar_middle;
    }

    public int getAlbum_num() {
        return album_num;
    }

    public void setAlbum_num(int album_num) {
        this.album_num = album_num;
    }

    public int getSong_num() {
        return song_num;
    }

    public void setSong_num(int song_num) {
        this.song_num = song_num;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getArtist_desc() {
        return artist_desc;
    }

    public void setArtist_desc(String artist_desc) {
        this.artist_desc = artist_desc;
    }

    public String getArtist_source() {
        return artist_source;
    }

    public void setArtist_source(String artist_source) {
        this.artist_source = artist_source;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.artist_id);
        dest.writeString(this.author);
        dest.writeString(this.ting_uid);
        dest.writeString(this.avatar_middle);
        dest.writeInt(this.album_num);
        dest.writeInt(this.song_num);
        dest.writeString(this.country);
        dest.writeString(this.artist_desc);
        dest.writeString(this.artist_source);
    }

    public SearchArtistInfo() {
    }

    protected SearchArtistInfo(Parcel in) {
        this.artist_id = in.readString();
        this.author = in.readString();
        this.ting_uid = in.readString();
        this.avatar_middle = in.readString();
        this.album_num = in.readInt();
        this.song_num = in.readInt();
        this.country = in.readString();
        this.artist_desc = in.readString();
        this.artist_source = in.readString();
    }

    public static final Parcelable.Creator<SearchArtistInfo> CREATOR = new Parcelable.Creator<SearchArtistInfo>() {
        @Override
        public SearchArtistInfo createFromParcel(Parcel source) {
            return new SearchArtistInfo(source);
        }

        @Override
        public SearchArtistInfo[] newArray(int size) {
            return new SearchArtistInfo[size];
        }
    };
}
