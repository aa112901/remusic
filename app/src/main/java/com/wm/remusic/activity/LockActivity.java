package com.wm.remusic.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.wm.remusic.R;
import com.wm.remusic.handler.HandlerUtil;
import com.wm.remusic.info.MusicInfo;
import com.wm.remusic.lrc.DefaultLrcParser;
import com.wm.remusic.lrc.LrcRow;
import com.wm.remusic.provider.PlaylistsManager;
import com.wm.remusic.service.MediaService;
import com.wm.remusic.service.MusicPlayer;
import com.wm.remusic.uitl.IConstants;
import com.wm.remusic.widget.SildingFinishLayout;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by wm on 2016/12/21.
 */
public class LockActivity extends LockBaseActivity implements View.OnClickListener{

    private TextView mTime,mDate,mMusicName,mMusicArtsit,mLrc;
    private ImageView pre,play,next,fav;
    private Handler mHandler;
    private SildingFinishLayout mView;
    private SimpleDraweeView mBack;
    private PlaylistsManager playlistsManager;
    private boolean isFav;
    private List<LrcRow> lrcRows;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Intent intent = new Intent();
        intent.setAction(MediaService.LOCK_SCREEN);
        sendBroadcast(intent);
        super.onCreate(savedInstanceState);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav
                        // bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);


        playlistsManager = PlaylistsManager.getInstance(this);

        setContentView(R.layout.activity_lock);
        mTime = (TextView) findViewById(R.id.lock_time);
        mDate = (TextView) findViewById(R.id.lock_date);
        mMusicName = (TextView) findViewById(R.id.lock_music_name);
        mMusicArtsit = (TextView) findViewById(R.id.lock_music_artsit);
        mLrc = (TextView) findViewById(R.id.lock_music_lrc);
        pre = (ImageView) findViewById(R.id.lock_music_pre);
        play = (ImageView) findViewById(R.id.lock_music_play);
        next = (ImageView) findViewById(R.id.lock_music_next);
        fav = (ImageView) findViewById(R.id.lock_music_fav);
        mView = (SildingFinishLayout) findViewById(R.id.lock_root);
        mBack = (SimpleDraweeView) findViewById(R.id.lock_background);
        mView.setOnSildingFinishListener(new SildingFinishLayout.OnSildingFinishListener() {

                    @Override
                    public void onSildingFinish() {
                        finish();
                    }
                });
        mView.setTouchView(getWindow().getDecorView());
        mHandler = HandlerUtil.getInstance(this);
        mHandler.post(updateRunnable);
        pre.setOnClickListener(this);
        play.setOnClickListener(this);
        next.setOnClickListener(this);
        fav.setOnClickListener(this);


    }

    @Override
    protected void onUserLeaveHint() {
        Log.d("lock","onUserLeaveHint");
        super.onUserLeaveHint();

        Intent intent = new Intent();
        intent.setAction(MediaService.LOCK_SCREEN);
        intent.putExtra("islock",false);
        sendBroadcast(intent);
        finish();
    }

    Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm-MM月dd日 E", Locale.CHINESE);
            String date[] = simpleDateFormat.format(new Date()).split("-");
            mTime.setText(date[0]);
            mDate.setText(date[1]);
            if(lrcRows != null){
                int len = lrcRows.size() - 1;
                for(int i = 0 ; i < len; i++){
                   // Log.e("lock",lrcRows.get(i).getTime() + "   " + lrcRows.get(i).getContent());
                    if(MusicPlayer.position() >= lrcRows.get(i).getTime()){
                        mLrc.setText(lrcRows.get(i).getContent());
                    }
                }
            }else {
                mLrc.setText(null);
            }
            mHandler.postDelayed(updateRunnable,300);
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        Log.e("lock"," on resume");
        updateTrackInfo();
        updateTrack();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("lock"," on pause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("lock"," on stop");
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent();
        intent.setAction(MediaService.LOCK_SCREEN);
        intent.putExtra("islock",false);
        sendBroadcast(intent);
        mHandler.removeCallbacks(updateRunnable);
        super.onDestroy();
        Log.e("lock"," on destroy");

    }

    @Override
    public void onBackPressed() {
        // do nothing
    }

    public void updateTrackInfo(){
            mMusicName.setText(MusicPlayer.getTrackName());
                mMusicArtsit.setText(MusicPlayer.getArtistName());
            isFav = false;
            long[] favlists = playlistsManager.getPlaylistIds(IConstants.FAV_PLAYLIST);
            long currentid = MusicPlayer.getCurrentAudioId();
            for(long i : favlists){
                if(currentid == i){
                    isFav = true;
                    break;
                }
            }

            updateFav(isFav);

            if (MusicPlayer.isPlaying()) {
                play.setImageResource(R.drawable.lock_btn_pause);

            } else {
                play.setImageResource(R.drawable.lock_btn_play);
            }
    }

    private void updateFav(boolean b) {
        if (b) {
            fav.setImageResource(R.drawable.lock_btn_loved);
        } else {
            fav.setImageResource(R.drawable.lock_btn_love);
        }
    }

    public void updateTrack(){
        lrcRows = getLrcRows();
        String url = MusicPlayer.getAlbumPath();
        if (url == null) {
            mBack.setImageURI(Uri.parse("res:/" + R.drawable.login_bg_night));
        } else {
            try {
                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url)).build();
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setOldController(mBack.getController())
                        .setImageRequest(request)
                        .setControllerListener(controllerListener)
                        .build();
                mBack.setController(controller);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
        @Override
        public void onFailure(String id, Throwable throwable) {
            mBack.setImageURI(Uri.parse("res:/" + R.drawable.login_bg_night));
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.lock_music_pre:
                MusicPlayer.previous(this,true);
                break;
            case R.id.lock_music_play:
                MusicPlayer.playOrPause();
                break;
            case R.id.lock_music_next:
                MusicPlayer.next();
                break;
            case R.id.lock_music_fav:
                    if (isFav) {
                        playlistsManager.removeItem(this, IConstants.FAV_PLAYLIST,
                                MusicPlayer.getCurrentAudioId());
                        fav.setImageResource(R.drawable.lock_btn_love);
                        isFav = false;
                    } else {
                        try {
                            MusicInfo info = MusicPlayer.getPlayinfos().get(MusicPlayer.getCurrentAudioId());
                            playlistsManager.insertMusic(this,IConstants.FAV_PLAYLIST,info);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        fav.setImageResource(R.drawable.lock_btn_loved);
                        isFav = true;
                    }

                break;
        }
    }

    private List<LrcRow> getLrcRows() {

        List<LrcRow> rows = null;
        InputStream is = null;
        try {
            is = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/remusic/lrc/" + MusicPlayer.getCurrentAudioId());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (is == null) {
                return null;
            }
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            rows = DefaultLrcParser.getIstance().getLrcRows(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }


}
