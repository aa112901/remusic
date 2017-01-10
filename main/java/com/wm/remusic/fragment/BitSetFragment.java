package com.wm.remusic.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.wm.remusic.MainApplication;
import com.wm.remusic.R;
import com.wm.remusic.uitl.PreferencesUtility;

/**
 * Created by wm on 2016/3/22.
 */
public class BitSetFragment extends DialogFragment implements View.OnClickListener {

    private TextView bit1, bit2, bit3, bit256, bit320;
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //设置无标题
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.fragment_bitset, container);
        bit1 = (TextView) view.findViewById(R.id.timing_10min);
        bit2 = (TextView) view.findViewById(R.id.timing_20min);
        bit3 = (TextView) view.findViewById(R.id.timing_30min);
        bit256 = (TextView) view.findViewById(R.id.timing_45min);
        bit320 = (TextView) view.findViewById(R.id.timing_60min);
        bit1.setOnClickListener(this);
        bit2.setOnClickListener(this);
        bit3.setOnClickListener(this);
        bit256.setOnClickListener(this);
        bit320.setOnClickListener(this);
        mContext = MainApplication.context;
        switch (PreferencesUtility.getInstance(mContext).getDownMusicBit()) {
            case 64:
                // TypedValue outValue = new TypedValue();
                //  getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground,outValue, true);
                // bit1.setBackgroundResource(outValue.resourceId);
                bit1.setPressed(true);
                break;
            case 128:
                // bit1.setPressed(true);
                bit2.setPressed(true);
                break;
            case 192:
                // bit1.setPressed(true);
                bit3.setPressed(true);
                break;
            case 256:
                bit256.setPressed(true);
                break;
            case 320:
                bit320.setPressed(true);
                break;
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.timing_10min:
                PreferencesUtility.getInstance(mContext).setDownMusicBit(64);
                dismiss();
                break;
            case R.id.timing_20min:
                PreferencesUtility.getInstance(mContext).setDownMusicBit(128);
                dismiss();
                break;
            case R.id.timing_30min:
                PreferencesUtility.getInstance(mContext).setDownMusicBit(192);
                dismiss();
                break;
            case R.id.timing_45min:
                PreferencesUtility.getInstance(mContext).setDownMusicBit(256);
                dismiss();
                break;
            case R.id.timing_60min:
                PreferencesUtility.getInstance(mContext).setDownMusicBit(320);
                dismiss();
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置样式
        //setStyle(DialogFragment.STYLE_NO_FRAME, R.style.CustomDatePickerDialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        //设置fragment高度 、宽度
        int dialogHeight = (int) (getActivity().getResources().getDisplayMetrics().heightPixels * 0.56);
        int dialogWidth = (int) (getActivity().getResources().getDisplayMetrics().widthPixels * 0.63);
        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
        getDialog().setCanceledOnTouchOutside(true);

    }

}
