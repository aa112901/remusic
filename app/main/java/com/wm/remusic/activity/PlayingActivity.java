package com.wm.remusic.activity;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.DrawableUtils;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.wm.remusic.R;
import com.wm.remusic.fragment.PlayQueueFragment;
import com.wm.remusic.fragment.RoundFragment;
import com.wm.remusic.fragment.SimpleMoreFragment;
import com.wm.remusic.handler.HandlerUtil;
import com.wm.remusic.info.MusicInfo;
import com.wm.remusic.lrc.DefaultLrcParser;
import com.wm.remusic.lrc.LrcRow;
import com.wm.remusic.lrc.LrcView;
import com.wm.remusic.provider.PlaylistsManager;
import com.wm.remusic.service.MediaService;
import com.wm.remusic.service.MusicPlayer;
import com.wm.remusic.uitl.IConstants;
import com.wm.remusic.uitl.ImageUtils;
import com.wm.remusic.uitl.L;
import com.wm.remusic.uitl.MusicUtils;
import com.wm.remusic.widget.AlbumViewPager;
import com.wm.remusic.widget.PlayerSeekBar;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.List;

import static com.wm.remusic.service.MusicPlayer.getAlbumPath;


/**
 * Created by wm on 2016/2/21.
 */
public class PlayingActivity extends BaseActivity implements IConstants {
    private ImageView backAlbum, playingmode, control, next, pre, playlist, cmt, fav, down, more, needle;
    private TextView timePlayed, duration;
    private PlayerSeekBar mProgress;

    private ActionBar ab;
    private ObjectAnimator needleAnim, animator;
    private AnimatorSet animatorSet;
    private AlbumViewPager mViewPager;
    private FragmentAdapter fAdapter;
    private BitmapFactory.Options newOpts;
    private View activeView;
    private PlaylistsManager playlistsManager;
    private WeakReference<ObjectAnimator> animatorWeakReference;
    private WeakReference<View> viewWeakReference;
    private boolean isFav = false;
    private boolean isNextOrPreSetPage = false; //判断viewpager由手动滑动 还是setcruuentitem换页
    private boolean duetoplaypause = false; //判读是否是播放暂停的通知，不要切换专辑封面
    private Toolbar toolbar;
    private FrameLayout albumLayout;
    private RelativeLayout lrcViewContainer;
    private LrcView mLrcView;
    private TextView tryGetLrc;
    private LinearLayout musicTool;
    private SeekBar mVolumeSeek;
    private boolean print = true;
    private String TAG = PlayingActivity.class.getSimpleName();


    @Override
    protected void showQuickControl(boolean show) {
        //super.showOrHideQuickControl(show);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playing);
        playlistsManager = PlaylistsManager.getInstance(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ab = getSupportActionBar();
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.actionbar_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
        albumLayout = (FrameLayout) findViewById(R.id.headerView);
        lrcViewContainer = (RelativeLayout) findViewById(R.id.lrcviewContainer);
        mLrcView = (LrcView) findViewById(R.id.lrcview);
        tryGetLrc = (TextView) findViewById(R.id.tragetlrc);
        musicTool = (LinearLayout) findViewById(R.id.music_tool);

        backAlbum = (ImageView) findViewById(R.id.albumArt);
        playingmode = (ImageView) findViewById(R.id.playing_mode);
        control = (ImageView) findViewById(R.id.playing_play);
        next = (ImageView) findViewById(R.id.playing_next);
        pre = (ImageView) findViewById(R.id.playing_pre);
        playlist = (ImageView) findViewById(R.id.playing_playlist);
        more = (ImageView) findViewById(R.id.playing_more);
        cmt = (ImageView) findViewById(R.id.playing_cmt);
        fav = (ImageView) findViewById(R.id.playing_fav);
        down = (ImageView) findViewById(R.id.playing_down);
        timePlayed = (TextView) findViewById(R.id.music_duration_played);
        duration = (TextView) findViewById(R.id.music_duration);
        mProgress = (PlayerSeekBar) findViewById(R.id.play_seek);
        needle = (ImageView) findViewById(R.id.needle);
        mViewPager = (AlbumViewPager) findViewById(R.id.view_pager);
        mViewPager.setOffscreenPageLimit(2);
        mVolumeSeek = (SeekBar) findViewById(R.id.volume_seek);
        mProgress.setIndeterminate(false);
        mProgress.setProgress(1);

        if(MusicPlayer.isTrackLocal()){
            mProgress.setSecondaryProgress(100);
        }
        loadOther();
        setViewPager();
        initLrcView();
        // setViewPager();
    }

    private void initLrcView() {
        mLrcView.setOnSeekToListener(onSeekToListener);
        mLrcView.setOnLrcClickListener(onLrcClickListener);
        mViewPager.setOnSingleTouchListener(new AlbumViewPager.OnSingleTouchListener() {
            @Override
            public void onSingleTouch(View v) {
                if (albumLayout.getVisibility() == View.VISIBLE) {
                    albumLayout.setVisibility(View.INVISIBLE);
                    lrcViewContainer.setVisibility(View.VISIBLE);
                    musicTool.setVisibility(View.INVISIBLE);
                }
            }
        });
        lrcViewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lrcViewContainer.getVisibility() == View.VISIBLE) {
                    lrcViewContainer.setVisibility(View.INVISIBLE);
                    albumLayout.setVisibility(View.VISIBLE);
                    musicTool.setVisibility(View.VISIBLE);
                }
            }
        });

        tryGetLrc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(MediaService.TRY_GET_TRACKINFO);
                sendBroadcast(intent);
                Toast.makeText(getApplicationContext(), "正在获取信息", Toast.LENGTH_SHORT).show();
            }
        });

        final AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int v = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int mMaxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolumeSeek.setMax(mMaxVol);
        mVolumeSeek.setProgress(v);
        mVolumeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress ,AudioManager.ADJUST_SAME);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    LrcView.OnLrcClickListener onLrcClickListener = new LrcView.OnLrcClickListener() {

        @Override
        public void onClick() {

            if (lrcViewContainer.getVisibility() == View.VISIBLE) {
                lrcViewContainer.setVisibility(View.INVISIBLE);
                albumLayout.setVisibility(View.VISIBLE);
                musicTool.setVisibility(View.VISIBLE);
            }
        }
    };
    LrcView.OnSeekToListener onSeekToListener = new LrcView.OnSeekToListener() {

        @Override
        public void onSeekTo(int progress) {
            MusicPlayer.seek(progress);

        }
    };


    private List<LrcRow> getLrcRows() {

        List<LrcRow> rows = null;
        InputStream is = null;
        try {
            is = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/remusic/lrc/" + MusicPlayer.getCurrentAudioId());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (is == null) {
                return null;
            }
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            rows = DefaultLrcParser.getIstance().getLrcRows(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }

    private void loadOther() {
        needleAnim = ObjectAnimator.ofFloat(needle, "rotation", -30, 0);
        needleAnim.setDuration(60);
        needleAnim.setRepeatMode(0);
        needleAnim.setInterpolator(new LinearInterpolator());

        setSeekBarListener();
        setTools();

    }

    private void setViewPager() {

        PlaybarPagerTransformer transformer = new PlaybarPagerTransformer();
        fAdapter = new FragmentAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(fAdapter);
        mViewPager.setPageTransformer(true, transformer);

        //改变viewpager动画时间
//        try {
//            Field mField = ViewPager.class.getDeclaredField("mScroller");
//            mField.setAccessible(true);
//            MyScroller mScroller = new MyScroller(mViewPager.getContext().getApplicationContext(), new AccelerateInterpolator());
//            mField.set(mViewPager, mScroller);
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(final int pPosition) {
                if (pPosition < 1) { //首位之前，跳转到末尾（N）
                    MusicPlayer.setQueuePosition(MusicPlayer.getQueue().length);
                    mViewPager.setCurrentItem(MusicPlayer.getQueue().length, false);
                    isNextOrPreSetPage = false;
                    return;

                } else if (pPosition > MusicPlayer.getQueue().length) { //末位之后，跳转到首位（1）
                    MusicPlayer.setQueuePosition(0);
                    mViewPager.setCurrentItem(1, false); //false:不显示跳转过程的动画
                    isNextOrPreSetPage = false;
                    return;
                } else {

                    if (isNextOrPreSetPage == false) {
                        if (pPosition < MusicPlayer.getQueuePosition() + 1) {
                            HandlerUtil.getInstance(PlayingActivity.this).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    MusicPlayer.previous(PlayingActivity.this, true);
                                }
                            }, 396);


                        } else if (pPosition > MusicPlayer.getQueuePosition() + 1) {
                            HandlerUtil.getInstance(PlayingActivity.this).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    MusicPlayer.next();
                                }
                            }, 396);

                        }
                    }

                }
                //MusicPlayer.setQueuePosition(pPosition - 1);
                isNextOrPreSetPage = false;

            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int pState) {
            }
        });
    }

    private void setTools() {
        playingmode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayer.cycleRepeat();
                updatePlaymode();
            }
        });

        pre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayer.previous(PlayingActivity.this.getApplication(), true);
            }
        });

        control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                duetoplaypause = true;


                if (MusicPlayer.isPlaying()) {
                    control.setImageResource(R.drawable.play_rdi_btn_pause);
                } else {
                    control.setImageResource(R.drawable.play_rdi_btn_play);
                }
                if (MusicPlayer.getQueueSize() != 0) {
                    MusicPlayer.playOrPause();
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (animator != null) {
                    animator.end();
                    animator = null;
                }
                MusicPlayer.next();
            }
        });

        playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayQueueFragment playQueueFragment = new PlayQueueFragment();
                playQueueFragment.show(getSupportFragmentManager(), "playlistframent");
            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SimpleMoreFragment moreFragment = SimpleMoreFragment.newInstance(MusicPlayer.getCurrentAudioId());
                moreFragment.show(getSupportFragmentManager(), "music");
            }
        });

        fav.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (isFav) {
                    playlistsManager.removeItem(PlayingActivity.this, IConstants.FAV_PLAYLIST,
                            MusicPlayer.getCurrentAudioId());
                    fav.setImageResource(R.drawable.play_rdi_icn_love);
                    isFav = false;
                } else {
                    try {
                        MusicInfo info = MusicPlayer.getPlayinfos().get(MusicPlayer.getCurrentAudioId());
                        playlistsManager.insertMusic(PlayingActivity.this,IConstants.FAV_PLAYLIST,info);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    fav.setImageResource(R.drawable.play_icn_loved);
                    isFav = true;
                }

                Intent intent = new Intent(IConstants.PLAYLIST_COUNT_CHANGED);
                sendBroadcast(intent);
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        if (item.getItemId() == R.id.menu_share) {
            MusicInfo musicInfo = MusicUtils.getMusicInfo(PlayingActivity.this, MusicPlayer.getCurrentAudioId());
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + musicInfo.data));
            shareIntent.setType("audio/*");
            this.startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.shared_to)));

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.playing_menu, menu);
        return true;

    }

    private void updatePlaymode() {
        if (MusicPlayer.getShuffleMode() == MediaService.SHUFFLE_NORMAL) {
            playingmode.setImageResource(R.drawable.play_icn_shuffle);
            Toast.makeText(PlayingActivity.this.getApplication(), getResources().getString(R.string.random_play),
                    Toast.LENGTH_SHORT).show();
        } else {
            switch (MusicPlayer.getRepeatMode()) {
                case MediaService.REPEAT_ALL:
                    playingmode.setImageResource(R.drawable.play_icn_loop);
                    Toast.makeText(PlayingActivity.this.getApplication(), getResources().getString(R.string.loop_play),
                            Toast.LENGTH_SHORT).show();
                    break;
                case MediaService.REPEAT_CURRENT:
                    playingmode.setImageResource(R.drawable.play_icn_one);
                    Toast.makeText(PlayingActivity.this.getApplication(), getResources().getString(R.string.play_one),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        //设置ViewPager的默认项
        mViewPager.setCurrentItem(MusicPlayer.getQueuePosition() + 1);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    public void updateQueue() {
        if (MusicPlayer.getQueueSize() == 0) {
            MusicPlayer.stop();
            finish();
            return;
        }
        fAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(MusicPlayer.getQueuePosition() + 1, false);
    }

    private void updateFav(boolean b) {
        if (b) {
            fav.setImageResource(R.drawable.play_icn_loved);
        } else {
            fav.setImageResource(R.drawable.play_rdi_icn_love);
        }
    }

    public void updateLrc() {
        List<LrcRow> list = getLrcRows();
        if (list != null && list.size() > 0) {
            tryGetLrc.setVisibility(View.INVISIBLE);
            mLrcView.setLrcRows(list);
        } else {
            tryGetLrc.setVisibility(View.VISIBLE);
            mLrcView.reset();
        }
    }

    public void updateTrack() {
        new setBlurredAlbumArt().execute();
    }

    public void updateTrackInfo() {

            if (MusicPlayer.getQueueSize() == 0) {
                return;
            }
            if (!duetoplaypause) {
                isFav = false;
                long[] favlists = playlistsManager.getPlaylistIds(IConstants.FAV_PLAYLIST);
                long currentid = MusicPlayer.getCurrentAudioId();
                for(long i : favlists){
                    if(currentid == i){
                        isFav = true;
                        break;
                    }
                }
                updateFav(isFav);
                updateLrc();
            }
            duetoplaypause = false;

            Fragment fragment = (RoundFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem());
            if(fragment != null){
                View v = fragment.getView();
                if(v != null){
                    ((ViewGroup) v).setAnimationCacheEnabled(false);
                }
                if(viewWeakReference != null)
                    viewWeakReference.clear();
                viewWeakReference = new WeakReference<View>(v);
                activeView = viewWeakReference.get();
            }


            if (activeView != null) {
    //            animatorWeakReference = new WeakReference<>((ObjectAnimator) activeView.getTag(R.id.tag_animator));
    //            animator = animatorWeakReference.get();
                animator = (ObjectAnimator) activeView.getTag(R.id.tag_animator);
            }

            ab.setTitle(MusicPlayer.getTrackName());
            ab.setSubtitle(MusicPlayer.getArtistName());


            duration.setText(MusicUtils.makeShortTimeString(PlayingActivity.this.getApplication(), MusicPlayer.duration() / 1000));
            //mProgress.setMax((int) MusicPlayer.duration());

            mProgress.postDelayed(mUpdateProgress, 10);


            if (MusicPlayer.isPlaying()) {
                control.setImageResource(R.drawable.play_rdi_btn_pause);

            } else {
                control.setImageResource(R.drawable.play_rdi_btn_play);
            }


            animatorSet = new AnimatorSet();
            if (MusicPlayer.isPlaying()) {
                if (animatorSet != null && animator != null && !animator.isRunning() ) {
                    //修复从playactivity回到Main界面null
                    if(needleAnim == null){
                        needleAnim = ObjectAnimator.ofFloat(needle, "rotation", -30, 0);
                        needleAnim.setDuration(60);
                        needleAnim.setRepeatMode(0);
                        needleAnim.setInterpolator(new LinearInterpolator());
                    }
                    animatorSet.play(needleAnim).before(animator);
                    animatorSet.start();
                }

            } else {
                if (needleAnim != null) {
                    needleAnim.reverse();
                    needleAnim.end();
                }

                if (animator != null && animator.isRunning()) {
                    animator.cancel();
                    float valueAvatar = (float) animator.getAnimatedValue();
                    animator.setFloatValues(valueAvatar, 360f + valueAvatar);
                }
            }

            isNextOrPreSetPage = false;
            if (MusicPlayer.getQueuePosition() + 1 != mViewPager.getCurrentItem()) {
                mViewPager.setCurrentItem(MusicPlayer.getQueuePosition() + 1);
                isNextOrPreSetPage = true;
            }

    }

    @Override
    public void updateTime() {
        duration.setText(MusicUtils.makeShortTimeString(PlayingActivity.this.getApplication(), MusicPlayer.duration() / 1000));
        duration.setText(MusicUtils.makeTimeString(MusicPlayer.duration()));
        //mProgress.setMax((int) MusicPlayer.duration());
    }

    @Override
    public void updateBuffer(int p) {
        mProgress.setSecondaryProgress(p);
    }

    @Override
    public void loading(boolean l) {
        mProgress.setLoading(l);
    }

    private Runnable mUpdateProgress = new Runnable() {

        @Override
        public void run() {

            if (mProgress != null) {
                long position = MusicPlayer.position();
                long duration = MusicPlayer.duration();
                if (duration > 0)
                    mProgress.setProgress((int) (mProgress.getMax() * position / duration));
                timePlayed.setText(MusicUtils.makeShortTimeString(PlayingActivity.this.getApplication(), position / 1000));

                if(MusicPlayer.isPlaying()){
                    mProgress.postDelayed(mUpdateProgress, 50);
                }
            }
        }
    };

    private void setSeekBarListener() {

        if (mProgress != null)
            mProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int progress = 0;

                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    i = (int) (i * MusicPlayer.duration() / 100);
                    mLrcView.seekTo(i, true, b);
                    if (b) {
                        MusicPlayer.seek((long) i);
                        timePlayed.setText(MusicUtils.makeShortTimeString(PlayingActivity.this.getApplication(), i / 1000));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
    }

    private void stopAnim() {
        activeView = null;

        if (animator != null) {
            animator.end();
            animator = null;
        }
        if (needleAnim != null) {
            needleAnim.end();
            needleAnim = null;
        }
        if (animatorSet != null) {
            animatorSet.end();
            animatorSet = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mProgress.removeCallbacks(mUpdateProgress);
        stopAnim();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopAnim();
        mProgress.removeCallbacks(mUpdateProgress);
    }


    public class PlaybarPagerTransformer implements ViewPager.PageTransformer {


        @Override
        public void transformPage(View view, float position) {

            if (position == 0) {
                if (MusicPlayer.isPlaying()) {
                    animator = (ObjectAnimator) view.getTag(R.id.tag_animator);
                    if (animator != null && !animator.isRunning()) {
                        animatorSet = new AnimatorSet();
                        animatorSet.play(needleAnim).before(animator);
                        animatorSet.start();
                    }
                }

            } else if (position == -1 || position == -2 || position == 1) {

                animator = (ObjectAnimator) view.getTag(R.id.tag_animator);
                if (animator != null) {
                    animator.setFloatValues(0);
                    animator.end();
                    animator = null;
                }
            } else {

                if (needleAnim != null) {
                    needleAnim.reverse();
                    needleAnim.end();
                }

                animator = (ObjectAnimator) view.getTag(R.id.tag_animator);
                if (animator != null) {
                    animator.cancel();
                    float valueAvatar = (float) animator.getAnimatedValue();
                    animator.setFloatValues(valueAvatar, 360f + valueAvatar);

                }
            }
        }

    }

    private Bitmap mBitmap;

    private class setBlurredAlbumArt extends AsyncTask<Void, Void, Drawable> {
        long albumid = MusicPlayer.getCurrentAlbumId();

        @Override
        protected Drawable doInBackground(Void... loadedImage) {

            Drawable drawable = null;
            mBitmap = null;
            if (newOpts == null) {
                newOpts = new BitmapFactory.Options();
                newOpts.inSampleSize = 6;
                newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
            }
            if (!MusicPlayer.isTrackLocal()) {
                L.D(print, TAG, "music is net");
                if (getAlbumPath() == null) {
                    L.D(print, TAG, "getalbumpath is null");
                    mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_disk_210);
                    drawable = ImageUtils.createBlurredImageFromBitmap(mBitmap, PlayingActivity.this.getApplication(), 3);
                    return drawable;
                }
                ImageRequest imageRequest = ImageRequestBuilder
                        .newBuilderWithSource(Uri.parse(getAlbumPath()))
                        .setProgressiveRenderingEnabled(true)
                        .build();

                ImagePipeline imagePipeline = Fresco.getImagePipeline();
                DataSource<CloseableReference<CloseableImage>>
                        dataSource = imagePipeline.fetchDecodedImage(imageRequest, PlayingActivity.this);

                dataSource.subscribe(new BaseBitmapDataSubscriber() {
                                         @Override
                                         public void onNewResultImpl(@Nullable Bitmap bitmap) {
                                             // You can use the bitmap in only limited ways
                                             // No need to do any cleanup.
                                             if (bitmap != null) {
                                                 mBitmap = bitmap;
                                                 L.D(print, TAG, "getalbumpath bitmap success");
                                             }
                                         }

                                         @Override
                                         public void onFailureImpl(DataSource dataSource) {
                                             // No cleanup required here.
                                             L.D(print, TAG, "getalbumpath bitmap failed");
                                             mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_disk_210);

                                         }
                                     },
                        CallerThreadExecutor.getInstance());
                if (mBitmap != null) {
                    drawable = ImageUtils.createBlurredImageFromBitmap(mBitmap, PlayingActivity.this.getApplication(), 3);
                }

            } else {
                try {
                    mBitmap = null;
                    Bitmap bitmap = null;
                    Uri art = Uri.parse(getAlbumPath());
                    L.D(print, TAG, "album is local ");
                    if (art != null) {
                        ParcelFileDescriptor fd = null;
                        try {
                            fd = getContentResolver().openFileDescriptor(art, "r");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        if (fd != null) {
                            bitmap = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, newOpts);
                        } else {
                            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_disk_210, newOpts);
                        }
                    } else {
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_disk_210, newOpts);
                    }
                    if (bitmap != null) {
                        drawable = ImageUtils.createBlurredImageFromBitmap(bitmap, PlayingActivity.this.getApplication(), 3);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return drawable;
        }

        @Override
        protected void onPostExecute(Drawable result) {

            if (albumid != MusicPlayer.getCurrentAlbumId()) {
                this.cancel(true);
                return;
            }
            setDrawable(result);

        }

    }

    private void setDrawable(Drawable result) {
        if (result != null) {
            if (backAlbum.getDrawable() != null) {
                final TransitionDrawable td =
                        new TransitionDrawable(new Drawable[]{backAlbum.getDrawable(), result});


                backAlbum.setImageDrawable(td);
                //去除过度绘制
                td.setCrossFadeEnabled(true);
                td.startTransition(200);

            } else {
                backAlbum.setImageDrawable(result);
            }
        }
    }

    class FragmentAdapter extends FragmentStatePagerAdapter {

        private int mChildCount = 0;

        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            if (position == MusicPlayer.getQueue().length + 1 || position == 0) {
                return RoundFragment.newInstance("");
            }
            // return RoundFragment.newInstance(MusicPlayer.getQueue()[position - 1]);
            return RoundFragment.newInstance(MusicPlayer.getAlbumPathAll()[position - 1]);
        }

        @Override
        public int getCount() {
            //左右各加一个
            return MusicPlayer.getQueue().length + 2;
        }


        @Override
        public void notifyDataSetChanged() {
            mChildCount = getCount();
            super.notifyDataSetChanged();
        }

        @Override
        public int getItemPosition(Object object) {
            if (mChildCount > 0) {
                mChildCount--;
                return POSITION_NONE;
            }
            return super.getItemPosition(object);
        }

    }

    public class MyScroller extends Scroller {
        private int animTime = 390;

        public MyScroller(Context context) {
            super(context);
        }

        public MyScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, animTime);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            super.startScroll(startX, startY, dx, dy, animTime);
        }

        public void setmDuration(int animTime) {
            this.animTime = animTime;
        }
    }


}
