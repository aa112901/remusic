package com.wm.remusic;

import android.app.Application;
import android.content.Context;

import com.facebook.common.internal.Supplier;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.wm.remusic.provider.PlaylistInfo;
import com.wm.remusic.uitl.PreferencesUtility;

/**
 * Created by wm on 2016/2/23.
 */
public class MainApplication extends Application {

    //    private RefWatcher refWatcher;
    private static int MAX_MEM = 60 * ByteConstants.MB;
    private long favPlaylist = 10;

    private ImagePipelineConfig getConfigureCaches(Context context) {
        final MemoryCacheParams bitmapCacheParams = new MemoryCacheParams(
                MAX_MEM,// 内存缓存中总图片的最大大小,以字节为单位。
                Integer.MAX_VALUE,// 内存缓存中图片的最大数量。
                MAX_MEM,// 内存缓存中准备清除但尚未被删除的总图片的最大大小,以字节为单位。
                Integer.MAX_VALUE,// 内存缓存中准备清除的总图片的最大数量。
                Integer.MAX_VALUE);// 内存缓存中单个图片的最大大小。

        Supplier<MemoryCacheParams> mSupplierMemoryCacheParams = new Supplier<MemoryCacheParams>() {
            @Override
            public MemoryCacheParams get() {
                return bitmapCacheParams;
            }
        };
        ImagePipelineConfig.Builder builder = ImagePipelineConfig.newBuilder(context);
        builder.setBitmapMemoryCacheParamsSupplier(mSupplierMemoryCacheParams);
        return builder.build();
    }

    private void frescoInit() {
        Fresco.initialize(this, getConfigureCaches(this));
    }


//    public static RefWatcher getRefWatcher(Context context) {
//        MainApplication application = (MainApplication) context.getApplicationContext();
//        return application.refWatcher;
//    }

    @Override
    public void onCreate() {
        frescoInit();
        super.onCreate();
//        refWatcher = LeakCanary.install(this);

        if (PreferencesUtility.getInstance(this).getFavriateMusicPlaylist() == false) {
            PlaylistInfo.getInstance(this).addPlaylist(favPlaylist, getResources().getString(R.string.my_fav_playlist),
                    0, "res:/" + R.mipmap.lay_protype_default);
            PreferencesUtility.getInstance(this).setFavriateMusicPlaylist(true);
        }
    }

}