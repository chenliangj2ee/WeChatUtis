package com.android.candy.lock.floatview;

import android.accessibilityservice.AccessibilityService;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.candy.lock.R;
import com.android.candy.lock.act.EditMessageActivity;
import com.android.candy.lock.act.MainTabActivity;
import com.android.candy.lock.act.WCImageActivity;
import com.android.candy.lock.act.WCVideoActivity;
import com.android.candy.lock.act.WeChatLockActivity;
import com.android.candy.lock.utils.MySettings;
import com.android.candy.lock.webview.BrowserActivity;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

import static android.content.Context.POWER_SERVICE;


/**
 * Created by chenliangj2ee on 2017/5/14.
 * 快捷入口启动时展示的开屏
 */

public class WeChat_MainMenu implements View.OnClickListener {

    public int screenHeight;
    public int screenWidth;
    protected View rootView;
    protected Button fx_xiangce;
    protected Button fx_paishe;
    protected Button papashipin;
    protected Button qianghongbao;
    protected Button weixinrewen;
    protected Button tianqi;
    protected Button chasha;
    protected Button autozan;
    protected Button zidongjiahaoyou;
    protected Button xiaoliaobaichu;
    protected Button shiqu;
    protected Button tupianhuancun;
    protected Button shipinhuancun;
    protected Button weixinJiasuo;
    protected Button zidonghuifu;
    protected ImageView weixinJiasuoCheckbox;
    protected ImageView redPacakge;
    protected Button dashang;
    protected ImageView zidonghuifuCheckbox;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private LayoutInflater mLayoutInflater;
    private FrameLayout mFloatView;
    private AccessibilityService con;
    String type;

    public WeChat_MainMenu(AccessibilityService con) {
        this.con = con;
        this.mWindowManager = (WindowManager) con.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        this.screenHeight = mWindowManager.getDefaultDisplay().getHeight();
        this.screenWidth = mWindowManager.getDefaultDisplay().getWidth();
    }

    public void event(AccessibilityService service, AccessibilityEvent event) {
        if (event.getPackageName() == null)
            return;

        AccessibilityNodeInfo nodeInfo = service.getRootInActiveWindow();
        if (nodeInfo == null)
            return;
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
            remove();

    }

    int TabHeight = -1;
    Rect rect;

    public void init() {
        try {
            if (rect == null && con != null) {
                List<AccessibilityNodeInfo> lists = con.getRootInActiveWindow().findAccessibilityNodeInfosByText("通讯录");
                if (lists.isEmpty())
                    return;
                AccessibilityNodeInfo bottominfo = lists.get(0).getParent().getParent().getParent();//.getParent();
                rect = new Rect();
                bottominfo.getBoundsInScreen(rect);
                TabHeight = rect.bottom - rect.top;//微信底部Tab高度
            }


            mWindowManager = (WindowManager) con.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            mLayoutInflater = LayoutInflater.from(con);
            mFloatView = (FrameLayout) mLayoutInflater.inflate(R.layout.float_we_chat_main_menu, null);
            mLayoutParams = new WindowManager.LayoutParams();
            mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            mLayoutParams.format = PixelFormat.RGBA_8888;
            mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            mLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            mLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            mLayoutParams.x = 0;
            mLayoutParams.y = 0;
            initView(mFloatView);
            Log.i("WeChat_MainMenu", "init......");
        } catch (Exception e) {

        }


    }


    boolean add = false;

    public void show() {
        if (add)
            return;
        if (mFloatView == null) {
            init();
        }
        if (mFloatView == null)
            return;


        mWindowManager.addView(mFloatView, mLayoutParams);
        initWeiChatLock();
        RedPackage();
        autoHuiFu();
        add = true;
        ObjectAnimator an = ObjectAnimator.ofFloat(mFloatView, "alpha", 0f, 1f);
        an.setDuration(500);
        an.start();
        Log.i("WeChat_MainMenu", "show......");
    }

    public void remove() {
        if (add == false)
            return;
        try {
            if (mWindowManager != null && mFloatView != null) {
                mWindowManager.removeView(mFloatView);
            }
            add = false;

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("WeChat_MainMenu", "remove......");
    }

    private void log(String s) {
        Log.i(getClass().getSimpleName(), s);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fx_xiangce) {

            Toast.makeText(con, "密聊消息，正在研发，敬请期待", Toast.LENGTH_SHORT).show();

        } else if (view.getId() == R.id.fx_paishe) {
            remove();
            MySettings.setMessageClear(con, true);
            clearMessageAction();
        } else if (view.getId() == R.id.papashipin) {
            remove();
            Intent intent = new Intent(con, BrowserActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("linkUrl", "http://k.360kan.com/?channel_id=0&notad=1&nav=1&dl=vh5_hsp&from=singlemessage");
            intent.putExtra("title", "微视频");
            con.startActivity(intent);
            MobclickAgent.onEvent(con, "click12");
        } else if (view.getId() == R.id.qianghongbao) {
            MySettings.setRedPackage(con, !MySettings.isRedPackage(con));
            if (MySettings.isRedPackage(con)) {
                Toast.makeText(con, "抢红包，仅在手机锁屏不加密情况下生效.", Toast.LENGTH_LONG).show();
                MobclickAgent.onEvent(con, "click13");
            }
            RedPackage();

        } else if (view.getId() == R.id.weixinrewen) {
            //群发
            remove();
            Intent intent = new Intent(con, EditMessageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            MobclickAgent.onEvent(con, "click15");
            con.startActivity(intent);
            MobclickAgent.onEvent(con, "click14");
        } else if (view.getId() == R.id.tianqi) {
            remove();
            Intent intent = new Intent(con, BrowserActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("linkUrl", "http://wx.weather.com.cn/mweather/101010100.shtml");
            intent.putExtra("title", "微天气");
            MobclickAgent.onEvent(con, "click15");
            con.startActivity(intent);
        } else if (view.getId() == R.id.chasha) {

            if(!MySettings.isShareWeChat(con)){
                Toast.makeText(con, "分享好友后，才能使用该功能...", Toast.LENGTH_LONG).show();
                Intent intent=new Intent(con, MainTabActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("from","wechat");
                con.startActivity(intent);
                return ;
            }
            if (MySettings.isCheckFinish(con)) {
                Toast.makeText(con, "当天已检测好友" + MySettings.getCheckUserCount(con) + "人，达到上限，请明日继续", Toast.LENGTH_LONG).show();
            } else {
                MySettings.setDeleteFriend(con, true);
                moreMenu();
                MobclickAgent.onEvent(con, "click16");
            }
            remove();

        } else if (view.getId() == R.id.autozan) {
            remove();
            MySettings.setZan(con, true);
            itemClick("朋友圈");
            MobclickAgent.onEvent(con, "click17");

        } else if (view.getId() == R.id.zidongjiahaoyou) {
            MobclickAgent.onEvent(con, "click18");
            MySettings.setAddFriend(con, true);
            itemClick("附近的人");
        } else if (view.getId() == R.id.xiaoliaobaichu) {
            MobclickAgent.onEvent(con, "click19");
            remove();
            Intent intent = new Intent(con, BrowserActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("linkUrl", "http://joke.oupeng.com/?did=45&channel=meizu1#/channel/cid/3/page/1");
            intent.putExtra("title", "笑料百出");
            con.startActivity(intent);
        } else if (view.getId() == R.id.shiqu) {
            MobclickAgent.onEvent(con, "click20");
            remove();
            WeChat_FloatScreenRecorder floatScreenRecorder = new WeChat_FloatScreenRecorder(con);
            floatScreenRecorder.show();
        } else if (view.getId() == R.id.tupianhuancun) {
            remove();
            MobclickAgent.onEvent(con, "click21");
            Intent intent = new Intent(con, WCImageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            con.startActivity(intent);
        } else if (view.getId() == R.id.shipinhuancun) {
            remove();
            MobclickAgent.onEvent(con, "click22");
            Intent intent = new Intent(con, WCVideoActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            con.startActivity(intent);
        } else if (view.getId() == R.id.weixin_jiasuo) {

            if (MySettings.isWeChatLock(con)) {
                MySettings.setWeChatLock(con, false);
                Toast.makeText(con, "微信锁已取消", Toast.LENGTH_LONG).show();
                initWeiChatLock();
            } else {
                remove();
                MobclickAgent.onEvent(con, "click23");
                Toast.makeText(con, "请设置微信解锁密码", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(con, WeChatLockActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                con.startActivity(intent);
            }

        } else if (view.getId() == R.id.zidonghuifu) {
            MySettings.setAutoHF(con, !MySettings.isAutoHF(con));
            if (MySettings.isAutoHF(con)) {
                MobclickAgent.onEvent(con, "click24");
                Toast.makeText(con, "自动回复，仅在手机锁屏不加密情况下生效.", Toast.LENGTH_LONG).show();
            }
            autoHuiFu();
        } else if (view.getId() == R.id.dashang) {
            remove();
            MobclickAgent.onEvent(con, "click25");
            FloatPrctureCutView view1 = new FloatPrctureCutView(con);
            view1.show();
        }
    }


    /**
     */
    private void itemClick(String text) {
        AccessibilityNodeInfo nodeInfo = con.getRootInActiveWindow();
        List<AccessibilityNodeInfo> lists = nodeInfo.findAccessibilityNodeInfosByText(text);
        if (lists.isEmpty() == false) {
            click(lists.get(0));
        }
    }

    private void click(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo != null && nodeInfo.isClickable()) {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            click(nodeInfo.getParent());
        }

    }

    private void initView(View rootView) {
        fx_xiangce = (Button) rootView.findViewById(R.id.fx_xiangce);
        fx_xiangce.setOnClickListener(WeChat_MainMenu.this);
        fx_paishe = (Button) rootView.findViewById(R.id.fx_paishe);
        fx_paishe.setOnClickListener(WeChat_MainMenu.this);
        papashipin = (Button) rootView.findViewById(R.id.papashipin);
        papashipin.setOnClickListener(WeChat_MainMenu.this);
        qianghongbao = (Button) rootView.findViewById(R.id.qianghongbao);
        qianghongbao.setOnClickListener(WeChat_MainMenu.this);
        dashang = (Button) rootView.findViewById(R.id.dashang);
        dashang.setOnClickListener(WeChat_MainMenu.this);


        weixinrewen = (Button) rootView.findViewById(R.id.weixinrewen);
        weixinrewen.setOnClickListener(WeChat_MainMenu.this);
        tianqi = (Button) rootView.findViewById(R.id.tianqi);
        tianqi.setOnClickListener(WeChat_MainMenu.this);


        chasha = (Button) rootView.findViewById(R.id.chasha);
        chasha.setOnClickListener(WeChat_MainMenu.this);
        autozan = (Button) rootView.findViewById(R.id.autozan);
        autozan.setOnClickListener(WeChat_MainMenu.this);
        zidongjiahaoyou = (Button) rootView.findViewById(R.id.zidongjiahaoyou);
        zidongjiahaoyou.setOnClickListener(WeChat_MainMenu.this);

        xiaoliaobaichu = (Button) rootView.findViewById(R.id.xiaoliaobaichu);
        xiaoliaobaichu.setOnClickListener(WeChat_MainMenu.this);
        shiqu = (Button) rootView.findViewById(R.id.shiqu);
        shiqu.setOnClickListener(WeChat_MainMenu.this);

        tupianhuancun = (Button) rootView.findViewById(R.id.tupianhuancun);
        tupianhuancun.setOnClickListener(WeChat_MainMenu.this);
        shipinhuancun = (Button) rootView.findViewById(R.id.shipinhuancun);
        shipinhuancun.setOnClickListener(WeChat_MainMenu.this);
        weixinJiasuo = (Button) rootView.findViewById(R.id.weixin_jiasuo);
        weixinJiasuo.setOnClickListener(WeChat_MainMenu.this);
        zidonghuifu = (Button) rootView.findViewById(R.id.zidonghuifu);
        zidonghuifu.setOnClickListener(WeChat_MainMenu.this);


        setTabWH(fx_xiangce);
        setTabWH(fx_paishe);
        setTabWH(dashang);

        setTabWH(tianqi);

        setTabWH(chasha);
        setTabWH(autozan);
        setTabWH(tupianhuancun);
        setTabWH(shipinhuancun);

        setTabWH(qianghongbao);
        setTabWH(zidongjiahaoyou);
        setTabWH(weixinJiasuo);
        setTabWH(zidonghuifu);


        setTabWH(papashipin);
        setTabWH(xiaoliaobaichu);
        setTabWH(weixinrewen);
        setTabWH(shiqu);
        weixinJiasuoCheckbox = (ImageView) rootView.findViewById(R.id.weixin_jiasuo_checkbox);
        redPacakge = (ImageView) rootView.findViewById(R.id.red_pacakge);

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.i("WeChat_FaXian_Menu", "onclick........");
                remove();
                return false;
            }
        });
        zidonghuifuCheckbox = (ImageView) rootView.findViewById(R.id.zidonghuifu_checkbox);


    }

    public void setTabWH(Button button) {
        Drawable[] drawables = button.getCompoundDrawables();
        drawables[1].setBounds(0, 0, 50, 50);
        button.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
    }


    private void initWeiChatLock() {
        if (MySettings.isWeChatLock(con)) {
            weixinJiasuoCheckbox.setBackgroundResource(R.drawable.fx_duihao_s);
        } else {
            weixinJiasuoCheckbox.setBackgroundResource(R.drawable.fx_duihao_d);
        }
    }

    private void RedPackage() {
        if (MySettings.isRedPackage(con)) {
            redPacakge.setBackgroundResource(R.drawable.fx_duihao_s);
        } else {
            redPacakge.setBackgroundResource(R.drawable.fx_duihao_d);
        }
    }

    PowerManager.WakeLock wakeLock;

    private void autoHuiFu() {
        if (MySettings.isAutoHF(con)) {
            zidonghuifuCheckbox.setBackgroundResource(R.drawable.fx_duihao_s);
            if (wakeLock == null)
                wakeLock = ((PowerManager) con.getSystemService(POWER_SERVICE))
                        .newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                                | PowerManager.ON_AFTER_RELEASE, "qu");
            wakeLock.setReferenceCounted(false);
            wakeLock.acquire();
        } else {
            zidonghuifuCheckbox.setBackgroundResource(R.drawable.fx_duihao_d);
            if (wakeLock != null)
                wakeLock.release();
        }
    }

    /**
     * 如果当前是自动检测删除好友，则自动点击右上角菜单
     */
    public void moreMenu() {
        if (MySettings.isDeleteFriend(con) == false)
            return;
        AccessibilityNodeInfo nodeInfo = con.getRootInActiveWindow();
        if (nodeInfo == null)
            return;
        List<AccessibilityNodeInfo> menus = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/fr");//获得左上角【+】按钮
        if (menus == null || menus.isEmpty())
            return;

        click(menus.get(0));
    }

    /**
     * 如果当前是自动检测删除好友，则自动点击右上角菜单
     */
    private void clearMessageAction() {
        AccessibilityNodeInfo nodeInfo = con.getRootInActiveWindow();
        List<AccessibilityNodeInfo> listviews = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bqc");
        if (listviews != null && listviews.size() > 0)
            listviews.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
    }

}
