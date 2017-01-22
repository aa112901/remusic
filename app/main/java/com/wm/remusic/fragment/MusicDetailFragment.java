package com.wm.remusic.fragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.wm.remusic.R;
import com.wm.remusic.info.MusicInfo;
import com.wm.remusic.uitl.MusicUtils;

/**
 * Created by wm on 2016/3/2.
 */
public class MusicDetailFragment extends AttachDialogFragment {
    private TextView title, name, time, qua, size, path;
    private MusicInfo musicInfo;

    public static MusicDetailFragment newInstance(MusicInfo musicInfo) {
        MusicDetailFragment fragment = new MusicDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("musicinfo", musicInfo);
        fragment.setArguments(bundle);
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
            musicInfo = getArguments().getParcelable("musicinfo");
        }

        View view = inflater.inflate(R.layout.fragment_music_detail, container);

        title = (TextView) view.findViewById(R.id.music_detail_title);
        name = (TextView) view.findViewById(R.id.music_detail_name);
        time = (TextView) view.findViewById(R.id.music_detail_time);
        //qua = (TextView) view.findViewById(R.id.music_detail_quater);
        size = (TextView) view.findViewById(R.id.music_detail_size);
        path = (TextView) view.findViewById(R.id.music_detail_path);


        title.setText(musicInfo.musicName);
        name.setText(musicInfo.artist + "-" + musicInfo.musicName);
        time.setText(MusicUtils.makeShortTimeString(mContext, musicInfo.duration / 1000));

        size.setText(musicInfo.size / 1000000 + "m");
        path.setText(musicInfo.data);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置样式
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDatePickerDialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        //设置fragment高度 、宽度
        int dialogHeight = (int) (mContext.getResources().getDisplayMetrics().heightPixels * 0.30);
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, dialogHeight);
        getDialog().setCanceledOnTouchOutside(true);

    }


}
