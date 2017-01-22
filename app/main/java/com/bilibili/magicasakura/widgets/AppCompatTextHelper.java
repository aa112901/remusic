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

import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.ColorRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.bilibili.magicasakura.utils.TintInfo;
import com.bilibili.magicasakura.utils.TintManager;
import com.wm.remusic.R;

/**
 * @author xyczero617@gmail.com
 * @time 15/9/26
 */
public class AppCompatTextHelper extends AppCompatBaseHelper {

    //If writing like this:
    //int[] ATTRS = { R.attr.tintText, android.R.attr.textColor, android.R.attr.textColorLink, ...};
    //we can't get textColor value when api is below 20;
    private static final int[] ATTRS = {
            android.R.attr.textColor,
            android.R.attr.textColorLink,
            android.R.attr.textAppearance,
    };

    private int mTextColorId;
    private int mTextLinkColorId;

    private TintInfo mTextColorTintInfo;
    private TintInfo mTextLinkColorTintInfo;

    public AppCompatTextHelper(View view, TintManager tintManager) {
        super(view, tintManager);
    }

    @SuppressWarnings("ResourceType")
    @Override
    void loadFromAttribute(AttributeSet attrs, int defStyleAttr) {
        TypedArray array = mView.getContext().obtainStyledAttributes(attrs, ATTRS, defStyleAttr, 0);

        int textColorId = array.getResourceId(0, 0);
        if (textColorId == 0) {
            setTextAppearanceForTextColor(array.getResourceId(2, 0), false);
        } else {
            setTextColor(textColorId);
        }

        if (array.hasValue(1)) {
            setLinkTextColor(array.getResourceId(1, 0));
        }
        array.recycle();
    }


    /**
     * External use
     */
    public void setTextColor() {
        if (skipNextApply()) return;

        resetTextColorTintResource(0);
        setSkipNextApply(false);
    }

    /**
     * useless for setLinkTextColor is final
     */
    @Deprecated
    public void setTextLinkColor() {
        if (skipNextApply()) return;

        resetTextLinkColorTintResource(0);
        setSkipNextApply(false);
    }

    public void setTextAppearanceForTextColor(int resId) {
        resetTextColorTintResource(0);
        setTextAppearanceForTextColor(resId, true);
    }

    public void setTextAppearanceForTextColor(int resId, boolean isForced) {
        boolean isTextColorForced = isForced || mTextColorId == 0;
        TypedArray appearance = mView.getContext().obtainStyledAttributes(resId, R.styleable.TextAppearance);
        if (appearance.hasValue(R.styleable.TextAppearance_android_textColor) && isTextColorForced) {
            setTextColor(appearance.getResourceId(R.styleable.TextAppearance_android_textColor, 0));
        }
        appearance.recycle();
    }

    public void setTextColorById(@ColorRes int colorId) {
        setTextColor(colorId);
    }

    /**
     * Internal use
     */
    private void setTextColor(ColorStateList tint) {
        if (skipNextApply()) return;

        ((TextView) mView).setTextColor(tint);
    }

    private void setTextColor(@ColorRes int resId) {
        if (mTextColorId != resId) {
            resetTextColorTintResource(resId);

            if (resId != 0) {
                setSupportTextColorTint(resId);
            }
        }
    }

    private void setLinkTextColor(@ColorRes int resId) {
        if (mTextLinkColorId != resId) {
            resetTextLinkColorTintResource(resId);

            if (resId != 0) {
                setSupportTextLinkColorTint(resId);
            }
        }
    }

    private void setSupportTextColorTint(int resId) {
        if (resId != 0) {
            if (mTextColorTintInfo == null) {
                mTextColorTintInfo = new TintInfo();
            }
            mTextColorTintInfo.mHasTintList = true;
            mTextColorTintInfo.mTintList = mTintManager.getColorStateList(resId);
        }
        applySupportTextColorTint();
    }

    private void setSupportTextLinkColorTint(int resId) {
        if (resId != 0) {
            if (mTextLinkColorTintInfo == null) {
                mTextLinkColorTintInfo = new TintInfo();
            }
            mTextLinkColorTintInfo.mHasTintList = true;
            mTextLinkColorTintInfo.mTintList = mTintManager.getColorStateList(resId);
        }
        applySupportTextLinkColorTint();
    }

    private void applySupportTextColorTint() {
        if (mTextColorTintInfo != null && mTextColorTintInfo.mHasTintList) {
            setTextColor(mTextColorTintInfo.mTintList);
        }
    }

    private void applySupportTextLinkColorTint() {
        if (mTextLinkColorTintInfo != null && mTextLinkColorTintInfo.mHasTintList) {
            ((TextView) mView).setLinkTextColor(mTextLinkColorTintInfo.mTintList);
        }
    }

    private void resetTextColorTintResource(@ColorRes int resId/*text resource id*/) {
        mTextColorId = resId;
        if (mTextColorTintInfo != null) {
            mTextColorTintInfo.mHasTintList = false;
            mTextColorTintInfo.mTintList = null;
        }
    }

    private void resetTextLinkColorTintResource(@ColorRes int resId/*text resource id*/) {
        mTextLinkColorId = resId;
        if (mTextLinkColorTintInfo != null) {
            mTextLinkColorTintInfo.mHasTintList = false;
            mTextLinkColorTintInfo.mTintList = null;
        }
    }

    @Override
    public void tint() {
        if (mTextColorId != 0) {
            setSupportTextColorTint(mTextColorId);
        }
        if (mTextLinkColorId != 0) {
            setSupportTextLinkColorTint(mTextLinkColorId);
        }
    }

    public interface TextExtensible {
        void setTextColorById(@ColorRes int colorId);
    }
}
