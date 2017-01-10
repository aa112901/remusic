package com.wm.remusic.fragmentnet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.wm.remusic.R;
import com.wm.remusic.activity.ArtistDetailActivity;
import com.wm.remusic.fragment.AttachFragment;
import com.wm.remusic.json.SearchArtistInfo;
import com.wm.remusic.widget.DividerItemDecoration;

import java.util.ArrayList;

/**
 * Created by wm on 2016/5/18.
 */
public class SearchArtistFragment extends AttachFragment {

    private ArrayList<SearchArtistInfo> artistInfos;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private ArtistAdapter mAdapter;
    private RecyclerView.ItemDecoration itemDecoration;

    public static SearchArtistFragment newInstance(ArrayList<SearchArtistInfo> list) {
        SearchArtistFragment fragment = new SearchArtistFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("searchArtist", list);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recylerview, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new ArtistAdapter(null);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);
        setItemDecoration();
        loadArtists();

        return view;
    }


    //设置分割线
    private void setItemDecoration() {

        itemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
    }


    private void loadArtists() {

        if (getArguments() != null) {
            artistInfos = getArguments().getParcelableArrayList("searchArtist");
        }
        mAdapter = new ArtistAdapter(artistInfos);
        recyclerView.setAdapter(mAdapter);
        setItemDecoration();

    }


    public class ArtistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ArrayList<SearchArtistInfo> mList;

        public ArtistAdapter(ArrayList<SearchArtistInfo> list) {
            mList = list;
        }

        //更新adpter的数据
        public void updateDataSet(ArrayList<SearchArtistInfo> list) {
            this.mList = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_common_item, viewGroup, false);
            return new ListItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int i) {
            SearchArtistInfo model = mList.get(i);
            //设置条目状态
            ((ListItemViewHolder) holder).mainTitle.setText(model.getAuthor());
            ((ListItemViewHolder) holder).draweeView.setImageURI(Uri.parse(model.getAvatar_middle()));

        }

        @Override
        public int getItemCount() {
            return mList == null ? 0 : mList.size();
        }

        public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //ViewHolder
            SimpleDraweeView draweeView;
            TextView mainTitle, title;
            ImageView moreOverflow;

            ListItemViewHolder(View view) {
                super(view);
                this.mainTitle = (TextView) view.findViewById(R.id.viewpager_list_toptext);
                this.title = (TextView) view.findViewById(R.id.viewpager_list_bottom_text);
                this.draweeView = (SimpleDraweeView) view.findViewById(R.id.viewpager_list_img);
                this.moreOverflow = (ImageView) view.findViewById(R.id.viewpager_list_button);


                //为每个条目设置监听
                view.setOnClickListener(this);

            }

            //加载歌手专辑界面fragment
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ArtistDetailActivity.class);
                SearchArtistInfo model = mList.get(getAdapterPosition());
                intent.putExtra("artistid", model.getArtist_id());
                intent.putExtra("artistart", model.getAvatar_middle());
                intent.putExtra("artistname", model.getAuthor());
                intent.putExtra("artistUid", model.getTing_uid());
                mContext.startActivity(intent);
            }

        }
    }

}
