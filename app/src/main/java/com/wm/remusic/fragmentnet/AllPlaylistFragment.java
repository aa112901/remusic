package com.wm.remusic.fragmentnet;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wm.remusic.R;
import com.wm.remusic.activity.PlaylistActivity;
import com.wm.remusic.fragment.AttachFragment;
import com.wm.remusic.json.GedanInfo;
import com.wm.remusic.net.BMA;
import com.wm.remusic.net.HttpUtil;

import java.util.ArrayList;

/**
 * Created by wm on 2016/5/15.
 */
public class AllPlaylistFragment extends AttachFragment {

    FrameLayout frameLayout;
    View view;
    private GridLayoutManager gridLayoutManager;
    private RecommendAdapter recomendAdapter;
    private RecyclerView recyclerView;
    private int lastVisibleItem;
    private ArrayList<GedanInfo> recommendList = new ArrayList<>();
    int pageCount = 1;
    Gson gson;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.load_framelayout, container, false);
        frameLayout = (FrameLayout) view.findViewById(R.id.loadframe);
        View loadView = LayoutInflater.from(mContext).inflate(R.layout.loading, frameLayout, false);
        frameLayout.addView(loadView);
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.recommend_all_playlist, frameLayout, false);
                recyclerView = (RecyclerView) view.findViewById(R.id.recommend_playlist_recyclerview);
                gridLayoutManager = new GridLayoutManager(mContext, 2);
                recyclerView.setLayoutManager(gridLayoutManager);
                recyclerView.setHasFixedSize(true);

                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 == recomendAdapter.getItemCount()) {
                            new MAsyncTask(++pageCount).execute();
                        }
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        lastVisibleItem = gridLayoutManager.findLastVisibleItemPosition();
                    }

                });


                loadData();

            }
        }
    }

    class MAsyncTask extends AsyncTask {

        private int next;

        public MAsyncTask(int next) {
            this.next = next;
        }

        @Override
        protected Object doInBackground(Object[] params) {

            JsonObject result = HttpUtil.getResposeJsonObject(BMA.GeDan.geDan(next, 10));
            if (result == null) {
                return null;
            }
            //热门歌单
            JsonArray pArray = result.get("content").getAsJsonArray();
            if (pArray == null) {
                return null;
            }

            int plen = pArray.size();

            for (int i = 0; i < plen; i++) {
                GedanInfo gedanInfo = gson.fromJson(pArray.get(i), GedanInfo.class);
                recommendList.add(gedanInfo);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            recomendAdapter.update(recommendList);
        }

    }

    private void loadData() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                gson = new Gson();
                JsonObject result = HttpUtil.getResposeJsonObject(BMA.GeDan.geDan(1, 10));
                if (result == null) {
                    return null;
                }
                //热门歌单
                JsonArray pArray = result.get("content").getAsJsonArray();
                if (pArray == null) {
                    return null;
                }

                int plen = pArray.size();

                for (int i = 0; i < plen; i++) {
                    GedanInfo GedanInfo = gson.fromJson(pArray.get(i), GedanInfo.class);
                    recommendList.add(GedanInfo);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                recomendAdapter = new RecommendAdapter(recommendList);
                recyclerView.setAdapter(recomendAdapter);
                frameLayout.removeAllViews();
                frameLayout.addView(view);

            }
        }.execute();

    }

    class RecommendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private ArrayList<GedanInfo> mList;
        public int TYPE_ITEM = 0;
        public int TYPE_FOOTER = 1;
        SpannableString spanString;

        public RecommendAdapter(ArrayList<GedanInfo> list) {
            mList = list;

            Bitmap b = BitmapFactory.decodeResource(getResources(), R.mipmap.index_icn_earphone);
            ImageSpan imgSpan = new ImageSpan(mContext, b, ImageSpan.ALIGN_BASELINE);
            spanString = new SpannableString("icon");
            spanString.setSpan(imgSpan, 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        public void update(ArrayList<GedanInfo> list) {
            mList = list;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            if (position + 1 == mList.size() + 1) {
                return TYPE_FOOTER;
            }
            return TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

            if (viewType == TYPE_ITEM) {
                return new ItemView(layoutInflater.inflate(R.layout.recommend_all_playlist_item, parent, false));
            } else {
                return new Footer(layoutInflater.inflate(R.layout.loading, parent, false));
            }

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            if (holder instanceof ItemView) {
                final GedanInfo info = mList.get(position);

                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(info.getPic_300()))
                        .setResizeOptions(new ResizeOptions(300, 300))
                        .build();

                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setOldController(((ItemView) holder).art.getController())
                        .setImageRequest(request)
                        .build();

                ((ItemView) holder).art.setController(controller);

                //((ItemView) holder).art.setImageURI(Uri.parse(info.getPic_300()));
                ((ItemView) holder).name.setText(info.getTitle());
                ((ItemView) holder).count.setText(spanString);

                int count = Integer.parseInt(info.getListenum());
                if (count > 10000) {
                    count = count / 10000;
                    ((ItemView) holder).count.append(" " + count + "万");
                } else {
                    ((ItemView) holder).count.append(" " + info.getListenum());
                }
                ((ItemView) holder).itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, PlaylistActivity.class);
                        intent.putExtra("playlistid", info.getListid());
                        intent.putExtra("islocal", false);
                        intent.putExtra("albumart", info.getPic_300());
                        intent.putExtra("playlistname", info.getTitle());
                        intent.putExtra("playlistDetail", info.getTag());
                        intent.putExtra("playlistcount", info.getListenum());
                        mContext.startActivity(intent);
                    }
                });
            }

        }

        @Override
        public int getItemCount() {
            if (mList == null) {
                return 0;
            }
            return mList.size() + 1;

        }

        class Footer extends RecyclerView.ViewHolder {

            public Footer(View itemView) {
                super(itemView);
            }
        }

        class ItemView extends RecyclerView.ViewHolder {
            private SimpleDraweeView art;
            private TextView name, count;

            public ItemView(View itemView) {
                super(itemView);
                art = (SimpleDraweeView) itemView.findViewById(R.id.playlist_art);
                name = (TextView) itemView.findViewById(R.id.playlist_name);
                count = (TextView) itemView.findViewById(R.id.playlist_listen_count);
            }
        }

    }


}
