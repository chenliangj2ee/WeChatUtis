package com.android.candy.lock.action;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.android.candy.lock.base.BaseAction;
import com.android.candy.lock.floatview.CheckFinishDialog;
import com.android.candy.lock.floatview.CheckingFriendFloatView;
import com.android.candy.lock.utils.MySettings;

import java.util.List;

/**
 * Created by chenliangj2ee on 2017/5/17.
 * 微信好友删除检测
 */

public class WeChat_FirendsCheckDelete extends BaseAction {
    public static int dayCreateCount = 20;

    private int index = 0;
    private String userNames = "";
    private String checkNames = "";
    private int page;
    private int forwardPage;
    public static boolean checkFinish = false;//检测是否结束
    public static String checkUserName = "";
    public static int totleUserNumber;


    private CheckingFriendFloatView checking;
    private int preSeletePage;//本次建群翻了几页

    public WeChat_FirendsCheckDelete(AccessibilityService service) {
        super(service);
        index = 0;
        page = MySettings.getCreateQunPageNum(service);//获取上次查询检测到的页数。
        checking = new CheckingFriendFloatView(service);
        checking.show();
    }

    public String getLauncherPackageName(Context context) {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);
        if (res.activityInfo == null) {
            return "";
        }
        //如果是不同桌面主题，可能会出现某些问题，这部分暂未处理
        if (res.activityInfo.packageName.equals("android")) {
            return "";
        } else {
            return res.activityInfo.packageName;
        }


    }

    private String lastUserName = "-1";

    @Override
    public void event(AccessibilityEvent event) {
        try {
            int eventType = event.getEventType();

            final AccessibilityNodeInfo nodeInfo = service.getRootInActiveWindow();

            if (nodeInfo == null) {
                return;
            }

            if (getLauncherPackageName(service).equals(event.getPackageName().toString())) {
                MySettings.setDeleteFriend(service, false);
                checking.remove();
            }
            log("index：" + index);


            List<AccessibilityNodeInfo> error = nodeInfo.findAccessibilityNodeInfosByText("操作太频繁，请稍后再试");
            if (error != null && error.size() > 0) {
                checkFinish = true;
                service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                Thread.sleep(1000);
                service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                finish();
                MySettings.setCheckFinish(service, true);
                return;
            }

            if (index == 0) {
                log("发起群聊1............................................" + index);
                List<AccessibilityNodeInfo> items = nodeInfo.findAccessibilityNodeInfosByText("发起群聊");
                if (items == null || items.isEmpty())
                    return;
                click(items.get(0));
                index = 1;
                log("发起群聊2............................................" + index);

            }


            if (index == 1) {
                List<AccessibilityNodeInfo> listviews = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/f_");//获取listview
                if (listviews == null || listviews.isEmpty())
                    return;
                log("forwardPage：" + forwardPage + "    page:" + page);
                if (forwardPage < page) {
                    listviews.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                    forwardPage++;
                } else {
                    index = 2;
                }

                log("下一页............................................" + index);
            }


            if (index == 2) {
                if (preSeletePage < 3) {
                    List<AccessibilityNodeInfo> checkboxs = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/pc");//获取ChcekBox
                    List<AccessibilityNodeInfo> names = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/jr");//获取用户名称
                    String username = "";
                    for (int i = 0; i < checkboxs.size(); i++) {


                        if (i < names.size()) {
                            if (names.get(i) != null && names.get(i).getText() != null) {
                                username += names.get(i).getText().toString()+"、";

                            }
                        }


                        if (!checkboxs.get(i).isChecked()) {
                            click(checkboxs.get(i));
                            WeChat_FirendsCheckDelete.totleUserNumber++;//记录已经检测了多少人
                            if (checkNames.contains(username) == false) {
                                checkNames = checkNames + username;
                                MySettings.setCheckUserCount(service, MySettings.getCheckUserCount(service) + 1);
                                log("当天检测好友人数：" + MySettings.getCheckUserCount(service));
                            }


                        }
                    }

                    checking.checking(username);
                    List<AccessibilityNodeInfo> listviews = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/f_");//获取listview
                    if (preSeletePage < 3) {

                        if (listviews != null && listviews.size() > 0) {
                            listviews.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                            page++;
                            Log.i("worini", "当前页:" + page);
                        }
                    } else {
                        index = 3;
                    }
                    preSeletePage++;

                    if (listviews != null && listviews.size() > 0 && "android.widget.ListView".equals(event.getClassName().toString())) {

                        int itemCount = event.getItemCount();
                        int toIndex = event.getToIndex();
                        if (toIndex == itemCount - 1) {//当toIndex等于listview的itemCount值时，则判定为滑动到listview最下面了，检测结束
                            checkFinish = true;
                            Toast.makeText(service, "检测结束...", Toast.LENGTH_SHORT).show();
//                            finish();//测试代码
                            index = 3;
                            page = 0;
                        }

                    }


                } else {
                    index = 3;
                }
                log("preSeletePage:" + preSeletePage);
            }

            if (index == 3) {

//                //测试代码
//                preSeletePage = 0;
//                forwardPage = 0;
//                service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
//                index=0;
//                //测试代码

                List<AccessibilityNodeInfo> buttons = nodeInfo.findAccessibilityNodeInfosByText("确定");//获得消【确定】
                preSeletePage = 0;
                forwardPage = 0;
                if (buttons.isEmpty() == false) {
                    log("群聊开始创建............................................");
                    buttons.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    preSeletePage = 0;
                    index = 4;

                }

            }


            if (index == 4) {
                Thread.sleep(1000);
                List<AccessibilityNodeInfo> messages = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/iq");//获得消息编辑框
                for (int i = 0; i < messages.size(); i++) {
                    AccessibilityNodeInfo info = messages.get(i);
                    info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    if (info.getText() != null) {
                        String text = info.getText().toString();
                        if (text.contains("你无法邀请未添加你为好友的用户进去群聊")) {
                            text = text.replace("你无法邀请未添加你为好友的用户进去群聊，请先向", "");
                            text = text.replace("发送朋友验证申请。对方通过验证后，才能加入群聊。", "");
                            log("你被以下好友删除：" + text);
                            text = text.replace("[", "").replace("]", "");
                            if (userNames.contains(text) == false) {
                                userNames = userNames + text + "&";
                            }


                        }
                    }
                }

                if (messages.size() > 0) {
                    index = 5;
                }

            }

            if (index == 5) {
                AccessibilityNodeInfo info = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ge").get(0).getChild(1).getChild(0);

                if (info != null) {
                    info.performAction(AccessibilityNodeInfo.ACTION_CLICK);//进入群聊详情
                    index = 6;
                }

            }

            if (index == 6) {
                List<AccessibilityNodeInfo> messages = nodeInfo.findAccessibilityNodeInfosByViewId("android:id/list");//获得聊天信息ListView

                if (messages != null && messages.size() > 0) {
                    messages.get(0).performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                    log("进入群聊详情向下滑动");
                    index = 7;
                }


            }


            if (index == 7) {
                List<AccessibilityNodeInfo> deletes = nodeInfo.findAccessibilityNodeInfosByText("删除并退出");//获得消息编辑框
                if (deletes != null && deletes.size() > 0) {
                    for (int i = 0; i < deletes.size(); i++) {
                        click(deletes.get(i));
                        index = 8;
                        log("删除并且退出。。。");
                    }
                } else {
                    index = 6;
                }


            }


            if (index == 8) {

                if (event.getText() == null)
                    return;
                if (event.getText().toString().contains("删除并退出后，将不再接收此群聊信息, 取消, 确定")) {
                    List<AccessibilityNodeInfo> ok = nodeInfo.findAccessibilityNodeInfosByText("确定"); //获得 确定

                    if (ok != null && ok.size() > 0) {
                        ok.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        finish();
                        log("确定退出群。。。");
                        index = 0;
                    }

                } else {
                    List<AccessibilityNodeInfo> delete = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/apg");//获得离开群聊对应的listview
                    if (delete.isEmpty() == false) {
                        AccessibilityNodeInfo likai = delete.get(0).getChild(0);
                        if (likai != null) {
                            likai.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            finish();
                            log("离开群聊。。。");
                            log("删除我的孙子列表：" + userNames);
                            index = 0;
                        }
                    }


                }


            }

        } catch (Exception e) {
            MySettings.setDeleteFriend(service, false);
            e.printStackTrace();
            index = 0;
        }
    }


    private void click(AccessibilityNodeInfo info) {
        Log.i("FirendsCheckDelete", "click。。。。。");
        if (info == null)
            return;
        if (info != null && info.isClickable()) {
            info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            click(info.getParent());
        }
    }

    private void finish() {
        if (checkFinish) {
            MySettings.setDeleteFriend(service, false);
            index = 0;
            checkFinish = false;
            checking.remove();
            if (userNames == null || "".equals(userNames)) {
                Toast.makeText(service, "检测结束,没有发现死人", Toast.LENGTH_SHORT).show();
            } else {
                CheckFinishDialog dialog = new CheckFinishDialog(service, userNames);
                dialog.show();
                WeChat_DeleteFirends.usernames = userNames;
                userNames = "";
                Toast.makeText(service, "检测结束...", Toast.LENGTH_SHORT).show();
            }

            MySettings.setCreateQunPageNum(service, 0);//当检测到底部时，记录页数，下次检测，从第一次检测，否则从上次记录的页数开始检测。
            checkNames = "";

            MySettings.setDeleteFriend(service, false);
            MySettings.setCreateQunPageNum(service, page);
        }

    }


}
