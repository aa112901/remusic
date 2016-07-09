package com.wm.remusic.uitl;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.wm.remusic.info.MusicInfo;

import java.util.ArrayList;

/**
 * Created by wm on 2016/3/4.
 */
public class SearchUtils {
    public static ArrayList<MusicInfo> searchSongs(Context context, String searchString) {
        return getSongsForCursor(makeSongCursor(context, "title LIKE ?", new String[]{"%" + searchString + "%"}));
    }

    public static ArrayList<MusicInfo> getSongsForCursor(Cursor cursor) {
        ArrayList arrayList = new ArrayList();
        if ((cursor != null) && (cursor.moveToFirst()))
            do {
                MusicInfo musicInfo = new MusicInfo();
                musicInfo.songId = (int) cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                musicInfo.albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                musicInfo.musicName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                musicInfo.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                musicInfo.albumName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                arrayList.add(musicInfo);
            }
            while (cursor.moveToNext());
        if (cursor != null)
            cursor.close();
        return arrayList;
    }

    public static Cursor makeSongCursor(Context context, String selection, String[] paramArrayOfString) {
        String selectionStatement = "is_music=1 AND title != ''";
        // final String songSortOrder = PreferencesUtility.getInstance(context).getSongSortOrder();

        if (!TextUtils.isEmpty(selection)) {
            selectionStatement = selectionStatement + " AND " + selection;
        }
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{"_id", "title", "artist", "album", "duration", "track", "artist_id", "album_id"}, selectionStatement, paramArrayOfString, null);

        return cursor;
    }


}
