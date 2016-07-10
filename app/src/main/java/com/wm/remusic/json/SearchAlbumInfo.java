package com.wm.remusic.json;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wm on 2016/5/18.
 */
public class SearchAlbumInfo implements Parcelable {

    /**
     * album_id : 14636595
     * author : Whistler
     * hot : 32
     * title : whistler
     * artist_id : 2859671
     * all_artist_id : 2859671
     * company : Om Music
     * publishtime : 1999-01-01
     * album_desc : by  Stephen CramerThe harmonica and carefree mood of "If I Give You a Smile" kicks off the self-titl...
     * pic_small : http://qukufile2.qianqian.com/data2/pic/39766008/39766008.jpg
     */

    private String album_id;
    private String author;
    private int hot;
    private String title;
    private String artist_id;
    private String all_artist_id;
    private String company;
    private String publishtime;
    private String album_desc;
    private String pic_small;

    public String getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(String album_id) {
        this.album_id = album_id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getHot() {
        return hot;
    }

    public void setHot(int hot) {
        this.hot = hot;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist_id() {
        return artist_id;
    }

    public void setArtist_id(String artist_id) {
        this.artist_id = artist_id;
    }

    public String getAll_artist_id() {
        return all_artist_id;
    }

    public void setAll_artist_id(String all_artist_id) {
        this.all_artist_id = all_artist_id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPublishtime() {
        return publishtime;
    }

    public void setPublishtime(String publishtime) {
        this.publishtime = publishtime;
    }

    public String getAlbum_desc() {
        return album_desc;
    }

    public void setAlbum_desc(String album_desc) {
        this.album_desc = album_desc;
    }

    public String getPic_small() {
        return pic_small;
    }

    public void setPic_small(String pic_small) {
        this.pic_small = pic_small;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.album_id);
        dest.writeString(this.author);
        dest.writeInt(this.hot);
        dest.writeString(this.title);
        dest.writeString(this.artist_id);
        dest.writeString(this.all_artist_id);
        dest.writeString(this.company);
        dest.writeString(this.publishtime);
        dest.writeString(this.album_desc);
        dest.writeString(this.pic_small);
    }

    public SearchAlbumInfo() {
    }

    protected SearchAlbumInfo(Parcel in) {
        this.album_id = in.readString();
        this.author = in.readString();
        this.hot = in.readInt();
        this.title = in.readString();
        this.artist_id = in.readString();
        this.all_artist_id = in.readString();
        this.company = in.readString();
        this.publishtime = in.readString();
        this.album_desc = in.readString();
        this.pic_small = in.readString();
    }

    public static final Parcelable.Creator<SearchAlbumInfo> CREATOR = new Parcelable.Creator<SearchAlbumInfo>() {
        @Override
        public SearchAlbumInfo createFromParcel(Parcel source) {
            return new SearchAlbumInfo(source);
        }

        @Override
        public SearchAlbumInfo[] newArray(int size) {
            return new SearchAlbumInfo[size];
        }
    };
}
