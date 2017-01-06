/*
 * Copyright (C) 2012 Andrew Neal
 * Copyright (C) 2014 The CyanogenMod Project
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.wm.remusic.service;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.wm.remusic.MediaAidlInterface;
import com.wm.remusic.R;
import com.wm.remusic.info.MusicInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.WeakHashMap;

public class MusicPlayer {

    private static final WeakHashMap<Context, ServiceBinder> mConnectionMap;
    private static final long[] sEmptyList;
    public static MediaAidlInterface mService = null;
    private static ContentValues[] mContentValuesCache = null;

    static {
        mConnectionMap = new WeakHashMap<Context, ServiceBinder>();
        sEmptyList = new long[0];
    }

    public static final ServiceToken bindToService(final Context context,
                                                   final ServiceConnection callback) {

        Activity realActivity = ((Activity) context).getParent();
        if (realActivity == null) {
            realActivity = (Activity) context;
        }
        final ContextWrapper contextWrapper = new ContextWrapper(realActivity);
        contextWrapper.startService(new Intent(contextWrapper, MediaService.class));
        final ServiceBinder binder = new ServiceBinder(callback,
                contextWrapper.getApplicationContext());
        if (contextWrapper.bindService(
                new Intent().setClass(contextWrapper, MediaService.class), binder, 0)) {
            mConnectionMap.put(contextWrapper, binder);
            return new ServiceToken(contextWrapper);
        }
        return null;
    }

    public static void unbindFromService(final ServiceToken token) {
        if (token == null) {
            return;
        }
        final ContextWrapper mContextWrapper = token.mWrappedContext;
        final ServiceBinder mBinder = mConnectionMap.remove(mContextWrapper);
        if (mBinder == null) {
            return;
        }
        mContextWrapper.unbindService(mBinder);
        if (mConnectionMap.isEmpty()) {
            mService = null;
        }
    }

    public static final boolean isPlaybackServiceConnected() {
        return mService != null;
    }

    public static void next() {
        try {
            if (mService != null) {
                mService.next();
            }
        } catch (final RemoteException ignored) {
        }
    }

    public static void initPlaybackServiceWithSettings(final Context context) {
        setShowAlbumArtOnLockscreen(true);
    }

    public static void setShowAlbumArtOnLockscreen(final boolean enabled) {
        try {
            if (mService != null) {
                mService.setLockscreenAlbumArt(enabled);
            }
        } catch (final RemoteException ignored) {
        }
    }

    public static void asyncNext(final Context context) {
        final Intent previous = new Intent(context, MediaService.class);
        previous.setAction(MediaService.NEXT_ACTION);
        context.startService(previous);
    }

    public static void previous(final Context context, final boolean force) {
        final Intent previous = new Intent(context, MediaService.class);
        if (force) {
            previous.setAction(MediaService.PREVIOUS_FORCE_ACTION);
        } else {
            previous.setAction(MediaService.PREVIOUS_ACTION);
        }
        context.startService(previous);
    }

    public static void playOrPause() {
        try {
            if (mService != null) {
                if (mService.isPlaying()) {
                    mService.pause();
                } else {
                    mService.play();
                }
            }
        } catch (final Exception ignored) {
        }
    }


    public static boolean isTrackLocal() {
        try {
            if (mService != null) {
                return mService.isTrackLocal();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void cycleRepeat() {
        try {
            if (mService != null) {
                if (mService.getShuffleMode() == MediaService.SHUFFLE_NORMAL) {
                    mService.setShuffleMode(MediaService.SHUFFLE_NONE);
                    mService.setRepeatMode(MediaService.REPEAT_CURRENT);
                    return;
                } else {

                    switch (mService.getRepeatMode()) {
                        case MediaService.REPEAT_CURRENT:
                            mService.setRepeatMode(MediaService.REPEAT_ALL);
                            break;
                        case MediaService.REPEAT_ALL:
                            mService.setShuffleMode(MediaService.SHUFFLE_NORMAL);
//                        if (mService.getShuffleMode() != MediaService.SHUFFLE_NONE) {
//                            mService.setShuffleMode(MediaService.SHUFFLE_NONE);
//                        }
                            break;

                    }
                }

            }
        } catch (final RemoteException ignored) {
        }
    }

    public static void cycleShuffle() {
        try {
            if (mService != null) {
                switch (mService.getShuffleMode()) {
                    case MediaService.SHUFFLE_NONE:
                        mService.setShuffleMode(MediaService.SHUFFLE_NORMAL);
                        if (mService.getRepeatMode() == MediaService.REPEAT_CURRENT) {
                            mService.setRepeatMode(MediaService.REPEAT_ALL);
                        }
                        break;
                    case MediaService.SHUFFLE_NORMAL:
                        mService.setShuffleMode(MediaService.SHUFFLE_NONE);
                        break;
//                    case MediaService.SHUFFLE_AUTO:
//                        mService.setShuffleMode(MediaService.SHUFFLE_NONE);
//                        break;
                    default:
                        break;
                }
            }
        } catch (final RemoteException ignored) {
        }
    }

    public static final boolean isPlaying() {
        if (mService != null) {
            try {
                return mService.isPlaying();
            } catch (final RemoteException ignored) {
            }
        }
        return false;
    }

    public static final int getShuffleMode() {
        if (mService != null) {
            try {
                return mService.getShuffleMode();
            } catch (final RemoteException ignored) {
            }
        }
        return 0;
    }

    public static void setShuffleMode(int mode) {
        try {
            if (mService != null) {
                mService.setShuffleMode(mode);
            }
        } catch (RemoteException ignored) {

        }
    }

    public static final int getRepeatMode() {
        if (mService != null) {
            try {
                return mService.getRepeatMode();
            } catch (final RemoteException ignored) {
            }
        }
        return 0;
    }

    public static final String getTrackName() {
        if (mService != null) {
            try {
                return mService.getTrackName();
            } catch (final RemoteException ignored) {
            }
        }
        return null;
    }

    public static final String getArtistName() {
        if (mService != null) {
            try {
                return mService.getArtistName();
            } catch (final RemoteException ignored) {
            }
        }
        return null;
    }

    public static final String getAlbumName() {
        if (mService != null) {
            try {
                return mService.getAlbumName();
            } catch (final RemoteException ignored) {
            }
        }
        return null;
    }

    public static final String getAlbumPath() {
        if (mService != null) {
            try {
                return mService.getAlbumPath();
            } catch (final RemoteException ignored) {
            }
        }
        return null;
    }

    public static final String[] getAlbumPathAll() {
        if (mService != null) {
            try {
                return mService.getAlbumPathtAll();
            } catch (final RemoteException ignored) {
            }
        }
        return null;
    }

    public static final long getCurrentAlbumId() {
        if (mService != null) {
            try {
                return mService.getAlbumId();
            } catch (final RemoteException ignored) {
            }
        }
        return -1;
    }

    public static final long getCurrentAudioId() {
        if (mService != null) {
            try {
                return mService.getAudioId();
            } catch (final RemoteException ignored) {
            }
        }
        return -1;
    }

    public static final MusicTrack getCurrentTrack() {
        if (mService != null) {
            try {
                return mService.getCurrentTrack();
            } catch (final RemoteException ignored) {
            }
        }
        return null;
    }

    public static final MusicTrack getTrack(int index) {
        if (mService != null) {
            try {
                return mService.getTrack(index);
            } catch (final RemoteException ignored) {
            }
        }
        return null;
    }

    public static final long getNextAudioId() {
        if (mService != null) {
            try {
                return mService.getNextAudioId();
            } catch (final RemoteException ignored) {
            }
        }
        return -1;
    }

    public static final long getPreviousAudioId() {
        if (mService != null) {
            try {
                return mService.getPreviousAudioId();
            } catch (final RemoteException ignored) {
            }
        }
        return -1;
    }

    public static final long getCurrentArtistId() {
        if (mService != null) {
            try {
                return mService.getArtistId();
            } catch (final RemoteException ignored) {
            }
        }
        return -1;
    }

    public static final int getAudioSessionId() {
        if (mService != null) {
            try {
                return mService.getAudioSessionId();
            } catch (final RemoteException ignored) {
            }
        }
        return -1;
    }

    public static final long[] getQueue() {
        try {
            if (mService != null) {
                return mService.getQueue();
            } else {
            }
        } catch (final RemoteException ignored) {
        }
        return sEmptyList;
    }

    public static final HashMap<Long, MusicInfo> getPlayinfos() {
        try {
            if (mService != null) {
                return (HashMap<Long, MusicInfo>) mService.getPlayinfos();
            } else {
            }
        } catch (final RemoteException ignored) {
        }
        return null;
    }

    public static final long getQueueItemAtPosition(int position) {
        try {
            if (mService != null) {
                return mService.getQueueItemAtPosition(position);
            } else {
            }
        } catch (final RemoteException ignored) {
        }
        return -1;
    }

    public static final int getQueueSize() {
        try {
            if (mService != null) {
                return mService.getQueueSize();
            } else {
            }
        } catch (final RemoteException ignored) {
        }
        return 0;
    }

    public static final int getQueuePosition() {
        try {
            if (mService != null) {
                return mService.getQueuePosition();
            }
        } catch (final RemoteException ignored) {
        }
        return 0;
    }

    public static void setQueuePosition(final int position) {
        if (mService != null) {
            try {
                mService.setQueuePosition(position);
            } catch (final RemoteException ignored) {
            }
        }
    }

    public static final int getQueueHistorySize() {
        if (mService != null) {
            try {
                return mService.getQueueHistorySize();
            } catch (final RemoteException ignored) {
            }
        }
        return 0;
    }

    public static final int getQueueHistoryPosition(int position) {
        if (mService != null) {
            try {
                return mService.getQueueHistoryPosition(position);
            } catch (final RemoteException ignored) {
            }
        }
        return -1;
    }

    public static final int[] getQueueHistoryList() {
        if (mService != null) {
            try {
                return mService.getQueueHistoryList();
            } catch (final RemoteException ignored) {
            }
        }
        return null;
    }

    public static final int removeTrack(final long id) {
        try {
            if (mService != null) {
                return mService.removeTrack(id);
            }
        } catch (final RemoteException ingored) {
        }
        return 0;
    }

    public static final boolean removeTrackAtPosition(final long id, final int position) {
        try {
            if (mService != null) {
                return mService.removeTrackAtPosition(id, position);
            }
        } catch (final RemoteException ingored) {
        }
        return false;
    }

    public static void moveQueueItem(final int from, final int to) {
        try {
            if (mService != null) {
                mService.moveQueueItem(from, to);
            } else {
            }
        } catch (final RemoteException ignored) {
        }
    }

    public static synchronized void playAll(final HashMap<Long, MusicInfo> infos, final long[] list, int position, final boolean forceShuffle) {
        if (list == null || list.length == 0 || mService == null) {
            return;
        }
        try {
            if (forceShuffle) {
                mService.setShuffleMode(MediaService.SHUFFLE_NORMAL);
            }
            final long currentId = mService.getAudioId();
            long playId = list[position];
            Log.e("currentId", currentId + "");
            final int currentQueuePosition = getQueuePosition();
            if (position != -1) {
                final long[] playlist = getQueue();
                if (Arrays.equals(list, playlist)) {
                    if (currentQueuePosition == position && currentId == list[position]) {
                        mService.play();
                        return;
                    } else {
                        mService.setQueuePosition(position);
                        return;
                    }

                }
            }
            if (position < 0) {
                position = 0;
            }
            mService.open(infos, list, forceShuffle ? -1 : position);
            mService.play();
            Log.e("time", System.currentTimeMillis() + "");
        } catch (final RemoteException ignored) {
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public static void playNext(Context context, final HashMap<Long, MusicInfo> map, final long[] list) {
        if (mService == null) {
            return;
        }
        try {
            int current = -1;
            long[] result = list;

            for (int i = 0; i < list.length; i++) {
                if (MusicPlayer.getCurrentAudioId() == list[i]) {
                    current = i;
                } else {
                    MusicPlayer.removeTrack(list[i]);
                }
            }

//            if( current != -1){
//                ArrayList lists = new ArrayList();
//                for(int i = 0; i<list.length;i++){
//                    if(i != current){
//                        lists.add(list[i]);
//                    }
//                }
//                result = new long[list.length - 1];
//                for(int i = 0;i<lists.size();i++){
//                     result[i] = (long) lists.get(i);
//                }
//            }

            mService.enqueue(list, map, MediaService.NEXT);

            Toast.makeText(context, R.string.next_play, Toast.LENGTH_SHORT).show();
        } catch (final RemoteException ignored) {
        }
    }

    public static String getPath() {
        if (mService == null) {
            return null;
        }
        try {
            return mService.getPath();

        } catch (Exception e) {

        }
        return null;
    }

    public static void stop() {
        try {
            mService.stop();
        } catch (Exception e) {

        }
    }

    public static final int getSongCountForAlbumInt(final Context context, final long id) {
        int songCount = 0;
        if (id == -1) {
            return songCount;
        }

        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id);
        Cursor cursor = context.getContentResolver().query(uri,
                new String[]{MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                if (!cursor.isNull(0)) {
                    songCount = cursor.getInt(0);
                }
            }
            cursor.close();
            cursor = null;
        }

        return songCount;
    }

    public static final String getReleaseDateForAlbum(final Context context, final long id) {
        if (id == -1) {
            return null;
        }
        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id);
        Cursor cursor = context.getContentResolver().query(uri, new String[]{
                MediaStore.Audio.AlbumColumns.FIRST_YEAR
        }, null, null, null);
        String releaseDate = null;
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                releaseDate = cursor.getString(0);
            }
            cursor.close();
            cursor = null;
        }
        return releaseDate;
    }

    public static void seek(final long position) {
        if (mService != null) {
            try {
                mService.seek(position);
            } catch (final RemoteException ignored) {
            }
        }
    }

    public static void seekRelative(final long deltaInMs) {
        if (mService != null) {
            try {
                mService.seekRelative(deltaInMs);
            } catch (final RemoteException ignored) {
            } catch (final IllegalStateException ignored) {

            }
        }
    }

    public static final long position() {
        if (mService != null) {
            try {
                return mService.position();
            } catch (final RemoteException ignored) {
            } catch (final IllegalStateException ex) {

            }
        }
        return 0;
    }

    public static final int secondPosition() {
        if (mService != null) {
            try {
                return mService.secondPosition();
            } catch (final RemoteException ignored) {
            } catch (final IllegalStateException ex) {

            }
        }
        return 0;
    }

    public static final long duration() {
        if (mService != null) {
            try {
                return mService.duration();
            } catch (final RemoteException ignored) {
            } catch (final IllegalStateException ignored) {

            }
        }
        return 0;
    }

    public static void clearQueue() {

        try {
            if(mService != null)
            mService.removeTracks(0, Integer.MAX_VALUE);
        } catch (final RemoteException ignored) {
        }
    }

    public static void addToQueue(final Context context, final long[] list, long sourceId) {
        if (mService == null) {
            return;
        }
        try {
            mService.enqueue(list, null, MediaService.LAST);
            //final String message = makeLabel(context, R.plurals.NNNtrackstoqueue, list.length);
            //Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        } catch (final RemoteException ignored) {
        }
    }


    public static void addToPlaylist(final Context context, final long[] ids, final long playlistid) {
        final int size = ids.length;
        final ContentResolver resolver = context.getContentResolver();
        final String[] projection = new String[]{
                "max(" + "play_order" + ")",
        };
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistid);
        Cursor cursor = null;
        int base = 0;

        try {
            cursor = resolver.query(uri, projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                base = cursor.getInt(0) + 1;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        int numinserted = 0;
        for (int offSet = 0; offSet < size; offSet += 1000) {
            makeInsertItems(ids, offSet, 1000, base);
            numinserted += resolver.bulkInsert(uri, mContentValuesCache);
        }

    }

    public static void makeInsertItems(final long[] ids, final int offset, int len, final int base) {
        if (offset + len > ids.length) {
            len = ids.length - offset;
        }

        if (mContentValuesCache == null || mContentValuesCache.length != len) {
            mContentValuesCache = new ContentValues[len];
        }
        for (int i = 0; i < len; i++) {
            if (mContentValuesCache[i] == null) {
                mContentValuesCache[i] = new ContentValues();
            }
            mContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + offset + i);
            mContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, ids[offset + i]);
        }
    }

    public static final long createPlaylist(final Context context, final String name) {
        if (name != null && name.length() > 0) {
            final ContentResolver resolver = context.getContentResolver();
            final String[] projection = new String[]{
                    MediaStore.Audio.PlaylistsColumns.NAME
            };
            final String selection = MediaStore.Audio.PlaylistsColumns.NAME + " = '" + name + "'";
            Cursor cursor = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    projection, selection, null, null);
            if (cursor.getCount() <= 0) {
                final ContentValues values = new ContentValues(1);
                values.put(MediaStore.Audio.PlaylistsColumns.NAME, name);
                final Uri uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                        values);
                return Long.parseLong(uri.getLastPathSegment());
            }
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
            return -1;
        }
        return -1;
    }

    public static void exitService() {
//        if (mService == null) {
//            return;
//        }
        try {
            mConnectionMap.clear();
            Log.e("exitmp", "Destroying service");
            mService.exit();
        } catch (Exception e) {
        }
    }

    public static void timing(int time) {
        if (mService == null) {
            return;
        }
        try {
            mService.timing(time);
        } catch (Exception e) {

        }
    }

    public static final class ServiceBinder implements ServiceConnection {
        private final ServiceConnection mCallback;
        private final Context mContext;


        public ServiceBinder(final ServiceConnection callback, final Context context) {
            mCallback = callback;
            mContext = context;
        }

        @Override
        public void onServiceConnected(final ComponentName className, final IBinder service) {
            mService = MediaAidlInterface.Stub.asInterface(service);
            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
            initPlaybackServiceWithSettings(mContext);
        }

        @Override
        public void onServiceDisconnected(final ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }
            mService = null;
        }
    }

    public static final class ServiceToken {
        public ContextWrapper mWrappedContext;

        public ServiceToken(final ContextWrapper context) {
            mWrappedContext = context;
        }
    }


}
