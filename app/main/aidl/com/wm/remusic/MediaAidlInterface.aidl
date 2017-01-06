package com.wm.remusic;

import com.wm.remusic.service.MusicTrack;

interface MediaAidlInterface
{
    void openFile(String path);
    void open(in Map infos, in long [] list, int position);
    void stop();
    void pause();
    void play();
    void prev(boolean forcePrevious);
    void next();
    void enqueue(in long [] list,in Map infos, int action);
    Map getPlayinfos();
    void setQueuePosition(int index);
    void setShuffleMode(int shufflemode);
    void setRepeatMode(int repeatmode);
    void moveQueueItem(int from, int to);
    void refresh();
    void playlistChanged();
    boolean isPlaying();
    long [] getQueue();
    long getQueueItemAtPosition(int position);
    int getQueueSize();
    int getQueuePosition();
    int getQueueHistoryPosition(int position);
    int getQueueHistorySize();
    int[] getQueueHistoryList();
    long duration();
    long position();
    int secondPosition();
    long seek(long pos);
    void seekRelative(long deltaInMs);
    long getAudioId();
    MusicTrack getCurrentTrack();
    MusicTrack getTrack(int index);
    long getNextAudioId();
    long getPreviousAudioId();
    long getArtistId();
    long getAlbumId();
    String getArtistName();
    String getTrackName();
    boolean isTrackLocal();
    String getAlbumName();
    String getAlbumPath();
    String[] getAlbumPathtAll();
    String getPath();
    int getShuffleMode();
    int removeTracks(int first, int last);
    int removeTrack(long id);
    boolean removeTrackAtPosition(long id, int position);
    int getRepeatMode();
    int getMediaMountedCount();
    int getAudioSessionId();
    void setLockscreenAlbumArt(boolean enabled);
    void exit();
    void timing(int time);
}

