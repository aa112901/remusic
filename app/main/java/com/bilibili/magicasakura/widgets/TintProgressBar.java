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
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.bilibili.magicasakura.utils.TintManager;

/**
 * @author xyczero617@gmail.com
 * @time 16/2/4
 */
public class TintProgressBar extends ProgressBar implements Tintable {
    private AppCompatProgressBarHelper mProgressBarHelper;

    public TintProgressBar(Context context) {
        this(context, null);
    }

    public TintProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.progressBarStyle);
    }

    public TintProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        TintManager tintManage = TintManager.get(context);

        mProgressBarHelper = new AppCompatProgressBarHelper(this, tintManage);
        mProgressBarHelper.loadFromAttribute(attrs, defStyleAttr);
    }


    public void setProgressTintList(ColorStateList tint) {
        if (mProgressBarHelper != null) {
            mProgressBarHelper.setSupportProgressTint(tint);
        }
    }

    @Override
    public void tint() {
        if (mProgressBarHelper != null) {
            mProgressBarHelper.tint();
        }
    }
}
