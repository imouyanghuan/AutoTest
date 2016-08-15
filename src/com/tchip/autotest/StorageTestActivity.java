package com.tchip.autotest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import com.tchip.autotest.R;
import com.tchip.autotest.helper.ControlButtonUtil;
import com.tchip.autotest.helper.SystemUtil;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

/**
 * 存储
 */
public class StorageTestActivity extends Activity {
	private final static String TAG = "StorageTest";
	private TextView mResult;

	boolean stop = false;
	private static final String TEST_FILE_PATH = "/system/bin/logcat";
	private static final String TEMP_FILE_PATH = DeviceTest.TEMP_FILE_PATH
			+ "_test";

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		DeviceTest.lockScreenOrientation(this);
		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
				+ ")");
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.storagetest);

		boolean pass = true;

		mResult = (TextView) findViewById(R.id.resultText);
		String result = "";
		// String sdPath = System.getenv("EXTERNAL_STORAGE");
		String sdPath = Environment.getExternalStorageDirectory().toString();
		Log.e(TAG, "EXTERNAL_STORAGE path :" + sdPath);
		if (sdPath == null) {
			result += getString(R.string.StorageSDNoFind);
			pass = false;
		} else {
			if (!testCopy(sdPath)) {
				pass = false;
				result += getString(R.string.StorageSDCopyF);
			} else {
				result += getString(R.string.StorageSDCopyS);
			}
		}

		String usbPath = System.getenv("EXTERNAL_HOST_USB");
		if (null == usbPath) {
			result += getString(R.string.StorageUsbNoFind);
			pass = false;
		} else {
			if (!testCopy(usbPath)) {
				pass = false;
				result += getString(R.string.StorageUsbCopyF);
			} else {
				result += getString(R.string.StorageUsbCopyS);
			}
		}

		result += pass ? "Pass!" : "Failed";
		mResult.setText(result);
		ControlButtonUtil.initControlButtonView(this);
		findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
		if (pass) {
			findViewById(R.id.btn_Pass).performClick();
		} else {
			mHandler.postDelayed(mFailedRunnable, DeviceTest.TEST_FAILED_DELAY);
		}
	}

	Handler mHandler = new Handler();
	Runnable mFailedRunnable = new Runnable() {

		public void run() {
			if (stop) {
				return;
			}
			mHandler.removeCallbacks(mFailedRunnable);
			findViewById(R.id.btn_Fail).performClick();
		}
	};

	private boolean testCopy(String dstPath) {
		SystemUtil.execScriptCmd("cat " + TEST_FILE_PATH + " > " + dstPath
				+ "/test", DeviceTest.TEMP_FILE_PATH, true);
		SystemUtil.execScriptCmd("cat " + dstPath + "/test" + " > "
				+ TEMP_FILE_PATH + "\nrm " + dstPath + "/test",
				DeviceTest.TEMP_FILE_PATH, true);

		byte[] buffer1 = new byte[1024];
		byte[] buffer2 = new byte[1024];
		int length = 0;
		try {
			BufferedInputStream bisSrc = new BufferedInputStream(
					new FileInputStream(TEST_FILE_PATH));
			BufferedInputStream bisDst = new BufferedInputStream(
					new FileInputStream(TEMP_FILE_PATH));
			Arrays.fill(buffer1, (byte) 0);
			Arrays.fill(buffer2, (byte) 0);
			while ((length = bisSrc.read(buffer1)) > 0) {
				if (length != bisDst.read(buffer2)) {
					Log.e(TAG, "length not equals  : failed");
					return false;
				}
				if (!Arrays.equals(buffer1, buffer2)) {
					Log.e(TAG, "data not equals  : failed");
					return false;
				}
			}
			bisSrc.close();
			bisDst.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			File file = new File(TEMP_FILE_PATH);
			if (file.exists()) {
				file.delete();
			}
		}
		return true;
	}

	public void onStop() {
		super.onStop();
		mHandler.removeCallbacks(mFailedRunnable);
		stop = true;
	}

	// if (device != null && progressBar.isShown()) {
	// progressBar.setVisibility(View.GONE);
	// mResult.setText("Find equipment....\n Pass!");

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
}
