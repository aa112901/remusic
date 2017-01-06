package com.wm.remusic.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class CustomTextView extends TextView {
    
    private final static String TAG = CustomTextView.class.getSimpleName();
    private Paint paint1;
    private Paint paint2;
    
    private int mWidth;
    private LinearGradient gradient;
    private Matrix matrix;
    //渐变的速度
    private int deltaX;

    public CustomTextView(Context context) {
        super(context, null);
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        paint1 = new Paint();
        paint1.setColor(getResources().getColor(android.R.color.holo_blue_dark));
        paint1.setStyle(Paint.Style.FILL);

    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(mWidth == 0){
            Log.e(TAG,"*********************");
            mWidth = getMeasuredWidth();
            paint2 = getPaint();
            //颜色渐变器
            gradient = new LinearGradient(0, 0, mWidth, 0, new int[]{Color.GRAY,Color.WHITE,Color.GRAY}, new float[]{
                    0.3f,0.5f,1.0f
            }, Shader.TileMode.CLAMP);
            paint2.setShader(gradient);
            
            matrix = new Matrix();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(matrix !=null){
            deltaX += mWidth / 5;
            if(deltaX > 2 * mWidth){
                deltaX = -mWidth;
            }
        }
        //关键代码通过矩阵的平移实现
        matrix.setTranslate(deltaX, 0);
        gradient.setLocalMatrix(matrix);
        postInvalidateDelayed(100);
    }
}