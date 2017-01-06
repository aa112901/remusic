package com.wm.remusic.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import com.wm.remusic.R;
import com.wm.remusic.downmusic.DownService;
import com.wm.remusic.json.GeDanGeInfo;
import com.wm.remusic.json.MusicFileDownInfo;
import com.wm.remusic.net.MusicFileDownInfoGet;
import com.wm.remusic.net.RequestThreadPool;
import com.wm.remusic.uitl.IConstants;
import com.wm.remusic.uitl.PreferencesUtility;

import java.util.ArrayList;

public class LoadAllDownInfos extends AsyncTask<Void, Void, Void> {
    private Activity mContext;
    private ArrayList<GeDanGeInfo> mList = new ArrayList<GeDanGeInfo>();
    private PopupWindow popupWindow;
    private int size;
    private ArrayList<String> urlList = new ArrayList<>();

    public LoadAllDownInfos(Activity context, ArrayList<GeDanGeInfo> list) {
        mContext = context;
        mList = list;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        View view;
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.loading_circle, null);
            popupWindow = new PopupWindow(view, 200, 220);
        }
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        popupWindow.showAtLocation(mContext.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                RequestThreadPool.finish();
                cancel(true);
            }
        });
        //animation.start();

    }
    SparseArray<MusicFileDownInfo> sparseArray = new SparseArray<>();
    @Override
    protected Void doInBackground(Void... params) {
        int le = mList.size();
        int downloadBit = PreferencesUtility.getInstance(mContext).getDownMusicBit();
        for (int i = 0; i < le; i++) {
            RequestThreadPool.post(new MusicFileDownInfoGet(mList.get(i).getSong_id(), i, sparseArray,downloadBit));
        }
        int tryCount = 0;
        while (sparseArray.size() != le && tryCount < 6000){
            tryCount++;
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for(int i = 0 ; i< le;i++){
            size += sparseArray.get(i).getFile_size();
            urlList.add(sparseArray.get(i).getFile_link());
        }

//        for (int j = 0; j < le; j++) {
//            try {
//                JsonArray jsonArray = HttpUtil.getResposeJsonObject(BMA.Song.songInfo(mList.get(j).getSong_id())).get("songurl")
//                        .getAsJsonObject().get("url").getAsJsonArray();
//                int len = jsonArray.size();
//
//                int downloadBit = PreferencesUtility.getInstance(mContext).getDownMusicBit();
//                MusicFileDownInfo musicFileDownInfo = null;
//                for (int i = len - 1; i > -1; i--) {
//                    int bit = Integer.parseInt(jsonArray.get(i).getAsJsonObject().get("file_bitrate").toString());
//                    if (bit == downloadBit) {
//                        musicFileDownInfo = MainApplication.gsonInstance().fromJson(jsonArray.get(i), MusicFileDownInfo.class);
//                    } else if (bit < downloadBit && bit >= 64) {
//                        musicFileDownInfo = MainApplication.gsonInstance().fromJson(jsonArray.get(i), MusicFileDownInfo.class);
//                    }
//                }
//                if (musicFileDownInfo != null) {
//                    urlList.add(musicFileDownInfo.getShow_link());
//                    size += musicFileDownInfo.getFile_size();
//                }
//
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }

        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.e("size", size + "");

        String result = null;
        size = size / (1024 * 1024);
        Log.e("size", size + "");
        if (size > 1024) {
            result = (float) Math.round((float) size / (1024 * 10)) / 10 + "G";
        } else {
            result = size + "M";

        }

        new AlertDialog.Builder(mContext).setTitle("将下载歌曲,大约占用" + result + "空间,确定下载吗")
                .setPositiveButton(mContext.getString(R.string.sure), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int len = mList.size();
                        String[] names = new String[len];
                        String[] artists = new String[len];
                        for (int i = 0; i < len; i++) {
                            names[i] = mList.get(i).getTitle();
                            artists[i] = mList.get(i).getAuthor();
                        }
                        Intent intent = new Intent();
                        intent.putExtra("names", names);
                        intent.putExtra("artists", artists);
                        intent.putExtra("urls", urlList);
                        intent.setAction(DownService.ADD_MULTI_DOWNTASK);
                        intent.setPackage(IConstants.PACKAGE);
                        mContext.startService(intent);


                        dialog.dismiss();
                    }
                }).
                setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RequestThreadPool.finish();
                        cancel(true);
                        dialog.dismiss();
                    }
                }).show();
        popupWindow.dismiss();
    }
}