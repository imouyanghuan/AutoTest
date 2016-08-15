package com.DeviceTest;

import android.os.Environment;
import android.os.StatFs;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.StorageEventListener;
import android.os.SystemProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xmlpull.v1.XmlSerializer;

import com.DeviceTest.FirstRun.TEST_STATUS;
import com.DeviceTest.helper.SystemUtil;
import com.DeviceTest.helper.TestCase;
import com.DeviceTest.helper.XmlDeal;
import com.DeviceTest.helper.TestCase.RESULT;
import com.DeviceTest.view.MyGridView;
import com.DeviceTest.view.MyItemView;

import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;
import android.view.View;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.util.Xml;
import android.os.SystemProperties;

public class DeviceTest extends Activity {

	public static final int DEVICE_TEST_MAX_NUM = 1000;
	public static final int TEST_FAILED_DELAY = 3000;
	public static final String EXTRA_TEST_PROGRESS = "test progress";
	public static final String EXTRA_TEST_RESULT_INFO = "test result info";

	public static final String RESULT_INFO_HEAD = ";";
	public static final String RESULT_INFO_HEAD_JUST_INFO = "just info;";

	public static final String SAVE_PATH = "/data/";
	public static final String EXTRA_PATH = "/system/etc/";
	private static final String CONFIG_FILE_NAME = "DeviceTestConfig.xml";
	private static final String EXTRA_CONFIG_FILE_NAME = EXTRA_PATH
			+ CONFIG_FILE_NAME;
	public static final String DATA_PATH = "/data/data/com.DeviceTest/";
	private static final String SAVE_FILE_PATH = SAVE_PATH + "DeviceTestResult";
	private static final String TAG = "DeviceTest";
	private static final String SAVE_DATA_PATH = DATA_PATH + "DeviceTest.tmp";
	public static final String TEMP_FILE_PATH = DeviceTest.DATA_PATH + "test";

	private XmlDeal xmldoc = null;
	private Spinner mGroupTestSpinner;
	private Button mButtonCancel;
	private Button mTestChecked;
	private Button mShowFistTest;
	MyGridView myGridView;

	private List<TestCase> mTestCases;
	private List<TestCase> mCurrentCaseGroup;
	Object[] mTestGroupNames;

	public static String flash_path = null;
	public static String sdcard_path = null;
	public static String sdcard2_path = null;
	public static String usb_path = null;
	// /storage/sdcard0 /storage/sdcard1 /storage/sdcard2 /mnt/usbotg
	private int flash_pit = 0;
	private int sdcard_pit = 1;
	private int sdcard2_pit = 2;
	private int usb_pit = 3;
	private StorageVolume[] storageVolumes = null;

	private Context mContext;

	final int firstItem = 1;
	int save_item = firstItem;
	int delete_item = firstItem + 1;
	int view_item = firstItem + 2;
	int extgps_item = firstItem + 3;
	int dsacopy_item = firstItem + 4;
	int autonavi_item = firstItem + 5;
	FileControl myFileControl = null;

	/** Called when the activity is first created. */

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		DeviceTest.lockScreenOrientation(this);
		mContext = this;
		// Environment.getFlashStorageDirectory();
		setContentView(R.layout.main);
		myFileControl = new FileControl(mContext);
		if (!InitTestData()) {
			System.exit(-1);
		}
		this.setTitle(getResources().getString(R.string.app_name_cn));
		mTestCases = xmldoc.mTestCases;
		try {
			loadData();
		} catch (Exception e) {
			Log.e("Jeffy", "load data error.");
			e.printStackTrace();
		}

		myGridView = (MyGridView) findViewById(R.id.myGridView);
		myGridView.setColumnCount(4);

		for (TestCase testCase : mTestCases) {
			MyItemView itemView = new MyItemView(this);
			itemView.setText(testCase.getTestName());
			itemView.setTag(testCase.getTestNo());
			itemView.setCheck(testCase.getneedtest());
			if (testCase.isShowResult()) {
				RESULT result = testCase.getResult();
				itemView.setResult(result);
			}
			myGridView.addView(itemView);
		}

		myGridView.setOnItemClickListener(new MyGridView.OnItemClickListener() {
			public void onItemClick(ViewParent parent, View view, int position) {
				if (((MyItemView) view).setCheckClick()) {
					if (!((MyItemView) view).getischeck()) {
						mTestCases.get(position).setneedtest(false);
					} else {
						mTestCases.get(position).setneedtest(true);
					}
					return;
				}
				if (enableitemclick)
					enableitemclick = false;
				else
					return;
				Intent intent = new Intent();
				try {
					if (mTestCases.get(position) != null) {
						String strClsPath = "com.DeviceTest."
								+ mTestCases.get(position).getClassName();
						intent.setClass(DeviceTest.this,
								Class.forName(strClsPath).newInstance()
										.getClass());
						intent.putExtra(EXTRA_TEST_PROGRESS, "0/1");
						startActivityForResult(intent, position);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		mGroupTestSpinner = (Spinner) findViewById(R.id.GroupTestSpinner);

		mTestGroupNames = xmldoc.mCaseGroups.keySet().toArray();
		String[] testGroupTexts = new String[mTestGroupNames.length + 1];
		for (int i = 1; i < testGroupTexts.length; i++) {
			testGroupTexts[i] = "Group: " + mTestGroupNames[i - 1].toString();
		}
		testGroupTexts[0] = "CaseGroups";

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, testGroupTexts);
		mGroupTestSpinner.setAdapter(adapter);
		mGroupTestSpinner.setSelection(0, false);
		mGroupTestSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						if (position == 0) {
							return;
						}
						testGroup(mTestGroupNames[position - 1].toString());
						mGroupTestSpinner.setSelection(0, false);
					}

					public void onNothingSelected(AdapterView<?> parent) {

					}
				});

		mButtonCancel = (Button) findViewById(R.id.btn_cancel);
		mButtonCancel.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				try {
					save(SAVE_FILE_PATH);
					// SystemProperties.set("app.test_save.start","1");
				} catch (Exception e) {
					Log.e(TAG, "Failed to save test result!");
					e.printStackTrace();
				}
				finish();
			}

		});
		mTestChecked = (Button) findViewById(R.id.btn_testall);
		mTestChecked.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				testGroup(mTestGroupNames[0].toString());
			}
		});
		Button clearButton = (Button) findViewById(R.id.btn_clear);
		clearButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				showDialog(DIALOG_CLEAR_ID);
			}
		});

		mShowFistTest = (Button) findViewById(R.id.btn_showFistTest);
		mShowFistTest.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_MAIN);
				i.setClass(mContext, FirstRun.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(i);
				Log.v("zxx", ">>>>>>>>>>to main view");
				// SystemProperties.set("app.test_save.start","1");
			}
		});

		Button uninstallButton = (Button) findViewById(R.id.btn_uninstall);
		uninstallButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				uninstallPackage("com.DeviceTest");
				File delFile = new File("/dpfdata/test.mp4");
				if (delFile.exists()) {
					// delFile.delete();
				}
			}
		});
		createAssetFile("memtester", MEMTESTER_PATH);
		createAssetFile("gps_coldstart", GPS_COLD_START_PATH);

		InitStorage();
	}

	private boolean enableitemclick = true;

	@Override
	protected void onResume() {
		enableitemclick = true;
		super.onResume();
	}

	private void createAssetFile(String name, String destPath) {

		InputStream is = null;
		OutputStream os = null;
		try {
			is = getAssets().open(name);
			os = new FileOutputStream(destPath);
			int data = 0;
			while (true) {
				data = is.read();
				if (data < 0) {
					break;
				}
				os.write(data);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (Exception e) {
				}
			}
			SystemUtil.execRootCmd("chmod 777 " + destPath);
		}
	}

	public final static String MEMTESTER_PATH = DeviceTest.DATA_PATH
			+ "memtester";
	public final static String GPS_COLD_START_PATH = DeviceTest.DATA_PATH
			+ "gps_coldstart";
	static final int DIALOG_CLEAR_ID = 10;

	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("清空所有测试状态?")
				.setCancelable(false)
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						for (int i = 0; i < myGridView.getChildCount(); i++) {
							MyItemView myItemView = (MyItemView) myGridView
									.getChildAt(i);
							myItemView.setResult(RESULT.UNDEF);
							myItemView.setCheck(true);
							mTestCases.get(i).setShowResult(false);
							mTestCases.get(i).setneedtest(true);
						}
						try {
							save(SAVE_FILE_PATH);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				})
				.setNegativeButton("否", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		return alert;
	}

	private void loadData() throws Exception {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				SAVE_DATA_PATH));
		try {
			List<TestCase> savedData = (List<TestCase>) ois.readObject();
			for (TestCase savedCase : savedData) {
				for (TestCase testCase : mTestCases) {
					if (testCase.getClassName()
							.equals(savedCase.getClassName())) {
						testCase.setResult(savedCase.getResult());
						testCase.setDetail(savedCase.getDetail());
						testCase.setShowResult(savedCase.isShowResult());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ois != null) {
				ois.close();
			}
		}

	}

	public static String formatResult(String testName, RESULT result,
			String detail) {
		String myResult = result.name();
		Log.v("zxx", ">>>>>>testName=" + testName + ">>>>>>>>result.name()="
				+ myResult + ">>>>>detail=" + detail);
		if (myResult.compareTo("UNDEF") == 0) {
			myResult = "NOTEST";
		}
		if (myResult.compareTo("NG") == 0) {
			myResult = "FAIL";
		}
		if (detail == null) {
			return "[" + testName + "]" + "      " + myResult + "\n";
		}
		if (detail.startsWith(RESULT_INFO_HEAD_JUST_INFO)) {
			return detail.substring(RESULT_INFO_HEAD_JUST_INFO.length());
		}
		return "[" + testName + "]" + "      " + result.name() + detail + "\n";
	}

	synchronized private void save(String saveFilePath) throws IOException {
		// Log.v("zxxsave",">>>>>>>>");
		FileWriter fw;
		String tempSavePath = DATA_PATH + "save";
		fw = new FileWriter(tempSavePath);
		boolean isNeedSave = true;
		int myi = -1;
		String myResult = "UNDEF";
		for (TestCase testCase : mTestCases) {
			myi++;
			if (testCase.getClassName().equals(
					RuninTestActivity.class.getSimpleName())) {
				if (testCase.getDetail() == null) {
					testCase.setDetail(new RuninTestActivity().getResult());
				}
				// Log.v("zxxsave",">>>>>>>>1");
			} else if (testCase.getClassName().equals(
					GpsTestActivity.class.getSimpleName())) {
				if (testCase.getDetail() == null) {
					testCase.setDetail(new GpsTestActivity().getResult());
				}
				// Log.v("zxxsave",">>>>>>>>2");
			}
			// Log.v("zxxsave",">>>>>>>>getneedtest="+testCase.getneedtest());
			// Log.v("zxxsave",">>>>>>>>testCase.getResult()="+testCase.getResult());
			RESULT mResult = testCase.getResult();
			if (testCase.getneedtest()) { // add by zxx
				if (mResult.toString().trim().compareTo("UNDEF") == 0) {
					isNeedSave = false;
					Log.v("zxxsave", ">>>>>>>>not need");
				}
			}

			myResult = mResult.name().trim();
			if (!("UNDEF".equals(myResult))) {
				Log.v("zxxsave", ">>>>>>>>tested");
				DeviceTestApplication.myPrefs.setString("my" + myi, myResult);
			}

			// Log.v("zxx",">>>>>>>>SAVE="+DeviceTestApplication.myPrefs.getString("my"+myi,"NOTEST"));
			fw.write(formatResult(testCase.getTestName(), testCase.getResult(),
					testCase.getDetail()) + "\n");
		}
		fw.close();
		SystemUtil.execScriptCmd("cat " + tempSavePath + ">" + saveFilePath,
				TEMP_FILE_PATH, true);

		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
				SAVE_DATA_PATH));
		oos.writeObject(mTestCases);
		oos.close();
		if (isNeedSave) {
			// SystemProperties.set("app.test_save.start","1");
			Log.v("zxxsave", ">>>>>>>>save");
		}
	}

	protected void testGroup(String selectGroup) {
		mCurrentCaseGroup = xmldoc.mCaseGroups.get(selectGroup);
		int pos = 0;
		if (pos < mCurrentCaseGroup.size()) {
			while (!mCurrentCaseGroup.get(pos).getneedtest()) {
				pos++;
				if (pos >= mCurrentCaseGroup.size()) {
					return;
				}
			}
		}
		Intent intent = new Intent();
		if (mCurrentCaseGroup != null && mCurrentCaseGroup.get(pos) != null) {
			try {
				String strClsPath = "com.DeviceTest."
						+ mCurrentCaseGroup.get(pos).getClassName();
				intent.setClass(DeviceTest.this, Class.forName(strClsPath)
						.newInstance().getClass());
				intent.putExtra(EXTRA_TEST_PROGRESS, pos + "/"
						+ mCurrentCaseGroup.size());
				// we use nagtiv value to keep the sequence number when
				// do a all test.
				startActivityForResult(intent, pos + DEVICE_TEST_MAX_NUM);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}
		}
	}

	private void uninstallPackage(String packageName) {
		String cmd = "mount -o remount,rw /system /system\n"
				+ "rm -r /data/data/*DeviceTest*\n"
				+ "rm /data/app/*DeviceTest*\n"
				+ "rm /system/app/*DeviceTest*\n";
		SystemUtil.execScriptCmd(cmd, TEMP_FILE_PATH, true);

		Uri uninstallUri = Uri.parse("package:" + packageName);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, uninstallUri);
		startActivity(uninstallIntent);
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent paramIntent) {
		super.onActivityResult(requestCode, resultCode, paramIntent);
		Log.i("Jeffy", " -------------------- onResult---request:"
				+ requestCode + ",result:" + resultCode);
		if (resultCode == RESULT_OK)
			return;

		if (mCurrentCaseGroup != null
				&& (requestCode - DEVICE_TEST_MAX_NUM) >= mCurrentCaseGroup
						.size())
			return;

		int pos = requestCode;
		boolean ignore = (resultCode == RESULT.UNDEF.ordinal());

		if (requestCode >= DEVICE_TEST_MAX_NUM) {
			if (mCurrentCaseGroup == null) {
				Log.d(TAG,
						" _________________ mCurrentCaseGroup == null~~~~!!!!!");
				return;
			}
			// test auto judged.
			TestCase tmpTestCase = mCurrentCaseGroup.get(requestCode
					- DEVICE_TEST_MAX_NUM);
			if (tmpTestCase == null) {
				Log.d(TAG, " _________________ tmpTestCase == null~~~~!!!!!");
			}
			pos = tmpTestCase.getTestNo();
			Log.d(TAG, " _________________ tmpTestCas-----------~~~~!!!!!"
					+ pos);
		}

		if (!ignore && pos < mTestCases.size()) {
			MyItemView itemView = (MyItemView) myGridView.getChildAt(pos);
			RESULT result = RESULT.values()[resultCode];
			itemView.setResult(result);
			mTestCases.get(pos).setResult(result);
			mTestCases.get(pos).setShowResult(true);
			try {
				String detail = paramIntent
						.getStringExtra(EXTRA_TEST_RESULT_INFO);
				mTestCases.get(pos).setDetail(detail);
				Log.d(TAG,
						" _________________ tmpTestCas---------detail--~~~~!!!!!"
								+ detail);
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				save(SAVE_FILE_PATH);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		if (requestCode >= DEVICE_TEST_MAX_NUM) {
			// test next autojuaged.
			pos = requestCode - DEVICE_TEST_MAX_NUM;
			if (!ignore)
				pos++;
			else
				pos--;
			Log.d(TAG, " _________________ tmpTestCas---------pos--1~~~~!!!!!"
					+ pos);
			Intent intent = new Intent();
			if (pos >= 0 && pos < mCurrentCaseGroup.size()) {
				while (!mCurrentCaseGroup.get(pos).getneedtest()) {
					pos++;
					if (pos >= mCurrentCaseGroup.size()) {
						return;
					}
				}
				Log.d(TAG,
						" _________________ tmpTestCas---------pos--2~~~~!!!!!"
								+ pos);
				try {
					String strClsPath = "com.DeviceTest."
							+ mCurrentCaseGroup.get(pos).getClassName();
					intent.setClass(DeviceTest.this, Class.forName(strClsPath)
							.newInstance().getClass());

					intent.putExtra(EXTRA_TEST_PROGRESS, pos + "/"
							+ mCurrentCaseGroup.size());

					// we use nagtiv value to keep the sequence number when
					// do a all test.
					startActivityForResult(intent, pos + DEVICE_TEST_MAX_NUM);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private boolean InitTestData(InputStream is) {
		if (is == null) {
			return false;
		}
		try {
			xmldoc = new XmlDeal(is);
		} catch (Exception e) {
			Log.e(TAG, "parse the xmlfile is fail");
			return false;
		}
		return true;

	}

	private boolean InitTestData() {
		InputStream is = null;
		try {
			File configFile = new File(EXTRA_CONFIG_FILE_NAME);
			if (configFile.exists()) {
				Log.i("Jeffy", "Use extra config file:"
						+ EXTRA_CONFIG_FILE_NAME);
				if (InitTestData(new FileInputStream(configFile))) {
					return true;
				}
			}

			// is = this.openFileInput(strXmlPath);
			is = getAssets().open(CONFIG_FILE_NAME);

			try {
				xmldoc = new XmlDeal(is);
			} catch (Exception e) {
				Log.e(TAG, "parse the xmlfile is fail");
				return false;
			}
		} catch (IOException e) {

			e.printStackTrace();
			Log.e(TAG, "read the xmlfile is fail" + e.getMessage());
			// ForwardErrorActive();
			return false;
		}

		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;

	}

	private Resources mRes;
	private StorageManager mStorageManager = null;

	/******************************************************************
	 * about DeviceStorage()
	 */
	private void InitStorage() {
		Log.v(TAG, ">>>>>>>>> InitStorage : ");
		if (mStorageManager == null) {
			mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
			storageVolumes = mStorageManager.getVolumeList();
			Log.v(TAG, ">>>>>>>>> InitStorage  storageVolumes : "
					+ storageVolumes);
			if (storageVolumes.length >= 3) {
				flash_path = storageVolumes[flash_pit].getPath();
				sdcard_path = storageVolumes[sdcard_pit].getPath();
				sdcard2_path = storageVolumes[sdcard2_pit].getPath();
				usb_path = storageVolumes[usb_pit].getPath();
				Log.d(TAG, " >>>>>>> _____ " + flash_path + "   " + sdcard_path
						+ "   " + sdcard2_path + "   " + usb_path);
			} else if (storageVolumes.length == 2) {
				sdcard_path = storageVolumes[flash_pit].getPath();
				usb_path = storageVolumes[sdcard_pit].getPath();
			}
		}
	}

	// ******************************************************************8
	// 静态方法 锁定屏幕
	public static int lockScreenOrientation(Activity mActivity) {
		Configuration config = mActivity.getResources().getConfiguration();

		int mHardwareRotation = 90;// SystemProperties.getInt("ro.sf.hwrotation",0);

		Log.v("", ">>>>>>>>>>>> config orientation : " + config.orientation
				+ " , mHardwareRotation : " + mHardwareRotation);

		switch (mHardwareRotation) {
		case 0:
			mActivity
					.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			break;
		case 90:
			mActivity
					.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			break;
		// case 180:
		// mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
		// break;
		// case 270:
		// mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
		// break;
		default:
			mActivity
					.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			break;
		}

		/*
		 * if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
		 * Log.d(TAG, ">>>>>>>>>> lock orientation to landscape");
		 * mActivity.setRequestedOrientation
		 * (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); } else { Log.d(TAG,
		 * ">>>>>>>>>> lock orientation to portrait");
		 * mActivity.setRequestedOrientation
		 * (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); }
		 */

		return mHardwareRotation;
	}

	// 取消返回按钮
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, delete_item, delete_item, R.string.item_delete);
		menu.add(0, save_item, save_item, R.string.item_save);
		menu.add(0, view_item, view_item, R.string.item_view);
		menu.add(0, extgps_item, extgps_item, R.string.item_extgps);
		menu.add(0, dsacopy_item, dsacopy_item, R.string.item_dsa_copy);
		menu.add(0, autonavi_item, autonavi_item, R.string.item_autonavi_copy);

		menu.getItem(menu.size() - 3).setShowAsAction(
				MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.getItem(menu.size() - 2).setShowAsAction(
				MenuItem.SHOW_AS_ACTION_ALWAYS);
		menu.getItem(menu.size() - 1).setShowAsAction(
				MenuItem.SHOW_AS_ACTION_ALWAYS);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		if (item.getItemId() == delete_item) {
			Log.v("zxx", ">>>>>>>>>>>>delete");
			if (myFileControl.isSdExist()) {
				myFileControl.deleteSaveData();
			}
		} else if (item.getItemId() == save_item) {
			Log.v("zxx", ">>>>>>>>>>>>save");
			if (myFileControl.isSdExist()) {
				String myContent = "";
				for (int i = 0; i < DeviceTestApplication.myItems.length; i++) {
					myContent += formatResult(DeviceTestApplication.myItems[i],
							DeviceTestApplication.myPrefs.getString("my" + i,
									"NOTEST"));
				}
				myFileControl.WriteToFile(myContent, "latest_result.txt");

				File fromFile = new File(myFileControl.FROMFILE_DIR);
				if (fromFile.exists()) {
					myFileControl.copyDataFile(false);
				}/*
				 * else { Toast.makeText(mContext, R.string.not_test,
				 * Toast.LENGTH_LONG).show(); }
				 */
				String out_success = mContext.getResources().getString(
						R.string.out_success,
						myFileControl.getSDPath() + "/"
								+ myFileControl.SAVEFILE_DIR);
				Toast.makeText(mContext, out_success, Toast.LENGTH_LONG).show();
			}
		} else if (item.getItemId() == view_item) {
			/*
			 * File myDir=new File(myFileControl.TESTSAVE_DIR); if
			 * (!myDir.exists()) { Toast.makeText(mContext,
			 * R.string.not_test_save, Toast.LENGTH_LONG).show(); }else { if
			 * ((myDir.list()).length==0) { Toast.makeText(mContext,
			 * R.string.not_test_save, Toast.LENGTH_LONG).show(); }else {
			 */
			/*
			 * Intent intent=new Intent(mContext,ViewFileActivity.class);
			 * startActivity(intent); overridePendingTransition(0,0);
			 */
			// }
			// }
			Intent intent = new Intent(mContext, ViewDetailActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("fileName", "latest_result.txt");
			intent.putExtras(bundle);
			startActivity(intent);
			overridePendingTransition(0, 0);

		} else if (item.getItemId() == extgps_item) {
			try {
				ComponentName componentExtGps = new ComponentName(
						"com.chartcross.gpstest",
						"com.chartcross.gpstest.GPSTest");
				Intent intentExtGps = new Intent();
				intentExtGps.setComponent(componentExtGps);
				startActivity(intentExtGps);
				overridePendingTransition(0, 0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (item.getItemId() == dsacopy_item) {
			Intent intent = new Intent(mContext, DSACopyActivity.class);
			startActivity(intent);
			overridePendingTransition(0, 0);
		} else if (item.getItemId() == autonavi_item) {
			Intent intent = new Intent(mContext, AutonaviCopyActivity.class);
			startActivity(intent);
			overridePendingTransition(0, 0);
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.v("zxx", ">>>>>>>>destroy");
		// SystemProperties.set("app.test_save.start","1");
	}

	private static String formatResult(String testName, String result) {

		Log.v("zxx", ">>>>>>testName=" + testName + ">>>>>>>>result.name()="
				+ result);
		if (result.compareTo("UNDEF") == 0) {
			result = "NOTEST";
		}
		if (result.compareTo("NG") == 0) {
			result = "FAIL";
		}
		return "[" + testName + "]" + "      " + result + "\n\n";

	}
}
