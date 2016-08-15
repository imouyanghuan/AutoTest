package com.tchip.autotest;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;

import android.util.Log;

/**
 * 广播接收器：系统首次启动执行
 */
public class SecretCodeReceiver extends BroadcastReceiver {

	public ContentResolver mContentResolver = null;
	public Context mContext;
	public static final String SECRET_CODE_ACTION = "android.provider.Telephony.SECRET_CODE";

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		mContentResolver = context.getContentResolver();
		// mLockPatternUtils = new LockPatternUtils(mContext);
		String action = intent.getAction();

		Log.e("", "SecretCodeReceiver ---------------> action : " + action);
		if (intent.getAction().equals(SECRET_CODE_ACTION)) {

			Intent i = new Intent(Intent.ACTION_MAIN);
			i.setClass(context, DeviceTest.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);

		}
	}

}
