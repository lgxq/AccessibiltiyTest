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
					// QQ�����
					eventList = rootInfo.findAccessibilityNodeInfosByText("�����");
					//�Ƿ��е��������
					if (eventList.size() > 0) {
						//Ѱ���ǲ����к��
						AccessibilityNodeInfo clickRoot = getRootInActiveWindow();
						List<AccessibilityNodeInfo> clickList = clickRoot.findAccessibilityNodeInfosByText("������鿴����");
						for(int i = clickList.size() - 1; i >= 0; --i) {
							//�Ӻ���ǰ��
							if(clickList.get(i).isClickable()) {
								clickList.get(i).performAction(AccessibilityNodeInfo.ACTION_CLICK);
								break;
							}
						}
					}
				} else {
					if("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(event.getClassName())) {						
			            //�����˺������һ������ȥ����
						chaiHongbao();
			        } else if(System.currentTimeMillis() - minTime > 100){
			        	//���ȶ��������ж��ж�
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
	
	//������
	@SuppressLint("NewApi") 
	private void dianHongBao() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if(nodeInfo == null) {
            return;
        }

        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("��ȡ���");
        if(list != null) {
            //�Ӻ���ǰ��
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
	
	//����
	@SuppressLint("NewApi")
	private void chaiHongbao() {
		List<AccessibilityNodeInfo> list = getChaiList();
		AccessibilityNodeInfo targetNode = null;
		
		//��ȡ������ťtargetNode
		if (list != null && !list.isEmpty()) {
			targetNode = list.get(0);
		} 
		
		//�����ť
		if (targetNode != null) {
			final AccessibilityNodeInfo n = targetNode;
			n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
		}
	}
	
	//�Ƿ�Ӧ�õ����ͨ��eventInfo.getBoundsInScreen����ȡ�����Ƿ��ں������
    private boolean isShouldClick(AccessibilityNodeInfo rootInfo, AccessibilityNodeInfo hongInfo) {    	
		List<AccessibilityNodeInfo> list = rootInfo.findAccessibilityNodeInfosByText("����ȡ��");
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
    
    //��ȡ����ҳ���List
    @SuppressLint("NewApi") 
    private List<AccessibilityNodeInfo> getChaiList() {
    	AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
		
		List<AccessibilityNodeInfo> list = null;
		//��һ�֣�ͨ��"����"��ȡlist
		list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b43");		
		if (list == null || list.isEmpty()) {
			list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b2c");
			if(list == null || list.isEmpty()) {
				list = nodeInfo.findAccessibilityNodeInfosByText("����");				
			}	
		}		
		return list;
    }
}
