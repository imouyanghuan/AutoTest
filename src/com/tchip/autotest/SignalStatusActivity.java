package com.tchip.autotest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tchip.autotest.R;
import com.android.internal.telephony.PhoneFactory;
//import com.android.internal.telephony.gemini.GeminiNetworkSubUtil;
//import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneStateIntentReceiver;
import com.tchip.autotest.helper.ControlButtonUtil;
//import static android.provider.Telephony.Intents.EXTRA_PLMN;
//import static android.provider.Telephony.Intents.EXTRA_SHOW_PLMN;
//import static android.provider.Telephony.Intents.EXTRA_SHOW_SPN;
//import static android.provider.Telephony.Intents.EXTRA_SPN;
//import static android.provider.Telephony.Intents.SPN_STRINGS_UPDATED_ACTION;

import android.preference.Preference;
//import android.provider.Telephony.SIMInfo;
import android.telephony.PhoneNumberUtils;
import android.telephony.ServiceState;

/**
 * SIM卡状态
 */
public class SignalStatusActivity extends Activity {

	private String TAG = "SignalStatusActivity";
	/* 我们可以用它们onResume和onPause方法停止listene */

	TelephonyManager mTelephonyManager;

	// private Phone mPhone = null;
	private PhoneStateIntentReceiver mPhoneStateReceiver;
	private Resources mRes;
	private String sUnknown;
	private int mServiceState;
	private String imsi;

	private static final int EVENT_SIGNAL_STRENGTH_CHANGED = 200;
	private static final int EVENT_SERVICE_STATE_CHANGED = 300;
	private static final int EVENT_UPDATE_STATS = 500;

	private TextView networkTV = null;
	private TextView signalStrengthTV = null;
	private TextView mobileNetworkTypeTV = null;
	private TextView serviceStateTV = null;
	private TextView simImei = null;
	private Button mPassBtn;

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DeviceTest.lockScreenOrientation(this);
		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
				+ ")");

		setContentView(R.layout.signalstatus);
		ControlButtonUtil.initControlButtonView(this);
		networkTV = (TextView) findViewById(R.id.status_operator);
		signalStrengthTV = (TextView) findViewById(R.id.status_signal_strength);
		mobileNetworkTypeTV = (TextView) findViewById(R.id.status_network_type);
		serviceStateTV = (TextView) findViewById(R.id.status_service_state);
		simImei = (TextView) findViewById(R.id.sim_imei);
		mPassBtn = (Button) findViewById(R.id.btn_Pass);

		mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		/* Update the listener, and start it */

		mRes = getResources();
		sUnknown = mRes.getString(R.string.device_info_default);

		// if (UserHandle.myUserId() == UserHandle.USER_OWNER) {
		// mPhone = PhoneFactory.getDefaultPhone();
		// }

		mPhoneStateReceiver = new PhoneStateIntentReceiver(this, mHandler);
		mPhoneStateReceiver.notifySignalStrength(EVENT_SIGNAL_STRENGTH_CHANGED);
		mPhoneStateReceiver.notifyServiceState(EVENT_SERVICE_STATE_CHANGED);

		// Log.v(TAG,
		// ">>>>>>>>> PhoneFactory.getDefaultPhone() : "
		// + PhoneFactory.getDefaultPhone());
		// Log.v(TAG,">>>>>>>>> UserHandle.myUserId() : " +
		// UserHandle.myUserId());
		// Log.v(TAG, ">>>>>>>>> UserHandle.USER_OWNER : " +
		// UserHandle.USER_OWNER);
		// Log.v(TAG,
		// ">>>>>>>>> PhoneFactory.getDefaultPhone() : "
		// + PhoneFactory.getDefaultPhone());

	}

	/* Called when the application is minimized */

	@Override
	protected void onPause() {
		super.onPause();
		if (imsi == null)
			return;
		// if (mPhone != null) {
		mPhoneStateReceiver.unregisterIntent();
		mTelephonyManager.listen(mPhoneStateListener,
				PhoneStateListener.LISTEN_NONE);
		// }
		unregisterReceiver(mBatteryInfoReceiver);
	}

	/* Called when the application resumes */

	@Override
	protected void onResume() {
		super.onResume();
		imsi = mTelephonyManager.getSubscriberId();
		if (imsi == null) {
			simImei.setText("");
			networkTV.setText("");
			signalStrengthTV.setText("");
			mobileNetworkTypeTV.setText("");
			serviceStateTV.setText(R.string.signal_cannot_get_imsi);
			serviceStateTV.setTextColor(Color.RED);
			mPassBtn.setVisibility(View.INVISIBLE);
			return;
		}
		simImei.append(": " + imsi);

		mPassBtn.setVisibility(View.VISIBLE);

		// if (mPhone != null ) {
		mPhoneStateReceiver.registerIntent();

		updateSignalStrength();
		// updateServiceState(mPhone.getServiceState());

		mTelephonyManager.listen(mPhoneStateListener,
				PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
		mTelephonyManager.listen(mSignalStrengthListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		mTelephonyManager.listen(mPhoneServiceListener,
				PhoneStateListener.LISTEN_SERVICE_STATE);
		// if (mShowLatestAreaInfo) {
		// registerReceiver(mAreaInfoReceiver, new
		// IntentFilter(CB_AREA_INFO_RECEIVED_ACTION),
		// CB_AREA_INFO_SENDER_PERMISSION, null);
		// // Ask CellBroadcastReceiver to broadcast the latest area info
		// received
		// Intent getLatestIntent = new Intent(GET_LATEST_CB_AREA_INFO_ACTION);
		// sendBroadcastAsUser(getLatestIntent, UserHandle.ALL,
		// CB_AREA_INFO_SENDER_PERMISSION);
		// }
		// }

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		// filter.addAction(SPN_STRINGS_UPDATED_ACTION);
		registerReceiver(mBatteryInfoReceiver, filter);
		mHandler.sendEmptyMessage(EVENT_UPDATE_STATS);
	}

	/**
	 * 信号监听
	 */
	private PhoneStateListener mSignalStrengthListener = new PhoneStateListener() {

		@Override
		public void onServiceStateChanged(ServiceState serviceState) {
			super.onServiceStateChanged(serviceState);
			updateSignalStrength();
		}

	};

	void updateSignalStrength() {
		// TODO PhoneStateIntentReceiver is deprecated and PhoneStateListener
		// should probably used instead.

		// not loaded in some versions of the code (e.g., zaku)

		int state = mPhoneStateReceiver.getServiceState().getState();
		Resources r = getResources();

		if ((ServiceState.STATE_OUT_OF_SERVICE == state)
				|| (ServiceState.STATE_POWER_OFF == state)) {
			// mSignalStrength.setSummary("0");
		}

		int signalDbm = mPhoneStateReceiver.getSignalStrengthDbm();
		Log.d(TAG, "updateSignalStrength() signalDbm : " + signalDbm);
		if (-1 == signalDbm)
			signalDbm = 0;

		int signalAsu = mPhoneStateReceiver.getSignalStrengthLevelAsu();
		Log.d(TAG, "updateSignalStrength() signalAsu : " + signalAsu);
		if (-1 == signalAsu)
			signalAsu = 0;

		// mSignalStrength.setSummary(String.valueOf(signalDbm) + " "
		// + r.getString(R.string.radioInfo_display_dbm) + "   "
		// + String.valueOf(signalAsu) + " "
		// + r.getString(R.string.radioInfo_display_asu));

		Log.v(TAG,
				">>>>>>>>>updateSignalStrength  display : "
						+ String.valueOf(signalDbm) + " "
						+ r.getString(R.string.radioInfo_display_dbm) + "   "
						+ String.valueOf(signalAsu) + " "
						+ r.getString(R.string.radioInfo_display_asu));

		signalStrengthTV.setText(getString(R.string.status_signal_strength)
				+ " : " + String.valueOf(signalDbm) + " "
				+ r.getString(R.string.radioInfo_display_dbm) + "   "
				+ String.valueOf(signalAsu) + " "
				+ r.getString(R.string.radioInfo_display_asu));

	}

	private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
		@Override
		public void onDataConnectionStateChanged(int state) {
			updateDataState();
			updateNetworkType();
		}
	};

	private void updateDataState() {
		int state = mTelephonyManager.getDataState();
		String display = mRes.getString(R.string.radioInfo_unknown);

		switch (state) {
		case TelephonyManager.DATA_CONNECTED:
			display = mRes.getString(R.string.radioInfo_data_connected);
			break;
		case TelephonyManager.DATA_SUSPENDED:
			display = mRes.getString(R.string.radioInfo_data_suspended);
			break;
		case TelephonyManager.DATA_CONNECTING:
			display = mRes.getString(R.string.radioInfo_data_connecting);
			break;
		case TelephonyManager.DATA_DISCONNECTED:
			display = mRes.getString(R.string.radioInfo_data_disconnected);
			break;
		}

		// setSummaryText(KEY_DATA_STATE, display);
		Log.v(TAG, ">>>>>>>>>updateDataState KEY_DATA_STATE, display : "
				+ display);

	}

	private void updateNetworkType() {
		// Whether EDGE, UMTS, etc...
		// setSummaryText(KEY_NETWORK_TYPE,
		// mTelephonyManager.getNetworkTypeName() +
		// ":" + mTelephonyManager.getNetworkType());

		Log.v(TAG, ">>>>>>>>>updateNetworkType KEY_NETWORK_TYPE : "
				+ mTelephonyManager.getNetworkTypeName() + ":"
				+ mTelephonyManager.getNetworkType());

		mobileNetworkTypeTV.setText(getString(R.string.status_network_type)
				+ " : " + mTelephonyManager.getNetworkTypeName() + ":"
				+ mTelephonyManager.getNetworkType());
	}

	private PhoneStateListener mPhoneServiceListener = new PhoneStateListener() {
		@Override
		public void onServiceStateChanged(ServiceState serviceState) {
			mServiceState = serviceState.getState();
			updateServiceState(serviceState);
		}
	};

	private void updateServiceState(ServiceState serviceState) {
		int state = serviceState.getState();
		String display = mRes.getString(R.string.radioInfo_unknown);

		switch (state) {
		case ServiceState.STATE_IN_SERVICE:
			display = mRes.getString(R.string.radioInfo_service_in);
			break;
		case ServiceState.STATE_OUT_OF_SERVICE:
		case ServiceState.STATE_EMERGENCY_ONLY:
			display = mRes.getString(R.string.radioInfo_service_out);
			break;
		case ServiceState.STATE_POWER_OFF:
			display = mRes.getString(R.string.radioInfo_service_off);
			break;
		}
		Log.v(TAG, ">>>>>>>>>updateServiceState KEY_SERVICE_STATE :  "
				+ display);

		serviceStateTV.setText(getString(R.string.status_service_state) + " : "
				+ display);

		// setSummaryText(KEY_SERVICE_STATE, display);
	}

	// ///////////////////////////////////////////////////

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_SIGNAL_STRENGTH_CHANGED:
				updateSignalStrength();
				break;

			case EVENT_SERVICE_STATE_CHANGED:
				ServiceState serviceState = mPhoneStateReceiver
						.getServiceState();
				updateServiceState(serviceState);
				break;

			case EVENT_UPDATE_STATS:
				// updateTimes();
				// sendEmptyMessageDelayed(EVENT_UPDATE_STATS, 1000);
				break;
			}
		}

	};

	// //////////////////////////////
	private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {

				// mBatteryLevel.setSummary(Utils.getBatteryPercentage(intent));
				// mBatteryStatus.setSummary(Utils.getBatteryStatus(getResources(),
				// intent));
			}
			// else if (SPN_STRINGS_UPDATED_ACTION.equals(action)) {

			// String operatorName = null;
			// String plmn = null;
			// String spn = null;
			// if (intent.getBooleanExtra(EXTRA_SHOW_PLMN, false)) {
			//
			// plmn = intent.getStringExtra(EXTRA_PLMN);
			// if (plmn != null) {
			// operatorName = plmn;
			// }
			// Log.v(TAG, ">>>>>>>>>>> EXTRA_SHOW_PLMN plmn != null: "
			// + (plmn != null));
			// }
			// if (intent.getBooleanExtra(EXTRA_SHOW_SPN, false)) {
			// spn = intent.getStringExtra(EXTRA_SPN);
			// if (spn != null) {
			// operatorName = spn;
			// }
			// Log.v(TAG, ">>>>>>>>>>> EXTRA_SHOW_PLMN spn != null: "
			// + (spn != null));
			// }
			// Log.v(TAG, ">>>>>>>>>>> operatorName : " + operatorName);
			// if (operatorName != null) {
			// networkTV.setVisibility(View.VISIBLE);
			// networkTV.setText(getString(R.string.status_operator)
			// + " : " + operatorName);
			// } else
			// networkTV.setVisibility(View.GONE);
			// // Preference p = findPreference(KEY_OPERATOR_NAME);
			// // if (p != null) {
			// // mExt.updateOpNameFromRec(p,operatorName);
			// // }
			// }
		}
	};

	// 取消返回按钮
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
}
