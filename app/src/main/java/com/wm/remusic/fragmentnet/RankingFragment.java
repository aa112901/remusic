package com.wm.remusic.fragmentnet;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wm.remusic.R;
import com.wm.remusic.activity.RankPlaylistActivity;
import com.wm.remusic.fragment.AttachFragment;
import com.wm.remusic.json.BillboardInfo;
import com.wm.remusic.net.BMA;
import com.wm.remusic.net.HttpUtil;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wm on 2016/5/14.
 */
public class RankingFragment extends AttachFragment {

    //新歌榜
    public static int BILLBOARD_NEW_MUSIC = 1;
    //原创音乐榜
    public static int BILLBOARD_ORIGINAL = 200;
    //热歌榜
    public static int BILLBOARD_HOT_MUSIC = 2;
    //欧美金曲榜
    public static int BILLBOARD_EU_UK = 21;
    //King榜
    public static int BILLBOARD_KING = 100;

    //华语金曲榜
    public static int BILLBOARD_NET_MUSIC = 25;
    //经典老哥榜
    public static int BILLBOARD_CLASSIC_OLD = 22;

    View view;
    FrameLayout frameLayout;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    RankingAdapter rankingAdapter;
    ArrayList<BillboardInfo> items = new ArrayList<>();
    int[] mBillList = new int[]{BILLBOARD_NEW_MUSIC, BILLBOARD_ORIGINAL, BILLBOARD_HOT_MUSIC
    , BILLBOARD_EU_UK , BILLBOARD_KING, BILLBOARD_NET_MUSIC};

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.ranking, null, false);
                recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
                linearLayoutManager = new LinearLayoutManager(mContext);
                recyclerView.setLayoutManager(linearLayoutManager);
                rankingAdapter = new RankingAdapter();
                recyclerView.setAdapter(rankingAdapter);
                recyclerView.setHasFixedSize(true);
                loadData();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.load_framelayout, container, false);
        frameLayout = (FrameLayout) view.findViewById(R.id.loadframe);
        View loadView = LayoutInflater.from(mContext).inflate(R.layout.loading, frameLayout, false);
        frameLayout.addView(loadView);

        return view;

    }


    public class MAsyncTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {

            JsonArray array = null;
            try {
                JsonObject jsonObject = HttpUtil.getResposeJsonObject(BMA.Billboard.billSongList(params[0], 0, 3));
                array = jsonObject.get("song_list").getAsJsonArray();

                for (int i = 0; i < array.size(); i++) {
                    BillboardInfo billboardInfo = new BillboardInfo();
                    billboardInfo.title = array.get(i).getAsJsonObject().get("title").toString();
                    billboardInfo.author = array.get(i).getAsJsonObject().get("author").toString();
                    billboardInfo.id = array.get(i).getAsJsonObject().get("artist_id").toString();
                    items.add(billboardInfo);
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            return null;
        }

    }


    private void loadData() {

        new MAsyncTask().execute(BILLBOARD_NEW_MUSIC);
        new MAsyncTask().execute(BILLBOARD_ORIGINAL);
        new MAsyncTask().execute(BILLBOARD_HOT_MUSIC);
        new MAsyncTask().execute(BILLBOARD_EU_UK);
        new MAsyncTask().execute(BILLBOARD_NET_MUSIC);
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    JsonObject jsonObject = HttpUtil.getResposeJsonObject(BMA.Billboard.billSongList(BILLBOARD_KING, 0, 3));
                    JsonArray array = jsonObject.get("song_list").getAsJsonArray();

                    for (int i = 0; i < array.size(); i++) {
                        BillboardInfo billboardInfo = new BillboardInfo();
                        billboardInfo.title = array.get(i).getAsJsonObject().get("title").toString();
                        billboardInfo.author = array.get(i).getAsJsonObject().get("author").toString();
                        billboardInfo.id = array.get(i).getAsJsonObject().get("artist_id").toString();
                        items.add(billboardInfo);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                rankingAdapter.updateAdapter(items);
                frameLayout.removeAllViews();
                frameLayout.addView(view);
            }
        }.execute();

    }

    class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.RankingViewHolder> {

        ArrayList<BillboardInfo> mList;
        int[] pic = {R.mipmap.ranklist_first, R.mipmap.ranklist_second, R.mipmap.ranklist_third
                , R.mipmap.ranklist_fifth, R.mipmap.ranklist_acg, R.mipmap.ranklist_six};

        public RankingAdapter() {

        }

        public void updateAdapter(ArrayList<BillboardInfo> list) {
            mList = list;
            this.notifyDataSetChanged();
        }

        @Override
        public RankingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new RankingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.ranking_recyclerview_adapter, parent, false));
        }


        @Override
        public void onBindViewHolder(RankingViewHolder holder, int position) {

            BillboardInfo billboardInfo1 = mList.get(position * 3);
            BillboardInfo billboardInfo2 = mList.get(position * 3 + 1);
            BillboardInfo billboardInfo3 = mList.get(position * 3 + 2);
            holder.textView1.setText(billboardInfo1.title + "-" + billboardInfo1.author);
            holder.textView2.setText(billboardInfo2.title + "-" + billboardInfo2.author);
            holder.textView3.setText(billboardInfo3.title + "-" + billboardInfo3.author);
            holder.draweeView.setImageResource(pic[position]);

        }

        @Override
        public int getItemCount() {
            return mList == null ? 0 : mList.size() / 3;
        }

        class RankingViewHolder extends RecyclerView.ViewHolder {
            SimpleDraweeView draweeView;
            TextView textView1, textView2, textView3;

            public RankingViewHolder(View itemView) {
                super(itemView);
                draweeView = (SimpleDraweeView) itemView.findViewById(R.id.item_image);
                textView1 = (TextView) itemView.findViewById(R.id.rank_first_txt);
                textView2 = (TextView) itemView.findViewById(R.id.rank_second_txt);
                textView3 = (TextView) itemView.findViewById(R.id.rank_third_txt);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(getAdapterPosition() > -1){
                            Intent intent = new Intent(mContext, RankPlaylistActivity.class);
                            intent.putExtra("type",mBillList[getAdapterPosition()]);
                            intent.putExtra("pic",pic[getAdapterPosition()]);
                            mContext.startActivity(intent);
                        }
                    }
                });
            }
        }
    }

}
