package com.wm.remusic.adapter;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import com.facebook.drawee.view.SimpleDraweeView;
import com.wm.remusic.R;
import com.wm.remusic.activity.PlaylistManagerActivity;
import com.wm.remusic.activity.SelectActivity;
import com.wm.remusic.fragment.PlaylistDetailFragment;
import com.wm.remusic.fragment.RecentFragment;
import com.wm.remusic.fragment.TabPagerFragment;
import com.wm.remusic.info.MusicInfo;
import com.wm.remusic.info.Playlist;
import com.wm.remusic.provider.PlaylistInfo;
import com.wm.remusic.provider.PlaylistsManager;
import com.wm.remusic.uitl.IConstants;
import com.wm.remusic.uitl.MusicUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by wm on 2016/3/10.
 */
public class MainFragmentAdapter extends RecyclerView.Adapter<MainFragmentAdapter.ItemHolder> {

    private ArrayList<Playlist> playlists;
    private boolean expanded = true;
    private Activity mContext;
    private List itemResults = Collections.emptyList();


    public MainFragmentAdapter(Activity context, List list, ArrayList<Playlist> playlists) {
        this.itemResults = new ArrayList();
        this.mContext = context;
        this.itemResults = list;
        this.playlists = playlists;
    }

    public void updateResults(List itemResults, ArrayList<Playlist> playlists) {

        this.itemResults = itemResults;
        this.playlists = playlists;
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
                View v1 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_main_playlist_item, viewGroup, false);
                ItemHolder ml1 = new ItemHolder(v1);
                return ml1;
            case 2:
                View v2 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.expandable_item, viewGroup, false);
                ItemHolder ml2 = new ItemHolder(v2);
                return ml2;

        }
        return null;
    }

    @Override
    public void onBindViewHolder(final ItemHolder itemHolder, int i) {
        switch (getItemViewType(i)) {
            case 0:
                MainFragmentItem item = (MainFragmentItem) itemResults.get(i);
                itemHolder.itemtitle.setText(item.getTitle());
                itemHolder.count.setText("(" + item.getCount() + ")");
                itemHolder.image.setImageResource(item.getAvatar());
                setOnListener(itemHolder, i);
                break;
            case 1:
                Playlist playlist = (Playlist) itemResults.get(i);
                itemHolder.albumArt.setImageURI(Uri.parse(playlist.albumArt));
                itemHolder.title.setText(playlist.name);
                itemHolder.songcount.setText(playlist.songCount + "首");
                setOnPlaylistListener(itemHolder, i, playlist.id, playlist.albumArt, playlist.name);
                break;
            case 2:
                itemHolder.sectionItem.setText("创建的歌单" + "(" + playlists.size() + ")");
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
        return itemResults == null ? 0 : itemResults.size();
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
                                TabPagerFragment fragment = TabPagerFragment.newInstance(0);
                                FragmentTransaction transaction = ((AppCompatActivity) mContext).getSupportFragmentManager().beginTransaction();
                                transaction.hide(((AppCompatActivity) mContext).getSupportFragmentManager().findFragmentById(R.id.fragment_container));
                                transaction.add(R.id.fragment_container, fragment);
                                transaction.addToBackStack(null).commit();
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
                                RecentFragment fragment = new RecentFragment();
                                FragmentTransaction transaction = ((AppCompatActivity) mContext).getSupportFragmentManager().beginTransaction();
                                transaction.hide(((AppCompatActivity) mContext).getSupportFragmentManager().findFragmentById(R.id.fragment_container));
                                transaction.add(R.id.fragment_container, fragment);
                                transaction.addToBackStack(null).commit();
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
                                Intent intent = new Intent(mContext, SelectActivity.class);
                                ArrayList<MusicInfo> mList = (ArrayList<MusicInfo>) MusicUtils.queryMusic(mContext, IConstants.START_FROM_LOCAL);
                                intent.putParcelableArrayListExtra("ids", mList);
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
                        TabPagerFragment fragment = TabPagerFragment.newInstance(1);
                        FragmentTransaction transaction = ((AppCompatActivity) mContext).getSupportFragmentManager().beginTransaction();
                        transaction.hide(((AppCompatActivity) mContext).getSupportFragmentManager().findFragmentById(R.id.fragment_container));
                        transaction.add(R.id.fragment_container, fragment);
                        transaction.addToBackStack(null).commit();
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
                        PlaylistDetailFragment fragment = PlaylistDetailFragment.newInstance(playlistid, albumArt, playlistname);
                        FragmentTransaction transaction = ((AppCompatActivity) mContext).getSupportFragmentManager().beginTransaction();
                        transaction.hide(((AppCompatActivity) mContext).getSupportFragmentManager().findFragmentById(R.id.fragment_container));
                        transaction.add(R.id.fragment_container, fragment);
                        transaction.addToBackStack(null).commit();
//                        Intent intent = new Intent(mContext, PlaylistDetailActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                        Bundle bundle = new Bundle();
//                        bundle.putLong("playlistid",playlistid);
//                        bundle.putString("albumart",albumArt);
//                        bundle.putString("playlistname",playlistname);
//                        intent.putExtra("playlist",bundle);
//                        mContext.startActivity(intent);

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
        if (itemResults.get(position) instanceof MainFragmentItem)
            return 0;
        if (itemResults.get(position) instanceof Playlist)
            return 1;
        return 2;
    }


    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView itemtitle, title, count, songcount, sectionItem;
        protected ImageView image, menu, sectionImg, sectionMenu;
        SimpleDraweeView albumArt;

        public ItemHolder(View view) {
            super(view);
            this.image = (ImageView) view.findViewById(R.id.fragment_main_item_img);
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
            switch (getItemViewType()) {
                case 2:
                    ObjectAnimator anim;
                    anim = ObjectAnimator.ofFloat(sectionImg, "rotation", 90, 0);
                    anim.setDuration(100);
                    anim.setRepeatCount(0);
                    anim.setInterpolator(new LinearInterpolator());

                    if (expanded) {
                        itemResults.removeAll(playlists);
                        updateResults(itemResults, playlists);
                        notifyItemRangeRemoved(5, playlists.size());
                        anim.start();

                        expanded = false;
                    } else {
                        itemResults.addAll(playlists);
                        updateResults(itemResults, playlists);
                        notifyItemRangeInserted(5, playlists.size());
                        anim.reverse();
                        expanded = true;
                    }

                    break;
            }
        }

    }
}
