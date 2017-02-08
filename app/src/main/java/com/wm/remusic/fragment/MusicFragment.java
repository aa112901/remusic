package com.wm.remusic.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bilibili.magicasakura.widgets.TintImageView;
import com.github.promeg.pinyinhelper.Pinyin;
import com.wm.remusic.R;
import com.wm.remusic.activity.AlbumsDetailActivity;
import com.wm.remusic.activity.SelectActivity;
import com.wm.remusic.handler.HandlerUtil;
import com.wm.remusic.info.MusicInfo;
import com.wm.remusic.service.MusicPlayer;
import com.wm.remusic.uitl.Comparator.MusicComparator;
import com.wm.remusic.uitl.IConstants;
import com.wm.remusic.uitl.MusicUtils;
import com.wm.remusic.uitl.PreferencesUtility;
import com.wm.remusic.uitl.SortOrder;
import com.wm.remusic.widget.DividerItemDecoration;
import com.wm.remusic.widget.SideBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by wm on 2016/1/19.
 */
public class MusicFragment extends BaseFragment {
    private Adapter mAdapter;
    private ArrayList<MusicInfo> musicInfos;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private PreferencesUtility mPreferences;
    private FrameLayout frameLayout;
    private View view;
    private boolean isFirstLoad = true;
    private SideBar sideBar;
    private TextView dialogText;
    private HashMap<String, Integer> positionMap = new HashMap<>();
    private boolean isAZSort = true;


    private void loadView() {
        //setUservisibleHint 可能先与attach
        if (view == null && mContext != null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.recylerview, frameLayout, false);

            dialogText = (TextView) view.findViewById(R.id.dialog_text);
            recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
            layoutManager = new LinearLayoutManager(mContext);
            recyclerView.setLayoutManager(layoutManager);
            mAdapter = new Adapter(null);
            recyclerView.setAdapter(mAdapter);
            recyclerView.setHasFixedSize(true);
            //fastScroller = (FastScroller) view.findViewById(R.id.fastscroller);
            recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));

            sideBar = (SideBar) view.findViewById(R.id.sidebar);
            sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
                @Override
                public void onTouchingLetterChanged(String s) {
                    dialogText.setText(s);
                    sideBar.setView(dialogText);
                    if (positionMap.get(s) != null) {
                        int i = positionMap.get(s);
                        ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(i + 1, 0);
                    }

                }
            });
            reloadAdapter();
            Log.e("MusicFragment", "load l");
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            loadView();
        }

    }

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                sideBar.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferencesUtility.getInstance(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.load_framelayout, container, false);
        frameLayout = (FrameLayout) view.findViewById(R.id.loadframe);
        View loadView = LayoutInflater.from(mContext).inflate(R.layout.loading, frameLayout, false);
        frameLayout.addView(loadView);
        isFirstLoad = true;
        isAZSort = mPreferences.getSongSortOrder().equals(SortOrder.SongSortOrder.SONG_A_Z);

        if(getUserVisibleHint()){
            loadView();
        }

        return view;
    }


    //刷新列表
    public void reloadAdapter() {
        if (mAdapter == null) {
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                isAZSort = mPreferences.getSongSortOrder().equals(SortOrder.SongSortOrder.SONG_A_Z);
                ArrayList<MusicInfo> songList = (ArrayList) MusicUtils.queryMusic(mContext, IConstants.START_FROM_LOCAL);
                // 名称排序时，重新排序并加入位置信息
                if (isAZSort) {
                    Collections.sort(songList, new MusicComparator());
                    for (int i = 0; i < songList.size(); i++) {
                        if (positionMap.get(songList.get(i).sort) == null)
                            positionMap.put(songList.get(i).sort, i);
                    }
                }
                mAdapter.updateDataSet(songList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mAdapter.notifyDataSetChanged();
                if (isAZSort) {
                    recyclerView.addOnScrollListener(scrollListener);
                } else {
                    sideBar.setVisibility(View.INVISIBLE);
                    recyclerView.removeOnScrollListener(scrollListener);
                }
                Log.e("MusicFragment","load t");
                if (isFirstLoad) {
                    Log.e("MusicFragment","load");
                    frameLayout.removeAllViews();
                    //framelayout 创建了新的实例
                    ViewGroup p = (ViewGroup) view.getParent();
                    if (p != null) {
                        p.removeAllViewsInLayout();
                    }
                    frameLayout.addView(view);
                    isFirstLoad = false;
                }
            }
        }.execute();
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.song_sort_by, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort_by_az:
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_A_Z);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_date:
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_DATE);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_artist:
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_ARTIST);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_album:
                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_ALBUM);
                reloadAdapter();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    //异步加载recyclerview界面
    private class loadSongs extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (mContext != null) {
                musicInfos = (ArrayList<MusicInfo>) MusicUtils.queryMusic(mContext, IConstants.START_FROM_LOCAL);

                for (int i = 0; i < musicInfos.size(); i++) {
                    char c = Pinyin.toPinyin(musicInfos.get(i).musicName.charAt(0)).charAt(0);
                }
                if (musicInfos != null)
                    mAdapter = new Adapter(musicInfos);
            }

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            recyclerView.setAdapter(mAdapter);
            if (mContext != null)
                recyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));

        }

        @Override
        protected void onPreExecute() {

        }
    }

    public class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        final static int FIRST_ITEM = 0;
        final static int ITEM = 1;
        private ArrayList<MusicInfo> mList;
        PlayMusic playMusic;
        Handler handler;

        public Adapter(ArrayList<MusicInfo> list) {
//            if (list == null) {
//                throw new IllegalArgumentException("model Data must not be null");
//            }
            handler = HandlerUtil.getInstance(mContext);
            mList = list;

        }

        //更新adpter的数据
        public void updateDataSet(ArrayList<MusicInfo> list) {
            this.mList = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            if (viewType == FIRST_ITEM)
                return new CommonItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.common_item, viewGroup, false));

            else {
                return new ListItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_musci_common_item, viewGroup, false));
            }
        }

        //判断布局类型
        @Override
        public int getItemViewType(int position) {
            return position == FIRST_ITEM ? FIRST_ITEM : ITEM;

        }

        //将数据与界面进行绑定
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MusicInfo model = null;
            if (position > 0) {
                model = mList.get(position - 1);
            }
            if (holder instanceof ListItemViewHolder) {

                ((ListItemViewHolder) holder).mainTitle.setText(model.musicName.toString());
                ((ListItemViewHolder) holder).title.setText(model.artist.toString());

                //判断该条目音乐是否在播放
                if (MusicPlayer.getCurrentAudioId() == model.songId) {
                    ((ListItemViewHolder) holder).playState.setVisibility(View.VISIBLE);
                    ((ListItemViewHolder) holder).playState.setImageResource(R.drawable.song_play_icon);
                    ((ListItemViewHolder) holder).playState.setImageTintList(R.color.theme_color_primary);
                } else {
                    ((ListItemViewHolder) holder).playState.setVisibility(View.GONE);
                }

            } else if (holder instanceof CommonItemViewHolder) {
                ((CommonItemViewHolder) holder).textView.setText("(共" + mList.size() + "首)");

                ((CommonItemViewHolder) holder).select.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, SelectActivity.class);
                        intent.putParcelableArrayListExtra("ids", mList);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        mContext.startActivity(intent);
                    }
                });

            }
        }

        @Override
        public int getItemCount() {
            return (null != mList ? mList.size() + 1 : 0);
        }


        public class CommonItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView textView;
            ImageView select;

            CommonItemViewHolder(View view) {
                super(view);
                this.textView = (TextView) view.findViewById(R.id.play_all_number);
                this.select = (ImageView) view.findViewById(R.id.select);
                view.setOnClickListener(this);
            }

            public void onClick(View v) {
                //// TODO: 2016/1/20
                if(playMusic != null)
                    handler.removeCallbacks(playMusic);
                if(getAdapterPosition() > -1){
                    playMusic = new PlayMusic(0);
                    handler.postDelayed(playMusic,70);
                }
//                HandlerUtil.getInstance(getContext()).postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        long[] list = new long[mList.size()];
//                        HashMap<Long, MusicInfo> infos = new HashMap();
//                        for (int i = 0; i < mList.size(); i++) {
//                            MusicInfo info = mList.get(i);
//                            list[i] = info.songId;
//                            info.islocal = true;
//                            info.albumData = MusicUtils.getAlbumArtUri(info.albumId) + "";
//                            infos.put(list[i], mList.get(i));
//                        }
//                        MusicPlayer.playAll(infos, list, 0, false);
//                    }
//                },70);

            }

        }


        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //ViewHolder
            ImageView moreOverflow;
            TextView mainTitle, title;
            TintImageView playState;


            ListItemViewHolder(View view) {
                super(view);
                this.mainTitle = (TextView) view.findViewById(R.id.viewpager_list_toptext);
                this.title = (TextView) view.findViewById(R.id.viewpager_list_bottom_text);
                this.playState = (TintImageView) view.findViewById(R.id.play_state);
                this.moreOverflow = (ImageView) view.findViewById(R.id.viewpager_list_button);


                moreOverflow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MoreFragment morefragment = MoreFragment.newInstance(mList.get(getAdapterPosition() - 1), IConstants.MUSICOVERFLOW);
                        morefragment.show(getFragmentManager(), "music");
                    }
                });
                view.setOnClickListener(this);

            }

            @Override
            public void onClick(View v) {
                if(playMusic != null)
                    handler.removeCallbacks(playMusic);
                if(getAdapterPosition() > -1){
                    playMusic = new PlayMusic(getAdapterPosition() - 1);
                    handler.postDelayed(playMusic,70);
                }
//                HandlerUtil.getInstance(getContext()).postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        long[] list = new long[mList.size()];
//                        HashMap<Long, MusicInfo> infos = new HashMap();
//                        for (int i = 0; i < mList.size(); i++) {
//                            MusicInfo info = mList.get(i);
//                            list[i] = info.songId;
//                            info.islocal = true;
//                            info.albumData = MusicUtils.getAlbumArtUri(info.albumId) + "";
//                            infos.put(list[i], mList.get(i));
//                        }
//                        if (getAdapterPosition() > 0)
//                            MusicPlayer.playAll(infos, list, getAdapterPosition() - 1, false);
//                    }
//                }, 60);
            }

        }

        class PlayMusic implements Runnable{
            int position;
            public PlayMusic(int position){
                this.position = position;
            }

            @Override
            public void run() {
                long[] list = new long[mList.size()];
                HashMap<Long, MusicInfo> infos = new HashMap();
                for (int i = 0; i < mList.size(); i++) {
                    MusicInfo info = mList.get(i);
                    list[i] = info.songId;
                    info.islocal = true;
                    info.albumData = MusicUtils.getAlbumArtUri(info.albumId) + "";
                    infos.put(list[i], mList.get(i));
                }
                if (position > -1)
                    MusicPlayer.playAll(infos, list, position, false);
            }
        }
    }


}