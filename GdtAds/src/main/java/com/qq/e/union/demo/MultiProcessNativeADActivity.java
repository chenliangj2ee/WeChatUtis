package com.qq.e.union.demo;

import android.os.Bundle;

import com.qq.e.ads.cfg.MultiProcessFlag;

public class MultiProcessNativeADActivity extends NativeADActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // 应该在广告初始化前调用此方法
    MultiProcessFlag.setMultiProcess(true);
    setTitle("广点通：原生广告-多进程");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }
}
