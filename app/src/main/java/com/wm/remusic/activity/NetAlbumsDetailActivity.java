package com.wm.remusic.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wm.remusic.MainApplication;
import com.wm.remusic.R;
import com.wm.remusic.downmusic.Down;
import com.wm.remusic.info.MusicInfo;
import com.wm.remusic.net.BMA;
import com.wm.remusic.net.HttpUtil;
import com.wm.remusic.service.MusicPlayer;
import com.wm.remusic.uitl.ImageUtils;
import com.wm.remusic.widget.DividerItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by wm on 2016/4/11.
 */
public class NetAlbumsDetailActivity extends BaseActivity {
    String albumId;
    private String albumPath, albumName, artistName, albumCount;
    private int publishTime;
    private ArrayList<MusicInfo> list = new ArrayList<>();


    private SimpleDraweeView albumArtSmall;
    private ImageView albumArt;
    private TextView albumTitle, albumDetails;

    private RecyclerView recyclerView;
    private PlaylistDetailAdapter mAdapter;

    private Toolbar toolbar;

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;
    MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getExtras() != null) {
            albumId = getIntent().getStringExtra("albumid");
            albumPath = getIntent().getStringExtra("albumart");
            albumName = getIntent().getStringExtra("albumname");
            artistName = getIntent().getStringExtra("artistname");
            publishTime = getIntent().getIntExtra("publishtime", -1);

        }
        setContentView(R.layout.fragment_playlist_detail);

        albumArt = (ImageView) findViewById(R.id.album_art);
        albumTitle = (TextView) findViewById(R.id.album_title);
        albumDetails = (TextView) findViewById(R.id.album_details);
        albumArtSmall = (SimpleDraweeView) findViewById(R.id.albumArtSmall);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        //recyclerView.setEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setUpEverything();

    }

    private void setUpEverything() {
        setupToolbar();
        loadBaiduAllLists();
        setAlbumart();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.actionbar_back);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("专辑");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }


    private void loadBaiduAllLists() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {

                JsonArray jsonArray = HttpUtil.getResposeJsonObject(BMA.Album.albumInfo(albumId)).get("songlist").getAsJsonArray();

                Iterator it = jsonArray.iterator();
                while (it.hasNext()) {
                    JsonElement e = (JsonElement) it.next();
                    JsonObject jo = e.getAsJsonObject();
                    MusicInfo mi = new MusicInfo();
                    mi.artist = getStringValue(jo, "author");
                    mi.musicName = getStringValue(jo, "title");
                    mi.data = getStringValue(jo, "song_id");

                    list.add(mi);
                }


                mAdapter = new PlaylistDetailAdapter(NetAlbumsDetailActivity.this, list);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                recyclerView.setAdapter(mAdapter);

                recyclerView.addItemDecoration(new DividerItemDecoration(NetAlbumsDetailActivity.this, DividerItemDecoration.VERTICAL_LIST));
            }
        }.execute();
    }


    private String getStringValue(JsonObject jsonObject, String key) {
        JsonElement nameElement = jsonObject.get(key);
        return nameElement.getAsString();
    }

    private int getIntValue(JsonObject jsonObject, String key) {
        JsonElement nameElement = jsonObject.get(key);
        return nameElement.getAsInt();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    private void setAlbumart() {
        albumTitle.setText(albumName);
        albumArtSmall.setImageURI(Uri.parse(albumPath));
        try {
            //drawable = Drawable.createFromStream( new URL(albumPath).openStream(),"src");
            ImageRequest imageRequest = ImageRequest.fromUri(albumPath);
            CacheKey cacheKey = DefaultCacheKeyFactory.getInstance()
                    .getEncodedCacheKey(imageRequest);
            BinaryResource resource = ImagePipelineFactory.getInstance()
                    .getMainDiskStorageCache().getResource(cacheKey);
            File file = ((FileBinaryResource) resource).getFile();
            new setBlurredAlbumArt().execute(ImageUtils.getArtworkQuick(file, 300, 300));


        } catch (Exception e) {

        }

    }


    private class setBlurredAlbumArt extends AsyncTask<Bitmap, Void, Drawable> {

        @Override
        protected Drawable doInBackground(Bitmap... loadedImage) {
            Drawable drawable = null;

            try {
                drawable = ImageUtils.createBlurredImageFromBitmap(loadedImage[0], NetAlbumsDetailActivity.this, 20);
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

    class PlaylistDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        final static int FIRST_ITEM = 0;
        final static int ITEM = 1;
        private ArrayList<MusicInfo> arraylist;
        private long playlistId;
        private Activity mContext;

        public PlaylistDetailAdapter(Activity context, ArrayList<MusicInfo> mList) {
            this.arraylist = mList;
            this.mContext = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            if (viewType == FIRST_ITEM) {
                return new CommonItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.common_item, viewGroup, false));
            } else {
                return new ItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_playlist_detail_item, viewGroup, false));
            }

        }

        //判断布局类型
        @Override
        public int getItemViewType(int position) {
            return position == FIRST_ITEM ? FIRST_ITEM : ITEM;

        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder itemHolder, final int i) {
            if (itemHolder instanceof ItemViewHolder) {
                final MusicInfo localItem = arraylist.get(i - 1);
                ((ItemViewHolder) itemHolder).trackNumber.setText(i + "");
                ((ItemViewHolder) itemHolder).title.setText(localItem.musicName);
                ((ItemViewHolder) itemHolder).artist.setText(artistName);
                ((ItemViewHolder) itemHolder).menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(mContext).setTitle("要下载音乐吗").
                                setPositiveButton(mContext.getString(R.string.sure), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Down.downMusic(MainApplication.context, localItem.data + "", localItem.musicName);
                                        dialog.dismiss();
                                    }
                                }).
                                setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                    }
                });

            } else if (itemHolder instanceof CommonItemViewHolder) {

                ((CommonItemViewHolder) itemHolder).textView.setText("(共" + arraylist.size() + "首)");

                ((CommonItemViewHolder) itemHolder).select.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, PlaylistSelectActivity.class);
                        intent.putParcelableArrayListExtra("ids", arraylist);
                        intent.putExtra("playlistid", playlistId);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        mContext.startActivity(intent);
                    }
                });

            }

        }

        @Override
        public int getItemCount() {
            return (null != arraylist ? arraylist.size() + 1 : 0);
        }

        public void updateDataSet(long playlistid, ArrayList<MusicInfo> arraylist) {
            this.arraylist = arraylist;
            this.playlistId = playlistid;
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
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        long[] list = new long[arraylist.size()];
                        for (int i = 0; i < arraylist.size(); i++) {
                            list[i] = arraylist.get(i).songId;
                        }
                        MusicPlayer.playAll(null, list, 0, false);
                    }
                }, 100);

            }

        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            protected TextView title, artist, trackNumber;
            protected ImageView menu;

            public ItemViewHolder(View view) {
                super(view);
                this.title = (TextView) view.findViewById(R.id.song_title);
                this.artist = (TextView) view.findViewById(R.id.song_artist);
                this.trackNumber = (TextView) view.findViewById(R.id.trackNumber);
                this.menu = (ImageView) view.findViewById(R.id.popup_menu);
                view.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(arraylist.get(getAdapterPosition()).data);
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            MusicPlayer.clearQueue();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }, 100);

            }

        }
    }
}
