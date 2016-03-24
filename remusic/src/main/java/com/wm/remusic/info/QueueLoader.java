/*
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.wm.remusic.info;

import android.content.Context;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;


public class QueueLoader {

    private static NowPlayingCursor mCursor;

    public static List<MusicInfo> getQueueSongs(Context context) {

        final ArrayList<MusicInfo> mSongList = new ArrayList<>();
        mCursor = new NowPlayingCursor(context);

        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                MusicInfo music = new MusicInfo();
                music.songId = mCursor.getInt(mCursor
                        .getColumnIndex(MediaStore.Audio.Media._ID));
                music.albumName = mCursor.getString(mCursor
                        .getColumnIndex(MediaStore.Audio.Albums.ALBUM));
                music.musicName = mCursor.getString(mCursor
                        .getColumnIndex(MediaStore.Audio.Media.TITLE));
                music.artist = mCursor.getString(mCursor
                        .getColumnIndex(MediaStore.Audio.Media.ARTIST));
                music.data = mCursor.getString(mCursor
                        .getColumnIndex(MediaStore.Audio.Media.DATA));

                mSongList.add(music);
            } while (mCursor.moveToNext());
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return mSongList;
    }

}
