package com.wm.remusic.net;

import android.util.Log;
import android.util.SparseArray;

import com.google.gson.JsonObject;
import com.wm.remusic.MainApplication;
import com.wm.remusic.json.MusicDetailInfo;

public class MusicDetailInfoGet implements Runnable {
    String id;
    int p;
    SparseArray<MusicDetailInfo> arrayList;

    public MusicDetailInfoGet(String id, int position, SparseArray<MusicDetailInfo> arrayList) {
        this.id = id;
        p = position;
        this.arrayList = arrayList;
    }

    @Override
    public void run() {
        try {
            MusicDetailInfo info = null;
            JsonObject jsonObject = HttpUtil.getResposeJsonObject(BMA.Song.songBaseInfo(id).trim()).get("result")
                    .getAsJsonObject().get("items").getAsJsonArray().get(0).getAsJsonObject();
            info = MainApplication.gsonInstance().fromJson(jsonObject, MusicDetailInfo.class);
            synchronized (this) {
                Log.e("arraylist", "size" + arrayList.size());
                arrayList.put(p, info);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}