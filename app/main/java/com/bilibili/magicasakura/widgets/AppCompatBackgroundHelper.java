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

import com.bilibili.magicasakura.utils.DrawableUtils;
import com.bilibili.magicasakura.utils.ThemeUtils;
import com.bilibili.magicasakura.utils.TintInfo;
import com.bilibili.magicasakura.utils.TintManager;
import com.wm.remusic.R;

/**
 * @author xyczero617@gmail.com
 * @time 15/9/26
 */
public class AppCompatBackgroundHelper extends AppCompatBaseHelper {

    private static final int[] ATTR = {
            android.R.attr.background,
            R.attr.backgroundTint,
            R.attr.backgroundTintMode
    };

    private TintInfo mBackgroundTintInfo;

    private int mBackgroundResId;
    private int mBackgroundTintResId;

    private int mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom;

    public AppCompatBackgroundHelper(View view, TintManager tintManager) {
        super(view, tintManager);
    }

    @SuppressWarnings("ResourceType")
    @Override
    void loadFromAttribute(AttributeSet attrs, int defStyleAttr) {
        initPadding();
        TypedArray array = mView.getContext().obtainStyledAttributes(attrs, ATTR, defStyleAttr, 0);
        if (array.hasValue(1)) {
            mBackgroundTintResId = array.getResourceId(1, 0);
            if (array.hasValue(2)) {
                setSupportBackgroundTintMode(DrawableUtils.parseTintMode(array.getInt(2, 0), null));
            }
            setSupportBackgroundTint(mBackgroundTintResId);
        } else {
            Drawable drawable = mTintManager.getDrawable(mBackgroundResId = array.getResourceId(0, 0));
            if (drawable != null) {
                setBackgroundDrawable(drawable);
            }
        }
        array.recycle();
    }

    /**
     * External use
     *
     * @param background
     */
    public void setBackgroundDrawableExternal(Drawable background) {
        if (skipNextApply()) return;

        resetTintResource(0);
        setSkipNextApply(false);
        recoverPadding(background);
    }

    public void setBackgroundColor(int color) {
        if (skipNextApply()) return;

        resetTintResource(0);
        mView.setBackgroundColor(ThemeUtils.getColor(mView.getContext(), color));
    }

    public void setBackgroundResId(int resId) {
        if (mBackgroundResId != resId) {
            resetTintResource(resId);

            if (resId != 0) {
                Drawable drawable = mTintManager.getDrawable(resId);
                setBackgroundDrawable(
                        drawable != null ? drawable : ContextCompat.getDrawable(mView.getContext(), resId));
            }
        }
    }

    public void setBackgroundTintList(int resId, PorterDuff.Mode mode) {
        if (mBackgroundTintResId != resId) {
            mBackgroundTintResId = resId;
            if (mBackgroundTintInfo != null) {
                mBackgroundTintInfo.mHasTintList = false;
                mBackgroundTintInfo.mTintList = null;
            }
            setSupportBackgroundTintMode(mode);
            setSupportBackgroundTint(resId);
        }
    }

    /**
     * Internal use
     */
    private void setBackgroundDrawable(Drawable drawable) {
        if (skipNextApply()) return;

        setBackground(drawable);
        recoverPadding(drawable);
    }

    private boolean setSupportBackgroundTint(int resId) {
        if (resId != 0) {
            if (mBackgroundTintInfo == null) {
                mBackgroundTintInfo = new TintInfo();
            }
            mBackgroundTintInfo.mHasTintList = true;
            mBackgroundTintInfo.mTintList = mTintManager.getColorStateList(resId);
        }
        return applySupportBackgroundTint();
    }

    private void setSupportBackgroundTintMode(PorterDuff.Mode mode) {
        if (mBackgroundTintResId != 0 && mode != null) {
            if (mBackgroundTintInfo == null) {
                mBackgroundTintInfo = new TintInfo();
            }
            mBackgroundTintInfo.mHasTintMode = true;
            mBackgroundTintInfo.mTintMode = mode;
        }
    }

    private boolean applySupportBackgroundTint() {
        Drawable backgroundDrawable = mView.getBackground();
        if (backgroundDrawable != null && mBackgroundTintInfo != null && mBackgroundTintInfo.mHasTintList) {
            backgroundDrawable = DrawableCompat.wrap(backgroundDrawable);
            backgroundDrawable = backgroundDrawable.mutate();
            if (mBackgroundTintInfo.mHasTintList) {
                DrawableCompat.setTintList(backgroundDrawable, mBackgroundTintInfo.mTintList);
            }
            if (mBackgroundTintInfo.mHasTintMode) {
                DrawableCompat.setTintMode(backgroundDrawable, mBackgroundTintInfo.mTintMode);
            }
            if (backgroundDrawable.isStateful()) {
                backgroundDrawable.setState(mView.getDrawableState());
            }
            setBackgroundDrawable(backgroundDrawable);
            return true;
        }
        return false;
    }

    private void setBackground(Drawable backgroundDrawable) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            mView.setBackgroundDrawable(backgroundDrawable);
        } else {
            mView.setBackground(backgroundDrawable);
        }
    }

    private void resetTintResource(int resId/*background resource id*/) {
        mBackgroundResId = resId;
        mBackgroundTintResId = 0;
        if (mBackgroundTintInfo != null) {
            mBackgroundTintInfo.mHasTintList = false;
            mBackgroundTintInfo.mTintList = null;
            mBackgroundTintInfo.mHasTintMode = false;
            mBackgroundTintInfo.mTintMode = null;
        }
    }

    private void initPadding() {
        mPaddingLeft = mView.getPaddingLeft();
        mPaddingTop = mView.getPaddingTop();
        mPaddingRight = mView.getPaddingRight();
        mPaddingBottom = mView.getPaddingBottom();
    }

    private boolean hasPadding() {
        return mPaddingLeft != 0 || mPaddingRight != 0 || mPaddingTop != 0 || mPaddingBottom != 0;
    }

    private void recoverPadding(Drawable background) {
        if (ThemeUtils.containsNinePatch(background) && hasPadding()) {
            mView.setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom);
        }
    }

    @Override
    public void tint() {
        if (mBackgroundTintResId == 0 || !setSupportBackgroundTint(mBackgroundTintResId)) {
            Drawable drawable = mTintManager.getDrawable(mBackgroundResId);
            if (drawable == null) {
                drawable = mBackgroundResId == 0 ? null : ContextCompat.getDrawable(mView.getContext(), mBackgroundResId);
            }
            setBackgroundDrawable(drawable);
        }
    }

    public interface BackgroundExtensible {
        void setBackgroundTintList(int resId);

        void setBackgroundTintList(int resId, PorterDuff.Mode mode);
    }
}
