package com.wm.remusic.json;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wm on 2016/5/18.
 */
public class SearchSongInfo implements Parcelable {

    /**
     * content :
     * copy_type : 1
     * toneid : 0
     * info :
     * all_rate : 24,64,128
     * resource_type : 2
     * relate_status : 1
     * has_mv_mobile : 0
     * versions :
     * song_id : 85060522
     * title : Whistle
     * ting_uid : 208132
     * author : Glee Cast
     * album_id : 0
     * album_title :
     * is_first_publish : 0
     * havehigh : 0
     * charge : 0
     * has_mv : 0
     * learn : 0
     * song_source : web
     * piao_id : 0
     * korean_bb_song : 0
     * resource_type_ext : 0
     * mv_provider : 0000000000
     * artist_id : 374550
     * all_artist_id : 374550
     * lrclink : http://musicdata.baidu.com/data2/lrc/239939129/239939129.lrc
     * data_source : 0
     * cluster_id : 0
     * bitrate_fee : {"0":"0|0","1":"0|0"}
     */

    private String content;
    private String copy_type;
    private String toneid;
    private String info;
    private String all_rate;
    private int resource_type;
    private int relate_status;
    private int has_mv_mobile;
    private String versions;
    private String song_id;
    private String title;
    private String ting_uid;
    private String author;
    private String album_id;
    private String album_title;
    private int is_first_publish;
    private int havehigh;
    private int charge;
    private int has_mv;
    private int learn;
    private String song_source;
    private String piao_id;
    private String korean_bb_song;
    private String resource_type_ext;
    private String mv_provider;
    private String artist_id;
    private String all_artist_id;
    private String lrclink;
    private int data_source;
    private int cluster_id;
    private String bitrate_fee;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCopy_type() {
        return copy_type;
    }

    public void setCopy_type(String copy_type) {
        this.copy_type = copy_type;
    }

    public String getToneid() {
        return toneid;
    }

    public void setToneid(String toneid) {
        this.toneid = toneid;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getAll_rate() {
        return all_rate;
    }

    public void setAll_rate(String all_rate) {
        this.all_rate = all_rate;
    }

    public int getResource_type() {
        return resource_type;
    }

    public void setResource_type(int resource_type) {
        this.resource_type = resource_type;
    }

    public int getRelate_status() {
        return relate_status;
    }

    public void setRelate_status(int relate_status) {
        this.relate_status = relate_status;
    }

    public int getHas_mv_mobile() {
        return has_mv_mobile;
    }

    public void setHas_mv_mobile(int has_mv_mobile) {
        this.has_mv_mobile = has_mv_mobile;
    }

    public String getVersions() {
        return versions;
    }

    public void setVersions(String versions) {
        this.versions = versions;
    }

    public String getSong_id() {
        return song_id;
    }

    public void setSong_id(String song_id) {
        this.song_id = song_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTing_uid() {
        return ting_uid;
    }

    public void setTing_uid(String ting_uid) {
        this.ting_uid = ting_uid;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(String album_id) {
        this.album_id = album_id;
    }

    public String getAlbum_title() {
        return album_title;
    }

    public void setAlbum_title(String album_title) {
        this.album_title = album_title;
    }

    public int getIs_first_publish() {
        return is_first_publish;
    }

    public void setIs_first_publish(int is_first_publish) {
        this.is_first_publish = is_first_publish;
    }

    public int getHavehigh() {
        return havehigh;
    }

    public void setHavehigh(int havehigh) {
        this.havehigh = havehigh;
    }

    public int getCharge() {
        return charge;
    }

    public void setCharge(int charge) {
        this.charge = charge;
    }

    public int getHas_mv() {
        return has_mv;
    }

    public void setHas_mv(int has_mv) {
        this.has_mv = has_mv;
    }

    public int getLearn() {
        return learn;
    }

    public void setLearn(int learn) {
        this.learn = learn;
    }

    public String getSong_source() {
        return song_source;
    }

    public void setSong_source(String song_source) {
        this.song_source = song_source;
    }

    public String getPiao_id() {
        return piao_id;
    }

    public void setPiao_id(String piao_id) {
        this.piao_id = piao_id;
    }

    public String getKorean_bb_song() {
        return korean_bb_song;
    }

    public void setKorean_bb_song(String korean_bb_song) {
        this.korean_bb_song = korean_bb_song;
    }

    public String getResource_type_ext() {
        return resource_type_ext;
    }

    public void setResource_type_ext(String resource_type_ext) {
        this.resource_type_ext = resource_type_ext;
    }

    public String getMv_provider() {
        return mv_provider;
    }

    public void setMv_provider(String mv_provider) {
        this.mv_provider = mv_provider;
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

    public String getLrclink() {
        return lrclink;
    }

    public void setLrclink(String lrclink) {
        this.lrclink = lrclink;
    }

    public int getData_source() {
        return data_source;
    }

    public void setData_source(int data_source) {
        this.data_source = data_source;
    }

    public int getCluster_id() {
        return cluster_id;
    }

    public void setCluster_id(int cluster_id) {
        this.cluster_id = cluster_id;
    }

    public String getBitrate_fee() {
        return bitrate_fee;
    }

    public void setBitrate_fee(String bitrate_fee) {
        this.bitrate_fee = bitrate_fee;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.content);
        dest.writeString(this.copy_type);
        dest.writeString(this.toneid);
        dest.writeString(this.info);
        dest.writeString(this.all_rate);
        dest.writeInt(this.resource_type);
        dest.writeInt(this.relate_status);
        dest.writeInt(this.has_mv_mobile);
        dest.writeString(this.versions);
        dest.writeString(this.song_id);
        dest.writeString(this.title);
        dest.writeString(this.ting_uid);
        dest.writeString(this.author);
        dest.writeString(this.album_id);
        dest.writeString(this.album_title);
        dest.writeInt(this.is_first_publish);
        dest.writeInt(this.havehigh);
        dest.writeInt(this.charge);
        dest.writeInt(this.has_mv);
        dest.writeInt(this.learn);
        dest.writeString(this.song_source);
        dest.writeString(this.piao_id);
        dest.writeString(this.korean_bb_song);
        dest.writeString(this.resource_type_ext);
        dest.writeString(this.mv_provider);
        dest.writeString(this.artist_id);
        dest.writeString(this.all_artist_id);
        dest.writeString(this.lrclink);
        dest.writeInt(this.data_source);
        dest.writeInt(this.cluster_id);
        dest.writeString(this.bitrate_fee);
    }

    public SearchSongInfo() {
    }

    protected SearchSongInfo(Parcel in) {
        this.content = in.readString();
        this.copy_type = in.readString();
        this.toneid = in.readString();
        this.info = in.readString();
        this.all_rate = in.readString();
        this.resource_type = in.readInt();
        this.relate_status = in.readInt();
        this.has_mv_mobile = in.readInt();
        this.versions = in.readString();
        this.song_id = in.readString();
        this.title = in.readString();
        this.ting_uid = in.readString();
        this.author = in.readString();
        this.album_id = in.readString();
        this.album_title = in.readString();
        this.is_first_publish = in.readInt();
        this.havehigh = in.readInt();
        this.charge = in.readInt();
        this.has_mv = in.readInt();
        this.learn = in.readInt();
        this.song_source = in.readString();
        this.piao_id = in.readString();
        this.korean_bb_song = in.readString();
        this.resource_type_ext = in.readString();
        this.mv_provider = in.readString();
        this.artist_id = in.readString();
        this.all_artist_id = in.readString();
        this.lrclink = in.readString();
        this.data_source = in.readInt();
        this.cluster_id = in.readInt();
        this.bitrate_fee = in.readString();
    }

    public static final Parcelable.Creator<SearchSongInfo> CREATOR = new Parcelable.Creator<SearchSongInfo>() {
        @Override
        public SearchSongInfo createFromParcel(Parcel source) {
            return new SearchSongInfo(source);
        }

        @Override
        public SearchSongInfo[] newArray(int size) {
            return new SearchSongInfo[size];
        }
    };
}
