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
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.bilibili.magicasakura.utils.DrawableUtils;
import com.bilibili.magicasakura.utils.TintInfo;
import com.bilibili.magicasakura.utils.TintManager;
import com.wm.remusic.R;

/**
 * @author xyczero617@gmail.com
 * @time 16/4/7
 */
public class AppCompatForegroundHelper extends AppCompatBaseHelper {
    private static final int[] ATTR = {
            android.R.attr.foreground,
            R.attr.foregroundTint,
            R.attr.foregroundTintMode
    };

    private TintInfo mForegroundTintInfo;

    private int mForegroundResId;
    private int mForegroundTintResId;

    public AppCompatForegroundHelper(View view, TintManager tintManager) {
        super(view, tintManager);
    }

    @SuppressWarnings("ResourceType")
    @Override
    void loadFromAttribute(AttributeSet attrs, int defStyleAttr) {
        TypedArray array = mView.getContext().obtainStyledAttributes(attrs, ATTR, defStyleAttr, 0);
        if (array.hasValue(1)) {
            mForegroundResId = array.getResourceId(1, 0);
            if (array.hasValue(2)) {
                setSupportForegroundTintMode(DrawableUtils.parseTintMode(array.getInt(2, 0), null));
            }
            setSupportForegroundTint(mForegroundTintResId);
        } else {
            Drawable drawable = mTintManager.getDrawable(mForegroundResId = array.getResourceId(0, 0));
            if (drawable != null) {
                setForegroundDrawable(drawable);
            }
        }
        array.recycle();
    }

    /**
     * External use
     *
     * @param foreground
     */
    public void setForegroundDrawableExternal(Drawable foreground) {
        if (skipNextApply()) return;

        resetTintResource(0);
        setSkipNextApply(false);
    }

    public void setForegroundResId(int resId) {
        if (mForegroundResId != resId) {
            resetTintResource(resId);

            if (resId != 0) {
                Drawable drawable = mTintManager.getDrawable(resId);
                setForegroundDrawable(
                        drawable != null ? drawable : ContextCompat.getDrawable(mView.getContext(), resId));
            }
        }
    }

    public void setForegroundTintList(int resId, PorterDuff.Mode mode) {
        if (mForegroundTintResId != resId) {
            mForegroundTintResId = resId;
            if (mForegroundTintInfo != null) {
                mForegroundTintInfo.mHasTintList = false;
                mForegroundTintInfo.mTintList = null;
            }
            setSupportForegroundTintMode(mode);
            setSupportForegroundTint(resId);
        }
    }

    /**
     * Internal use
     */
    private void setForegroundDrawable(Drawable drawable) {
        if (skipNextApply()) return;

        setForeground(drawable);
    }

    private boolean setSupportForegroundTint(int resId) {
        if (resId != 0) {
            if (mForegroundTintInfo == null) {
                mForegroundTintInfo = new TintInfo();
            }
            mForegroundTintInfo.mHasTintList = true;
            mForegroundTintInfo.mTintList = mTintManager.getColorStateList(resId);
        }
        return applySupportForegroundTint();
    }

    private void setSupportForegroundTintMode(PorterDuff.Mode mode) {
        if (mForegroundTintResId != 0 && mode != null) {
            if (mForegroundTintInfo == null) {
                mForegroundTintInfo = new TintInfo();
            }
            mForegroundTintInfo.mHasTintMode = true;
            mForegroundTintInfo.mTintMode = mode;
        }
    }

    private boolean applySupportForegroundTint() {
        Drawable foregroundDrawable = getForeground();
        if (foregroundDrawable != null && mForegroundTintInfo != null && mForegroundTintInfo.mHasTintList) {
            foregroundDrawable = DrawableCompat.wrap(foregroundDrawable);
            foregroundDrawable = foregroundDrawable.mutate();
            if (mForegroundTintInfo.mHasTintList) {
                DrawableCompat.setTintList(foregroundDrawable, mForegroundTintInfo.mTintList);
            }
            if (mForegroundTintInfo.mHasTintMode) {
                DrawableCompat.setTintMode(foregroundDrawable, mForegroundTintInfo.mTintMode);
            }
            if (foregroundDrawable.isStateful()) {
                foregroundDrawable.setState(mView.getDrawableState());
            }
            setForegroundDrawable(foregroundDrawable);
            return true;
        }
        return false;
    }

    private Drawable getForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mView.getForeground();
        } else if (mView instanceof FrameLayout) {
            ((FrameLayout) mView).getForeground();
        }
        return null;
    }


    private void setForeground(Drawable foreground) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mView.setForeground(foreground);
        } else if (mView instanceof FrameLayout) {
            ((FrameLayout) mView).setForeground(foreground);
        }
    }

    private void resetTintResource(int resId/*foreground resource id*/) {
        mForegroundResId = resId;
        mForegroundTintResId = 0;
        if (mForegroundTintInfo != null) {
            mForegroundTintInfo.mHasTintList = false;
            mForegroundTintInfo.mTintList = null;
            mForegroundTintInfo.mHasTintMode = false;
            mForegroundTintInfo.mTintMode = null;
        }
    }

    @Override
    public void tint() {
        if (mForegroundTintResId == 0 || !setSupportForegroundTint(mForegroundTintResId)) {
            Drawable drawable = mTintManager.getDrawable(mForegroundResId);
            if (drawable == null) {
                drawable = mForegroundResId == 0 ? null : ContextCompat.getDrawable(mView.getContext(), mForegroundResId);
            }
            setForegroundDrawable(drawable);
        }
    }

    public interface ForegroundExtensible {
        void setForegroundTintList(int resId);

        void setForegroundTintList(int resId, PorterDuff.Mode mode);
    }
}
