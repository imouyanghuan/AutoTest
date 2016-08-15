package com.tchip.autotest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.mediatek.xlog.Xlog;

public class YgpsService extends Service {

	private static final String TAG = "EM/YGPS_Service";
	protected static final String SERVICE_START_ACTION = "com.mediatek.ygps.YgpsService";

	@Override
	public void onCreate() {
		Xlog.v(TAG, "YGPSService onCreate");
		// sSelf = this;
		// mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

	}

	@Override
	public void onDestroy() {
		Xlog.v(TAG, "YGPSService onDestroy");

		// mNM.cancel(R.string.mobilelog_service_start);
		// mNM.cancelAll();
		// sSelf = null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Xlog.v(TAG, "onStartCommand " + intent + " flags " + flags);

		if (intent == null
				|| (!intent.getAction().equals(SERVICE_START_ACTION))) {
			Xlog.w(TAG, "intent null error: " + intent);
			// mNM.cancelAll();
			return START_STICKY;
		}

		// mNM.cancelAll();

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
