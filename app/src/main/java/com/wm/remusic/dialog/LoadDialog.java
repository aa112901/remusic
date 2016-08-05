package com.wm.remusic.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.wm.remusic.R;

/**
 * Created by wm on 2016/5/14.
 */
public class LoadDialog extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //设置无标题
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.loading, container, false);

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
        int dialogHeight = (int) (getActivity().getResources().getDisplayMetrics().heightPixels);
        int dialogWidth = (int) (getActivity().getResources().getDisplayMetrics().widthPixels);
        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
        getDialog().setCanceledOnTouchOutside(true);

    }


}
