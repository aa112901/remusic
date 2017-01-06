package com.wm.remusic.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.wm.remusic.R;


public class SideBar extends View {
    // 触摸事件
    private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
    // 26个字母
    public static String[] b = {"A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#"};
    private int choose = -1;// 选中
    private Paint paint = new Paint();


    private TextView mTextDialog;


    public void setView(TextView mTextDialog) {
        this.mTextDialog = mTextDialog;
    }


    public SideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundResource(R.drawable.search_indexbar_bg_prs);
    }


    public SideBar(Context context) {
        super(context);

    }


    /**
     * 重写这个方法
     */
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.removeCallbacks(runnable);
// 获取焦点改变背景颜色.
        int height = getHeight();// 获取对应高度
        int width = getWidth(); // 获取对应宽度
        int singleHeight = height / b.length;// 获取每一个字母的高度


        for (int i = 0; i < b.length; i++) {
            //paint.setColor(Color.argb(66, 0, 0, 0));
            paint.setColor(Color.WHITE);
            paint.setTypeface(Typeface.SANS_SERIF);
            paint.setAntiAlias(true);
            paint.setTextSize(30);
// 选中的状态
            if (i == choose) {
                paint.setColor(Color.WHITE);
                paint.setFakeBoldText(true);
            }
// x坐标等于中间-字符串宽度的一半.
            float xPos = width / 2 - paint.measureText(b[i]) / 2;
            float yPos = singleHeight * i + singleHeight;
            canvas.drawText(b[i], xPos, yPos, paint);
            paint.reset();// 重置画笔
        }
        this.postDelayed(runnable, 2000);

    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            SideBar.this.setVisibility(INVISIBLE);
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();// 点击y坐标
        final int oldChoose = choose;
        final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
        final int c = (int) (y / getHeight() * b.length);// 点击y坐标所占总高度的比例*b数组的长度就等于点击b中的个数.


        switch (action) {
            case MotionEvent.ACTION_UP:

                choose = -1;//
                invalidate();
                if (mTextDialog != null) {
                    mTextDialog.setVisibility(View.INVISIBLE);
                }
                //  this.postDelayed(runnable,2000);
                break;
            case MotionEvent.ACTION_DOWN:
                this.removeCallbacks(runnable);
                SideBar.this.setVisibility(VISIBLE);
                break;

            default:
//setBackgroundResource(R.drawable.sort_listview_sidebar_background);
                if (oldChoose != c) {
                    if (c >= 0 && c < b.length) {
                        if (listener != null) {
                            listener.onTouchingLetterChanged(b[c]);
                        }
                        if (mTextDialog != null) {
                            mTextDialog.setText(b[c]);
                            mTextDialog.setVisibility(View.VISIBLE);//这里是指的在屏幕中央显示当前点击的一个A，B，C，D...的一个状态显示。
                        }


                        choose = c;
                        invalidate();
                    }
                }


                break;
        }
        return true;
    }


    /**
     * 向外公开的方法
     *
     * @param onTouchingLetterChangedListener
     */
    public void setOnTouchingLetterChangedListener(
            OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }


    /**
     * 接口
     *
     * @author coder
     */
    public interface OnTouchingLetterChangedListener {
        void onTouchingLetterChanged(String s);
    }


    public void setSelected(String nowChar) {
        Log.i("OnRecyclerViewOnScrol", "setSelected:" + nowChar);
        if (nowChar != null) {
            for (int i = 0; i < b.length; i++) {
                if (b[i].equals(nowChar)) {
                    choose = i;
                    break;
                }
                if (i == b.length - 1) {
                    choose = -1;
                }
            }
            invalidate();//刷新整个view
        }
    }
}
