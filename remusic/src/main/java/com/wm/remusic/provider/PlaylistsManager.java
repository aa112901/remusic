package com.wm.remusic.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wm.remusic.service.MusicTrack;
import com.wm.remusic.uitl.IConstants;

import java.util.ArrayList;

/**
 * Created by wm on 2016/3/3.
 */
public class PlaylistsManager {


    private static PlaylistsManager sInstance = null;

    private MusicDB mMusicDatabase = null;
    private long favPlaylistId = IConstants.FAV_PLAYLIST;

    public PlaylistsManager(final Context context) {
        mMusicDatabase = MusicDB.getInstance(context);
    }

    public static final synchronized PlaylistsManager getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new PlaylistsManager(context.getApplicationContext());
        }
        return sInstance;
    }

    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + PlaylistsColumns.NAME + " ("
                + PlaylistsColumns.PLAYLIST_ID + " LONG NOT NULL," + PlaylistsColumns.TRACK_ID + " LONG NOT NULL,"
                + PlaylistsColumns.TRACK_ORDER + " LONG NOT NULL);");
    }

    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PlaylistsColumns.NAME);
        onCreate(db);
    }

    public synchronized void Insert(Context context, long playlistid, long id, int order) {
        ArrayList<MusicTrack> m = getPlaylist(playlistid);
        for (int i = 0; i < m.size(); i++) {
            if (m.get(i).mId == id)
                return;
        }

        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(3);
            values.put(PlaylistsColumns.PLAYLIST_ID, playlistid);
            values.put(PlaylistsColumns.TRACK_ID, id);
            values.put(PlaylistsColumns.TRACK_ORDER, getPlaylist(playlistid).size());
            database.insert(PlaylistsColumns.NAME, null, values);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        PlaylistInfo playlistInfo = PlaylistInfo.getInstance(context);
        playlistInfo.update(playlistid, getPlaylist(playlistid).size());

    }

    public synchronized void update(long playlistid, long id, int order) {

        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(1);
            values.put(PlaylistsColumns.TRACK_ORDER, order);
            database.update(PlaylistsColumns.NAME, values, PlaylistsColumns.PLAYLIST_ID + " = ?" + " AND " +
                    PlaylistsColumns.TRACK_ID + " = ?", new String[]{playlistid + "", id + ""});
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

    }

    public synchronized boolean getFav(long id) {

        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaylistsColumns.NAME, null,
                    PlaylistsColumns.PLAYLIST_ID + " = ?" + " AND " +
                            PlaylistsColumns.TRACK_ID + " = ?", new String[]{favPlaylistId + "", id + ""}, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return true;
            }
            return false;

        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

    }


    public synchronized void update(long playlistid, long[] ids, int[] order) {

        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            for (int i = 0; i < order.length; i++) {
                ContentValues values = new ContentValues(1);
                values.put(PlaylistsColumns.TRACK_ORDER, order[i]);
                database.update(PlaylistsColumns.NAME, values, PlaylistsColumns.PLAYLIST_ID + " = ?" + " AND " +
                        PlaylistsColumns.TRACK_ID + " = ?", new String[]{playlistid + "", ids[i] + ""});
            }

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

    }


    public void removeItem(Context context, final long playlistId, long songId) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(PlaylistsColumns.NAME, PlaylistsColumns.PLAYLIST_ID + " = ?" + " AND " + PlaylistsColumns.TRACK_ID + " = ?", new String[]{
                String.valueOf(playlistId), String.valueOf(songId)
        });

        PlaylistInfo playlistInfo = PlaylistInfo.getInstance(context);
        playlistInfo.update(playlistId, getPlaylist(playlistId).size());

    }

    public void delete(final long PlaylistId) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(PlaylistsColumns.NAME, PlaylistsColumns.PLAYLIST_ID + " = ?", new String[]
                {String.valueOf(PlaylistId)});
    }


    //删除播放列表中的记录的音乐 ，删除本地文件时调用
    public synchronized void deleteMusic(Context context,final long musicId) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();

        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaylistsColumns.NAME, null,
                    PlaylistsColumns.TRACK_ID + " = " + String.valueOf(musicId), null, null, null, null, null);
            if(cursor != null && cursor.moveToFirst()){
                long[] deletedPlaylistIds = new long[cursor.getCount()];
                int i = 0;

               do{
                    deletedPlaylistIds[i] = cursor.getLong(0);
                    i++;
                }while (cursor.moveToNext());

                PlaylistInfo.getInstance(context).updatePlaylistMusicCount(deletedPlaylistIds);
            }


        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        database.delete(PlaylistsColumns.NAME, PlaylistsColumns.TRACK_ID + " = ?", new String[]
                {String.valueOf(musicId)});
    }



    public void deleteAll() {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(PlaylistsColumns.NAME, null, null);
    }


    public ArrayList<MusicTrack> getPlaylist(final long playlistid) {
        ArrayList<MusicTrack> results = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(PlaylistsColumns.NAME, null,
                    PlaylistsColumns.PLAYLIST_ID + " = " + String.valueOf(playlistid), null, null, null, PlaylistsColumns.TRACK_ORDER + " ASC ", null);
            if (cursor != null && cursor.moveToFirst()) {
                results.ensureCapacity(cursor.getCount());

                do {
                    results.add(new MusicTrack(cursor.getLong(1), cursor.getInt(0)));
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

    public interface PlaylistsColumns {
        /* Table name */
        String NAME = "playlists";

        /* Album IDs column */
        String PLAYLIST_ID = "playlist_id";

        /* Time played column */
        String TRACK_ID = "track_id";

        String TRACK_ORDER = "track_order";

        String IS_FAV = "is_fav";
    }
}
