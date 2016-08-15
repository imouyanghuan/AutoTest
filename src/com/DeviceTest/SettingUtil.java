package com.DeviceTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import com.DeviceTest.util.ProviderUtil;
import com.DeviceTest.util.ProviderUtil.Name;

import android.content.Context;
import android.content.Intent;

public class SettingUtil {

	/**
	 * FM Transmit Enable Node
	 * 
	 * 1：Open 0：Off
	 */
	public static File nodeFmEnable = new File(
			"/sys/bus/i2c/devices/2-002c/enable_qn8027");

	/**
	 * FM Transmit Frequency
	 * 
	 * Frequency：7600~10800:8750-10800
	 */
	public static File nodeFmChannel = new File(
			"/sys/bus/i2c/devices/2-002c/setch_qn8027");

	/**
	 * FM发射是否打开:Config
	 * 
	 * @return
	 * @deprecated Using {@link #isFmTransmitPowerOn()} instead.
	 */
	public static boolean isFmTransmitConfigOn(Context context) {
		String strFmEnable = ProviderUtil.getValue(context,
				Name.FM_TRANSMIT_STATE, "0");
		if ("1".equals(strFmEnable)) {
			return true;
		} else {
			return false;
		}
	}

	public static void setFmTransmitConfigOn(Context context, boolean isOn) {
		ProviderUtil
				.setValue(context, Name.FM_TRANSMIT_STATE, isOn ? "1" : "0");
	}

	/**
	 * FM发射是否打开:Power
	 * 
	 * @return
	 */
	public static boolean isFmTransmitOnNode() {
		return getFileInt(nodeFmEnable) == 1;
	}

	public static int getFileInt(File file) {
		if (file.exists()) {
			try {
				InputStream inputStream = new FileInputStream(file);
				InputStreamReader inputStreamReader = new InputStreamReader(
						inputStream);
				int ch = 0;
				if ((ch = inputStreamReader.read()) != -1) {
					inputStreamReader.close();
					inputStream.close();
					return Integer.parseInt(String.valueOf((char) ch));
				} else {
					inputStreamReader.close();
					inputStream.close();
				}
			} catch (FileNotFoundException e) {
				MyLog.e("[AutoTest.SettintUtil.getFileInt] FileNotFoundException:"
						+ e.toString());
				e.printStackTrace();
			} catch (IOException e) {
				MyLog.e("[AutoTest.SettintUtil.getFileInt] IOException:"
						+ e.toString());
				e.printStackTrace();
			}
		}
		return 0;
	}

	public static void setFmTransmitPowerOn(Context context, boolean isOn) {
		SettingUtil
				.SaveFileToNode(SettingUtil.nodeFmEnable, (isOn ? "1" : "0"));
		MyLog.v("[SettingUtil]setFmTransmitPowerOn:" + isOn);
		context.sendBroadcast(new Intent(isOn ? "tchip.intent.action.FM_ON"
				: "tchip.intent.action.FM_OFF"));
	}

	public static void SaveFileToNode(File file, String value) {
		if (file.exists()) {
			try {
				StringBuffer strbuf = new StringBuffer("");
				strbuf.append(value);
				OutputStream output = null;
				OutputStreamWriter outputWrite = null;
				PrintWriter print = null;
				try {
					output = new FileOutputStream(file);
					outputWrite = new OutputStreamWriter(output);
					print = new PrintWriter(outputWrite);
					print.print(strbuf.toString());
					print.flush();
					output.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
			}
		} else {
		}
	}

	/**
	 * 获取设置中存取的频率
	 * 
	 * @return 8750-10800
	 */
	public static int getFmFrequcenyNode(Context context) {
		int fmFreqency = 9600; // 8800; // Default
		String strNodeFmChannel = "";
		if (nodeFmChannel.exists()) {
			try {
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(nodeFmChannel), "utf-8");
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					strNodeFmChannel += lineTxt.toString();
				}
				read.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				MyLog.e("SettingUtil.getLCDValue: FileNotFoundException");
			} catch (IOException e) {
				e.printStackTrace();
				MyLog.e("SettingUtil.getLCDValue: IOException");
			}
		}
		// ProviderUtil.setValue(context, Name.FM_TRANSMIT_FREQ,
		// strNodeFmChannel);
		fmFreqency = Integer.parseInt(strNodeFmChannel);

		MyLog.v("SettingUtil.getFmFrequcenyNode,fmFreqency:" + fmFreqency);
		return fmFreqency;
	}

	/**
	 * 设置FM发射频率:8750-10800
	 * 
	 * @param frequency
	 */
	public static void setFmFrequencyNode(Context context, int frequency) {
		if (frequency >= 8750 && frequency <= 10800) {
			SaveFileToNode(nodeFmChannel, String.valueOf(frequency));
			MyLog.v("SettingUtil.setFmFrequencyNode success:" + frequency
					/ 100.0f + "MHz");
		}
	}

	/**
	 * @return 8750-10800
	 * 
	 * @deprecated
	 */
	public static int getFmFrequencyConfig(Context context) {
		String strFrequency = ProviderUtil.getValue(context,
				Name.FM_TRANSMIT_FREQ, "9600");
		return Integer.parseInt(strFrequency);
	}

	/**
	 * @param context
	 * @param frequency
	 */
	public static void setFmFrequencyConfig(Context context, int frequency) {
		if (frequency >= 8750 && frequency <= 10800) {
			ProviderUtil.setValue(context, Name.FM_TRANSMIT_FREQ, ""
					+ frequency);
			MyLog.v("[SettingUtil]setFmFrequencyConfig success:" + frequency
					/ 100.0f + "MHz");
		}
	}

	/**
	 * Edog Status
	 * 
	 * 1-Open
	 * 
	 * 0-Close
	 */
	public static File fileEDogPower = new File(
			"/sys/bus/i2c/devices/0-007f/EDog_enable");

	/**
	 * Set Edog Status
	 * 
	 * @param isEDogOn
	 */
	public static void setEDogEnable(boolean isEDogOn) {
		if (isEDogOn) {
			SaveFileToNode(fileEDogPower, "1");
		} else {
			SaveFileToNode(fileEDogPower, "0");
		}
	}

}
