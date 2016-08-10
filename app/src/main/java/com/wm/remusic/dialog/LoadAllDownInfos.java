package com.wm.remusic.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.wm.remusic.MainApplication;
import com.wm.remusic.R;
import com.wm.remusic.downmusic.DownloadManager;
import com.wm.remusic.downmusic.DownloadTask;
import com.wm.remusic.json.GeDanGeInfo;
import com.wm.remusic.json.MusicFileDownInfo;
import com.wm.remusic.net.BMA;
import com.wm.remusic.net.HttpUtil;
import com.wm.remusic.uitl.PreferencesUtility;

import java.io.File;
import java.util.ArrayList;

public class LoadAllDownInfos extends AsyncTask<Void,Void,Void> {
    Activity mContext;
    private ArrayList<GeDanGeInfo> mList = new ArrayList<GeDanGeInfo>();
    public LoadAllDownInfos(Activity context ,ArrayList<GeDanGeInfo> list){
        mContext = context;
        mList = list;
    }

    PopupWindow popupWindow;
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        View view;
        RotateAnimation animation = new RotateAnimation(0f,360f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,0.5f);
        animation.setDuration(5000);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setInterpolator(new LinearInterpolator());
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.loading_circle, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.rotate);
            popupWindow = new PopupWindow(view, 200, 220);


            imageView.setAnimation(animation);

        }
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        popupWindow.showAtLocation(mContext.getWindow().getDecorView(), Gravity.CENTER,0,0);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                cancel(true);
            }
        });
        animation.start();

    }

    int size;
    ArrayList<String> urlList = new ArrayList<>();

    @Override
    protected Void doInBackground(Void... params) {
        int le = mList.size();
        for(int j = 0; j< le ; j++){
            try {
                JsonArray jsonArray = HttpUtil.getResposeJsonObject(BMA.Song.songInfo(mList.get(j).getSong_id())).get("songurl")
                        .getAsJsonObject().get("url").getAsJsonArray();
                int len = jsonArray.size();

                int downloadBit = PreferencesUtility.getInstance(mContext).getDownMusicBit();
                MusicFileDownInfo musicFileDownInfo = null;
                for (int i = len - 1; i > -1; i--) {
                    int bit = Integer.parseInt(jsonArray.get(i).getAsJsonObject().get("file_bitrate").toString());
                    if (bit == downloadBit) {
                        musicFileDownInfo = MainApplication.gsonInstance().fromJson(jsonArray.get(i), MusicFileDownInfo.class);
                    } else if (bit < downloadBit && bit >= 64) {
                        musicFileDownInfo = MainApplication.gsonInstance().fromJson(jsonArray.get(i), MusicFileDownInfo.class);
                    }
                }
                if(musicFileDownInfo != null){
                    urlList.add(musicFileDownInfo.getShow_link());
                    size += musicFileDownInfo.getFile_size();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Log.e("size",size + "");

        String result = null;
        size = size /(1024 * 1024);
        Log.e("size",size + "");
        if(size > 1024){
            result = (float)Math.round((float) size/(1024 *10))/10 + "G";
        }else {
            result = size + "M";

        }

        new AlertDialog.Builder(mContext).setTitle("将下载歌曲,大约占用" + result + "空间,确定下载吗")
                .setPositiveButton(mContext.getString(R.string.sure), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/remusic/");
                            if (!file.exists()) {
                                file.mkdir();
                            }
                            int len = mList.size();
                            for(int i = 0; i<len; i++){
                                DownloadTask task = new DownloadTask.Builder(mContext, urlList.get(i))
                                        .setFileName(mList.get(i).getTitle()).setSaveDirPath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/remusic/").build();
                                DownloadManager.getInstance(mContext).addDownloadTask(task);
                            }


                        } else {
                            Toast.makeText(mContext,"没有储存卡",Toast.LENGTH_SHORT).show();
                            return;
                        }

                        dialog.dismiss();
                    }
                }).
                setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
        popupWindow.dismiss();
    }
}