package com.wm.remusic.fragment;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.wm.remusic.R;
import com.wm.remusic.info.AlbumInfo;
import com.wm.remusic.service.MusicPlayer;
import com.wm.remusic.uitl.IConstants;
import com.wm.remusic.uitl.MusicUtils;
import com.wm.remusic.uitl.PreferencesUtility;
import com.wm.remusic.uitl.SortOrder;
import com.wm.remusic.widget.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wm on 2016/1/8.
 */
public class AlbumFragment extends BaseFragment {

    private LinearLayoutManager layoutManager;
    private List<AlbumInfo> mAlbumList = new ArrayList<>();
    private AlbumAdapter mAdapter;
    private RecyclerView recyclerView;
    private PreferencesUtility mPreferences;
    //private FastScroller fastScroller;
    private RecyclerView.ItemDecoration itemDecoration;


    public static final AlbumFragment newInstance(int title, String message) {
        AlbumFragment f = new AlbumFragment();
        Bundle bdl = new Bundle(2);
        bdl.putInt("EXTRA_TITLE", title);
        bdl.putString("EXTRA_MESSAGE", message);
        f.setArguments(bdl);
        return f;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferencesUtility.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recylerview, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new AlbumAdapter(null);
        recyclerView.setAdapter(mAdapter);
        setItemDecoration();
        reloadAdapter();

        //fastScroller = (FastScroller) view.findViewById(R.id.fastscroller);
        //new loadAlbums().execute("");

        return view;
    }


    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.album_sort_by, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort_by_az:
                mPreferences.setAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_A_Z);
                reloadAdapter();
                return true;
            case R.id.menu_sort_by_number_of_songs:
                mPreferences.setAlbumSortOrder(SortOrder.AlbumSortOrder.ALBUM_NUMBER_OF_SONGS);
                reloadAdapter();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //设置分割线
    private void setItemDecoration() {
        itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
    }

    //更新adapter界面
    public void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                List<AlbumInfo> albumList = MusicUtils.queryAlbums(getContext());
                mAdapter.updateDataSet(albumList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    //异步加载recyclerview界面
    private class loadAlbums extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (getActivity() != null)
                mAdapter = new AlbumAdapter(mAlbumList);
            mAlbumList = MusicUtils.queryAlbums(getContext());
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            recyclerView.setAdapter(mAdapter);
            if (getActivity() != null) {
                setItemDecoration();
            }
        }

        @Override
        protected void onPreExecute() {
        }
    }

    public class AlbumAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        final static int FIRST_ITEM = 0;
        final static int ITEM = 1;
        private List<AlbumInfo> mList;

        public AlbumAdapter(List<AlbumInfo> list) {
            //mactivity = activity;
//            if (list == null) {
//                throw new IllegalArgumentException("model Data must not be null");
//            }
            mList = list;
        }

        //更新adpter的数据
        public void updateDataSet(List<AlbumInfo> list) {
            this.mList = list;
        }


        //创建新View，被LayoutManager调用
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            return new ListItemViewHolder(LayoutInflater.from(viewGroup.getContext()).
                    inflate(R.layout.recyclerview_common_item, viewGroup, false));
        }


        //判断布局类型
        @Override
        public int getItemViewType(int position) {
            return position == FIRST_ITEM ? FIRST_ITEM : ITEM;

        }

        //将数据与界面进行绑定
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            AlbumInfo model = mList.get(position);
            ((ListItemViewHolder) holder).title.setText(model.album_name.toString());
            ((ListItemViewHolder) holder).title2.setText(model.number_of_songs + "首" + model.album_artist);
            ((ListItemViewHolder) holder).draweeView.setImageURI(Uri.parse(model.album_art + ""));//要加“” 弹出println needs a message
            //根据播放中歌曲的专辑名判断当前专辑条目是否有播放的歌曲
            if (MusicPlayer.getArtistName() != null && MusicPlayer.getAlbumName().equals(model.album_name)) {
                ((ListItemViewHolder) holder).moreOverflow.setImageResource(R.drawable.song_play_icon);
            } else {
                ((ListItemViewHolder) holder).moreOverflow.setImageResource(R.drawable.abc_ic_menu_moreoverflow_mtrl_alpha);
            }

        }

        //条目数量
        @Override
        public int getItemCount() {
            return mList == null ? 0 : mList.size();
        }

        //ViewHolder
        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView moreOverflow;
            SimpleDraweeView draweeView;
            TextView title, title2;

            ListItemViewHolder(View view) {
                super(view);
                this.title = (TextView) view.findViewById(R.id.viewpager_list_toptext);
                this.title2 = (TextView) view.findViewById(R.id.viewpager_list_bottom_text);
                this.draweeView = (SimpleDraweeView) view.findViewById(R.id.viewpager_list_img);
                this.moreOverflow = (ImageView) view.findViewById(R.id.viewpager_list_button);
                moreOverflow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MoreFragment morefragment = MoreFragment.newInstance(mList.get(getAdapterPosition()).album_id + "", IConstants.ALBUMOVERFLOW);
                        morefragment.show(getFragmentManager(), "album");
                    }
                });
                view.setOnClickListener(this);

            }

            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = ((AppCompatActivity) getContext()).getSupportFragmentManager().beginTransaction();
                AlbumDetailFragment fragment = AlbumDetailFragment.newInstance(mList.get(getAdapterPosition()).album_id, false, null);
                transaction.hide(((AppCompatActivity) getContext()).getSupportFragmentManager().findFragmentById(R.id.tab_container));
                transaction.add(R.id.tab_container, fragment);
                transaction.addToBackStack(null).commit();
            }

        }
    }


    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mAlbumList.size();
        }

        @Override
        public AlbumInfo getItem(int position) {
            return mAlbumList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            AlbumInfo album = getItem(position);

            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = getActivity().getLayoutInflater().inflate(
                        R.layout.recyclerview_common_item, null);
                viewHolder.albumNameTv = (TextView) convertView
                        .findViewById(R.id.viewpager_list_toptext);
                viewHolder.numberTv = (TextView) convertView
                        .findViewById(R.id.viewpager_list_bottom_text);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.albumNameTv.setText(album.album_name);
            viewHolder.numberTv.setText(album.number_of_songs + "首歌");

            return convertView;
        }

        private class ViewHolder {
            TextView albumNameTv, numberTv;
        }

    }
}
