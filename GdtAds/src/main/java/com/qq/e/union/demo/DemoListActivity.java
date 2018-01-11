package com.qq.e.union.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Toast;


public class DemoListActivity extends Activity implements OnClickListener {

  private static final String TAG = DemoListActivity.class.getSimpleName();
  @SuppressLint("NewApi")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    if (Build.VERSION.SDK_INT >= 19) {
      WebView.setWebContentsDebuggingEnabled(true);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      Toast.makeText(this, "广点通，结盟而赢", Toast.LENGTH_LONG).show();
      return true;
    }
    if (id == R.id.action_splash) {
      startActivity(new Intent(this, SplashActivity.class));
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onClick(View v) {
    int i = v.getId();
    if (i == R.id.bannerADButton) {
      startActivity(new Intent(this, BannerActivity.class));

    } else if (i == R.id.interstitialADButton) {
      startActivity(new Intent(this, InterstitialADActivity.class));

    } else if (i == R.id.nativeADButton) {
      startActivity(new Intent(this, NativeADActivity.class));

    } else if (i == R.id.nativeADButton_MP) {
      startActivity(new Intent(this, MultiProcessNativeADActivity.class));

    } else if (i == R.id.nativeVideoADButton) {
      startActivity(new Intent(this, NativeVideoADActivity.class));

    } else if (i == R.id.nativeExpressADButton) {
      startActivity(new Intent(this, NativeExpressActivity.class));

    } else if (i == R.id.nativeExpressFeedADButton) {
      startActivity(new Intent(this, NativeExpressRecyclerViewActivity.class));

    } else {
    }
  }
}
