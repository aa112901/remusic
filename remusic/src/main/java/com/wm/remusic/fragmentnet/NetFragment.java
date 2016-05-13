package com.wm.remusic.fragmentnet;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wm.remusic.R;
import com.wm.remusic.activity.AlbumsDetailActivity;
import com.wm.remusic.activity.NetPlaylistDetailActivity;
import com.wm.remusic.adapter.OverFlowAdapter;
import com.wm.remusic.info.Playlist;
import com.wm.remusic.json.PlaylistNet;
import com.wm.remusic.json.RadioNet;
import com.wm.remusic.json.RecommendAlbum;
import com.wm.remusic.net.BMA;
import com.wm.remusic.net.HttpUtil;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

/**
 * Created by wm on 2016/4/9.
 */
public class NetFragment extends Fragment {

    private RecyclerView recyclerView1,recyclerView2,recyclerView3;
    private GridLayoutManager gridLayoutManager,gridLayoutManager2,gridLayoutManager3;
    private RecommendAdapter recomendAdapter;
    private NewAlbumsAdapter newAlbumsAdapter;
    private RadioAdapter radioAdapter;

    private ArrayList<PlaylistNet> recomendList = new ArrayList<>();
    private ArrayList<NewAlbums> newAlbumsList = new ArrayList<>();
    private ArrayList<RadioNet> radioList = new ArrayList<>();
    int width = 160,height = 160;

    String newAlbums = "http://music.163.com/api/album/new?area=ALL&offset=" + "0" + "&total=true&limit=" + "6" ;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recommend, container, false);

        ImageButton privateFm = (ImageButton) view.findViewById(R.id.private_fm);


        recyclerView1 = (RecyclerView) view.findViewById(R.id.recommend_playlist_recyclerview);
        recyclerView2 = (RecyclerView) view.findViewById(R.id.recommend_newalbums_recyclerview);
        recyclerView3 = (RecyclerView) view.findViewById(R.id.recommend_radio_recyclerview);
        gridLayoutManager = new GridLayoutManager(getActivity(),3);
        gridLayoutManager2 = new GridLayoutManager(getActivity(),3);
        gridLayoutManager3 = new GridLayoutManager(getActivity(),3);
        recyclerView1.setLayoutManager(gridLayoutManager);
        recyclerView2.setLayoutManager(gridLayoutManager2);
        recyclerView3.setLayoutManager(gridLayoutManager3);
        recomendAdapter = new RecommendAdapter(null);
        newAlbumsAdapter = new NewAlbumsAdapter(null);
        radioAdapter = new RadioAdapter(null);
        recyclerView1.setAdapter(recomendAdapter);
        recyclerView2.setAdapter(newAlbumsAdapter);
        recyclerView3.setAdapter(radioAdapter);

        TextView dailyText = (TextView) view.findViewById(R.id.daily_text);
        dailyText.setText(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "");

        reloadAdapter();

        linearLayout = (LinearLayout) view.findViewById(R.id.recommend_layout);

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
      final   View v1 = layoutInflater.inflate(R.layout.recommend_newalbums, linearLayout,false);
        recyclerView3 = (RecyclerView) v1.findViewById(R.id.recommend_newalbums_recyclerview);
        gridLayoutManager3 = new GridLayoutManager(getActivity(),3);
        recyclerView3.setLayoutManager(gridLayoutManager3);
        recyclerView3.setAdapter(newAlbumsAdapter);

     final    View v2 = layoutInflater.inflate(R.layout.recommend_playlist, linearLayout,false);
        recyclerView3 = (RecyclerView) v2.findViewById(R.id.recommend_playlist_recyclerview);
        gridLayoutManager3 = new GridLayoutManager(getActivity(),3);
        recyclerView3.setLayoutManager(gridLayoutManager3);
        recyclerView3.setAdapter(recomendAdapter);

        linearLayout.addView(v1);
        linearLayout.addView(v2);



        TextView change = (TextView) view.findViewById(R.id.change_item_position);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLayout.removeView(v1);
                linearLayout.addView(v1,0);
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

    private void reloadAdapter(){
       final Gson gson = new Gson();


        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... params) {


                String fmtrash = "http://music.163.com/api/radio/get";

                JsonObject result = HttpUtil.get(BMA.GeDan.hotGeDan(6), "PlaylistHot");
                if(result == null){
                    return null;
                }
                //热门歌单
                JsonArray pArray = result.get("content")
                        .getAsJsonObject().get("list").getAsJsonArray();
                if(pArray == null){
                    return null;
                }

                int plen = pArray.size();

                for(int i = 0;i < plen; i++){
                    PlaylistNet playlistNet = gson.fromJson(pArray.get(i),PlaylistNet.class);
                    recomendList.add(playlistNet);
                }


                return null;
            }

            @Override
            protected void onPostExecute(Void v){
                recomendAdapter.update(recomendList);
            }

        }.execute();

        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... params) {

                JsonObject result = HttpUtil.getResposeJsonObject(newAlbums);
                if(result == null){
                    return null;
                }
                //新专辑
                JsonArray jsonArray2  = result.get("albums").getAsJsonArray();
                if(jsonArray2 == null){
                    return null;
                }

                Iterator it2 = jsonArray2.iterator();
                while(it2.hasNext()){
                    JsonElement e = (JsonElement)it2.next();
                    JsonObject jo = e.getAsJsonObject();

                    String artistName = jo.get("artist").getAsJsonObject().get("name").getAsString();
                    NewAlbums newAlbums = new NewAlbums(getStringValue(jo, "blurPicUrl"),getIntValue(jo, "id"),
                            getStringValue(jo, "name"), artistName, getIntValue(jo, "publishTime"));
                    newAlbumsList.add(newAlbums);
                }


                return null;
            }

            @Override
            protected void onPostExecute(Void v){
                newAlbumsAdapter.update(newAlbumsList);
            }

        }.execute();

        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... params) {

                JsonObject result = HttpUtil.get(BMA.Radio.recommendRadioList(6), "RadioList");
                if(result == null){
                    return null;
                }

                //推荐电台
                JsonArray rArray = result.get("list")
                        .getAsJsonArray();
                if(rArray == null){
                    return null;
                }

                int rlen = rArray.size();

                for(int i = 0;i < rlen; i++){
                    RadioNet radioNet = gson.fromJson(rArray.get(i),RadioNet.class);
                    radioList.add(radioNet);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void v){
                radioAdapter.update(radioList);



            }

        }.execute();


    }
    private String getStringValue(JsonObject jsonObject,String key){
        JsonElement nameElement = jsonObject.get(key);
        return nameElement.getAsString();
    }

    private int getIntValue(JsonObject jsonObject,String key){
        JsonElement nameElement = jsonObject.get(key);
        return nameElement.getAsInt();
    }


    class RecommendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private ArrayList<PlaylistNet> mList;

        public RecommendAdapter(ArrayList<PlaylistNet> list){
            mList = list;
        }

        public void update(ArrayList<PlaylistNet> list){
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
        final PlaylistNet info = mList.get(position);

            ((ItemView) holder).art.setImageURI(Uri.parse(info.getPic()));
            ((ItemView) holder).name.setText(info.getTitle());
            ((ItemView) holder).count.setText(info.getListenum());
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
            RadioNet info = mList.get(position);

            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(info.getPic()))
                    .setResizeOptions(new ResizeOptions(width, height))
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(((ItemView) holder).art.getController())
                    .setImageRequest(request)
                    .build();
            ((ItemView) holder).art.setController(controller);

            //  ((ItemView) holder).art.setImageURI(Uri.parse(info.coverImgUrl));
            ((ItemView) holder).albumName.setText(info.getTitle());
            ((ItemView) holder).artsit.setText(info.getDesc());
            ((ItemView) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RadioNet info = mList.get(position);
                    Intent intent = new Intent(getActivity(), AlbumsDetailActivity.class);
                    info.getChannelid();
//                    intent.putExtra("albumid", info.id);
//                    intent.putExtra("albumart", info.coverImgUrl);
//                    intent.putExtra("albumname", info.albumName);
//                    intent.putExtra("artistname", info.artistName);
//                    intent.putExtra("publisttime", info.publishTime);
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
            NewAlbums info = mList.get(position);

            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(info.coverImgUrl))
                    .setResizeOptions(new ResizeOptions(width, height))
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(((ItemView) holder).art.getController())
                    .setImageRequest(request)
                    .build();
            ((ItemView) holder).art.setController(controller);

          //  ((ItemView) holder).art.setImageURI(Uri.parse(info.coverImgUrl));
            ((ItemView) holder).albumName.setText(info.albumName);
            ((ItemView) holder).artsit.setText(info.artistName);
            ((ItemView) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NewAlbums info =  mList.get(position);
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
