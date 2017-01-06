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

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.SparseArray;

import com.bilibili.magicasakura.drawables.FilterableStateListDrawable;
import com.wm.remusic.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author xyczero617@gmail.com
 * @time 16/2/22
 */
public class StateListDrawableUtils extends DrawableUtils {

    @Override
    protected Drawable inflateDrawable(Context context, XmlPullParser parser, AttributeSet attrs) throws IOException, XmlPullParserException {
        StateListDrawable sd = null;
        ArrayList<int[]> states = new ArrayList<>();
        ArrayList<Drawable> drawables = new ArrayList<>();
        SparseArray<ColorFilter> mColorFilterMap = null;
        final int innerDepth = parser.getDepth() + 1;
        int type;
        int depth;

        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && ((depth = parser.getDepth()) >= innerDepth
                || type != XmlPullParser.END_TAG)) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }

            if (depth > innerDepth || !parser.getName().equals("item")) {
                continue;
            }

            Drawable dr = getAttrDrawable(context, attrs, android.R.attr.drawable);

            states.add(extractStateSet(attrs));

            // Loading child elements modifies the state of the AttributeSet's
            // underlying parser, so it needs to happen after obtaining
            // attributes and extracting states.
            if (dr == null) {
                while ((type = parser.next()) == XmlPullParser.TEXT) {
                }
                if (type != XmlPullParser.START_TAG) {
                    throw new XmlPullParserException(
                            parser.getPositionDescription()
                                    + ": <item> tag requires a 'drawable' attribute or "
                                    + "child tag defining a drawable");
                }
                dr = createFromXmlInner(context, parser, attrs);
            } else {
                ColorFilter colorFilter = getAttrColorFilter(context, attrs, R.attr.drawableTint, R.attr.drawableTintMode);
                if (colorFilter != null) {
                    if (mColorFilterMap == null) {
                        mColorFilterMap = new SparseArray<>();
                    }
                    mColorFilterMap.put(drawables.size(), colorFilter);
                }
            }
            drawables.add(dr);
        }
        if (states.size() >= 1) {
            if (mColorFilterMap != null) {
                sd = new FilterableStateListDrawable();
                for (int i = 0; i < states.size(); i++) {
                    ((FilterableStateListDrawable) sd).addState(states.get(i), drawables.get(i), mColorFilterMap.get(i));
                }
            } else {
                sd = new StateListDrawable();
                for (int i = 0; i < states.size(); i++) {
                    sd.addState(states.get(i), drawables.get(i));
                }
            }
        }
        return sd;
    }
}
