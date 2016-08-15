package com.tchip.autotest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import com.tchip.autotest.R;
import com.tchip.autotest.helper.ControlButtonUtil;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.telephony.TelephonyManager;

public class SimCardTestActivity extends Activity {
	private static final String TAG = "SdCardTestActivity";

	TextView mResult;

	private Button mPassBtn;

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		DeviceTest.lockScreenOrientation(this);
		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
				+ ")");
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.simcardtest);

		this.mResult = (TextView) findViewById(R.id.text);
		this.mResult.setVisibility(View.VISIBLE);
		this.mResult.setGravity(17);

		ControlButtonUtil.initControlButtonView(this);
		mPassBtn = (Button) findViewById(R.id.btn_Pass);

	}

	protected void onResume() {
		super.onResume();
		TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = mTelephonyMgr.getSubscriberId();

		if (imsi == null) {
			mResult.setText(getString(R.string.sim_test_failed));
			mPassBtn.setVisibility(View.INVISIBLE);
		} else {
			mResult.setText("IMSI:" + imsi);
			mResult.append("\n" + getString(R.string.sim_test_passed));
			mPassBtn.setVisibility(View.VISIBLE);
		}
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
}
