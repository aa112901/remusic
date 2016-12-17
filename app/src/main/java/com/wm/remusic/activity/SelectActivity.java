package com.wm.remusic.activity;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wm.remusic.R;
import com.wm.remusic.dialog.AddPlaylistDialog;
import com.wm.remusic.info.MusicInfo;
import com.wm.remusic.provider.PlaylistsManager;
import com.wm.remusic.service.MediaService;
import com.wm.remusic.service.MusicPlayer;
import com.wm.remusic.uitl.IConstants;
import com.wm.remusic.uitl.MusicUtils;
import com.wm.remusic.widget.DividerItemDecoration;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by wm on 2016/3/9.
 */
public class SelectActivity extends AppCompatActivity implements View.OnClickListener {

    private ArrayList<MusicInfo> mList;
    private SelectAdapter mAdapter;
    private ActionBar ab;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private Toolbar toolbar;
    private LinearLayout nextPlay, addtoPlaylist, delete;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.actionbar_back);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("已选择0项");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        nextPlay = (LinearLayout) findViewById(R.id.select_next);
        addtoPlaylist = (LinearLayout) findViewById(R.id.select_addtoplaylist);
        delete = (LinearLayout) findViewById(R.id.select_del);
        nextPlay.setOnClickListener(this);
        addtoPlaylist.setOnClickListener(this);
        delete.setOnClickListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);


        new loadSongs().execute("");

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.down, menu);
        MenuItem down = menu.findItem(R.id.down);
        down.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(SelectActivity.this, DownActivity.class);
                startActivity(intent);
                return false;
            }
        });


        return true;

    }


    @Override
    public void onClick(View v) {
        final ArrayList<MusicInfo> selectList = mAdapter.getSelectedItem();
        switch (v.getId()) {

            case R.id.select_next:

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        ArrayList<MusicInfo> select = mAdapter.getSelectedItem();
                        long currentAudioId = MusicPlayer.getCurrentAudioId();

                        for (int i = 0; i < select.size(); i++) {
                            if (select.get(i).songId == currentAudioId) {
                                select.remove(i);
                                break;
                            }
                        }

                        final long[] list = new long[select.size()];
                        HashMap<Long, MusicInfo> infos = new HashMap();
                        for (int i = 0; i < select.size(); i++) {
                            list[i] = select.get(i).songId;
                            MusicInfo info = select.get(i);
                            list[i] = info.songId;
                            info.islocal = true;
                            info.albumData = MusicUtils.getAlbumArtUri(info.albumId) + "";
                            infos.put(list[i], select.get(i));
                        }

                        MusicPlayer.playNext(SelectActivity.this, infos, list);

                    }
                }, 100);

                break;
            case R.id.select_addtoplaylist:
                long[] list1 = new long[selectList.size()];
                for (int i = 0; i < mAdapter.getSelectedItem().size(); i++) {
                    list1[i] = selectList.get(i).songId;
                }
                AddPlaylistDialog.newInstance(list1).show(getSupportFragmentManager(), "add");
                Intent intent = new Intent(MediaService.PLAYLIST_CHANGED);
                sendBroadcast(intent);

                break;
            case R.id.select_del:
                new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.sure_to_delete_music)).
                        setPositiveButton(getResources().getString(R.string.sure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new AsyncTask<Void, Void, Void>() {

                                    @Override
                                    protected Void doInBackground(Void... params) {
                                        for (MusicInfo music : selectList) {

                                            if (MusicPlayer.getCurrentAudioId() == music.songId) {
                                                if (MusicPlayer.getQueueSize() == 0) {
                                                    MusicPlayer.stop();
                                                } else {
                                                    MusicPlayer.next();
                                                }

                                            }
                                            Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, music.songId);
                                            SelectActivity.this.getContentResolver().delete(uri, null, null);
                                            PlaylistsManager.getInstance(SelectActivity.this).deleteMusic(SelectActivity.this,
                                                    music.songId);
                                        }

                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(Void v) {
                                        mAdapter.updateDataSet();
                                        mAdapter.notifyDataSetChanged();
                                        SelectActivity.this.sendBroadcast(new Intent(IConstants.MUSIC_COUNT_CHANGED));
                                    }

                                }.execute();
                                dialog.dismiss();
                            }
                        }).
                        setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();

                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        finish();
    }

    //异步加载recyclerview界面
    private class loadSongs extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (getIntent().getParcelableArrayListExtra("ids") != null) {
                mList = getIntent().getParcelableArrayListExtra("ids");
            }
            if (mList != null)
                mAdapter = new SelectAdapter(mList);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            recyclerView.setAdapter(mAdapter);
            recyclerView.addItemDecoration(new DividerItemDecoration(SelectActivity.this, DividerItemDecoration.VERTICAL_LIST));

        }

        @Override
        protected void onPreExecute() {

        }
    }

    public class SelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        ArrayList selected;
        private ArrayList<MusicInfo> mList;
        private SparseBooleanArray mSelectedPositions = new SparseBooleanArray();
        private boolean mIsSelectable = false;


        public SelectAdapter(ArrayList<MusicInfo> list) {
            if (list == null) {
                throw new IllegalArgumentException("model Data must not be null");
            }
            mList = list;
        }

        public ArrayList<MusicInfo> getSelectedItem() {


            ArrayList<MusicInfo> selectList = new ArrayList<>();
            for (int i = 0; i < mList.size(); i++) {
                if (isItemChecked(i)) {
                    selectList.add(mList.get(i));
                }
            }
            return selectList;
        }

        //更新adpter的数据
        public void updateDataSet() {
            ab.setTitle("已选择0项");
            mList.removeAll(getSelectedItem());
            mSelectedPositions.clear();
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.select_item, viewGroup, false);
            return new ListItemViewHolder(itemView);
        }

        private void setItemChecked(int position, boolean isChecked) {
            mSelectedPositions.put(position, isChecked);
        }

        private boolean isItemChecked(int position) {
            return mSelectedPositions.get(position);
        }

        private boolean isSelectable() {
            return mIsSelectable;
        }

        private void setSelectable(boolean selectable) {
            mIsSelectable = selectable;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int i) {
            MusicInfo model = mList.get(i);
            //设置条目状态
            ((ListItemViewHolder) holder).mainTitle.setText(model.musicName);
            ((ListItemViewHolder) holder).title.setText(model.artist);
            ((ListItemViewHolder) holder).checkBox.setChecked(isItemChecked(i));
            ((ListItemViewHolder) holder).checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isItemChecked(i)) {
                        setItemChecked(i, false);
                    } else {
                        setItemChecked(i, true);
                    }
                    ab.setTitle("已选择" + getSelectedItem().size() + "项");
                }
            });
            ((ListItemViewHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isItemChecked(i)) {
                        setItemChecked(i, false);
                    } else {
                        setItemChecked(i, true);
                    }
                    notifyItemChanged(i);
                    ab.setTitle("已选择" + getSelectedItem().size() + "项");
                }
            });


        }

        @Override
        public int getItemCount() {
            return mList == null ? 0 : mList.size();
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder {
            //ViewHolder
            CheckBox checkBox;
            TextView mainTitle, title;

            ListItemViewHolder(View view) {
                super(view);
                this.mainTitle = (TextView) view.findViewById(R.id.select_title_main);
                this.title = (TextView) view.findViewById(R.id.select_title_small);
                this.checkBox = (CheckBox) view.findViewById(R.id.select_checkbox);

            }

        }
    }


}
