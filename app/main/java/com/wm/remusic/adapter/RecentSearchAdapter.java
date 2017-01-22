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

package com.wm.remusic.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wm.remusic.R;
import com.wm.remusic.fragmentnet.SearchWords;
import com.wm.remusic.provider.SearchHistory;

import java.util.ArrayList;

public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchAdapter.ItemHolder> {
    private Context mContext;
    private ArrayList<String> recentSearches = new ArrayList<>();
    private SearchWords searchWords;

    public RecentSearchAdapter(Activity context) {
        mContext = context;
        recentSearches = SearchHistory.getInstance(context).getRecentSearches();
    }

    public void setListenter(SearchWords search) {
        searchWords = search;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v0 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recent_search_item, null);
        ItemHolder ml0 = new ItemHolder(v0);
        return ml0;
    }

    @Override
    public void onBindViewHolder(final ItemHolder itemHolder, int i) {

        itemHolder.title.setText(recentSearches.get(i));
        setOnPopupMenuListener(itemHolder, i);
    }

    @Override
    public void onViewRecycled(ItemHolder itemHolder) {

    }

    @Override
    public int getItemCount() {
        return recentSearches.size();
    }

    private void setOnPopupMenuListener(ItemHolder itemHolder, final int position) {

        itemHolder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchHistory.getInstance(mContext).deleteRecentSearches(recentSearches.get(position));
                recentSearches = SearchHistory.getInstance(mContext).getRecentSearches();
                notifyDataSetChanged();
            }
        });
    }


    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title;
        ImageView menu;

        public ItemHolder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(R.id.title);
            this.menu = (ImageView) view.findViewById(R.id.menu);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (searchWords != null) {
                searchWords.onSearch(recentSearches.get(getAdapterPosition()));
            }

        }

    }
}





