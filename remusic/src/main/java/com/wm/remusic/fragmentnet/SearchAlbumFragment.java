package com.wm.remusic.fragmentnet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wm.remusic.json.SearchAlbumInfo;
import com.wm.remusic.json.SearchSongInfo;

import java.util.ArrayList;

/**
 * Created by wm on 2016/5/18.
 */
public class SearchAlbumFragment extends Fragment {

    public static SearchAlbumFragment newInstance(ArrayList<SearchAlbumInfo> list){
        SearchAlbumFragment fragment = new SearchAlbumFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("searchMusic",list);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }



}
