package com.DeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Environment;
import android.os.storage.IMountService;
import android.os.ServiceManager;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageEventListener;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;
import android.os.SystemProperties;

import com.DeviceTest.helper.ControlButtonUtil;
import android.text.TextUtils;

import java.io.File;
import java.util.List;

/**
 * 存储
 * 
 * @author zzp
 * 
 */
public class StorageActivity extends Activity implements OnCancelListener {
	private static final String TAG = "Memory";
	private static final boolean localLOGV = false;

	private Resources mRes;

	private TextView mNandSize;
	private TextView mNandAvail;
	private TextView mDataAvail;

	boolean mSdMountToggleAdded = true;
	boolean mNandMountToggleAdded = true;

	private StorageManager mStorageManager = null;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		DeviceTest.lockScreenOrientation(this);
		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
				+ ")");
		if (mStorageManager == null) {
			mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
			mStorageManager.registerListener(mStorageListener);
		}
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.storageactivitytest);

		mRes = getResources();
		mNandSize = (TextView) findViewById(R.id.nand_total_space);
		mNandAvail = (TextView) findViewById(R.id.nand_available_space);
		mDataAvail = (TextView) findViewById(R.id.data_available_space);

		ControlButtonUtil.initControlButtonView(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		IntentFilter intentFilter = new IntentFilter(
				Intent.ACTION_MEDIA_SCANNER_STARTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		intentFilter.addDataScheme("file");
		registerReceiver(mReceiver, intentFilter);
		updateMemoryStatus(DeviceTest.flash_path);
	}

	StorageEventListener mStorageListener = new StorageEventListener() {

		@Override
		public void onStorageStateChanged(String path, String oldState,
				String newState) {
			Log.d(TAG, "Received storage state changed notification that "
					+ path + " changed state from " + oldState + " to "
					+ newState);
			if (path.equals(DeviceTest.sdcard_path)
					&& !newState.equals(Environment.MEDIA_MOUNTED)) {
			} else {
				updateMemoryStatus(DeviceTest.flash_path);
			}
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

	@Override
	protected void onDestroy() {
		if (mStorageManager != null && mStorageListener != null) {
			mStorageManager.unregisterListener(mStorageListener);
		}
		super.onDestroy();
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateMemoryStatus(DeviceTest.flash_path);
			// updateMemoryStatus(Environment.getExternalStorageDirectory().getPath());
		}
	};

	private void updateMemoryStatus(String path) {
		String status = SystemProperties.get("EXTERNAL_STORAGE_STATE",
				"unmounted");
		try {
			if (path.equals(DeviceTest.flash_path)) {
				status = mStorageManager.getVolumeState(path);
			}
		} catch (Exception e) {
			Log.e(TAG, ">>>>>>>>>>>> DeviceTest.flash_path == null <<<<<");
		}

		String readOnly = "";
		if (status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			status = Environment.MEDIA_MOUNTED;
			readOnly = mRes.getString(R.string.read_only);
		}

		if (status.equals(Environment.MEDIA_MOUNTED)) {
			try {
				// File path = Environment.getExternalStorageDirectory();
				StatFs stat = new StatFs(path);
				long blockSize = stat.getBlockSize();
				long totalBlocks = stat.getBlockCount();
				long availableBlocks = stat.getAvailableBlocks();

				if (path.equals(DeviceTest.flash_path)) {
					mNandSize.setText(formatSize(totalBlocks * blockSize));
					mNandAvail.setText(formatSize(availableBlocks * blockSize)
							+ readOnly);
				}
			} catch (IllegalArgumentException e) {
				// this can occur if the SD card is removed, but we haven't
				// received the
				// ACTION_MEDIA_REMOVED Intent yet.
				status = Environment.MEDIA_REMOVED;
			}

		} else {
			if (TextUtils.isEmpty(DeviceTest.flash_path)) {
				mNandSize.setText(mRes.getString(R.string.nand_unavailable));
				mNandAvail.setText(mRes.getString(R.string.nand_unavailable));
				mNandAvail.append("\n注意：nand flash已经被屏蔽");
			} else if (path.equals(DeviceTest.flash_path)) {
				mNandSize.setText(mRes.getString(R.string.nand_unavailable));
				mNandAvail.setText(mRes.getString(R.string.nand_unavailable));
				if (status.equals(Environment.MEDIA_UNMOUNTED)
						|| status.equals(Environment.MEDIA_NOFS)
						|| status.equals(Environment.MEDIA_UNMOUNTABLE)) {
				}
			}
		}

		File dataPath = Environment.getDataDirectory();
		StatFs stat = new StatFs(dataPath.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		mDataAvail.setText(formatSize(availableBlocks * blockSize));
	}

	private String formatSize(long size) {
		return Formatter.formatFileSize(this, size);
	}

	public void onCancel(DialogInterface dialog) {
		finish();
	}

	// 取消返回按钮
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}

}
