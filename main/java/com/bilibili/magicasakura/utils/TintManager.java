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
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.bilibili.magicasakura.drawables.FilterableStateListDrawable;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author xyczero617@gmail.com
 * @time 15/9/15
 */
public class TintManager {

    private static final String TAG = "TintManager";
    private static final boolean DEBUG = false;
    private static final PorterDuff.Mode DEFAULT_MODE = PorterDuff.Mode.SRC_IN;
    private static final String SKIP_DRAWABLE_TAG = "appcompat_skip_skip";

    private static final WeakHashMap<Context, TintManager> INSTANCE_CACHE = new WeakHashMap<>();
    private static final ColorFilterLruCache COLOR_FILTER_CACHE = new ColorFilterLruCache(6);

    private final Object mDrawableCacheLock = new Object();

    private WeakReference<Context> mContextRef;
    private SparseArray<ColorStateList> mCacheTintList;
    private SparseArray<WeakReference<Drawable.ConstantState>> mCacheDrawables;
    private SparseArray<String> mSkipDrawableIdTags;

    public static TintManager get(Context context) {
        if (context == null) return null;

        if (context instanceof ContextThemeWrapper) {
            context = ((ContextThemeWrapper) context).getBaseContext();
        }
        if (context instanceof android.view.ContextThemeWrapper) {
            context = ((android.view.ContextThemeWrapper) context).getBaseContext();
        }
        TintManager tm = INSTANCE_CACHE.get(context);
        if (tm == null) {
            tm = new TintManager(context);
            INSTANCE_CACHE.put(context, tm);
            printLog("[get TintManager] create new TintManager.");
        }
        return tm;
    }

    private TintManager(Context context) {
        mContextRef = new WeakReference<>(context);
    }

    public static void clearTintCache() {
        for (Map.Entry<Context, TintManager> entry : INSTANCE_CACHE.entrySet()) {
            TintManager tm = entry.getValue();
            if (tm != null)
                tm.clear();
        }
        COLOR_FILTER_CACHE.evictAll();
    }

    private void clear() {
        if (mCacheTintList != null) {
            mCacheTintList.clear();
        }
        if (mCacheDrawables != null) {
            mCacheDrawables.clear();
        }
        if (mSkipDrawableIdTags != null) {
            mSkipDrawableIdTags.clear();
        }
    }

    @Nullable
    public ColorStateList getColorStateList(@ColorRes int resId) {
        if (resId == 0) return null;

        final Context context = mContextRef.get();
        if (context == null) return null;

        ColorStateList colorStateList = mCacheTintList != null ? mCacheTintList.get(resId) : null;
        if (colorStateList == null) {
            colorStateList = ColorStateListUtils.createColorStateList(context, resId);
            if (colorStateList != null) {
                if (mCacheTintList == null) {
                    mCacheTintList = new SparseArray<>();
                }
                mCacheTintList.append(resId, colorStateList);
            }
        }
        return colorStateList;
    }

    @Nullable
    public Drawable getDrawable(@DrawableRes int resId) {
        final Context context = mContextRef.get();
        if (context == null) return null;

        if (resId == 0) return null;
        if (mSkipDrawableIdTags != null) {
            final String cachedTagName = mSkipDrawableIdTags.get(resId);
            if (SKIP_DRAWABLE_TAG.equals(cachedTagName)) {
                printLog("[Match Skip DrawableTag] Skip the drawable which is matched with the skip tag.");
                return null;
            }
        } else {
            // Create an id cache as we'll need one later
            mSkipDrawableIdTags = new SparseArray<>();
        }

        // Try the cache first (if it exists)
        Drawable drawable = getCacheDrawable(context, resId);
        if (drawable == null) {
            drawable = DrawableUtils.createDrawable(context, resId);
            if (drawable != null && !(drawable instanceof ColorDrawable)) {
                if (addCachedDrawable(resId, drawable)) {
                    printLog("[loadDrawable] Saved drawable to cache: " +
                            context.getResources().getResourceName(resId));
                }
            }
        }

        if (drawable == null) {
            mSkipDrawableIdTags.append(resId, SKIP_DRAWABLE_TAG);
        }
        return drawable;
    }

    private Drawable getCacheDrawable(@NonNull final Context context, final int key) {
        synchronized (mDrawableCacheLock) {
            if (mCacheDrawables == null) return null;

            final WeakReference<Drawable.ConstantState> weakReference = mCacheDrawables.get(key);
            if (weakReference != null) {
                Drawable.ConstantState cs = weakReference.get();
                if (cs != null) {
                    printLog("[getCacheDrawable] Get drawable from cache: " +
                            context.getResources().getResourceName(key));
                    return cs.newDrawable();
                } else {
                    mCacheDrawables.delete(key);
                }
            }
        }
        return null;
    }

    private boolean addCachedDrawable(final int key, @NonNull final Drawable drawable) {
        if (drawable instanceof FilterableStateListDrawable) {
            return false;
        }
        final Drawable.ConstantState cs = drawable.getConstantState();
        if (cs != null) {
            synchronized (mDrawableCacheLock) {
                if (mCacheDrawables == null) {
                    mCacheDrawables = new SparseArray<>();
                }
                mCacheDrawables.put(key, new WeakReference<>(cs));
            }
            return true;
        }
        return false;
    }

    private static class ColorFilterLruCache extends LruCache<Integer, PorterDuffColorFilter> {

        public ColorFilterLruCache(int maxSize) {
            super(maxSize);
        }

        PorterDuffColorFilter get(int color, PorterDuff.Mode mode) {
            return get(generateCacheKey(color, mode));
        }

        PorterDuffColorFilter put(int color, PorterDuff.Mode mode, PorterDuffColorFilter filter) {
            return put(generateCacheKey(color, mode), filter);
        }

        private static int generateCacheKey(int color, PorterDuff.Mode mode) {
            int hashCode = 1;
            hashCode = 31 * hashCode + color;
            hashCode = 31 * hashCode + mode.hashCode();
            return hashCode;
        }
    }

    public static void tintViewBackground(View view, TintInfo tint) {
        Drawable background;
        if (view == null || (background = view.getBackground()) == null) return;

        if (tint.mHasTintList || tint.mHasTintMode) {
            background.mutate();
            if (background instanceof ColorDrawable) {
                ((ColorDrawable) background).setColor(ThemeUtils.replaceColor(view.getContext(), tint.mTintList.getColorForState(view.getDrawableState(), tint.mTintList.getDefaultColor())));
            } else {
                background.setColorFilter(createTintFilter(view.getContext(),
                        tint.mHasTintList ? tint.mTintList : null,
                        tint.mHasTintMode ? tint.mTintMode : DEFAULT_MODE,
                        view.getDrawableState()));
            }
        } else {
            background.clearColorFilter();
        }

        if (Build.VERSION.SDK_INT <= 23) {
            // On Gingerbread, GradientDrawable does not invalidate itself when it's ColorFilter
            // has changed, so we need to force an invalidation
            background.invalidateSelf();
        }
    }

    public static void tintViewDrawable(View view, Drawable drawable, TintInfo tint) {
        if (view == null || drawable == null) return;
        if (tint.mHasTintList || tint.mHasTintMode) {
            drawable.mutate();
            if (drawable instanceof ColorDrawable) {
                ((ColorDrawable) drawable).setColor(ThemeUtils.replaceColor(view.getContext(), tint.mTintList.getColorForState(view.getDrawableState(), tint.mTintList.getDefaultColor())));
            } else {
                drawable.setColorFilter(createTintFilter(view.getContext(),
                        tint.mHasTintList ? tint.mTintList : null,
                        tint.mHasTintMode ? tint.mTintMode : DEFAULT_MODE,
                        view.getDrawableState()));
            }
        } else {
            drawable.clearColorFilter();
        }

        if (Build.VERSION.SDK_INT <= 23) {
            // On Gingerbread, GradientDrawable does not invalidate itself when it's ColorFilter
            // has changed, so we need to force an invalidation
            drawable.invalidateSelf();
        }
    }

    private static PorterDuffColorFilter createTintFilter(Context context, ColorStateList tint, PorterDuff.Mode tintMode, final int[] state) {
        if (tint == null || tintMode == null) {
            return null;
        }
        final int color = ThemeUtils.replaceColor(context, tint.getColorForState(state, tint.getDefaultColor()));
        return getPorterDuffColorFilter(color, tintMode);
    }

    private static PorterDuffColorFilter getPorterDuffColorFilter(int color, PorterDuff.Mode mode) {
        // First, lets see if the cache already contains the color filter
        PorterDuffColorFilter filter = COLOR_FILTER_CACHE.get(color, mode);

        if (filter == null) {
            // Cache miss, so create a color filter and add it to the cache
            filter = new PorterDuffColorFilter(color, mode);
            COLOR_FILTER_CACHE.put(color, mode, filter);
        }

        return filter;
    }

    private static void printLog(String msg) {
        if (DEBUG) {
            Log.i(TAG, msg);
        }
    }
}
