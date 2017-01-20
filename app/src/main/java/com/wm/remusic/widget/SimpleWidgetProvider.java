package com.wm.remusic.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.wm.remusic.R;
import com.wm.remusic.info.MusicInfo;
import com.wm.remusic.provider.PlaylistsManager;
import com.wm.remusic.service.MediaService;
import com.wm.remusic.service.MusicPlayer;
import com.wm.remusic.uitl.IConstants;
import com.wm.remusic.uitl.ImageUtils;

import java.util.HashMap;

public class SimpleWidgetProvider extends AppWidgetProvider {

    private SimpleWidgetProvider mProvider = null;
    private boolean mStop = true;
    private static String art,trackname ,albumuri;
    private static boolean isTrackLocal = true;
    private static Bitmap noBit;
    private static boolean isFav ,isPlaying;
    private static long currentId = -1;
    private static long position,duration;
    private static HashMap<String, Bitmap> albumMap = new HashMap<>();   //储存专辑封面的图片
    private static boolean isInUse;
    private String TAG = SimpleWidgetProvider.class.getSimpleName();

    private PendingIntent getPendingIntent(Context context, int buttonId) {
        Intent intent = new Intent();
        intent.setClass(context, SimpleWidgetProvider.class);
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        intent.setData(Uri.parse("harvic:" + buttonId));
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        return pi;
    }

    // 更新所有的 widget
    private synchronized void pushUpdate(final Context context,AppWidgetManager appWidgetManager ,boolean updateProgress) {
        pushAction(context,MediaService.SEND_PROGRESS);
        RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.simple_control_widget_layout);
        //将按钮与点击事件绑定
        remoteView.setOnClickPendingIntent(R.id.widget_play,getPendingIntent(context, R.id.widget_play));
        remoteView.setOnClickPendingIntent(R.id.widget_pre, getPendingIntent(context, R.id.widget_pre));
        remoteView.setOnClickPendingIntent(R.id.widget_next, getPendingIntent(context, R.id.widget_next));
        remoteView.setOnClickPendingIntent(R.id.widget_love, getPendingIntent(context, R.id.widget_love));
        remoteView.setTextViewText(R.id.widget_content,trackname == null && art == null ? "" : trackname + "-" + art);
        remoteView.setProgressBar(R.id.widget_progress, (int) duration,(int) position,false);
        isFav = false;
        long[] favlists = PlaylistsManager.getInstance(context).getPlaylistIds(IConstants.FAV_PLAYLIST);
        for(long i : favlists){
            if(currentId == i){
                isFav = true;
                break;
            }
        }
        if (isFav) {
            remoteView.setImageViewResource(R.id.widget_love,R.drawable.widget_unstar_selector);
        } else {
            remoteView.setImageViewResource(R.id.widget_love,R.drawable.widget_star_selector);
        }
        if(isPlaying){
            remoteView.setImageViewResource(R.id.widget_play,R.drawable.widget_pause_selector);
        }else {
            remoteView.setImageViewResource(R.id.widget_play,R.drawable.widget_play_selector);
        }

        if(updateProgress){
            if(albumuri == null){
                remoteView.setImageViewResource(R.id.widget_image, R.drawable.placeholder_disk_210);
            }else {
                if(isTrackLocal){
                    Bitmap bitmap = ImageUtils.getArtworkQuick(context, Uri.parse(albumuri), 160, 160);
                    if (bitmap != null) {
                        remoteView.setImageViewBitmap(R.id.widget_image, bitmap);
                    }else {
                        remoteView.setImageViewResource(R.id.widget_image, R.drawable.placeholder_disk_210);
                    }

                }else {
                    Bitmap bitmap = albumMap.get(albumuri);
                    if(bitmap != null)
                    remoteView.setImageViewBitmap(R.id.widget_image,bitmap);
                }
            }

        }else {
            if(albumuri == null){
                remoteView.setImageViewResource(R.id.widget_image, R.drawable.placeholder_disk_210);
            }else {
                if(isTrackLocal){
                    final Bitmap bitmap = ImageUtils.getArtworkQuick(context, Uri.parse(albumuri), 160, 160);
                    if (bitmap != null) {
                        remoteView.setImageViewBitmap(R.id.widget_image, bitmap);
                    }else {
                        remoteView.setImageViewResource(R.id.widget_image, R.drawable.placeholder_disk_210);
                    }
                    albumMap.clear();
                }else {

                    if (albumMap.get(albumuri) != null) {
                        remoteView.setImageViewBitmap(R.id.widget_image, albumMap.get(albumuri));
                        //noBit = null;
                    } else {
                        Uri uri = Uri.parse(albumuri);
                        if(uri == null){
                            noBit = BitmapFactory.decodeResource(context.getResources(), R.drawable.placeholder_disk_210);
                            albumMap.put(albumuri,noBit);
                            pushUpdate(context,AppWidgetManager.getInstance(context),false);
                        }else {
                            ImageRequest imageRequest = ImageRequestBuilder
                                    .newBuilderWithSource(uri)
                                    .setProgressiveRenderingEnabled(true)
                                    .build();
                            ImagePipeline imagePipeline = Fresco.getImagePipeline();
                            DataSource<CloseableReference<CloseableImage>>
                                    dataSource = imagePipeline.fetchDecodedImage(imageRequest, this);

                            dataSource.subscribe(new BaseBitmapDataSubscriber() {

                                                     @Override
                                                     public void onNewResultImpl(@Nullable Bitmap bitmap) {
                                                         // You can use the bitmap in only limited ways
                                                         // No need to do any cleanup.
                                                         if (bitmap != null) {
                                                             noBit = bitmap.copy(bitmap.getConfig(),true);
                                                             albumMap.put(albumuri,noBit);
                                                         }
                                                         pushUpdate(context,AppWidgetManager.getInstance(context),false);
                                                     }

                                                     @Override
                                                     public void onFailureImpl(DataSource dataSource) {
                                                         // No cleanup required here.
                                                         noBit = BitmapFactory.decodeResource(context.getResources(), R.drawable.placeholder_disk_210);
                                                         albumMap.put(albumuri,noBit);
                                                         pushUpdate(context,AppWidgetManager.getInstance(context), false);
                                                     }
                                                 },
                                    CallerThreadExecutor.getInstance());
                        }
                    }
                }
            }

        }


        // 相当于获得所有本程序创建的appwidget
        ComponentName componentName = new ComponentName(context,SimpleWidgetProvider.class);
        appWidgetManager.updateAppWidget(componentName, remoteView);
    }


    public void onEnabled(Context context) {
        Log.e(TAG,"onenabled = " + isInUse);
        super.onEnabled(context);

        isInUse = true;
        Log.e(TAG,"onenabled = " + isInUse);

    }
    //当最后一个该Widget删除是调用该方法，注意是最后一个
    public void onDisabled(Context context) {
        Log.e(TAG,"ondisable = " + isInUse);
        super.onDisabled(context);
        isInUse = false;
        Log.e(TAG,"ondisable = " + isInUse);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        Log.e(TAG,"update = " + isInUse);
        if(isInUse)
        pushUpdate(context,appWidgetManager ,false);

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        albumMap.clear();
    }

    // 接收广播的回调函数
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(action)) {
            this.onEnabled(context);
        }
        else if (AppWidgetManager.ACTION_APPWIDGET_DISABLED.equals(action)) {
            this.onDisabled(context);
        }
        Log.e(TAG,"action = " + action);
        if(!isInUse){
            return;
        }

        if (intent.hasCategory(Intent.CATEGORY_ALTERNATIVE)) {
            Uri data = intent.getData();
            int buttonId = Integer.parseInt(data.getSchemeSpecificPart());
            switch (buttonId) {
                case R.id.widget_play:
                  pushAction(context, MediaService.TOGGLEPAUSE_ACTION);
                break;
                case R.id.widget_pre:
                  pushAction(context, MediaService.PREVIOUS_ACTION);
                break;
                case R.id.widget_next:
                  pushAction(context, MediaService.NEXT_ACTION);
                break;
                case R.id.widget_love:
                    long[] favlists = PlaylistsManager.getInstance(context).getPlaylistIds(IConstants.FAV_PLAYLIST);
                    for(long i : favlists){
                        if(currentId == i){
                            isFav = true;
                            break;
                        }
                    }
                    if (isFav) {
                        PlaylistsManager.getInstance(context).removeItem(context, IConstants.FAV_PLAYLIST,
                                MusicPlayer.getCurrentAudioId());
                        isFav = false;
                    } else {
                        try {
                            MusicInfo info = MusicPlayer.getPlayinfos().get(MusicPlayer.getCurrentAudioId());
                            PlaylistsManager.getInstance(context).insertMusic(context,IConstants.FAV_PLAYLIST,info);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        isFav = true;
                    }
                    pushUpdate(context,AppWidgetManager.getInstance(context) ,true);
                    break;

            }

        } else if(action.equals(MediaService.META_CHANGED)){
            isPlaying = intent.getBooleanExtra("playing",false);
            pushUpdate(context,AppWidgetManager.getInstance(context) ,false);

        } else if(action.equals(MediaService.SEND_PROGRESS)){
            Log.e("widget","  " + duration + "    " + position);
            duration = intent.getLongExtra("duration",0);
            position = intent.getLongExtra("position",0);
            pushUpdate(context,AppWidgetManager.getInstance(context) ,true);
        } else if(action.equals(MediaService.MUSIC_CHANGED)){
            trackname = intent.getStringExtra("track");
            art = intent.getStringExtra("artist");
            albumuri = intent.getStringExtra("albumuri");
            isTrackLocal = intent.getBooleanExtra("islocal",true);
            currentId = intent.getLongExtra("id",-1);
            isPlaying = intent.getBooleanExtra("playing",false);
            Log.e("harvic","art = " + art + "  trackname = " + trackname);
            pushUpdate(context,AppWidgetManager.getInstance(context) ,false);
        }

        super.onReceive(context, intent);
    }

    private void pushAction(Context context, String ACTION) {
        Log.e("widget","action = " + ACTION);
        Intent startIntent = new Intent(context,MediaService.class);
        startIntent.setAction(ACTION);
        context.startService(startIntent);
    }
}