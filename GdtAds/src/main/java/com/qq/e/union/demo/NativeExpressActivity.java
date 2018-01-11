package com.qq.e.union.demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;

import java.util.List;

/**
 * 原生模板广告基本接入示例
 *
 * Created by noughtchen on 2017/4/17.
 */

public class NativeExpressActivity extends Activity implements View.OnClickListener,
        NativeExpressAD.NativeExpressADListener {

  private static final String TAG = "NativeExpressActivity";
  private ViewGroup container;
  private NativeExpressAD nativeExpressAD;
  private NativeExpressADView nativeExpressADView;
  private Button buttonRefresh, buttonResize;
  private EditText editTextWidth, editTextHeight; // 编辑框输入的宽高
  private int adWidth, adHeight; // 广告宽高

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_native_express);
    container = (ViewGroup) findViewById(R.id.container);
    editTextWidth = (EditText) findViewById(R.id.editWidth);
    editTextHeight = (EditText) findViewById(R.id.editHeight);
    buttonRefresh = (Button) findViewById(R.id.buttonRefresh);
    buttonResize = (Button) findViewById(R.id.buttonDestroy);
    buttonRefresh.setOnClickListener(this);
    buttonResize.setOnClickListener(this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // 使用完了每一个NativeExpressADView之后都要释放掉资源
    if (nativeExpressADView != null) {
      nativeExpressADView.destroy();
    }
  }

  @Override
  public void onClick(View v) {
    int i = v.getId();
    if (i == R.id.buttonRefresh) {
      refreshAd();

    } else if (i == R.id.buttonDestroy) {
      resizeAd();

    }
  }

  private void refreshAd() {
    if (checkEditTextEmpty()) {
      return;
    }

    if (nativeExpressAD == null || checkEditTextChanged()) {
      adWidth = Integer.valueOf(editTextWidth.getText().toString());
      adHeight = Integer.valueOf(editTextHeight.getText().toString());
      hideSoftInput();
      buttonRefresh.setText("刷新广告");
      ADSize adSize = new ADSize(adWidth, adHeight); // 不支持MATCH_PARENT or WRAP_CONTENT，必须传入实际的宽高
      nativeExpressAD =
          new NativeExpressAD(this, adSize, Constants.APPID, Constants.NativeExpressPosID, this);
    }

    nativeExpressAD.loadAD(1);
  }

  /**
   * 在接入、调试模板广告的过程中，可以利用这个方法调整广告View的大小，找到与自己的App需求适合的最佳广告位尺寸。
   * 确定最佳尺寸后，应该把这个ADSize固定下来，并在构造NativeExpressAD的时候传入，给一个固定的广告位ID去使用。
   * 所以生产环境中，可以不需要再调用NativeExpressADView#setAdSize方法。
   */
  private void resizeAd() {
    if (nativeExpressADView == null) {
      return;
    }

    if (checkEditTextEmpty()) {
      return;
    }

    if (checkEditTextChanged()) {
      adWidth = Integer.valueOf(editTextWidth.getText().toString());
      adHeight = Integer.valueOf(editTextHeight.getText().toString());
      nativeExpressADView.setAdSize(new ADSize(adWidth, adHeight));
      hideSoftInput();
    }
  }

  @Override
  public void onNoAD(int errorCode) {
    Log.i(TAG, "onNoAD: errorCode = " + errorCode);
  }

  @Override
  public void onADLoaded(List<NativeExpressADView> adList) {
    Log.i(TAG, "onADLoaded: " + adList.size());
    // 释放前一个NativeExpressADView的资源
    if (nativeExpressADView != null) {
      nativeExpressADView.destroy();
    }

    if (container.getVisibility() != View.VISIBLE) {
      container.setVisibility(View.VISIBLE);
    }

    if (container.getChildCount() > 0) {
      container.removeAllViews();
    }

    nativeExpressADView = adList.get(0);
    // 保证View被绘制的时候是可见的，否则将无法产生曝光和收益。
    container.addView(nativeExpressADView);
    nativeExpressADView.render();
  }

  @Override
  public void onRenderFail(NativeExpressADView adView) {
    Log.i(TAG, "onRenderFail");
  }

  @Override
  public void onRenderSuccess(NativeExpressADView adView) {
    Log.i(TAG, "onRenderSuccess");
  }

  @Override
  public void onADExposure(NativeExpressADView adView) {
    Log.i(TAG, "onADExposure");
  }

  @Override
  public void onADClicked(NativeExpressADView adView) {
    Log.i(TAG, "onADClicked");
  }

  @Override
  public void onADClosed(NativeExpressADView adView) {
    Log.i(TAG, "onADClosed");
    // 当广告模板中的关闭按钮被点击时，广告将不再展示。NativeExpressADView也会被Destroy，不再可用。
    if (container != null && container.getChildCount() > 0) {
      container.removeAllViews();
      container.setVisibility(View.GONE);
    }
  }

  @Override
  public void onADLeftApplication(NativeExpressADView adView) {
    Log.i(TAG, "onADLeftApplication");
  }

  @Override
  public void onADOpenOverlay(NativeExpressADView adView) {
    Log.i(TAG, "onADOpenOverlay");
  }

  private boolean checkEditTextEmpty() {
    String width = editTextWidth.getText().toString();
    String height = editTextHeight.getText().toString();
    if (TextUtils.isEmpty(width) || TextUtils.isEmpty(height)) {
      Toast.makeText(this, "请先输入广告位的宽、高！", Toast.LENGTH_SHORT).show();
      return true;
    }

    return false;
  }

  private boolean checkEditTextChanged() {
    return Integer.valueOf(editTextWidth.getText().toString()) != adWidth
        || Integer.valueOf(editTextHeight.getText().toString()) != adHeight;
  }

  // 隐藏软键盘，这只是个简单的隐藏软键盘示例实现，与广告sdk功能无关
  private void hideSoftInput() {
    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
            NativeExpressActivity.this.getCurrentFocus().getWindowToken(),
            InputMethodManager.HIDE_NOT_ALWAYS);
  }

}
