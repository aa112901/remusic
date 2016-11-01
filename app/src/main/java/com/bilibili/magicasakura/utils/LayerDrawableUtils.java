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
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;

import com.wm.remusic.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * @author xyczero617@gmail.com
 * @time 16/3/17
 */
public class LayerDrawableUtils extends DrawableUtils {
    private static final int STEP = 1;

    private static final int[] ATTRS = new int[]{
            android.R.attr.left, android.R.attr.top, android.R.attr.right,
            android.R.attr.bottom, android.R.attr.id};

    @Override
    protected Drawable inflateDrawable(Context context, XmlPullParser parser, AttributeSet attrs) throws IOException, XmlPullParserException {
        final int innerDepth = parser.getDepth() + 1;
        int type;
        int depth;
        int layerAttrUseCount = 0;
        int drawableUseCount = 0;
        int space = STEP << 1;
        //L,T,R,B,S,E,id
        int[][] childLayersAttrs = new int[space][ATTRS.length];
        Drawable[] drawables = new Drawable[space];

        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && ((depth = parser.getDepth()) >= innerDepth || type != XmlPullParser.END_TAG)) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }

            if (depth > innerDepth || !parser.getName().equals("item")) {
                continue;
            }

            if (layerAttrUseCount >= childLayersAttrs.length) {
                int[][] dstInt = new int[drawables.length + STEP][ATTRS.length];
                System.arraycopy(childLayersAttrs, 0, dstInt, 0, childLayersAttrs.length);
                childLayersAttrs = dstInt;
            }
            updateLayerAttrs(context, attrs, childLayersAttrs[layerAttrUseCount]);
            layerAttrUseCount++;

            Drawable drawable = getAttrDrawable(context, attrs, android.R.attr.drawable);

            // If the layer doesn't have a drawable or unresolved theme
            // attribute for a drawable, attempt to parse one from the child
            // element.
            if (drawable == null) {
                while ((type = parser.next()) == XmlPullParser.TEXT) {
                }
                if (type != XmlPullParser.START_TAG) {
                    throw new XmlPullParserException(parser.getPositionDescription()
                            + ": <item> tag requires a 'drawable' attribute or "
                            + "child tag defining a drawable");
                }
                drawable = createFromXmlInner(context, parser, attrs);
            } else {
                final ColorStateList cls = getTintColorList(context, attrs, R.attr.drawableTint);
                if (cls != null) {
                    drawable = ThemeUtils.tintDrawable(drawable, cls, getTintMode(context, attrs, R.attr.drawableTintMode));
                }
            }

            if (drawable != null) {
                if (drawableUseCount >= drawables.length) {
                    Drawable[] dst = new Drawable[drawables.length + STEP];
                    System.arraycopy(drawables, 0, dst, 0, drawables.length);
                    drawables = dst;
                }
                drawables[drawableUseCount] = drawable;
                drawableUseCount++;
            }
        }

        if (drawables[0] == null || drawableUseCount != layerAttrUseCount) {
            return null;
        } else {
            LayerDrawable layerDrawable = new LayerDrawable(drawables);
            for (int i = 0; i < drawables.length; i++) {
                int[] childLayersAttr = childLayersAttrs[i];
                if (childLayersAttr[0] != 0 || childLayersAttr[1] != 0 || childLayersAttr[2] != 0 || childLayersAttr[3] != 0) {
                    layerDrawable.setLayerInset(i, childLayersAttr[0], childLayersAttr[1], childLayersAttr[2], childLayersAttr[3]);
                }
                if (childLayersAttr[4] != 0) {
                    layerDrawable.setId(i, childLayersAttr[4]);
                }
            }
            return layerDrawable;
        }
    }

    void updateLayerAttrs(Context context, AttributeSet attrs, int[] childLayersAttrs) {
        childLayersAttrs[0] = getAttrDimensionPixelOffset(context, attrs, ATTRS[0]);
        childLayersAttrs[1] = getAttrDimensionPixelOffset(context, attrs, ATTRS[1]);
        childLayersAttrs[2] = getAttrDimensionPixelOffset(context, attrs, ATTRS[2]);
        childLayersAttrs[3] = getAttrDimensionPixelOffset(context, attrs, ATTRS[3]);
        childLayersAttrs[4] = getAttrResourceId(context, attrs, ATTRS[4], 0);
    }
}
