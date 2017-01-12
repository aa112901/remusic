package com.wm.remusic.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.bilibili.magicasakura.utils.ThemeUtils;
import com.wm.remusic.R;
import com.wm.remusic.fragment.DownFragment;
import com.wm.remusic.fragment.DownMusicFragment;
import com.wm.remusic.uitl.CommonUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by wm on 2016/4/12.
 */
public class DownActivity extends BaseActivity {


    Toolbar toolbar;
    ViewPager viewPager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down);
//        ButterKnife.bind(this);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setPadding(0, CommonUtils.getStatusHeight(DownActivity.this), 0, 0);
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.actionbar_back);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("下载管理");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        if (viewPager != null) {
            setupViewPager(viewPager);
            viewPager.setOffscreenPageLimit(2);
        }

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setTabTextColors(R.color.text_color, ThemeUtils.getThemeColorStateList(this, R.color.theme_color_primary).getDefaultColor());
        tabLayout.setSelectedTabIndicatorColor(ThemeUtils.getThemeColorStateList(this, R.color.theme_color_primary).getDefaultColor());
        tabLayout.setupWithViewPager(viewPager);


    }


    private void setupViewPager(ViewPager viewPager) {
        Adapter adapter = new Adapter(getSupportFragmentManager());
        adapter.addFragment(DownMusicFragment.newInstance("/storage/emulated/0/remusic", false, null), "单曲");
        adapter.addFragment(new DownFragment(), "下载中");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    static class Adapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }


}
