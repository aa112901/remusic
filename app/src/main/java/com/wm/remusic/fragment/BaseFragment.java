package com.wm.remusic.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;

import com.wm.remusic.service.MediaService;
import com.wm.remusic.uitl.IConstants;

/**
 * Created by wm on 2016/3/17.
 */
public class BaseFragment extends Fragment {

    //接受广播
    private BroadcastReceiver mStatusListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MediaService.META_CHANGED)) {
                reloadAdapter();
                updateTrackInfo();
            } else if (action.equals(IConstants.MUSIC_COUNT_CHANGED)) {
                reloadAdapter();
            } else if (action.equals(IConstants.PLAYLIST_COUNT_CHANGED)) {
                reloadAdapter();
            } else if (action.equals(MediaService.TRACK_PREPARED)) {
                updateTime();
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter f = new IntentFilter();
        f.addAction(MediaService.META_CHANGED);
        f.addAction(IConstants.MUSIC_COUNT_CHANGED);
        f.addAction(IConstants.PLAYLIST_COUNT_CHANGED);
        f.addAction(MediaService.TRACK_PREPARED);
        getActivity().registerReceiver(mStatusListener, f);
    }


    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mStatusListener);
    }

    public void reloadAdapter() {
    }

    public void updateTrackInfo() {
    }

    public void updateTime() {

    }

}
