/**
 * Copyright (lrc_arrow) www.longdw.com
 */
package com.wm.remusic.info;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class ArtistInfo implements Parcelable {

    public static final String KEY_ARTIST_NAME = "artist_name";
    public static final String KEY_NUMBER_OF_TRACKS = "number_of_tracks";
    public static final String KEY_ARTIST_ID = "artist_id";
    public static final String KEY_ARTIST_SORT = "artist_sort";


    public String artist_name;
    public int number_of_tracks;
    public long artist_id;
    public String artist_sort;
    public static final Creator<ArtistInfo> CREATOR = new Creator<ArtistInfo>() {

        @Override
        public ArtistInfo createFromParcel(Parcel source) {
            Bundle bundle = source.readBundle();
            ArtistInfo info = new ArtistInfo();
            info.artist_name = bundle.getString(KEY_ARTIST_NAME);
            info.number_of_tracks = bundle.getInt(KEY_NUMBER_OF_TRACKS);
            info.artist_id = bundle.getLong(KEY_ARTIST_ID);
            info.artist_sort = bundle.getString(KEY_ARTIST_SORT);
            return info;
        }

        @Override
        public ArtistInfo[] newArray(int size) {
            return new ArtistInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_ARTIST_NAME, artist_name);
        bundle.putInt(KEY_NUMBER_OF_TRACKS, number_of_tracks);
        bundle.putLong(KEY_ARTIST_ID, artist_id);
        bundle.putString(KEY_ARTIST_SORT, artist_sort);
        dest.writeBundle(bundle);
    }

}
