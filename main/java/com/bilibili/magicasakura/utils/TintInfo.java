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

package com.bilibili.magicasakura.utils;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;

import java.util.LinkedList;

/**
 * @author xyczero617@gmail.com
 * @time 15/11/21
 */

public class TintInfo {
    public ColorStateList mTintList;
    public PorterDuff.Mode mTintMode;
    public boolean mHasTintMode;
    public boolean mHasTintList;

    int[] mTintColors;
    int[][] mTintStates;

    public TintInfo() {

    }

    public TintInfo(LinkedList<int[]> stateList, LinkedList<Integer> colorList) {
        if (colorList == null || stateList == null) return;

        mTintColors = new int[colorList.size()];
        for (int i = 0; i < colorList.size(); i++)
            mTintColors[i] = colorList.get(i);
        mTintStates = stateList.toArray(new int[stateList.size()][]);
    }

    public boolean isInvalid() {
        return mTintColors == null || mTintStates == null || mTintColors.length != mTintStates.length;
    }
}
