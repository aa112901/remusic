package com.wm.remusic.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wm.remusic.R;
import com.wm.remusic.activity.PlaylistSelectActivity;
import com.wm.remusic.fragment.MoreFragment;
import com.wm.remusic.info.MusicInfo;
import com.wm.remusic.service.MusicPlayer;
import com.wm.remusic.uitl.IConstants;

import java.util.ArrayList;

/**
 * Created by wm on 2016/3/8.
 */
public class PlaylistDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    final static int FIRST_ITEM = 0;
    final static int ITEM = 1;
    private ArrayList<MusicInfo> arraylist;
    private long playlistId;
    private Activity mContext;
    private long[] songIDs;

    public PlaylistDetailAdapter(Activity context, long playlistid, ArrayList<MusicInfo> mList) {
        this.arraylist = mList;
        this.mContext = context;
        this.playlistId = playlistid;
        this.songIDs = getSongIds();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == FIRST_ITEM) {
            return new CommonItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.common_item, viewGroup, false));
        } else {
            return new ItemViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_playlist_detail_item, viewGroup, false));
        }

    }

    //判断布局类型
    @Override
    public int getItemViewType(int position) {
        return position == FIRST_ITEM ? FIRST_ITEM : ITEM;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder itemHolder, final int i) {
        if (itemHolder instanceof ItemViewHolder) {
            MusicInfo localItem = arraylist.get(i - 1);
            ((ItemViewHolder) itemHolder).trackNumber.setText(i + "");
            ((ItemViewHolder) itemHolder).title.setText(localItem.musicName);
            ((ItemViewHolder) itemHolder).artist.setText(localItem.artist);
            ((ItemViewHolder) itemHolder).menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MoreFragment morefragment = MoreFragment.newInstance(arraylist.get(i - 1).songId + "", IConstants.MUSICOVERFLOW);
                    morefragment.show(((AppCompatActivity) mContext).getSupportFragmentManager(), "music");
                }
            });

        } else if (itemHolder instanceof CommonItemViewHolder) {

            ((CommonItemViewHolder) itemHolder).textView.setText("(共" + arraylist.size() + "首)");

            ((CommonItemViewHolder) itemHolder).select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, PlaylistSelectActivity.class);
                    intent.putParcelableArrayListExtra("ids", arraylist);
                    intent.putExtra("playlistid", playlistId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    mContext.startActivity(intent);
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return (null != arraylist ? arraylist.size() + 1 : 0);
    }

    public long[] getSongIds() {
        long[] ret = new long[arraylist.size()];
        for (int i = 0; i < arraylist.size(); i++) {
            ret[i] = arraylist.get(i).songId;
        }

        return ret;
    }

    public void updateDataSet(long playlistid, ArrayList<MusicInfo> arraylist) {
        this.arraylist = arraylist;
        this.playlistId = playlistid;
    }

    public class CommonItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textView;
        ImageView select;

        CommonItemViewHolder(View view) {
            super(view);
            this.textView = (TextView) view.findViewById(R.id.play_all_number);
            this.select = (ImageView) view.findViewById(R.id.select);
            view.setOnClickListener(this);
        }

        public void onClick(View v) {
            //// TODO: 2016/1/20
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    long[] list = new long[arraylist.size()];
                    for (int i = 0; i < arraylist.size(); i++) {
                        list[i] = arraylist.get(i).songId;
                    }
                    MusicPlayer.playAll(null, list, 0, false);
                }
            }, 100);

        }

    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView title, artist, trackNumber;
        protected ImageView menu;

        public ItemViewHolder(View view) {
            super(view);
            this.title = (TextView) view.findViewById(R.id.song_title);
            this.artist = (TextView) view.findViewById(R.id.song_artist);
            this.trackNumber = (TextView) view.findViewById(R.id.trackNumber);
            this.menu = (ImageView) view.findViewById(R.id.popup_menu);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MusicPlayer.playAll(null, songIDs, getAdapterPosition() - 1, false);
                }
            }, 100);

        }

    }
}
