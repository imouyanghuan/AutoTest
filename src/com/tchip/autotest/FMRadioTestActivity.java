package com.tchip.autotest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import com.tchip.autotest.R;
import com.tchip.autotest.helper.ControlButtonUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class FMRadioTestActivity extends Activity {
	private static final int REQUEST_START_FM_RADIO = 1001;
	private static final int KEY_BACK = 4;
	TextView mResult;

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_START_FM_RADIO)
			mResult.setVisibility(View.VISIBLE);
	}

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		DeviceTest.lockScreenOrientation(this);
		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
				+ ")");
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.fmradiotest);

		mResult = (TextView) findViewById(R.id.FMRadioTestResult);

		ControlButtonUtil.initControlButtonView(this);
	}

	public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
		if (keyCode == KEY_BACK)
			setResult(KEY_BACK);
		return super.onKeyDown(keyCode, keyEvent);
	}

	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {

			// Intent intent = new Intent("com.rk.FMRadio");
			Intent intent = new Intent();
			intent.setClassName("com.mediatek.FMRadio",
					"com.mediatek.FMRadio.FMRadioActivity");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivityIfNeeded(intent, REQUEST_START_FM_RADIO);
		}
		return super.onTouchEvent(event);
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
}
