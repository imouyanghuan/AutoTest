package com.DeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import com.DeviceTest.helper.ControlButtonUtil;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GocBtTestActivity extends Activity {
	private static final String TAG = GocBtTestActivity.class.getSimpleName();
	private final static String ERRMSG = "FAIL!Can not find a bluetooth equipment!";

	private static final int MSG_OPEN = 0;
	private static final int MSG_FAILED = 1;
	private static final int MSG_FINISH_TEST = 2;
	private static final int MSG_NO_FINISH_DEVICE = 3;

	private boolean isTestFinish = false;
	// private boolean isUnRegOver = false;
	private BluetoothAdapter mAdapter;
	private BroadcastReceiver mBluetoothReceiver;

	// private ArrayList<String> mDeviceNames;
	/** Test Result **/
	private TextView textResult;

	/** Name & MacAddress of Find Device **/
	private TextView textFindDevice;
	private String strFindDevices = "";

	/** Number of Find Device **/
	private int numberFindDevice = 0;

	private TextView textSubTitle;
	private int mTestCount;
	private int mTestOpen;
	private ProgressBar progressBar;
	boolean stop = false;

	public GocBtTestActivity() {
		// this.mDeviceNames = new ArrayList<String>();

	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DeviceTest.lockScreenOrientation(this);
		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
				+ ")");
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.gocbttest);

		textFindDevice = (TextView) findViewById(R.id.textFindDevice);
		textSubTitle = (TextView) findViewById(R.id.textSubTitle);
		textResult = (TextView) findViewById(R.id.textResult);
		progressBar = (ProgressBar) findViewById(R.id.progress);

		ControlButtonUtil.initControlButtonView(this);

		numberFindDevice = 0;
		btScanResultReceiver = new BtScanResultReceiver();
		IntentFilter btScanResultFilter = new IntentFilter();
		btScanResultFilter.addAction("com.tchip.DISCOVERY_SUCCESSED");
		btScanResultFilter.addAction("com.tchip.DISCOVERY_DONE");
		registerReceiver(btScanResultReceiver, btScanResultFilter);

		sendBroadcast(new Intent("com.tchip.DISCOVERY_PHONE"));
		textResult.setText(getString(R.string.bt_scan_start));

	}

	private BtScanResultReceiver btScanResultReceiver;

	public class BtScanResultReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("com.tchip.DISCOVERY_SUCCESSED")) {
				// Scan Success
				String deviceName = intent.getStringExtra("name");
				String deviceAddress = intent.getStringExtra("addr");
				if (deviceName != null && deviceName.trim().length() > 0) {
					numberFindDevice++;
					strFindDevices = strFindDevices + numberFindDevice + " - "
							+ deviceName + " (" + deviceAddress + ")" + "\n";
					textFindDevice.setText(strFindDevices);

					textResult.setText(String.format(
							getString(R.string.bt_scanning_with_result),
							numberFindDevice));
				}
			} else if (action.equals("com.tchip.DISCOVERY_DONE")) {
				// Scan Done
				if (numberFindDevice > 0) {
					textResult.setText(String.format(
							getString(R.string.bt_scan_finish_with_result),
							numberFindDevice));
				} else {
					textResult
							.setText(getString(R.string.bt_scan_finish_not_find));
				}
				progressBar.setVisibility(View.GONE);
			}

		}
	}

	protected void onResume() {

		super.onResume();
		stop = false;
	}

	public void onPause() {
		super.onPause();
		stop = true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unregisterReceiver(btScanResultReceiver);
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
}
