package com.wm.remusic.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wm.remusic.R;
import com.wm.remusic.fragment.MoreFragment;
import com.wm.remusic.handler.HandlerUtil;
import com.wm.remusic.info.MusicInfo;
import com.wm.remusic.provider.RecentStore;
import com.wm.remusic.recent.Song;
import com.wm.remusic.recent.SongLoader;
import com.wm.remusic.recent.TopTracksLoader;
import com.wm.remusic.service.MediaService;
import com.wm.remusic.service.MusicPlayer;
import com.wm.remusic.uitl.CommonUtils;
import com.wm.remusic.uitl.IConstants;
import com.wm.remusic.uitl.L;
import com.wm.remusic.uitl.MusicUtils;
import com.wm.remusic.widget.DividerItemDecoration;

import java.util.HashMap;
import java.util.List;

/**
 * Created by wm on 2016/5/11.
 */
public class RecentActivity extends BaseActivity {


    private int currentlyPlayingPosition = 0;
    private Adapter mAdapter;
    private RecentStore recentStore;
    private Toolbar toolbar;
    private List<Song> mList;
    private RecyclerView recyclerView;
    private String TAG = "RecentActivity";
    private boolean d = true;
    private LinearLayoutManager layoutManager;
    //接受歌曲播放变化和列表变化广播，刷新列表

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_recent);
        // initQuickControls();
        recentStore = RecentStore.getInstance(this);

        TopTracksLoader recentloader = new TopTracksLoader(this, TopTracksLoader.QueryType.RecentSongs);
        List<Song> recentsongs = SongLoader.getSongsForCursor(TopTracksLoader.getCursor());
        int songCountInt = recentsongs.size();
        mList = recentsongs;

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setPadding(0, CommonUtils.getStatusHeight(RecentActivity.this), 0, 0);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.actionbar_back);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("最近播放");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        new loadSongs().execute("");
    }


    //刷新列表
    public void updateTrack() {
        if(mAdapter != null){
            mAdapter.updateDataSet(mList);
        }

    }

    //异步加载recyclerview界面
    private class loadSongs extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (RecentActivity.this != null)
                mAdapter = new Adapter(mList);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            recyclerView.setAdapter(mAdapter);
            if (RecentActivity.this != null)
                recyclerView.addItemDecoration(new DividerItemDecoration(RecentActivity.this, DividerItemDecoration.VERTICAL_LIST));

        }

        @Override
        protected void onPreExecute() {

        }
    }


    public class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        final static int FIRST_ITEM = 0;
        final static int ITEM = 1;
        private List<Song> mList;

        public Adapter(List<Song> list) {
            if (list == null) {
                throw new IllegalArgumentException("model Data must not be null");
            }
            mList = list;
        }

        //更新adpter的数据
        public void updateDataSet(List<Song> list) {
            this.mList = list;
            notifyDataSetChanged();
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
            Song model = null;
            if (position > 0) {
                model = mList.get(position - 1);
            }
            if (holder instanceof ListItemViewHolder) {

                ((ListItemViewHolder) holder).mainTitle.setText(model.title.toString());
                ((ListItemViewHolder) holder).title.setText(model.artistName.toString());

                //判断该条目音乐是否在播放
                if (MusicPlayer.getCurrentAudioId() == model.id) {
                    ((ListItemViewHolder) holder).playState.setVisibility(View.VISIBLE);
                    ((ListItemViewHolder) holder).playState.setImageResource(R.drawable.song_play_icon);
                    currentlyPlayingPosition = position;
                } else {
                    ((ListItemViewHolder) holder).playState.setVisibility(View.GONE);
                }

            } else if (holder instanceof CommonItemViewHolder) {
                ((CommonItemViewHolder) holder).textView.setText("(共" + mList.size() + "首)");
                ((CommonItemViewHolder) holder).select.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        Intent intent = new Intent(getActivity(), SelectActivity.class);
//                        intent.putParcelableArrayListExtra("ids", (ArrayList) mList);
//                        getActivity().startActivity(intent);
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
                HandlerUtil.getInstance(RecentActivity.this).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        long[] list = new long[mList.size()];
                        HashMap<Long, MusicInfo> infos = new HashMap();
                        for (int i = 0; i < mList.size(); i++) {
                            MusicInfo info = MusicUtils.getMusicInfo(RecentActivity.this, mList.get(i).id);
                            list[i] = info.songId;
                            info.islocal = true;
                            info.albumData = MusicUtils.getAlbumArtUri(info.albumId) + "";
                            infos.put(list[i], info);
                        }
                        MusicPlayer.playAll(infos, list, 0, false);
                    }
                },70);

            }

        }


        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //ViewHolder
            ImageView moreOverflow, playState;
            TextView mainTitle, title;

            ListItemViewHolder(View view) {
                super(view);
                this.mainTitle = (TextView) view.findViewById(R.id.viewpager_list_toptext);
                this.title = (TextView) view.findViewById(R.id.viewpager_list_bottom_text);
                this.playState = (ImageView) view.findViewById(R.id.play_state);
                this.moreOverflow = (ImageView) view.findViewById(R.id.viewpager_list_button);
                moreOverflow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MoreFragment morefragment = MoreFragment.newInstance(mList.get(getAdapterPosition() - 1).id + "", IConstants.MUSICOVERFLOW);
                        morefragment.show(getSupportFragmentManager(), "music");
                    }
                });
                view.setOnClickListener(this);

            }

            @Override
            public void onClick(View v) {
                HandlerUtil.getInstance(RecentActivity.this).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        long[] list = new long[mList.size()];
                        HashMap<Long, MusicInfo> infos = new HashMap();
                        for (int i = 0; i < mList.size(); i++) {
                            MusicInfo info = MusicUtils.getMusicInfo(RecentActivity.this, mList.get(i).id);
                            list[i] = info.songId;
                            info.islocal = true;
                            info.albumData = MusicUtils.getAlbumArtUri(info.albumId) + "";
                            infos.put(list[i], info);
                        }
                        if (getAdapterPosition() > 0)
                            MusicPlayer.playAll(infos, list, getAdapterPosition() - 1, false);
                    }
                }, 70);

            }
        }
    }
}
