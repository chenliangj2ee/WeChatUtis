package com.qq.e.union.demo;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.qq.e.ads.nativ.MediaListener;
import com.qq.e.ads.nativ.MediaView;
import com.qq.e.ads.nativ.NativeMediaAD;
import com.qq.e.ads.nativ.NativeMediaADData;

import java.util.List;

/**
 * 贴片广告示例，为了简明地展示SDK的调用方法，示例中的布局较为简单，只简单演示了怎样让视频广告跟随屏幕进行旋转，请开发者根据自己的实际需要处理自己的布局。
 *
 *
 * 增强型接口onConfigurationChanged：
 * -  NativeMediaADData.onConfigurationChanged(Configuration newConfig)：
 *      -  newConfig参数：运行时变更配置
 *
 *
 *
 * 处理屏幕旋转这类运行时的变更，请仔细参考Google官方的建议：http://developer.android.com/intl/zh-cn/guide/topics/resources/runtime-changes.html
 *
 * @see #onConfigurationChanged 为了让MediaView可以跟随屏幕的转动保持播放效果，需要处理横屏、竖屏的LayoutParams。
 */
public class NativeVideoPreMovieActivity extends Activity {

  private static final String TAG = NativeVideoPreMovieActivity.class.getSimpleName();
  public static final int AD_COUNT = 1;                   // 一次拉取的广告条数：范围1-10。还是建议不要加载太多广告哈，贴片场景下其实一个就够了。
  private NativeMediaADData mAD;                          // 原生视频广告对象
  private NativeMediaAD mADManager;                       // 原生广告manager，用于load广告数据，监听广告行为的回调。
  private AQuery mAQuery;                                 // AQuery开源组件，在广点通SDKDemo中用于这个框架来加载图片，开发者可根据需要使用自己的图片加载框架。
  private final Handler tikTokHandler = new Handler();    // 倒计时读取Handler，本示例渲染倒计时的方法仅供参考。
  private FrameLayout mAdContainer;                       // 贴片广告的大容器
  private MediaView mMediaView;                           // 广点通SDK提供MediaView视频播放组件，丢到大容器里面播视频
  private ImageView mImageView;                           // 广告大图，广点通原生广告可以有2图、2文、1视频（可选）。
  private TextView mTextCountDown;                        // 显示倒计时的TextView
  private Button mButtonDetail;                           // 点击下载/查看详情按钮

  private VideoView mVideoView;                           // 为了简化示例，使用系统自带的VideoView播放电影，开发者应该换成自己播放器组件
  private TextView mTextView;
  private MediaController mController;
  private static final String MOVIE_URL = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"; // 需要播放的视频URL，开发者可以换成自己的视频地址
  private boolean mVideoReady = false;
  // 处理好LayoutParams
  private FrameLayout.LayoutParams landScapeParams, portraitParams;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_native_video_pre_movie);
    initViews();
    initNativeVideoAD();
    loadAD();
  }

  private void initViews() {
    mAdContainer = (FrameLayout) findViewById(R.id.ad_container);
    mVideoView = (VideoView) findViewById(R.id.video_view);
    mTextCountDown = (TextView) findViewById(R.id.text_count_down);
    mButtonDetail = (Button) findViewById(R.id.button_download);
    mMediaView = (MediaView) findViewById(R.id.gdt_media_view);
    mImageView = (ImageView) findViewById(R.id.ad_poster);
    mTextView = (TextView) findViewById(R.id.introduction);
    mController = new MediaController(this);
    mVideoView.setMediaController(mController);
    mController.setMediaPlayer(mVideoView);
    mVideoView.setVideoURI(Uri.parse(MOVIE_URL));
    mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override
      public void onPrepared(MediaPlayer mp) {
        Log.i(TAG, "Movie is onPrepared.");
        mVideoReady = true;
      }
    });
    mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
        finish();
      }
    });
    mAQuery = new AQuery(mAdContainer);
    mButtonDetail.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mAD != null) {
          mAD.onClicked(v);
        }
      }
    });
  }

  private void initNativeVideoAD() {
    NativeMediaAD.NativeMediaADListener listener = new NativeMediaAD.NativeMediaADListener() {

      @Override
      public void onADLoaded(List<NativeMediaADData> ads) {
        Toast.makeText(getApplicationContext(), "成功加载原生广告：" + ads.size() + "条", Toast.LENGTH_SHORT)
            .show();
        if (ads.size() > 0) {
          mAD = ads.get(0);
          // 广告加载成功 渲染UI
          mAQuery.id(mImageView).image(mAD.getImgUrl(), false, true, 0, 0, new BitmapAjaxCallback() {
            @Override
            protected void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {
              // AQuery框架有一个问题，就是即使在图片加载完成之前将ImageView设置为了View.GONE，在图片加载完成后，这个ImageView会被重新设置为VIEW.VISIBLE。
              // 所以在这里需要判断一下，如果已经把ImageView设置为隐藏，开始播放视频了，就不要再显示广告的大图。开发者在用其他的图片加载框架时，也应该注意检查下是否有这个问题。
              if (iv.getVisibility() == View.VISIBLE) {
                iv.setImageBitmap(bm);
              }
            }
          });
          /**
           * 特别注意：和普通图文类原生广告一样，渲染带有视频素材的原生广告时，也需要开发者调用曝光接口onExposured来曝光广告，否则onClicked点击接口将无效
           */
          mAD.onExposured(mAdContainer);

          // 加载视频
          if (mAD.isVideoAD()) {
            mAD.preLoadVideo();
          }
        }

      }

      @Override
      public void onNoAD(int errorCode) {
        Toast.makeText(getApplicationContext(), "加载失败，错误码：" + errorCode, Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onADStatusChanged(NativeMediaADData ad) {
      }

      @Override
      public void onADError(NativeMediaADData adData, int errorCode) {
        Log.i(TAG, adData.getTitle() + " onADError: " + errorCode);
      }

      @Override
      public void onADVideoLoaded(NativeMediaADData adData) {
        Log.i(TAG, adData.getTitle() + " ---> 视频加载完成"); // 仅仅是加载视频文件完成，如果没有绑定MediaView视频仍然不可以播放
        bindMediaView();
      }

      @Override
      public void onADExposure(NativeMediaADData adData) {
        Log.i(TAG, adData.getTitle() + " onADExposure");
      }

      @Override
      public void onADClicked(NativeMediaADData adData) {
        Log.i(TAG, adData.getTitle() + " onADClicked");
      }
    };

    mADManager = new NativeMediaAD(getApplicationContext(), Constants.APPID, Constants.NativeVideoPosID, listener);
  }

  private void bindMediaView() {
    if (mAD.isVideoAD()) {
      // 首先把预设的大图隐藏，显示出MediaView。一定要保证MediaView可见，才能播放视频，否则SDK将无法上报曝光效果并计费。
      mImageView.setVisibility(View.INVISIBLE);
      mMediaView.setVisibility(View.VISIBLE);
      // bindView时指定第二个参数为false，则不会调用广点通的默认视频控制条。贴片场景下可能不太需要用到SDK默认的控制条。
      mAD.bindView(mMediaView, false);  
      mAD.play();
      mAD.setVolumeOn(true);

      mAD.setMediaListener(new MediaListener() {

        @Override
        public void onVideoReady(long videoDuration) {
          Log.i(TAG, "onVideoReady");
          duration = videoDuration;
        }

        @Override
        public void onVideoStart() {
          Log.i(TAG, "onVideoStart");
          tikTokHandler.post(countDown);
          mTextCountDown.setVisibility(View.VISIBLE);
          mButtonDetail.setVisibility(View.VISIBLE);
        }

        @Override
        public void onVideoPause() {
          Log.i(TAG, "onVideoPause");
        }

        @Override
        public void onVideoComplete() {
          Log.i(TAG, "onVideoComplete");
          releaseCountDown();
          mAdContainer.setVisibility(View.GONE);
          mVideoView.setVisibility(View.VISIBLE);
          mVideoView.start();
        }

        @Override
        public void onVideoError(int errorCode) {
          Log.i(TAG, "onVideoError");
          releaseCountDown();
          mAdContainer.setVisibility(View.GONE);
          mVideoView.setVisibility(View.VISIBLE);
          mVideoView.start();
        }

        @Override
        public void onADButtonClicked() {
          // 当广点通默认视频控制界面中的“查看详情/免费下载”按钮被点击时，会回调此接口，如果没有使用广点通的控制条此接口不会被回调
          Log.i(TAG, "onVideoADClicked");
        }

        @Override
        public void onReplayButtonClicked() {
          // 当广点通默认视频控制界面中的“重新播放”按钮被点击时，会回调此接口
          Log.i(TAG, "onVideoReplay");
        }

        @Override
        public void onFullScreenChanged(boolean inFullScreen) {
          Log.i(TAG, "onFullScreenChanged, inFullScreen = " + inFullScreen);
        }
      });
    }
  }

  private void loadAD() {
    if (mADManager != null) {
      try {
        mADManager.loadAD(AD_COUNT);
      } catch (Exception e) {
        Toast.makeText(getApplicationContext(), "加载失败，请重试", Toast.LENGTH_SHORT).show();
      }
    }
  }

  @Override
  protected void onResume() {
    if (mAD != null) {
      mAD.resume();
    }
    super.onResume();
  }

  @Override
  protected void onPause() {
    if (mAD != null) {
      mAD.stop();
    }
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    Log.i(TAG, "onDestroy");
    if (mAD != null) {
      mAD.destroy();
    }
    super.onDestroy();
  }

  /**
   * 如果要使得MediaView也能跟随屏幕旋转而全屏播放，请处理好运行时变更
   */
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    Log.d(TAG, "onConfigurationChanged");
    // 1.开发者请把新的横屏LayoutParams或者竖屏LayoutParams设置给MediaView
    setMediaParams(newConfig);
    // 2.通知SDK，发生了运行时变更。这个接口也是增强型接口，开发者可以自己实现同样的功能。
    if (mAD != null) {
      mAD.onConfigurationChanged(newConfig);
    }

    super.onConfigurationChanged(newConfig);
  }

  /** 设置好横屏、竖屏的LayoutParams */
  private void setMediaParams(Configuration newConfig) {
    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
      Log.d(TAG, "ORIENTATION_LANDSCAPE");
      if (portraitParams == null) { // 先保存竖屏的params
        portraitParams = (FrameLayout.LayoutParams) mMediaView.getLayoutParams();
      }

      if (landScapeParams == null) { // 进入横屏，新建一个MATCH_PARENT的LayoutParams
        landScapeParams =
            new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        landScapeParams.gravity = Gravity.CENTER;
      }

      mMediaView.setLayoutParams(landScapeParams);
    } else {
      Log.d(TAG, "ORIENTATION_PORTRAIT");
      if (landScapeParams == null) { // 先保存横屏的params
        landScapeParams = (FrameLayout.LayoutParams) mMediaView.getLayoutParams();
      }

      if (portraitParams == null) { // 进入竖屏，新建一个LayoutParams
        portraitParams =
            new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        portraitParams.gravity = Gravity.TOP;
      }

      mMediaView.setLayoutParams(portraitParams);
    }
  }

  /**
   * 刷新广告倒计时
   */
  private static final String TEXT_COUNTDOWN = "广告倒计时：%s ";
  private long currentPosition, oldPosition, duration;
  private Runnable countDown = new Runnable() {
    public void run() {
      if (mAD != null) {
        currentPosition = mAD.getCurrentPosition();
        long position = currentPosition;
        if (oldPosition == position && mAD.isPlaying()) {
          Log.i(TAG, "玩命加载中...");
          mTextCountDown.setTextColor(Color.WHITE);
          mTextCountDown.setText("玩命加载中...");
        } else {
          mTextCountDown.setTextColor(Color.WHITE);
          mTextCountDown.setText(String.format(TEXT_COUNTDOWN, Math.round((duration - position) / 1000.0) + ""));
        }
        oldPosition = position;
        if (mAD.isPlaying()) {
          tikTokHandler.postDelayed(countDown, 500);
        }
      }
    }

  };

  private void releaseCountDown() {
    if (tikTokHandler != null && countDown != null) {
      tikTokHandler.removeCallbacks(countDown);
    }
  }

}
