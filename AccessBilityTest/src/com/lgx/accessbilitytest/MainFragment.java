package com.lgx.accessbilitytest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

public class MainFragment extends Fragment{
	public static final String ISCANUSE = "com.lgx.iscanuse";
	private Switch mSwitch;
	private SharedPreferences mSp;
	@SuppressWarnings("unused")
	private TextView mWranText;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main, container, false);
		mSwitch = (Switch) view.findViewById(R.id.mainSwitch);
		mWranText = (TextView) view.findViewById(R.id.main_notRun);
		
		mSp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mSwitch.setChecked(mSp.getBoolean(ISCANUSE, true));
		mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				Editor editor = mSp.edit();
				editor.putBoolean(ISCANUSE, arg1);
				editor.commit();
				MyAccessBilityService.sIsCanUse = arg1;
			}
		});
		
		//判断是否开启辅助功能 
		if(!isAccessibilitySettingsOn(getActivity())) {
			new AlertDialog.Builder(getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
						.setTitle("提示")
						.setCancelable(false)
						.setMessage("请将该应用的辅助功能打开，以实现自动抢红包功能")
						.setNegativeButton("取消", null)
						.setPositiveButton("去开启", new OnClickListener() {
							
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
								getActivity().startActivity(intent);
							}
						}).show();					
		}
		
		return view;
	}
	
	
	private boolean isAccessibilitySettingsOn(Context mContext) {
		int accessibilityEnabled = 0;
		final String service = getActivity().getPackageName()
				+ "/com.lgx.accessbilitytest.MyAccessBilityService";
		boolean accessibilityFound = false;
		try {
			accessibilityEnabled = Settings.Secure.getInt(mContext
					.getApplicationContext().getContentResolver(),
					android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(
				':');
		if (accessibilityEnabled == 1) {
			String settingValue = Settings.Secure.getString(mContext
					.getApplicationContext().getContentResolver(),
					Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
			if (settingValue != null) {
				TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
				splitter.setString(settingValue);
				while (splitter.hasNext()) {
					String accessabilityService = splitter.next();
					if (accessabilityService.equalsIgnoreCase(service)) {
						return true;
					}
				}
			}
		}
		return accessibilityFound;
	}
}
