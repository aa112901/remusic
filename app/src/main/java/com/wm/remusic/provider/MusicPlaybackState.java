/*
* Copyright (C) 2014 The CyanogenMod Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.wm.remusic.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wm.remusic.service.MusicTrack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * This keeps track of the music playback and history state of the playback service
 */
public class MusicPlaybackState {
    private static MusicPlaybackState sInstance = null;

    private MusicDB mMusicDatabase = null;

    public MusicPlaybackState(final Context context) {
        mMusicDatabase = MusicDB.getInstance(context);
    }

    public static final synchronized MusicPlaybackState getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new MusicPlaybackState(context.getApplicationContext());
        }
        return sInstance;
    }

    public void onCreate(final SQLiteDatabase db) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ");
        builder.append(PlaybackQueueColumns.NAME);
        builder.append("(");


        builder.append(PlaybackQueueColumns.TRACK_ID);
        builder.append(" LONG NOT NULL,");

        builder.append(PlaybackQueueColumns.SOURCE_POSITION);
        builder.append(" INT NOT NULL);");

        db.execSQL(builder.toString());

        builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ");
        builder.append(PlaybackHistoryColumns.NAME);
        builder.append("(");

        builder.append(PlaybackHistoryColumns.POSITION);
        builder.append(" INT NOT NULL);");

        db.execSQL(builder.toString());

        builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ");
        builder.append(PlaybackHistoryColumns.NAME);
        builder.append("(");

        builder.append(PlaybackHistoryColumns.POSITION);
        builder.append(" INT NOT NULL);");

        db.execSQL(builder.toString());
    }

    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        // this table was created in version 2 so call the onCreate method if we hit that scenario
        if (oldVersion < 2 && newVersion >= 2) {
            onCreate(db);
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + PlaybackQueueColumns.NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PlaybackHistoryColumns.NAME);
        onCreate(db);
    }

    public synchronized void Insert(MusicTrack track) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(2);
            values.put(PlaybackQueueColumns.TRACK_ID, track.mId);
            values.put(PlaybackQueueColumns.SOURCE_POSITION, track.mSourcePosition);
            database.insert(PlaybackQueueColumns.NAME, null, values);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

    }

    public synchronized void Delete(long id) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            database.delete(PlaybackQueueColumns.NAME, PlaybackQueueColumns.TRACK_ID + " = " + id, null);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

    }

    public synchronized void clearQueue() {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        try {
            database.delete(PlaybackQueueColumns.NAME, null, null);
            database.setTransactionSuccessful();
        } catch (Exception e) {

        }
    }

    public synchronized void saveState(final ArrayList<MusicTrack> queue,
                                       LinkedList<Integer> history) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();

        try {
            database.delete(PlaybackQueueColumns.NAME, null, null);
            database.delete(PlaybackHistoryColumns.NAME, null, null);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        final int NUM_PROCESS = 20;
        int position = 0;
        while (position < queue.size()) {
            database.beginTransaction();
            try {
                for (int i = position; i < queue.size() && i < position + NUM_PROCESS; i++) {
                    MusicTrack track = queue.get(i);
                    ContentValues values = new ContentValues(2);
                    values.put(PlaybackQueueColumns.TRACK_ID, track.mId);
                    values.put(PlaybackQueueColumns.SOURCE_POSITION, track.mSourcePosition);

                    database.insert(PlaybackQueueColumns.NAME, null, values);
                }
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
                position += NUM_PROCESS;
            }
        }

        if (history != null) {
            Iterator<Integer> iter = history.iterator();
            while (iter.hasNext()) {
                database.beginTransaction();
                try {
                    for (int i = 0; iter.hasNext() && i < NUM_PROCESS; i++) {
                        ContentValues values = new ContentValues(1);
                        values.put(PlaybackHistoryColumns.POSITION, iter.next());

                        database.insert(PlaybackHistoryColumns.NAME, null, values);
                    }

                    database.setTransactionSuccessful();
                } finally {
                    database.endTransaction();
                }
            }
        }
    }

    public ArrayList<MusicTrack> getQueue() {
        ArrayList<MusicTrack> results = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaybackQueueColumns.NAME, null,
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                results.ensureCapacity(cursor.getCount());

                do {
                    results.add(new MusicTrack(cursor.getLong(0), cursor.getInt(1)));
                } while (cursor.moveToNext());
            }

            return results;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public LinkedList<Integer> getHistory(final int playlistSize) {
        LinkedList<Integer> results = new LinkedList<>();

        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaybackHistoryColumns.NAME, null,
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int pos = cursor.getInt(0);
                    if (pos >= 0 && pos < playlistSize) {
                        results.add(pos);
                    }
                } while (cursor.moveToNext());
            }

            return results;
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    public class PlaybackQueueColumns {

        public static final String NAME = "playbacklist";
        public static final String TRACK_ID = "trackid";
        public static final String SOURCE_POSITION = "sourceposition";
    }

    public class PlaybackHistoryColumns {

        public static final String NAME = "playbackhistory";

        public static final String POSITION = "position";
    }
}
