package com.wm.remusic.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bilibili.magicasakura.widgets.TintImageView;
import com.wm.remusic.R;
import com.wm.remusic.activity.SelectActivity;
import com.wm.remusic.handler.HandlerUtil;
import com.wm.remusic.info.AlbumInfo;
import com.wm.remusic.info.MusicInfo;
import com.wm.remusic.service.MusicPlayer;
import com.wm.remusic.uitl.CommonUtils;
import com.wm.remusic.uitl.IConstants;
import com.wm.remusic.uitl.MusicUtils;
import com.wm.remusic.widget.DividerItemDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by wm on 2016/1/9.
 */
public class AlbumDetailFragment extends BaseFragment {

    private int currentlyPlayingPosition = 0;
    private ActionBar ab;
    private AlbumDetailAdapter mAdapter;
    private List<MusicInfo> musicInfos = new ArrayList<>();
    private long albumID = -1;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private RecyclerView.ItemDecoration itemDecoration;

    public static AlbumDetailFragment newInstance(long id, boolean useTransition, String transitionName) {
        AlbumDetailFragment fragment = new AlbumDetailFragment();
        Bundle args = new Bundle();
        args.putLong("album_id", id);
        args.putBoolean("transition", useTransition);
        if (useTransition)
            args.putString("transition_name", transitionName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            albumID = getArguments().getLong("album_id");
        }

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_common, container, false);


        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new AlbumDetailAdapter(null);
        recyclerView.setAdapter(mAdapter);
        itemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setHasFixedSize(true);
        reloadAdapter();

        AlbumInfo albumInfo = MusicUtils.getAlbumInfo(mContext, albumID);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setPadding(0, CommonUtils.getStatusHeight(mContext), 0, 0);
        ((AppCompatActivity) mContext).setSupportActionBar(toolbar);
        ab = ((AppCompatActivity) mContext).getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.actionbar_back);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(albumInfo.album_name);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity() != null)
                getActivity().onBackPressed();
            }
        });

        return view;

    }

    @Override
    public void onPause() {
        super.onPause();
    }


    //更新adapter界面
    public void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                List<MusicInfo> albumList = MusicUtils.queryMusic(mContext, albumID + "", IConstants.START_FROM_ALBUM);
                mAdapter.updateDataSet((ArrayList) albumList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    class AlbumDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        final static int FIRST_ITEM = 0;
        final static int ITEM = 1;
        ArrayList<MusicInfo> mList;

        public AlbumDetailAdapter(ArrayList<MusicInfo> musicInfos) {
            mList = musicInfos;
            //list.add(0,null);
        }

        //更新adpter的数据
        public void updateDataSet(ArrayList<MusicInfo> list) {
            this.mList = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            if (viewType == FIRST_ITEM) {
                return new CommonItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.common_item, viewGroup, false));
            } else {
                return new ListItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_musci_common_item, viewGroup, false));
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position == FIRST_ITEM ? FIRST_ITEM : ITEM;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            if (holder instanceof CommonItemViewHolder) {
                ((CommonItemViewHolder) holder).textView.setText("共" + mList.size() + "首");
                ((CommonItemViewHolder) holder).select.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, SelectActivity.class);
                        intent.putParcelableArrayListExtra("ids", mList);
                        mContext.startActivity(intent);
                    }
                });
            }
            if (holder instanceof ListItemViewHolder) {
                MusicInfo musicInfo = mList.get(position - 1);
                ((ListItemViewHolder) holder).mainTitle.setText(musicInfo.musicName);
                ((ListItemViewHolder) holder).title.setText(musicInfo.artist);
                //判断该条目音乐是否在播放
                if (MusicPlayer.getCurrentAudioId() == musicInfo.songId) {
                    ((ListItemViewHolder) holder).playState.setVisibility(View.VISIBLE);
                    ((ListItemViewHolder) holder).playState.setImageResource(R.drawable.song_play_icon);
                    ((ListItemViewHolder) holder).playState.setImageTintList(R.color.theme_color_primary);
                } else {
                    ((ListItemViewHolder) holder).playState.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return (null != mList ? mList.size() + 1 : 0);
        }


        class CommonItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView textView;
            ImageView select;

            CommonItemViewHolder(View view) {
                super(view);
                this.textView = (TextView) view.findViewById(R.id.play_all_number);
                this.select = (ImageView) view.findViewById(R.id.select);
                view.setOnClickListener(this);
            }

            //播放专辑
            @Override
            public void onClick(View v) {
                HandlerUtil.getInstance(mContext).postDelayed(new Runnable() {
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
                        MusicPlayer.playAll(infos, list, 0, false);
                    }
                },70);
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
                view.setOnClickListener(this);
                //设置弹出菜单
                moreOverflow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MoreFragment moreFragment = new MoreFragment().newInstance(mList.get(getAdapterPosition() - 1), IConstants.MUSICOVERFLOW);
                        moreFragment.show(getFragmentManager(), "music");
                    }
                });

            }

            //播放歌曲
            @Override
            public void onClick(View v) {
                HandlerUtil.getInstance(mContext).postDelayed(new Runnable() {
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
                        if (getAdapterPosition() > 0)
                            MusicPlayer.playAll(infos, list, getAdapterPosition() - 1, false);
                    }
                }, 60);
            }

        }


    }


}
