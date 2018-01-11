package com.qq.e.union.demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * ListView中接入原生视频广告的示例，这个示例在普通场景的基础上演示了onScroll接口的用法，此接口可以帮助开发者自动管理视频的播放和停止。
 *
 *
 * 增强型接口onScroll：
 * - NativeMediaADData.onScroll(int position, View view)：
 *      - position：  广告实例在ListView数据集中的位置
 *      - view：      ListView的实例
 *
 *
 * 在ListView这种可以滑动的UI下，请开发者一定要管理好视频的播放和暂停，不要让广告处于屏幕可见范围之外时，还在播放视频，这将对用户体验造成不好的影响，也会影响到SDK内部的数据上报从而影响开发者的收入。
 * 如果开发者不选用onScroll接口，请自己根据MediaView的可见性，调用基础接口来管理视频的播放和停止，但一定保证视频在播放时MediaView是在屏幕中可见的。
 */
public class NativeVideoListDemoActivity extends Activity {

  private static final String TAG = NativeVideoListDemoActivity.class.getSimpleName();
  private ListView mListView;
  private Context mContext;
  private List<NormalItem> mList = new ArrayList<NormalItem>();         // 非广告数据
  private int mScrollState;                                    // ListView的滑动状态
  private CustomAdapter mAdapter;                              // 自定义适配器
  private NativeMediaAD mADManager;                            // 原生广告manager，用于load广告数据
  private AQuery mAQuery;                                      // 第三方框架，用于加载图片，开发者根据需要使用图片加载框架即可
  private List<NativeMediaADData> mADs;                        // 广告对象数组，广点通SDK原生广告可以一次加载多条，但是我们强烈建议开发者不要加载过多的广告而不展示，这样会影响曝光率。
  public static final int MAX_ITEMS = 50;                      // 本示例中加载50条非广告数据
  public static final int AD_COUNT = 1;                        // 本示例中加载1条广告
  public static final int AD_POSITION = 1;                     // 插在ListView数据集的第2个位置

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_gdtnative_video_ad);
    mListView = (ListView) findViewById(R.id.listview);
    initData();
  }

  private void initData() {
    initNativeVideoAD();
    loadAD();
    for (int i = 0; i < MAX_ITEMS; ++i) {
      mList.add(new NormalItem("No." + i + " Normal Data"));
    }
    mContext = getApplicationContext();
    mAQuery = new AQuery(this);
    mAdapter = new CustomAdapter(mList);
    mListView.setAdapter(mAdapter);

    /** 监听滚动事件，调用onScroll接口来自动管理视频的播放和停止 */
    mListView.setOnScrollListener(new OnScrollListener() {
      @Override
      public void onScrollStateChanged(AbsListView view, int scrollState) {
        mScrollState = scrollState;
      }

      @Override
      public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
          int totalItemCount) {
        if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
          return;
        }

        if (mAdapter != null) {
          mAdapter.onScroll();
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
          mADs = ads;
          preLoadVideo();
          addADIntoList();
        }
      }

      @Override
      public void onNoAD(int errorCode) {
        Toast.makeText(getApplicationContext(), "加载失败，错误码：" + errorCode,
            Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onADStatusChanged(NativeMediaADData ad) {
        if (mAdapter != null && ad != null) {
          /**
           * 不要直接调用mAdapter.notifyDataSetChanged()来更新ListView中广告的下载进度，这样会引起视频播放反复重头播放、数据上报出现问题。
           * 开发者应该采用局部刷新ListView数据的方法，首先检查广告item是否在屏幕可见，然后更新这一条Item的子View来刷新下载进度
           */
          mAdapter.updateDownloadingItem(ad);
        }
      }

      /**
       * 广告处理发生错误，当调用一个广告对象的onExposured、onClicked、preLoadVideo接口时，如果发生了错误会回调此接口，具体的错误码含义请参考开发者文档。
       *
       * @param adData      广告对象
       * @param errorCode   错误码，700表示视频加载失败，701表示视频播放时出现错误
       */
      @Override
      public void onADError(NativeMediaADData adData, int errorCode) {
        Log.i(TAG, "onADError: " + errorCode);
      }

      /**
       * 当调用一个广告对象的preLoadVideo接口时，视频素材加载完成后，会回调此接口，在此回调中可以给广告对象绑定MediaView组件播放视频。
       *
       * @param adData 视频素材加载完成的广告对象，很显然这个广告一定是一个带有视频素材的广告，需要给它bindView并播放它
       */
      @Override
      public void onADVideoLoaded(NativeMediaADData adData) {
        Log.i(TAG, adData.getTitle() + " ---> 视频加载完成");
      }

      /**
       * 广告曝光时的回调
       *
       * 注意：带有视频素材的原生广告可以多次曝光 按照曝光计费
       * 没带有视频素材的广告只能曝光一次 按照点击计费
       *
       * @param adData 曝光的广告对象
       */
      @Override
      public void onADExposure(NativeMediaADData adData) {
        Log.i(TAG, adData.getTitle() + " onADExposure");
      }

      /**
       * 广告被点击时的回调
       *
       * @param adData 被点击的广告对象
       */
      @Override
      public void onADClicked(NativeMediaADData adData) {
        Log.i(TAG, adData.getTitle() + " onADClicked");
      }
    };

    mADManager = new NativeMediaAD(getApplicationContext(), Constants.APPID, Constants.NativeVideoPosID, listener);
  }

  /** 预加载广告的视频素材，保证观看体验 */
  private void preLoadVideo() {
    if (mADs != null && !mADs.isEmpty()) {
      for (int i = 0; i < mADs.size(); i++) {
        final NativeMediaADData ad = mADs.get(i);
        if (ad.isVideoAD()) {
          ad.preLoadVideo(); // 加载结果在onADVideoLoaded回调中返回
        }
      }
    }
  }

  /** 把广告对象添加到ListView的数据集中 */
  protected void addADIntoList() {
    if (mADs != null && mAdapter != null && mADs.size() > 0) {
      for (int i = 0; i < mADs.size(); i++) {
        // 强烈建议：多个广告之间的间隔最好大一些，优先保证用户体验！
        // 此外，如果开发者的App的使用场景不是经常被用户滚动浏览多屏的话，没有必要在调用loadAD(int count)时去加载多条，只需要在用户即将进入界面时加载1条广告即可。
        mAdapter.addADToPosition((AD_POSITION + i * 10) % MAX_ITEMS, mADs.get(i));
      }

      mAdapter.notifyDataSetChanged();
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
    Log.i(TAG, "onResume");
    if (mAdapter != null) {
      mAdapter.resumeVideo();
    }
    super.onResume();
  }

  @Override
  protected void onPause() {
    Log.i(TAG, "onPause");
    if (mAdapter != null) {
      mAdapter.stopVideo();
    }
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    if (mAdapter != null) {
      mAdapter.destroyVideo();
    }
    super.onDestroy();
  }


  /**
   * 非广告的数据
   */
  class NormalItem {
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

  /** CustomAdapter适配器，仅供参考 */
  class CustomAdapter extends BaseAdapter {
    static final int TYPE_DATA = 0;
    static final int TYPE_AD = 1;

    static final int TYPE_COUNT = 2;
    private List<Object> mData = new ArrayList<Object>();
    private TreeSet mADSet = new TreeSet();

    public CustomAdapter(List data) {
      mData = data;
    }

    public void addADToPosition(int position, NativeMediaADData ad) {
      if (position >= 0 && position < mData.size()) {
        mData.add(position, ad);
        mADSet.add(position);
      }
    }

    public boolean isAd(int position) {
      return mADSet.contains(position);
    }

    public void onScroll() {
      int first = mListView.getFirstVisiblePosition();
      int last = mListView.getLastVisiblePosition();
      for (int i = first; i <= last; i++) {
        if (isAd(i) && ((NativeMediaADData) mData.get(i)).isVideoAD()
            && ((NativeMediaADData) mData.get(i)).isVideoLoaded()) {
          ((NativeMediaADData) mData.get(i)).onScroll(i, mListView);
        }
      }
    }

    public void resumeVideo() {
      int first = mListView.getFirstVisiblePosition();
      int last = mListView.getLastVisiblePosition();
      for (int i = first; i <= last; i++) {
        if (isAd(i) && ((NativeMediaADData) mData.get(i)).isVideoAD()) {
          ((NativeMediaADData) mData.get(i)).resume();
        }
      }
    }

    public void stopVideo() {
      int first = mListView.getFirstVisiblePosition();
      int last = mListView.getLastVisiblePosition();

      for (int i = first; i <= last; i++) {
        if (isAd(i) && ((NativeMediaADData) mData.get(i)).isVideoAD()) {
          ((NativeMediaADData) mData.get(i)).stop(); // i 为当前这条广告在CustomAdapter数据集中的位置
        }
      }
    }

    public void destroyVideo() {
      if (mADs != null) {
        for (int i = 0; i < mADs.size(); ++i) {
          mADs.get(i).destroy();
        }
      }
    }

    /**
     * 根据下载的App类广告，根据ListView中的该条广告Item的下载进度。
     */
    public void updateDownloadingItem(NativeMediaADData ad) {
      int first = mListView.getFirstVisiblePosition();
      int last = mListView.getLastVisiblePosition();
      for (int i = first; i <= last; i++) {
        if (isAd(i) && ((NativeMediaADData) mData.get(i)).equals(ad)) {
          View view = mListView.getChildAt(i - first);
          if (view.getTag() instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if (!ad.isAPP()) {
              holder.download.setText("浏览");
            } else {
              switch (ad.getAPPStatus()) {
                case 0:
                  holder.download.setText("下载");
                  break;
                case 1:
                  holder.download.setText("启动");
                  break;
                case 2:
                  holder.download.setText("更新");
                  break;
                case 4:
                  holder.download.setText(ad.getProgress() + "%");
                  break;
                case 8:
                  holder.download.setText("安装");
                  break;
                case 16:
                  holder.download.setText("下载失败，重新下载");
                  break;
                default:
                  holder.download.setText("浏览");
                  break;
              }
            }
          }
        }
      }
    }

    @Override
    public int getViewTypeCount() {
      return TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
      if (mADSet.contains(position)) {
        return TYPE_AD;
      }
      return TYPE_DATA;
    }

    @Override
    public int getCount() {
      return mData.size();
    }

    @Override
    public Object getItem(int position) {
      return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
      ViewHolder holder = null;
      int type = getItemViewType(position);
      if (convertView == null || convertView.getTag() == null) {
        holder = new ViewHolder();
        switch (type) {
          case TYPE_DATA:
            convertView = View.inflate(mContext, R.layout.item_data, null);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            break;
          case TYPE_AD:
            convertView = View.inflate(mContext, R.layout.item_ad, null);
            holder.mediaView = (MediaView) convertView.findViewById(R.id.gdt_media_view);
            holder.adInfoContainer = (RelativeLayout) convertView.findViewById(R.id.ad_info);
            holder.logo = (ImageView) convertView.findViewById(R.id.img_logo);
            holder.poster = (ImageView) convertView.findViewById(R.id.img_poster);
            holder.name = (TextView) convertView.findViewById(R.id.text_title);
            holder.desc = (TextView) convertView.findViewById(R.id.text_desc);
            holder.download = (Button) convertView.findViewById(R.id.btn_download);
            holder.play = (Button) convertView.findViewById(R.id.btn_play);
            break;
        }
        convertView.setTag(holder);
      } else {
        holder = (ViewHolder) convertView.getTag();
      }

      // 渲染普通数据
      if (TYPE_DATA == type) {
        holder.title.setText(((NormalItem) mData.get(position)).getTitle());
      }

      // 渲染广告数据
      if (TYPE_AD == type) {
        // 根据position取出广告实例
        final NativeMediaADData ad = (NativeMediaADData) mData.get(position);
        // 渲染广告基本图文素材
        AQuery logoAQ = mAQuery.recycle(convertView);
        logoAQ.id(R.id.img_logo).image(TextUtils.isEmpty(ad.getIconUrl())? ad.getImgUrl() : ad.getIconUrl(), false, true);
        holder.name.setText(ad.getTitle());
        holder.desc.setText(ad.getDesc());

        holder.download.setOnClickListener(new View.OnClickListener() {

          @Override
          public void onClick(View v) {
            ad.onClicked(v); // 设置点击接口
          }
        });
        // 渲染右上角的"下载/浏览"按钮
        if (!ad.isAPP()) {
          holder.download.setText("浏览");
        } else {
          switch (ad.getAPPStatus()) {
            case 0:
              holder.download.setText("下载");
              break;
            case 1:
              holder.download.setText("启动");
              break;
            case 2:
              holder.download.setText("更新");
              break;
            case 4:
              holder.download.setText(ad.getProgress() + "%");
              break;
            case 8:
              holder.download.setText("安装");
              break;
            case 16:
              holder.download.setText("下载失败，重新下载");
              break;
            default:
              holder.download.setText("浏览");
              break;
          }
        }
        logoAQ.id(R.id.img_poster).image((String) ad.getImgUrl(), false, true, 0, 0,
            new BitmapAjaxCallback() {
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
         * 注意：曝光这条广告，此接口必须在onClicked接口调用之前调用，否则onClicked点击接口将无法生效。
         */
        ad.onExposured(holder.adInfoContainer);

        // 先把大图显示出来，后面再判断视频是否加载完成，如果加载完成了再把广告大图隐藏，显示MediaView
        holder.mediaView.setVisibility(View.GONE);
        holder.poster.setVisibility(View.VISIBLE);

        // 检查视频素材是否加载完成，然后对视频区域做渲染。可以做成自动播放或者手动播放
        if (ad.isVideoAD() && ad.isVideoLoaded()) {
          if (ad.isPlaying()) {
            // 这里需要用isPlaying判断一下，如果已经在播放就不要再显示手动播放的按钮了。
            // 例如用户已经在切到全屏观看视频，退出后，ListView的getView方法会重新调用，此时应该显示MediaView。
            holder.mediaView.setVisibility(View.VISIBLE);
            holder.poster.setVisibility(View.GONE);
            holder.play.setVisibility(View.GONE);
          } else {
            // 1、自动播放
            holder.mediaView.setVisibility(View.VISIBLE);
            holder.poster.setVisibility(View.GONE);
            ad.bindView(holder.mediaView, true); // 只有将MediaView和广告实例绑定之后，才能播放视频
            ad.play();
            // end - 自动播放代码
            // 注意：原生视频广告提供setVolumeOn(boolean flag)接口来设置开始播放时是否有声音。一般在消息流中都是静音+自动播放。


            // 2、手动播放
            // holder.play.setVisibility(View.VISIBLE);
            // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) { // 给大图设点透明度
            // holder.poster.setAlpha(0.85f);
            // }
            //
            // final ViewHolder fHolder = holder;
            // holder.play.setOnClickListener(new View.OnClickListener() { //
            // 监听点击，隐藏大图、播放按钮，显示MediaView播放视频
            // @Override
            // public void onClick(View v) {
            // fHolder.mediaView.setVisibility(View.VISIBLE);
            // fHolder.poster.setVisibility(View.GONE);
            // fHolder.play.setVisibility(View.GONE);
            // ad.bindView(fHolder.mediaView, true);
            // ad.play();
            // ad.setVolumeOn(true);
            // }
            // });
            // end - 手动播放代码
            // 注意：原生视频广告提供setVolumeOn(boolean flag)接口来设置开始播放时是否有声音。一般在消息流中都是有声+手动播放。


            // 设置对视频播放信息的监听器
            ad.setMediaListener(new MediaListener() {

              @Override
              public void onVideoReady(long videoDuration) {
                Log.i(TAG, "onVideoReady, video duration = " + videoDuration);
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
                Log.i(TAG, "onVideoError, errorCode = " + errorCode);
              }

              @Override
              public void onReplayButtonClicked() {
                Log.i(TAG, "onReplayButtonClicked");

              }

              /**
               * SDK内置的播放器控制条中的下载/查看详情按钮被点击
               *
               * 注意:这里是指UI中的按钮被点击了，而广告的点击事件回调是在onADClicked中，开发者如需统计点击只需要在onADClicked回调中进行一次统计即可。
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
                  ad.setVolumeOn(true);
                } else {
                  ad.setVolumeOn(false);
                }
              }
            });
          }
        }
      }

      return convertView;
    }

  }

  static class ViewHolder {
    public TextView title;
    public MediaView mediaView;
    public RelativeLayout adInfoContainer;
    public TextView name, desc;
    public ImageView logo, poster;
    public Button download, play;
  }

}