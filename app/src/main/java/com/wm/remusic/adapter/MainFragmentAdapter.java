package com.wm.remusic.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bilibili.magicasakura.widgets.TintImageView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.wm.remusic.R;
import com.wm.remusic.activity.DownActivity;
import com.wm.remusic.activity.PlaylistActivity;
import com.wm.remusic.activity.PlaylistManagerActivity;
import com.wm.remusic.activity.RecentActivity;
import com.wm.remusic.activity.TabActivity;
import com.wm.remusic.info.Playlist;
import com.wm.remusic.provider.PlaylistInfo;
import com.wm.remusic.provider.PlaylistsManager;
import com.wm.remusic.uitl.IConstants;

import java.util.ArrayList;

/**
 * Created by wm on 2016/3/10.
 */
public class MainFragmentAdapter extends RecyclerView.Adapter<MainFragmentAdapter.ItemHolder> {

    private ArrayList<Playlist> playlists, netplaylists = new ArrayList<>();
    private boolean createdExpanded = true;
    private boolean collectExpanded = true;
    private Context mContext;
    private ArrayList itemResults = new ArrayList();
    private boolean isLoveList = true;


    public MainFragmentAdapter(Context context) {
        this.mContext = context;
    }

    public void updateResults(ArrayList itemResults, ArrayList<Playlist> playlists, ArrayList<Playlist> netplaylists) {
        isLoveList = true;
        this.itemResults = itemResults;
        this.playlists = playlists;
        this.netplaylists = netplaylists;
    }


    public void updatePlaylists(ArrayList<Playlist> playlists) {
        this.playlists = playlists;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case 0:
                View v0 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_main_item, viewGroup, false);
                ItemHolder ml0 = new ItemHolder(v0);
                return ml0;
            case 1:
                if(isLoveList){
                    View v1 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_main_playlist_first_item, viewGroup, false);
                    ItemHolder ml1 = new ItemHolder(v1);
                    return ml1;
                }
                View v1 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_main_playlist_item, viewGroup, false);
                ItemHolder ml1 = new ItemHolder(v1);
                return ml1;
            case 2:
                View v2 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.expandable_item, viewGroup, false);
                ItemHolder ml2 = new ItemHolder(v2);
                return ml2;
            case 3:
                View v3 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.expandable_item, viewGroup, false);
                ItemHolder ml3 = new ItemHolder(v3);
                return ml3;

        }
        return null;
    }

    @Override
    public void onBindViewHolder(final ItemHolder itemHolder, int i) {
        switch (getItemViewType(i)) {
            case 0:
                MainFragmentItem item = (MainFragmentItem) itemResults.get(i);
                itemHolder.itemtitle.setText(item.title);
                itemHolder.count.setText("(" + item.count + ")");
                itemHolder.image.setImageResource(item.avatar);
                itemHolder.image.setImageTintList(R.color.theme_color_primary);
                setOnListener(itemHolder, i);
                break;
            case 1:
                Playlist playlist = (Playlist) itemResults.get(i);
                if (createdExpanded && playlist.author.equals("local")) {
                    if (playlist.albumArt != null)
                        itemHolder.albumArt.setImageURI(Uri.parse(playlist.albumArt));
                    itemHolder.title.setText(playlist.name);
                    itemHolder.songcount.setText(playlist.songCount + "首");

                }
                if (collectExpanded && !playlist.author.equals("local")) {
                    if (playlist.albumArt != null)
                        itemHolder.albumArt.setImageURI(Uri.parse(playlist.albumArt));
                    itemHolder.title.setText(playlist.name);
                    itemHolder.songcount.setText(playlist.songCount + "首");
                }
                setOnPlaylistListener(itemHolder, i, playlist.id, playlist.albumArt, playlist.name);
                isLoveList = false;
                break;
            case 2:
                itemHolder.sectionItem.setText("创建的歌单" + "(" + playlists.size() + ")");
                itemHolder.sectionImg.setImageResource(R.drawable.list_icn_arr_right);
                setSectionListener(itemHolder, i);
                break;
            case 3:
                itemHolder.sectionItem.setText("收藏的歌单" + "(" + netplaylists.size() + ")");
                itemHolder.sectionImg.setImageResource(R.drawable.list_icn_arr_right);
                setSectionListener(itemHolder, i);
                break;
        }
    }

    @Override
    public void onViewRecycled(ItemHolder itemHolder) {

    }

    @Override
    public int getItemCount() {
        if(itemResults == null){
            return 0;
        }
        if (!createdExpanded && playlists != null) {
            itemResults.removeAll(playlists);
        }
        if (!collectExpanded) {
            itemResults.removeAll(netplaylists);
        }
        return itemResults.size();
    }

    private void setOnListener(ItemHolder itemHolder, final int position) {
        switch (position) {
            case 0:
                itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(mContext, TabActivity.class);
                                intent.putExtra("page_number", 0);
                                mContext.startActivity(intent);
                            }
                        }, 60);

                    }
                });
                break;
            case 1:
                itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(mContext, RecentActivity.class);
                                mContext.startActivity(intent);
                            }
                        }, 60);
                    }
                });


                break;
            case 2:
                itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(mContext, DownActivity.class);
                                mContext.startActivity(intent);
                            }
                        }, 60);

                    }
                });
                break;
            case 3:
                itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(mContext, TabActivity.class);
                        intent.putExtra("page_number", 1);
                        mContext.startActivity(intent);

                    }
                });
        }

    }

    private void setOnPlaylistListener(ItemHolder itemHolder, final int position, final long playlistid, final String albumArt, final String playlistname) {
        itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(mContext, PlaylistActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        intent.putExtra("islocal", true);
                        intent.putExtra("playlistid", playlistid + "");
                        intent.putExtra("albumart", albumArt);
                        intent.putExtra("playlistname", playlistname);
                        mContext.startActivity(intent);

                    }
                }, 60);

            }
        });

        itemHolder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(mContext, v);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (position == 5) {
                            Toast.makeText(mContext, "此歌单不应删除", Toast.LENGTH_SHORT).show();
                        } else {
                            new AlertDialog.Builder(mContext).setTitle(mContext.getString(R.string.sure_to_delete_music)).
                                    setPositiveButton(mContext.getString(R.string.sure), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            PlaylistInfo.getInstance(mContext).deletePlaylist(playlistid);
                                            PlaylistsManager.getInstance(mContext).delete(playlistid);
                                            Intent intent = new Intent();
                                            intent.setAction(IConstants.PLAYLIST_COUNT_CHANGED);
                                            mContext.sendBroadcast(intent);
                                            dialog.dismiss();
                                        }
                                    }).
                                    setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                        }

                        return true;
                    }
                });
                popupMenu.inflate(R.menu.popmenu);
                popupMenu.show();
            }
        });
    }


    private void setSectionListener(final ItemHolder itemHolder, int position) {
        itemHolder.sectionMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PlaylistManagerActivity.class);
                mContext.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemViewType(int position) {
        if (getItemCount() == 0) {
            return -1;
        }
        if (itemResults.get(position) instanceof MainFragmentItem)
            return 0;
        if (itemResults.get(position) instanceof Playlist) {
            return 1;
        }
        if (itemResults.get(position) instanceof String) {
            if (((String) itemResults.get(position)).equals("收藏的歌单"))
                return 3;
        }
        return 2;
    }


    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView itemtitle, title, count, songcount, sectionItem;
        protected ImageView menu, sectionImg, sectionMenu;
        SimpleDraweeView albumArt;
        protected TintImageView image;

        public ItemHolder(View view) {
            super(view);
            this.image = (TintImageView) view.findViewById(R.id.fragment_main_item_img);
            this.itemtitle = (TextView) view.findViewById(R.id.fragment_main_item_title);
            this.count = (TextView) view.findViewById(R.id.fragment_main_item_count);

            this.title = (TextView) view.findViewById(R.id.fragment_main_playlist_item_title);
            this.songcount = (TextView) view.findViewById(R.id.fragment_main_playlist_item_count);
            this.albumArt = (SimpleDraweeView) view.findViewById(R.id.fragment_main_playlist_item_img);
            this.menu = (ImageView) view.findViewById(R.id.fragment_main_playlist_item_menu);

            this.sectionItem = (TextView) view.findViewById(R.id.expand_title);
            this.sectionImg = (ImageView) view.findViewById(R.id.expand_img);
            this.sectionMenu = (ImageView) view.findViewById(R.id.expand_menu);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ObjectAnimator anim = null;
            anim = ObjectAnimator.ofFloat(sectionImg, "rotation", 90, 0);
            anim.setDuration(100);
            anim.setRepeatCount(0);
            anim.setInterpolator(new LinearInterpolator());
            switch (getItemViewType()) {

                case 2:
                    if (createdExpanded) {
                        itemResults.removeAll(playlists);
                        updateResults(itemResults, playlists, netplaylists);
                        notifyItemRangeRemoved(5, playlists.size());
                        anim.start();

                        createdExpanded = false;
                    } else {
                        itemResults.removeAll(netplaylists);
                        itemResults.remove("收藏的歌单");
                        itemResults.addAll(playlists);
                        itemResults.add("收藏的歌单");
                        itemResults.addAll(netplaylists);
                        updateResults(itemResults, playlists, netplaylists);
                        notifyItemRangeInserted(5, playlists.size());
                        anim.reverse();
                        createdExpanded = true;
                    }

                    break;

                case 3:
                    if (collectExpanded) {
                        itemResults.removeAll(netplaylists);
                        updateResults(itemResults, playlists, netplaylists);
                        int len = playlists.size();
                        notifyItemRangeRemoved(6 + len, netplaylists.size());
                        anim.start();

                        collectExpanded = false;
                    } else {
                        itemResults.addAll(netplaylists);
                        updateResults(itemResults, playlists, netplaylists);
                        int len = playlists.size();
                        notifyItemRangeInserted(6 + len, netplaylists.size());
                        anim.reverse();
                        collectExpanded = true;
                    }
                    break;
            }
        }

    }
}
