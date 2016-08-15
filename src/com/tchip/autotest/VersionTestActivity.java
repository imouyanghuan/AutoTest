package com.tchip.autotest;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.os.StatFs;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.ITelephony;
import com.tchip.autotest.helper.ControlButtonUtil;

import android.os.ServiceManager;
import android.os.Build.VERSION_CODES;
import android.annotation.TargetApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tchip.autotest.R;

import android.os.SystemProperties;

import android.text.TextUtils;
//import com.mediatek.common.featureoption.FeatureOption;
//chang by xinw
//import com.mediatek.common.telephony.ITelephonyEx;
import android.os.ServiceManager;
import android.os.IBinder;

public class VersionTestActivity extends Activity {
	private Phone phone;

	/** M: Add MTK TelephonyManagerEx */

	private String getFormattedKernelVersion() {
		String procVersionStr;

		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					"/proc/version"), 256);
			try {
				procVersionStr = reader.readLine();
			} finally {
				reader.close();
			}

			final String PROC_VERSION_REGEX = "\\w+\\s+" + /* ignore: Linux */
			"\\w+\\s+" + /* ignore: version */
			"([^\\s]+)\\s+" + /* group 1: 2.6.22-omap1 */
			"\\(([^\\s@]+(?:@[^\\s.]+)?)[^)]*\\)\\s+" + /*
														 * group 2:
														 * (xxxxxx@xxxxx
														 * .constant)
														 */
			"\\((?:[^(]*\\([^)]*\\))?[^)]*\\)\\s+" + /* ignore: (gcc ..) */
			"([^\\s]+)\\s+" + /* group 3: #26 */
			"(?:PREEMPT\\s+)?" + /* ignore: PREEMPT (optional) */
			"(.+)"; /* group 4: date */

			Pattern p = Pattern.compile(PROC_VERSION_REGEX);
			Matcher m = p.matcher(procVersionStr);

			if (!m.matches()) {

				return "Unavailable";
			} else if (m.groupCount() < 4) {

				return "Unavailable";
			} else {
				return (new StringBuilder(m.group(1)).append("\n")
						.append(m.group(2)).append(" ").append(m.group(3))
						.append("\n").append(m.group(4))).toString();
			}
		} catch (IOException e) {

			return "Unavailable";
		}
	}

	@Override
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		DeviceTest.lockScreenOrientation(this);
		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
				+ ")");

		setContentView(R.layout.versiontest);

		TextView ClientVersion = (TextView) findViewById(R.id.ClientVersion);
		ClientVersion.setText(getClientVersion());

		TextView firmwareTextView = (TextView) findViewById(R.id.TextFirmwareversion);
		firmwareTextView.setText(Build.VERSION.RELEASE);

		TextView kernelTextView = (TextView) findViewById(R.id.TextKernelversion);
		kernelTextView.setText(getFormattedKernelVersion());

		TextView buildTextView = (TextView) findViewById(R.id.TextBuildversion);
		buildTextView.setText(Build.DISPLAY);

		TextView localTextView5 = (TextView) findViewById(R.id.TextBasebandversion);
		String str4 = SystemProperties.get("gsm.version.baseband",
				"Unavailable");
		localTextView5.setText(str4);

		// checkFlashSize();

		ControlButtonUtil.initControlButtonView(this);

		// /////////////////////// add by zzp
		// ////////////////////////////////////
		TextView snVersion = (TextView) findViewById(R.id.sn_version);
		TextView imeiVersion = (TextView) findViewById(R.id.imei_version);
		TextView calibrationStatus = (TextView) findViewById(R.id.calibration_status);
		TextView finalStatus = (TextView) findViewById(R.id.final_status);

		TelephonyManager tm = (TelephonyManager) this
				.getSystemService(TELEPHONY_SERVICE);
		String imei = tm.getDeviceId();
		imeiVersion.setText(imei);

		if (TextUtils.isEmpty(imei)) {
			imeiVersion.setText(getString(R.string.set_imei));
		}
		// chang by xinw
		String sn = "";
		String barcode_string = "";
//		try {
			// ITelephonyEx mTelEx = ITelephonyEx.Stub
			// .asInterface(android.os.ServiceManager
			// .getService("phoneEx"));
			// sn = mTelEx.getSerialNumber();
			// String sn = Build.SERIAL;
			barcode_string = sn;
			Log.i("xinw", "sn:" + sn);
			Log.i("xinw", "barcode_string:" + barcode_string);
//		} catch (RemoteException e) {
//		}
		// ///////////////////////////////////////////////////////////////////////
		snVersion.setText(sn);// tm.getSN());
		/*
		 * // 解决测试信号中断为空 Log.v("",">>>>>>>>>>>>>>>>>>> getSN :  " + getSN());
		 * try { Log.v("",">>>>>>>>>>>>>>>>>>>  getITelephony().getSN :  " +
		 * getITelephony().getSN());
		 * //snVersion.append("\n"+getITelephony().getSN()); } catch
		 * (RemoteException ex) { // the phone process is restarting. } catch
		 * (NullPointerException ex) { }
		 */

//		Log.v("",
//				">>>>>>>>>>>>>>>>>>> TelephonyManager  getSN :  " + tm.getSN());

		String barcode = barcode_string;// tm.getSN();
		if (null != barcode) {
			Log.v("", ">>>>>>> barcode : " + barcode.length());
			calibrationStatus.setText(getCalibrationFlag(barcode) ? "Pass"
					: "Fail");
			finalStatus.setText(getFinalStatus(barcode) ? "Pass" : "Fail");
		} else {
			Log.v("zzp", ">>>>>>>get  barcode  faile ");
			calibrationStatus.setText("false");
			finalStatus.setText("false");

		}
	}

	private String getClientVersion() {
		String version = SystemProperties.get("ro.esky.build.version");
		if (TextUtils.isEmpty(version))
			version = SystemProperties.get("ro.custom.build.version");
		return version;
	}

//	private String getSN() {
//		PhoneFactory.makeDefaultPhones(this);
//		// Get the default phone
//		phone = PhoneFactory.getDefaultPhone();
//
//		return phone.getSN();
//	}

	private ITelephony getITelephony() {
		return ITelephony.Stub.asInterface(ServiceManager
				.getService(Context.TELEPHONY_SERVICE));
	}

	private boolean getCalibrationFlag(String barcode) {
		if (barcode.length() < 62)
			return false;
		else {

			char[] barcodeArr = barcode.toCharArray();

			Log.v("", ">>>>>>> barcode 60 : " + barcodeArr[60]
					+ " >>>>> barcode 61 : " + barcodeArr[61]);

			if (barcodeArr[60] == '1' && barcodeArr[61] == '0')
				return true;
			else
				return false;
		}
	}

	private boolean getFinalStatus(String barcode) {
		if (barcode.length() < 63)
			return false;
		else {
			char[] barcodeArr = barcode.toCharArray();
			Log.v("", ">>>>>>> barcode 62 : " + barcodeArr[62]);
			if (barcodeArr[62] == 'P')
				return true;
			else
				return false;
		}
	}

	// ///////////////////////////////////////////////////////////////

	/*
	 * Flash test Used for mid
	 * 
	 * public void checkFlashSize() { TextView flashsizeView =
	 * (TextView)findViewById(R.id.FlashSize);
	 * 
	 * String path = getDirectory("FLASH_STORAGE", "/flash").getPath(); StatFs
	 * stat = new StatFs(path); long blockSize = stat.getBlockSize(); long
	 * availableBlocks = stat.getAvailableBlocks(); long availableSize =
	 * (availableBlocks * blockSize)/(1024 * 1024); //MBtye long freeBlocks =
	 * stat.getFreeBlocks(); long freeSize = (freeBlocks * blockSize)/(1024 *
	 * 1024); //MBtye
	 * 
	 * flashsizeView.setText(String.valueOf(availableSize)+" MB"); }
	 * 
	 * static File getDirectory(String variableName, String defaultPath) {
	 * String path = System.getenv(variableName); return path == null ? new
	 * File(defaultPath) : new File(path); }
	 */

	// ////////////////////
	// //// Get IMEI //////
	// ////////////////////
	private static final int EVENT_WRITE_IMEI = 7;
	private int IMEI_DIGITS = 14;
	private String[] mRandomImeiNum = new String[IMEI_DIGITS];
	private Phone mPhone = null;

	private String[] SetImeiNumber() {
		String imeivalue = "";
		String imeiString[] = { "AT+EGMR=1,", "" };
		Random r = new Random();
		for (int i = 0; i < IMEI_DIGITS; i++) {
			mRandomImeiNum[i] = String.valueOf(r.nextInt(10));
		}
		String tt = "";
		for (int i = 0; i < IMEI_DIGITS; i++) {
			imeivalue += mRandomImeiNum[i];

		}
		imeivalue += "0";

		// String imeiString[] = { "AT+EGMR=1,", "" };
		// if (FeatureOption.MTK_GEMINI_SUPPORT) {
		// int simId = mPhone.getMySimId();
		// if (simId == PhoneConstants.GEMINI_SIM_1) {
		// imeiString[0] = "AT+EGMR=1,7,\"" + imeivalue + "\"";
		// } else if (simId == PhoneConstants.GEMINI_SIM_2) {
		// imeiString[0] = "AT+EGMR=1,10,\"" + imeivalue + "\"";
		// }
		// } else {
		// imeiString[0] = "AT+EGMR=1,7,\"" + imeivalue + "\"";
		// }
		Log.v("ZZP", "IMEI String:" + imeiString[0] + imeiString[1]);
		return imeiString;
	}
}
