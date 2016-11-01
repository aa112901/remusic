/*
 * Copyright (C) 2016 Bilibili
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wm.remusic.uitl;

import android.content.Context;
import android.support.annotation.AnimRes;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * @author xyczero617@gmail.com
 * @time 16/6/24
 */

public class SnackAnimationUtil {
    private View mTargetView;
    private long mAutoDismissTime;
    private Animation mSnackInAnim;
    private Animation mSnackOutAnim;
    private SnackAnimationCallback mSnackAnimationCallback;

    public static SnackAnimationUtil with(Context context, @AnimRes int snackInRes, @AnimRes int snackOutRes) {
        return new SnackAnimationUtil(context, snackInRes, snackOutRes);
    }

    private SnackAnimationUtil(Context context, @AnimRes int snackInRes, @AnimRes int snackOutRes) {
        mSnackInAnim = AnimationUtils.loadAnimation(context, snackInRes);
        mSnackOutAnim = AnimationUtils.loadAnimation(context, snackOutRes);
    }

    public SnackAnimationUtil setDismissDelayTime(long autoDismissTime) {
        mAutoDismissTime = autoDismissTime;
        return this;
    }

    public SnackAnimationUtil setTarget(View playView) {
        mTargetView = playView;
        return this;
    }

    public SnackAnimationUtil setDismissDelayCallback(SnackAnimationCallback snackAnimationCallback) {
        mSnackAnimationCallback = snackAnimationCallback;
        return this;
    }

    public void play() {
        if (mTargetView == null || mSnackInAnim == null || mSnackOutAnim == null) {
            return;
        }
        mTargetView.setVisibility(View.VISIBLE);
        mSnackInAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                playSnackOutAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mTargetView.startAnimation(mSnackInAnim);
    }

    private void playSnackOutAnimation() {
        mSnackOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTargetView.setVisibility(View.GONE);
                if (mSnackAnimationCallback != null) {
                    mSnackAnimationCallback.dismissCallback();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mTargetView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mTargetView.startAnimation(mSnackOutAnim);
            }
        }, mAutoDismissTime);
    }

    public interface SnackAnimationCallback {
        void dismissCallback();
    }
}
