package com.wm.remusic.activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.wm.remusic.R;
import com.wm.remusic.adapter.PlaylistDetailAdapter;
import com.wm.remusic.info.MusicInfo;
import com.wm.remusic.provider.PlaylistsManager;
import com.wm.remusic.service.MusicTrack;
import com.wm.remusic.uitl.CommonUtils;
import com.wm.remusic.uitl.ImageUtils;
import com.wm.remusic.uitl.MusicUtils;
import com.wm.remusic.widget.DividerItemDecoration;

import java.util.ArrayList;

/**
 * Created by wm on 2016/4/11.
 */
public class PlaylistDetailActivity extends BaseActivity {

    private long playlsitId = -1;
    private String albumPath, playlistname;

    private PlaylistsManager playlistsManager;

    private SimpleDraweeView albumArtSmall;
    private ImageView albumArt;
    private TextView albumTitle, albumDetails;

    private RecyclerView recyclerView;
    private PlaylistDetailAdapter mAdapter;

    private Toolbar toolbar;

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        playlistsManager = PlaylistsManager.getInstance(this);
        super.onCreate(savedInstanceState);
        if (getIntent().getExtras() != null) {
            playlsitId = getIntent().getLongExtra("playlistid", -1);
            albumPath = getIntent().getStringExtra("albumart");
            playlistname = getIntent().getStringExtra("playlistname");
        }

        setContentView(R.layout.fragment_playlist_detail);

        albumArt = (ImageView) findViewById(R.id.album_art);
        albumTitle = (TextView) findViewById(R.id.album_title);
        albumDetails = (TextView) findViewById(R.id.album_details);
        albumArtSmall = (SimpleDraweeView) findViewById(R.id.albumArtSmall);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setPadding(0, CommonUtils.getStatusHeight(this) / 2, 0, 0);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        //recyclerView.setEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setUpEverything();

//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                getWindow().getDecorView().setFitsSystemWindows(true);
//            }
//        });

    }


    private void setUpEverything() {
        setupToolbar();
        loadAllLists();
        setAlbumart();
    }

    private void setupToolbar() {

        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.actionbar_back);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("歌单");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //collapsingToolbarLayout.setTitle("歌单");

    }

    private void loadAllLists() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                ArrayList<MusicTrack> musicInfos = playlistsManager.getPlaylist(playlsitId);
                long[] ids = new long[musicInfos.size()];
                for (int i = 0; i < musicInfos.size(); i++) {
                    ids[i] = musicInfos.get(i).mId;
                }
                ArrayList<MusicInfo> mList = MusicUtils.getMusicLists(PlaylistDetailActivity.this, ids);
                mAdapter = new PlaylistDetailAdapter(PlaylistDetailActivity.this, playlsitId, mList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                recyclerView.setAdapter(mAdapter);
                recyclerView.addItemDecoration(new DividerItemDecoration(PlaylistDetailActivity.this, DividerItemDecoration.VERTICAL_LIST));
            }
        }.execute();
    }

    //更新adapter界面
    public void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {

                ArrayList<MusicTrack> musicInfos = playlistsManager.getPlaylist(playlsitId);
                long[] ids = new long[musicInfos.size()];
                for (int i = 0; i < musicInfos.size(); i++) {
                    ids[i] = musicInfos.get(i).mId;
                }
                ArrayList<MusicInfo> mList = MusicUtils.getMusicLists(PlaylistDetailActivity.this, ids);
                mAdapter.updateDataSet(playlsitId, mList);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        toolbar.setBackgroundColor(Color.TRANSPARENT);
        reloadAdapter();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void setAlbumart() {
        albumTitle.setText(playlistname);
        albumArtSmall.setImageURI(Uri.parse(albumPath));
        final Drawable drawable;
        try {
            drawable = Drawable.createFromStream(PlaylistDetailActivity.this.getContentResolver().openInputStream(Uri.parse(albumPath)), null);

            new setBlurredAlbumArt().execute(ImageUtils.getBitmapFromDrawable(drawable));

        } catch (Exception e) {

        }

    }


    private class setBlurredAlbumArt extends AsyncTask<Bitmap, Void, Drawable> {

        @Override
        protected Drawable doInBackground(Bitmap... loadedImage) {
            Drawable drawable = null;

            try {
                drawable = ImageUtils.createBlurredImageFromBitmap(loadedImage[0], PlaylistDetailActivity.this, 20);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return drawable;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result != null) {
                if (albumArt.getDrawable() != null) {
                    final TransitionDrawable td =
                            new TransitionDrawable(new Drawable[]{
                                    albumArt.getDrawable(),
                                    result
                            });
                    albumArt.setImageDrawable(td);
                    td.startTransition(200);

                } else {
                    albumArt.setImageDrawable(result);
                }
            }
        }

        @Override
        protected void onPreExecute() {
        }
    }


}
