package com.wm.remusic.net;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wm.remusic.json.GeDanGeInfo;
import com.wm.remusic.json.MusicDetailInfo;
import com.wm.remusic.service.MusicNetInfo;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by wm on 2016/6/29.
 */
public class JsonGet {
    static ArrayList<MusicDetailInfo> arrayList;
    public JsonGet(ArrayList<MusicDetailInfo> arrayList) {
        this.arrayList = arrayList;
    }
    static ExecutorService pool = Executors.newFixedThreadPool(20);
    public static void get(GetJson getJson){
        pool.execute(getJson);
    }
}
