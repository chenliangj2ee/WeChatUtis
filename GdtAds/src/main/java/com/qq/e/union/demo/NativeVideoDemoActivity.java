package com.qq.e.union.demo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
 * 原生视频广告接入的普通场景，这里示例里面演示了如何使用原生视频广告里面最基本的接口。
 *
 * 在广点通SDK中，带有视频素材的原生广告和普通图文原生广告可以混合拉取。当拉取多条广告时，返回的列表中可能既有视频类原生广告，又有普通的图片类原生广告。
 * （普通图文原生广告是2图2文，带视频素材的原生广告是2图2文1视频）
 *
 *
 * 一般拉取原生视频广告的步骤分为如下3步：
 * - 1. loadAD(int adCount)：拉取成功后会回调onADLoaded方法，此方法中可以获取到广告对象列表adList。
 * - 2. preLoadVideo()：当一个广告对象为视频广告（即ad.isVideoAD()=true）时，需要调用preLoadVideo方法来缓存视频。视频缓存成功后，会回调onADVideoLoaded方法通知开发者。
 * - 3. bindMediaView(MediaView view, boolean useDefaultController)：当视频缓存完成后，可以给这个广告对象bind一个MediaView组件，并调用play方法来播放视频。
 *
 *
 * 与视频相关的基本接口：
 * - preLoadVideo()：预加载视频素材
 * - bindView(MediaView view, boolean useDefaultController)：绑定MediaView组件，需在视频加载完成后调用
 * - play()：播放
 * - stop()：停止
 * - resume()：恢复播放
 * - destroy()：销毁资源
 *
 */
public class NativeVideoDemoActivity extends Activity {

  private static final String TAG = NativeVideoDemoActivity.class.getSimpleName();
  public static final int AD_COUNT = 1;                 // 一次拉取的广告条数：范围和以前的原生广告一样，是1-10。但是我们强烈建议：需要展示几个广告就加载几个，不要过多加载广告而不去曝光它们。这样会导致曝光率过低。

  // 与广告无关的变量，本示例中的一些其他UI
  private AQuery mAQuery;                               // 用于加载图片AQuery开源组件，开发者可根据需求使用自己的图片加载框架
  private Button mDownloadButton;
  private RelativeLayout mADInfoContainer;
  private TextView textLoadResult;

  // 与广告有关的变量，用来显示广告素材的UI
  private NativeMediaADData mAD;                        // 加载的原生视频广告对象，本示例为简便只演示加载1条广告的示例
  private NativeMediaAD mADManager;                     // 原生广告manager，用于管理广告数据的加载，监听广告回调
  private MediaView mMediaView;                         // 广点通视频容器，需要开发者添加到自己的xml布局文件中，用于播放视频
  private ImageView mImagePoster;                       // 广告大图，没有加载好视频素材前，先显示广告的大图
  private final Handler tikTokHandler = new Handler();  // 倒计时读取Handler，开发者可以按照自己的设计实现，此处仅供参考
  private TextView mTextCountDown;                      // 倒计时显示的TextView，广点通原生视频广告SDK提供了非常多的接口，允许开发者自由地定制视频播放器控制条和倒计时等UI

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_native_video_demo);
    initView();
    initNativeVideoAD();
    loadAD();
  }

  private void initView() {
    mMediaView = (MediaView) findViewById(R.id.gdt_media_view);
    mImagePoster = (ImageView) findViewById(R.id.img_poster);
    mADInfoContainer = (RelativeLayout) this.findViewById(R.id.ad_info_container);
    mDownloadButton = (Button) mADInfoContainer.findViewById(R.id.btn_download);
    mTextCountDown = (TextView) findViewById(R.id.text_count_down);
    textLoadResult = (TextView) findViewById(R.id.text_load_result);
    mAQuery = new AQuery((RelativeLayout) findViewById(R.id.root));
  }

  private void initNativeVideoAD() {
    NativeMediaADListener listener = new NativeMediaADListener() {

      /**
       * 广告加载成功
       *
       * @param adList  广告对象列表
       */
      @Override
      public void onADLoaded(List<NativeMediaADData> adList) {
        if (adList.size() > 0) {
          mAD = adList.get(0);
          /**
           * 加载广告成功，开始渲染。
           */
          initADUI();

          if (mAD.isVideoAD()) {
            /**
             * 如果该条原生广告是一条带有视频素材的广告，还需要先调用preLoadVideo接口来加载视频素材：
             *    - 加载成功：回调NativeMediaADListener.onADVideoLoaded(NativeMediaADData adData)方法
             *    - 加载失败：回调NativeMediaADListener.onADError(NativeMediaADData adData, int errorCode)方法，错误码为700
             */
            mAD.preLoadVideo();
            textLoadResult.setText("isVideoAD()=true：" + "这是一条视频广告");
          } else {
            /**
             * 如果该条原生广告只是一个普通图文的广告，不带视频素材，那么渲染普通的UI即可。
             */
            textLoadResult.setText("isVideoAD()=false：" + "这不是一条视频广告");
          }

        }
      }

      /**
       * 广告加载失败
       *
       * @param errorCode   广告加载失败的错误码，错误码含义请参考开发者文档第4章。
       */
      @Override
      public void onNoAD(int errorCode) {
        textLoadResult.setText(String.format("广告加载失败，错误码：%d", errorCode));
      }

      /**
       * 广告状态发生变化，对于App类广告而言，下载/安装的状态和下载进度可以变化。
       *
       * @param ad    状态发生变化的广告对象
       */
      @Override
      public void onADStatusChanged(NativeMediaADData ad) {
        if (ad != null && ad.equals(mAD)) {
          updateADUI();   // App类广告在下载过程中，下载进度会发生变化，如果开发者需要让用户了解下载进度，可以更新UI。
        }
      }

      /**
       * 广告处理发生错误，当调用一个广告对象的onExposured、onClicked、preLoadVideo接口时，如果发生了错误会回调此接口，具体的错误码含义请参考开发者文档。
       *
       * @param adData    广告对象
       * @param errorCode 错误码，700表示视频加载失败，701表示视频播放时出现错误
       */
      @Override
      public void onADError(NativeMediaADData adData, int errorCode) {
        Log.i(TAG, adData.getTitle() + " onADError: " + errorCode);
      }

      /**
       * 当调用一个广告对象的preLoadVideo接口时，视频素材加载完成后，会回调此接口，在此回调中可以给广告对象绑定MediaView组件播放视频。
       *
       * @param adData  视频素材加载完成的广告对象，很显然这个广告一定是一个带有视频素材的广告，需要给它bindView并播放它
       */
      @Override
      public void onADVideoLoaded(NativeMediaADData adData) {
        Log.i(TAG, adData.getTitle() + " ---> 视频素材加载完成"); // 仅仅是加载视频文件完成，如果没有绑定MediaView视频仍然不可以播放
        bindMediaView();
      }

      /**
       * 广告曝光时的回调
       *
       * 注意：带有视频素材的原生广告可以多次曝光 按照曝光计费
       * 没带有视频素材的广告只能曝光一次 按照点击计费
       *
       * @param adData  曝光的广告对象
       */
      @Override
      public void onADExposure(NativeMediaADData adData) {
        Log.i(TAG, adData.getTitle() + " onADExposure");
      }

      /**
       * 广告被点击时的回调
       *
       * @param adData  被点击的广告对象
       */
      @Override
      public void onADClicked(NativeMediaADData adData) {
        Log.i(TAG, adData.getTitle() + " onADClicked");
      }
    };

    mADManager = new NativeMediaAD(getApplicationContext(), Constants.APPID, Constants.NativeVideoPosID, listener);
  }

  /**
   * 将广告实例和MediaView绑定，播放视频。
   *
   * 注意：播放视频前需要将广告的大图隐藏，将MediaView显示出来，否则视频将无法播放，也无法上报视频曝光，无法产生计费。
   */
  private void bindMediaView() {
    if (mAD.isVideoAD()) {
      mImagePoster.setVisibility(View.GONE);
      mMediaView.setVisibility(View.VISIBLE);

      /**
       * bindView(MediaView view, boolean useDefaultController):
       *    - useDefaultController: false，不会调用广点通的默认视频控制条
       *    - useDefaultController: true，使用SDK内置的播放器控制条，此时开发者需要把demo下的res文件夹里面的图片拷贝到自己项目的res文件夹去
       *
       * 在这里绑定MediaView后，SDK会根据视频素材的宽高比例，重新给MediaView设置新的宽高
       */
      mAD.bindView(mMediaView, true);
      mAD.play();

      /** 设置视频播放过程中的监听器 */
      mAD.setMediaListener(new MediaListener() {

        /**
         * 视频播放器初始化完成，准备好可以播放了
         *
         * @param videoDuration 视频素材的总时长
         */
        @Override
        public void onVideoReady(long videoDuration) {
          Log.i(TAG, "onVideoReady, videoDuration = " + videoDuration);
          duration = videoDuration;
        }

        /** 视频开始播放 */
        @Override
        public void onVideoStart() {
          Log.i(TAG, "onVideoStart");
          tikTokHandler.post(countDown);
          mTextCountDown.setVisibility(View.VISIBLE);
        }

        /** 视频暂停 */
        @Override
        public void onVideoPause() {
          Log.i(TAG, "onVideoPause");
          mTextCountDown.setVisibility(View.GONE);
        }

        /** 视频自动播放结束，到达最后一帧 */
        @Override
        public void onVideoComplete() {
          Log.i(TAG, "onVideoComplete");
          releaseCountDown();
          mTextCountDown.setVisibility(View.GONE);
        }

        /** 视频播放时出现错误 */
        @Override
        public void onVideoError(int errorCode) {
          Log.i(TAG, "onVideoError, errorCode: " + errorCode);
        }

        /** SDK内置的播放器控制条中的重播按钮被点击 */
        @Override
        public void onReplayButtonClicked() {
          Log.i(TAG, "onReplayButtonClicked");
        }

        /**
         * SDK内置的播放器控制条中的下载/查看详情按钮被点击
         * 注意: 这里是指UI中的按钮被点击了，而广告的点击事件回调是在onADClicked中，开发者如需统计点击只需要在onADClicked回调中进行一次统计即可。
         */
        @Override
        public void onADButtonClicked() {
          Log.i(TAG, "onADButtonClicked");
        }

        /** SDK内置的全屏和非全屏切换回调，进入全屏时inFullScreen为true，退出全屏时inFullScreen为false */
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
    mAQuery.id(R.id.img_logo).image(mAD.getIconUrl(), false, true);
    mAQuery.id(R.id.img_poster).image(mAD.getImgUrl(), false, true, 0, 0, new BitmapAjaxCallback() {
      @Override
      protected void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {
        // AQuery框架有一个问题，就是即使在图片加载完成之前将ImageView设置为了View.GONE，在图片加载完成后，这个ImageView会被重新设置为VIEW.VISIBLE。
        // 所以在这里需要判断一下，如果已经把ImageView设置为隐藏，开始播放视频了，就不要再显示广告的大图。否则将影响到sdk的曝光上报，无法产生收益。
        // 开发者在用其他的图片加载框架时，也应该注意检查下是否有这个问题。
        if (iv.getVisibility() == View.VISIBLE) {
          iv.setImageBitmap(bm);
        }
      }
    });
    mAQuery.id(R.id.text_title).text(mAD.getTitle());
    mAQuery.id(R.id.text_desc).text(mAD.getDesc());
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
        Toast.makeText(getApplicationContext(), "加载失败", Toast.LENGTH_SHORT).show();
      }
    }
  }

  /**
   * 在Activity生命周期中一定要调用视频广告的暂停和播放接口，保证用户在Activity之间切换时，视频可以暂停和续播，同时也保证数据上报正常，否则将影响到开发者的收益：
   *    - Activity.onResume() ：  调用NativeMediaADData.resume()
   *    - Activity.onPause()  ：  调用NativeMediaADData.stop()，广点通目前在产品概念上要求每次观看视频广告都要重头播放，目前不提供暂停接口。调用stop接口，一个作用是保证用户体验，当用户看不到当前界面时不要再播放视频。另一个作用是触发SDK的播放信息上报事件，保证每次观看都有效果上报。
   *    - Activity.onDestroy()：  调用NativeMediaADData.destroy()释放资源，不再播放视频广告时，一定要记得调用。
   */
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
    releaseCountDown();
    super.onDestroy();
  }

  /**
   * 刷新广告倒计时，本示例提供的思路仅开发者供参考，开发者完全可以根据自己的需求设计不同的样式。
   */
  private static final String TEXT_COUNTDOWN = "广告倒计时：%s ";
  private long currentPosition, oldPosition, duration;
  private Runnable countDown = new Runnable() {
    public void run() {
      if (mAD != null) {
        currentPosition = mAD.getCurrentPosition();
        long position = currentPosition;
        if (oldPosition == position && mAD.isPlaying()) {
          Log.d(TAG, "玩命加载中...");
          mTextCountDown.setTextColor(Color.WHITE);
          mTextCountDown.setText("玩命加载中...");
        } else {
          Log.d(TAG, String.format(TEXT_COUNTDOWN, Math.round((duration - position) / 1000.0) + ""));
          mTextCountDown.setText(String.format(TEXT_COUNTDOWN, Math.round((duration - position) / 1000.0) + ""));
        }
        oldPosition = position;
        if (mAD.isPlaying()) {
          tikTokHandler.postDelayed(countDown, 500); // 500ms刷新一次进度即可
        }
      }
    }

  };

  private void releaseCountDown() {
    if (countDown != null) {
      tikTokHandler.removeCallbacks(countDown);
    }
  }

}
