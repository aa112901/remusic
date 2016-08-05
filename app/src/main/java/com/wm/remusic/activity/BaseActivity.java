package com.wm.remusic.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.wm.remusic.MediaAidlInterface;
import com.wm.remusic.R;
import com.wm.remusic.fragment.QuickControlsFragment;
import com.wm.remusic.service.MediaService;
import com.wm.remusic.service.MusicPlayer;
import com.wm.remusic.uitl.IConstants;

import java.lang.ref.WeakReference;

import static com.wm.remusic.service.MusicPlayer.mService;

/**
 * Created by wm on 2016/2/25.
 * activity基类
 */
public class BaseActivity extends AppCompatActivity implements ServiceConnection {

    private MusicPlayer.ServiceToken mToken;
    private PlaybackStatus mPlaybackStatus; //receiver 接受播放状态变化等
    private QuickControlsFragment fragment; //底部播放控制栏


    /**
     * 更新播放队列
     */
    public void updateQueue() {

    }

    /**
     * 更新歌曲状态信息
     */
    public void updateTrackInfo() {
    }

    public void refreshUI() {
    }

    public void updateTime() {

    }

    /**
     * @param p 更新歌曲缓冲进度值，p取值从0~100
     */
    public void updateBuffer(int p) {

    }


    /**
     * @param outState 取消保存状态
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
    }

    /**
     * @param savedInstanceState 取消保存状态
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        //super.onRestoreInstanceState(savedInstanceState);
    }


    /**
     * @param show 显示或关闭底部播放控制栏
     */
    protected void showQuickControl(boolean show) {
        Log.e("showorhide", MusicPlayer.getQueue().length + "");
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (show) {
            if (fragment == null) {
                fragment = QuickControlsFragment.newInstance();
                ft.add(R.id.bottom_container, fragment).commitAllowingStateLoss();
            } else {
                ft.show(fragment).commitAllowingStateLoss();
            }
        } else {
            if (fragment != null)
                ft.hide(fragment).commitAllowingStateLoss();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToken = MusicPlayer.bindToService(this, this);
        mPlaybackStatus = new PlaybackStatus(this);

        IntentFilter f = new IntentFilter();
        f.addAction(MediaService.PLAYSTATE_CHANGED);
        f.addAction(MediaService.META_CHANGED);
        f.addAction(MediaService.QUEUE_CHANGED);
        f.addAction(IConstants.MUSIC_COUNT_CHANGED);
        f.addAction(MediaService.TRACK_PREPARED);
        f.addAction(MediaService.BUFFER_UP);
        f.addAction("com.wm.remusic.emptyplaylist");
        registerReceiver(mPlaybackStatus, new IntentFilter(f));
        showQuickControl(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service) {
        mService = MediaAidlInterface.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
        mService = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        unbindService();
        try {
            unregisterReceiver(mPlaybackStatus);
        } catch (final Throwable e) {
        }

    }

    public void unbindService() {
        if (mToken != null) {
            MusicPlayer.unbindFromService(mToken);
            mToken = null;
        }
    }


    private final static class PlaybackStatus extends BroadcastReceiver {

        private final WeakReference<BaseActivity> mReference;


        public PlaybackStatus(final BaseActivity activity) {
            mReference = new WeakReference<>(activity);
        }


        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            BaseActivity baseActivity = mReference.get();
            if (baseActivity != null) {
                if (action.equals(MediaService.META_CHANGED)) {
                    baseActivity.updateTrackInfo();

                } else if (action.equals(MediaService.PLAYSTATE_CHANGED)) {

                } else if (action.equals(MediaService.TRACK_PREPARED)) {
                    baseActivity.updateTime();
                } else if (action.equals(MediaService.BUFFER_UP)) {
                    baseActivity.updateBuffer(intent.getIntExtra("progress", 0));
                } else if (action.equals("com.wm.remusic.emptyplaylist")) {

                } else if (action.equals(MediaService.REFRESH)) {

                } else if (action.equals(IConstants.MUSIC_COUNT_CHANGED)) {
                    baseActivity.refreshUI();
                } else if (action.equals(MediaService.PLAYLIST_CHANGED)) {

                } else if (action.equals(MediaService.QUEUE_CHANGED)) {
                    baseActivity.updateQueue();
                } else if (action.equals(MediaService.TRACK_ERROR)) {
                    final String errorMsg = context.getString(R.string.exit,
                            intent.getStringExtra(MediaService.TrackErrorExtra.TRACK_NAME));
                    Toast.makeText(baseActivity, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
