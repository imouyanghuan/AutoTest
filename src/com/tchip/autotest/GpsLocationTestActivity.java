package com.tchip.autotest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.tchip.autotest.R;
import com.tchip.autotest.helper.ControlButtonUtil;
import com.tchip.autotest.helper.SystemUtil;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class GpsLocationTestActivity extends Activity {
	private static final String TAG = GpsLocationTestActivity.class
			.getSimpleName();

	public Context mContext;
	private List<GpsSatellite> numSatelliteList = new ArrayList<GpsSatellite>(); // 卫星信号

	LocationManager mLocatManager;
	LocationListener mLocationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			updateToNewLocation(location);
			Toast.makeText(mContext, ">>>>>> mLocationListener",
					Toast.LENGTH_LONG).show();
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};
	TextView mResult;
	TextView mText;
	TextView mTitle;

	private GpsStatus.Listener statusListener = new MystatusListener() {
		public void onGpsStatusChanged(int event) { // GPS状态变化时的回调，如卫星数
			GpsStatus status = mLocatManager.getGpsStatus(null); // 取当前状态
			updateGpsStatus(event, status);
		}
	};

	private void updateGpsStatus(int event, GpsStatus status) {
		if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
			int maxSatellites = status.getMaxSatellites();
			Iterator<GpsSatellite> it = status.getSatellites().iterator();
			numSatelliteList.clear();
			int count = 0;
			while (it.hasNext() && count <= maxSatellites) {
				GpsSatellite s = it.next();
				numSatelliteList.add(s);
				count++;
			}
		}
	}

	private static final int step = 1000; // msecs
	private static final int MSG_RUN = 0;

	boolean stop = false;
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			removeMessages(MSG_RUN);
			switch (msg.what) {
			case MSG_RUN:
				((Runnable) msg.obj).run();
				break;
			default:
				break;
			}
		}
	};
	Runnable mFailedRunnable = new Runnable() {

		public void run() {
			if (stop) {
				return;
			}
			findViewById(R.id.btn_Fail).performClick();
		}
	};
	Runnable mSkipRunnable = new Runnable() {

		public void run() {
			if (stop) {
				return;
			}
			findViewById(R.id.btn_Skip).performClick();
		}
	};

	Runnable mPassRunnable = new Runnable() {

		public void run() {
			if (stop) {
				return;
			}
			findViewById(R.id.btn_Pass).performClick();
		}
	};

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		DeviceTest.lockScreenOrientation(this);
		mContext = this;
		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
				+ ")");
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.gpslocationtest);

		this.mResult = (TextView) findViewById(R.id.gpslocationresultText);
		this.mResult.setVisibility(View.VISIBLE);
		this.mResult.setGravity(Gravity.CENTER);
		ControlButtonUtil.initControlButtonView(this);
		this.mResult.setText(R.string.GpsWaitforLocationData);

		// findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);

		this.mLocatManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// mLocatManager.getGpsStatus(gpsStatus);
		// if(gpsStatus != null && gpsStatus.getTimeToFirstFix() != 0) {
		// mResult.setText("TTFF already been set, please turn off & turn on GPS..");
		//
		// mHandler.postDelayed(mSkipRunnable, 5000);
		// return;
		// }

		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		wifiManager.setWifiEnabled(false);
		if (BluetoothAdapter.getDefaultAdapter() != null) {
			BluetoothAdapter.getDefaultAdapter().disable();
		}

		/*
		 * Settings.Secure.setLocationProviderEnabled(getContentResolver(),
		 * LocationManager.GPS_PROVIDER, true);
		 */

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!mLocatManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			this.mResult.setText(R.string.GpsOff);
			// mHandler.postDelayed(mSkipRunnable, 5000);
			// return;
		} else {

			TextView nmeaView = (TextView) findViewById(R.id.nmealocationresultText);
			nmeaView.setText(R.string.GpsWaitforNMEAInformation);

			mLocatManager.addGpsStatusListener(this.statusListener);
			mLocatManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					step, 0, mLocationListener);
			mLocatManager.addNmeaListener(nmeaListener);
			stop = true;
			mHandler.postDelayed(new Runnable() {

				public void run() {
					SystemUtil.execRootCmd(DeviceTest.GPS_COLD_START_PATH);
					SystemUtil.execRootCmd(DeviceTest.GPS_COLD_START_PATH);
				}
			}, 2000);
			stop = false;
			mHandler.postDelayed(mFailedRunnable, 120 * 1000);
		}

		openGPSSettings();
		getLocation();
	}

	public boolean onTouchEvent(MotionEvent paramMotionEvent) {
		if (paramMotionEvent.getAction() == MotionEvent.ACTION_DOWN) {
			if (!mLocatManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				Intent intent = new Intent();
				intent.setAction("android.settings.LOCATION_SOURCE_SETTINGS");
				intent.setComponent(new ComponentName("com.android.settings",
						"com.android.settings.Settings$LocationSettingsActivity"));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		}
		return super.onTouchEvent(paramMotionEvent);
	}

	protected void onStop() {
		super.onStop();
		this.mLocatManager.removeGpsStatusListener(this.statusListener);
		this.mLocatManager.removeUpdates(this.mLocationListener);
		stop = true;
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}

	GpsStatus gpsStatus;

	private void updateNmeaStatus(String nmea) {
		TextView nmeaView = (TextView) findViewById(R.id.nmealocationresultText);
		nmeaView.setText(nmea);
	}

	private final GpsStatus.NmeaListener nmeaListener = new GpsStatus.NmeaListener() {
		public void onNmeaReceived(long timestamp, String nmea) {

			updateNmeaStatus(nmea);
		}
	};

	class MystatusListener implements GpsStatus.Listener {

		public void onGpsStatusChanged(int event) {
			gpsStatus = mLocatManager.getGpsStatus(null);
			if (stop) {
				return;
			}
			switch (event) {
			case GpsStatus.GPS_EVENT_FIRST_FIX:

				Log.e("Jeffy",
						"GPS_EVENT_FIRST_FIX:" + gpsStatus.getTimeToFirstFix());
				String ttff = ((int) (gpsStatus.getTimeToFirstFix() / 100))
						/ 10.0 + "s";
				mResult.setText("TTFF: " + ttff);
				ControlButtonUtil.setResult(DeviceTest.RESULT_INFO_HEAD + ttff);
				mHandler.removeMessages(MSG_RUN);
				if (gpsStatus.getTimeToFirstFix() > 90 * 1000) {
					mHandler.postDelayed(mFailedRunnable, 2 * 1000);
				} else {
					mHandler.postDelayed(mPassRunnable, 2 * 1000);
				}
				findViewById(R.id.btn_Pass).setVisibility(View.VISIBLE);
				findViewById(R.id.btn_Fail).setVisibility(View.INVISIBLE);
				break;
			default:
				break;
			}
		}

	}

	// ////////////////////////////////////////////////////////////////////////
	private void openGPSSettings() {
		LocationManager alm = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			Toast.makeText(this, "GPS模块正常", Toast.LENGTH_SHORT).show();
			return;
		}

		Toast.makeText(this, "请开启GPS！", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivityForResult(intent, 0); // 此为设置完成后返回到获取界面
	}

	private void getLocation() {
		// 获取位置管理服务
		LocationManager locationManager;
		String serviceName = Context.LOCATION_SERVICE;
		locationManager = (LocationManager) this.getSystemService(serviceName);

		String provider = locationManager.getBestProvider(getCriteria(), true); // 获取GPS信息

		Location location = locationManager.getLastKnownLocation(provider); // 通过GPS获取位置
		updateToNewLocation(location);
		// 设置监听器，自动更新的最小时间为间隔N秒(1秒为1*1000，这样写主要为了方便)或最小位移变化超过N米
		locationManager.requestLocationUpdates(provider, step, 1,
				mLocationListener);
		locationManager.addGpsStatusListener(statusListener); // 注册状态信息回调
	}

	private void updateToNewLocation(Location location) {

		// 获取系统时间
		Time t = new Time();
		t.setToNow(); // 取得系统时间
		int year = t.year;
		int month = t.month + 1;
		int date = t.monthDay;
		int hour = t.hour; // 24小时制
		int minute = t.minute;
		int second = t.second;

		TextView tv1;
		tv1 = (TextView) this
				.findViewById(R.id.longitudeLatitudeLocationresultText);
		if (location != null) {
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			double altitude = location.getAltitude(); // 海拔

			tv1.setText("搜索卫星个数：" + numSatelliteList.size());
			tv1.append("\n维度：" + latitude + "\n经度" + longitude);
			tv1.append("\n海拔：" + altitude);
			tv1.append("\n时间：" + year + "年" + month + "月" + date + "日" + hour
					+ ":" + minute + ":" + second);
		} else {
			tv1.setText("无法获取地理信息");
		}

	}

	/**
	 * 返回查询条件
	 * 
	 * @return
	 */
	private Criteria getCriteria() {
		Criteria criteria = new Criteria();
		// 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		// */ // ???
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		/*
		 * / //设置是否要求速度 criteria.setSpeedRequired(true); // 设置是否允许运营商收费
		 * criteria.setCostAllowed(false); //设置是否需要方位信息
		 * criteria.setBearingRequired(true); //设置是否需要海拔信息
		 * criteria.setAltitudeRequired(true); //
		 */
		// 设置对电源的需求
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		return criteria;
	}

}
