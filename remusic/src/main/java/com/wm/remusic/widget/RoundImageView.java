package com.wm.remusic.widget;


import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.wm.remusic.R;


/**
 * 圆形ImageView，可以旋转和平移
 *
 * @author Alan
 */
public class RoundImageView extends ImageView {


    private static final int DEFAULT_DURATION = 60 * 370;
    private final RectF mDrawableRect = new RectF();
    private final Matrix mShaderMatrix = new Matrix();
    private final Paint mBitmapPaint = new Paint();
    public ObjectAnimator mObjectAnimator, mObjectAnimator1, mObjectAnimator3, defaultAinmator;
    private Bitmap mBitmap;
    private Bitmap srcBitmap;
    private BitmapShader mBitmapShader;
    private int mBitmapWidth;
    private int mBitmapHeight;
    private float mDrawableRadius;
    private float v = 0;

    // 控件默认长宽
    private int defaultWidth = 0;
    private int defaultHeight = 0;


    public RoundImageView(Context context) {
        super(context);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    //开始旋转
    public void startRotation() {
        if (mObjectAnimator != null) {
            if (!mObjectAnimator.isRunning()) {
                mObjectAnimator.start();
            }
        } else {
            initRotateAnimation(0f);
            mObjectAnimator.start();
        }
    }

    //停止旋转
    public void stopRotation() {
        if (mObjectAnimator != null) {
            if (mObjectAnimator.isRunning()) {
                mObjectAnimator.cancel();
            }
            float valueAvatar = (float) mObjectAnimator.getAnimatedValue();
            v = valueAvatar;
            mObjectAnimator.setFloatValues(valueAvatar, 360f + valueAvatar);
        } else {
            initRotateAnimation(0f);
        }
    }

    //平移动画的准备
    public void translationToLeft() {
        if (mObjectAnimator != null) {
            mObjectAnimator.cancel();
            mObjectAnimator = null;
        }
        initTranlationAnimate(0, (int) (-getWidth() * 1.2));
        initTranlationAnimate1(getWidth(), 0);
    }

    //平移动画的准备
    public void translationToRight() {
        mObjectAnimator.cancel();
        mObjectAnimator = null;
        initRotateAnimation(0f);
        initTranlationAnimate(0, (int) (getWidth() * 1.2));
        initTranlationAnimate1(-getWidth(), 0);
    }

    public boolean getAnimateState() {
        return mObjectAnimator.isRunning();
    }

    public void initTranlationAnimate(float start, float stop) {
        mObjectAnimator1 = ObjectAnimator.ofFloat(this, "translationX", start, stop);
        //mObjectAnimator1.setDuration(390);
        //mObjectAnimator1.setInterpolator(new LinearInterpolator());
        //mObjectAnimator1.setRepeatCount(0);
    }

    public void initTranlationAnimate1(float start, float stop) {
        mObjectAnimator3 = ObjectAnimator.ofFloat(this, "translationX", start, stop);
        //mObjectAnimator3.setDuration(200);
        //mObjectAnimator3.setInterpolator(new LinearInterpolator());
        // mObjectAnimator3.setRepeatCount(0);
    }

    public void initRotateAnimation(float start) {
        mObjectAnimator = ObjectAnimator.ofFloat(this, "rotation", start, 360f + start);
        mObjectAnimator.setDuration(DEFAULT_DURATION);
        mObjectAnimator.setInterpolator(new LinearInterpolator());
        mObjectAnimator.setRepeatCount(ObjectAnimator.INFINITE);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initRotateAnimation(0f);
        initTranlationAnimate(0, getWidth());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        // this.measure(0, 0);
        if (drawable.getClass() == NinePatchDrawable.class)
            return;

        if (defaultWidth == 0) {
            defaultWidth = getWidth();

        }
        if (defaultHeight == 0) {
            defaultHeight = getHeight();
        }

        canvas.drawCircle(defaultWidth / 2, defaultHeight / 2, mDrawableRadius, mBitmapPaint);
    }


    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        mBitmap = ((BitmapDrawable) getDrawable()).getBitmap();
        make(mBitmap);

    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        super.setImageResource(resId);
        mBitmap = ((BitmapDrawable) getDrawable()).getBitmap();
        make(mBitmap);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        mBitmap = ((BitmapDrawable) getDrawable()).getBitmap();
        make(mBitmap);

    }


    public Bitmap getCroppedRoundBitmap(Bitmap bmp, int radius, int borde) {
        //处理传入的bitmap得到外围有黑色边的圆形bitmap

        Bitmap scaledSrcBmp;
        int diameter = radius * 2;

        // 为了防止宽高不相等，造成圆形图片变形，因此截取长方形中处于中间位置最大的正方形图�?
        int bmpWidth = bmp.getWidth();
        int bmpHeight = bmp.getHeight();
        int squareWidth = 0, squareHeight = 0;
        int x = 0, y = 0;
        Bitmap squareBitmap;
        if (bmpHeight > bmpWidth) {// 高大于宽
            squareWidth = squareHeight = bmpWidth;
            x = 0;
            y = (bmpHeight - bmpWidth) / 2;
            // 截取正方形图�?
            squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth, squareHeight);
        } else if (bmpHeight < bmpWidth) {// 宽大于高
            squareWidth = squareHeight = bmpHeight;
            x = (bmpWidth - bmpHeight) / 2;
            y = 0;
            squareBitmap = Bitmap.createBitmap(bmp, x, y, squareWidth, squareHeight);
        } else {
            squareBitmap = bmp;
        }

        if (squareBitmap.getWidth() != diameter || squareBitmap.getHeight() != diameter) {
            scaledSrcBmp = Bitmap.createScaledBitmap(squareBitmap, diameter, diameter, true);

        } else {
            scaledSrcBmp = squareBitmap;
        }

        int width = scaledSrcBmp.getWidth();
        int height = scaledSrcBmp.getHeight();

        //建立返回的bitmap画布 设置长宽和属性
        Bitmap output = Bitmap.createBitmap(width + 30, height + 30, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Paint bordePaint = new Paint();

        //根据图片长宽建立rect
        Rect rect = new Rect(0, 0, width, height);


        //圆形的画笔
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);

        //边界的画笔
        bordePaint.setStyle(Paint.Style.STROKE);
        bordePaint.setAntiAlias(true);
        bordePaint.setColor(Color.BLACK);
        bordePaint.setStrokeWidth(borde);

        //画外围圆形
        canvas.drawCircle(15 + (width / 2), 15 + (height / 2), Math.min(rect.height() / 2, rect.width() / 2), paint);
        //设置图片合成用的画笔
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        //画上传入的bitmap
        canvas.drawBitmap(scaledSrcBmp, 15 + (width / 2) - rect.width() / 2, 15 + (height / 2) - rect.height() / 2, paint);
        //画上外边框
        canvas.drawCircle((15 + width / 2), 15 + (height / 2), (width / 2) + 6, bordePaint);

        //给圆形图片一个外围的图片
        srcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.play_disc);

        Bitmap bitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), srcBitmap.getConfig());
        Paint p = new Paint();
        canvas = new Canvas(bitmap);
        canvas.drawARGB(0, 0, 0, 0);

        p.setAntiAlias(true);
        p.setFilterBitmap(true);
        p.setDither(true);

        //首先绘制第一张图片，很简单，就是和方法中getDstImage一样
        canvas.drawBitmap(srcBitmap, 0, 0, p);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(output, (bitmap.getWidth() - output.getWidth()) / 2, (bitmap.getHeight() - output.getHeight()) / 2, null);
        return bitmap;
    }


    private void updateShaderMatrix() {
        float scale;
        float dx = 0;
        float dy = 0;

        mShaderMatrix.set(null);

        if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width() * mBitmapHeight) {
            scale = mDrawableRect.height() / (float) mBitmapHeight;
            dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f;
        } else {
            scale = mDrawableRect.width() / (float) mBitmapWidth;
            dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f;
        }

        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate((int) (dx + 0.5f) + mDrawableRect.left, (int) (dy + 0.5f) + mDrawableRect.top);

        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }

    private void make(Bitmap bitmap) {

        mBitmap = getCroppedRoundBitmap(bitmap, 255, 15);

        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mBitmapPaint.setAntiAlias(true);

        mBitmapPaint.setShader(mBitmapShader);
        mDrawableRect.set(0, 0, getWidth(), getHeight());
        mDrawableRadius = Math.min(mDrawableRect.height() / 2, mDrawableRect.width() / 2);

        mBitmapHeight = mBitmap.getHeight();
        mBitmapWidth = mBitmap.getWidth();

        updateShaderMatrix();
        invalidate();
    }


}
