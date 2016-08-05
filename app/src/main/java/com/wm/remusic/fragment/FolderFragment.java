package com.wm.remusic.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.wm.remusic.R;
import com.wm.remusic.info.FolderInfo;
import com.wm.remusic.service.MediaService;
import com.wm.remusic.service.MusicPlayer;
import com.wm.remusic.uitl.IConstants;
import com.wm.remusic.uitl.MusicUtils;
import com.wm.remusic.uitl.PreferencesUtility;
import com.wm.remusic.widget.DividerItemDecoration;

import java.io.File;
import java.util.List;

/**
 * Created by wm on 2016/1/18.
 */
public class FolderFragment extends BaseFragment {
    private List<FolderInfo> folderInfos;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private RecyclerView.ItemDecoration itemDecoration;
    private Adapter mAdapter;
    private PreferencesUtility mPreferences;
//    //接受广播
//    private BroadcastReceiver mStatusListener = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (action.equals(MediaService.META_CHANGED)) {
//                reloadAdapter();
//            }
//        }
//    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferencesUtility.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recylerview, container, false);

        folderInfos = MusicUtils.queryFolder(getActivity());
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        //recyclerView.setAdapter(new Adapter(folderInfos));
        //fastScroller = (FastScroller) view.findViewById(R.id.fastscroller);
        //itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        //recyclerView.addItemDecoration(itemDecoration);
        // new loadFolders().execute("");

        mAdapter = new Adapter(null);
        recyclerView.setAdapter(mAdapter);
        setItemDecoration();
        reloadAdapter();


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter f = new IntentFilter();
        f.addAction(MediaService.META_CHANGED);
       // getActivity().registerReceiver(mStatusListener, f);
    }

    @Override
    public void onPause() {
        super.onPause();
      //  getActivity().unregisterReceiver(mStatusListener);
    }

    //设置分割线
    private void setItemDecoration() {
        itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
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

                //reloadAdapter();
                return true;
            case R.id.menu_sort_by_number_of_songs:

                // reloadAdapter();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //更新adapter界面
    public void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                List<FolderInfo> folderList = MusicUtils.queryFolder(getContext());
                mAdapter.updateDataSet(folderList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    //异步加载recyclerview界面
    private class loadFolders extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (getActivity() != null)
                mAdapter = new Adapter(folderInfos);
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

    public class Adapter extends RecyclerView.Adapter<Adapter.ListItemViewHolder> {

        private List<FolderInfo> mList;


        public Adapter(List<FolderInfo> list) {
//            if (list == null) {
//                throw new IllegalArgumentException("model Data must not be null");
//            }
            mList = list;
        }

        //更新adpter的数据
        public void updateDataSet(List<FolderInfo> list) {
            this.mList = list;
        }

        @Override
        public ListItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_common_item, viewGroup, false);
            return new ListItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ListItemViewHolder holder, int i) {
            FolderInfo model = mList.get(i);
            holder.Maintitle.setText(model.folder_name);
            holder.title.setText(model.folder_path);
            holder.imageView.setImageResource(R.drawable.list_icn_folder);
            //根据播放中歌曲的专辑名判断当前专辑条目是否有播放的歌曲
            String folder_path = null;
            if (MusicPlayer.getPath() != null && MusicPlayer.getTrackName() != null) {
                folder_path = MusicPlayer.getPath().substring(0, MusicPlayer.getPath().lastIndexOf(File.separator));
            }
            if (folder_path != null && folder_path.equals(model.folder_path)) {
                holder.moreOverflow.setImageResource(R.drawable.song_play_icon);
            } else {
                holder.moreOverflow.setImageResource(R.drawable.abc_ic_menu_moreoverflow_mtrl_alpha);
            }

        }

        @Override
        public int getItemCount() {
            return mList == null ? 0 : mList.size();
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //ViewHolder
            ImageView imageView, moreOverflow;
            TextView Maintitle, title;

            ListItemViewHolder(View view) {
                super(view);
                this.Maintitle = (TextView) view.findViewById(R.id.viewpager_list_toptext);
                this.title = (TextView) view.findViewById(R.id.viewpager_list_bottom_text);
                this.moreOverflow = (ImageView) view.findViewById(R.id.viewpager_list_button);
                this.imageView = (ImageView) view.findViewById(R.id.viewpager_list_img);
                moreOverflow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MoreFragment morefragment = MoreFragment.newInstance(mList.get(getAdapterPosition()).folder_name, IConstants.FOLDEROVERFLOW);
                        morefragment.show(getFragmentManager(), "music");
                    }
                });
                view.setOnClickListener(this);

            }

            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = ((AppCompatActivity) getContext()).getSupportFragmentManager().beginTransaction();
                FolderDetailFragment fragment = FolderDetailFragment.newInstance(mList.get(getAdapterPosition()).folder_path, false, null);
                transaction.hide(((AppCompatActivity) getContext()).getSupportFragmentManager().findFragmentById(R.id.tab_container));
                transaction.add(R.id.tab_container, fragment);
                transaction.addToBackStack(null).commit();
            }

        }
    }
}
