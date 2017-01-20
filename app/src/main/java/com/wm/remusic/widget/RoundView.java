package com.wm.remusic.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.wm.remusic.R;

/**
 * Created by wm on 2017/1/4.
 */
public class RoundView extends FrameLayout {
    private View mView;
    private SimpleDraweeView albumView;

    public RoundView(Context context) {
        super(context);
        initView(context);
    }

    public RoundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);

    }

    public static RoundView getView(Context context ,String str){
        RoundView view = new RoundView(context);
        view.setAlbum(str);
        Log.e("uqueue",str);
        return view;
    }

    private void initView(Context context){
        this.setAnimationCacheEnabled(false);
        mView = LayoutInflater.from(context).inflate(R.layout.fragment_roundimage,null);
        albumView = (SimpleDraweeView) mView.findViewById(R.id.sdv);
        addView(mView);
        //设置图像是否为圆形
        rp.setRoundAsCircle(true);
        //设置圆角半径
        //rp.setCornersRadius(20);
        //分别设置左上角、右上角、左下角、右下角的圆角半径
        //rp.setCornersRadii(20,25,30,35);
        //分别设置（前2个）左上角、(3、4)右上角、(5、6)左下角、(7、8)右下角的圆角半径
        //rp.setCornersRadii(new float[]{20,25,30,35,40,45,50,55});
        //设置边框颜色及其宽度
        rp.setBorder(Color.BLACK, 6);
        albumView.setHierarchy(hierarchy);
    }

    public void setAlbum(String albumPath){
        if (albumPath == null) {
            albumView.setImageURI(Uri.parse("res:/" + R.drawable.placeholder_disk_play_song));
        } else {
            try {
                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(albumPath)).build();
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setOldController(albumView.getController())
                        .setImageRequest(request)
                        .setControllerListener(controllerListener)
                        .build();
                albumView.setController(controller);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.e("roundview","setalbum = " + albumPath);

    }

    //初始化圆角圆形参数对象
    RoundingParams rp = new RoundingParams();


    //获取GenericDraweeHierarchy对象
    GenericDraweeHierarchy hierarchy = GenericDraweeHierarchyBuilder.newInstance(getResources())
            //设置圆形圆角参数
            .setRoundingParams(rp)
            //设置圆角半径
            //.setRoundingParams(RoundingParams.fromCornersRadius(20))
            //分别设置左上角、右上角、左下角、右下角的圆角半径
            //.setRoundingParams(RoundingParams.fromCornersRadii(20,25,30,35))
            //分别设置（前2个）左上角、(3、4)右上角、(5、6)左下角、(7、8)右下角的圆角半径
            //.setRoundingParams(RoundingParams.fromCornersRadii(new float[]{20,25,30,35,40,45,50,55}))
            //设置圆形圆角参数；RoundingParams.asCircle()是将图像设置成圆形
            //.setRoundingParams(RoundingParams.asCircle())
            //设置淡入淡出动画持续时间(单位：毫秒ms)
            .setFadeDuration(300)
            //构建
            .build();

    ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
        @Override
        public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable anim) {

        }

        @Override
        public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
        }

        @Override
        public void onFailure(String id, Throwable throwable) {
            albumView.setImageURI(Uri.parse("res:/" + R.drawable.placeholder_disk_play_song));
        }
    };


}
