package com.wm.remusic.net;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.internal.DiskLruCache;
import com.squareup.okhttp.internal.Util;
import com.squareup.okhttp.internal.io.FileSystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

import okio.BufferedSource;
import okio.Okio;

/**
 * Created by wm on 2016/4/10.
 */
public class HttpUtil {
    public  static final OkHttpClient mOkHttpClient = new OkHttpClient();


    public static void getOut(final String url) {
        try {
            mOkHttpClient.setConnectTimeout(1000, TimeUnit.MINUTES);
            mOkHttpClient.setReadTimeout(1000, TimeUnit.MINUTES);
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {

                FileOutputStream fo = new FileOutputStream("/storage/emulated/0/" + "gedangein" + ".json");
                byte[] c = new byte[1024];
                while (response.body().source().read(c) != -1) {
                    fo.write(c);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Bitmap getBitmapStream(Context context, String url, boolean forceCache) {
        try {
            File sdcache = context.getExternalCacheDir();
            //File cacheFile = new File(context.getCacheDir(), "[缓存目录]");
            Cache cache = new Cache(sdcache.getAbsoluteFile(), 1024 * 1024 * 30); //30Mb
            mOkHttpClient.setCache(cache);

            mOkHttpClient.setConnectTimeout(1000, TimeUnit.MINUTES);
            mOkHttpClient.setReadTimeout(1000, TimeUnit.MINUTES);
            Request.Builder builder = new Request.Builder()
                    .url(url);
            if (forceCache) {
                builder.cacheControl(CacheControl.FORCE_CACHE);
            }
            Request request = builder.build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return _decodeBitmapFromStream(response.body().byteStream(), 160, 160);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static FilterInputStream getFromCache(Context context, String url) throws Exception {
        //  File cacheDirectory = new File("/storage/emulated/0/Android/data/com.name.demo .dev/cache/HttpCache");
        File cacheDirectory = context.getExternalCacheDir();
        DiskLruCache cache = DiskLruCache.create(FileSystem.SYSTEM, cacheDirectory, 201105, 2, 1024 * 1024 * 30);
        cache.flush();
        String key = Util.md5Hex(url);
        final DiskLruCache.Snapshot snapshot;
        try {
            snapshot = cache.get(key);
            if (snapshot == null) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
        okio.Source source = snapshot.getSource(1);
        BufferedSource metadata = Okio.buffer(source);
        FilterInputStream bodyIn = new FilterInputStream(metadata.inputStream()) {
            @Override
            public void close() throws IOException {
                snapshot.close();
                super.close();
            }
        };
        return bodyIn;
    }

    private static int _calculateInSampleSize(BitmapFactory.Options options,
                                              int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    /**
     * URL编码
     *
     * @param url
     * @return
     */
    public static String urlEncode(String url) {
        try {
            url = java.net.URLEncoder.encode(url, "UTF-8");
            url = url.replaceAll("%2F", "/");
            url = url.replaceAll("%3A", ":");
            url = url.replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static Bitmap _decodeBitmapFromStream(InputStream inputStream,
                                                 int reqWidth, int reqHeight) {
        byte[] byteArr = new byte[0];
        byte[] buffer = new byte[1024];
        int len;
        int count = 0;

        try {
            while ((len = inputStream.read(buffer)) > -1) {
                if (len != 0) {
                    if (count + len > byteArr.length) {
                        byte[] newbuf = new byte[(count + len) * 2];
                        System.arraycopy(byteArr, 0, newbuf, 0, count);
                        byteArr = newbuf;
                    }

                    System.arraycopy(buffer, 0, byteArr, count, len);
                    count += len;
                }
            }

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(byteArr, 0, count, options);

            options.inSampleSize = _calculateInSampleSize(options, reqWidth,
                    reqHeight);
            options.inPurgeable = false;
            options.inInputShareable = true;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            return BitmapFactory.decodeByteArray(byteArr, 0, count, options);

        } catch (Exception e) {
            // close_print_exception

            return null;
        }
    }


    public static String getResposeString(String action1) {
        try {
            mOkHttpClient.setConnectTimeout(1000, TimeUnit.MINUTES);
            mOkHttpClient.setReadTimeout(1000, TimeUnit.MINUTES);
            Request request = new Request.Builder()
                    .url(action1)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                String c = response.body().string();
                Log.e("billboard", c);
                return c;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

//       mOkHttpClient.setCookieHandler(new CookieManager(
//                new PersistentCookieStore(getContext().getApplicationContext()),
//                CookiePolicy.ACCEPT_ALL));

        return null;
    }

    public static JsonObject getResposeJsonObject(String action1, Context context, boolean forceCache) {
        try {
            Log.e("action-cache", action1);
            File sdcache = context.getCacheDir();
            //File cacheFile = new File(context.getCacheDir(), "[缓存目录]");
            Cache cache = new Cache(sdcache.getAbsoluteFile(), 1024 * 1024 * 30); //30Mb
            mOkHttpClient.setCache(cache);

            mOkHttpClient.setConnectTimeout(1000, TimeUnit.MINUTES);
            mOkHttpClient.setReadTimeout(1000, TimeUnit.MINUTES);
            Request.Builder builder = new Request.Builder()
                    .url(action1);
            if (forceCache) {
                builder.cacheControl(CacheControl.FORCE_CACHE);
            }
            Request request = builder.build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                String c = response.body().string();
                Log.e("cache", c);
                JsonParser parser = new JsonParser();
                JsonElement el = parser.parse(c);
                return el.getAsJsonObject();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

//       mOkHttpClient.setCookieHandler(new CookieManager(
//                new PersistentCookieStore(getContext().getApplicationContext()),
//                CookiePolicy.ACCEPT_ALL));

        return null;
    }


    public static JsonObject getResposeJsonObject(String action1) {
        try {
            mOkHttpClient.setConnectTimeout(3000, TimeUnit.MINUTES);
            mOkHttpClient.setReadTimeout(3000, TimeUnit.MINUTES);
            Request request = new Request.Builder()
                    .url(action1)
//                    .addHeader("Referer","http://music.163.com/")
//                    .addHeader("Cookie", "appver=1.5.0.75771")
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                String c = response.body().string();
//                FileOutputStream fileOutputStream = new FileOutputStream("/sdcard/" + System.currentTimeMillis() + ".txt");
//                fileOutputStream.write(c.getBytes());
//                fileOutputStream.close();
                JsonParser parser = new JsonParser();
                JsonElement el = parser.parse(c);
                return el.getAsJsonObject();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

//       mOkHttpClient.setCookieHandler(new CookieManager(
//                new PersistentCookieStore(getContext().getApplicationContext()),
//                CookiePolicy.ACCEPT_ALL));

        return null;
    }

    public static void downMp3(final String url, final String name) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mOkHttpClient.setConnectTimeout(1000, TimeUnit.MINUTES);
                    mOkHttpClient.setReadTimeout(1000, TimeUnit.MINUTES);
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    Response response = mOkHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        FileOutputStream fo = new FileOutputStream("/storage/emulated/0/" + name + ".mp3");
                        byte[] c = new byte[1024];
                        while (response.body().source().read(c) != -1) {
                            fo.write(c);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    public static void postUrl(Context context, String j) {
        try {
            String action = "https://music.163.com/weapi/login/";
            RequestBody formBody = new FormEncodingBuilder()
                    //         .add("",)
                    .build();
            Log.e("post", "p");
            Request request = new Request.Builder()
                    .url(action)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "music.163.com")
                    .header("Cookie", "appver=1.5.0.75771")
                    .header("Referer", "http://music.163.com/")
                    .header("Connection", "keep-alive")
                    .header("Accept-Encoding", "gzip,deflate")
                    .header("Accept", "*/*")
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                    .post(formBody)
                    .build();

            mOkHttpClient.setCookieHandler(new CookieManager(
                    new PersistentCookieStore(context.getApplicationContext()),
                    CookiePolicy.ACCEPT_ALL));

            Response response = mOkHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                Log.e("respose", response.body().string());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void postNetease(Context context, String j) {
        try {
            String action = "https://music.163.com/weapi/login/";
            RequestBody formBody = new FormEncodingBuilder()
                    .add("params", "9NdyZTlp0Q/f1E1ora4tGM0uLYXqh7MD0mk7632ilWQvRDPZ02UkHrGFUccwW4HZYpacpPnmE+oMr/HI/vhuQvg8zYKgDP6NOaXG8nKDJpQTfOAiXT5KDrJOvb7ejSj/")
                    .add("encSeckey", "ae878167c394a959699c025a5c36043d0ae043c42d7f55fe4d1191c8ac9f3abe285b78c4a25ed6d9394a0ba0cb83a9a62de697199bd337f1de183bb07d6764a051495ea873ad615bb0a7e69f44d9168fc78ed1d61feb142ad06679dce58257ee9005756a18032ff499a4e24f7658bb59de2219f21f568301d43dba500e0c2d3b")
                    .build();
            String json = "{\"params\": \"9NdyZTlp0Q/f1E1ora4tGM0uLYXqh7MD0mk7632ilWQvRDPZ02UkHrGFUccwW4HZYpacpPnmE+oMr/HI/vhuQvg8zYKgDP6NOaXG8nKDJpQTfOAiXT5KDrJOvb7ejSj/\",  " +
                    "\"encSecKey\": \"ae878167c394a959699c025a5c36043d0ae043c42d7f55fe4d1191c8ac9f3abe285b78c4a25ed6d9394a0ba0cb83a9a62de697199bd337f1de183bb07d6764a051495ea873ad615bb0a7e69f44d9168fc78ed1d61feb142ad06679dce58257ee9005756a18032ff499a4e24f7658bb59de2219f21f568301d43dba500e0c2d3b\"}";
            RequestBody requestBody = RequestBody.create(MediaType.parse("JSON"), json);
            Log.e("post", "p");
            Request request = new Request.Builder()
                    .url(action)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Host", "music.163.com")
                    .header("Cookie", "appver=1.5.0.75771")
                    .header("Referer", "http://music.163.com/")
                    .header("Connection", "keep-alive")
                    .header("Accept-Encoding", "gzip,deflate")
                    .header("Accept", "*/*")
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                    .post(requestBody)
                    .build();

            mOkHttpClient.setCookieHandler(new CookieManager(
                    new PersistentCookieStore(context.getApplicationContext()),
                    CookiePolicy.ACCEPT_ALL));

            Response response = mOkHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                Log.e("respose", response.body().string());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
