package com.wm.remusic.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nineoldandroids.view.ViewHelper;
import com.wm.remusic.MainApplication;
import com.wm.remusic.R;
import com.wm.remusic.dialog.AddDownTask;
import com.wm.remusic.dialog.LoadAllDownInfos;
import com.wm.remusic.downmusic.Down;
import com.wm.remusic.downmusic.DownloadManager;
import com.wm.remusic.downmusic.DownloadTask;
import com.wm.remusic.fragment.MoreFragment;
import com.wm.remusic.fragment.NetMoreFragment;
import com.wm.remusic.handler.HandlerUtil;
import com.wm.remusic.info.MusicInfo;
import com.wm.remusic.json.GeDanGeInfo;
import com.wm.remusic.json.MusicDetailInfo;
import com.wm.remusic.json.MusicFileDownInfo;
import com.wm.remusic.net.BMA;
import com.wm.remusic.net.HttpUtil;
import com.wm.remusic.net.MusicDetailInfoGet;
import com.wm.remusic.net.NetworkUtils;
import com.wm.remusic.net.PlaylistPlayInfoGet;
import com.wm.remusic.provider.PlaylistsManager;
import com.wm.remusic.service.MusicPlayer;
import com.wm.remusic.service.MusicTrack;
import com.wm.remusic.uitl.CommonUtils;
import com.wm.remusic.uitl.IConstants;
import com.wm.remusic.uitl.ImageUtils;
import com.wm.remusic.uitl.MusicUtils;
import com.wm.remusic.uitl.PreferencesUtility;
import com.wm.remusic.widget.DividerItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by wm on 2016/4/15.
 */

//歌单
public class PlaylistActivity extends BaseActivity implements ObservableScrollViewCallbacks {

    private String playlsitId;
    private String albumPath, albumName, playlistDetail;
    private ArrayList<GeDanGeInfo> mList = new ArrayList<GeDanGeInfo>();
    private ArrayList<MusicInfo> adapterList = new ArrayList<>();

    private SimpleDraweeView albumArtSmall;
    private ImageView albumArt;
    private TextView albumTitle, albumDetails;
    private boolean isLocalPlaylist;

    private PlaylistDetailAdapter mAdapter;
    private Toolbar toolbar;
    private SparseArray<MusicDetailInfo> sparseArray = new SparseArray<MusicDetailInfo>();
    private FrameLayout loadFrameLayout;
    private int musicCount;
    private Handler mHandler;
    private int tryCount;
    private View loadView;
    private int mFlexibleSpaceImageHeight;
    private ActionBar actionBar;
    private int mActionBarSize;
    private int mStatusSize;
    private TextView tryAgain;
    private TextView playlistCountView;
    private String playlistCount;
    private FrameLayout headerViewContent; //上部header
    private RelativeLayout headerDetail; //上部header信息
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if (getIntent().getExtras() != null) {
            isLocalPlaylist = getIntent().getBooleanExtra("islocal", false);
            playlsitId = getIntent().getStringExtra("playlistid");
            albumPath = getIntent().getStringExtra("albumart");
            albumName = getIntent().getStringExtra("playlistname");
            playlistDetail = getIntent().getStringExtra("playlistDetail");
            playlistCount = getIntent().getStringExtra("playlistcount");

        }
        mContext = this;
        setContentView(R.layout.activity_playlist);
        loadFrameLayout = (FrameLayout) findViewById(R.id.state_container);

        headerViewContent = (FrameLayout) findViewById(R.id.headerview);
        headerDetail = (RelativeLayout) findViewById(R.id.headerdetail);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mHandler = HandlerUtil.getInstance(this);

        mFlexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        mActionBarSize = CommonUtils.getActionBarHeight(this);
        mStatusSize = CommonUtils.getStatusHeight(this);


        tryAgain = (TextView) findViewById(R.id.try_again);

        setUpEverything();

    }

    private void setUpEverything() {
        setupToolbar();
        setHeaderView();
        setAlbumart();
        setList();
        loadAllLists();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.actionbar_back);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("歌单");
        toolbar.setPadding(0, mStatusSize, 0, 0);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if (!isLocalPlaylist) {
            toolbar.setSubtitle(playlistDetail);
        }

    }


    private void setHeaderView() {
        albumArt = (ImageView) findViewById(R.id.album_art);
        albumTitle = (TextView) findViewById(R.id.album_title);
        albumDetails = (TextView) findViewById(R.id.album_details);
        albumArtSmall = (SimpleDraweeView) findViewById(R.id.playlist_art);
        SpannableString spanString;
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.mipmap.index_icn_earphone);
        ImageSpan imgSpan = new ImageSpan(this, b, ImageSpan.ALIGN_BASELINE);
        spanString = new SpannableString("icon");
        spanString.setSpan(imgSpan, 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        playlistCountView = (TextView) findViewById(R.id.playlist_listen_count);
        playlistCountView.setText(spanString);
        if (playlistCount == null) {
            playlistCount = "0";
        }
        int count = Integer.parseInt(playlistCount);
        if (count > 10000) {
            count = count / 10000;
            playlistCountView.append(" " + count + "万");
        } else {
            playlistCountView.append(" " + playlistCount);
        }
        LinearLayout downAll = (LinearLayout) headerViewContent.findViewById(R.id.playlist_down);
        downAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new LoadAllDownInfos((Activity)mContext,mList).execute();
               // addDownTask.show(getSupportFragmentManager(),"");
//                ArrayList<MusicFileDownInfo> downList = new ArrayList<MusicFileDownInfo>();
//                int le = mList.size();
//                for(int j = 0; j< le ; j++){
//                    try {
//                        JsonArray jsonArray = HttpUtil.getResposeJsonObject(BMA.Song.songInfo(mList.get(j).getSong_id()))
//                                .get("songurl").getAsJsonObject().get("url").getAsJsonArray();
//                        int len = jsonArray.size();
//
//                        int downloadBit = PreferencesUtility.getInstance(mContext).getDownMusicBit();
//                        MusicFileDownInfo musicFileDownInfo = null;
//                        for (int i = len - 1; i > -1; i--) {
//                            int bit = Integer.parseInt(jsonArray.get(i).getAsJsonObject().get("file_bitrate").toString());
//                            if (bit == downloadBit) {
//                                musicFileDownInfo = MainApplication.gsonInstance().fromJson(jsonArray.get(i), MusicFileDownInfo.class);
//                            } else if (bit < downloadBit && bit >= 64) {
//                                musicFileDownInfo = MainApplication.gsonInstance().fromJson(jsonArray.get(i), MusicFileDownInfo.class);
//                            }
//                        }
//                        if(musicFileDownInfo != null)
//                            downList.add(musicFileDownInfo);
//
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                }

//                new AlertDialog.Builder(PlaylistActivity.this).setTitle("要下载音乐吗").
//                        setPositiveButton(PlaylistActivity.this.getString(R.string.sure), new DialogInterface.OnClickListener() {
//
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//
//                                int len = mList.size();
//                                for(int i = 0; i < len ; i++){
//                                    Down.downMusic(MainApplication.context, mList.get(i).getSong_id(),mList.get(i).getTitle());
//                                }
//                                mHandler.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Toast.makeText(PlaylistActivity.this, "已加入到下载", Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                                dialog.dismiss();
//                            }
//                        }).
//                        setNegativeButton(PlaylistActivity.this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        }).show();

            }
        });
        if(!isLocalPlaylist)
        headerDetail.setVisibility(View.GONE);
    }

    private void setList() {
        ObservableRecyclerView recyclerView = (ObservableRecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setScrollViewCallbacks(PlaylistActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(PlaylistActivity.this));
        recyclerView.setHasFixedSize(false);
        mAdapter = new PlaylistDetailAdapter(PlaylistActivity.this, adapterList);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(PlaylistActivity.this, DividerItemDecoration.VERTICAL_LIST));
    }


    protected void updateViews(int scrollY, boolean animated) {
        // If it's ListView, onScrollChanged is called before ListView is laid out (onGlobalLayout).
        // This causes weird animation when onRestoreInstanceState occurred,
        // so we check if it's laid out already.
//        if (!mReady) {
//            return;
//        }

        // Translate header
        ViewHelper.setTranslationY(headerViewContent, getHeaderTranslationY(scrollY));

    }

    protected float getHeaderTranslationY(int scrollY) {
        final int headerHeight = headerViewContent.getHeight();
        int headerTranslationY = mActionBarSize + mStatusSize - headerHeight;
        if (mActionBarSize + mStatusSize <= -scrollY + headerHeight) {
            headerTranslationY = -scrollY;
        }
        Log.e("headerY", "  " + headerTranslationY);
        return headerTranslationY;
    }


    private void loadAllLists() {


        if (isLocalPlaylist) {
            loadView = LayoutInflater.from(this).inflate(R.layout.loading, loadFrameLayout, false);
            loadFrameLayout.addView(loadView);
            new LoadLocalPlaylistInfo().execute();
            return;
        }

        if (NetworkUtils.isConnectInternet(this)) {
            tryAgain.setVisibility(View.GONE);
            loadView = LayoutInflater.from(this).inflate(R.layout.loading, loadFrameLayout, false);
            loadFrameLayout.addView(loadView);
            new LoadNetPlaylistInfo().execute();

        } else {
            tryAgain.setVisibility(View.VISIBLE);
            tryAgain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadAllLists();
                }
            });
        }

    }

    class LoadLocalPlaylistInfo extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(final Void... unused) {
            ArrayList<MusicTrack> musicInfos = PlaylistsManager.getInstance(PlaylistActivity.this).getPlaylist(Long.parseLong(playlsitId));
            long[] ids = new long[musicInfos.size()];
            for (int i = 0; i < musicInfos.size(); i++) {
                ids[i] = musicInfos.get(i).mId;

            }
            adapterList = MusicUtils.getMusicLists(PlaylistActivity.this, ids);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            loadFrameLayout.removeAllViews();
            mAdapter.updateDataSet(adapterList);
        }
    }

    class LoadNetPlaylistInfo extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(final Void... unused) {
            try {
                JsonObject jsonObject = HttpUtil.getResposeJsonObject(BMA.GeDan.geDanInfo(playlsitId + ""));
                // GeDanSrcInfo geDanSrcInfo = MainApplication.gsonInstance().fromJson(jsonObject.toString(), GeDanSrcInfo.class);
                JsonArray pArray = jsonObject.get("content").getAsJsonArray();
                musicCount = pArray.size();

                for (int i = 0; i < musicCount; i++) {
                    GeDanGeInfo geDanGeInfo = MainApplication.gsonInstance().fromJson(pArray.get(i), GeDanGeInfo.class);
                    mList.add(geDanGeInfo);
                    PlaylistPlayInfoGet.get(new MusicDetailInfoGet(geDanGeInfo.getSong_id(), i, sparseArray));
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mHandler.postDelayed(showPlaylistView, 100);

        }
    }

    Runnable showPlaylistView = new Runnable() {
        @Override
        public void run() {
            if (sparseArray.size() != musicCount && tryCount < 36) {
                mHandler.postDelayed(showPlaylistView, 200);
                tryCount++;
            } else {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        for (int i = 0; i < mList.size(); i++) {
                            try {
                                MusicInfo musicInfo = new MusicInfo();
                                musicInfo.songId = Integer.parseInt(mList.get(i).getSong_id());
                                musicInfo.musicName = mList.get(i).getTitle();
                                musicInfo.artist = sparseArray.get(i).getArtist_name();
                                musicInfo.islocal = false;
                                musicInfo.albumName = sparseArray.get(i).getAlbum_title();
                                musicInfo.albumId = Integer.parseInt(mList.get(i).getAlbum_id());
                                musicInfo.artistId = Integer.parseInt(sparseArray.get(i).getArtist_id());
                                musicInfo.lrc = sparseArray.get(i).getLrclink();
                                musicInfo.albumData = sparseArray.get(i).getPic_radio();
                                adapterList.add(musicInfo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        Log.e("mlist", mList.toString());
                        loadFrameLayout.removeAllViews();
                        mAdapter.updateDataSet(adapterList);
                        headerDetail.setVisibility(View.VISIBLE);
                    }
                }.execute();
            }
        }
    };


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

            if (isLocalPlaylist) {
                new setBlurredAlbumArt().execute(ImageUtils.getArtworkQuick(PlaylistActivity.this, Uri.parse(albumPath), 300, 300));
            } else {
                //drawable = Drawable.createFromStream( new URL(albumPath).openStream(),"src");
                ImageRequest imageRequest = ImageRequest.fromUri(albumPath);
                CacheKey cacheKey = DefaultCacheKeyFactory.getInstance()
                        .getEncodedCacheKey(imageRequest);
                BinaryResource resource = ImagePipelineFactory.getInstance()
                        .getMainDiskStorageCache().getResource(cacheKey);
                File file = ((FileBinaryResource) resource).getFile();
                new setBlurredAlbumArt().execute(ImageUtils.getArtworkQuick(file, 300, 300));
            }

        } catch (Exception e) {

        }

    }


    private class setBlurredAlbumArt extends AsyncTask<Bitmap, Void, Drawable> {

        @Override
        protected Drawable doInBackground(Bitmap... loadedImage) {
            Drawable drawable = null;

            try {
                drawable = ImageUtils.createBlurredImageFromBitmap(loadedImage[0], PlaylistActivity.this, 20);
//                drawable = ImageUtils.createBlurredImageFromBitmap(ImageUtils.getBitmapFromDrawable(Drawable.createFromStream(new URL(albumPath).openStream(), "src")),
//                        NetPlaylistDetailActivity.this, 30);
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
    }


    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {

        updateViews(scrollY, false);

        if (scrollY > 0 && scrollY < mFlexibleSpaceImageHeight - mActionBarSize - mStatusSize) {
            toolbar.setTitle(albumName);
            toolbar.setSubtitle(playlistDetail);
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.toolbar_background));
        }
        if (scrollY == 0) {
            toolbar.setTitle("歌单");
            actionBar.setBackgroundDrawable(null);
        }
        if (scrollY > mFlexibleSpaceImageHeight - mActionBarSize - mStatusSize) {

//            if(mBlurDrawable != null){
//                mBlurDrawable.setColorFilter(Color.parseColor("#79000000"), PorterDuff.Mode.SRC_OVER);
//                actionBar.setBackgroundDrawable(mBlurDrawable);
//            }
        }

        float a = (float) scrollY / (mFlexibleSpaceImageHeight - mActionBarSize - mStatusSize);
        headerDetail.setAlpha(1f - a);
        Log.e("alpha", " " + a);
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.playlit_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menu_sort_by_az:
//                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_A_Z);
//                reloadAdapter();
//                return true;
//            case R.id.menu_sort_by_date:
//                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_DATE);
//                reloadAdapter();
//                return true;
//            case R.id.menu_sort_by_artist:
//                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_ARTIST);
//                reloadAdapter();
//                return true;
//            case R.id.menu_sort_by_album:
//                mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_ALBUM);
//                reloadAdapter();
//                return true;
//
//        }
//        return super.onOptionsItemSelected(item);
//    }


    class PlaylistDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        final static int FIRST_ITEM = 0;
        final static int ITEM = 1;
        private ArrayList<MusicInfo> arraylist;
        private Activity mContext;

        public PlaylistDetailAdapter(Activity context, ArrayList<MusicInfo> mList) {
            this.arraylist = mList;
            this.mContext = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            if (viewType == FIRST_ITEM) {
                return new CommonItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.header_common_item, viewGroup, false));
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
                ((ItemViewHolder) itemHolder).artist.setText(localItem.artist);
                ((ItemViewHolder) itemHolder).menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (localItem.islocal) {
                            MoreFragment morefragment = MoreFragment.newInstance(arraylist.get(i - 1),
                                    IConstants.MUSICOVERFLOW);
                            morefragment.show(((AppCompatActivity) mContext).getSupportFragmentManager(), "music");
                        } else {
                            NetMoreFragment morefragment = NetMoreFragment.newInstance(arraylist.get(i - 1),
                                    IConstants.MUSICOVERFLOW);
                            morefragment.show(((AppCompatActivity) mContext).getSupportFragmentManager(), "music");
                        }
//                        new AlertDialog.Builder(mContext).setTitle("要下载音乐吗").
//                                setPositiveButton(mContext.getString(R.string.sure), new DialogInterface.OnClickListener() {
//
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//
//                                        Down.downMusic(MainApplication.context, localItem.songId + "", localItem.musicName);
//                                        dialog.dismiss();
//                                    }
//                                }).
//                                setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        dialog.dismiss();
//                                    }
//                                }).show();
                    }
                });

            } else if (itemHolder instanceof CommonItemViewHolder) {

                ((CommonItemViewHolder) itemHolder).textView.setText("(共" + arraylist.size() + "首)");

                ((CommonItemViewHolder) itemHolder).select.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

            }

        }

        @Override
        public int getItemCount() {
            return arraylist == null ? 0 : arraylist.size() + 1;
        }

        public void updateDataSet(ArrayList<MusicInfo> arraylist) {
            this.arraylist = arraylist;
            this.notifyDataSetChanged();
        }

        public class CommonItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView textView;
            ImageView select;
            RelativeLayout layout;

            CommonItemViewHolder(View view) {
                super(view);
                this.textView = (TextView) view.findViewById(R.id.play_all_number);
                this.select = (ImageView) view.findViewById(R.id.select);
                this.layout = (RelativeLayout) view.findViewById(R.id.play_all_layout);
                layout.setOnClickListener(this);
            }

            public void onClick(View v) {
                //// TODO: 2016/1/20
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HashMap<Long, MusicInfo> infos = new HashMap<Long, MusicInfo>();
                        int len = arraylist.size();
                        long[] list = new long[len];
                        for (int i = 0; i < len; i++) {
                            MusicInfo info = arraylist.get(i);
                            list[i] = info.songId;
                            infos.put(list[i], info);
                        }
                        MusicPlayer.playAll(infos, list, 0, false);
                    }
                }).start();

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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HashMap<Long, MusicInfo> infos = new HashMap<Long, MusicInfo>();
                        int len = arraylist.size();
                        long[] list = new long[len];
                        for (int i = 0; i < len; i++) {
                            MusicInfo info = arraylist.get(i);
                            list[i] = info.songId;
                            infos.put(list[i], info);
                        }

//                            long[] list = new long[arraylist.size()];
//                            HashMap<Long,MusicInfo> infos = new HashMap<Long,MusicInfo>();
//                            for (int i = 0; i < arraylist.size(); i++) {
//                                list[i] = Long.parseLong(arraylist.get(i).getSong_id());
//                                MusicInfo musicInfo = new MusicInfo();
//                                musicInfo.songId = Integer.parseInt(arraylist.get(i).getSong_id());
//                                musicInfo.musicName = arraylist.get(i).getTitle();
//                                musicInfo.artist = sparseArray.get(i).getArtist_name();
//                                musicInfo.islocal = false;
//                                musicInfo.albumName = sparseArray.get(i).getAlbum_title();
//                                musicInfo.albumId = Integer.parseInt(arraylist.get(i).getAlbum_id());
//                                musicInfo.artistId = Integer.parseInt(sparseArray.get(i).getArtist_id());
//                                musicInfo.lrc = sparseArray.get(i).getLrclink();
//                                musicInfo.albumData = sparseArray.get(i).getPic_radio();
//                                infos.put(list[i] , musicInfo);
//                            }
                        MusicPlayer.playAll(infos, list, getAdapterPosition() - 1, false);
                    }
                }).start();

            }

        }
    }
}
