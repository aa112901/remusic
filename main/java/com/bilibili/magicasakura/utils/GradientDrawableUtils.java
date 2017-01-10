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
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * @author xyczero617@gmail.com
 * @time 16/2/22
 */
public class GradientDrawableUtils extends DrawableUtils {
    private static Field sPaddingField;
    private static Field sStPaddingField;
    private static Field sStGradientPositions;
    private static Field sStGradientAngle;

    @Override
    protected Drawable inflateDrawable(Context context, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
        GradientDrawable gradientDrawable = new GradientDrawable();
        inflateGradientRootElement(context, attrs, gradientDrawable);

        int type;
        final int innerDepth = parser.getDepth() + 1;
        int depth;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && ((depth = parser.getDepth()) >= innerDepth
                || type != XmlPullParser.END_TAG)) {
            if (type != XmlPullParser.START_TAG) {
                continue;
            }

            if (depth > innerDepth) {
                continue;
            }

            String name = parser.getName();

            if (name.equals("size")) {
                final int width = getAttrDimensionPixelSize(context, attrs, android.R.attr.width);
                final int height = getAttrDimensionPixelSize(context, attrs, android.R.attr.height);
                gradientDrawable.setSize(width, height);
            } else if (name.equals("gradient") && Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                final float centerX = getAttrFloatOrFraction(context, attrs, android.R.attr.centerX, 0.5f, 1.0f, 1.0f);
                final float centerY = getAttrFloatOrFraction(context, attrs, android.R.attr.centerY, 0.5f, 1.0f, 1.0f);
                gradientDrawable.setGradientCenter(centerX, centerY);
                final boolean useLevel = getAttrBoolean(context, attrs, android.R.attr.useLevel, false);
                gradientDrawable.setUseLevel(useLevel);
                final int gradientType = getAttrInt(context, attrs, android.R.attr.type, 0);
                gradientDrawable.setGradientType(gradientType);
                final int startColor = getAttrColor(context, attrs, android.R.attr.startColor, Color.TRANSPARENT);
                final int centerColor = getAttrColor(context, attrs, android.R.attr.centerColor, Color.TRANSPARENT);
                final int endColor = getAttrColor(context, attrs, android.R.attr.endColor, Color.TRANSPARENT);
                if (!getAttrHasValue(context, attrs, android.R.attr.centerColor)) {
                    gradientDrawable.setColors(new int[]{startColor, endColor});
                } else {
                    gradientDrawable.setColors(new int[]{startColor, centerColor, endColor});
                    setStGradientPositions(gradientDrawable.getConstantState(), 0.0f, centerX != 0.5f ? centerX : centerY, 1f);
                }

                if (gradientType == GradientDrawable.LINEAR_GRADIENT) {
                    int angle = (int) getAttrFloat(context, attrs, android.R.attr.angle, 0.0f);
                    angle %= 360;

                    if (angle % 45 != 0) {
                        throw new XmlPullParserException("<gradient> tag requires"
                                + "'angle' attribute to "
                                + "be a multiple of 45");
                    }

                    setStGradientAngle(gradientDrawable.getConstantState(), angle);

                    switch (angle) {
                        case 0:
                            gradientDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
                            break;
                        case 45:
                            gradientDrawable.setOrientation(GradientDrawable.Orientation.BL_TR);
                            break;
                        case 90:
                            gradientDrawable.setOrientation(GradientDrawable.Orientation.BOTTOM_TOP);
                            break;
                        case 135:
                            gradientDrawable.setOrientation(GradientDrawable.Orientation.BR_TL);
                            break;
                        case 180:
                            gradientDrawable.setOrientation(GradientDrawable.Orientation.RIGHT_LEFT);
                            break;
                        case 225:
                            gradientDrawable.setOrientation(GradientDrawable.Orientation.TR_BL);
                            break;
                        case 270:
                            gradientDrawable.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
                            break;
                        case 315:
                            gradientDrawable.setOrientation(GradientDrawable.Orientation.TL_BR);
                            break;
                    }
                } else {
                    setGradientRadius(context, attrs, gradientDrawable, gradientType);
                }
            } else if (name.equals("solid")) {
                int color = getAttrColor(context, attrs, android.R.attr.color, Color.TRANSPARENT);
                gradientDrawable.setColor(getAlphaColor(color, getAttrFloat(context, attrs, android.R.attr.alpha, 1.0f)));
            } else if (name.equals("stroke")) {
                final float alphaMod = getAttrFloat(context, attrs, android.R.attr.alpha, 1.0f);
                final int strokeColor = getAttrColor(context, attrs, android.R.attr.color, Color.TRANSPARENT);
                final int strokeWidth = getAttrDimensionPixelSize(context, attrs, android.R.attr.width);
                final float dashWidth = getAttrDimension(context, attrs, android.R.attr.dashWidth);
                if (dashWidth != 0.0f) {
                    final float dashGap = getAttrDimension(context, attrs, android.R.attr.dashGap);
                    gradientDrawable.setStroke(strokeWidth, getAlphaColor(strokeColor, alphaMod), dashWidth, dashGap);
                } else {
                    gradientDrawable.setStroke(strokeWidth, getAlphaColor(strokeColor, alphaMod));
                }
            } else if (name.equals("corners")) {
                final int radius = getAttrDimensionPixelSize(context, attrs, android.R.attr.radius);
                gradientDrawable.setCornerRadius(radius);

                final int topLeftRadius = getAttrDimensionPixelSize(context, attrs, android.R.attr.topLeftRadius, radius);
                final int topRightRadius = getAttrDimensionPixelSize(context, attrs, android.R.attr.topRightRadius, radius);
                final int bottomLeftRadius = getAttrDimensionPixelSize(context, attrs, android.R.attr.bottomLeftRadius, radius);
                final int bottomRightRadius = getAttrDimensionPixelSize(context, attrs, android.R.attr.bottomRightRadius, radius);
                if (topLeftRadius != radius || topRightRadius != radius ||
                        bottomLeftRadius != radius || bottomRightRadius != radius) {
                    // The corner radii are specified in clockwise order (see Path.addRoundRect())
                    gradientDrawable.setCornerRadii(new float[]{
                            topLeftRadius, topLeftRadius,
                            topRightRadius, topRightRadius,
                            bottomRightRadius, bottomRightRadius,
                            bottomLeftRadius, bottomLeftRadius
                    });
                }
            } else if (name.equals("padding")) {
                final int paddingLeft = getAttrDimensionPixelOffset(context, attrs, android.R.attr.left);
                final int paddingTop = getAttrDimensionPixelOffset(context, attrs, android.R.attr.top);
                final int paddingRight = getAttrDimensionPixelOffset(context, attrs, android.R.attr.right);
                final int paddingBottom = getAttrDimensionPixelOffset(context, attrs, android.R.attr.bottom);
                if (paddingLeft != 0 || paddingTop != 0 || paddingRight != 0 || paddingBottom != 0) {
                    final Rect pad = new Rect();
                    pad.set(paddingLeft, paddingTop, paddingRight, paddingBottom);
                    try {
                        if (sPaddingField == null) {
                            sPaddingField = GradientDrawable.class.getDeclaredField("mPadding");
                            sPaddingField.setAccessible(true);
                        }
                        sPaddingField.set(gradientDrawable, pad);
                        if (sStPaddingField == null) {
                            sStPaddingField = Class.forName("android.graphics.drawable.GradientDrawable$GradientState").getDeclaredField("mPadding");
                            sStPaddingField.setAccessible(true);
                        }
                        sStPaddingField.set(gradientDrawable.getConstantState(), pad);
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Log.w("drawable", "Bad element under <shape>: " + name);
            }
        }
        return gradientDrawable;
    }

    void inflateGradientRootElement(Context context, AttributeSet attrs, GradientDrawable gradientDrawable) {
        int shape = getAttrInt(context, attrs, android.R.attr.shape, GradientDrawable.RECTANGLE);
        gradientDrawable.setShape(shape);
        boolean dither = getAttrBoolean(context, attrs, android.R.attr.dither, false);
        gradientDrawable.setDither(dither);
    }

    void setGradientRadius(Context context, AttributeSet attrs, GradientDrawable drawable, int gradientType) throws XmlPullParserException {
        TypedArray a = obtainAttributes(context.getResources(), context.getTheme(), attrs, new int[]{android.R.attr.gradientRadius});
        TypedValue value = a.peekValue(0);
        if (value != null) {
            boolean radiusRel = value.type == TypedValue.TYPE_FRACTION;
            drawable.setGradientRadius(radiusRel ? value.getFraction(1.0f, 1.0f) : value.getFloat());
        } else if (gradientType == GradientDrawable.RADIAL_GRADIENT) {
            throw new XmlPullParserException(
                    "<gradient> tag requires 'gradientRadius' "
                            + "attribute with radial type");
        }
        a.recycle();
    }

    void setStGradientAngle(Drawable.ConstantState constantState, int angle) {
        try {
            if (sStGradientAngle == null) {
                sStGradientAngle = Class.forName("android.graphics.drawable.GradientDrawable$GradientState").getDeclaredField("mAngle");
                sStGradientAngle.setAccessible(true);
            }
            sStGradientAngle.set(constantState, angle);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    void setStGradientPositions(Drawable.ConstantState constantState, float... positions) {
        try {
            if (sStGradientPositions == null) {
                sStGradientPositions = Class.forName("android.graphics.drawable.GradientDrawable$GradientState").getDeclaredField("mPositions");
                sStGradientPositions.setAccessible(true);
            }
            sStGradientPositions.set(constantState, positions);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    float getAttrFloatOrFraction(Context context, AttributeSet attrs, int attr, float defaultValue, float base, float pbase) {
        TypedArray a = obtainAttributes(context.getResources(), context.getTheme(), attrs, new int[]{attr});
        TypedValue tv = a.peekValue(0);
        float v = defaultValue;
        if (tv != null) {
            boolean isFraction = tv.type == TypedValue.TYPE_FRACTION;
            v = isFraction ? tv.getFraction(base, pbase) : tv.getFloat();
        }
        a.recycle();
        return v;
    }

    int getAlphaColor(int baseColor, float alpha) {
        return alpha != 1.0f
                ? ColorUtils.setAlphaComponent(baseColor, Math.round(Color.alpha(baseColor) * alpha))
                : baseColor;
    }
}
