package com.lgx.accessbilitytest;

import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class MyAccessBilityService extends AccessibilityService{
	public static boolean sIsCanUse;
	private static SharedPreferences sp;
	
    private long minTime = 0;   
    private Rect mHongRect, mHasRect;

	@SuppressLint("NewApi")
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (sp == null) {
			sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			sIsCanUse = sp.getBoolean(MainFragment.ISCANUSE, true);
		}
		if (sIsCanUse == false)
			return;

		if (event.getText() != null && event.getSource() != null) {
			AccessibilityNodeInfo rootInfo = getRootInActiveWindow();
			List<AccessibilityNodeInfo> eventList;
			try {
				if (event.getPackageName().equals("com.tencent.mobileqq")) {
					// QQ抢红包
					eventList = rootInfo.findAccessibilityNodeInfosByText("点击拆开");
					//是否有点击拆开字样
					if (eventList.size() > 0) {
						//寻找是不是有红包
						AccessibilityNodeInfo clickRoot = getRootInActiveWindow();
						List<AccessibilityNodeInfo> clickList = clickRoot.findAccessibilityNodeInfosByText("，点击查看详情");
						for(int i = clickList.size() - 1; i >= 0; --i) {
							//从后往前点
							if(clickList.get(i).isClickable()) {
								clickList.get(i).performAction(AccessibilityNodeInfo.ACTION_CLICK);
								break;
							}
						}
					}
				} else {
					if("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(event.getClassName())) {						
			            //点中了红包，下一步就是去拆红包
						chaiHongbao();
			        } else if(System.currentTimeMillis() - minTime > 100){
			        	//不稳定，暂所有都判断
			        	minTime = System.currentTimeMillis();
			        	dianHongBao();
			        }
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	
	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		new Handler().postDelayed(new Runnable() {
			
			@SuppressLint("NewApi") @Override
			public void run() {
				performGlobalAction(GLOBAL_ACTION_BACK);
			}
		}, 500);
		new Handler().postDelayed(new Runnable() {
			
			@SuppressLint("NewApi") @Override
			public void run() {
				performGlobalAction(GLOBAL_ACTION_BACK);
			}
		}, 1000);
	}

	@Override
	public void onInterrupt() {
		
	}
	
	//点击红包
	@SuppressLint("NewApi") 
	private void dianHongBao() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if(nodeInfo == null) {
            return;
        }

        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("领取红包");
        if(list != null) {
            //从后往前点
            for(int i = list.size() - 1; i >= 0; --i) {
                AccessibilityNodeInfo parent = list.get(i).getParent();
                if(parent != null) {
                	if(isShouldClick(nodeInfo, list.get(i))) {
                		parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                	}                	
                    break;
                }
            }
        }
    }
	
	//拆红包
	@SuppressLint("NewApi")
	private void chaiHongbao() {
		List<AccessibilityNodeInfo> list = getChaiList();
		AccessibilityNodeInfo targetNode = null;
		
		//获取拆红包按钮targetNode
		if (list != null && !list.isEmpty()) {
			targetNode = list.get(0);
		} 
		
		//点击按钮
		if (targetNode != null) {
			final AccessibilityNodeInfo n = targetNode;
			n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
		}
	}
	
	//是否应该点击，通过eventInfo.getBoundsInScreen已领取字样是否在红包下面
    private boolean isShouldClick(AccessibilityNodeInfo rootInfo, AccessibilityNodeInfo hongInfo) {    	
		List<AccessibilityNodeInfo> list = rootInfo.findAccessibilityNodeInfosByText("你领取了");
		if(mHasRect == null || mHongRect == null) {
			mHasRect = new Rect();
			mHongRect = new Rect();
		}

		hongInfo.getBoundsInScreen(mHongRect);
		for (AccessibilityNodeInfo n : list) {
			n.getBoundsInScreen(mHasRect);
			if(mHasRect.top > mHongRect.top) {
				return false;
			}
		}		
		return true;
    }
    
    //获取拆红包页面的List
    @SuppressLint("NewApi") 
    private List<AccessibilityNodeInfo> getChaiList() {
    	AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		
		List<AccessibilityNodeInfo> list = null;
		//第一种，通过"拆红包"获取list
		list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b43");		
		if (list == null || list.isEmpty()) {
			list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b2c");
			if(list == null || list.isEmpty()) {
				list = nodeInfo.findAccessibilityNodeInfosByText("拆红包");				
			}	
		}		
		return list;
    }
}
