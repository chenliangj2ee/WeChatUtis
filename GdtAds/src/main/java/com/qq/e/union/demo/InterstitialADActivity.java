package com.qq.e.union.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.qq.e.ads.interstitial.AbstractInterstitialADListener;
import com.qq.e.ads.interstitial.InterstitialAD;


public class InterstitialADActivity extends Activity implements OnClickListener {

  InterstitialAD iad;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_interstitial_ad);
    this.findViewById(R.id.showIAD).setOnClickListener(this);
    this.findViewById(R.id.showIADAsPPW).setOnClickListener(this);
    this.findViewById(R.id.closePPWIAD).setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    int i = v.getId();
    if (i == R.id.showIAD) {
      showAD();

    } else if (i == R.id.showIADAsPPW) {
      showAsPopup();

    } else if (i == R.id.closePPWIAD) {
      closeAsPopup();

    } else {
    }
  }


  private InterstitialAD getIAD() {
    if (iad == null) {
      iad = new InterstitialAD(this, Constants.APPID, Constants.InterteristalPosID);
    }
    return iad;
  }

  private void showAD() {
    getIAD().setADListener(new AbstractInterstitialADListener() {

      @Override
      public void onNoAD(int arg0) {
        Log.i("AD_DEMO", "LoadInterstitialAd Fail:" + arg0);
      }

      @Override
      public void onADReceive() {
          Log.i("AD_DEMO", "onADReceive");
        iad.show();
      }
    });
    iad.loadAD();
  }

  private void showAsPopup() {
    getIAD().setADListener(new AbstractInterstitialADListener() {

      @Override
      public void onNoAD(int arg0) {
        Log.i("AD_DEMO", "LoadInterstitialAd Fail:" + arg0);
      }

      @Override
      public void onADReceive() {
        iad.showAsPopupWindow();
      }
    });
    iad.loadAD();
  }

  private void closeAsPopup() {
    if (iad != null) {
      iad.closePopupWindow();
    }
  }

}
