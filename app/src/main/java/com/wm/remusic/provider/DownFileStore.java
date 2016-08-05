package com.wm.remusic.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wm.remusic.downmusic.DownloadDBEntity;

import java.util.ArrayList;

/**
 * Created by wm on 2016/4/12.
 */
public class DownFileStore {

    private static DownFileStore sInstance = null;

    private MusicDB mMusicDatabase = null;

    public DownFileStore(final Context context) {
        mMusicDatabase = MusicDB.getInstance(context);
    }

    public static final synchronized DownFileStore getInstance(final Context context) {
        if (sInstance == null) {
            sInstance = new DownFileStore(context.getApplicationContext());
        }

        return sInstance;
    }

    public void onCreate(final SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DownFileStoreColumns.NAME + " ("
                + DownFileStoreColumns.ID + " STRING NOT NULL PRIMARY KEY," + DownFileStoreColumns.TOOL_SIZE + " INT NOT NULL,"
                + DownFileStoreColumns.FILE_LENGTH + " INT NOT NULL, " + DownFileStoreColumns.URL + " STRING NOT NULL,"
                + DownFileStoreColumns.DIR + " STRING NOT NULL," + DownFileStoreColumns.FILE_NAME + " STRING NOT NULL,"
                + DownFileStoreColumns.DOWNSTATUS + " INT NOT NULL);");
    }

    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DownFileStoreColumns.NAME);
        onCreate(db);
    }

    public synchronized void insert(DownloadDBEntity entity) {

        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(7);
            values.put(DownFileStoreColumns.ID, entity.getDownloadId());
            values.put(DownFileStoreColumns.TOOL_SIZE, entity.getTotalSize());
            values.put(DownFileStoreColumns.FILE_LENGTH, entity.getCompletedSize());
            values.put(DownFileStoreColumns.URL, entity.getUrl());
            values.put(DownFileStoreColumns.DIR, entity.getSaveDirPath());
            values.put(DownFileStoreColumns.FILE_NAME, entity.getFileName());
            values.put(DownFileStoreColumns.DOWNSTATUS, entity.getDownloadStatus());
            database.replace(DownFileStoreColumns.NAME, null, values);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public synchronized void update(DownloadDBEntity entity) {

        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues(6);
            values.put(DownFileStoreColumns.TOOL_SIZE, entity.getTotalSize());
            values.put(DownFileStoreColumns.FILE_LENGTH, entity.getCompletedSize());
            values.put(DownFileStoreColumns.URL, entity.getUrl());
            values.put(DownFileStoreColumns.DIR, entity.getSaveDirPath());
            values.put(DownFileStoreColumns.FILE_NAME, entity.getFileName());
            values.put(DownFileStoreColumns.DOWNSTATUS, entity.getDownloadStatus());
            database.update(DownFileStoreColumns.NAME, values, DownFileStoreColumns.ID + " = ?",
                    new String[]{entity.getDownloadId()});
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void deleteTask(String Id) {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(DownFileStoreColumns.NAME, DownFileStoreColumns.ID + " = ?", new String[]
                {String.valueOf(Id)});
    }

    public synchronized void deleteAll() {
        final SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(DownFileStoreColumns.NAME, null, null);
    }

    public synchronized DownloadDBEntity getDownLoadedList(String Id) {
        Cursor cursor = null;
        DownloadDBEntity entity = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(DownFileStoreColumns.NAME, null,
                    DownFileStoreColumns.ID + " = ?", new String[]{String.valueOf(Id)}, null, null, null);
            if (cursor == null) {
                return null;
            }

            if (cursor != null && cursor.moveToFirst()) {

                do {
                    entity = new DownloadDBEntity(cursor.getString(0), cursor.getLong(1), cursor.getLong(2),
                            cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getInt(6));
                } while (cursor.moveToNext());
                return entity;
            } else return null;

        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }


    public synchronized ArrayList<DownloadDBEntity> getDownLoadedListAll() {
        ArrayList<DownloadDBEntity> results = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = mMusicDatabase.getReadableDatabase().query(DownFileStoreColumns.NAME, null,
                    null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                results.ensureCapacity(cursor.getCount());

                do {

                    results.add(new DownloadDBEntity(cursor.getString(0), cursor.getLong(1), cursor.getLong(2),
                            cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getInt(6)));
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


    public interface DownFileStoreColumns {
        /* Table name */
        String NAME = "downfile_info";

        /* Album IDs column */
        String ID = "id";

        /* Time played column */
        String TOOL_SIZE = "totalsize";

        String FILE_LENGTH = "complete_length";

        String URL = "url";

        String DIR = "dir";
        String FILE_NAME = "file_name";
        String DOWNSTATUS = "notification_type";
    }

}
