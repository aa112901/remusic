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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.StateSet;
import android.util.TypedValue;
import android.util.Xml;

import com.wm.remusic.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * @author xyczero617@gmail.com
 * @time 16/2/22
 */
public abstract class DrawableUtils {

    protected abstract Drawable inflateDrawable(Context context, XmlPullParser parser, AttributeSet attrs) throws IOException, XmlPullParserException;

    static Drawable createDrawable(Context context, int resId) {
        if (resId <= 0) return null;

        final TypedValue typedValue = new TypedValue();
        final Resources res = context.getResources();
        res.getValue(resId, typedValue, true);
        Drawable dr = null;

        if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT
                && typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            dr = new ColorDrawable(ThemeUtils.replaceColorById(context, resId));
        } else {
            try {
                if (typedValue.string != null && typedValue.string.toString().endsWith("xml")) {
                    final XmlResourceParser rp = res.getXml(resId);
                    final AttributeSet attrs = Xml.asAttributeSet(rp);
                    int type;

                    while ((type = rp.next()) != XmlPullParser.START_TAG &&
                            type != XmlPullParser.END_DOCUMENT) {
                        // Empty loop
                    }
                    if (type != XmlPullParser.START_TAG) {
                        throw new XmlPullParserException("No start tag found");
                    }

                    dr = createFromXmlInner(context, rp, attrs);
                    rp.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
        }
        return dr;
    }

    static Drawable createFromXmlInner(Context context, XmlPullParser parser, AttributeSet attrs) throws IOException, XmlPullParserException {
        final DrawableUtils drawableUtils;

        final String name = parser.getName();
        switch (name) {
            case "selector":
                drawableUtils = new StateListDrawableUtils();
                break;
            case "shape":
                drawableUtils = new GradientDrawableUtils();
                break;
            case "layer-list":
                drawableUtils = new LayerDrawableUtils();
                break;
            default:
                drawableUtils = null;
        }
        return drawableUtils == null ? null : drawableUtils.inflateDrawable(context, parser, attrs);
    }

    /**
     * Extracts state_ attributes from an attribute set.
     *
     * @param attrs The attribute set.
     * @return An array of state_ attributes.
     */
    protected int[] extractStateSet(AttributeSet attrs) {
        int j = 0;
        final int numAttrs = attrs.getAttributeCount();
        int[] states = new int[numAttrs];
        for (int i = 0; i < numAttrs; i++) {
            final int stateResId = attrs.getAttributeNameResource(i);
            if (stateResId == 0) {
                break;
            } else if (stateResId == android.R.attr.drawable
                    || stateResId == android.R.attr.id
                    || stateResId == R.attr.drawableTint
                    || stateResId == R.attr.drawableTintMode) {
                // Ignore attributes from StateListDrawableItem and
                // AnimatedStateListDrawableItem.
                continue;
            } else {
                states[j++] = attrs.getAttributeBooleanValue(i, false)
                        ? stateResId : -stateResId;
            }
        }
        states = StateSet.trimStateSet(states, j);
        return states;
    }

    static int getAttrTintColor(Context context, AttributeSet attrs, int attr, int defaultValue) {
        final TypedArray a = obtainAttributes(context.getResources(), context.getTheme(), attrs, new int[]{attr});
        final int tintColor = ThemeUtils.replaceColor(context, a.getColor(0, defaultValue));
        a.recycle();
        return tintColor;
    }

    static Drawable getAttrDrawable(Context context, AttributeSet attrs, int attr) {
        final TypedArray a = obtainAttributes(context.getResources(), context.getTheme(), attrs, new int[]{attr});
        final int resId = a.getResourceId(0, 0);
        Drawable drawable = null;
        if (resId != 0) {
            drawable = createDrawable(context, resId);
            if (drawable == null) {
                drawable = a.getDrawable(0);
            }
        }
        a.recycle();
        return drawable;
    }

    static ColorFilter getAttrColorFilter(Context context, AttributeSet attrs, int tintAttr, int tintModeAttr) {
        final int color = getAttrColor(context, attrs, tintAttr, Color.TRANSPARENT);
        if (color == Color.TRANSPARENT) return null;
        return new PorterDuffColorFilter(color, getTintMode(context, attrs, tintModeAttr));
    }

    static ColorStateList getTintColorList(Context context, AttributeSet attrs, int tintAttr) {
        TypedArray a = obtainAttributes(context.getResources(), context.getTheme(), attrs, new int[]{tintAttr});
        if (!a.hasValue(0)) {
            a.recycle();
            return null;
        }
        final ColorStateList cls = TintManager.get(context).getColorStateList(a.getResourceId(0, 0));
        a.recycle();
        return cls;
    }

    static PorterDuff.Mode getTintMode(Context context, AttributeSet attrs, int tintModeAttr) {
        TypedArray a = obtainAttributes(context.getResources(), context.getTheme(), attrs, new int[]{tintModeAttr});
        final int tintModeValue = a.getInt(0, 0);
        a.recycle();
        return parseTintMode(tintModeValue, PorterDuff.Mode.SRC_IN);
    }

    static int getAttrDimensionPixelSize(Context context, AttributeSet attrs, int attr) {
        return getAttrDimensionPixelSize(context, attrs, attr, 0);
    }

    static int getAttrDimensionPixelSize(Context context, AttributeSet attrs, int attr, int defaultValue) {
        TypedArray a = obtainAttributes(context.getResources(), context.getTheme(), attrs, new int[]{attr});
        final int value = a.getDimensionPixelSize(0, defaultValue);
        a.recycle();
        return value;
    }

    static int getAttrColor(Context context, AttributeSet attrs, int attr, int defaultValue) {
        TypedArray a = obtainAttributes(context.getResources(), context.getTheme(), attrs, new int[]{attr});
        final int colorId = a.getResourceId(0, 0);
        final int value = colorId != 0 ? ThemeUtils.replaceColorById(context, colorId) : ThemeUtils.replaceColor(context, a.getColor(0, defaultValue));
        a.recycle();
        return value;
    }

    static int getAttrDimensionPixelOffset(Context context, AttributeSet attrs, int attr) {
        return getAttrDimensionPixelOffset(context, attrs, attr, 0);
    }

    static int getAttrDimensionPixelOffset(Context context, AttributeSet attrs, int attr, int defaultValue) {
        TypedArray a = obtainAttributes(context.getResources(), context.getTheme(), attrs, new int[]{attr});
        final int value = a.getDimensionPixelOffset(0, defaultValue);
        a.recycle();
        return value;
    }

    static float getAttrDimension(Context context, AttributeSet attrs, int attr) {
        return getAttrDimension(context, attrs, attr, 0);
    }

    static float getAttrDimension(Context context, AttributeSet attrs, int attr, int defaultValue) {
        TypedArray a = obtainAttributes(context.getResources(), context.getTheme(), attrs, new int[]{attr});
        final float value = a.getDimension(0, defaultValue);
        a.recycle();
        return value;
    }

    static float getAttrFloat(Context context, AttributeSet attrs, int attr, float defaultValue) {
        TypedArray a = obtainAttributes(context.getResources(), context.getTheme(), attrs, new int[]{attr});
        final float alphaMod = a.getFloat(0, defaultValue);
        a.recycle();
        return alphaMod;
    }

    static int getAttrInt(Context context, AttributeSet attrs, int attr, int defaultValue) {
        TypedArray a = obtainAttributes(context.getResources(), context.getTheme(), attrs, new int[]{attr});
        final int value = a.getInt(0, defaultValue);
        a.recycle();
        return value;
    }

    static boolean getAttrBoolean(Context context, AttributeSet attrs, int attr, boolean defaultValue) {
        TypedArray a = obtainAttributes(context.getResources(), context.getTheme(), attrs, new int[]{attr});
        final boolean value = a.getBoolean(0, defaultValue);
        a.recycle();
        return value;
    }

    static boolean getAttrHasValue(Context context, AttributeSet attrs, int attr) {
        TypedArray a = obtainAttributes(context.getResources(), context.getTheme(), attrs, new int[]{attr});
        final boolean flag = a.hasValue(0);
        a.recycle();
        return flag;
    }

    static int getAttrResourceId(Context context, AttributeSet attrs, int attr, int defaultValue) {
        TypedArray a = obtainAttributes(context.getResources(), context.getTheme(), attrs, new int[]{attr});
        final int id = a.getResourceId(0, defaultValue);
        a.recycle();
        return id;
    }

    static TypedArray obtainAttributes(
            Resources res, Resources.Theme theme, AttributeSet set, int[] attrs) {
        if (theme == null) {
            return res.obtainAttributes(set, attrs);
        }
        return theme.obtainStyledAttributes(set, attrs, 0, 0);
    }

    public static PorterDuff.Mode parseTintMode(int value, PorterDuff.Mode defaultMode) {
        switch (value) {
            case 3:
                return PorterDuff.Mode.SRC_OVER;
            case 5:
                return PorterDuff.Mode.SRC_IN;
            case 9:
                return PorterDuff.Mode.SRC_ATOP;
            case 14:
                return PorterDuff.Mode.MULTIPLY;
            case 15:
                return PorterDuff.Mode.SCREEN;
            case 16:
                return Build.VERSION.SDK_INT >= 11 ? PorterDuff.Mode.valueOf("ADD")
                        : defaultMode;
            default:
                return defaultMode;
        }
    }
}
