package com.wm.remusic.downmusic;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.wm.remusic.MainApplication;
import com.wm.remusic.json.MusicDetailInfo;
import com.wm.remusic.json.MusicFileDownInfo;
import com.wm.remusic.net.BMA;
import com.wm.remusic.net.HttpUtil;
import com.wm.remusic.uitl.IConstants;
import com.wm.remusic.uitl.PreferencesUtility;

import java.io.File;

/**
 * Created by wm on 2016/5/30.
 */
public class Down {

    public static void downMusic(final Context context, final String ids[], final String names[]) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                int le = ids.length;
                for (int j = 0; j < le; j++) {
                    try {
                        JsonArray jsonArray = HttpUtil.getResposeJsonObject(BMA.Song.songInfo(ids[j]).trim()).get("songurl")
                                .getAsJsonObject().get("url").getAsJsonArray();
                        int len = jsonArray.size();

                        int downloadBit = PreferencesUtility.getInstance(context).getDownMusicBit();
                        MusicFileDownInfo musicFileDownInfo = null;
                        for (int i = len - 1; i > -1; i--) {
                            int bit = Integer.parseInt(jsonArray.get(i).getAsJsonObject().get("file_bitrate").toString());
                            if (bit == downloadBit) {
                                musicFileDownInfo = MainApplication.gsonInstance().fromJson(jsonArray.get(i), MusicFileDownInfo.class);
                            } else if (bit < downloadBit && bit >= 64) {
                                musicFileDownInfo = MainApplication.gsonInstance().fromJson(jsonArray.get(i), MusicFileDownInfo.class);
                            }
                        }

                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/remusic/");
                            if (!file.exists()) {
                                boolean r = file.mkdirs();
                            }
                            DownloadTask task = new DownloadTask.Builder(context, musicFileDownInfo.getShow_link())
                                    .setFileName(names[j])
                                    .setSaveDirPath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/remusic/").build();
                            //DownloadManager.getInstance(context).addDownloadTask(task);

                        } else {
                            Toast.makeText(context, "没有储存卡", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                Toast.makeText(context, "已经加入下载", Toast.LENGTH_SHORT).show();

            }
        }).start();
    }

    public static void downMusic(final Context context, final String id, final String name, final String artist) {


        new AsyncTask<String, String, MusicFileDownInfo>() {
            @Override
            protected MusicFileDownInfo doInBackground(final String... name) {
                JsonArray jsonArray = HttpUtil.getResposeJsonObject(BMA.Song.songInfo(id).trim()).get("songurl")
                        .getAsJsonObject().get("url").getAsJsonArray();
                int len = jsonArray.size();

                int downloadBit = PreferencesUtility.getInstance(context).getDownMusicBit();
                MusicFileDownInfo musicFileDownInfo;
                for (int i = len - 1; i > -1; i--) {
                    int bit = Integer.parseInt(jsonArray.get(i).getAsJsonObject().get("file_bitrate").toString());
                    if (bit == downloadBit) {
                        musicFileDownInfo = MainApplication.gsonInstance().fromJson(jsonArray.get(i), MusicFileDownInfo.class);
                        return musicFileDownInfo;
                    } else if (bit < downloadBit && bit >= 64) {
                        musicFileDownInfo = MainApplication.gsonInstance().fromJson(jsonArray.get(i), MusicFileDownInfo.class);
                        return musicFileDownInfo;
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(MusicFileDownInfo musicFileDownInfo) {
                if (musicFileDownInfo != null && musicFileDownInfo.getShow_link() != null) {
                    Intent i = new Intent(DownService.ADD_DOWNTASK);
                    i.setAction(DownService.ADD_DOWNTASK);
                    i.putExtra("id", id);
                    i.putExtra("name", name);
                    i.putExtra("artist", artist);
                    i.putExtra("url", musicFileDownInfo.getShow_link());
                    i.setPackage(IConstants.PACKAGE);
                    context.startService(i);
                }
            }
        }.execute();
    }

    public static MusicFileDownInfo getUrl(final Context context, final String id) {
        MusicFileDownInfo musicFileDownInfo = null;
        try {
            JsonArray jsonArray = HttpUtil.getResposeJsonObject(BMA.Song.songInfo(id).trim(), context, false).get("songurl")
                    .getAsJsonObject().get("url").getAsJsonArray();
            int len = jsonArray.size();
            int downloadBit = 192;

            for (int i = len - 1; i > -1; i--) {
                int bit = Integer.parseInt(jsonArray.get(i).getAsJsonObject().get("file_bitrate").toString());
                if (bit == downloadBit) {
                    musicFileDownInfo = MainApplication.gsonInstance().fromJson(jsonArray.get(i), MusicFileDownInfo.class);

                } else if (bit < downloadBit && bit >= 64) {
                    musicFileDownInfo = MainApplication.gsonInstance().fromJson(jsonArray.get(i), MusicFileDownInfo.class);
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return musicFileDownInfo;
    }

    public static MusicDetailInfo getInfo(final String id) {
        MusicDetailInfo info = null;
        try {
            JsonObject jsonObject = HttpUtil.getResposeJsonObject(BMA.Song.songBaseInfo(id).trim()).get("result")
                    .getAsJsonObject().get("items").getAsJsonArray().get(0).getAsJsonObject();
            info = MainApplication.gsonInstance().fromJson(jsonObject, MusicDetailInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return info;
    }


    static class getUrl extends Thread {
        boolean isRun = true;
        String id;
        MusicFileDownInfo musicFileDownInfo;

        public getUrl(String id) {
            this.id = id;
        }

        @Override
        public void run() {
            JsonArray jsonArray = HttpUtil.getResposeJsonObject(BMA.Song.songInfo(id).trim()).get("songurl")
                    .getAsJsonObject().get("url").getAsJsonArray();
            int len = jsonArray.size();

            int downloadBit = 128;

            for (int i = len - 1; i > -1; i--) {
                int bit = Integer.parseInt(jsonArray.get(i).getAsJsonObject().get("file_bitrate").toString());
                if (bit == downloadBit) {
                    musicFileDownInfo = MainApplication.gsonInstance().fromJson(jsonArray.get(i), MusicFileDownInfo.class);

                } else if (bit < downloadBit && bit >= 64) {
                    musicFileDownInfo = MainApplication.gsonInstance().fromJson(jsonArray.get(i), MusicFileDownInfo.class);
                }
            }
        }
    }

}
