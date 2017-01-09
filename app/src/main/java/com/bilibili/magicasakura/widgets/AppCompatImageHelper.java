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

package com.bilibili.magicasakura.widgets;

import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bilibili.magicasakura.utils.DrawableUtils;
import com.bilibili.magicasakura.utils.TintInfo;
import com.bilibili.magicasakura.utils.TintManager;
import com.wm.remusic.R;

/**
 * @author xyczero617@gmail.com
 * @time 15/11/15
 */
public class AppCompatImageHelper extends AppCompatBaseHelper {
    public static final int[] ATTRS = {
            android.R.attr.src,
            R.attr.imageTint,
            R.attr.imageTintMode
    };

    private TintInfo mImageTintInfo;
    private int mImageResId;
    private int mImageTintResId;

    public AppCompatImageHelper(View view, TintManager tintManager) {
        super(view, tintManager);
    }

    @SuppressWarnings("ResourceType")
    @Override
    void loadFromAttribute(AttributeSet attrs, int defStyleAttr) {
        TypedArray array = mView.getContext().obtainStyledAttributes(attrs, ATTRS, defStyleAttr, 0);
        if (array.hasValue(1)) {
            mImageTintResId = array.getResourceId(1, 0);
            if (array.hasValue(2)) {
                setSupportImageTintMode(DrawableUtils.parseTintMode(array.getInt(2, 0), null));
            }
            setSupportImageTint(mImageTintResId);
        } else {
            Drawable image = mTintManager.getDrawable(mImageResId = array.getResourceId(0, 0));
            if (image != null) {
                setImageDrawable(image);
            }
        }
        array.recycle();
    }

    /**
     * External use
     */
    public void setImageDrawable() {
        if (skipNextApply()) return;

        resetTintResource(0);
        setSkipNextApply(false);
    }

    public void setImageResId(int resId) {
        if (mImageResId != resId) {
            resetTintResource(resId);

            if (resId != 0) {
                Drawable image = mTintManager.getDrawable(resId);
                setImageDrawable(image != null ? image : ContextCompat.getDrawable(mView.getContext(), resId));
            }
        }
    }

    public void setImageTintList(int resId, PorterDuff.Mode mode) {
        if (mImageTintResId != resId) {
            mImageTintResId = resId;
            if (mImageTintInfo != null) {
                mImageTintInfo.mHasTintList = false;
                mImageTintInfo.mTintList = null;
            }
            setSupportImageTintMode(mode);
            setSupportImageTint(resId);
        }
    }

    /**
     * Internal use
     */
    private void setImageDrawable(Drawable drawable) {
        if (skipNextApply()) return;
        if (drawable instanceof AnimationDrawable) {
            Log.e("drawable", "instanceof true");
            AnimationDrawable drawable1 = ((AnimationDrawable) drawable);
            ((ImageView) mView).setImageDrawable(drawable1);
            drawable1.start();
        } else {
            ((ImageView) mView).setImageDrawable(drawable);
        }

    }

    private boolean setSupportImageTint(int resId) {
        if (resId != 0) {
            if (mImageTintInfo == null) {
                mImageTintInfo = new TintInfo();
            }
            mImageTintInfo.mHasTintList = true;
            mImageTintInfo.mTintList = mTintManager.getColorStateList(resId);
        }
        return applySupportImageTint();
    }

    private void setSupportImageTintMode(PorterDuff.Mode mode) {
        if (mImageTintResId != 0 && mode != null) {
            if (mImageTintInfo == null) {
                mImageTintInfo = new TintInfo();
            }
            mImageTintInfo.mHasTintMode = true;
            mImageTintInfo.mTintMode = mode;
        }
    }

    private boolean applySupportImageTint() {
        Drawable image = ((ImageView) mView).getDrawable();
        AnimationDrawable animationDrawable = null;
        if (image instanceof AnimationDrawable) {
            Log.e("drawable", "is animationdrawable");
            animationDrawable = ((AnimationDrawable) image);
            //image = animationDrawable;
        }

        if (image != null && mImageTintInfo != null && mImageTintInfo.mHasTintList) {

            if (animationDrawable != null) {
                Log.e("drawable", "is animationdrawable not null");
                Drawable tintDrawable = animationDrawable;
                Log.e("drawable", "start0");
                tintDrawable = DrawableCompat.wrap(tintDrawable);
                Log.e("drawable", "start1");
                if (mImageTintInfo.mHasTintList) {
                    DrawableCompat.setTintList(tintDrawable, mImageTintInfo.mTintList);
                }
                if (mImageTintInfo.mHasTintMode) {
                    DrawableCompat.setTintMode(tintDrawable, mImageTintInfo.mTintMode);
                }
                if (tintDrawable.isStateful()) {
                    tintDrawable.setState(mView.getDrawableState());
                }
                tintDrawable = DrawableCompat.unwrap(tintDrawable);
                setImageDrawable(tintDrawable);
                if (image == tintDrawable) {
                    Log.e("drawable", "invalidateself");
                    // tintDrawable.invalidateSelf();
                }
                return true;
            } else {
                Drawable tintDrawable = image.mutate();
                tintDrawable = DrawableCompat.wrap(tintDrawable);
                if (mImageTintInfo.mHasTintList) {
                    DrawableCompat.setTintList(tintDrawable, mImageTintInfo.mTintList);
                }
                if (mImageTintInfo.mHasTintMode) {
                    DrawableCompat.setTintMode(tintDrawable, mImageTintInfo.mTintMode);
                }
                if (tintDrawable.isStateful()) {
                    tintDrawable.setState(mView.getDrawableState());
                }
                setImageDrawable(tintDrawable);
                if (image == tintDrawable) {
                    tintDrawable.invalidateSelf();
                }
                return true;
            }

        }
        return false;
    }

    private void resetTintResource(int resId/*background resource id*/) {
        mImageResId = resId;
        mImageTintResId = 0;
        if (mImageTintInfo != null) {
            mImageTintInfo.mHasTintList = false;
            mImageTintInfo.mTintList = null;
            mImageTintInfo.mHasTintMode = false;
            mImageTintInfo.mTintMode = null;
        }
    }

    @Override
    public void tint() {
        if (mImageTintResId == 0 || !setSupportImageTint(mImageTintResId)) {
            Drawable drawable = mTintManager.getDrawable(mImageResId);
            if (drawable == null) {
                drawable = mImageResId == 0 ? null : ContextCompat.getDrawable(mView.getContext(), mImageResId);
            }
            setImageDrawable(drawable);
        }
    }

    public interface ImageExtensible {
        void setImageTintList(int resId);

        void setImageTintList(int resId, PorterDuff.Mode mode);
    }
}
