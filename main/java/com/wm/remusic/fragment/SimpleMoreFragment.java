package com.wm.remusic.fragment;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
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
import com.wm.remusic.adapter.OverFlowItem;
import com.wm.remusic.dialog.AddNetPlaylistDialog;
import com.wm.remusic.handler.HandlerUtil;
import com.wm.remusic.info.MusicInfo;
import com.wm.remusic.provider.PlaylistsManager;
import com.wm.remusic.service.MusicPlayer;
import com.wm.remusic.uitl.IConstants;
import com.wm.remusic.uitl.MusicUtils;
import com.wm.remusic.widget.DividerItemDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by wm on 2016/1/31.
 */
public class SimpleMoreFragment extends AttachDialogFragment {

    private double heightPercent = 0.5;
    private TextView topTitle;
    private MusicFlowAdapter musicflowAdapter;
    private MusicInfo adapterMusicInfo;

    //弹出的activity列表
    private List<OverFlowItem> mlistInfo = new ArrayList<>();  //声明一个list，动态存储要显示的信息
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private long args;
    private String musicName;


    public static SimpleMoreFragment newInstance(long id) {
        SimpleMoreFragment fragment = new SimpleMoreFragment();
        Bundle args = new Bundle();
        args.putLong("id", id);
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
            args = getArguments().getLong("id");
        }
        //布局
        View view = inflater.inflate(R.layout.more_fragment, container);
        topTitle = (TextView) view.findViewById(R.id.pop_list_title);
        recyclerView = (RecyclerView) view.findViewById(R.id.pop_list);
        layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        getList();
        setClick();
        setItemDecoration();
        return view;
    }

    //设置分割线
    private void setItemDecoration() {
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(itemDecoration);
    }

    private void getList() {

        long musicId = args;
        adapterMusicInfo = MusicUtils.getMusicInfo(mContext, musicId);
        musicName = adapterMusicInfo.musicName;
        if (musicName == null) {
            musicName = MusicPlayer.getTrackName();
        }
        topTitle.setText("歌曲：" + " " + musicName);
        setMusicInfo();
        musicflowAdapter = new MusicFlowAdapter(mContext, mlistInfo, adapterMusicInfo);

    }

    private void setClick() {
        if (musicflowAdapter != null) {
            musicflowAdapter.setOnItemClickListener(new MusicFlowAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, String data) {
                    switch (Integer.parseInt(data)) {
                        case 0:
                            if (adapterMusicInfo.islocal) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (adapterMusicInfo.songId == MusicPlayer.getCurrentAudioId())
                                            return;

                                        long[] ids = new long[1];
                                        ids[0] = adapterMusicInfo.songId;
                                        HashMap<Long, MusicInfo> map = new HashMap<Long, MusicInfo>();
                                        map.put(ids[0], adapterMusicInfo);
                                        MusicPlayer.playNext(mContext, map, ids);
                                    }
                                }, 100);
                            }


                            dismiss();
                            break;
                        case 1:
                            AddNetPlaylistDialog.newInstance(adapterMusicInfo).show(getFragmentManager(), "add");
                            dismiss();
                            break;
                        case 2:
                            Intent shareIntent = new Intent();
                            shareIntent.setAction(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + adapterMusicInfo.data));
                            shareIntent.setType("audio/*");
                            mContext.startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.shared_to)));
                            dismiss();
                            break;
                        case 3:
                            if (adapterMusicInfo.islocal) {
                                new AlertDialog.Builder(mContext).setTitle(getResources().getString(R.string.sure_to_delete_music)).
                                        setPositiveButton(getResources().getString(R.string.sure), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, adapterMusicInfo.songId);
                                                mContext.getContentResolver().delete(uri, null, null);
                                                if (MusicPlayer.getCurrentAudioId() == adapterMusicInfo.songId) {
                                                    if (MusicPlayer.getQueueSize() == 0) {
                                                        MusicPlayer.stop();
                                                    } else {
                                                        MusicPlayer.next();
                                                    }

                                                }

                                                HandlerUtil.getInstance(mContext).postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        PlaylistsManager.getInstance(mContext).deleteMusic(mContext, adapterMusicInfo.songId);
                                                        mContext.sendBroadcast(new Intent(IConstants.MUSIC_COUNT_CHANGED));
                                                    }
                                                }, 200);

//                                            File file;
//                                            file = new File(adapterMusicInfo.data);
//                                            if (file.exists())
//                                                file.delete();

//                                                mContext.getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
//                                                        Uri.parse("file://" + adapterMusicInfo.data)));
//                                            mContext.sendBroadcast(new Intent(IConstants.MUSIC_COUNT_CHANGED));
                                                dismiss();
                                            }
                                        }).
                                        setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dismiss();
                                            }
                                        }).show();
                            }

                            dismiss();
                            break;
                        case 4:
                            if (adapterMusicInfo.islocal) {
                                new AlertDialog.Builder(mContext).setTitle(getResources().getString(R.string.sure_to_set_ringtone)).
                                        setPositiveButton(getResources().getString(R.string.sure), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Uri ringUri = Uri.parse("file://" + adapterMusicInfo.data);
                                                RingtoneManager.setActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_NOTIFICATION, ringUri);
                                                dialog.dismiss();
                                                Toast.makeText(mContext, getResources().getString(R.string.set_ringtone_successed),
                                                        Toast.LENGTH_SHORT).show();
                                                dismiss();
                                            }
                                        }).
                                        setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                            }

                            break;
                        case 5:
                            MusicDetailFragment detailFrament = MusicDetailFragment.newInstance(adapterMusicInfo);
                            detailFrament.show(getActivity().getSupportFragmentManager(), "detail");
                            dismiss();
                            break;
                        default:
                            break;
                    }
                }
            });
            recyclerView.setAdapter(musicflowAdapter);
        }

    }

    //设置音乐overflow条目
    private void setMusicInfo() {
        //设置mlistInfo，listview要显示的内容
        setInfo("下一首播放", R.drawable.lay_icn_next);
        setInfo("收藏到歌单", R.drawable.lay_icn_fav);
        setInfo("分享", R.drawable.lay_icn_share);
        setInfo("删除", R.drawable.lay_icn_delete);
        setInfo("设为铃声", R.drawable.lay_icn_ring);
        setInfo("查看歌曲信息", R.drawable.lay_icn_document);
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
        int dialogHeight = (int) (mContext.getResources().getDisplayMetrics().heightPixels * heightPercent);
        ;
//        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
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