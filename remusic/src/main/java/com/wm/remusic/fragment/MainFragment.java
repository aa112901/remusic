package com.wm.remusic.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wm.remusic.R;
import com.wm.remusic.adapter.MainFragmentAdapter;
import com.wm.remusic.adapter.MainFragmentItem;
import com.wm.remusic.info.Playlist;
import com.wm.remusic.recent.Song;
import com.wm.remusic.recent.SongLoader;
import com.wm.remusic.recent.TopTracksLoader;
import com.wm.remusic.provider.PlaylistInfo;
import com.wm.remusic.service.MediaService;
import com.wm.remusic.uitl.DividerItemDecoration;
import com.wm.remusic.uitl.IConstants;
import com.wm.remusic.uitl.MusicUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by wm on 2016/3/8.
 */
public class MainFragment extends Fragment {

    private MainFragmentAdapter mAdapter;
    private ActionBar ab;
    private ArrayList<Playlist> playlists;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private List results = Collections.emptyList();
    private List<MainFragmentItem> mList = new ArrayList<>();
    private PlaylistInfo playlistInfo;
    private int localMusicCount, recentMusicCount, artistsCount;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        playlistInfo = PlaylistInfo.getInstance(getContext());
        this.results = new ArrayList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);


        ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setTitle("");
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        //先给adapter设置空数据，异步加载好后更新数据，防止Recyclerview no attach
        mAdapter = new MainFragmentAdapter(getActivity(), null, null);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        reloadAdapter();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

//        //注册广播
        IntentFilter f = new IntentFilter();
        f.addAction(IConstants.MUSIC_COUNT_CHANGED);
        f.addAction(IConstants.PLAYLIST_COUNT_CHANGED);
        getActivity().registerReceiver(mStatusListener, f);
        reloadAdapter();

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            //相当于Fragment的onResume
            reloadAdapter();
        } else {
            //相当于Fragment的onPause

        }
    }

    @Override
    public void onPause() {

        getActivity().unregisterReceiver(mStatusListener);
        super.onPause();
    }

    //接受歌曲播放变化和列表变化广播，刷新列表
    private BroadcastReceiver mStatusListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MediaService.META_CHANGED)) {
                //reloadAdapter();
            } else if (action.equals(IConstants.MUSIC_COUNT_CHANGED)) {
                reloadAdapter();
            } else if (action.equals(IConstants.PLAYLIST_COUNT_CHANGED)) {
                reloadAdapter();
            }
        }
    };

    //为info设置数据，并放入mlistInfo
    public void setInfo(String title, int count, int id) {
        // mlistInfo.clear();
        MainFragmentItem information = new MainFragmentItem();
        information.setTitle(title);
        information.setCount(count);
        information.setAvatar(id);
        mList.add(information); //将新的info对象加入到信息列表中
    }

    //设置音乐overflow条目
    private void setMusicInfo() {
        TopTracksLoader recentloader = new TopTracksLoader(getActivity().getApplicationContext(), TopTracksLoader.QueryType.RecentSongs);
        List<Song> recentsongs = SongLoader.getSongsForCursor(TopTracksLoader.getCursor());
        recentMusicCount = recentsongs.size();
        //设置mlistInfo，listview要显示的内容
        localMusicCount = MusicUtils.queryMusic(getContext().getApplicationContext(), IConstants.START_FROM_LOCAL).size();
        artistsCount = MusicUtils.queryArtist(getContext().getApplicationContext()).size();
        setInfo(getContext().getResources().getString(R.string.local_music), localMusicCount, R.drawable.music_icn_local);
        setInfo(getContext().getResources().getString(R.string.recent_play), recentMusicCount, R.drawable.music_icn_recent);
        setInfo(getContext().getResources().getString(R.string.local_manage), localMusicCount, R.drawable.music_icn_dld);
        setInfo(getContext().getResources().getString(R.string.my_artist), artistsCount, R.drawable.music_icn_artist);
    }

    //刷新列表
    public void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                mList.clear();
                results.clear();
                setMusicInfo();
                playlists = playlistInfo.getPlaylist();
                results.addAll(mList);
                results.add(getContext().getResources().getString(R.string.created_playlists));
                results.addAll(playlists);
                mAdapter.updateResults(results, playlists);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    //异步加载recyclerview界面
    private class loadSongs extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (getActivity() != null) {
                setMusicInfo();
                playlists = playlistInfo.getPlaylist();
                results.addAll(mList);
                results.add(getContext().getResources().getString(R.string.created_playlists));
                results.addAll(playlists);
                mAdapter = new MainFragmentAdapter(getActivity(), results, playlists);
            }

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            recyclerView.setAdapter(mAdapter);


        }

        @Override
        protected void onPreExecute() {

        }
    }


}
