package com.wm.remusic.fragment;


import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
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

import com.facebook.drawee.view.SimpleDraweeView;
import com.wm.remusic.R;
import com.wm.remusic.adapter.PlaylistDetailAdapter;
import com.wm.remusic.info.MusicInfo;
import com.wm.remusic.provider.PlaylistsManager;
import com.wm.remusic.service.MediaService;
import com.wm.remusic.service.MusicTrack;
import com.wm.remusic.uitl.CommonUtils;
import com.wm.remusic.uitl.IConstants;
import com.wm.remusic.uitl.ImageUtils;
import com.wm.remusic.uitl.MusicUtils;
import com.wm.remusic.widget.DividerItemDecoration;

import java.util.ArrayList;

/**
 * Created by wm on 2016/3/8.
 */
public class PlaylistDetailFragment extends Fragment {

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
    private Context context;
    //接受广播
    private BroadcastReceiver mStatusListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action.equals(IConstants.PLAYLIST_ITEM_MOVED)) {
                reloadAdapter();

            } else if (action.equals(IConstants.MUSIC_COUNT_CHANGED)) {
                refreshPlaylist();
                reloadAdapter();
            }
        }
    };

    public static PlaylistDetailFragment newInstance(long id, String albumArt, String name) {
        PlaylistDetailFragment fragment = new PlaylistDetailFragment();
        Bundle args = new Bundle();
        args.putLong("playlistid", id);
        args.putString("albumart", albumArt);
        args.putString("playlistname", name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ViewGroup) getView().getParent()).setFitsSystemWindows(true);
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            playlsitId = getArguments().getLong("playlistid");
            albumPath = getArguments().getString("albumart");
            playlistname = getArguments().getString("playlistname");
        }
        context = getActivity();
        playlistsManager = PlaylistsManager.getInstance(context);
    }

    @TargetApi(21)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(
                R.layout.fragment_playlist_detail, container, false);


        albumArt = (ImageView) rootView.findViewById(R.id.album_art);
        albumTitle = (TextView) rootView.findViewById(R.id.album_title);
        albumDetails = (TextView) rootView.findViewById(R.id.album_details);
        albumArtSmall = (SimpleDraweeView) rootView.findViewById(R.id.albumArtSmall);

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setPadding(0, CommonUtils.getStatusHeight(getActivity()) / 2, 0, 0);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        collapsingToolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);
        appBarLayout = (AppBarLayout) rootView.findViewById(R.id.app_bar);
        //recyclerView.setEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        setUpEverything();

        return rootView;
    }

    private void setUpEverything() {
        setupToolbar();
        loadAllLists();
        setAlbumart();
    }

    private void setupToolbar() {

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.actionbar_back);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("歌单");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
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
                ArrayList<MusicInfo> mList = MusicUtils.getMusicLists(getContext(), ids);
                mAdapter = new PlaylistDetailAdapter(getActivity(), playlsitId, mList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                recyclerView.setAdapter(mAdapter);
                recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
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
                ArrayList<MusicInfo> mList = MusicUtils.getMusicLists(getContext(), ids);
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

        IntentFilter f = new IntentFilter();
        f.addAction(IConstants.MUSIC_COUNT_CHANGED);
        f.addAction(IConstants.PLAYLIST_ITEM_MOVED);
        f.addAction(MediaService.META_CHANGED);
        getActivity().registerReceiver(mStatusListener, f);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mStatusListener);
    }

    private void refreshPlaylist() {

    }


    private void setAlbumart() {
        albumTitle.setText(playlistname);
        albumArtSmall.setImageURI(Uri.parse(albumPath));
        final Drawable drawable;
        try {
            drawable = Drawable.createFromStream(getContext().getContentResolver().openInputStream(Uri.parse(albumPath)), null);

            new setBlurredAlbumArt().execute(ImageUtils.getBitmapFromDrawable(drawable));

        } catch (Exception e) {

        }

    }


    private class setBlurredAlbumArt extends AsyncTask<Bitmap, Void, Drawable> {

        @Override
        protected Drawable doInBackground(Bitmap... loadedImage) {
            Drawable drawable = null;

            try {
                drawable = ImageUtils.createBlurredImageFromBitmap(loadedImage[0], getContext(), 20);
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
