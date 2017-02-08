package com.wm.remusic.fragmentnet;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wm.remusic.MainApplication;
import com.wm.remusic.R;
import com.wm.remusic.activity.AlbumsDetailActivity;
import com.wm.remusic.activity.NetItemChangeActivity;
import com.wm.remusic.activity.PlaylistActivity;
import com.wm.remusic.activity.RadioDetailActivity;
import com.wm.remusic.fragment.AttachFragment;
import com.wm.remusic.json.RecommendListNewAlbumInfo;
import com.wm.remusic.json.RecommendListRadioInfo;
import com.wm.remusic.json.RecommendListRecommendInfo;
import com.wm.remusic.net.HttpUtil;
import com.wm.remusic.net.NetworkUtils;
import com.wm.remusic.uitl.PreferencesUtility;
import com.wm.remusic.widget.LoodView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by wm on 2016/4/9.
 */
public class RecommendFragment extends AttachFragment {


    private RecyclerView mRecyclerView1, mRecyclerView2, mRecyclerView3;
    private GridLayoutManager mGridLayoutManager, mGridLayoutManager2, mGridLayoutManager3;
    private RecommendAdapter mRecomendAdapter;
    private NewAlbumsAdapter mNewAlbumsAdapter;
    private RadioAdapter mRadioAdapter;

    private ArrayList<RecommendListRecommendInfo> mRecomendList = new ArrayList<>();
    private ArrayList<RecommendListNewAlbumInfo> mNewAlbumsList = new ArrayList<>();
    private ArrayList<RecommendListRadioInfo> mRadioList = new ArrayList<>();
    private int width = 160, height = 160;
    private LinearLayout mItemLayout ,mViewContent;;
    private LayoutInflater mLayoutInflater;
    private View mLoadView, v1, v2, v3;
    private HashMap<String, View> mViewHashMap;
    private String mPosition;
    private ChangeView mChangeView;
    private boolean isFromCache = true;
    private boolean isDayFirst;
    private ViewGroup mContent;
    private View mRecommendView;
    private LoodView mLoodView;

    public void setChanger(ChangeView changer) {
        mChangeView = changer;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContent = (ViewGroup) inflater.inflate(R.layout.fragment_recommend_container, container, false);

        mLayoutInflater = LayoutInflater.from(mContext);
        mRecommendView = mLayoutInflater.inflate(R.layout.recommend,container,false);
        String date = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "";
        TextView dailyText = (TextView) mRecommendView.findViewById(R.id.daily_text);
        dailyText.setText(date);
        mItemLayout = (LinearLayout) mRecommendView.findViewById(R.id.item_change);
        mViewContent = (LinearLayout) mRecommendView.findViewById(R.id.recommend_layout);
        if(!PreferencesUtility.getInstance(mContext).isCurrentDayFirst(date)){
            PreferencesUtility.getInstance(mContext).setCurrentDate(date);
            View dayRec = mLayoutInflater.inflate(R.layout.loading_daymusic,container,false);
            ImageView view1 = (ImageView) dayRec.findViewById(R.id.loading_dayimage) ;
            RotateAnimation rotateAnimation = new RotateAnimation(0,360, 1, 0.5F, 1, 0.5F );
            rotateAnimation.setDuration(20000L);
            rotateAnimation.setInterpolator(new LinearInterpolator());
            rotateAnimation.setRepeatCount(Animation.INFINITE);
            rotateAnimation.setRepeatMode(Animation.INFINITE);
            view1.startAnimation(rotateAnimation);
            isDayFirst = true;
            mContent.addView(dayRec);
        }

        mLoadView = mLayoutInflater.inflate(R.layout.loading, null, false);
        mItemLayout.setVisibility(View.INVISIBLE);
        mViewContent.addView(mLoadView);

        mRecomendAdapter = new RecommendAdapter(null);
        mNewAlbumsAdapter = new NewAlbumsAdapter(null);
        mRadioAdapter = new RadioAdapter(null);

        TextView change = (TextView) mRecommendView.findViewById(R.id.change_item_position);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent itent = new Intent(mContext, NetItemChangeActivity.class);
                mContext.startActivity(itent);
            }
        });

        mLoodView = (LoodView) mRecommendView.findViewById(R.id.loop_view);
        if(!isDayFirst){
            mContent.addView(mRecommendView);
        }

        return mContent;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            if(mLoodView != null)
            mLoodView.requestFocus();
        }
    }

    public void requestData(){
        reloadAdapter();
    }

    private void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                if (NetworkUtils.isConnectInternet(mContext)) {
                    isFromCache = false;
                }

                //推荐电台
                try {
                    JsonObject list = HttpUtil.getResposeJsonObject("http://tingapi.ting.baidu.com/v1/restserver/ting?from=android&version=5.8.1.0&channel=ppzs&operator=3&method=baidu.ting.plaza.index&cuid=89CF1E1A06826F9AB95A34DC0F6AAA14"
                            , mContext, isFromCache);

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

                v1 = mLayoutInflater.inflate(R.layout.recommend_playlist, mViewContent, false);

                mRecyclerView1 = (RecyclerView) v1.findViewById(R.id.recommend_playlist_recyclerview);
                mGridLayoutManager = new GridLayoutManager(mContext, 3);
                mRecyclerView1.setLayoutManager(mGridLayoutManager);
                mRecyclerView1.setAdapter(mRecomendAdapter);
                mRecyclerView1.setHasFixedSize(true);
                TextView more = (TextView) v1.findViewById(R.id.more);
                more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mChangeView.changeTo(1);
                    }
                });


                v2 = mLayoutInflater.inflate(R.layout.recommend_newalbums, mViewContent, false);
                mRecyclerView2 = (RecyclerView) v2.findViewById(R.id.recommend_newalbums_recyclerview);
                mGridLayoutManager2 = new GridLayoutManager(mContext, 3);
                mRecyclerView2.setLayoutManager(mGridLayoutManager2);
                mRecyclerView2.setAdapter(mNewAlbumsAdapter);
                mRecyclerView2.setHasFixedSize(true);

                v3 = mLayoutInflater.inflate(R.layout.recommend_radio, mViewContent, false);
                mRecyclerView3 = (RecyclerView) v3.findViewById(R.id.recommend_radio_recyclerview);
                mGridLayoutManager3 = new GridLayoutManager(mContext, 3);
                mRecyclerView3.setLayoutManager(mGridLayoutManager3);
                mRecyclerView3.setAdapter(mRadioAdapter);
                mRecyclerView3.setHasFixedSize(true);

                mRecomendAdapter.update(mRecomendList);
                mNewAlbumsAdapter.update(mNewAlbumsList);
                mRadioAdapter.update(mRadioList);

                mViewHashMap = new HashMap<>();
                mViewHashMap.put("推荐歌单", v1);
                mViewHashMap.put("最新专辑", v2);
                mViewHashMap.put("主播电台", v3);
                mPosition = PreferencesUtility.getInstance(mContext).getItemPosition();
                mViewContent.removeView(mLoadView);
                if(isDayFirst){
                    mContent.removeAllViews();
                    mContent.addView(mRecommendView);
                }

                addViews();

                mItemLayout.setVisibility(View.VISIBLE);

            }

        }.execute();
    }



    class LoadRecommend extends AsyncTask<Integer, Void, Integer> {
        @Override
        protected Integer doInBackground(Integer... params) {

            if (NetworkUtils.isConnectInternet(mContext)) {
                isFromCache = false;
            }

            //推荐电台
            try {
                JsonObject list = HttpUtil.getResposeJsonObject("http://tingapi.ting.baidu.com/v1/restserver/ting?from=android&version=5.8.1.0&channel=ppzs&operator=3&method=baidu.ting.plaza.index&cuid=89CF1E1A06826F9AB95A34DC0F6AAA14"
                        , mContext, isFromCache);

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
            if (mRecomendList.size() != 6 && mNewAlbumsList.size() != 6 && mRadioList.size() != 6) {
                if (tryCount < 5) {
                    tryCount++;
                    new LoadRecommend().execute(tryCount);
                } else {
                    Toast.makeText(mContext, "网络连接失败", Toast.LENGTH_SHORT).show();
                    View tryAgain = LayoutInflater.from(mContext).inflate(R.layout.try_again, mViewContent, false);
                    tryAgain.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new LoadRecommend().execute(0);
                        }
                    });
                    mViewContent.removeView(mLoadView);
                    mViewContent.addView(tryAgain);
                }

            }

            v1 = mLayoutInflater.inflate(R.layout.recommend_playlist, mViewContent, false);
            mRecyclerView1 = (RecyclerView) v1.findViewById(R.id.recommend_playlist_recyclerview);
            mGridLayoutManager = new GridLayoutManager(mContext, 3);
            mRecyclerView1.setLayoutManager(mGridLayoutManager);
            mRecyclerView1.setAdapter(mRecomendAdapter);
            TextView more = (TextView) v1.findViewById(R.id.more);
            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mChangeView.changeTo(1);
                }
            });


            v2 = mLayoutInflater.inflate(R.layout.recommend_newalbums, mViewContent, false);
            mRecyclerView2 = (RecyclerView) v2.findViewById(R.id.recommend_newalbums_recyclerview);
            mGridLayoutManager2 = new GridLayoutManager(mContext, 3);
            mRecyclerView2.setLayoutManager(mGridLayoutManager2);
            mRecyclerView2.setAdapter(mNewAlbumsAdapter);

            v3 = mLayoutInflater.inflate(R.layout.recommend_radio, mViewContent, false);
            mRecyclerView3 = (RecyclerView) v3.findViewById(R.id.recommend_radio_recyclerview);
            mGridLayoutManager3 = new GridLayoutManager(mContext, 3);
            mRecyclerView3.setLayoutManager(mGridLayoutManager3);
            mRecyclerView3.setAdapter(mRadioAdapter);


            mRecomendAdapter.update(mRecomendList);
            mNewAlbumsAdapter.update(mNewAlbumsList);
            mRadioAdapter.update(mRadioList);

            mViewHashMap = new HashMap<>();
            mViewHashMap.put("推荐歌单", v1);
            mViewHashMap.put("最新专辑", v2);
            mViewHashMap.put("主播电台", v3);
            mPosition = PreferencesUtility.getInstance(mContext).getItemPosition();
            mViewContent.removeView(mLoadView);

            addViews();

            mItemLayout.setVisibility(View.VISIBLE);

        }
    }

    private void addViews() {

        String[] strs = mPosition.split(" ");
        for (int i = 0; i < strs.length; i++) {
            mViewContent.addView(mViewHashMap.get(strs[i]));
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        if (mPosition == null) {
            return;
        }
        String st = PreferencesUtility.getInstance(mContext).getItemPosition();
        if (!st.equals(mPosition)) {
            mPosition = st;
            mViewContent.removeAllViews();
            addViews();
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mLoodView.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    class RecommendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private ArrayList<RecommendListRecommendInfo> mList;
        SpannableString spanString;

        public RecommendAdapter(ArrayList<RecommendListRecommendInfo> list) {
            Bitmap b = BitmapFactory.decodeResource(getResources(), R.mipmap.index_icn_earphone);
            ImageSpan imgSpan = new ImageSpan(mContext, b, ImageSpan.ALIGN_BASELINE);
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
                    Intent intent = new Intent(mContext, PlaylistActivity.class);
                    intent.putExtra("playlistid", info.getListid());
                    intent.putExtra("islocal", false);
                    intent.putExtra("albumart", info.getPic());
                    intent.putExtra("playlistname", info.getTitle());
                    intent.putExtra("playlistDetail", info.getTag());
                    intent.putExtra("playlistcount", info.getListenum());

                    mContext.startActivity(intent);
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

                    Intent intent = new Intent(mContext, RadioDetailActivity.class);
                    intent.putExtra("albumid", info.getAlbum_id());
                    intent.putExtra("albumart", info.getPic());
                    intent.putExtra("albumname", info.getTitle());
                    intent.putExtra("artistname", info.getDesc());
                    mContext.startActivity(intent);

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

                    Intent intent = new Intent(mContext, AlbumsDetailActivity.class);
//                    intent.putExtra("albumid",info.getType_id());
//                    intent.putExtra("albumart",info.getPic());
//                    intent.putExtra("albumname",info.getTitle());
//                    intent.putExtra("artistname",info.getAuthor());

                    intent.putExtra("albumid", info.getType_id());
                    intent.putExtra("albumart", info.getPic());
                    intent.putExtra("albumname", info.getTitle());
                    intent.putExtra("albumdetail", info.getDesc());
                    // intent.putExtra("playlistcount",info.get);
                    mContext.startActivity(intent);

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
