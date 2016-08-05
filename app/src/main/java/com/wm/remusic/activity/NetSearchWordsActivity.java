/*
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.wm.remusic.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.wm.remusic.R;
import com.wm.remusic.fragmentnet.SearchHotWordFragment;
import com.wm.remusic.fragmentnet.SearchTabPagerFragment;
import com.wm.remusic.fragmentnet.SearchWords;
import com.wm.remusic.provider.SearchHistory;
import com.wm.remusic.uitl.CommonUtils;

public class NetSearchWordsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, View.OnTouchListener, SearchWords {

    private SearchView mSearchView;
    private InputMethodManager mImm;
    private String queryString;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setPadding(0, CommonUtils.getStatusHeight(this), 0, 0);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SearchHotWordFragment f = new SearchHotWordFragment();
        f.searchWords(NetSearchWordsActivity.this);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.search_frame, f);
        ft.commit();

        mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search, menu);

        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));

        mSearchView.setOnQueryTextListener(this);
        mSearchView.setQueryHint(getResources().getString(R.string.search_net_music));

        mSearchView.setIconifiedByDefault(false);
        mSearchView.setIconified(false);

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.menu_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                finish();
                return false;
            }
        });

        menu.findItem(R.id.menu_search).expandActionView();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(final String query) {

        hideInputManager();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        SearchTabPagerFragment fragment = SearchTabPagerFragment.newInstance(0, query);
        ft.replace(R.id.search_frame, fragment).commitAllowingStateLoss();

        return true;
    }


    @Override
    public boolean onQueryTextChange(final String newText) {

        if (newText.equals(queryString)) {
            return true;
        }
        queryString = newText;
        if (!queryString.trim().equals("")) {
            //this.searchResults = new ArrayList();
            //List<MusicInfo> songList = SearchUtils.searchSongs(this, queryString);


            // searchResults.addAll((songList.size() < 10 ? songList : songList.subList(0, 10)));
        } else {
//            searchResults.clear();
//            adapter.updateSearchResults(searchResults);
//            adapter.notifyDataSetChanged();
        }

//        adapter.updateSearchResults(searchResults);
//        adapter.notifyDataSetChanged();

        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        hideInputManager();
        return false;
    }

    public void hideInputManager() {
        if (mSearchView != null) {
            if (mImm != null) {
                mImm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
            }
            mSearchView.clearFocus();

            SearchHistory.getInstance(this).addSearchString(mSearchView.getQuery().toString());
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        finish();
    }

    @Override
    public void onSearch(String t) {
        mSearchView.setQuery(t, true);
    }
}
