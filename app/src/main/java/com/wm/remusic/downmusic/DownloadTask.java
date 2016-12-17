package com.wm.remusic.downmusic;

/**
 * Created by wm on 2016/4/12.
 */

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.wm.remusic.provider.DownFileStore;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by dzc on 15/11/21.
 */
public class DownloadTask implements Runnable {
    private DownloadDBEntity dbEntity;
    private DownFileStore downFileStore;
    private OkHttpClient client;
    private Context mContext;

    private String id;
    private long totalSize;
    private long completedSize;         //  Download section has been completed
    //    private float percent;        //  Percent Complete
    private String url;
    private String saveDirPath;
    private RandomAccessFile file;
    private int UPDATE_SIZE = 50 * 1024;    // The database is updated once every 50k
    private int downloadStatus = DownloadStatus.DOWNLOAD_STATUS_INIT;

    private String fileName;    // File name when saving
    private String artist;
    private String temp = ".temp";
    private boolean isPreparingDown;
    private String TAG = "DownloadTask";


    private List<DownloadTaskListener> listeners;

    public DownloadTask(Context context) {
        mContext = context.getApplicationContext();
        listeners = new ArrayList<>();
        downFileStore = DownFileStore.getInstance(context);
    }

    public DownloadTask(Context context, Builder builder) {
        //  mContext = context.getApplicationContext();
        listeners = new ArrayList<>();
        downFileStore = DownFileStore.getInstance(context);
        init(builder);
    }

    private void init(Builder builder) {
        mContext = builder.context;
        fileName = builder.fileName;
        artist = builder.art;
        saveDirPath = builder.saveDirPath;
        completedSize = builder.completedSize;
        dbEntity = builder.dbEntity;
        url = builder.url;
        totalSize = builder.totalSize;
        completedSize = builder.completedSize;
        id = builder.id;
        downloadStatus = builder.downloadStatus;
        UPDATE_SIZE = builder.UPDATE_SIZE;
        listeners = builder.listeners;

    }

    public static class Builder {

        private String url;
        private String fileName = url;    // File name when saving
        private String art;
        private String saveDirPath;
        private Context context;
        private DownloadDBEntity dbEntity = null;

        private String id;
        private long totalSize;
        private long completedSize;         //  Download section has been completed

        private int UPDATE_SIZE = 50 * 1024;    // The database is updated once every 50k

        private int downloadStatus = DownloadStatus.DOWNLOAD_STATUS_INIT;

        private List<DownloadTaskListener> listeners = new ArrayList<>();

        public Builder(Context context) {
            this.context = context.getApplicationContext();
        }


        public Builder(Context context, String url) {
            this.url = url;
            this.context = context.getApplicationContext();
        }

        public Builder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder setArtName(String art) {
            this.art = art;
            return this;
        }

        public Builder setSaveDirPath(String saveDirPath) {
            this.saveDirPath = saveDirPath;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setCache(int UPDATE_SIZE) {
            this.UPDATE_SIZE = UPDATE_SIZE;
            return this;
        }


        public Builder setCompletedSize(long completedSize) {
            this.completedSize = completedSize;
            return this;
        }

        public Builder setTotalSize(long totalSize) {
            this.totalSize = totalSize;
            return this;
        }

        public Builder setDBEntity(DownloadDBEntity dbEntity) {
            this.dbEntity = dbEntity;
            downloadStatus = dbEntity.getDownloadStatus();
            url = dbEntity.getUrl();
            id = dbEntity.getDownloadId();
            fileName = dbEntity.getFileName();
            art = dbEntity.getArtist();
            saveDirPath = dbEntity.getSaveDirPath();
            completedSize = dbEntity.getCompletedSize();
            totalSize = dbEntity.getTotalSize();

            return this;
        }


        public Builder setListeners(List<DownloadTaskListener> listeners) {
            this.listeners = listeners;
            return this;
        }

        public Builder setDownloadStatus(int downloadStatus) {
            this.downloadStatus = downloadStatus;
            return this;
        }


        public DownloadTask build() {
            // id = (saveDirPath + fileName).hashCode() + "";

            return new DownloadTask(context, this);
        }

    }


    @Override
    public void run() {
        Log.e("start", completedSize + "");
        downloadStatus = DownloadStatus.DOWNLOAD_STATUS_PREPARE;
        //  id = (saveDirPath + fileName).hashCode() + "";

        onPrepare();

        InputStream inputStream = null;
        BufferedInputStream bis = null;
        try {
            dbEntity = downFileStore.getDownLoadedList(id);
            file = new RandomAccessFile(saveDirPath + fileName, "rwd");
            if (dbEntity != null) {
                completedSize = dbEntity.getCompletedSize();
                totalSize = dbEntity.getTotalSize();
            }
            if (file.length() < completedSize) {
                completedSize = file.length();
            }
            long fileLength = file.length();
            if (fileLength != 0 && totalSize == fileLength) {
                downloadStatus = DownloadStatus.DOWNLOAD_STATUS_COMPLETED;
                totalSize = completedSize = fileLength;
                dbEntity = new DownloadDBEntity(id, totalSize, completedSize, url, saveDirPath, fileName, artist, downloadStatus);
                downFileStore.insert(dbEntity);
                Log.e(TAG, "file is completed , file length = " + fileLength + "  file totalsize = " + totalSize);
                Toast.makeText(mContext, fileName + "已经下载完成", Toast.LENGTH_SHORT).show();
                onCompleted();
                return;
            } else if (fileLength > totalSize) {
                completedSize = 0;
                totalSize = 0;
            }
            downloadStatus = DownloadStatus.DOWNLOAD_STATUS_START;

            onStart();
            Request request = new Request.Builder()
                    .url(url)
                    .header("RANGE", "bytes=" + completedSize + "-")//  Http value set breakpoints RANGE
                    .addHeader("Referer", url)
                    .build();
            Log.e("comlesize", completedSize + "");
            file.seek(completedSize);
            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                downloadStatus = DownloadStatus.DOWNLOAD_STATUS_DOWNLOADING;
                if (totalSize <= 0)
                    totalSize = responseBody.contentLength();

                inputStream = responseBody.byteStream();
                bis = new BufferedInputStream(inputStream);
                byte[] buffer = new byte[4 * 1024];
                int length = 0;
                int buffOffset = 0;
                if (dbEntity == null) {
                    dbEntity = new DownloadDBEntity(id, totalSize, 0L, url, saveDirPath, fileName, artist, downloadStatus);
                    downFileStore.insert(dbEntity);
                }
                while ((length = bis.read(buffer)) > 0 && downloadStatus != DownloadStatus.DOWNLOAD_STATUS_CANCEL && downloadStatus != DownloadStatus.DOWNLOAD_STATUS_PAUSE) {
                    file.write(buffer, 0, length);
                    completedSize += length;
                    buffOffset += length;
                    if (buffOffset >= UPDATE_SIZE) {
                        // Update download information database
                        if (totalSize <= 0 || dbEntity.getTotalSize() <= 0)
                            dbEntity.setToolSize(totalSize);
                        buffOffset = 0;
                        dbEntity.setCompletedSize(completedSize);
                        dbEntity.setDownloadStatus(downloadStatus);
                        downFileStore.update(dbEntity);
                        onDownloading();
                    }
                }
                //这两句根据需要自行选择是否注释，注释掉的话由于少了数据库的读取，速度会快一点，但同时如果在下载过程程序崩溃的话，程序不会保存最新的下载进度
                dbEntity.setCompletedSize(completedSize);
                dbEntity.setDownloadStatus(downloadStatus);
                downFileStore.update(dbEntity);

                onDownloading();
            }
        } catch (FileNotFoundException e) {
            downloadStatus = DownloadStatus.DOWNLOAD_STATUS_ERROR;
            onError(DownloadTaskListener.DOWNLOAD_ERROR_FILE_NOT_FOUND);
            return;
//            e.printStackTrace();
        } catch (IOException e) {
            downloadStatus = DownloadStatus.DOWNLOAD_STATUS_ERROR;
            onError(DownloadTaskListener.DOWNLOAD_ERROR_IO_ERROR);
            return;
        } finally {

            //String  nP = fileName.substring(0, path.length() - 5);
            dbEntity.setCompletedSize(completedSize);
            dbEntity.setFileName(fileName);
            downFileStore.update(dbEntity);
            if (bis != null) try {
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (inputStream != null) try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (file != null) try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        if (totalSize == completedSize) {
            String path = saveDirPath + fileName;
            File file = new File(path);
            Log.e("rename", path.substring(0, path.length() - 5));
            boolean c = file.renameTo(new File(path + ".mp3"));
            Log.e("rename", c + "");

            downloadStatus = DownloadStatus.DOWNLOAD_STATUS_COMPLETED;
            dbEntity.setDownloadStatus(downloadStatus);
            downFileStore.update(dbEntity);
            Uri contentUri = Uri.fromFile(new File(saveDirPath + fileName + ".mp3"));
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
            mContext.sendBroadcast(mediaScanIntent);
        }


        switch (downloadStatus) {
            case DownloadStatus.DOWNLOAD_STATUS_COMPLETED:
                onCompleted();
                break;
            case DownloadStatus.DOWNLOAD_STATUS_PAUSE:
                onPause();
                break;
            case DownloadStatus.DOWNLOAD_STATUS_CANCEL:
                downFileStore.deleteTask(dbEntity.getDownloadId());
                File temp = new File(saveDirPath + fileName);
                if (temp.exists()) temp.delete();
                onCancel();
                break;
        }
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public float getPercent() {
        if (totalSize == 0) {
            return 0;
        }
        return completedSize * 100 / totalSize;
    }

    public void setPreparingDown(boolean b) {
        isPreparingDown = b;
    }

    public boolean getPreparingDown() {
        return isPreparingDown;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }


    public long getCompletedSize() {
        return completedSize;
    }

    public void setCompletedSize(long completedSize) {
        this.completedSize = completedSize;
    }

    public String getSaveDirPath() {
        return saveDirPath;
    }

    public void setSaveDirPath(String saveDirPath) {
        this.saveDirPath = saveDirPath;
    }

    public int getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public void setdownFileStore(DownFileStore downFileStore) {
        this.downFileStore = downFileStore;
    }

    public void setDbEntity(DownloadDBEntity dbEntity) {
        this.dbEntity = dbEntity;
    }

    public DownloadDBEntity getDbEntity() {
        return dbEntity;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setHttpClient(OkHttpClient client) {
        this.client = client;
    }

    public String getFileName() {
        return fileName;
    }

    public String getArtistName() {
        return artist;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public void cancel() {
        setDownloadStatus(DownloadStatus.DOWNLOAD_STATUS_CANCEL);
        File temp = new File(saveDirPath + fileName);
        if (temp.exists()) temp.delete();
    }

    public void pause() {
        setDownloadStatus(DownloadStatus.DOWNLOAD_STATUS_PAUSE);
    }

    private void onPrepare() {
        if (listeners == null) {
            return;
        }
        for (DownloadTaskListener listener : listeners) {
            listener.onPrepare(this);
        }
    }

    private void onStart() {
        if (listeners == null) {
            return;
        }
        for (DownloadTaskListener listener : listeners) {
            listener.onStart(this);
        }
    }

    private void onDownloading() {
        if (listeners == null) {
            return;
        }
        for (DownloadTaskListener listener : listeners) {
            listener.onDownloading(this);
        }
    }

    private void onCompleted() {
        if (listeners == null) {
            return;
        }
        for (DownloadTaskListener listener : listeners) {
            listener.onCompleted(this);
        }
    }

    private void onPause() {
        if (listeners == null) {
            return;
        }
        for (DownloadTaskListener listener : listeners) {
            listener.onPause(this);
        }
    }

    private void onCancel() {
        if (listeners == null) {
            return;
        }
        for (DownloadTaskListener listener : listeners) {
            listener.onCancel(this);
        }
    }

    private void onError(int errorCode) {
        if (listeners == null) {
            return;
        }
        for (DownloadTaskListener listener : listeners) {
            listener.onError(this, errorCode);
        }
    }

    public void addDownloadListener(DownloadTaskListener listener) {
        Log.e("downtask", (listeners == null) + "");
        if (listener != null)
            listeners.add(listener);
    }

    /**
     * if listener is null,clear all listener
     *
     * @param listener
     */
    public void removeDownloadListener(DownloadTaskListener listener) {
        if (listener == null) {
            listeners.clear();
        } else {
            listeners.remove(listener);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DownloadTask)) {
            return false;
        }
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(saveDirPath)) {
            return false;
        }
        return url.equals(((DownloadTask) o).url) && saveDirPath.equals(((DownloadTask) o).saveDirPath);
    }

    public static DownloadTask parse(DownloadDBEntity entity, Context context) {
        //  DownloadTask task = new DownloadTask(context);
        DownloadTask task = new Builder(context).setDBEntity(entity).build();

//        task.setDownloadStatus(entity.getDownloadStatus());
//        task.setId(entity.getDownloadId());
//        task.setUrl(entity.getUrl());
//        task.setFileName(entity.getFileName());
//        task.setSaveDirPath(entity.getSaveDirPath());
//        task.setCompletedSize(entity.getCompletedSize());
//        task.setDbEntity(entity);
//        task.setTotalSize(entity.getTotalSize());
        return task;
    }
}
