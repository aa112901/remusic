package com.wm.remusic.fragment;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.wm.remusic.MainApplication;
import com.wm.remusic.R;
import com.wm.remusic.activity.PlayingActivity;
import com.wm.remusic.handler.HandlerUtil;
import com.wm.remusic.service.MusicPlayer;

public class QuickControlsFragment extends BaseFragment {


    private ProgressBar mProgress;
    public Runnable mUpdateProgress = new Runnable() {

        @Override
        public void run() {

            long position = MusicPlayer.position();
            mProgress.setMax((int) MusicPlayer.duration());
            mProgress.setProgress((int) position);

            if (MusicPlayer.isPlaying()) {
                mProgress.postDelayed(mUpdateProgress, 50);
            } else mProgress.removeCallbacks(this);

        }
    };
    private ImageView mPlayPause;
    private TextView mTitle;
    private TextView mArtist;
    private SimpleDraweeView mAlbumArt;
    private View rootView;
    private ImageView playQueue, next;
    private LinearLayout layout;
    private boolean duetoplaypause = false;
    private static QuickControlsFragment fragment;

    public static QuickControlsFragment newInstance() {
        return new QuickControlsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.bottom_nav, container, false);
        this.rootView = rootView;

        mPlayPause = (ImageView) rootView.findViewById(R.id.control);
        mProgress = (ProgressBar) rootView.findViewById(R.id.song_progress_normal);
        mTitle = (TextView) rootView.findViewById(R.id.playbar_info);
        mArtist = (TextView) rootView.findViewById(R.id.playbar_singer);
        mAlbumArt = (SimpleDraweeView) rootView.findViewById(R.id.playbar_img);
        next = (ImageView) rootView.findViewById(R.id.play_next);
        playQueue = (ImageView) rootView.findViewById(R.id.play_list);

        mProgress.setMax((int) MusicPlayer.duration());
        mProgress.setProgress((int) MusicPlayer.position());


//        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mProgress.getLayoutParams();
//        mProgress.measure(0, 0);
//        layoutParams.setMargins(0, 0, 0, -(mProgress.getMeasuredHeight() / 3));
//        mProgress.setLayoutParams(layoutParams);

        mPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mPlayPause.setImageResource(MusicPlayer.isPlaying() ? R.drawable.playbar_btn_pause
                        : R.drawable.playbar_btn_play);

                if (MusicPlayer.getQueueSize() == 0) {
                    Toast.makeText(MainApplication.context, getResources().getString(R.string.queue_is_empty),
                            Toast.LENGTH_SHORT).show();
                } else {
                    HandlerUtil.getInstance(MainApplication.context).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MusicPlayer.playOrPause();
                        }
                    }, 60);
                }

            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MusicPlayer.next();
                    }
                }, 60);

            }
        });

        playQueue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        PlayQueueFragment playQueueFragment = new PlayQueueFragment();
                        playQueueFragment.show(getFragmentManager(), "playqueueframent");
                    }
                }, 60);

            }
        });

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainApplication.context, PlayingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MainApplication.context.startActivity(intent);
            }
        });


        return rootView;
    }

    public void updateNowplayingCard() {
        mTitle.setText(MusicPlayer.getTrackName());
        mArtist.setText(MusicPlayer.getArtistName());
        if (!duetoplaypause) {
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
            if (MusicPlayer.getAlbumPath() != null) {
                Log.e("albumpath", MusicPlayer.getAlbumPath());
                ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(MusicPlayer.getAlbumPath())).build();

                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setOldController(mAlbumArt.getController())
                        .setImageRequest(request)
                        .setControllerListener(controllerListener)
                        .build();

                mAlbumArt.setController(controller);
            } else {
                mAlbumArt.setImageURI(Uri.parse("content://" + MusicPlayer.getAlbumPath()));
            }
        }
        duetoplaypause = false;
        mProgress.setMax((int) MusicPlayer.duration());
        mProgress.postDelayed(mUpdateProgress, 10);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public void updateState() {
        if (MusicPlayer.isPlaying()) {
            mPlayPause.setImageResource(R.drawable.playbar_btn_pause);

        } else {
            mPlayPause.setImageResource(R.drawable.playbar_btn_play);
        }
    }


    public void updateTrackInfo() {
        updateNowplayingCard();
        updateState();
    }

    @Override
    public void updateTime() {
        mProgress.setMax((int) MusicPlayer.duration());
    }


}