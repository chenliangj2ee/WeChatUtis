package com.android.candy.lock.action;

import android.accessibilityservice.AccessibilityService;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.android.candy.lock.base.BaseAction;
import com.android.candy.lock.utils.MySettings;

import java.util.List;

/**
 * Created by chenliangj2ee on 2017/5/17.
 */

public class WeChat_AutoAddFriends extends BaseAction {

    private int index = 0;
    private int page;
    private int zanIndex;//当前页第几个赞
    private int totalCount;
    private int preCount=1;//当前页itemCount;

    private String preText = "";//记录上次点击item时的text;


    public WeChat_AutoAddFriends(AccessibilityService service) {
        super(service);
        index = 0;
    }


    @Override
    public void event(AccessibilityEvent event) {
        try {
            if (MySettings.issetAddFriend(service) == false) {
                index=0;
                return;
            }
            String packageName = event.getPackageName() == null ? "" : event.getPackageName().toString();

            if (!"com.tencent.mm".equals(packageName)) {
                return;
            }
            int eventType = event.getEventType();

            final AccessibilityNodeInfo nodeInfo = service.getRootInActiveWindow();

            if (nodeInfo == null) {
                return;
            }


            if (index == 0) {//item点击进入用户详情
                log("当前步骤：" + index);
                List<AccessibilityNodeInfo> listviews = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/c29");//获得附近人ListView
                if (listviews == null || listviews.isEmpty()||listviews.size()<=0) {
                    log("listviews is null" + index+event.getEventType());
                    return;
                }
                AccessibilityNodeInfo item = listviews.get(0).getChild(preCount);//获得ListView的第count个item
                if (item != null && item.isClickable()) {

                    if(totalCount<100){
                        log("item 点击了。。。。。");
                        preCount++;
                        totalCount++;
                        index = 1;
                        item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }else{
                        MySettings.setAddFriend(service,false);
                        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                        Toast.makeText(service, "添加结束", Toast.LENGTH_SHORT).show();
                    }


                }
            }

            if (index == 1) {//获取【打招呼】按钮
                log("当前步骤：" + index);

                List<AccessibilityNodeInfo> sends = nodeInfo.findAccessibilityNodeInfosByText("发消息");//获得【打招呼】按钮
                if (sends != null && !sends.isEmpty()) {
                    service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    index = 5;
                    return;
                }

                List<AccessibilityNodeInfo> dazhaohus = nodeInfo.findAccessibilityNodeInfosByText("打招呼");//获得【打招呼】按钮
                if (dazhaohus != null && !dazhaohus.isEmpty()) {
                    dazhaohus.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    index = 2;
                }
            }

            if (index == 2) {//获得输入编辑框Edittext
                log("当前步骤：" + index);
                List<AccessibilityNodeInfo> edits = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/chc"); //获得输入编辑框
                if (edits != null && !edits.isEmpty()) {
                    ClipboardManager clipboard = (ClipboardManager) service.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("text", "你好，简单认识下吧....");
                    clipboard.setPrimaryClip(clip);
                    edits.get(0).performAction(AccessibilityNodeInfo.ACTION_PASTE);
                    index = 3;
                }

            }


            if (index == 3) {////获得【发送】按钮
                log("当前步骤：" + index);
                List<AccessibilityNodeInfo> sends = nodeInfo.findAccessibilityNodeInfosByText("发送"); //获得【发送】按钮
                if (sends != null && !sends.isEmpty()) {
                    sends.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    index = 4;
                }

            }

            if (index == 4) {////获得【发送】按钮
                log("当前步骤：" + index);
                List<AccessibilityNodeInfo> xiangqingziliao = nodeInfo.findAccessibilityNodeInfosByText("详细资料"); //获得详情页【返回】按钮
                if (xiangqingziliao != null && !xiangqingziliao.isEmpty()) {
                    service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    index = 5;
                }

            }

            if (index == 5) {//获得附近人ListView,判断是否进入下一页
                log("当前步骤：" + index);
                List<AccessibilityNodeInfo> listviews = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/c29");//获得附近人ListView
                if (listviews != null && !listviews.isEmpty()) {
                    if (preCount>= listviews.get(0).getChildCount()) {
                        listviews.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                        preCount=0;
                    }
                    index = 0;
                }
            }


        } catch (Exception e) {

            index = 0;
        }

    }
}
