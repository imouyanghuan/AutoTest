package com.tchip.autotest;

import android.app.Application;
import android.util.Log;

public class DeviceTestApplication extends Application {
	public static Prefs myPrefs = null;
	// public static final String[]
	// myItems={"版本","屏幕","触屏","摄像头","震动","光感","距离传感器","重力感应","亮度","蓝牙","无线","电池","键盘","SD 卡","存储","喇叭","录音","耳机录音","听筒","收音机","SIM卡状态","U 盘","HDMI","GPS定位"};//注意次序
	public static final String[] myItems = { "版本", "屏幕", "触屏", "重力感应", "亮度",
			"蓝牙", "无线", "FM发射", "SD 卡", "喇叭", "录音", "GPS定位", "摄像头" };// 注意次序

	@Override
	public void onCreate() {
		super.onCreate();
		myPrefs = new Prefs(this);
		Boolean isFirst = myPrefs.getBoolean("my_first_in", true);
		if (isFirst) {
			Log.v("zxx", ">>>>>>>>>>>first");
			for (int i = 0; i < myItems.length; i++) {
				myPrefs.setString("my" + i, "NOTEST");
			}
			myPrefs.setBoolean("my_first_in", false);
		}
	}

	public static Prefs getPrefs() {
		return myPrefs;
	}

}
