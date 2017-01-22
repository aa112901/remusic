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
import android.util.AttributeSet;
import android.view.View;

import com.bilibili.magicasakura.utils.ThemeUtils;
import com.wm.remusic.R;

/**
 * @author xyczero617@gmail.com
 * @time 2015/9/1
 */
public class TintAppAlertDialogDividingView extends View {
    public static final int[] TINT_ATTRS = {
            android.R.attr.background
    };

    public TintAppAlertDialogDividingView(Context context) {
        this(context, null);
    }

    public TintAppAlertDialogDividingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintAppAlertDialogDividingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, TINT_ATTRS);
        if (a.hasValue(0)) {
            if (a.getResourceId(0, 0) == android.R.color.holo_blue_light) {
                setBackgroundColor(ThemeUtils.getThemeAttrColor(context, R.attr.themeColorSecondary));
            }
        }
    }
}
