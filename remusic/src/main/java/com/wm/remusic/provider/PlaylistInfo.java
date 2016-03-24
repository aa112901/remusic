package com.wm.remusic.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wm.remusic.info.Playlist;

import java.util.ArrayList;

/**
 * Created by wm on 2016/3/3.
 */
public class PlaylistInfo {

    private static PlaylistInfo sInstance = null;

    private MusicDB mMusicDatabase = null;

    public PlaylistInfo(final Context context) {
        mMusicDatabase = MusicDB.getInstance(context);
    }

    public static final synchronized PlaylistInfo getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new PlaylistInfo(context.getApplicationContext());
        }

        return sInstance;
    }

    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + PlaylistInfoColumns.NAME + " ("
                + PlaylistInfoColumns.PLAYLIST_ID + " LONG NOT NULL," + PlaylistInfoColumns.PLAYLIST_NAME + " STRING NOT NULL,"
                + PlaylistInfoColumns.SONG_COUNT + " INT NOT NULL, " + PlaylistInfoColumns.ALBUM_ART + " STRING NOT NULL);");
    }

    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PlaylistInfoColumns.NAME);
        onCreate(db);
    }


    public synchronized void addPlaylist(long playlistid, String name, int count, String albumart) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();

        try {
            ContentValues values = new ContentValues(4);
            values.put(PlaylistInfoColumns.PLAYLIST_ID, playlistid);
            values.put(PlaylistInfoColumns.PLAYLIST_NAME, name);
            values.put(PlaylistInfoColumns.SONG_COUNT, count);
            values.put(PlaylistInfoColumns.ALBUM_ART, albumart);

            database.insert(PlaylistInfoColumns.NAME, null, values);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }


    public synchronized void updatePlaylist(long playlistid, int oldcount) {
        ArrayList<Playlist> results = getPlaylist();
        int countt = 0;
        for (int i = 0; i < results.size(); i++) {
            if (results.get(i).id == playlistid) {
                countt = results.get(i).songCount;
            }
        }
        countt = countt + oldcount;
        update(playlistid, countt);

    }

    public synchronized void update(long playlistid, int count) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(2);
            values.put(PlaylistInfoColumns.PLAYLIST_ID, playlistid);
            //values.put(PlaylistInfoColumns.PLAYLIST_NAME, name);
            values.put(PlaylistInfoColumns.SONG_COUNT, count);
            database.update(PlaylistInfoColumns.NAME, values, PlaylistInfoColumns.PLAYLIST_ID + " = " + playlistid, null);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void deletePlaylist(final long PlaylistId) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(PlaylistInfoColumns.NAME, PlaylistInfoColumns.PLAYLIST_ID + " = ?", new String[]
                {String.valueOf(PlaylistId)});
    }

    public void deleteAll() {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(PlaylistInfoColumns.NAME, null, null);
    }


    public ArrayList<Playlist> getPlaylist() {
        ArrayList<Playlist> results = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaylistInfoColumns.NAME, null,
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                results.ensureCapacity(cursor.getCount());

                do {
                    ;
                    results.add(new Playlist(cursor.getLong(0), cursor.getString(1), cursor.getInt(2), cursor.getString(3)));
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

    public interface PlaylistInfoColumns {
        /* Table name */
        String NAME = "playlist_info";

        /* Album IDs column */
        String PLAYLIST_ID = "playlist_id";

        /* Time played column */
        String PLAYLIST_NAME = "playlist_name";

        String SONG_COUNT = "count";

        String ALBUM_ART = "album_art";
    }

}
