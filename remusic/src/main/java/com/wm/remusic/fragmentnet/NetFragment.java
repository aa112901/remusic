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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.wm.remusic.R;
import com.wm.remusic.activity.AlbumsDetailActivity;
import com.wm.remusic.activity.NetPlaylistDetailActivity;
import com.wm.remusic.json.BillboardItem;
import com.wm.remusic.activity.NetItemChangeActivity;
import com.wm.remusic.json.GedanHot;
import com.wm.remusic.json.RadioNet;
import com.wm.remusic.net.BMA;
import com.wm.remusic.net.HttpUtil;
import com.wm.remusic.net.NetworkUtils;
import com.wm.remusic.uitl.ImageUtils;
import com.wm.remusic.uitl.PreferencesUtility;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wm on 2016/4/9.
 */
public class NetFragment extends Fragment {



    private RecyclerView recyclerView1,recyclerView2,recyclerView3;
    private GridLayoutManager gridLayoutManager,gridLayoutManager2,gridLayoutManager3;
    private RecommendAdapter recomendAdapter;
    private NewAlbumsAdapter newAlbumsAdapter;
    private RadioAdapter radioAdapter;

    private ArrayList<GedanHot> recomendList = new ArrayList<>();
    private ArrayList<NewAlbums> newAlbumsList = new ArrayList<>();
    private ArrayList<RadioNet> radioList = new ArrayList<>();
    int width = 160,height = 160;

    String newAlbums = "http://music.163.com/api/album/new?area=ALL&offset=" + "0" + "&total=true&limit=" + "6" ;
    LayoutInflater layoutInflater;
    View loadView , v1,v2,v3;
    LinearLayout itemChanged;
    HashMap<String,View> hashMap;
    String position;
    private ArrayList<BillboardItem> items = new ArrayList<>(7);
    private static ExecutorService exec = Executors.newFixedThreadPool(6);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recommend, container, false);

        ImageButton privateFm = (ImageButton) view.findViewById(R.id.private_fm);

        layoutInflater = LayoutInflater.from(getContext());
        TextView dailyText = (TextView) view.findViewById(R.id.daily_text);
        dailyText.setText(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "");
//        recyclerView1 = (RecyclerView) view.findViewById(R.id.recommend_playlist_recyclerview);
//        recyclerView2 = (RecyclerView) view.findViewById(R.id.recommend_newalbums_recyclerview);
//        recyclerView3 = (RecyclerView) view.findViewById(R.id.recommend_radio_recyclerview);
//        gridLayoutManager = new GridLayoutManager(getActivity(),3);
//        gridLayoutManager2 = new GridLayoutManager(getActivity(),3);
//        gridLayoutManager3 = new GridLayoutManager(getActivity(),3);
//        recyclerView1.setLayoutManager(gridLayoutManager);
//        recyclerView2.setLayoutManager(gridLayoutManager2);
//        recyclerView3.setLayoutManager(gridLayoutManager3);
//        recomendAdapter = new RecommendAdapter(null);
//        newAlbumsAdapter = new NewAlbumsAdapter(null);
//        radioAdapter = new RadioAdapter(null);
//        recyclerView1.setAdapter(recomendAdapter);
//        recyclerView2.setAdapter(newAlbumsAdapter);
//        recyclerView3.setAdapter(radioAdapter);

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
               Intent itent = new Intent(getContext(),NetItemChangeActivity.class);
                getActivity().startActivity(itent);
            }
        });

        return view;
    }
    LinearLayout linearLayout;


    public String getThisMonthDate(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cc2 = Calendar.getInstance();
        int maxMonthDay = cc2.getActualMaximum(Calendar.DAY_OF_MONTH);
        cc2.set(cc2.get(Calendar.YEAR), cc2.get(Calendar.MONTH),maxMonthDay,23,59,59);
        String end = sdf.format(cc2.getTime());
        cc2.set(cc2.get(Calendar.YEAR), cc2.get(Calendar.MONTH),1,0,0,0);
        String start = sdf.format(cc2.getTime());
        return start+"|"+end;
    }







private boolean isFromCache = true;
    private void reloadAdapter(){
       final Gson gson = new Gson();
        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... params) {

                if(NetworkUtils.isConnectInternet(getActivity())){
                    isFromCache = false;
                }
                String fmtrash = "http://music.163.com/api/radio/get";


                //热门歌单
                try {
                    JsonObject result = HttpUtil.getResposeJsonObject(BMA.GeDan.hotGeDan(6),getActivity() , isFromCache);

                    if(result == null){
                        return null;
                    }

                    JsonArray pArray = result.get("content")
                            .getAsJsonObject().get("list").getAsJsonArray();
                    if(pArray == null){
                        return null;
                    }

                    int plen = pArray.size();

                    for(int i = 0;i < plen; i++){
                        GedanHot gedanHot = gson.fromJson(pArray.get(i),GedanHot.class);
                        recomendList.add(gedanHot);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                //新专辑
                try {
                    JsonObject result2 = HttpUtil.getResposeJsonObject(newAlbums,getContext() ,isFromCache);
                    JsonArray jsonArray2  = result2.get("albums").getAsJsonArray();

                    Iterator it2 = jsonArray2.iterator();
                    while(it2.hasNext()){
                        JsonElement e = (JsonElement)it2.next();
                        JsonObject jo = e.getAsJsonObject();

                        String artistName = jo.get("artist").getAsJsonObject().get("name").getAsString();
                        NewAlbums newAlbums = new NewAlbums(getStringValue(jo, "blurPicUrl"),getIntValue(jo, "id"),
                                getStringValue(jo, "name"), artistName, getIntValue(jo, "publishTime"));
                        newAlbumsList.add(newAlbums);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                //推荐电台
                try {
                    JsonObject result3 = HttpUtil.getResposeJsonObject(BMA.Radio.recommendRadioList(6),getActivity() , isFromCache);

                    JsonArray rArray = result3.get("list").getAsJsonArray();

                    int rlen = rArray.size();

                    for(int i = 0;i < rlen; i++){
                        RadioNet radioNet = gson.fromJson(rArray.get(i),RadioNet.class);
                        radioList.add(radioNet);
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void v){

                v1 = layoutInflater.inflate(R.layout.recommend_playlist, linearLayout,false);
                recyclerView1 = (RecyclerView) v1.findViewById(R.id.recommend_playlist_recyclerview);
                gridLayoutManager = new GridLayoutManager(getActivity(),3);
                recyclerView1.setLayoutManager(gridLayoutManager);
                recyclerView1.setAdapter(recomendAdapter);


                v2 = layoutInflater.inflate(R.layout.recommend_newalbums, linearLayout,false);
                recyclerView2 = (RecyclerView) v2.findViewById(R.id.recommend_newalbums_recyclerview);
                gridLayoutManager2 = new GridLayoutManager(getActivity(),3);
                recyclerView2.setLayoutManager(gridLayoutManager2);
                recyclerView2.setAdapter(newAlbumsAdapter);

                v3 = layoutInflater.inflate(R.layout.recommend_radio, linearLayout,false);
                recyclerView3 = (RecyclerView) v3.findViewById(R.id.recommend_radio_recyclerview);
                gridLayoutManager3 = new GridLayoutManager(getActivity(),3);
                recyclerView3.setLayoutManager(gridLayoutManager3);
                recyclerView3.setAdapter(radioAdapter);


                recomendAdapter.update(recomendList);
                newAlbumsAdapter.update(newAlbumsList);
                radioAdapter.update(radioList);

                hashMap = new HashMap<>();
                hashMap.put("推荐歌单",v1);
                hashMap.put("最新专辑",v2);
                hashMap.put("主播电台",v3);
                position = PreferencesUtility.getInstance(getActivity()).getItemPosition();
                linearLayout.removeView(loadView);

                addViews();

                itemChanged.setVisibility(View.VISIBLE);

            }

        }.execute();


    }

    public void addViews(){

        String[] strs = position.split(" ");
        for(int i = 0 ; i< strs.length ; i++ ){
            linearLayout.addView(hashMap.get(strs[i]));
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        if(position == null){
            return;
        }
        String st = PreferencesUtility.getInstance(getActivity()).getItemPosition();
        if(!st.equals(position)){
            position = st;
            linearLayout.removeAllViews();
            addViews();
        }

    }

    private String getStringValue(JsonObject jsonObject, String key){
        JsonElement nameElement = jsonObject.get(key);
        return nameElement.getAsString();
    }


    private int getIntValue(JsonObject jsonObject,String key){
        JsonElement nameElement = jsonObject.get(key);
        return nameElement.getAsInt();
    }


    class RecommendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private ArrayList<GedanHot> mList;
        SpannableString spanString;
        public RecommendAdapter(ArrayList<GedanHot> list){
            Bitmap b = BitmapFactory.decodeResource(getResources(), R.mipmap.index_icn_earphone);
            ImageSpan imgSpan = new ImageSpan(getActivity(), b, ImageSpan.ALIGN_BASELINE);
            spanString = new SpannableString("icon");
            spanString.setSpan(imgSpan, 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            mList = list;
        }

        public void update(ArrayList<GedanHot> list){
            mList = list;
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            ItemView viewholder = new ItemView(layoutInflater.inflate(R.layout.recommend_playlist_item,parent,false));

            return viewholder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final GedanHot info = mList.get(position);

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
            if(count > 10000){
                count = count / 10000;
                ((ItemView) holder).count.append(" " + count + "万");
            }else {
                ((ItemView) holder).count.append(" " + info.getListenum());
            }
            ((ItemView) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), NetPlaylistDetailActivity.class);
                    intent.putExtra("albumid",info.getListid());
                    intent.putExtra("albumart",info.getPic());
                    intent.putExtra("albumname",info.getTitle());
                    getActivity().startActivity(intent);
                }
            });

        }

        @Override
        public int getItemCount() {
            if(mList == null){
                return 0;
            }

            if(mList.size() < 7){
                return mList.size();
            }else {
                return  6;
            }
        }

        class ItemView extends RecyclerView.ViewHolder{
            private SimpleDraweeView art;
            private TextView name,count;

            public ItemView(View itemView) {
                super(itemView);
                art = (SimpleDraweeView) itemView.findViewById(R.id.playlist_art);
                name = (TextView) itemView.findViewById(R.id.playlist_name);
                count = (TextView) itemView.findViewById(R.id.playlist_listen_count);
            }
        }

    }

    class RadioAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private ArrayList<RadioNet> mList;

        public RadioAdapter(ArrayList<RadioNet> list){
            mList = list;
        }

        public void update(ArrayList<RadioNet> list){
            mList = list;
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            ItemView viewholder = new ItemView(layoutInflater.inflate(R.layout.recommend_newalbums_item,parent,false));

            return viewholder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
           final RadioNet info = mList.get(position);

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

                    Intent intent = new Intent(getActivity(), AlbumsDetailActivity.class);
                    info.getChannelid();
//                    intent.putExtra("albumid", info.id);
//                    intent.putExtra("albumart", info.coverImgUrl);
//                    intent.putExtra("albumname", info.albumName);
//                    intent.putExtra("artistname", info.artistName);
//                    intent.putExtra("publisttime", info.publishTime);
                    getActivity().startActivity(intent);

                }
            });
        }

        @Override
        public int getItemCount() {
            if(mList == null){
                return 0;
            }

            if(mList.size() < 7){
                return mList.size();
            }else {
                return  6;
            }
        }

        class ItemView extends RecyclerView.ViewHolder implements View.OnClickListener{
            private SimpleDraweeView art;
            private TextView albumName,artsit;

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

    class NewAlbumsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private ArrayList<NewAlbums> mList;

        public NewAlbumsAdapter(ArrayList<NewAlbums> list){
            mList = list;
        }

        public void update(ArrayList<NewAlbums> list){
            mList = list;
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            ItemView viewholder = new ItemView(layoutInflater.inflate(R.layout.recommend_newalbums_item,parent,false));

            return viewholder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
         final  NewAlbums info = mList.get(position);

//            ImageRequest imageRequest = ImageRequest.fromUri(info.coverImgUrl);
//            CacheKey cacheKey= DefaultCacheKeyFactory.getInstance()
//                    .getEncodedCacheKey(imageRequest);
//            BinaryResource resource = ImagePipelineFactory.getInstance()
//                    .getMainDiskStorageCache().getResource(cacheKey);
//            File file=((FileBinaryResource)resource).getFile();

            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(info.coverImgUrl))
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


            ((ItemView) holder).albumName.setText(info.albumName);
            ((ItemView) holder).artsit.setText(info.artistName);
            ((ItemView) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(getActivity(), AlbumsDetailActivity.class);
                    intent.putExtra("albumid",info.id);
                    intent.putExtra("albumart",info.coverImgUrl);
                    intent.putExtra("albumname",info.albumName);
                    intent.putExtra("artistname",info.artistName);
                    intent.putExtra("publisttime",info.publishTime);
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
            if(mList == null){
                return 0;
            }

            if(mList.size() < 7){
                return mList.size();
            }else {
                return  6;
            }
        }

        class ItemView extends RecyclerView.ViewHolder implements View.OnClickListener{
            private SimpleDraweeView art;
            private TextView albumName,artsit;

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
