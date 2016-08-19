/**
 * Copyright (c) www.longdw.com
 */
package com.wm.remusic.uitl;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Albums;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Files.FileColumns;

import com.wm.remusic.R;
import com.wm.remusic.info.AlbumInfo;
import com.wm.remusic.info.ArtistInfo;
import com.wm.remusic.info.FolderInfo;
import com.wm.remusic.info.MusicInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询各主页信息，获取封面图片等
 */
public class MusicUtils implements IConstants {

    public static final int FILTER_SIZE = 1 * 1024 * 1024;// 1MB
    public static final int FILTER_DURATION = 1 * 60 * 1000;// 1分钟


    private static String[] proj_music = new String[]{
            Media._ID, Media.TITLE,
            Media.DATA, Media.ALBUM_ID,
            Media.ALBUM, Media.ARTIST,
            Media.ARTIST_ID, Media.DURATION, Media.SIZE};
    private static String[] proj_album = new String[]{Albums._ID, Albums.ALBUM_ART,
            Albums.ALBUM, Albums.NUMBER_OF_SONGS, Albums.ARTIST};
    private static String[] proj_artist = new String[]{
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
            MediaStore.Audio.Artists._ID};
    private static String[] proj_folder = new String[]{FileColumns.DATA};


    /**
     * 获取包含音频文件的文件夹信息
     *
     * @param context
     * @return
     */
    public static List<FolderInfo> queryFolder(Context context) {

        Uri uri = MediaStore.Files.getContentUri("external");
        ContentResolver cr = context.getContentResolver();
        StringBuilder mSelection = new StringBuilder(FileColumns.MEDIA_TYPE
                + " = " + FileColumns.MEDIA_TYPE_AUDIO + " and " + "("
                + FileColumns.DATA + " like'%.mp3' or " + Media.DATA
                + " like'%.wma')");
        // 查询语句：检索出.mp3为后缀名，时长大于1分钟，文件大小大于1MB的媒体文件
//		if(sp.getFilterSize()) {
//			mSelection.append(" and " + Media.SIZE + " > " + FILTER_SIZE);
//		}
//		if(sp.getFilterTime()) {
//			mSelection.append(" and " + Media.DURATION + " > " + FILTER_DURATION);
//		}
        mSelection.append(") group by ( " + FileColumns.PARENT);

        List<FolderInfo> list = getFolderList(cr.query(uri, proj_folder, mSelection.toString(), null,
                PreferencesUtility.getInstance(context).getFoloerSortOrder()));

        return list;

    }

    /**
     * 获取歌手信息
     *
     * @param context
     * @return
     */
    public static List<ArtistInfo> queryArtist(Context context) {

        Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;
        ContentResolver cr = context.getContentResolver();

        List<ArtistInfo> list = getArtistList(cr.query(uri, proj_artist,
                null, null, PreferencesUtility.getInstance(context).getArtistSortOrder()));

        return list;

    }

    /**
     * 获取专辑信息
     *
     * @param context
     * @return
     */
    public static List<AlbumInfo> queryAlbums(Context context) {

        ContentResolver cr = context.getContentResolver();
        StringBuilder where = new StringBuilder(Albums._ID
                + " in (select distinct " + Media.ALBUM_ID
                + " from audio_meta where (1=1 ");

//		if(sp.getFilterSize()) {
//			where.append(" and " + Media.SIZE + " > " + FILTER_SIZE);
//		}
//		if(sp.getFilterTime()) {
//			where.append(" and " + Media.DURATION + " > " + FILTER_DURATION);
//		}
        where.append("))");

//		if (mAlbumInfoDao.hasData()) {
//			return mAlbumInfoDao.getAlbumInfo();
//		} else

        // Media.ALBUM_KEY 按专辑名称排序
        List<AlbumInfo> list = getAlbumList(cr.query(Albums.EXTERNAL_CONTENT_URI, proj_album,
                where.toString(), null, PreferencesUtility.getInstance(context).getAlbumSortOrder()));
        //mAlbumInfoDao.saveAlbumInfo(list);
        return list;

    }

    public static List<MusicInfo> getAlbumsongs(Context context, String selection, String order) {
        ContentResolver cr = context.getContentResolver();
        return getMusicListCursor(cr.query(Media.EXTERNAL_CONTENT_URI, proj_music,
                "album_id = ?", new String[]{selection},
                order));
    }


    /**
     * @param context
     * @param from    不同的界面进来要做不同的查询
     * @return
     */
    public static List<MusicInfo> queryMusic(Context context, int from) {
        return queryMusic(context, null, null, from);
    }


    public static ArrayList<MusicInfo> queryMusic(Context context,
                                             String selections, String selection, int from) {

        Uri uri = Media.EXTERNAL_CONTENT_URI;
        ContentResolver cr = context.getContentResolver();

        StringBuffer select = new StringBuffer(" 1=1 ");
        // 查询语句：检索出.mp3为后缀名，时长大于1分钟，文件大小大于1MB的媒体文件
//		if(sp.getFilterSize()) {
//			select.append(" and " + Media.SIZE + " > " + FILTER_SIZE);
//		}
//		if(sp.getFilterTime()) {
//			select.append(" and " + Media.DURATION + " > " + FILTER_DURATION);
//		}
//
//		if (!TextUtils.isEmpty(selections)) {
//			select.append(selections);
//		}
        String selectionStatement = "is_music=1 AND title != ''";
        final String songSortOrder = PreferencesUtility.getInstance(context).getSongSortOrder();


        switch (from) {
            case START_FROM_LOCAL:
                ArrayList<MusicInfo> list3 = getMusicListCursor(cr.query(uri, proj_music,
                        selectionStatement, null,
                        songSortOrder));
                return list3;
            case START_FROM_ARTIST:
                return getMusicListCursor(cr.query(uri, proj_music, Media.ARTIST_ID + " = ?", new String[]{selection},
                        PreferencesUtility.getInstance(context).getArtistSongSortOrder()));
            case START_FROM_ALBUM:

                return getMusicListCursor(cr.query(uri, proj_music,
                        "album_id = ?", new String[]{selection},
                        PreferencesUtility.getInstance(context).getAlbumSongSortOrder()));
            case START_FROM_FOLDER:
                ArrayList<MusicInfo> list1 = new ArrayList<>();
                ArrayList<MusicInfo> list = getMusicListCursor(cr.query(Media.EXTERNAL_CONTENT_URI, proj_music,
                        null, null,
                        null));
                for (MusicInfo music : list) {
                    if (music.data.substring(0, music.data.lastIndexOf(File.separator)).equals(selection)) {
                        list1.add(music);
                    }
                }
                return list1;
            default:
                return null;
        }

    }


    public static ArrayList<MusicInfo> getMusicLists(Context context, long[] id) {
        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.Media._ID + " IN (");
        for (int i = 0; i < id.length; i++) {
            selection.append(id[i]);
            if (i < id.length - 1) {
                selection.append(",");
            }
        }
        selection.append(")");

        //sqlite 不支持decode

//        final StringBuilder order = new StringBuilder();
//        order.append("DECODE(" +MediaStore.Audio.Media._ID +",");
//        for (int i = 0; i < id.length; i++) {
//            order.append(id[i]);
//            order.append(",");
//            order.append(i);
//            if (i < id.length - 1) {
//                order.append(",");
//            }
//        }
//        order.append(")");

        Cursor cursor = (context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, proj_music,
                selection.toString(),
                null, null));
        ArrayList<MusicInfo> musicList = new ArrayList<>();
        musicList.ensureCapacity(id.length);
        for (int i = 0; i < id.length; i++) {
            musicList.add(null);
        }

        while (cursor.moveToNext()) {
            MusicInfo music = new MusicInfo();
            music.songId = cursor.getInt(cursor
                    .getColumnIndex(Media._ID));
            music.albumId = cursor.getInt(cursor
                    .getColumnIndex(Media.ALBUM_ID));
            music.albumName = cursor.getString(cursor
                    .getColumnIndex(Albums.ALBUM));
            music.albumData = getAlbumArtUri(music.albumId) + "";
            music.musicName = cursor.getString(cursor
                    .getColumnIndex(Media.TITLE));
            music.artist = cursor.getString(cursor
                    .getColumnIndex(Media.ARTIST));
            music.artistId = cursor.getLong(cursor.getColumnIndex(Media.ARTIST_ID));
            music.islocal = true;
            for (int i = 0; i < id.length; i++) {
                if (id[i] == music.songId) {
                    musicList.set(i, music);
                }
            }
        }
        cursor.close();
        return musicList;
    }


    public static ArrayList<MusicInfo> getMusicListCursor(Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        ArrayList<MusicInfo> musicList = new ArrayList<>();
        while (cursor.moveToNext()) {
            MusicInfo music = new MusicInfo();
            music.songId = cursor.getInt(cursor
                    .getColumnIndex(Media._ID));
            music.albumId = cursor.getInt(cursor
                    .getColumnIndex(Media.ALBUM_ID));
            music.albumName = cursor.getString(cursor
                    .getColumnIndex(Albums.ALBUM));
            music.albumData = getAlbumArtUri(music.albumId) + "";
            music.duration = cursor.getInt(cursor
                    .getColumnIndex(Media.DURATION));
            music.musicName = cursor.getString(cursor
                    .getColumnIndex(Media.TITLE));
            music.artist = cursor.getString(cursor
                    .getColumnIndex(Media.ARTIST));
            music.artistId = cursor.getLong(cursor.getColumnIndex(Media.ARTIST_ID));
            String filePath = cursor.getString(cursor
                    .getColumnIndex(Media.DATA));
            music.data = filePath;
            String folderPath = filePath.substring(0, filePath.lastIndexOf(File.separator));
            music.folder = folderPath;
            music.size = cursor.getInt(cursor
                    .getColumnIndex(Media.SIZE));
            music.islocal = true;
            musicList.add(music);
        }
        cursor.close();
        return musicList;
    }

    public static List<AlbumInfo> getAlbumList(Cursor cursor) {
        List<AlbumInfo> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            AlbumInfo info = new AlbumInfo();
            info.album_name = cursor.getString(cursor
                    .getColumnIndex(Albums.ALBUM));
            info.album_id = cursor.getInt(cursor.getColumnIndex(Albums._ID));
            info.number_of_songs = cursor.getInt(cursor
                    .getColumnIndex(Albums.NUMBER_OF_SONGS));
            info.album_art = getAlbumArtUri(info.album_id) + "";
            info.album_artist = cursor.getString(cursor.getColumnIndex(Albums.ARTIST));
            list.add(info);
        }
        cursor.close();
        return list;
    }

    public static List<ArtistInfo> getArtistList(Cursor cursor) {
        List<ArtistInfo> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            ArtistInfo info = new ArtistInfo();
            info.artist_name = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Artists.ARTIST));
            info.number_of_tracks = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS));
            info.artist_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Artists._ID));
            list.add(info);
        }
        cursor.close();
        return list;
    }

    public static List<FolderInfo> getFolderList(Cursor cursor) {
        List<FolderInfo> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            FolderInfo info = new FolderInfo();
            String filePath = cursor.getString(cursor
                    .getColumnIndex(FileColumns.DATA));
            info.folder_path = filePath.substring(0, filePath.lastIndexOf(File.separator));
            info.folder_name = info.folder_path.substring(info.folder_path
                    .lastIndexOf(File.separator) + 1);
            list.add(info);
        }
        cursor.close();
        return list;
    }

    public static String makeTimeString(long milliSecs) {
        StringBuffer sb = new StringBuffer();
        long m = milliSecs / (60 * 1000);
        sb.append(m < 10 ? "0" + m : m);
        sb.append(":");
        long s = (milliSecs % (60 * 1000)) / 1000;
        sb.append(s < 10 ? "0" + s : s);
        return sb.toString();
    }


    public static Uri getAlbumArtUri(long albumId) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
    }

    public static Uri getAlbumUri(Context context, long musicId) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(Media.EXTERNAL_CONTENT_URI, proj_music, "_id =" + String.valueOf(musicId), null, null);
        long id = -3;
        if (cursor == null) {
            cursor.close();
            return null;
        }
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndex(Media.ALBUM_ID));
        }

        cursor.close();
        return getAlbumArtUri(id);
    }

    public static String getalbumdata(Context context, long musicid) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(Media.EXTERNAL_CONTENT_URI, proj_music, "_id = " + String.valueOf(musicid), null, null);
        if (cursor == null) {
            return null;
        }
        long albumId = -1;
        if (cursor.moveToNext()) {
            albumId = cursor.getLong(cursor.getColumnIndex(Media.ALBUM_ID));
        }

        if (albumId != -1) {
            cursor = cr.query(Albums.EXTERNAL_CONTENT_URI, proj_album, Albums._ID + " = " + String.valueOf(albumId), null, null);
        }
        if (cursor == null) {
            return null;
        }
        String data = "";
        if (cursor.moveToNext()) {
            data = cursor.getString(cursor.getColumnIndex(Albums.ALBUM_ART));
        }
        cursor.close();

        return data;
    }


    public static ArtistInfo getArtistinfo(Context context, long id) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, proj_artist, "_id =" + String.valueOf(id), null, null);
        if (cursor == null) {
            cursor.close();
            return null;
        }
        ArtistInfo artistInfo = new ArtistInfo();
        while (cursor.moveToNext()) {
            artistInfo.artist_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
            artistInfo.number_of_tracks = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS));
        }
        cursor.close();
        return artistInfo;
    }


    public static AlbumInfo getAlbumInfo(Context context, long albumId) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(Albums.EXTERNAL_CONTENT_URI, proj_album, "_id =" + String.valueOf(albumId), null, null);
        if (cursor == null) {
            cursor.close();
            return null;
        }
        AlbumInfo albumInfo = new AlbumInfo();
        while (cursor.moveToNext()) {
            albumInfo.album_name = cursor.getString(cursor.getColumnIndex(Albums.ALBUM));
            albumInfo.album_art = cursor.getString(cursor.getColumnIndex(Albums.ALBUM_ART));
        }
        cursor.close();
        return albumInfo;

    }


    public static MusicInfo getMusicInfo(Context context, long id) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(Media.EXTERNAL_CONTENT_URI, proj_music, "_id = " + String.valueOf(id), null, null);
        if (cursor == null) {
            cursor.close();
            return null;
        }
        MusicInfo music = new MusicInfo();
        while (cursor.moveToNext()) {
            music.songId = cursor.getInt(cursor
                    .getColumnIndex(Media._ID));
            music.albumId = cursor.getInt(cursor
                    .getColumnIndex(Media.ALBUM_ID));
            music.albumName = cursor.getString(cursor
                    .getColumnIndex(Albums.ALBUM));
            music.albumData = getAlbumArtUri(music.albumId) + "";
            music.duration = cursor.getInt(cursor
                    .getColumnIndex(Media.DURATION));
            music.musicName = cursor.getString(cursor
                    .getColumnIndex(Media.TITLE));
            music.size = cursor.getInt(cursor.getColumnIndex(Media.SIZE));
            music.artist = cursor.getString(cursor
                    .getColumnIndex(Media.ARTIST));
            music.artistId = cursor.getLong(cursor.getColumnIndex(Media.ARTIST_ID));
            String filePath = cursor.getString(cursor
                    .getColumnIndex(Media.DATA));
            music.data = filePath;
            String folderPath = filePath.substring(0,
                    filePath.lastIndexOf(File.separator));
            music.folder = folderPath;
        }
        cursor.close();
        return music;
    }


    public static final String makeShortTimeString(final Context context, long secs) {
        long hours, mins;

        hours = secs / 3600;
        secs %= 3600;
        mins = secs / 60;
        secs %= 60;

        final String durationFormat = context.getResources().getString(hours == 0 ? R.string.durationformatshort : R.string.durationformatlong);
        return String.format(durationFormat, hours, mins, secs);
    }


}

