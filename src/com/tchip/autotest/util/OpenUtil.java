package com.tchip.autotest.util;

import java.lang.reflect.Method;
import java.util.List;

import android.app.ActivityManager;
import android.content.Context;

public class OpenUtil {

	public static void killApp(Context context, String app) {
		ActivityManager myActivityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> mRunningPros = myActivityManager
				.getRunningAppProcesses();
		for (ActivityManager.RunningAppProcessInfo amPro : mRunningPros) {
			if (amPro.processName.contains(app)) {
				try {
					Method forceStopPackage = myActivityManager
							.getClass()
							.getDeclaredMethod("forceStopPackage", String.class);
					forceStopPackage.setAccessible(true);
					forceStopPackage.invoke(myActivityManager,
							amPro.processName);
					MyLog.v("Kill App Success:" + app);
				} catch (Exception e) {
					MyLog.v("Kill App Fail:" + app);
					e.printStackTrace();
				}
			}
		}
	}

	public static void killApp(Context context, String[] app) {
		ActivityManager myActivityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> mRunningPros = myActivityManager
				.getRunningAppProcesses();
		for (ActivityManager.RunningAppProcessInfo amPro : mRunningPros) {
			for (String strApp : app) {
				if (amPro.processName.contains(strApp)) {
					try {
						Method forceStopPackage = myActivityManager.getClass()
								.getDeclaredMethod("forceStopPackage",
										String.class);
						forceStopPackage.setAccessible(true);
						forceStopPackage.invoke(myActivityManager,
								amPro.processName);
					} catch (Exception e) {
					}
				}
			}
		}
	}

}
