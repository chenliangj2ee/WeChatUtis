package com.qq.e.union.demo;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.qq.e.ads.nativ.MediaListener;
import com.qq.e.ads.nativ.MediaView;
import com.qq.e.ads.nativ.NativeMediaAD;
import com.qq.e.ads.nativ.NativeMediaAD.NativeMediaADListener;
import com.qq.e.ads.nativ.NativeMediaADData;

import java.util.List;

/**
 * ScrollView中展示原生广告的示例，这个示例在普通场景的基础上演示了onScroll接口的用法，这个接口可以帮助开发者自动管理视频的播放和停止。
 *
 *
 * 增强型接口onScroll：
 * - NativeMediaADData.onScroll(int position, View view)：
 *      - position参数： 可以传入任意int值，当View参数为ScrollView时，SDK内部不会使用这个值。
 *      - view参数：     ScrollView实例
 *
 *
 * 在ScrollView这种可以滑动的场景下，请开发者一定要管理好视频的播放和停止，不要让广告处于屏幕可见范围之外时，还在播放视频，这将对用户体验造成不好的影响，也会影响到SDK内部的数据上报从而影响开发者的收入。
 * 如果开发者不选用onScroll接口，请自己根据MediaView的可见性，调用基础接口来管理视频的播放和停止，保证视频在播放时MediaView是在屏幕中可见的。
 */
public class NativeVideoScrollViewActivity extends Activity {

  private static final String TAG = NativeVideoScrollViewActivity.class.getSimpleName();
  public static final int AD_COUNT = 1;                 // 一次拉取的广告条数：范围和以前的原生广告一样，是1-10。但是我们强烈建议：需要展示几个广告就加载几个，不要过多加载广告而不去曝光它们。这样会导致曝光率过低。
  private AQuery mAQuery;                               // 用于加载图片AQuery开源组件，开发者可根据需求使用自己的图片加载框架
  private Button mDownloadButton;
  private RelativeLayout mADInfoContainer;
  private ScrollView mScrollView;

  private NativeMediaADData mAD;                        // 加载的原生视频广告对象，本示例为简便只演示加载1条广告的示例
  private NativeMediaAD mADManager;                     // 原生广告manager，用于管理广告数据的加载，监听广告回调
  private MediaView mMediaView;                         // 广点通视频容器，需要开发者添加到自己的xml布局文件中，用于播放视频
  private ImageView mImagePoster;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_native_video_scroll_view);
    initView();
    initNativeVideoAD();
    loadAD();
  }

  private void initView() {
    mScrollView = (ScrollView) findViewById(R.id.scrollview);
    mMediaView = (MediaView) findViewById(R.id.gdt_media_view);
    mImagePoster = (ImageView) findViewById(R.id.img_poster);
    mADInfoContainer = (RelativeLayout) findViewById(R.id.ad_info_container);
    mDownloadButton = (Button) findViewById(R.id.btn_download);
    mAQuery = new AQuery((RelativeLayout) findViewById(R.id.ad_container));

    // 监听ScrollView的滚动，调用onScroll接口，SDK就会帮助开发者自动管理视频的播放和停止。
    mScrollView.getViewTreeObserver().addOnScrollChangedListener(
        new ViewTreeObserver.OnScrollChangedListener() {

          @Override
          public void onScrollChanged() {
            if (mAD != null && mAD.isVideoAD()) {
              mAD.onScroll(0, mScrollView);
            }
          }
        });
  }

  private void initNativeVideoAD() {
    NativeMediaADListener listener = new NativeMediaADListener() {

      @Override
      public void onADLoaded(List<NativeMediaADData> ads) {
        Toast.makeText(getApplicationContext(), "成功加载原生广告：" + ads.size() + "条",
            Toast.LENGTH_SHORT).show();
        if (ads.size() > 0) {
          mAD = ads.get(0);
          initADUI();

          if (mAD.isVideoAD()) {
            mAD.preLoadVideo();
          }
        }
      }

      @Override
      public void onNoAD(int errorCode) {
        Toast.makeText(getApplicationContext(), "加载失败，错误码：" + errorCode,
            Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onADStatusChanged(NativeMediaADData ad) {
        if (ad != null && ad.equals(mAD)) {
          updateADUI();
        }
      }

      @Override
      public void onADError(NativeMediaADData adData, int errorCode) {
        Log.i(TAG, adData.getTitle() + " 广告发生错误，错误码： " + errorCode);
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
      mImagePoster.setVisibility(View.INVISIBLE);
      mMediaView.setVisibility(View.VISIBLE);

      mAD.bindView(mMediaView, true); // 指定第二个参数为true，使用广点通的默认视频控制条
      mAD.play();

      mAD.setMediaListener(new MediaListener() {

        @Override
        public void onVideoReady(long videoDuration) {
          Log.i(TAG, "onVideoReady");

        }

        @Override
        public void onVideoStart() {
          Log.i(TAG, "onVideoStart");
        }

        @Override
        public void onVideoPause() {
          Log.i(TAG, "onVideoPause");
        }

        @Override
        public void onVideoComplete() {
          Log.i(TAG, "onVideoComplete");
        }

        @Override
        public void onVideoError(int errorCode) {
          Log.i(TAG, "onVideoError");
        }

        @Override
        public void onReplayButtonClicked() {
          Log.i(TAG, "onReplayButtonClicked");
        }

        @Override
        public void onADButtonClicked() {
          Log.i(TAG, "onADButtonClicked");
        }

        @Override
        public void onFullScreenChanged(boolean inFullScreen) {
          Log.i(TAG, "onFullScreenChanged, inFullScreen = " + inFullScreen);
          // 原生视频广告默认静音播放，进入到全屏后建议开发者可以设置为有声播放
          if (inFullScreen) {
            mAD.setVolumeOn(true);
          } else {
            mAD.setVolumeOn(false);
          }
        }
      });
    }
  }

  private void initADUI() {
    mAQuery.id(R.id.img_logo).image(TextUtils.isEmpty(mAD.getIconUrl())? mAD.getImgUrl() : mAD.getIconUrl(), false, true);
    mAQuery.id(R.id.img_poster).image((String) mAD.getImgUrl(), false, true, 0, 0, new BitmapAjaxCallback() {
      @Override
      protected void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {
        // AQuery框架有一个问题，就是即使在图片加载完成之前将ImageView设置为了View.GONE，在图片加载完成后，这个ImageView会被重新设置为VIEW.VISIBLE。
        // 所以在这里需要判断一下，如果已经把ImageView设置为隐藏，开始播放视频了，就不要再显示广告的大图。
        if (iv.getVisibility() == View.VISIBLE) {
          iv.setImageBitmap(bm);
        }
      }
    });
    mAQuery.id(R.id.text_title).text((String) mAD.getTitle());
    mAQuery.id(R.id.text_desc).text((String) mAD.getDesc());
    updateADUI();
    /**
     * 注意：在渲染时，必须先调用onExposured接口曝光广告，否则点击接口onClicked将无效
     */
    mAD.onExposured(mADInfoContainer);
    mDownloadButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View view) {
        mAD.onClicked(view);
      }
    });
  }

  private void updateADUI() {
    if (!mAD.isAPP()) {
      mDownloadButton.setText("浏览");
      return;
    }
    switch (mAD.getAPPStatus()) {
      case 0:
        mDownloadButton.setText("下载");
        break;
      case 1:
        mDownloadButton.setText("启动");
        break;
      case 2:
        mDownloadButton.setText("更新");
        break;
      case 4:
        mDownloadButton.setText(mAD.getProgress() + "%");
        break;
      case 8:
        mDownloadButton.setText("安装");
        break;
      case 16:
        mDownloadButton.setText("下载失败，重新下载");
        break;
      default:
        mDownloadButton.setText("浏览");
        break;
    }
  }

  private void loadAD() {
    if (mADManager != null) {
      try {
        mADManager.loadAD(AD_COUNT);
      } catch (Exception e) {
        Toast.makeText(getApplicationContext(), "加载失败", Toast.LENGTH_SHORT)
            .show();
      }
    }
  }

  @Override
  protected void onResume() {
    Log.i(TAG, "onResume");

    if (mAD != null) {
      mAD.resume();
    }
    super.onResume();
  }

  @Override
  protected void onPause() {
    Log.i(TAG, "onPause");

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

}
