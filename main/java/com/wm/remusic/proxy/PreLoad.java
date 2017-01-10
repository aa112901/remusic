package com.wm.remusic.proxy;

import android.content.Context;
import android.util.Log;

import com.wm.remusic.proxy.db.CacheFileInfoDao;
import com.wm.remusic.proxy.utils.Constants;
import com.wm.remusic.proxy.utils.HttpUtils;
import com.wm.remusic.proxy.utils.ProxyFileUtils;
import com.wm.remusic.proxy.utils.RequestDealThread;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class PreLoad extends Thread {

    private static final String LOG_TAG = RequestDealThread.class.getSimpleName();

    CacheFileInfoDao cacheDao = CacheFileInfoDao.getInstance();
    ProxyFileUtils fileUtils;
    URI uri;
    URL url;

    public PreLoad(Context context, String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        uri = URI.create(url);
        fileUtils = ProxyFileUtils.getInstance(context, uri, false);
    }

    public boolean download(int size) {
        try {
            Log.i(LOG_TAG, "缓存开始");
            //
            if (!fileUtils.isEnable()) {
                return false;
            }
            // 得到文件长度，如果超过缓冲给定长度，则返回
            int fileLength = fileUtils.getLength();
            if (fileLength >= size) {
                return true;
            }
            // 如果已经下载完成，返回
            System.out.println(fileUtils.getLength() + " " + cacheDao.getFileSize(fileUtils.getFileName()));
            if (fileUtils.getLength() == cacheDao.getFileSize(fileUtils.getFileName())) {
                return true;
            }
            // 从之前的位置缓存
            HttpURLConnection response = HttpUtils.send(url.openConnection());
            if (response == null) {
                return false;
            }
            int contentLength = Integer.valueOf(response.getHeaderField(Constants.CONTENT_LENGTH));
            cacheDao.insertOrUpdate(fileUtils.getFileName(), contentLength);
            //
            InputStream data = response.getInputStream();
            byte[] buff = new byte[1024 * 40];
            int readBytes = 0;
            int fileSize = 0;
            while (fileUtils.isEnable() && (readBytes = data.read(buff, 0, buff.length)) != -1) {
                fileUtils.write(buff, readBytes);
                fileSize += readBytes;
                if (fileSize >= size) {
                    break;
                }
            }
            if (fileUtils.isEnable()) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "缓存异常", e);
            return false;
        } finally {
            Log.i(LOG_TAG, "缓存结束");
            ProxyFileUtils.close(fileUtils, false);
        }
    }
}
