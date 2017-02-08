package com.wm.remusic.widget;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.wm.remusic.R;
import com.wm.remusic.json.FocusItemInfo;
import com.wm.remusic.net.BMA;
import com.wm.remusic.net.HttpUtil;
import com.wm.remusic.net.NetworkUtils;
import com.wm.remusic.service.MusicPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by muyang on 2016/3/24.
 */
public class LoodView extends FrameLayout {
    //轮播图图片数量
    private final static int IMAGE_COUNT = 7;
    //自动轮播时间间隔
    private final static int TIME_INTERVAL = 3;
    //自动轮播启用开关
    private final static boolean isAutoPlay = true;
    //ImageView资源ID
    private int[] imageResIds;
    private FPagerAdapter fPagerAdapter;
    private ArrayList<String> imageNet = new ArrayList<>();
    private List<ImageView> imageViewList;
    private List<View> dotViewList;
    private ViewPager viewPager;
    private boolean isFromCache = true;
    private Context mContext;
    //当前轮播页面
    private int currentItem = 0;
    //定时任务
    private ScheduledExecutorService scheduledExecutorService;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            viewPager.setCurrentItem(currentItem);
        }
    };


    public LoodView(Context context) {
        super(context);
        mContext = context;
    }

    public LoodView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
        mContext = context;
    }

    public LoodView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        mContext = context;
        initImageView();
        initUI(context);
        if (isAutoPlay) {
            startPlay();
        }

    }

    public void onDestroy(){
        scheduledExecutorService.shutdownNow();
        scheduledExecutorService = null;
    }

    /**
     * 开始轮播图切换
     */

    public void startPlay() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new LoopTask(), 1, TIME_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * 停止切换
     */
    private void stopPlay() {
        scheduledExecutorService.shutdown();
    }

    /**
     * 初始化UI
     *
     * @param context
     */
    private void initUI(Context context) {
        LayoutInflater.from(context).inflate(R.layout.load_view, this, true);
        for (String imagesID : imageNet) {
            final SimpleDraweeView mAlbumArt = new SimpleDraweeView(context);

            ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable anim) {
                    if (imageInfo == null) {
                        return;
                    }
                    QualityInfo qualityInfo = imageInfo.getQualityInfo();
                    FLog.d("Final image received! " +
                                    "Size %d x %d",
                            "Quality level %d, good enough: %s, full quality: %s",
                            imageInfo.getWidth(),
                            imageInfo.getHeight(),
                            qualityInfo.getQuality(),
                            qualityInfo.isOfGoodEnoughQuality(),
                            qualityInfo.isOfFullQuality());
                }

                @Override
                public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
                    //FLog.d("Intermediate image received");
                }

                @Override
                public void onFailure(String id, Throwable throwable) {
                    mAlbumArt.setImageURI(Uri.parse("res:/" + R.drawable.placeholder_disk_210));
                }
            };
            Uri uri = null;
            try{
                uri = Uri.parse(imagesID);
            }catch (Exception e){
                e.printStackTrace();
            }
            if (uri != null) {
                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri).build();

                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setOldController(mAlbumArt.getController())
                        .setImageRequest(request)
                        .setControllerListener(controllerListener)
                        .build();

                mAlbumArt.setController(controller);
            } else {
                mAlbumArt.setImageURI(Uri.parse("res:/" + R.drawable.placeholder_disk_210));
            }


            //view.setImageURI(Uri.parse(imagesID));

            // view.setImageResource(imagesID);
            // view.setImageResource(imagesID);
            mAlbumArt.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageViewList.add(mAlbumArt);
        }
        dotViewList.add(findViewById(R.id.v_dot1));
        dotViewList.add(findViewById(R.id.v_dot2));
        dotViewList.add(findViewById(R.id.v_dot3));
        dotViewList.add(findViewById(R.id.v_dot4));
        dotViewList.add(findViewById(R.id.v_dot5));
        dotViewList.add(findViewById(R.id.v_dot6));
        dotViewList.add(findViewById(R.id.v_dot7));

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setFocusable(true);
        fPagerAdapter = new FPagerAdapter();
        viewPager.setAdapter(fPagerAdapter);
        viewPager.addOnPageChangeListener(new MyPageChangeListener());
    }

    private void initImageView() {
        imageResIds = new int[]{
                R.mipmap.first,
                R.mipmap.second,
                R.mipmap.third,
                R.mipmap.fourth,
                R.mipmap.five,
                R.mipmap.six,
                R.mipmap.seven
        };

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                if (NetworkUtils.isConnectInternet(mContext)) {
                    isFromCache = false;
                }

                try {
                    JsonArray rray = HttpUtil.getResposeJsonObject(BMA.focusPic(7), mContext, isFromCache).get("pic").getAsJsonArray();
                    int en = rray.size();
                    Gson gson = new Gson();

                    imageNet.clear();
                    for (int i = 0; i < en; i++) {
                        FocusItemInfo focusItemInfo = gson.fromJson(rray.get(i), FocusItemInfo.class);
                        if (focusItemInfo != null) {
                            imageNet.add(focusItemInfo.getRandpic());
                        } else {
                            imageNet.add("");
                        }
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }


                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                for (int i = 0; i < 7; i++) {
                    imageViewList.get(i).setImageURI(Uri.parse(imageNet.get(i)));
                }
            }
        }.execute();


        for (int i = 0; i < 7; i++) {
            imageNet.add("");
        }

        imageViewList = new ArrayList<>();
        dotViewList = new ArrayList<>();
    }

    private class FPagerAdapter extends PagerAdapter {


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(imageViewList.get(position));
            return imageViewList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(imageViewList.get(position));
        }

        @Override
        public int getCount() {
            return imageViewList.size();
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
            super.restoreState(state, loader);
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(ViewGroup container) {
            super.startUpdate(container);
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            super.finishUpdate(container);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    private class MyPageChangeListener implements ViewPager.OnPageChangeListener {
        boolean isAutoPlay = false;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            currentItem = position;
            for (int i = 0; i < dotViewList.size(); i++) {
                if (i == position) {
                    dotViewList.get(i).setBackgroundResource(R.mipmap.red_point);
                } else {
                    dotViewList.get(i).setBackgroundResource(R.mipmap.grey_point);
                }
            }

        }

        @Override
        public void onPageScrollStateChanged(int state) {
            switch (state) {
                //手势滑动，空闲
                case 1:
                    isAutoPlay = false;
                    stopPlay();
                    startPlay();
                    break;
                //界面切换中
                case 2:
                    isAutoPlay = true;
                    break;
                //滑动完毕，继续回到第一张图
                case 0:
                    if (viewPager.getCurrentItem() == viewPager.getAdapter().getCount() - 1 && !isAutoPlay) {
                        viewPager.setCurrentItem(0);
                    }
                    //当前为第一张，从左向右滑
                    else if (viewPager.getCurrentItem() == 0 && !isAutoPlay) {
                        viewPager.setCurrentItem(viewPager.getAdapter().getCount() - 1);
                    }
                    break;
            }

        }
    }

    //解决滑动冲突
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            }
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private class LoopTask implements Runnable {
        @Override
        public void run() {
            synchronized (viewPager) {
                currentItem = (currentItem + 1) % imageViewList.size();
                handler.obtainMessage().sendToTarget();

            }

        }
    }

    /**
     * 销毁ImageView回收资源
     */
    private void destoryBitmaps() {
        for (int i = 0; i < IMAGE_COUNT; i++) {
            ImageView imageView = imageViewList.get(i);
            Drawable drawable = imageView.getDrawable();
            if (drawable != null)
                //解除drawable对view的引用
                drawable.setCallback(null);
        }
    }
}

