package com.wm.remusic.activity;

/**
 * Created by wm on 2016/5/20.
 */

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.Toast;

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
import com.wm.remusic.uitl.ImageUtils;
import com.wm.remusic.widget.DividerItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Created by wm on 2016/4/11.
 */
public class NetArtistDetailActivity extends BaseActivity {
    String artistId;
    private String artistPath, artistName, tingUid;
    private int publishTime;
    private ArrayList<MusicInfo> list = new ArrayList<>();

    private LinearLayoutManager linearLayoutManager;
    private SimpleDraweeView artistArtSmall;
    private ImageView artistArt;
    private TextView artistTitle, artistDetails;

    private RecyclerView recyclerView;
    private ArtistDetailAdapter mAdapter;

    private Toolbar toolbar;
    private int count = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getExtras() != null) {
            artistId = getIntent().getStringExtra("artistid");
            artistPath = getIntent().getStringExtra("artistart");
            artistName = getIntent().getStringExtra("artistname");
            tingUid = getIntent().getStringExtra("artistUid");
            publishTime = getIntent().getIntExtra("publishtime", -1);

        }
        setContentView(R.layout.fragment_playlist_detail);

        artistArt = (ImageView) findViewById(R.id.album_art);
        artistTitle = (TextView) findViewById(R.id.album_title);
        artistDetails = (TextView) findViewById(R.id.album_details);
        artistArtSmall = (SimpleDraweeView) findViewById(R.id.albumArtSmall);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new ArtistDetailAdapter(NetArtistDetailActivity.this, null);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(NetArtistDetailActivity.this, DividerItemDecoration.VERTICAL_LIST));

        setUpEverything();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastVisiableItem;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisiableItem + 1 == mAdapter.getItemCount()) {
                    count++;
                    loadBaiduAllLists(count);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisiableItem = linearLayoutManager.findLastVisibleItemPosition();
            }
        });
    }

    private void setUpEverything() {
        setupToolbar();
        loadBaiduAllLists(count);
        setAlbumart();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.actionbar_back);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("歌手");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }


    private void loadBaiduAllLists(int count) {
        new AsyncTask<Integer, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(final Integer... offset) {

                try {
                    JsonArray jsonArray = HttpUtil.getResposeJsonObject(BMA.Artist.artistSongList(tingUid, artistId, offset[0] * 10, 10)).get("songlist").getAsJsonArray();

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
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;

                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean update) {
                if (update) {
                    mAdapter.update(list);
                } else {
                    Toast.makeText(NetArtistDetailActivity.this, "已经到最后了", Toast.LENGTH_SHORT).show();
                    mAdapter.setNoLoad();
                }
            }
        }.execute(count);
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
        artistTitle.setText(artistName);
        artistArtSmall.setImageURI(Uri.parse(artistPath));
        try {
            //drawable = Drawable.createFromStream( new URL(albumPath).openStream(),"src");
            ImageRequest imageRequest = ImageRequest.fromUri(artistPath);
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
                drawable = ImageUtils.createBlurredImageFromBitmap(loadedImage[0], NetArtistDetailActivity.this, 20);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return drawable;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result != null) {
                if (artistArt.getDrawable() != null) {
                    final TransitionDrawable td =
                            new TransitionDrawable(new Drawable[]{
                                    artistArt.getDrawable(),
                                    result
                            });
                    artistArt.setImageDrawable(td);
                    td.startTransition(200);

                } else {
                    artistArt.setImageDrawable(result);
                }
            }
        }

        @Override
        protected void onPreExecute() {
        }
    }

    class ArtistDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        final static int LAST_ITEM = 0;
        final static int ITEM = 1;
        private ArrayList<MusicInfo> arraylist;
        private long playlistId;
        private Activity mContext;
        private boolean continueLoad = true;

        public ArtistDetailAdapter(Activity context, ArrayList<MusicInfo> mList) {
            this.arraylist = mList;
            this.mContext = context;
        }

        public void update(ArrayList<MusicInfo> mList) {
            this.arraylist = mList;
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            if (viewType == LAST_ITEM) {
                return new CommonItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.loading, viewGroup, false));
            } else {
                return new ItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_playlist_detail_item, viewGroup, false));
            }

        }

        public void setNoLoad() {
            continueLoad = false;
            notifyDataSetChanged();
        }

        //判断布局类型
        @Override
        public int getItemViewType(int position) {
            if (!continueLoad) {
                return ITEM;
            }
            return position == getItemCount() - 1 ? LAST_ITEM : ITEM;

        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder itemHolder, final int i) {
            if (itemHolder instanceof ItemViewHolder) {
                final MusicInfo localItem = arraylist.get(i);
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

            }

        }

        @Override
        public int getItemCount() {
            if (!continueLoad) {
                return (null != arraylist ? arraylist.size() : 0);
            }
            return (null != arraylist ? arraylist.size() + 1 : 0);
        }

        public void updateDataSet(long playlistid, ArrayList<MusicInfo> arraylist) {
            this.arraylist = arraylist;
            this.playlistId = playlistid;
        }

        public class CommonItemViewHolder extends RecyclerView.ViewHolder {

            CommonItemViewHolder(View view) {
                super(view);
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

            }

        }
    }
}

