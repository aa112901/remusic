package com.wm.remusic.activity;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.github.ksoichiro.android.observablescrollview.CacheFragmentStatePagerAdapter;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.wm.remusic.MainApplication;
import com.wm.remusic.R;
import com.wm.remusic.fragmentnet.ArtistInfoFragment;
import com.wm.remusic.fragmentnet.ArtistInfoMusicFragment;
import com.wm.remusic.handler.HandlerUtil;
import com.wm.remusic.info.MusicInfo;
import com.wm.remusic.json.ArtistInfo;
import com.wm.remusic.json.GeDanGeInfo;
import com.wm.remusic.json.MusicDetailInfo;
import com.wm.remusic.net.BMA;
import com.wm.remusic.net.HttpUtil;
import com.wm.remusic.net.MusicDetailInfoGet;
import com.wm.remusic.net.NetworkUtils;
import com.wm.remusic.net.RequestThreadPool;
import com.wm.remusic.uitl.CommonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wm on 2016/4/15.
 */

//歌单
public class ArtistDetailActivity extends BaseActivity implements ObservableScrollViewCallbacks {

    private String TAG = ArtistDetailActivity.class.getSimpleName();
    private String artistId;
    private String artistPath, artistName, artistDes;
    private ArrayList<GeDanGeInfo> mList = new ArrayList<GeDanGeInfo>();
    private ArrayList<MusicInfo> adapterList = new ArrayList<>();

    private SimpleDraweeView artistArt;
    private TextView artistTitle, tryAgain;

    private Toolbar toolbar;
    private SparseArray<MusicDetailInfo> sparseArray = new SparseArray<MusicDetailInfo>();
    private FrameLayout loadFrameLayout;
    private int musicCount;
    private Handler mHandler;
    private int tryCount;
    private View loadView;
    private ActionBar actionBar;
    private int mActionBarSize;
    private int mStatusSize;
    private LinearLayout mHeaderView;
    private int mBaseTranslationY;
    private ViewPager mPager;
    private NavigationAdapter mPagerAdapter;
    private TabLayout tabLayout;
    private ImageView toolbar_bac;
    private LoadNetPlaylistInfo mLoadNetList;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if (getIntent().getExtras() != null) {
            artistId = getIntent().getStringExtra("artistid");
            artistName = getIntent().getStringExtra("artistname");
        }
        setContentView(R.layout.activity_artist);
        loadFrameLayout = (FrameLayout) findViewById(R.id.state_container);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mHandler = HandlerUtil.getInstance(this);

        mActionBarSize = CommonUtils.getActionBarHeight(this);
        mStatusSize = CommonUtils.getStatusHeight(this);
        artistArt = (SimpleDraweeView) findViewById(R.id.artist_art);
        mHeaderView = (LinearLayout) findViewById(R.id.header);
        tryAgain = (TextView) findViewById(R.id.try_again);

        mPager = (ViewPager) findViewById(R.id.pager);
        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        toolbar_bac = (ImageView) findViewById(R.id.toolbar_bac);
        ViewGroup.LayoutParams layoutParams = toolbar_bac.getLayoutParams();
        layoutParams.height = mActionBarSize + mStatusSize;
        toolbar_bac.setLayoutParams(layoutParams);

        setUpEverything();

    }

    private void setUpEverything() {
        setupToolbar();
        setAlbumart();
        loadAllLists();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.actionbar_back);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(artistName);
        toolbar.setPadding(0, mStatusSize, 0, 0);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setSubtitle(artistDes);

    }


    private void loadAllLists() {
        tryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAllLists();
            }
        });

        if (NetworkUtils.isConnectInternet(this)) {
            tryAgain.setVisibility(View.GONE);
            loadView = LayoutInflater.from(this).inflate(R.layout.loading, loadFrameLayout, false);
            loadFrameLayout.addView(loadView);
            mLoadNetList = new LoadNetPlaylistInfo();
            mLoadNetList.execute();

        } else {
            tryAgain.setVisibility(View.VISIBLE);

        }

    }


    class LoadNetPlaylistInfo extends AsyncTask<Void, Void, Boolean> {
        //artistInfo artistInfo;
        @Override
        protected Boolean doInBackground(final Void... unused) {
            try {
                JsonObject jsonObject = HttpUtil.getResposeJsonObject(BMA.Artist.artistSongList("", artistId, 0, 50));

                JsonArray pArray = jsonObject.get("songlist").getAsJsonArray();
                musicCount = pArray.size();

                for (int i = 0; i < musicCount; i++) {
                    GeDanGeInfo geDanGeInfo = MainApplication.gsonInstance().fromJson(pArray.get(i), GeDanGeInfo.class);
                    mList.add(geDanGeInfo);
                    RequestThreadPool.post(new MusicDetailInfoGet(geDanGeInfo.getSong_id(), i, sparseArray));
                }

                int tryCount = 0;
                while (sparseArray.size() != musicCount && tryCount < 1000 && !isCancelled()){
                    tryCount++;
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(sparseArray.size() == musicCount){
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
                    return true;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean comlete) {

            if (!comlete) {
                tryAgain.setVisibility(View.VISIBLE);
            } else {
                Log.e("mlist", mList.toString());
                loadFrameLayout.removeAllViews();
                mPagerAdapter = new NavigationAdapter(getSupportFragmentManager());
                mPagerAdapter.addFragment(ArtistInfoMusicFragment.getInstance(adapterList));
                mPagerAdapter.addFragment(ArtistInfoFragment.getInstance(artistId));
                mPager.setAdapter(mPagerAdapter);
                tabLayout.setupWithViewPager(mPager);
                mPager.setCurrentItem(0);
            }

        }

        public void cancleTask(){
            cancel(true);
            RequestThreadPool.finish();
        }
    }




    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLoadNetList != null){
            mLoadNetList.cancleTask();
        }
    }

    ArtistInfo artistInfo;

    private void setAlbumart() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JsonObject object = HttpUtil.getResposeJsonObject(BMA.Artist.artistInfo("", artistId));
                artistInfo = MainApplication.gsonInstance().fromJson(object, ArtistInfo.class);
                if (artistInfo != null && artistInfo.getAvatar_s500() != null) {
                    artistPath = artistInfo.getAvatar_s500();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            artistArt.setImageURI(Uri.parse(artistPath));
                        }
                    });
                    Log.e(TAG, artistPath);
                }
            }
        }).start();
    }


    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        if (dragging) {
            int toolbarHeight = mHeaderView.getHeight() - mActionBarSize - mStatusSize - tabLayout.getHeight();
            float currentHeaderTranslationY = ViewHelper.getTranslationY(mHeaderView);
            if (firstScroll) {
                if (-toolbarHeight < currentHeaderTranslationY) {
                    mBaseTranslationY = scrollY;
                }
            }
            float headerTranslationY = ScrollUtils.getFloat(-(scrollY - mBaseTranslationY), -toolbarHeight, 0);
            ViewPropertyAnimator.animate(mHeaderView).cancel();
            ViewHelper.setTranslationY(mHeaderView, headerTranslationY);
        }

        toolbar_bac.setImageResource(R.drawable.toolbar_background_black);
        float a = (float) scrollY / (ViewHelper.getScrollY(mHeaderView));
        toolbar_bac.setAlpha(a);

    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        mBaseTranslationY = 0;

        Fragment fragment = getCurrentFragment();
        if (fragment == null) {
            return;
        }
        View view = fragment.getView();
        if (view == null) {
            return;
        }

        int toolbarHeight = mHeaderView.getHeight() - mActionBarSize - mStatusSize - tabLayout.getHeight();
        final ObservableRecyclerView listView = (ObservableRecyclerView) view.findViewById(R.id.scroll);
        if (listView == null) {
            return;
        }
        int scrollY = listView.getCurrentScrollY();
        if (scrollState == ScrollState.DOWN) {
            showToolbar();
        } else if (scrollState == ScrollState.UP) {
            if (toolbarHeight <= scrollY) {
                hideToolbar();
            } else {
                showToolbar();
            }
        } else {
            // Even if onScrollChanged occurs without scrollY changing, toolbar should be adjusted
            if (toolbarIsShown() || toolbarIsHidden()) {
                // Toolbar is completely moved, so just keep its state
                // and propagate it to other pages
                propagateToolbarState(toolbarIsShown());
            } else {
                // Toolbar is moving but doesn't know which to move:
                // you can change this to hideToolbar()
                showToolbar();
            }
        }
    }

    private Fragment getCurrentFragment() {
        return mPagerAdapter.getItemAt(mPager.getCurrentItem());
    }

    private void propagateToolbarState(boolean isShown) {
        int toolbarHeight = mHeaderView.getHeight() - mActionBarSize - mStatusSize - tabLayout.getHeight();

        // Set scrollY for the fragments that are not created yet
        mPagerAdapter.setScrollY(isShown ? 0 : toolbarHeight);

        // Set scrollY for the active fragments
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            // Skip current item
            if (i == mPager.getCurrentItem()) {
                continue;
            }

            // Skip destroyed or not created item
            Fragment f = mPagerAdapter.getItemAt(i);
            if (f == null) {
                continue;
            }

            View view = f.getView();
            if (view == null) {
                continue;
            }
            ObservableRecyclerView listView = (ObservableRecyclerView) view.findViewById(R.id.scroll);
            if (listView == null) {
                continue;
            }
            if (isShown) {
                // Scroll up
                if (0 < listView.getCurrentScrollY()) {
                    listView.scrollVerticallyToPosition(0);
                }
            } else {
                // Scroll down (to hide padding)
                if (listView.getCurrentScrollY() < toolbarHeight) {
                    listView.scrollVerticallyToPosition(1);
                }
            }
        }
    }

    private boolean toolbarIsShown() {
        return ViewHelper.getTranslationY(mHeaderView) == 0;
    }

    private boolean toolbarIsHidden() {
        return ViewHelper.getTranslationY(mHeaderView) == -mHeaderView.getHeight() - mActionBarSize - mStatusSize
                - tabLayout.getHeight();
    }

    private void showToolbar() {
        float headerTranslationY = ViewHelper.getTranslationY(mHeaderView);
        if (headerTranslationY != 0) {
            ViewPropertyAnimator.animate(mHeaderView).cancel();
            ViewPropertyAnimator.animate(mHeaderView).translationY(0).setDuration(200).start();
        }
        propagateToolbarState(true);
    }

    private void hideToolbar() {
        float headerTranslationY = ViewHelper.getTranslationY(mHeaderView);
        int toolbarHeight = mHeaderView.getHeight() - mActionBarSize - mStatusSize - tabLayout.getHeight();
        if (headerTranslationY != -toolbarHeight) {
            ViewPropertyAnimator.animate(mHeaderView).cancel();
            ViewPropertyAnimator.animate(mHeaderView).translationY(-toolbarHeight).setDuration(200).start();
        }
        propagateToolbarState(false);
    }

    private static class NavigationAdapter extends CacheFragmentStatePagerAdapter {

        private static final String[] TITLES = new String[]{"热门歌曲", "歌手信息"};
        private final List<Fragment> mFragments = new ArrayList<>();

        private int mScrollY;

        public NavigationAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setScrollY(int scrollY) {
            mScrollY = scrollY;
        }

        public void addFragment(Fragment fragment) {
            mFragments.add(fragment);
        }


        @Override
        protected Fragment createItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }
    }

}
