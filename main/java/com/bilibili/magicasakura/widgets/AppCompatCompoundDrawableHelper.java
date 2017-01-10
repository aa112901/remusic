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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.widget.TextView;

import com.bilibili.magicasakura.utils.DrawableUtils;
import com.bilibili.magicasakura.utils.ThemeUtils;
import com.bilibili.magicasakura.utils.TintInfo;
import com.bilibili.magicasakura.utils.TintManager;
import com.wm.remusic.R;

/**
 * @author xyczero617@gmail.com
 * @time 15/9/26
 */
public class AppCompatCompoundDrawableHelper extends AppCompatBaseHelper {

    private static final int[] ATTR = {
            R.attr.drawableLeftTint,
            R.attr.drawableTopTint,
            R.attr.drawableRightTint,
            R.attr.drawableBottomTint,
            R.attr.drawableLeftTintMode,
            R.attr.drawableTopTintMode,
            R.attr.drawableRightTintMode,
            R.attr.drawableBottomTintMode
    };

    private TintInfo[] mCompoundDrawableTintInfos = new TintInfo[4];

    private int[] mCompoundDrawableResIds = new int[4];
    private int[] mCompoundDrawableTintResIds = new int[4];
    private PorterDuff.Mode[] mCompoundDrawableTintModes = new PorterDuff.Mode[4];

    public AppCompatCompoundDrawableHelper(TextView view, TintManager tintManager) {
        super(view, tintManager);
    }

    @SuppressWarnings("ResourceType")
    @Override
    void loadFromAttribute(AttributeSet attrs, int defStyleAttr) {
        Context context = mView.getContext();
        TypedArray a = context.obtainStyledAttributes(attrs, ATTR, defStyleAttr, 0);
        for (int tintIndex = 0; tintIndex < 4; tintIndex++) {
            int modeIndex = tintIndex + 4;
            mCompoundDrawableResIds[tintIndex] = a.getResourceId(tintIndex, 0);
            mCompoundDrawableTintResIds[tintIndex] = a.getResourceId(tintIndex, 0);
            if (a.hasValue(modeIndex)) {
                mCompoundDrawableTintModes[tintIndex] = DrawableUtils.parseTintMode(a.getInt(modeIndex, 0), null);
            }
        }
        mCompoundDrawableResIds[0] = ThemeUtils.getThemeAttrId(context, attrs, android.R.attr.drawableLeft);
        mCompoundDrawableResIds[1] = ThemeUtils.getThemeAttrId(context, attrs, android.R.attr.drawableTop);
        mCompoundDrawableResIds[2] = ThemeUtils.getThemeAttrId(context, attrs, android.R.attr.drawableRight);
        mCompoundDrawableResIds[3] = ThemeUtils.getThemeAttrId(context, attrs, android.R.attr.drawableBottom);
        a.recycle();

        setCompoundDrawablesWithIntrinsicBounds(
                getCompoundDrawableByPosition(0),
                getCompoundDrawableByPosition(1),
                getCompoundDrawableByPosition(2),
                getCompoundDrawableByPosition(3));
    }

    /**
     * External use
     */
    public void setCompoundDrawablesWithIntrinsicBounds() {
        if (skipNextApply()) return;

        resetTintResource(0, 0, 0, 0);
        setSkipNextApply(false);
    }

    public void setCompoundDrawablesWithIntrinsicBounds(int left, int top, int right, int bottom) {
        resetTintResource(left, top, right, bottom);

        setCompoundDrawablesWithIntrinsicBounds(
                getCompoundDrawableByPosition(0),
                getCompoundDrawableByPosition(1),
                getCompoundDrawableByPosition(2),
                getCompoundDrawableByPosition(3));
    }

    public void setCompoundDrawablesTintList(int... resIds) {
        for (int i = 0; i < resIds.length; i++) {
            mCompoundDrawableTintResIds[i] = resIds[i];
            TintInfo tintInfo = mCompoundDrawableTintInfos[i];
            if (tintInfo != null) {
                tintInfo.mHasTintList = false;
                tintInfo.mTintList = null;
            }
        }
        setCompoundDrawablesWithIntrinsicBounds(
                getCompoundDrawableByPosition(0),
                getCompoundDrawableByPosition(1),
                getCompoundDrawableByPosition(2),
                getCompoundDrawableByPosition(3));
    }

    /**
     * Internal use
     */
    private void setCompoundDrawablesWithIntrinsicBounds(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        if (skipNextApply()) return;

        ((TextView) mView).setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
    }

    private Drawable getCompoundDrawableByPosition(int i) {
        PorterDuff.Mode tintMode = mCompoundDrawableTintModes[i];
        int tintResId = mCompoundDrawableTintResIds[i];
        int resId = mCompoundDrawableResIds[i];
        if (tintResId != 0) {
            setSupportCompoundDrawableTintModeByPosition(i, tintMode);
            return setSupportCompoundDrawableByPosition(i, tintResId);
        } else {
            Drawable drawable = mTintManager.getDrawable(resId);
            if (drawable == null) {
                return resId == 0 ? null : ContextCompat.getDrawable(mView.getContext(), resId);
            }
            return drawable;
        }
    }

    private Drawable setSupportCompoundDrawableByPosition(int position, int tintId) {
        if (tintId != 0) {
            if (mCompoundDrawableTintInfos[position] == null) {
                mCompoundDrawableTintInfos[position] = new TintInfo();
            }
            mCompoundDrawableTintInfos[position].mHasTintList = true;
            mCompoundDrawableTintInfos[position].mTintList = mTintManager.getColorStateList(tintId);
        }
        return applySupportCompoundDrawableTint(position);
    }

    private void setSupportCompoundDrawableTintModeByPosition(int position, PorterDuff.Mode mode) {
        if (mode != null) {
            if (mCompoundDrawableTintInfos[position] == null) {
                mCompoundDrawableTintInfos[position] = new TintInfo();
            }
            mCompoundDrawableTintInfos[position].mHasTintMode = true;
            mCompoundDrawableTintInfos[position].mTintMode = mode;
        }
    }

    private Drawable applySupportCompoundDrawableTint(int position) {
        Drawable originDrawable = ((TextView) mView).getCompoundDrawables()[position];
        Drawable compoundDrawable = originDrawable;
        TintInfo tintInfo = mCompoundDrawableTintInfos[position];
        if (compoundDrawable != null && tintInfo != null && tintInfo.mHasTintList) {
            compoundDrawable = DrawableCompat.wrap(compoundDrawable);
            compoundDrawable.mutate();
            if (tintInfo.mHasTintList) {
                DrawableCompat.setTintList(compoundDrawable, tintInfo.mTintList);
            }
            if (tintInfo.mHasTintMode) {
                DrawableCompat.setTintMode(compoundDrawable, tintInfo.mTintMode);
            }
            if (compoundDrawable.isStateful()) {
                compoundDrawable.setState(originDrawable.getState());
            }
            return compoundDrawable;
        }
        return originDrawable;
    }

    private void resetTintResource(int... resIds/*background resource id:left,top,right,bottom*/) {
        for (int i = 0; i < resIds.length; i++) {
            mCompoundDrawableResIds[i] = resIds[i];
            mCompoundDrawableTintResIds[i] = 0;
            TintInfo tintInfo = mCompoundDrawableTintInfos[i];
            if (tintInfo != null) {
                tintInfo.mHasTintList = false;
                tintInfo.mTintList = null;
                tintInfo.mHasTintMode = false;
                tintInfo.mTintMode = null;
            }
        }
    }

    @Override
    public void tint() {
        setCompoundDrawablesWithIntrinsicBounds(
                getCompoundDrawableByPosition(0),
                getCompoundDrawableByPosition(1),
                getCompoundDrawableByPosition(2),
                getCompoundDrawableByPosition(3));
    }

    public interface CompoundDrawableExtensible {
        void setCompoundDrawableTintList(int leftResId, int topResId, int rightResId, int bottomResId);
    }
}
