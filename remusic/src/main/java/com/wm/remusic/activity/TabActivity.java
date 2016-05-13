package com.wm.remusic.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.wm.remusic.R;
import com.wm.remusic.fragment.AlbumFragment;
import com.wm.remusic.fragment.ArtistDetailFragment;
import com.wm.remusic.fragment.ArtistFragment;
import com.wm.remusic.fragment.FolderFragment;
import com.wm.remusic.fragment.MusicFragment;
import com.wm.remusic.fragment.PlayQueueFragment;
import com.wm.remusic.fragment.TabPagerFragment;
import com.wm.remusic.handler.HandlerUtil;
import com.wm.remusic.service.MusicPlayer;
import com.wm.remusic.uitl.CommonUtils;
import com.wm.remusic.uitl.IConstants;
import com.wm.remusic.uitl.MusicUtils;

import java.util.ArrayList;
import java.util.List;

import static com.wm.remusic.service.MusicPlayer.mService;

/**
 * Created by wm on 2016/4/11.
 */
public class TabActivity extends BaseActivity {

    private int page,artistId,albumId;
    private SimpleDraweeView navPlayImg;
    private TextView navMusicName,navArtist;
    private ProgressBar mProgress;
    private ImageView control;

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        //super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //        super.onSaveInstanceState(outState);
    }


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getExtras() != null) {
            page = getIntent().getIntExtra("page_number",0);
            artistId = getIntent().getIntExtra("artist",0);
            albumId = getIntent().getIntExtra("album",0);
        }
        setContentView(R.layout.activity_tab);
        if(artistId != 0){
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            ArtistDetailFragment fragment = ArtistDetailFragment.newInstance(artistId);
            transaction.hide(getSupportFragmentManager().findFragmentById(R.id.fragment_container));
            transaction.add(R.id.fragment_container, fragment);
            transaction.addToBackStack(null).commit();
        }
        if(albumId != 0){

        }



        String[] title = {"单曲","歌手", "专辑", "文件夹"};
        TabPagerFragment fragment = TabPagerFragment.newInstance(page,title);
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.add(R.id.tab_container, fragment);
                        transaction.commitAllowingStateLoss();


        navPlayImg = (SimpleDraweeView) findViewById(R.id.playbar_img);
        navMusicName = (TextView) findViewById(R.id.playbar_info);
        navArtist = (TextView) findViewById(R.id.playbar_singer);
        mProgress = (ProgressBar) findViewById(R.id.song_progress_normal);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mProgress.getLayoutParams();
        mProgress.measure(0, 0);
        layoutParams.setMargins(0, -20, 0, -(mProgress.getMeasuredHeight() / 2));
        mProgress.setLayoutParams(layoutParams);

        //	获取底部播放栏实例、绑定监听器

        LinearLayout nowPlay = (LinearLayout) findViewById(R.id.nav_play);
        nowPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicPlayer.getQueueSize() == 0) {
                    Toast.makeText(TabActivity.this, getResources().getString(R.string.queue_is_empty),
                            Toast.LENGTH_SHORT).show();
                } else {
                    HandlerUtil.getInstance(TabActivity.this).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(TabActivity.this, PlayingActivity.class);
                            intent.setAction(IConstants.NAVIGATE_NOWPLAYING);
                            startActivity(intent);
                        }
                    }, 60);

                }
            }
        });
        final ImageView playQueue = (ImageView) findViewById(R.id.play_list);
        playQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PlayQueueFragment playQueueFragment = new PlayQueueFragment();
                playQueueFragment.show(getSupportFragmentManager(), "playqueueframent");

            }
        });

        final ImageView next = (ImageView) findViewById(R.id.play_next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HandlerUtil.getInstance(TabActivity.this).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MusicPlayer.next();
                    }
                }, 100);
            }
        });

        control = (ImageView) findViewById(R.id.control);
        control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                control.setImageResource(MusicPlayer.isPlaying() ? R.drawable.playbar_btn_pause
                        : R.drawable.playbar_btn_play);

                if (MusicPlayer.getQueueSize() == 0) {
                    Toast.makeText(TabActivity.this, getResources().getString(R.string.queue_is_empty),
                            Toast.LENGTH_SHORT).show();
                } else {
                    HandlerUtil.getInstance(TabActivity.this).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MusicPlayer.playOrPause();
                        }
                    }, 100);
                }

            }
        });

    }

    public void updateTrackInfo() {
        if (mService == null) {
            return;
        }

        String data = MusicUtils.getalbumdata(this, MusicPlayer.getCurrentAudioId());


        if (data != null) {
            Uri uri1 = Uri.parse("file://" + data);
            navPlayImg.setImageURI(uri1);

        } else {
            Uri urr = Uri.parse("res:/" + R.drawable.placeholder_disk_210);
            navPlayImg.setImageURI(urr);
            // navPlayImg.setImageResource(R.drawable.placeholder_disk_210);
        }

        navMusicName.setText(MusicPlayer.getTrackName());
        navArtist.setText(MusicPlayer.getArtistName());
        mProgress.setMax((int) MusicPlayer.duration());
        mProgress.postDelayed(mUpdateProgress, 10);
        if (MusicPlayer.isPlaying()) {
            control.setImageResource(R.drawable.playbar_btn_pause);

        } else {
            control.setImageResource(R.drawable.playbar_btn_play);
        }
    }

    public Runnable mUpdateProgress = new Runnable() {

        @Override
        public void run() {

            long position = MusicPlayer.position();
            mProgress.setProgress((int) position);

            if (MusicPlayer.isPlaying()) {
                mProgress.postDelayed(mUpdateProgress, 50);
            } else mProgress.removeCallbacks(this);

        }
    };


}
