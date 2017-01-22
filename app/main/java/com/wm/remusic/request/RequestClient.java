package com.wm.remusic.request;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by wm on 2017/1/2.
 */
public class RequestClient {
    public static final String API_URL = "http://tingapi.ting.baidu.com";


    public interface ZhuanLanApi {
        @GET("/v1/restserver/ting?from=android&version=5.8.1.0&channel=ppzs&operator=3&method=baidu.ting.plaza.index&cuid=89CF1E1A06826F9AB95A34DC0F6AAA14")
        Call<ZhuanLanAuthor> getAuthor(@Query("") String user);
    }

    public static void main(String[] args){
//        Gson customGsonInstance = new GsonBuilder()
//                .registerTypeAdapter(new TypeToken<List<AvengersCharacter>>() {
//                        }.getType(),
//                        new MarvelResultsDeserializer<AvengersCharacter>())
//                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ZhuanLanApi api = retrofit.create(ZhuanLanApi.class);

        Call<ZhuanLanAuthor> call = api.getAuthor("");
        call.enqueue(new Callback<ZhuanLanAuthor>() {
            @Override
            public void onResponse(Call<ZhuanLanAuthor> call, Response<ZhuanLanAuthor> response) {
                try {
                    System.out.println(response.body().mName + response.code() + "  " + response.isSuccessful() + "");
                } catch (Exception e) {


                }
            }

            @Override
            public void onFailure(Call<ZhuanLanAuthor> call, Throwable t) {
                System.out.println( "null");
            }
        });

    }

}
