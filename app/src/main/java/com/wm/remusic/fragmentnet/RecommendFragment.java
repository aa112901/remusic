package com.wm.remusic.fragmentnet;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wm.remusic.MainApplication;
import com.wm.remusic.R;
import com.wm.remusic.activity.AlbumsDetailActivity;
import com.wm.remusic.activity.NetItemChangeActivity;
import com.wm.remusic.activity.PlaylistActivity;
import com.wm.remusic.activity.RadioDetailActivity;
import com.wm.remusic.json.GedanHotInfo;
import com.wm.remusic.json.RadioNetInfo;
import com.wm.remusic.json.RecommendListNewAlbumInfo;
import com.wm.remusic.json.RecommendListRadioInfo;
import com.wm.remusic.json.RecommendListRecommendInfo;
import com.wm.remusic.net.HttpUtil;
import com.wm.remusic.net.NetworkUtils;
import com.wm.remusic.uitl.PreferencesUtility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by wm on 2016/4/9.
 */
public class RecommendFragment extends Fragment {


    private RecyclerView recyclerView1, recyclerView2, recyclerView3;
    private GridLayoutManager gridLayoutManager, gridLayoutManager2, gridLayoutManager3;
    private RecommendAdapter recomendAdapter;
    private NewAlbumsAdapter newAlbumsAdapter;
    private RadioAdapter radioAdapter;

    private ArrayList<RecommendListRecommendInfo> mRecomendList = new ArrayList<>();
    private ArrayList<RecommendListNewAlbumInfo> mNewAlbumsList = new ArrayList<>();
    private ArrayList<RecommendListRadioInfo> mRadioList = new ArrayList<>();
    int width = 160, height = 160;

    LayoutInflater layoutInflater;
    View loadView, v1, v2, v3;
    LinearLayout itemChanged;
    HashMap<String, View> hashMap;
    String position;
    private ChangeView changeView;
    private boolean isFromCache = true;

    public void setChanger(ChangeView changer) {
        changeView = changer;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recommend, container, false);

        ImageButton privateFm = (ImageButton) view.findViewById(R.id.private_fm);

        layoutInflater = LayoutInflater.from(getContext());
        TextView dailyText = (TextView) view.findViewById(R.id.daily_text);
        dailyText.setText(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "");

        itemChanged = (LinearLayout) view.findViewById(R.id.item_change);
        linearLayout = (LinearLayout) view.findViewById(R.id.recommend_layout);
        loadView = layoutInflater.inflate(R.layout.loading, null, false);
        itemChanged.setVisibility(View.INVISIBLE);
        linearLayout.addView(loadView);

        recomendAdapter = new RecommendAdapter(null);
        newAlbumsAdapter = new NewAlbumsAdapter(null);
        radioAdapter = new RadioAdapter(null);

        reloadAdapter();

        TextView change = (TextView) view.findViewById(R.id.change_item_position);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent itent = new Intent(getContext(), NetItemChangeActivity.class);
                getActivity().startActivity(itent);
            }
        });

        return view;
    }

    LinearLayout linearLayout;


    public String getThisMonthDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cc2 = Calendar.getInstance();
        int maxMonthDay = cc2.getActualMaximum(Calendar.DAY_OF_MONTH);
        cc2.set(cc2.get(Calendar.YEAR), cc2.get(Calendar.MONTH), maxMonthDay, 23, 59, 59);
        String end = sdf.format(cc2.getTime());
        cc2.set(cc2.get(Calendar.YEAR), cc2.get(Calendar.MONTH), 1, 0, 0, 0);
        String start = sdf.format(cc2.getTime());
        return start + "|" + end;
    }


    private void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                if (NetworkUtils.isConnectInternet(getActivity())) {
                    isFromCache = false;
                }

                //推荐电台
                try {
                    JsonObject list = HttpUtil.getResposeJsonObject("http://tingapi.ting.baidu.com/v1/restserver/ting?from=android&version=5.8.1.0&channel=ppzs&operator=3&method=baidu.ting.plaza.index&cuid=89CF1E1A06826F9AB95A34DC0F6AAA14"
                            , getActivity(), isFromCache);

                    JsonObject object = list.get("result").getAsJsonObject();
                    JsonArray radioArray = object.get("radio").getAsJsonObject().get("result").getAsJsonArray();
                    JsonArray recommendArray = object.get("diy").getAsJsonObject().get("result").getAsJsonArray();
                    JsonArray newAlbumArray = object.get("mix_1").getAsJsonObject().get("result").getAsJsonArray();


                    for (int i = 0; i < 6; i++) {
                        mRecomendList.add(MainApplication.gsonInstance().fromJson(recommendArray.get(i), RecommendListRecommendInfo.class));
                        mNewAlbumsList.add(MainApplication.gsonInstance().fromJson(newAlbumArray.get(i), RecommendListNewAlbumInfo.class));
                        mRadioList.add(MainApplication.gsonInstance().fromJson(radioArray.get(i), RecommendListRadioInfo.class));
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void v) {

                v1 = layoutInflater.inflate(R.layout.recommend_playlist, linearLayout, false);
                recyclerView1 = (RecyclerView) v1.findViewById(R.id.recommend_playlist_recyclerview);
                gridLayoutManager = new GridLayoutManager(getActivity(), 3);
                recyclerView1.setLayoutManager(gridLayoutManager);
                recyclerView1.setAdapter(recomendAdapter);
                TextView more = (TextView) v1.findViewById(R.id.more);
                more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeView.changeTo(1);
                    }
                });


                v2 = layoutInflater.inflate(R.layout.recommend_newalbums, linearLayout, false);
                recyclerView2 = (RecyclerView) v2.findViewById(R.id.recommend_newalbums_recyclerview);
                gridLayoutManager2 = new GridLayoutManager(getActivity(), 3);
                recyclerView2.setLayoutManager(gridLayoutManager2);
                recyclerView2.setAdapter(newAlbumsAdapter);

                v3 = layoutInflater.inflate(R.layout.recommend_radio, linearLayout, false);
                recyclerView3 = (RecyclerView) v3.findViewById(R.id.recommend_radio_recyclerview);
                gridLayoutManager3 = new GridLayoutManager(getActivity(), 3);
                recyclerView3.setLayoutManager(gridLayoutManager3);
                recyclerView3.setAdapter(radioAdapter);


                recomendAdapter.update(mRecomendList);
                newAlbumsAdapter.update(mNewAlbumsList);
                radioAdapter.update(mRadioList);

                hashMap = new HashMap<>();
                hashMap.put("推荐歌单", v1);
                hashMap.put("最新专辑", v2);
                hashMap.put("主播电台", v3);
                position = PreferencesUtility.getInstance(getActivity()).getItemPosition();
                linearLayout.removeView(loadView);

                addViews();

                itemChanged.setVisibility(View.VISIBLE);

            }

        }.execute();
    }

    class LoadRecommend extends AsyncTask<Integer,Void,Integer>{
        @Override
        protected Integer doInBackground(Integer... params) {

            if (NetworkUtils.isConnectInternet(getActivity())) {
                isFromCache = false;
            }

            //推荐电台
            try {
                JsonObject list = HttpUtil.getResposeJsonObject("http://tingapi.ting.baidu.com/v1/restserver/ting?from=android&version=5.8.1.0&channel=ppzs&operator=3&method=baidu.ting.plaza.index&cuid=89CF1E1A06826F9AB95A34DC0F6AAA14"
                        , getActivity(), isFromCache);

                JsonObject object = list.get("result").getAsJsonObject();
                JsonArray radioArray = object.get("radio").getAsJsonObject().get("result").getAsJsonArray();
                JsonArray recommendArray = object.get("diy").getAsJsonObject().get("result").getAsJsonArray();
                JsonArray newAlbumArray = object.get("mix_1").getAsJsonObject().get("result").getAsJsonArray();


                for (int i = 0; i < 6; i++) {
                    mRecomendList.add(MainApplication.gsonInstance().fromJson(recommendArray.get(i), RecommendListRecommendInfo.class));
                    mNewAlbumsList.add(MainApplication.gsonInstance().fromJson(newAlbumArray.get(i), RecommendListNewAlbumInfo.class));
                    mRadioList.add(MainApplication.gsonInstance().fromJson(radioArray.get(i), RecommendListRadioInfo.class));
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            return params[0];
        }

        @Override
        protected void onPostExecute(Integer tryCount) {
            if(mRecomendList.size() != 6 && mNewAlbumsList.size() != 6 && mRadioList.size() != 6){
                if(tryCount < 5){
                    tryCount++;
                    new LoadRecommend().execute(tryCount);
                }else {
                    Toast.makeText(getContext(),"网络连接失败",Toast.LENGTH_SHORT).show();
                    View tryAgain = LayoutInflater.from(getContext()).inflate(R.layout.try_again,linearLayout,false);
                    tryAgain.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new LoadRecommend().execute(0);
                        }
                    });
                    linearLayout.removeView(loadView);
                    linearLayout.addView(tryAgain);
                }

            }

            v1 = layoutInflater.inflate(R.layout.recommend_playlist, linearLayout, false);
            recyclerView1 = (RecyclerView) v1.findViewById(R.id.recommend_playlist_recyclerview);
            gridLayoutManager = new GridLayoutManager(getActivity(), 3);
            recyclerView1.setLayoutManager(gridLayoutManager);
            recyclerView1.setAdapter(recomendAdapter);
            TextView more = (TextView) v1.findViewById(R.id.more);
            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeView.changeTo(1);
                }
            });


            v2 = layoutInflater.inflate(R.layout.recommend_newalbums, linearLayout, false);
            recyclerView2 = (RecyclerView) v2.findViewById(R.id.recommend_newalbums_recyclerview);
            gridLayoutManager2 = new GridLayoutManager(getActivity(), 3);
            recyclerView2.setLayoutManager(gridLayoutManager2);
            recyclerView2.setAdapter(newAlbumsAdapter);

            v3 = layoutInflater.inflate(R.layout.recommend_radio, linearLayout, false);
            recyclerView3 = (RecyclerView) v3.findViewById(R.id.recommend_radio_recyclerview);
            gridLayoutManager3 = new GridLayoutManager(getActivity(), 3);
            recyclerView3.setLayoutManager(gridLayoutManager3);
            recyclerView3.setAdapter(radioAdapter);


            recomendAdapter.update(mRecomendList);
            newAlbumsAdapter.update(mNewAlbumsList);
            radioAdapter.update(mRadioList);

            hashMap = new HashMap<>();
            hashMap.put("推荐歌单", v1);
            hashMap.put("最新专辑", v2);
            hashMap.put("主播电台", v3);
            position = PreferencesUtility.getInstance(getActivity()).getItemPosition();
            linearLayout.removeView(loadView);

            addViews();

            itemChanged.setVisibility(View.VISIBLE);

        }
    }

    public void addViews() {

        String[] strs = position.split(" ");
        for (int i = 0; i < strs.length; i++) {
            linearLayout.addView(hashMap.get(strs[i]));
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        if (position == null) {
            return;
        }
        String st = PreferencesUtility.getInstance(getActivity()).getItemPosition();
        if (!st.equals(position)) {
            position = st;
            linearLayout.removeAllViews();
            addViews();
        }

    }


    class RecommendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private ArrayList<RecommendListRecommendInfo> mList;
        SpannableString spanString;

        public RecommendAdapter(ArrayList<RecommendListRecommendInfo> list) {
            Bitmap b = BitmapFactory.decodeResource(getResources(), R.mipmap.index_icn_earphone);
            ImageSpan imgSpan = new ImageSpan(getActivity(), b, ImageSpan.ALIGN_BASELINE);
            spanString = new SpannableString("icon");
            spanString.setSpan(imgSpan, 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            mList = list;
        }

        public void update(ArrayList<RecommendListRecommendInfo> list) {
            mList = list;
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            ItemView viewholder = new ItemView(layoutInflater.inflate(R.layout.recommend_playlist_item, parent, false));

            return viewholder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final RecommendListRecommendInfo info = mList.get(position);

            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(info.getPic()))
                    .setResizeOptions(new ResizeOptions(width, height))
                    .build();

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(((ItemView) holder).art.getController())
                    .setImageRequest(request)
                    .build();

            ((ItemView) holder).art.setController(controller);


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
                    Intent intent = new Intent(getActivity(), PlaylistActivity.class);
                    intent.putExtra("playlistid", info.getListid());
                    intent.putExtra("islocal", false);
                    intent.putExtra("albumart", info.getPic());
                    intent.putExtra("playlistname", info.getTitle());
                    intent.putExtra("playlistDetail", info.getTag());
                    intent.putExtra("playlistcount", info.getListenum());

                    getActivity().startActivity(intent);
                }
            });

        }

        @Override
        public int getItemCount() {
            if (mList == null) {
                return 0;
            }

            if (mList.size() < 7) {
                return mList.size();
            } else {
                return 6;
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

    class RadioAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private ArrayList<RecommendListRadioInfo> mList;

        public RadioAdapter(ArrayList<RecommendListRadioInfo> list) {
            mList = list;
        }

        public void update(ArrayList<RecommendListRadioInfo> list) {
            mList = list;
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            ItemView viewholder = new ItemView(layoutInflater.inflate(R.layout.recommend_newalbums_item, parent, false));

            return viewholder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final RecommendListRadioInfo info = mList.get(position);

            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(info.getPic()))
                    .setResizeOptions(new ResizeOptions(width, height))
                    .build();

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(((ItemView) holder).art.getController())
                    .setImageRequest(request)
                    .build();

            ((ItemView) holder).art.setController(controller);


//            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(info.getPic()))
//                    .setResizeOptions(new ResizeOptions(width, height))
//                    .build();
//            DraweeController controller = Fresco.newDraweeControllerBuilder()
//                    .setOldController(((ItemView) holder).art.getController())
//                    .setImageRequest(request)
//                    .build();
//            ((ItemView) holder).art.setController(controller);


            ((ItemView) holder).albumName.setText(info.getTitle());
            ((ItemView) holder).artsit.setText(info.getDesc());
            ((ItemView) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(getActivity(), RadioDetailActivity.class);
                    intent.putExtra("albumid", info.getAlbum_id());
                    intent.putExtra("albumart", info.getPic());
                    intent.putExtra("albumname", info.getTitle());
                    intent.putExtra("artistname", info.getDesc());
                    getActivity().startActivity(intent);

                }
            });
        }

        @Override
        public int getItemCount() {
            if (mList == null) {
                return 0;
            }

            if (mList.size() < 7) {
                return mList.size();
            } else {
                return 6;
            }
        }

        class ItemView extends RecyclerView.ViewHolder implements View.OnClickListener {
            private SimpleDraweeView art;
            private TextView albumName, artsit;

            public ItemView(View itemView) {
                super(itemView);
                art = (SimpleDraweeView) itemView.findViewById(R.id.album_art);
                albumName = (TextView) itemView.findViewById(R.id.album_name);
                artsit = (TextView) itemView.findViewById(R.id.artist_name);
            }

            @Override
            public void onClick(View v) {

            }
        }

    }

    class NewAlbumsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private ArrayList<RecommendListNewAlbumInfo> mList;

        public NewAlbumsAdapter(ArrayList<RecommendListNewAlbumInfo> list) {
            mList = list;
        }

        public void update(ArrayList<RecommendListNewAlbumInfo> list) {
            mList = list;
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            ItemView viewholder = new ItemView(layoutInflater.inflate(R.layout.recommend_newalbums_item, parent, false));

            return viewholder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final RecommendListNewAlbumInfo info = mList.get(position);

//            ImageRequest imageRequest = ImageRequest.fromUri(info.coverImgUrl);
//            CacheKey cacheKey= DefaultCacheKeyFactory.getInstance()
//                    .getEncodedCacheKey(imageRequest);
//            BinaryResource resource = ImagePipelineFactory.getInstance()
//                    .getMainDiskStorageCache().getResource(cacheKey);
//            File file=((FileBinaryResource)resource).getFile();

            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(info.getPic()))
                    .setResizeOptions(new ResizeOptions(width, height))
                    .build();

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(((ItemView) holder).art.getController())
                    .setImageRequest(request)
                    .build();

            ((ItemView) holder).art.setController(controller);

//            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(info.coverImgUrl))
//                    .setResizeOptions(new ResizeOptions(width, height))
//                    .build();
//            DraweeController controller = Fresco.newDraweeControllerBuilder()
//                    .setOldController(((ItemView) holder).art.getController())
//                    .setImageRequest(request)
//                    .build();
//            ((ItemView) holder).art.setController(controller);


            ((ItemView) holder).albumName.setText(info.getTitle());
            ((ItemView) holder).artsit.setText(info.getAuthor());
            ((ItemView) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(getActivity(), AlbumsDetailActivity.class);
//                    intent.putExtra("albumid",info.getType_id());
//                    intent.putExtra("albumart",info.getPic());
//                    intent.putExtra("albumname",info.getTitle());
//                    intent.putExtra("artistname",info.getAuthor());

                    intent.putExtra("albumid", info.getType_id());
                    intent.putExtra("albumart", info.getPic());
                    intent.putExtra("albumname", info.getTitle());
                    intent.putExtra("albumdetail", info.getDesc());
                    // intent.putExtra("playlistcount",info.get);
                    getActivity().startActivity(intent);

//                    AlbumsDetail fragment = AlbumsDetail.newInstance(info.id, info.coverImgUrl, info.albumName,
//                            info.artistName, info.publishTime);
//                    FragmentTransaction transaction = ((AppCompatActivity) getContext()).getSupportFragmentManager().beginTransaction();
//                    transaction.replace(R.id.fragment_container, fragment);
//                    transaction.commitAllowingStateLoss();
                }
            });
        }

        @Override
        public int getItemCount() {
            if (mList == null) {
                return 0;
            }

            if (mList.size() < 7) {
                return mList.size();
            } else {
                return 6;
            }
        }

        class ItemView extends RecyclerView.ViewHolder implements View.OnClickListener {
            private SimpleDraweeView art;
            private TextView albumName, artsit;

            public ItemView(View itemView) {
                super(itemView);
                art = (SimpleDraweeView) itemView.findViewById(R.id.album_art);
                albumName = (TextView) itemView.findViewById(R.id.album_name);
                artsit = (TextView) itemView.findViewById(R.id.artist_name);
            }

            @Override
            public void onClick(View v) {

            }
        }

    }


}
