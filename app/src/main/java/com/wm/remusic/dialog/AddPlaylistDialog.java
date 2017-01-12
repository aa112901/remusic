package com.wm.remusic.dialog;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.wm.remusic.MainApplication;
import com.wm.remusic.R;
import com.wm.remusic.fragment.AttachDialogFragment;
import com.wm.remusic.info.Playlist;
import com.wm.remusic.provider.PlaylistInfo;
import com.wm.remusic.provider.PlaylistsManager;
import com.wm.remusic.service.MusicTrack;
import com.wm.remusic.uitl.IConstants;
import com.wm.remusic.uitl.MusicUtils;
import com.wm.remusic.widget.DividerItemDecoration;

import java.util.ArrayList;

/**
 * Created by wm on 20/12/15.
 */
public class AddPlaylistDialog extends AttachDialogFragment {
    private PlaylistInfo playlistInfo;
    private PlaylistsManager playlistsManager;
    private RecyclerView recyclerView;
    private long[] musicId;

    public static AddPlaylistDialog newInstance(long[] songList) {
        AddPlaylistDialog dialog = new AddPlaylistDialog();
        Bundle bundle = new Bundle();
        bundle.putLongArray("songs", songList);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        //设置无标题
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (getArguments() != null) {
            musicId = getArguments().getLongArray("songs");
        }
        playlistInfo = PlaylistInfo.getInstance(getContext());
        playlistsManager = PlaylistsManager.getInstance(getContext());

        View view = inflater.inflate(R.layout.fragment_add_playlist, container);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.create_new_playlist);
        recyclerView = (RecyclerView) view.findViewById(R.id.add_playlist_recyclerview);


        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                alertDialog.setView((getActivity().getLayoutInflater().inflate(R.layout.dialog, null)));
                alertDialog.show();
                Window window = alertDialog.getWindow();
                window.setContentView(R.layout.dialog);
                final EditText editText = (EditText) (window.findViewById(R.id.message));
                editText.requestFocus();
                (window.findViewById(R.id.positiveButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                (window.findViewById(R.id.negativeButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String albumart = null;

                                for (long id : musicId) {
                                    albumart = MusicUtils.getAlbumdata(MainApplication.context, id);
                                    if (albumart != null) {
                                        break;
                                    }
                                }
                                //String albumart = MusicUtils.getMusicInfo(getContext(), musicId[0]).albumData;
                                playlistInfo.addPlaylist(editText.getText().hashCode(), editText.getText().toString(),
                                        musicId.length, "file://" + albumart, "local");
                                for (int i = 0; i < musicId.length; i++) {
                                    playlistsManager.insert(MainApplication.context, editText.getText().hashCode(), musicId[i], i);
                                }
                                Intent intent = new Intent(IConstants.PLAYLIST_COUNT_CHANGED);
                                MainApplication.context.sendBroadcast(intent);

                            }
                        }).start();

                        alertDialog.dismiss();
                    }
                });
            }
        });
        ArrayList<Playlist> playlists = playlistInfo.getPlaylist();
        recyclerView.setLayoutManager(layoutManager);
        AddPlaylistAdapter adapter = new AddPlaylistAdapter(playlists);
        recyclerView.setAdapter(adapter);
        //setItemDecoration();
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDatePickerDialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        //设置fragment高度 、宽度
        int dialogHeight = (int) (getActivity().getResources().getDisplayMetrics().heightPixels * 0.65);
        int dialogWidth = (int) (getActivity().getResources().getDisplayMetrics().widthPixels * 0.77);
        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
        getDialog().setCanceledOnTouchOutside(true);

    }

    //设置分割线
    private void setItemDecoration() {
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
    }

    private class AddPlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        ArrayList<Playlist> playlists;

        public AddPlaylistAdapter(ArrayList<Playlist> p) {
            playlists = p;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.fragment_add_playlist_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Playlist playlist = playlists.get(position);
            //((ViewHolder)holder).imageView.setImageURI();
            ((ViewHolder) holder).title.setText(playlist.name);
            ((ViewHolder) holder).count.setText(playlist.songCount + "");
            Uri uri = Uri.parse(playlist.albumArt);
            ((ViewHolder) holder).imageView.setImageURI(uri);

        }

        @Override
        public int getItemCount() {
            return playlists.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            SimpleDraweeView imageView;
            TextView title, count;

            public ViewHolder(View v) {
                super(v);
                this.imageView = (SimpleDraweeView) v.findViewById(R.id.add_playlist_img);
                this.title = (TextView) v.findViewById(R.id.add_playlist_toptext);
                this.count = (TextView) v.findViewById(R.id.add_playlist_bottom_text);
                v.setOnClickListener(this);

            }

            @Override
            public void onClick(View v) {
                Playlist playlist = playlists.get(getAdapterPosition());
                //playlistInfo.updatePlaylist(playlist.id, musicId.length);
                ArrayList<MusicTrack> musicTracks = playlistsManager.getPlaylist(playlist.id);

                if (getAdapterPosition() == 0 && musicTracks.size() == 0) {
                    for (int i = 0; i < musicId.length; i++) {
                        playlistsManager.insert(getContext(), playlist.id, musicId[i], 0);
                    }
                }

                for (int i = 0; i < musicId.length; i++) {

                    for (int j = 0; j < musicTracks.size(); j++) {
                        if (musicId[i] != musicTracks.get(j).mId) {
                            playlistsManager.insert(getContext(), playlist.id, musicId[i], 0);
                        }
                    }

                }
                Intent intent = new Intent(IConstants.PLAYLIST_COUNT_CHANGED);
                getActivity().sendBroadcast(intent);
                dismiss();
            }
        }
    }


}
