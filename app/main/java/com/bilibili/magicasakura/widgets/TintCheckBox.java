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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimatedStateListDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.widget.CompoundButtonCompat;
import android.util.AttributeSet;
import android.widget.CheckBox;

import com.bilibili.magicasakura.utils.ThemeUtils;
import com.bilibili.magicasakura.utils.TintManager;

/**
 * @author xyczero617@gmail.com
 * @time 16/1/27
 */
public class TintCheckBox extends CheckBox implements Tintable, AppCompatBackgroundHelper.BackgroundExtensible,
        AppCompatCompoundButtonHelper.CompoundButtonExtensible, AppCompatTextHelper.TextExtensible {
    private AppCompatBackgroundHelper mBackgroundHelper;
    private AppCompatCompoundButtonHelper mCompoundButtonHelper;
    private AppCompatTextHelper mTextHelper;

    public TintCheckBox(Context context) {
        this(context, null);
    }

    public TintCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.checkboxStyle);
    }

    public TintCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        TintManager tintManager = TintManager.get(getContext());

        mBackgroundHelper = new AppCompatBackgroundHelper(this, tintManager);
        mBackgroundHelper.loadFromAttribute(attrs, defStyleAttr);

        mCompoundButtonHelper = new AppCompatCompoundButtonHelper(this, tintManager);
        mCompoundButtonHelper.loadFromAttribute(attrs, defStyleAttr);

        mTextHelper = new AppCompatTextHelper(this, tintManager);
        mTextHelper.loadFromAttribute(attrs, defStyleAttr);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (ThemeUtils.isSkipAnimatedSelector()) {
            Drawable drawable = CompoundButtonCompat.getButtonDrawable(this);
            try {
                if (ThemeUtils.getWrapperDrawable(drawable) instanceof AnimatedStateListDrawable) {
                    drawable.jumpToCurrentState();
                }
            } catch (NoClassDefFoundError error) {
                error.printStackTrace();
            }
        }
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        if (mTextHelper != null) {
            mTextHelper.setTextColor();
        }
    }

    @Override
    public void setTextColor(ColorStateList colors) {
        super.setTextColor(colors);
        if (mTextHelper != null) {
            mTextHelper.setTextColor();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void setTextAppearance(int resId) {
        super.setTextAppearance(resId);
        if (mTextHelper != null) {
            mTextHelper.setTextAppearanceForTextColor(resId);
        }
    }

    @Override
    public void setTextAppearance(Context context, int resId) {
        super.setTextAppearance(context, resId);
        if (mTextHelper != null) {
            mTextHelper.setTextAppearanceForTextColor(resId);
        }
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        super.setBackgroundDrawable(background);
        if (mBackgroundHelper != null) {
            mBackgroundHelper.setBackgroundDrawableExternal(background);
        }
    }

    @Override
    public void setBackgroundResource(int resId) {
        if (mBackgroundHelper != null) {
            mBackgroundHelper.setBackgroundResId(resId);
        } else {
            super.setBackgroundResource(resId);
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);
        if (mBackgroundHelper != null) {
            mBackgroundHelper.setBackgroundColor(color);
        }
    }

    @Nullable
    @Override
    public void setButtonDrawable(Drawable drawable) {
        super.setButtonDrawable(drawable);
        if (mCompoundButtonHelper != null) {
            mCompoundButtonHelper.setButtonDrawable();
        }
    }

    @Override
    public void setButtonDrawable(@DrawableRes int resId) {
        if (mCompoundButtonHelper != null) {
            mCompoundButtonHelper.setButtonDrawable(resId);
        } else {
            super.setButtonDrawable(resId);
        }
    }

    @Override
    public int getCompoundPaddingLeft() {
        final int value = super.getCompoundPaddingLeft();
        return mCompoundButtonHelper != null
                ? mCompoundButtonHelper.getCompoundPaddingLeft(value)
                : value;
    }

    @Override
    public void setBackgroundTintList(int resId) {
        if (mBackgroundHelper != null) {
            mBackgroundHelper.setBackgroundTintList(resId, null);
        }
    }

    @Override
    public void setBackgroundTintList(int resId, PorterDuff.Mode mode) {
        if (mBackgroundHelper != null) {
            mBackgroundHelper.setBackgroundTintList(resId, mode);
        }
    }

    @Override
    public void setCompoundButtonTintList(int resId) {
        if (mCompoundButtonHelper != null) {
            mCompoundButtonHelper.setButtonDrawableTintList(resId, null);
        }
    }

    @Override
    public void setCompoundButtonTintList(int resId, PorterDuff.Mode mode) {
        if (mCompoundButtonHelper != null) {
            mCompoundButtonHelper.setButtonDrawableTintList(resId, mode);
        }
    }

    @Override
    public void setTextColorById(@ColorRes int colorId) {
        if (mTextHelper != null) {
            mTextHelper.setTextColorById(colorId);
        }
    }

    @Override
    public void tint() {
        if (mTextHelper != null) {
            mTextHelper.tint();
        }
        if (mCompoundButtonHelper != null) {
            mCompoundButtonHelper.tint();
        }
        if (mBackgroundHelper != null) {
            mBackgroundHelper.tint();
        }
    }
}
