package com.wm.remusic.fragment;

import android.app.Activity;
import android.support.v4.app.DialogFragment;

/**
 * Created by wm on 2016/3/17.
 */
public class AttachDialogFragment extends DialogFragment {

    public Activity mContext;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }


}
