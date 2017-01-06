package com.wm.remusic.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.JsonArray;
import com.wm.remusic.MainApplication;
import com.wm.remusic.R;
import com.wm.remusic.downmusic.DownService;
import com.wm.remusic.handler.HandlerUtil;
import com.wm.remusic.json.MusicFileDownInfo;
import com.wm.remusic.net.BMA;
import com.wm.remusic.net.HttpUtil;
import com.wm.remusic.uitl.IConstants;
import com.wm.remusic.uitl.PreferencesUtility;

import java.util.ArrayList;

/**
 * Created by wm on 2016/8/8.
 */
public class AddDownTask extends DialogFragment {

    private String[] ids, names, artists;
    private Context mContext;
    private ArrayList<String> mList = new ArrayList<>();
    private String isLoding;

    public static AddDownTask newIntance(String[] ids, String[] names, String[] artists) {
        AddDownTask addDownTask = new AddDownTask();
        Bundle bundle = new Bundle();
        bundle.putStringArray("ids", ids);
        bundle.putStringArray("names", names);
        bundle.putStringArray("artists", artists);
        addDownTask.setArguments(bundle);
        return addDownTask;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        //设置无标题
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (getArguments() != null) {
            ids = getArguments().getStringArray("ids");
            names = getArguments().getStringArray("names");
        }

        if (getContext() != null) {
            mContext = getContext();
        }

        final LoadDownInfos loadDownInfos = new LoadDownInfos();
        loadDownInfos.execute();

        View view = inflater.inflate(R.layout.loading_dialog_fragment, container);
        SimpleDraweeView draweeView = (SimpleDraweeView) view.findViewById(R.id.loding_circle);
        RotateAnimation animation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(5000);
        draweeView.setAnimation(animation);
        animation.start();
        isLoding = "loding";
        HandlerUtil.getInstance(mContext).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isLoding != null) {
                    loadDownInfos.cancel(true);
                    dismiss();
                }
            }
        }, 10000);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.DownLoadingDialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        //设置fragment高度 、宽度
        int dialogHeight = (int) (getActivity().getResources().getDisplayMetrics().heightPixels);
        int dialogWidth = (int) (getActivity().getResources().getDisplayMetrics().widthPixels);
        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
        getDialog().setCanceledOnTouchOutside(true);

    }


    class LoadDownInfos extends AsyncTask<Void, Void, Void> {
        int size;

        @Override
        protected Void doInBackground(Void... params) {
            int le = ids.length;
            for (int j = 0; j < le; j++) {
                try {
                    JsonArray jsonArray = HttpUtil.getResposeJsonObject(BMA.Song.songInfo(ids[j]).trim()).get("songurl")
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
                    if (musicFileDownInfo != null) {
                        mList.add(musicFileDownInfo.getFile_link());
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
                            Intent intent = new Intent();
                            intent.putExtra("names", names);
                            intent.putExtra("artists", artists);
                            intent.putExtra("urls", mList);
                            intent.setAction(DownService.ADD_MULTI_DOWNTASK);
                            intent.setPackage(IConstants.PACKAGE);
                            mContext.startService(intent);

                            dialog.dismiss();
                        }
                    }).
                    setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();

            dismiss();
        }
    }

}
