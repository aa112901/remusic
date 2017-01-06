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

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StyleRes;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.StateSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.WrapperListAdapter;

import com.bilibili.magicasakura.widgets.Tintable;
import com.wm.remusic.R;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;

/**
 * Created by xyczero on 15/9/6.
 * Email : xyczero@sina.com
 */
public class ThemeUtils {
    private static final ThreadLocal<TypedValue> TL_TYPED_VALUE = new ThreadLocal<>();

    public static final int[] DISABLED_STATE_SET = new int[]{-android.R.attr.state_enabled};
    public static final int[] ENABLED_STATE_SET = new int[]{android.R.attr.state_enabled};
    public static final int[] FOCUSED_STATE_SET = new int[]{android.R.attr.state_focused};
    public static final int[] ACTIVATED_STATE_SET = new int[]{android.R.attr.state_activated};
    public static final int[] PRESSED_STATE_SET = new int[]{android.R.attr.state_pressed};
    public static final int[] CHECKED_STATE_SET = new int[]{android.R.attr.state_checked};
    public static final int[] SELECTED_STATE_SET = new int[]{android.R.attr.state_selected};
    public static final int[] EMPTY_STATE_SET = new int[0];

    private static final int[] TEMP_ARRAY = new int[1];


    public static Drawable tintDrawable(Drawable drawable, @ColorInt int color, PorterDuff.Mode mode) {
        if (drawable == null) return null;
        Drawable wrapper = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTint(wrapper, color);
        DrawableCompat.setTintMode(drawable, mode);
        return wrapper;
    }

    public static Drawable tintDrawable(Context context, @DrawableRes int resId, @ColorRes int colorId) {
        if (resId <= 0 || colorId <= 0) return null;
        Drawable drawable = context.getResources().getDrawable(resId);
        return tintDrawableByColorId(context, drawable, colorId);
    }

    public static Drawable tintDrawable(Drawable drawable, @ColorInt int color) {
        return tintDrawable(drawable, color, PorterDuff.Mode.SRC_IN);
    }

    public static Drawable tintDrawableByDrawableId(Context context, @DrawableRes int resId, @ColorInt int color) {
        if (resId <= 0) return null;
        Drawable drawable = context.getResources().getDrawable(resId);
        return tintDrawable(drawable, color);
    }

    public static Drawable tintDrawableByColorId(Context context, Drawable drawable, @ColorRes int colorId) {
        if (drawable == null) return null;
        if (colorId <= 0) return drawable;
        return tintDrawable(drawable, replaceColor(context, context.getResources().getColor(colorId)));
    }

    public static Drawable tintDrawable(Drawable drawable, ColorStateList cls, PorterDuff.Mode mode) {
        if (drawable == null) return null;
        Drawable wrapper = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTintList(wrapper, cls);
        DrawableCompat.setTintMode(drawable, mode);
        return wrapper;
    }

    public static Drawable tintDrawableByColorList(Context context, Drawable drawable, @ColorRes int colorListId, PorterDuff.Mode mode) {
        if (drawable == null) return null;
        if (colorListId <= 0) return drawable;
        return tintDrawable(drawable, TintManager.get(context).getColorStateList(colorListId), mode == null ? PorterDuff.Mode.SRC_IN : mode);
    }

    public static
    @ColorInt
    int getColorById(Context context, @ColorRes int colorId) {
        return replaceColorById(context, colorId);
    }

    public static
    @ColorInt
    int getColor(Context context, @ColorInt int color) {
        return replaceColor(context, color);
    }

    public static
    @ColorInt
    int getThemeAttrColor(Context context, @AttrRes int attr) {
        return hasThemeAttr(context, attr) ? replaceColorById(context, getThemeAttrId(context, attr)) : Color.TRANSPARENT;
    }

    static int getThemeAttrColor(Context context, @AttrRes int attr, float alpha) {
        final int color = getThemeAttrColor(context, attr);
        final int originalAlpha = Color.alpha(color);

        return ColorUtils.setAlphaComponent(color, Math.round(originalAlpha * alpha));
    }

    public static ColorStateList getThemeAttrColorStateList(Context context, @AttrRes int attr) {
        TEMP_ARRAY[0] = attr;
        TypedArray a = context.obtainStyledAttributes(null, TEMP_ARRAY);
        try {
            return a.getColorStateList(0);
        } finally {
            a.recycle();
        }
    }

    public static int getDisabledThemeAttrColor(Context context, @AttrRes int attr) {
        final ColorStateList csl = getThemeAttrColorStateList(context, attr);
        if (csl != null && csl.isStateful()) {
            // If the CSL is stateful, we'll assume it has a disabled state and use it
            return csl.getColorForState(DISABLED_STATE_SET, csl.getDefaultColor());
        } else {
            // Else, we'll generate the color using disabledAlpha from the theme

            final TypedValue tv = getTypedValue();
            // Now retrieve the disabledAlpha value from the theme
            context.getTheme().resolveAttribute(android.R.attr.disabledAlpha, tv, true);
            final float disabledAlpha = tv.getFloat();

            return getThemeAttrColor(context, attr, disabledAlpha);
        }
    }

    public static int getThemeAttrId(Context context, @AttrRes int attr) {
        TEMP_ARRAY[0] = attr;
        TypedArray a = context.obtainStyledAttributes(null, TEMP_ARRAY);
        try {
            return a.getResourceId(0, 0);
        } finally {
            a.recycle();
        }
    }

    public static int getThemeAttrId(Context context, AttributeSet attrs, @AttrRes int attr) {
        TEMP_ARRAY[0] = attr;
        TypedArray a = context.obtainStyledAttributes(attrs, TEMP_ARRAY);
        try {
            return a.getResourceId(0, 0);
        } finally {
            a.recycle();
        }
    }

    public static boolean getThemeAttrBoolean(Context context, @AttrRes int attr) {
        TEMP_ARRAY[0] = attr;
        TypedArray a = context.obtainStyledAttributes(null, TEMP_ARRAY);
        try {
            return a.getBoolean(0, false);
        } finally {
            a.recycle();
        }
    }

    public static boolean hasThemeAttr(Context context, @AttrRes int attr) {
        TEMP_ARRAY[0] = attr;
        TypedArray a = context.obtainStyledAttributes(null, TEMP_ARRAY);
        try {
            return a.hasValue(0);
        } finally {
            a.recycle();
        }
    }

    public static int getThemeAttrDimensionPixelSize(Context context, @AttrRes int attr) {
        TEMP_ARRAY[0] = attr;
        TypedArray a = context.obtainStyledAttributes(null, TEMP_ARRAY);
        try {
            return a.getDimensionPixelSize(0, 0);
        } finally {
            a.recycle();
        }
    }

    private static TypedValue getTypedValue() {
        TypedValue typedValue = TL_TYPED_VALUE.get();
        if (typedValue == null) {
            typedValue = new TypedValue();
            TL_TYPED_VALUE.set(typedValue);
        }
        return typedValue;
    }

    // skip animated-selector when android version is 5.0.x
    private static boolean isSkipAnimatedSelector = false;
    private static boolean hasRecordedVersion = false;

    public static boolean isSkipAnimatedSelector() {
        if (!hasRecordedVersion) {
            final String sdkVersion = Build.VERSION.RELEASE;
            isSkipAnimatedSelector = !Build.UNKNOWN.equals(sdkVersion) && "5.0".compareTo(sdkVersion) <= 0 && "5.1".compareTo(sdkVersion) > 0;
            hasRecordedVersion = true;
        }
        return isSkipAnimatedSelector;
    }

    public static boolean containsNinePatch(Drawable drawable) {
        drawable = getWrapperDrawable(drawable);
        if (drawable instanceof NinePatchDrawable
                || drawable instanceof InsetDrawable
                || drawable instanceof LayerDrawable) {
            return true;
        } else if (drawable instanceof StateListDrawable) {
            final DrawableContainer.DrawableContainerState containerState = ((DrawableContainer.DrawableContainerState) drawable.getConstantState());
            //can't get containState from drawable which is containing DrawableWrapperDonut
            //https://code.google.com/p/android/issues/detail?id=169920
            if (containerState == null) {
                return true;
            }
            for (Drawable dr : containerState.getChildren()) {
                dr = getWrapperDrawable(dr);
                if (dr instanceof NinePatchDrawable
                        || dr instanceof InsetDrawable
                        || dr instanceof LayerDrawable) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Drawable getWrapperDrawable(Drawable drawable) {
        if (drawable instanceof android.support.v4.graphics.drawable.DrawableWrapper) {
            return ((android.support.v4.graphics.drawable.DrawableWrapper) drawable).getWrappedDrawable();
        } else if (drawable instanceof android.support.v7.graphics.drawable.DrawableWrapper) {
            return ((android.support.v7.graphics.drawable.DrawableWrapper) drawable).getWrappedDrawable();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && drawable instanceof android.graphics.drawable.DrawableWrapper) {
            return ((android.graphics.drawable.DrawableWrapper) drawable).getDrawable();
        }
        return drawable;
    }

    public static Activity getWrapperActivity(Context context) {
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return getWrapperActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }

    public static ContextWrapper getWrapperContext(Context context, @StyleRes int themeId) {
        if (context == null) return null;

        return new ContextThemeWrapper(context, themeId);
    }

    public static Resources updateNightMode(Resources resource, boolean on) {
        DisplayMetrics dm = resource.getDisplayMetrics();
        Configuration config = resource.getConfiguration();
        final int uiModeNightMaskOrigin = config.uiMode &= ~Configuration.UI_MODE_TYPE_MASK;
        final int uiModeNightMaskNew = on ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO;
        if (uiModeNightMaskOrigin != uiModeNightMaskNew) {
            config.uiMode &= ~Configuration.UI_MODE_NIGHT_MASK;
            config.uiMode |= uiModeNightMaskNew;
            resource.updateConfiguration(config, dm);
        }
        return resource;
    }

    static TintInfo parseColorStateList(ColorStateList origin) {
        if (origin == null) return null;

        boolean hasDisable = false;
        int originDefaultColor = origin.getDefaultColor();
        LinkedList<int[]> stateList = new LinkedList<>();
        LinkedList<Integer> colorList = new LinkedList<>();

        int disableColor = origin.getColorForState(DISABLED_STATE_SET, 0);
        if (disableColor != originDefaultColor) {
            hasDisable = true;
            stateList.add(DISABLED_STATE_SET);
            colorList.add(disableColor);
        }

        int pressedColor = origin.getColorForState(wrapState(hasDisable, PRESSED_STATE_SET), 0);
        if (pressedColor != originDefaultColor) {
            stateList.add(PRESSED_STATE_SET);
            colorList.add(pressedColor);
        }

        int focusColor = origin.getColorForState(wrapState(hasDisable, FOCUSED_STATE_SET), 0);
        if (focusColor != originDefaultColor) {
            stateList.add(FOCUSED_STATE_SET);
            colorList.add(focusColor);
        }

        int checkedColor = origin.getColorForState(wrapState(hasDisable, CHECKED_STATE_SET), 0);
        if (checkedColor != originDefaultColor) {
            stateList.add(CHECKED_STATE_SET);
            colorList.add(checkedColor);
        }

        int selectedColor = origin.getColorForState(wrapState(hasDisable, SELECTED_STATE_SET), 0);
        if (selectedColor != originDefaultColor) {
            stateList.add(SELECTED_STATE_SET);
            colorList.add(selectedColor);
        }

        int normalColor = origin.getColorForState(wrapState(hasDisable, EMPTY_STATE_SET), 0);
        if (normalColor != 0) {
            stateList.add(EMPTY_STATE_SET);
            colorList.add(normalColor);
        }

        if (colorList.size() > 1) {
            return new TintInfo(stateList, colorList);
        } else {
            return null;
        }
    }

    private static int[] wrapState(boolean hasDisable, int[] targetState) {
        return targetState.length > 0
                ? (hasDisable ? new int[]{ENABLED_STATE_SET[0], targetState[0]} : targetState)
                : (hasDisable ? ENABLED_STATE_SET : targetState);
    }

    public static ColorStateList getThemeColorStateList(Context context, ColorStateList origin) {
        if (origin == null) return null;

        if (origin.isStateful()) {
            TintInfo tintInfo = parseColorStateList(origin);
            if (tintInfo == null || tintInfo.isInvalid()) {
                return origin;
            }

            int[] newColors;
            int[][] newStates;
            int index = 0;
            boolean hasDisableColor = StateSet.stateSetMatches(tintInfo.mTintStates[0], DISABLED_STATE_SET);
            if (!hasDisableColor) {
                newStates = new int[tintInfo.mTintStates.length + 1][];
                newColors = new int[tintInfo.mTintStates.length + 1];
                newStates[index] = DISABLED_STATE_SET;
                newColors[index] = getDisabledThemeAttrColor(context, R.attr.themeColorSecondary);
                index++;
            } else {
                newStates = new int[tintInfo.mTintStates.length][];
                newColors = new int[tintInfo.mTintStates.length];
            }

            for (int i = 0; i < tintInfo.mTintStates.length; i++) {
                newStates[index] = tintInfo.mTintStates[i];
                newColors[index] = replaceColor(context, tintInfo.mTintColors[i]);
                index++;
            }
            return new ColorStateList(newStates, newColors);
        }
        return ColorStateList.valueOf(replaceColor(context, origin.getDefaultColor()));
    }

    public static ColorStateList getThemeColorStateList(Context context, @ColorRes int colorId) {
        return ColorStateListUtils.createColorStateList(context, colorId);
    }

    public static void refreshUI(Context context) {
        refreshUI(context, null);
    }

    public static void refreshUI(Context context, ExtraRefreshable extraRefreshable) {
        TintManager.clearTintCache();
        Activity activity = getWrapperActivity(context);
        if (activity != null) {
            if (extraRefreshable != null) {
                extraRefreshable.refreshGlobal(activity);
            }
            View rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
            refreshView(rootView, extraRefreshable);
        }
    }

    private static Field mRecycler;
    private static Method mClearMethod;

    private static void refreshView(View view, ExtraRefreshable extraRefreshable) {
        if (view == null) return;

        view.destroyDrawingCache();
        if (view instanceof Tintable) {
            ((Tintable) view).tint();
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    refreshView(((ViewGroup) view).getChildAt(i), extraRefreshable);
                }
            }
        } else {
            if (extraRefreshable != null) {
                extraRefreshable.refreshSpecificView(view);
            }
            if (view instanceof AbsListView) {
                ListAdapter adapter = ((AbsListView) view).getAdapter();
                while (adapter instanceof WrapperListAdapter) {
                    adapter = ((WrapperListAdapter) adapter).getWrappedAdapter();
                }
                if (adapter instanceof BaseAdapter) {
                    ((BaseAdapter) adapter).notifyDataSetChanged();
                }
            }
            if (view instanceof RecyclerView) {
                try {
                    if (mRecycler == null) {
                        mRecycler = RecyclerView.class.getDeclaredField("mRecycler");
                        mRecycler.setAccessible(true);
                    }
                    if (mClearMethod == null) {
                        mClearMethod = Class.forName("android.support.v7.widget.RecyclerView$Recycler")
                                .getDeclaredMethod("clear");
                        mClearMethod.setAccessible(true);
                    }
                    mClearMethod.invoke(mRecycler.get(view));
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                ((RecyclerView) view).getRecycledViewPool().clear();
                ((RecyclerView) view).invalidateItemDecorations();
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    refreshView(((ViewGroup) view).getChildAt(i), extraRefreshable);
                }
            }
        }
    }

    public interface ExtraRefreshable {
        void refreshGlobal(Activity activity);

        void refreshSpecificView(View view);
    }

    public static switchColor mSwitchColor;

    public static void setSwitchColor(switchColor switchColor) {
        mSwitchColor = switchColor;
    }

    static
    @ColorInt
    int replaceColorById(Context context, @ColorRes int colorId) {
        return mSwitchColor == null ? Color.TRANSPARENT : mSwitchColor.replaceColorById(context, colorId);
    }

    static
    @ColorInt
    int replaceColor(Context context, @ColorInt int color) {
        return mSwitchColor == null ? Color.TRANSPARENT : mSwitchColor.replaceColor(context, color);
    }

    public interface switchColor {
        @ColorInt
        int replaceColorById(Context context, @ColorRes int colorId);

        @ColorInt
        int replaceColor(Context context, @ColorInt int color);
    }
}
