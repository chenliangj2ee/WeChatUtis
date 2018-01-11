package com.qq.e.union.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


/**
 * 一、原生视频广告的接入示例：<br/>
 *
 * 1.介绍了如何在不同Layout中嵌入MediaView来播放视频<br/>
 * 2.介绍了如何使用广点通SDK默认视频播放控制条，如何使用SDK提供的接口来自定义播放器控制条（如贴片、倒计时）<br/>
 * 3.介绍了在Scrollable场景下如ListView、RecyclerView等，如何使用SDK的play接口和onScroll接口，自动管理视频的播放和暂停<br/>
 *
 * 请开发者仔细阅读接入文档，了解原生视频广告的API，再根据自己的需要参考不同场景下的接入示例。
 *
 * 二、原生视频广告的基本逻辑是：<br/>
 *
 * 1.首先加载原生广告实例，广点通支持一次加载1-30条原生广告，支持视频类原生广告和非视频类原生广告混出（如果需要配置只出视频素材，请联系广点通商务团队申请）。
 * 2.得到广告实例，调用preLoadVideo接口加载视频素材。
 * 3.当视频素材加载完成后，可以把MediaView组件和广告对象bind起来，进行视频播放。MediaView是广点通SDK提供的一个视频播放组件，可以管理视频播放效果的上报。
 */
public class NativeVideoADActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_native_video_ad);
  }

  /** 普通界面中展示原生视频广告，演示了最基本的原生视频广告接口应该如何调用 */
  public void onNormalViewClicked(View view) {
    Intent intent = new Intent();
    intent.setClass(NativeVideoADActivity.this, NativeVideoDemoActivity.class);
    NativeVideoADActivity.this.startActivity(intent);
  }

  // 原生视频广告作为前贴片时的接入示例
  public void onPreMovieAdClicked(View view) {
    Intent intent = new Intent(this, NativeVideoPreMovieActivity.class);
    startActivity(intent);
  }

  // ListView中展示原生视频广告，可以调用广点通提供的onScroll方法，根据广告在屏幕中的可见性，自动控制视频的播放和暂停
  public void onListViewClicked(View view) {
    Intent intent = new Intent();
    intent.setClass(NativeVideoADActivity.this, NativeVideoListDemoActivity.class);
    NativeVideoADActivity.this.startActivity(intent);
  }

  // RecyclerView中展示原生视频广告，可以调用广点通提供的onScroll方法，根据广告在屏幕中的可见性，自动控制视频的播放和暂停
  public void onRecyclerViewClicked(View view) {
    Intent intent = new Intent(NativeVideoADActivity.this, NativeVideoRecyclerViewActivity.class);
    NativeVideoADActivity.this.startActivity(intent);
  }

  // ScrollView中展示原生视频广告，可以调用广点通提供的onScroll方法，根据广告在屏幕中的可见性，自动控制视频的播放和暂停
  public void onScrollViewClicked(View view) {
    Intent intent = new Intent(NativeVideoADActivity.this, NativeVideoScrollViewActivity.class);
    NativeVideoADActivity.this.startActivity(intent);
  }

}
