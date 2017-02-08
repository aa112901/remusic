package com.wm.remusic.activity;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.wm.remusic.service.MusicPlayer.getAlbumPath;


/**
 * Created by wm on 2016/2/21.
 */
public class PlayingActivity extends BaseActivity implements IConstants {
    private ImageView mBackAlbum, mPlayingmode, mControl, mNext, mPre, mPlaylist, mCmt, mFav, mDown, mMore, mNeedle;
    private TextView mTimePlayed, mDuration;
    private PlayerSeekBar mProgress;

    private ActionBar ab;
    private ObjectAnimator mNeedleAnim, mRotateAnim;
    private AnimatorSet mAnimatorSet;
    private AlbumViewPager mViewPager;
    private FragmentAdapter mAdapter;
    private BitmapFactory.Options mNewOpts;
    private View mActiveView;
    private PlaylistsManager mPlaylistsManager;
    private WeakReference<ObjectAnimator> animatorWeakReference;
    private WeakReference<View> mViewWeakReference = new WeakReference<View>(null);
    private boolean isFav = false;
    private boolean isNextOrPreSetPage = false; //判断viewpager由手动滑动 还是setcruuentitem换页
    private Toolbar toolbar;
    private FrameLayout mAlbumLayout;
    private RelativeLayout mLrcViewContainer;
    private LrcView mLrcView;
    private TextView mTryGetLrc;
    private LinearLayout mMusicTool;
    private SeekBar mVolumeSeek;
    private Handler mHandler;
    private Handler mPlayHandler;
    private static final int VIEWPAGER_SCROLL_TIME = 390;
    private static final int TIME_DELAY = 500;
    private static final int NEXT_MUSIC = 0;
    private static final int PRE_MUSIC = 1;
    private Bitmap mBitmap;
    private long lastAlbum;
    private PlayMusic mPlayThread;
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
        mPlaylistsManager = PlaylistsManager.getInstance(this);

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



        mAlbumLayout = (FrameLayout) findViewById(R.id.headerView);
        mLrcViewContainer = (RelativeLayout) findViewById(R.id.lrcviewContainer);
        mLrcView = (LrcView) findViewById(R.id.lrcview);
        mTryGetLrc = (TextView) findViewById(R.id.tragetlrc);
        mMusicTool = (LinearLayout) findViewById(R.id.music_tool);

        mBackAlbum = (ImageView) findViewById(R.id.albumArt);
        mPlayingmode = (ImageView) findViewById(R.id.playing_mode);
        mControl = (ImageView) findViewById(R.id.playing_play);
        mNext = (ImageView) findViewById(R.id.playing_next);
        mPre = (ImageView) findViewById(R.id.playing_pre);
        mPlaylist = (ImageView) findViewById(R.id.playing_playlist);
        mMore = (ImageView) findViewById(R.id.playing_more);
        mCmt = (ImageView) findViewById(R.id.playing_cmt);
        mFav = (ImageView) findViewById(R.id.playing_fav);
        mDown = (ImageView) findViewById(R.id.playing_down);
        mTimePlayed = (TextView) findViewById(R.id.music_duration_played);
        mDuration = (TextView) findViewById(R.id.music_duration);
        mProgress = (PlayerSeekBar) findViewById(R.id.play_seek);
        mNeedle = (ImageView) findViewById(R.id.needle);
        mViewPager = (AlbumViewPager) findViewById(R.id.view_pager);

        mNeedleAnim = ObjectAnimator.ofFloat(mNeedle, "rotation", -25, 0);
        mNeedleAnim.setDuration(200);
        mNeedleAnim.setRepeatMode(0);
        mNeedleAnim.setInterpolator(new LinearInterpolator());

        mVolumeSeek = (SeekBar) findViewById(R.id.volume_seek);
        mProgress.setIndeterminate(false);
        mProgress.setProgress(1);
        mProgress.setMax(1000);
        loadOther();
        setViewPager();
        initLrcView();
        mHandler = HandlerUtil.getInstance(this);

        mHandler.postDelayed(mUpAlbumRunnable, 0);
        mPlayThread = new PlayMusic();
        mPlayThread.start();
    }

    private void initLrcView() {
        mLrcView.setOnSeekToListener(onSeekToListener);
        mLrcView.setOnLrcClickListener(onLrcClickListener);
        mViewPager.setOnSingleTouchListener(new AlbumViewPager.OnSingleTouchListener() {
            @Override
            public void onSingleTouch(View v) {
                if (mAlbumLayout.getVisibility() == View.VISIBLE) {
                    mAlbumLayout.setVisibility(View.INVISIBLE);
                    mLrcViewContainer.setVisibility(View.VISIBLE);
                    mMusicTool.setVisibility(View.INVISIBLE);
                }
            }
        });
        mLrcViewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLrcViewContainer.getVisibility() == View.VISIBLE) {
                    mLrcViewContainer.setVisibility(View.INVISIBLE);
                    mAlbumLayout.setVisibility(View.VISIBLE);
                    mMusicTool.setVisibility(View.VISIBLE);
                }
            }
        });

        mTryGetLrc.setOnClickListener(new View.OnClickListener() {
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
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.ADJUST_SAME);
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

            if (mLrcViewContainer.getVisibility() == View.VISIBLE) {
                mLrcViewContainer.setVisibility(View.INVISIBLE);
                mAlbumLayout.setVisibility(View.VISIBLE);
                mMusicTool.setVisibility(View.VISIBLE);
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

        setSeekBarListener();
        setTools();

    }

    private void setViewPager() {
        mViewPager.setOffscreenPageLimit(2);
        PlaybarPagerTransformer transformer = new PlaybarPagerTransformer();
        mAdapter = new FragmentAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setPageTransformer(true, transformer);

      // 改变viewpager动画时间
        try {
            Field mField = ViewPager.class.getDeclaredField("mScroller");
            mField.setAccessible(true);
            MyScroller mScroller = new MyScroller(mViewPager.getContext().getApplicationContext(), new LinearInterpolator());
            mField.set(mViewPager, mScroller);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

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

                    if (!isNextOrPreSetPage) {
                        if (pPosition < MusicPlayer.getQueuePosition() + 1) {
//                            HandlerUtil.getInstance(PlayingActivity.this).postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                  //  MusicPlayer.previous(PlayingActivity.this, true);
//                                    Message msg = new Message();
//                                    msg.what = 0;
//                                    mPlayHandler.sendMessage(msg);
//                                }
//                            }, 500);

                            Message msg = new Message();
                            msg.what = PRE_MUSIC;
                            mPlayHandler.sendMessageDelayed(msg,TIME_DELAY);


                        } else if (pPosition > MusicPlayer.getQueuePosition() + 1) {
//                            HandlerUtil.getInstance(PlayingActivity.this).postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                  //  MusicPlayer.mNext();
//
//
//                                }
//                            }, 500);

                            Message msg = new Message();
                            msg.what = NEXT_MUSIC;
                            mPlayHandler.sendMessageDelayed(msg,TIME_DELAY);

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
        mPlayingmode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayer.cycleRepeat();
                updatePlaymode();
            }
        });

        mPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // MusicPlayer.previous(PlayingActivity.this.getApplication(), true);

                Message msg = new Message();
                msg.what = PRE_MUSIC;
                mPlayHandler.sendMessage(msg);
            }
        });

        mControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (MusicPlayer.isPlaying()) {
                    mControl.setImageResource(R.drawable.play_rdi_btn_pause);
                } else {
                    mControl.setImageResource(R.drawable.play_rdi_btn_play);
                }
                if (MusicPlayer.getQueueSize() != 0) {
                    MusicPlayer.playOrPause();
                }
            }
        });

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRotateAnim != null) {
                    mRotateAnim.end();
                    mRotateAnim = null;
                }
//                mHandler.removeCallbacks(mNextRunnable);
//                mHandler.postDelayed(mNextRunnable,300);
                Message msg = new Message();
                msg.what = NEXT_MUSIC;
                mPlayHandler.sendMessage(msg);

             //   MusicPlayer.mNext();
            }
        });

        mPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayQueueFragment playQueueFragment = new PlayQueueFragment();
                playQueueFragment.show(getSupportFragmentManager(), "playlistframent");
            }
        });

        mMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SimpleMoreFragment moreFragment = SimpleMoreFragment.newInstance(MusicPlayer.getCurrentAudioId());
                moreFragment.show(getSupportFragmentManager(), "music");
            }
        });

        mFav.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (isFav) {
                    mPlaylistsManager.removeItem(PlayingActivity.this, IConstants.FAV_PLAYLIST,
                            MusicPlayer.getCurrentAudioId());
                    mFav.setImageResource(R.drawable.play_rdi_icn_love);
                    isFav = false;
                } else {
                    try {
                        MusicInfo info = MusicPlayer.getPlayinfos().get(MusicPlayer.getCurrentAudioId());
                        mPlaylistsManager.insertMusic(PlayingActivity.this, IConstants.FAV_PLAYLIST, info);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mFav.setImageResource(R.drawable.play_icn_loved);
                    isFav = true;
                }

                Intent intent = new Intent(IConstants.PLAYLIST_COUNT_CHANGED);
                sendBroadcast(intent);
            }
        });


    }

    Runnable mNextRunnable = new Runnable() {
        @Override
        public void run() {
            MusicPlayer.next();
        }
    };

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
            mPlayingmode.setImageResource(R.drawable.play_icn_shuffle);
            Toast.makeText(PlayingActivity.this.getApplication(), getResources().getString(R.string.random_play),
                    Toast.LENGTH_SHORT).show();
        } else {
            switch (MusicPlayer.getRepeatMode()) {
                case MediaService.REPEAT_ALL:
                    mPlayingmode.setImageResource(R.drawable.play_icn_loop);
                    Toast.makeText(PlayingActivity.this.getApplication(), getResources().getString(R.string.loop_play),
                            Toast.LENGTH_SHORT).show();
                    break;
                case MediaService.REPEAT_CURRENT:
                    mPlayingmode.setImageResource(R.drawable.play_icn_one);
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
        lastAlbum = -1;
        if(MusicPlayer.isTrackLocal())
            updateBuffer(100);
        else {
            updateBuffer(MusicPlayer.secondPosition());
        }
        mHandler.postDelayed(mUpdateProgress,0);
    }


    public void updateQueue() {
        if (MusicPlayer.getQueueSize() == 0) {
            MusicPlayer.stop();
            finish();
            return;
        }
        mAdapter.notifyDataSetChanged();
        mViewPager.setCurrentItem(MusicPlayer.getQueuePosition() + 1, false);
    }

    private void updateFav(boolean b) {
        if (b) {
            mFav.setImageResource(R.drawable.play_icn_loved);
        } else {
            mFav.setImageResource(R.drawable.play_rdi_icn_love);
        }
    }

    public void updateLrc() {
        List<LrcRow> list = getLrcRows();
        if (list != null && list.size() > 0) {
            mTryGetLrc.setVisibility(View.INVISIBLE);
            mLrcView.setLrcRows(list);
        } else {
            mTryGetLrc.setVisibility(View.VISIBLE);
            mLrcView.reset();
        }
    }

    public void updateTrack() {
        mHandler.removeCallbacks(mUpAlbumRunnable);
        if(MusicPlayer.getCurrentAlbumId() != lastAlbum)
        mHandler.postDelayed(mUpAlbumRunnable, 1600);


        isFav = false;
        long[] favlists = mPlaylistsManager.getPlaylistIds(IConstants.FAV_PLAYLIST);
        long currentid = MusicPlayer.getCurrentAudioId();
        for (long i : favlists) {
            if (currentid == i) {
                isFav = true;
                break;
            }
        }
        updateFav(isFav);
        updateLrc();


        ab.setTitle(MusicPlayer.getTrackName());
        ab.setSubtitle(MusicPlayer.getArtistName());
        mDuration.setText(MusicUtils.makeShortTimeString(PlayingActivity.this.getApplication(), MusicPlayer.duration() / 1000));
    }

    private Runnable mUpAlbumRunnable = new Runnable() {
        @Override
        public void run() {
            new setBlurredAlbumArt().execute();
        }
    };

    public void updateTrackInfo() {

        if (MusicPlayer.getQueueSize() == 0) {
            return;
        }

        Fragment fragment = (RoundFragment) mViewPager.getAdapter().instantiateItem(mViewPager, mViewPager.getCurrentItem());
        if (fragment != null) {
            View v = fragment.getView();
            if(mViewWeakReference.get() != v && v != null){
                ((ViewGroup) v).setAnimationCacheEnabled(false);
                if (mViewWeakReference != null)
                    mViewWeakReference.clear();
                mViewWeakReference = new WeakReference<View>(v);
                mActiveView = mViewWeakReference.get();
            }
        }

        if (mActiveView != null) {
            //            animatorWeakReference = new WeakReference<>((ObjectAnimator) mActiveView.getTag(R.id.tag_animator));
            //            mRotateAnim = animatorWeakReference.get();
            mRotateAnim = (ObjectAnimator) mActiveView.getTag(R.id.tag_animator);
        }

        //mProgress.setMax((int) MusicPlayer.mDuration());


        mAnimatorSet = new AnimatorSet();
        if (MusicPlayer.isPlaying()) {
            mProgress.removeCallbacks(mUpdateProgress);
            mProgress.postDelayed(mUpdateProgress, 200);
            mControl.setImageResource(R.drawable.play_rdi_btn_pause);
            if (mAnimatorSet != null && mRotateAnim != null && !mRotateAnim.isRunning()) {
                //修复从playactivity回到Main界面null
                if (mNeedleAnim == null) {
                    mNeedleAnim = ObjectAnimator.ofFloat(mNeedle, "rotation", -30, 0);
                    mNeedleAnim.setDuration(200);
                    mNeedleAnim.setRepeatMode(0);
                    mNeedleAnim.setInterpolator(new LinearInterpolator());
                }
                mAnimatorSet.play(mNeedleAnim).before(mRotateAnim);
                mAnimatorSet.start();
            }

        } else {
            mProgress.removeCallbacks(mUpdateProgress);
            mControl.setImageResource(R.drawable.play_rdi_btn_play);
            if (mNeedleAnim != null) {
                mNeedleAnim.reverse();
                mNeedleAnim.end();
            }

            if (mRotateAnim != null && mRotateAnim.isRunning()) {
                mRotateAnim.cancel();
                float valueAvatar = (float) mRotateAnim.getAnimatedValue();
                mRotateAnim.setFloatValues(valueAvatar, 360f + valueAvatar);
            }
        }

        isNextOrPreSetPage = false;
        if (MusicPlayer.getQueuePosition() + 1 != mViewPager.getCurrentItem()) {
            mViewPager.setCurrentItem(MusicPlayer.getQueuePosition() + 1);
            isNextOrPreSetPage = true;
        }

    }

    @Override
    public void updateBuffer(int p) {
        mProgress.setSecondaryProgress(p*10);
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
                if (duration > 0 && duration < 627080716){
                    mProgress.setProgress((int) (1000 * position / duration));
                    mTimePlayed.setText(MusicUtils.makeTimeString( position ));
                }

                if (MusicPlayer.isPlaying()) {
                    mProgress.postDelayed(mUpdateProgress, 200);
                }else {
                    mProgress.removeCallbacks(mUpdateProgress);
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
                    i = (int) (i * MusicPlayer.duration() / 1000);
                    mLrcView.seekTo(i, true, b);
                    if (b) {
                        MusicPlayer.seek((long)i);
                        mTimePlayed.setText(MusicUtils.makeTimeString( i ));
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
        mActiveView = null;

        if (mRotateAnim != null) {
            mRotateAnim.end();
            mRotateAnim = null;
        }
        if (mNeedleAnim != null) {
            mNeedleAnim.end();
            mNeedleAnim = null;
        }
        if (mAnimatorSet != null) {
            mAnimatorSet.end();
            mAnimatorSet = null;
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
        mPlayHandler.removeCallbacksAndMessages(null);
        mPlayHandler.getLooper().quit();
        mPlayHandler = null;

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
                    mRotateAnim = (ObjectAnimator) view.getTag(R.id.tag_animator);
                    if (mRotateAnim != null && !mRotateAnim.isRunning() && mNeedleAnim != null) {
                        mAnimatorSet = new AnimatorSet();
                        mAnimatorSet.play(mNeedleAnim).before(mRotateAnim);
                        mAnimatorSet.start();
                    }
                }

            } else if (position == -1 || position == -2 || position == 1) {

                mRotateAnim = (ObjectAnimator) view.getTag(R.id.tag_animator);
                if (mRotateAnim != null) {
                    mRotateAnim.setFloatValues(0);
                    mRotateAnim.end();
                    mRotateAnim = null;
                }
            } else {

                if (mNeedleAnim != null) {
                    mNeedleAnim.reverse();
                    mNeedleAnim.end();
                }

                mRotateAnim = (ObjectAnimator) view.getTag(R.id.tag_animator);
                if (mRotateAnim != null) {
                    mRotateAnim.cancel();
                    float valueAvatar = (float) mRotateAnim.getAnimatedValue();
                    mRotateAnim.setFloatValues(valueAvatar, 360f + valueAvatar);

                }
            }
        }

    }


    private class setBlurredAlbumArt extends AsyncTask<Void, Void, Drawable> {

        long albumid = MusicPlayer.getCurrentAlbumId();

        @Override
        protected Drawable doInBackground(Void... loadedImage) {
            lastAlbum = albumid;
            Drawable drawable = null;
            mBitmap = null;
            if (mNewOpts == null) {
                mNewOpts = new BitmapFactory.Options();
                mNewOpts.inSampleSize = 6;
                mNewOpts.inPreferredConfig = Bitmap.Config.RGB_565;
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
                            bitmap = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, mNewOpts);
                        } else {
                            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_disk_210, mNewOpts);
                        }
                    } else {
                        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_disk_210, mNewOpts);
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
            if (mBackAlbum.getDrawable() != null) {
                final TransitionDrawable td =
                        new TransitionDrawable(new Drawable[]{mBackAlbum.getDrawable(), result});


                mBackAlbum.setImageDrawable(td);
                //去除过度绘制
                td.setCrossFadeEnabled(true);
                td.startTransition(200);

            } else {
                mBackAlbum.setImageDrawable(result);
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
        private int animTime = VIEWPAGER_SCROLL_TIME;

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


//    private Thread mPlayThread = new Thread(new Runnable() {
//        @Override
//        public void run() {
//            Looper.prepare();
//            mPlayHandler = new Handler(){
//                @Override
//                public void handleMessage(Message msg) {
//                    super.handleMessage(msg);
//                    switch (msg.what){
//                        case PRE_MUSIC:
//                            MusicPlayer.previous(PlayingActivity.this,true);
//                            break;
//                        case NEXT_MUSIC:
//                            MusicPlayer.next();
//                            break;
//                        case 3:
//                            MusicPlayer.setQueuePosition(msg.arg1);
//                            break;
//                    }
//
//
//                }
//            };
//
//            Looper.loop();
//        }
//    });

    public class PlayMusic extends Thread {
        public void run(){
                if(Looper.myLooper() == null)
                Looper.prepare();
                mPlayHandler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                            switch (msg.what){
                                case PRE_MUSIC:
                                    MusicPlayer.previous(PlayingActivity.this,true);
                                    break;
                                case NEXT_MUSIC:
                                    MusicPlayer.next();
                                    break;
                                case 3:
                                    MusicPlayer.setQueuePosition(msg.arg1);
                                    break;
                        }
                    }
                };

                Looper.loop();

        }
    }


}