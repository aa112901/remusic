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
import android.view.WindowManager;
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
public class LockBaseActivity extends AppCompatActivity implements ServiceConnection {

    private MusicPlayer.ServiceToken mToken;
    private PlaybackStatus mPlaybackStatus; //receiver 接受播放状态变化等
    private String TAG = "BaseActivity";

    /**
     * 更新歌曲状态信息
     */
    public void updateTrackInfo() {
    }


    public void updateTrack() {

    }

    public void updateLrc() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToken = MusicPlayer.bindToService(this, this);
        mPlaybackStatus = new PlaybackStatus(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter f = new IntentFilter();
        f.addAction(MediaService.META_CHANGED);
        f.addAction(MediaService.MUSIC_CHANGED);
        f.addAction(MediaService.LRC_UPDATED);
        registerReceiver(mPlaybackStatus, new IntentFilter(f));
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service) {
        mService = MediaAidlInterface.Stub.asInterface(service);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(mPlaybackStatus);
        } catch (final Throwable e) {
        }
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
        mService = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("lock"," on destroy");
        unbindService();
        // Unbind from the service
    }

    public void unbindService() {
        if (mToken != null) {
            MusicPlayer.unbindFromService(mToken);
            mToken = null;
        }
    }


    private final static class PlaybackStatus extends BroadcastReceiver {

        private final WeakReference<LockBaseActivity> mReference;


        public PlaybackStatus(final LockBaseActivity activity) {
            mReference = new WeakReference<>(activity);
        }


        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            LockBaseActivity baseActivity = mReference.get();
            if (baseActivity != null) {
                if (action.equals(MediaService.META_CHANGED)) {
                    baseActivity.updateTrackInfo();
                }   else if (action.equals(MediaService.MUSIC_CHANGED)) {
                    baseActivity.updateTrack();
                } else if (action.equals(MediaService.LRC_UPDATED)) {
                    baseActivity.updateLrc();
                }

            }
        }
    }
}
