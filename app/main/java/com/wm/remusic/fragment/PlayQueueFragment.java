package com.wm.remusic.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bilibili.magicasakura.widgets.TintImageView;
import com.wm.remusic.R;
import com.wm.remusic.dialog.AddNetPlaylistDialog;
import com.wm.remusic.handler.HandlerUtil;
import com.wm.remusic.info.MusicInfo;
import com.wm.remusic.provider.MusicPlaybackState;
import com.wm.remusic.recent.QueueLoader;
import com.wm.remusic.service.MusicPlayer;
import com.wm.remusic.widget.DividerItemDecoration;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by wm on 2016/2/4.
 */
public class PlayQueueFragment extends AttachDialogFragment {

    private RecyclerView.ItemDecoration itemDecoration;
    private PlaylistAdapter adapter;
    private ArrayList<MusicInfo> playlist;
    private TextView playlistNumber, clearAll, addToPlaylist;
    private MusicInfo musicInfo;
    private int currentlyPlayingPosition = 0;
    private MusicPlaybackState musicPlaybackState;
    private RecyclerView recyclerView;  //弹出的activity列表
    private LinearLayoutManager layoutManager;
    private Handler mHandler;
    private PlayQuueuListener mQueueListener;
    public interface PlayQuueuListener{
        void onPlay(int position);
    }

    public void setQueueListener(PlayQuueuListener listener){
        mQueueListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置样式
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDatePickerDialog);
        musicPlaybackState = MusicPlaybackState.getInstance(mContext);
        mHandler = HandlerUtil.getInstance(mContext);
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //设置无标题
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置从底部弹出
        WindowManager.LayoutParams params = getDialog().getWindow()
                .getAttributes();
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setAttributes(params);


        View view = inflater.inflate(R.layout.fragment_queue, container);

        //布局
        playlistNumber = (TextView) view.findViewById(R.id.play_list_number);
        addToPlaylist = (TextView) view.findViewById(R.id.playlist_addto);
        clearAll = (TextView) view.findViewById(R.id.playlist_clear_all);

        addToPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNetPlaylistDialog.newInstance(playlist).show(getFragmentManager(), "add");
            }
        });


        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayer.clearQueue();
                MusicPlayer.stop();
                File file = new File(mContext.getCacheDir().getAbsolutePath() + "playlist");
                if (file.exists()) {
                    file.delete();
                }
                MusicPlaybackState.getInstance(mContext).clearQueue();
                if (adapter != null)
                    adapter.notifyDataSetChanged();
                dismiss();
            }
        });
        recyclerView = (RecyclerView) view.findViewById(R.id.play_list);
        layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        new loadSongs().execute();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //设置fragment高度 、宽度
        int dialogHeight = (int) (mContext.getResources().getDisplayMetrics().heightPixels * 0.6);
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, dialogHeight);
        getDialog().setCanceledOnTouchOutside(true);

    }

    private String readTextFromSDcard(InputStream is) throws Exception {
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuffer buffer = new StringBuffer("");
        String str;
        while ((str = bufferedReader.readLine()) != null) {
            buffer.append(str);
            buffer.append("\n");
        }
        return buffer.toString();
    }

    //异步加载recyclerview界面
    private class loadSongs extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if (mContext != null) {
                try {
//                    FileInputStream in = new FileInputStream(new File(getContext().getCacheDir().getAbsolutePath() + "playlist"));
//                    String c = readTextFromSDcard(in);
//                    HashMap<Long, MusicInfo> play = MainApplication.gsonInstance().fromJson(c, new TypeToken<HashMap<Long, MusicInfo>>() {
//                    }.getType());
                    HashMap<Long, MusicInfo> play = MusicPlayer.getPlayinfos();
                    if (play != null && play.size() > 0) {
                        long[] queue = MusicPlayer.getQueue();
                        int len = queue.length;
                        playlist = new ArrayList<>();
                        for (int i = 0; i < len; i++) {
                            playlist.add(play.get(queue[i]));
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (playlist != null && playlist.size() > 0) {
                adapter = new PlaylistAdapter(playlist);
                recyclerView.setAdapter(adapter);
                itemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST);
                recyclerView.addItemDecoration(itemDecoration);
                playlistNumber.setText("播放列表（" + playlist.size() + "）");

                for (int i = 0; i < playlist.size(); i++) {
                    MusicInfo info = playlist.get(i);

                    if (info != null && MusicPlayer.getCurrentAudioId() == info.songId) {
                        recyclerView.scrollToPosition(i);
                    }
                }

            }

        }

    }

    class PlaylistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private ArrayList<MusicInfo> playlist = new ArrayList<>();

        public PlaylistAdapter(ArrayList<MusicInfo> list) {
            playlist = list;
        }

        public void updateDataSet(ArrayList<MusicInfo> list) {
            this.playlist = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            return new ItemViewHolder(LayoutInflater.from(mContext).inflate(R.layout.fragment_playqueue_item, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            musicInfo = playlist.get(position);
            ((ItemViewHolder) holder).MusicName.setText(playlist.get(position).musicName);
            ((ItemViewHolder) holder).Artist.setText("-" + playlist.get(position).artist);
            //判断该条目音乐是否在播放
            Log.e("current", MusicPlayer.getCurrentAudioId() + "" + "   " + musicInfo.songId);
            if (MusicPlayer.getCurrentAudioId() == musicInfo.songId) {
                Log.e("current", MusicPlayer.getCurrentAudioId() + "" + "   " + musicInfo.songId);
                ((ItemViewHolder) holder).playstate.setVisibility(View.VISIBLE);
                ((ItemViewHolder) holder).playstate.setImageResource(R.drawable.song_play_icon);
                ((ItemViewHolder) holder).playstate.setImageTintList(R.color.theme_color_primary);
                currentlyPlayingPosition = position;
            } else {
                ((ItemViewHolder) holder).playstate.setVisibility(View.GONE);
            }

        }

        @Override
        public int getItemCount() {
            return playlist == null ? 0 : playlist.size();
        }


        class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView delete;
            TextView MusicName, Artist;
            TintImageView playstate;

            public ItemViewHolder(View itemView) {
                super(itemView);
                this.playstate = (TintImageView) itemView.findViewById(R.id.play_state);
                this.delete = (ImageView) itemView.findViewById(R.id.play_list_delete);
                this.MusicName = (TextView) itemView.findViewById(R.id.play_list_musicname);
                this.Artist = (TextView) itemView.findViewById(R.id.play_list_artist);
                itemView.setOnClickListener(this);

                this.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int a = getAdapterPosition();
                        long deleteId = playlist.get(a).songId;

                        // musicPlaybackState.Delete(deleteId);
                        notifyItemRemoved(a);
                        MusicPlayer.removeTrack(deleteId);

                        updateDataSet(QueueLoader.getQueueSongs(mContext));
                        if (playlist == null) {
                            MusicPlayer.stop();
                        }
                        if (MusicPlayer.isPlaying() && (MusicPlayer.getCurrentAudioId() == deleteId)) {
                            MusicPlayer.next();
                        }
                        notifyDataSetChanged();
                        if (playlist != null) {
                            playlistNumber.setText("播放列表（" + playlist.size() + "）");
                        } else {
                            playlistNumber.setText("播放列表");
                        }

                    }
                });

            }

            @Override
            public void onClick(View v) {

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final int a = getAdapterPosition();
                        if (a == -1) {
                            return;
                        }
                        long[] ids = new long[1];
                        ids[0] = playlist.get(a).songId;
                        MusicPlayer.setQueuePosition(a);

                        if(mQueueListener != null)
                        mQueueListener.onPlay(a);

                        notifyItemChanged(currentlyPlayingPosition);
                        notifyItemChanged(a);
                    }
                }, 70);

            }
        }

    }


}
