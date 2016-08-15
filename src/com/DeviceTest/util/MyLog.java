package com.DeviceTest.util;

import android.util.Log;

public class MyLog {

	private static boolean isDebug = true;
	private static final String TAG = "AutoTest";

	public static void e(String log) {
		if (isDebug)
			Log.e(TAG, log);
	}

	public static void v(String log) {
		if (isDebug)
			Log.v(TAG, log);
	}

	public static void d(String log) {
		if (isDebug)
			Log.d(TAG, log);
	}

	public static void i(String log) {
		if (isDebug)
			Log.i(TAG, log);
	}

	public static void w(String log) {
		if (isDebug)
			Log.w(TAG, log);
	}

}
