package com.wm.remusic.activity;

import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.wm.remusic.R;
import com.wm.remusic.fragment.BitSetFragment;
import com.wm.remusic.fragment.MainFragment;
import com.wm.remusic.fragment.PlayQueueFragment;
import com.wm.remusic.fragmentnet.TabNetPagerFragment;
import com.wm.remusic.fragment.TimingFragment;
import com.wm.remusic.handler.HandlerUtil;
import com.wm.remusic.service.MusicPlayer;
import com.wm.remusic.uitl.IConstants;
import com.wm.remusic.uitl.MusicUtils;

import static com.wm.remusic.service.MusicPlayer.mService;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,
        ServiceConnection {
    private SimpleDraweeView navPlayImg;
    private TextView navMusicName,navArtist;
    private ProgressBar mProgress;
    private ActionBar ab;
    private ImageView barnet, barmusic,barfriends,search;
    private ImageView control;
    private DrawerLayout drawerLayout;
    LinearLayout nowPlay;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //        super.onSaveInstanceState(outState);
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




    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentTransaction  transaction = getSupportFragmentManager().beginTransaction();
        final MainFragment mfragment = new MainFragment();
        transaction.add(R.id.fragment_container, mfragment, "music").commitAllowingStateLoss();
        final  TabNetPagerFragment fragment = new TabNetPagerFragment();

        barnet = (ImageView) findViewById(R.id.bar_net);
        barmusic = (ImageView) findViewById(R.id.bar_music);
        barfriends = (ImageView) findViewById(R.id.bar_friends);
        search = (ImageView) findViewById(R.id.bar_search);
        barmusic = (ImageView) findViewById(R.id.bar_music);
        barmusic.setSelected(true);

        barnet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barmusic.setSelected(false);
                barnet.setSelected(true);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.hide(mfragment);
                if(getSupportFragmentManager().findFragmentByTag("net") == null){
                    transaction.add(R.id.fragment_container, fragment,"net");
                }else {
                    transaction.show(fragment);
                }
                transaction.commitAllowingStateLoss();
            }
        });
        barmusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barmusic.setSelected(true);
                barnet.setSelected(false);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.hide(fragment);
                transaction.show(mfragment);
                transaction.commitAllowingStateLoss();
            }
        });



        ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setTitle("");



//        SystemBarTintManager tintManager = new SystemBarTintManager(this);
//        // enable status bar tint
//        tintManager.setStatusBarTintEnabled(true);
//        // enable navigation bar tint
//        tintManager.setNavigationBarTintEnabled(true);
//        //tintManager.setTintColor(Color.parseColor("#00000000"));


        //	获取底部播放栏实例、绑定监听器
        nowPlay = (LinearLayout) findViewById(R.id.nav_play);
        navPlayImg = (SimpleDraweeView) findViewById(R.id.playbar_img);
        navMusicName = (TextView) findViewById(R.id.playbar_info);
        navArtist = (TextView) findViewById(R.id.playbar_singer);
        mProgress = (ProgressBar) findViewById(R.id.song_progress_normal);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mProgress.getLayoutParams();
        mProgress.measure(0, 0);
        layoutParams.setMargins(0, -20, 0, -(mProgress.getMeasuredHeight() / 2));
        mProgress.setLayoutParams(layoutParams);

        setListener();

        drawerLayout = (DrawerLayout) findViewById(R.id.fd);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, null, R.string.app_name, R.string.search);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationview = (NavigationView) findViewById(R.id.nav);
        navigationview.setClickable(true);
        navigationview.setNavigationItemSelectedListener(this);
        getWindow().setBackgroundDrawableResource(R.color.background_material_light_1);

    }

    private void setListener(){
        nowPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicPlayer.getQueueSize() == 0) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.queue_is_empty),
                            Toast.LENGTH_SHORT).show();
                } else {
                    HandlerUtil.getInstance(MainActivity.this).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(MainActivity.this, PlayingActivity.class);
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
                HandlerUtil.getInstance(MainActivity.this).postDelayed(new Runnable() {
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
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.queue_is_empty),
                            Toast.LENGTH_SHORT).show();
                } else {
                    HandlerUtil.getInstance(MainActivity.this).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MusicPlayer.playOrPause();
                        }
                    }, 100);
                }

            }
        });


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(MainActivity.this, NetSearchWordsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                MainActivity.this.startActivity(intent);
            }
        });

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


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {

            case R.id.timing_play:
                TimingFragment fragment = new TimingFragment();
                fragment.show(getSupportFragmentManager(), "timing");
                break;
            case R.id.action_exit:// 退出

                if (MusicPlayer.isPlaying()) {
                    MusicPlayer.playOrPause();
                }
                unbindService();
                finish();

            case R.id.down_bit:
                  BitSetFragment bfragment = new BitSetFragment();
                   bfragment.show(getSupportFragmentManager(),"bitset");

                break;

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.fd);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        //super.onRestoreInstanceState(savedInstanceState);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case android.R.id.home: //Menu icon
                drawerLayout.openDrawer(Gravity.LEFT);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }



//    @Override
//    public void down(String key) {
//        // 字母索引被按下时回调
//        if (map.get(key) != null) {
//            lv.setSelectionFromTop(map.get(key), 0);
//
//            text_select.setText(key);
//        }
//        Iv_select_bg.setVisibility(View.VISIBLE);
//    }
//
//    @Override
//    public void up() {
//        // 字母索引被松开时回调
//        Iv_select_bg.setVisibility(View.GONE);
//        text_select.setText(null);
//    }
//
//    @Override
//    public void move(String key) {
//        // 字母索引被按下并移动时回调
//        down(key);
//    }


     long time = 0;
    /**
     * 双击返回桌面
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - time > 1000)) {
                Toast.makeText(this, "再按一次返回桌面", Toast.LENGTH_SHORT).show();
                time = System.currentTimeMillis();
            } else {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        Intent startMain = new Intent(Intent.ACTION_MAIN);
//        startMain.addCategory(Intent.CATEGORY_HOME);
//        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(startMain);
//        moveTaskToBack(true);
        // System.exit(0);
        // finish();
    }
}
