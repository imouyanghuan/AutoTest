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

public class CameraTestActivity extends Activity {
	private static final int mRequestCode = 1000;

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		DeviceTest.lockScreenOrientation(this);
		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
				+ ")");
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.cameratest);

		ControlButtonUtil.initControlButtonView(this);
	}

	public boolean onTouchEvent(MotionEvent paramMotionEvent) {
		if (paramMotionEvent.getAction() == MotionEvent.ACTION_DOWN) {
			Intent localIntent = new Intent(
					"android.media.action.IMAGE_CAPTURE");
			startActivityIfNeeded(localIntent, 1000);
		}
		return super.onTouchEvent(paramMotionEvent);
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
}
