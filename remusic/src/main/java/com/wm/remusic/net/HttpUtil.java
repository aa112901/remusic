package com.wm.remusic.net;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.wm.remusic.net.PersistentCookieStore;

import java.io.File;
import java.io.FileOutputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import retrofit.client.OkClient;

/**
 * Created by wm on 2016/4/10.
 */
public class HttpUtil {
    private static final OkHttpClient mOkHttpClient = new OkHttpClient();

    public static JsonObject get(final  String url, String name) {
        try {
            mOkHttpClient.setConnectTimeout(1000, TimeUnit.MINUTES);
            mOkHttpClient.setReadTimeout(1000, TimeUnit.MINUTES);
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if(response.isSuccessful()){
                String c = response.body().string();
                Log.e("re",c);
                JsonParser parser = new JsonParser();
                JsonElement el = parser.parse(c);
                return el.getAsJsonObject();

            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public static void getOut(final  String url) {
        try {
            mOkHttpClient.setConnectTimeout(1000, TimeUnit.MINUTES);
            mOkHttpClient.setReadTimeout(1000, TimeUnit.MINUTES);
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if(response.isSuccessful()){

                FileOutputStream fo = new FileOutputStream("/storage/emulated/0/" + "gedangein" + ".json");
                byte[] c = new byte[1024];
                while (response.body().source().read(c) != -1){
                    fo.write(c);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static JsonObject getResposeJsonObject(String action1){
        try {
            mOkHttpClient.setConnectTimeout(1000, TimeUnit.MINUTES);
            mOkHttpClient.setReadTimeout(1000, TimeUnit.MINUTES);
            Request request = new Request.Builder()
                    .url(action1)
                    .addHeader("Referer","http://music.163.com/")
                    .addHeader("Cookie", "appver=1.5.0.75771")
                    .build();
            Response response = mOkHttpClient.newCall(request).execute();
            if(response.isSuccessful()){
                String c = response.body().string();
                JsonParser parser = new JsonParser();
                JsonElement el = parser.parse(c);
                return el.getAsJsonObject();

            }

        }catch (Exception e){
            e.printStackTrace();
        }

//       mOkHttpClient.setCookieHandler(new CookieManager(
//                new PersistentCookieStore(getContext().getApplicationContext()),
//                CookiePolicy.ACCEPT_ALL));

        return null;
    }

    public static void downMp3(final  String url,final String name){
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
                    if(response.isSuccessful()){
                        FileOutputStream fo = new FileOutputStream("/storage/emulated/0/" + name + ".mp3");
                        byte[] c = new byte[1024];
                        while (response.body().source().read(c) != -1){
                            fo.write(c);
                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();



}

    public static void postUrl(Context context,String j){
        try{
            String action = "https://music.163.com/weapi/login/";
            RequestBody formBody = new FormEncodingBuilder()
           //         .add("",)
                    .build();
            Log.e("post","p");
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
                Log.e("respose",response.body().string());
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void postNetease(Context context,String j){
        try{
            String action = "https://music.163.com/weapi/login/";
            RequestBody formBody = new FormEncodingBuilder()
                    .add("params", "9NdyZTlp0Q/f1E1ora4tGM0uLYXqh7MD0mk7632ilWQvRDPZ02UkHrGFUccwW4HZYpacpPnmE+oMr/HI/vhuQvg8zYKgDP6NOaXG8nKDJpQTfOAiXT5KDrJOvb7ejSj/")
                    .add("encSeckey", "ae878167c394a959699c025a5c36043d0ae043c42d7f55fe4d1191c8ac9f3abe285b78c4a25ed6d9394a0ba0cb83a9a62de697199bd337f1de183bb07d6764a051495ea873ad615bb0a7e69f44d9168fc78ed1d61feb142ad06679dce58257ee9005756a18032ff499a4e24f7658bb59de2219f21f568301d43dba500e0c2d3b")
                    .build();
            String json = "{\"params\": \"9NdyZTlp0Q/f1E1ora4tGM0uLYXqh7MD0mk7632ilWQvRDPZ02UkHrGFUccwW4HZYpacpPnmE+oMr/HI/vhuQvg8zYKgDP6NOaXG8nKDJpQTfOAiXT5KDrJOvb7ejSj/\",  " +
                    "\"encSecKey\": \"ae878167c394a959699c025a5c36043d0ae043c42d7f55fe4d1191c8ac9f3abe285b78c4a25ed6d9394a0ba0cb83a9a62de697199bd337f1de183bb07d6764a051495ea873ad615bb0a7e69f44d9168fc78ed1d61feb142ad06679dce58257ee9005756a18032ff499a4e24f7658bb59de2219f21f568301d43dba500e0c2d3b\"}";
            RequestBody requestBody = RequestBody.create(MediaType.parse("JSON"), json);
            Log.e("post","p");
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
                 Log.e("respose",response.body().string());
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
