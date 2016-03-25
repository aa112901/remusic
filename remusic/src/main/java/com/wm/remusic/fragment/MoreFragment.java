package com.wm.remusic.fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.wm.remusic.R;
import com.wm.remusic.adapter.MusicFlowAdapter;
import com.wm.remusic.adapter.OverFlowAdapter;
import com.wm.remusic.adapter.OverFlowItem;
import com.wm.remusic.dialog.AddPlaylistDialog;
import com.wm.remusic.info.MusicInfo;
import com.wm.remusic.service.MusicPlayer;
import com.wm.remusic.uitl.IConstants;
import com.wm.remusic.uitl.MusicUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wm on 2016/1/31.
 */
public class MoreFragment extends DialogFragment {
    int type;
    double h;
    TextView topTitle;
    List<MusicInfo> list = null;
    MusicFlowAdapter muaicflowAdapter;
    MusicInfo adapterMusicInfo;
    OverFlowAdapter commonAdapter;
    //弹出的activity列表
    private List<OverFlowItem> mlistInfo = new ArrayList<>();  //声明一个list，动态存储要显示的信息
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private String args;
    private String musicName, artist, albumId, albumName;

    public static MoreFragment newInstance(String id, int startFrom) {
        MoreFragment fragment = new MoreFragment();
        Bundle args = new Bundle();
        args.putString("id", id);
        args.putInt("type", startFrom);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //设置无标题
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置从底部弹出
        WindowManager.LayoutParams params = getDialog().getWindow()
                .getAttributes();
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setAttributes(params);
        if (getArguments() != null) {
            type = getArguments().getInt("type");
            args = getArguments().getString("id");
        }

        //布局
        View view = inflater.inflate(R.layout.more_fragment, container);
        topTitle = (TextView) view.findViewById(R.id.pop_list_title);
        recyclerView = (RecyclerView) view.findViewById(R.id.pop_list);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        getList();
        setClick();
        setItemDecoration();
        return view;
    }

    //设置分割线
    private void setItemDecoration() {
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
    }

    private void getList() {

        if (type == IConstants.MUSICOVERFLOW) {
            long musicId = Long.parseLong(args.trim());
            adapterMusicInfo = MusicUtils.getMusicInfo(getContext(), musicId);
            artist = adapterMusicInfo.artist;
            albumId = adapterMusicInfo.albumId + "";
            albumName = adapterMusicInfo.albumName;
            musicName = adapterMusicInfo.musicName;
            topTitle.setText("歌曲：" + " " + musicName);
            h = 0.6;
            setMusicInfo();
            muaicflowAdapter = new MusicFlowAdapter(getActivity(), mlistInfo, adapterMusicInfo);

        } else {
            switch (type) {
                case IConstants.ARTISTOVERFLOW:
                    String artist = args;
                    list = MusicUtils.queryMusic(getContext(), null, artist, IConstants.START_FROM_ARTIST);
                    topTitle.setText("歌曲：" + " " + list.get(0).artist);
                    break;
                case IConstants.ALBUMOVERFLOW:
                    String albumId = args;
                    list = MusicUtils.queryMusic(getContext(), null, albumId, IConstants.START_FROM_ALBUM);
                    topTitle.setText("专辑：" + " " + list.get(0).albumName);
                    break;
                case IConstants.FOLDEROVERFLOW:
                    String folder = args;
                    list = MusicUtils.queryMusic(getContext(), null, folder, IConstants.START_FROM_FOLDER);
                    topTitle.setText("文件夹：" + " " + folder);
                    break;
            }
            setCommonInfo();
            h = 0.3;
            commonAdapter = new OverFlowAdapter(getActivity(), mlistInfo, list);

        }

    }

    private void setClick() {
        if (muaicflowAdapter != null) {
            muaicflowAdapter.setOnItemClickListener(new MusicFlowAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, String data) {
                    switch (Integer.parseInt(data)) {
                        case 0:
                            long[] ids = new long[1];
                            ids[0] = adapterMusicInfo.songId;
                            MusicPlayer.playNext(getContext(), ids, -1);
                            dismiss();
                            break;
                        case 1:
                            long[] list = new long[1];
                            list[0] = adapterMusicInfo.songId;
                            AddPlaylistDialog.newInstance(list).show(getFragmentManager(), "add");
                            dismiss();
                            break;
                        case 2:
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + adapterMusicInfo.data));
                            shareIntent.setType("audio/*");
                            getActivity().startActivity(Intent.createChooser(shareIntent, "分享到"));
                            dismiss();
                            break;
                        case 3:
                            new AlertDialog.Builder(getContext()).setTitle("确定删除歌曲吗").
                                    setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            File file;
                                            file = new File(adapterMusicInfo.data);
                                            if (file.exists())
                                                file.delete();
                                            {
                                                getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                                        Uri.parse("file://" + adapterMusicInfo.data)));
                                            }
                                            getContext().sendBroadcast(new Intent(IConstants.MUSIC_COUNT_CHANGED));
                                            dismiss();
                                        }
                                    }).
                                    setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dismiss();
                                        }
                                    }).show();
                            dismiss();
                            break;
                        case 4:
                            FragmentTransaction transaction = ((AppCompatActivity) getContext()).getSupportFragmentManager().beginTransaction();
                            ArtistDetailFragment fragment = ArtistDetailFragment.newInstance(adapterMusicInfo.artistId);
                            transaction.hide(((AppCompatActivity) getContext()).getSupportFragmentManager().findFragmentById(R.id.fragment_container));
                            transaction.add(R.id.fragment_container, fragment);
                            transaction.addToBackStack(null).commit();
                            dismiss();
                            break;
                        case 5:
                            FragmentTransaction transaction1 = ((AppCompatActivity) getContext()).getSupportFragmentManager().beginTransaction();
                            AlbumDetailFragment fragment1 = AlbumDetailFragment.newInstance(adapterMusicInfo.albumId, false, null);
                            transaction1.hide(((AppCompatActivity) getContext()).getSupportFragmentManager().findFragmentById(R.id.fragment_container));
                            transaction1.add(R.id.fragment_container, fragment1);
                            transaction1.addToBackStack(null).commit();
                            dismiss();
                            break;
                        case 6:
                            Uri ringUri = Uri.parse("file://" + adapterMusicInfo.data);
                            RingtoneManager.setActualDefaultRingtoneUri(getContext(), RingtoneManager.TYPE_NOTIFICATION, ringUri);
                            Toast.makeText(getContext(), "设置铃声成功！", Toast.LENGTH_SHORT).show();
                            dismiss();
                            break;
                        case 7:
                            MusicDetailFragment detailFrament = MusicDetailFragment.newInstance(adapterMusicInfo);
                            detailFrament.show(getActivity().getFragmentManager(), "detail");
                            dismiss();
                            break;
                        default:
                            break;
                    }
                }
            });
            recyclerView.setAdapter(muaicflowAdapter);
            return;
        }

        commonAdapter.setOnItemClickListener(new OverFlowAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, String data) {
                switch (Integer.parseInt(data)) {
                    case 0:
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                long[] queuelist = new long[list.size()];
                                for (int i = 0; i < list.size(); i++) {
                                    queuelist[i] = list.get(i).songId;
                                }
                                MusicPlayer.playAll(getContext(), queuelist, 0, false);
                            }
                        }, 100);
                        dismiss();
                        break;
                    case 1:
                        long[] queuelist = new long[list.size()];
                        for (int i = 0; i < list.size(); i++) {
                            queuelist[i] = list.get(i).songId;
                        }
                        AddPlaylistDialog.newInstance(queuelist).show(getFragmentManager(), "add");

                        dismiss();
                        break;
                    case 2:
                        File file;
                        for (MusicInfo music : list) {
                            file = new File(music.data);
                            if (file.exists())
                                file.delete();
                            if (file.exists() == false) {
                                getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                        Uri.parse("file://" + music.data)));
                            }
                        }
                        getActivity().sendBroadcast(new Intent(IConstants.MUSIC_COUNT_CHANGED));
                        dismiss();
                        break;
                }
            }
        });
        recyclerView.setAdapter(commonAdapter);
    }

    //设置音乐overflow条目
    private void setMusicInfo() {
        //设置mlistInfo，listview要显示的内容
        setInfo("下一首播放", R.drawable.lay_icn_next);
        setInfo("收藏到歌单", R.drawable.lay_icn_fav);
        setInfo("分享", R.drawable.lay_icn_share);
        setInfo("删除", R.drawable.lay_icn_delete);
        setInfo("歌手：" + artist, R.drawable.lay_icn_artist);
        setInfo("专辑：" + albumName, R.drawable.lay_icn_alb);
        setInfo("设为铃声", R.drawable.lay_icn_ring);
        setInfo("查看歌曲信息", R.drawable.lay_icn_document);
    }

    //设置专辑，艺术家，文件夹overflow条目
    private void setCommonInfo() {
        setInfo("播放", R.drawable.lay_icn_play);
        setInfo("收藏到歌单", R.drawable.lay_icn_fav);
        setInfo("删除", R.drawable.lay_icn_delete);
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
        int dialogHeight = (int) (getActivity().getResources().getDisplayMetrics().heightPixels * h);
        ;
//        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
//        Display display = wm.getDefaultDisplay();
//        int height = display.getHeight();
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, dialogHeight);
        getDialog().setCanceledOnTouchOutside(true);

    }

    //为info设置数据，并放入mlistInfo
    public void setInfo(String title, int id) {
        // mlistInfo.clear();
        OverFlowItem information = new OverFlowItem();
        information.setTitle(title);
        information.setAvatar(id);
        mlistInfo.add(information); //将新的info对象加入到信息列表中
    }


}
