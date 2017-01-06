/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wm.remusic.uitl;

/**
 * <p/>
 * 常量接口
 */
public interface IConstants {

    String MUSIC_COUNT_CHANGED = "com.wm.remusic.musiccountchanged";
    String PLAYLIST_ITEM_MOVED = "com.wm.remusic.mmoved";
    String PLAYLIST_COUNT_CHANGED = "com.wm.remusic.playlistcountchanged";
    String CHANGE_THEME = "com.wm.remusic.themechange";
    String EMPTY_LIST = "com.wm.remusic.emptyplaylist";
    String PACKAGE = "com.wm.remusic";
    int MUSICOVERFLOW = 0;
    int ARTISTOVERFLOW = 1;
    int ALBUMOVERFLOW = 2;
    int FOLDEROVERFLOW = 3;

    //歌手和专辑列表点击都会进入MyMusic 此时要传递参数表明是从哪里进入的
    int START_FROM_ARTIST = 1;
    int START_FROM_ALBUM = 2;
    int START_FROM_LOCAL = 3;
    int START_FROM_FOLDER = 4;
    int START_FROM_FAVORITE = 5;

    int FAV_PLAYLIST = 10;


    String NAVIGATE_NOWPLAYING = "navigate_nowplaying";


}
