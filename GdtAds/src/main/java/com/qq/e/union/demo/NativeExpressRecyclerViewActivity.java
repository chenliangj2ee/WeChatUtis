package com.qq.e.union.demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 在消息流中接入原生模板广告的示例
 *
 * Created by noughtchen on 2017/4/26.
 */

public class NativeExpressRecyclerViewActivity extends Activity implements
    NativeExpressAD.NativeExpressADListener {

  private static final String TAG = NativeExpressRecyclerViewActivity.class.getSimpleName();
  public static final int MAX_ITEMS = 30;
  public static final int AD_COUNT = 3;    // 加载广告的条数，取值范围为[1, 10]
  public static int FIRST_AD_POSITION = 3; // 第一条广告的位置
  public static int ITEMS_PER_AD = 10;     // 每间隔10个条目插入一条广告

  private RecyclerView mRecyclerView;
  private LinearLayoutManager mLinearLayoutManager;
  private CustomAdapter mAdapter;
  private List<NormalItem> mNormalDataList = new ArrayList<NormalItem>();
  private NativeExpressAD mADManager;
  private List<NativeExpressADView> mAdViewList;
  private HashMap<NativeExpressADView, Integer> mAdViewPositionMap = new HashMap<NativeExpressADView, Integer>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_native_express_recycler_view);
    mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    mRecyclerView.setHasFixedSize(true);
    mLinearLayoutManager = new LinearLayoutManager(this);
    mRecyclerView.setLayoutManager(mLinearLayoutManager);
    initData();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    // 使用完了每一个NativeExpressADView之后都要释放掉资源。
    if (mAdViewList != null) {
      for (NativeExpressADView view : mAdViewList) {
        view.destroy();
      }
    }
  }

  private void initData() {
    for (int i = 0; i < MAX_ITEMS; ++i) {
      mNormalDataList.add(new NormalItem("No." + i + " Normal Data"));
    }
    mAdapter = new CustomAdapter(mNormalDataList);
    mRecyclerView.setAdapter(mAdapter);
    initNativeExpressAD();
  }


  private void initNativeExpressAD() {
    final float density = getResources().getDisplayMetrics().density;
    ADSize adSize = new ADSize((int) (getResources().getDisplayMetrics().widthPixels / density), 340); // 宽、高的单位是dp。ADSize不支持MATCH_PARENT or WRAP_CONTENT，必须传入实际的宽高
    mADManager = new NativeExpressAD(NativeExpressRecyclerViewActivity.this, adSize, Constants.APPID, Constants.NativeExpressPosID, this);
    mADManager.loadAD(AD_COUNT);
  }

  @Override
  public void onNoAD(int errorCode) {
    Log.i(TAG, "onNoAD: errorCode = " + errorCode);
  }

  @Override
  public void onADLoaded(List<NativeExpressADView> adList) {
    Log.i(TAG, "onADLoaded: " + adList.size());
    mAdViewList = adList;
    for (int i = 0; i < mAdViewList.size(); i++) {
      int position = FIRST_AD_POSITION + ITEMS_PER_AD * i;
      if (position < mNormalDataList.size()) {
        mAdViewPositionMap.put(mAdViewList.get(i), position); // 把每个广告在列表中位置记录下来
        mAdapter.addADViewToPosition(position, mAdViewList.get(i));
      }
    }
    mAdapter.notifyDataSetChanged();
  }

  @Override
  public void onRenderFail(NativeExpressADView adView) {
    Log.i(TAG, "onRenderFail: " + adView.toString());
  }

  @Override
  public void onRenderSuccess(NativeExpressADView adView) {
    Log.i(TAG, "onRenderSuccess: " + adView.toString());
  }

  @Override
  public void onADExposure(NativeExpressADView adView) {
    Log.i(TAG, "onADExposure: " + adView.toString());
  }

  @Override
  public void onADClicked(NativeExpressADView adView) {
    Log.i(TAG, "onADClicked: " + adView.toString());
  }

  @Override
  public void onADClosed(NativeExpressADView adView) {
    Log.i(TAG, "onADClosed: " + adView.toString());
    if (mAdapter != null) {
      int removedPosition = mAdViewPositionMap.get(adView);
      mAdapter.removeADView(removedPosition, adView);
    }
  }

  @Override
  public void onADLeftApplication(NativeExpressADView adView) {
    Log.i(TAG, "onADLeftApplication: " + adView.toString());
  }

  @Override
  public void onADOpenOverlay(NativeExpressADView adView) {
    Log.i(TAG, "onADOpenOverlay: " + adView.toString());
  }

  public class NormalItem {
    private String title;

    public NormalItem(String title) {
      this.title = title;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }
  }

  /** RecyclerView的Adapter */
  class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {

    static final int TYPE_DATA = 0;
    static final int TYPE_AD = 1;
    private List<Object> mData;

    public CustomAdapter(List list) {
      mData = list;
    }

    // 把返回的NativeExpressADView添加到数据集里面去
    public void addADViewToPosition(int position, NativeExpressADView adView) {
      if (position >= 0 && position < mData.size() && adView != null) {
        mData.add(position, adView);
      }
    }

    // 移除NativeExpressADView的时候是一条一条移除的
    public void removeADView(int position, NativeExpressADView adView) {
      mData.remove(position);
      mAdapter.notifyItemRemoved(position); // position为adView在当前列表中的位置
      mAdapter.notifyItemRangeChanged(0, mData.size() - 1);
    }

    @Override
    public int getItemCount() {
      if (mData != null) {
        return mData.size();
      } else {
        return 0;
      }
    }

    @Override
    public int getItemViewType(int position) {
      return mData.get(position) instanceof NativeExpressADView ? TYPE_AD : TYPE_DATA;
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder customViewHolder, final int position) {
      int type = getItemViewType(position);
      if (TYPE_AD == type) {
        final NativeExpressADView adView = (NativeExpressADView) mData.get(position);
        mAdViewPositionMap.put(adView, position); // 广告在列表中的位置是可以被更新的
        if (customViewHolder.container.getChildCount() > 0
            && customViewHolder.container.getChildAt(0) == adView) {
          return;
        }

        if (customViewHolder.container.getChildCount() > 0) {
          customViewHolder.container.removeAllViews();
        }

        if (adView.getParent() != null) {
          ((ViewGroup) adView.getParent()).removeView(adView);
        }

        customViewHolder.container.addView(adView);
        adView.render(); // 调用render方法后sdk才会开始展示广告
      } else {
        customViewHolder.title.setText(((NormalItem) mData.get(position)).getTitle());
      }
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
      int layoutId = (viewType == TYPE_AD) ? R.layout.item_express_ad : R.layout.item_data;
      View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, null);
      CustomViewHolder viewHolder = new CustomViewHolder(view);
      return viewHolder;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
      public TextView title;
      public ViewGroup container;

      public CustomViewHolder(View view) {
        super(view);
        title = (TextView) view.findViewById(R.id.title);
        container = (ViewGroup) view.findViewById(R.id.express_ad_container);
      }
    }

  }


}
