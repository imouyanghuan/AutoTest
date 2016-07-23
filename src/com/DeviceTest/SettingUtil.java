package com.DeviceTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import android.content.Context;
import android.provider.Settings;

public class SettingUtil {

	/**
	 * FM Transmit Enable Node
	 * 
	 * 1：Open 0：Off
	 */
	public static File nodeFmEnable = new File(
			"/sys/devices/platform/mt-i2c.1/i2c-1/1-002c/enable_qn8027");

	/**
	 * FM Transmit Frequency
	 * 
	 * Frequency：7600~10800:8750-10800
	 */
	public static File nodeFmChannel = new File(
			"/sys/devices/platform/mt-i2c.1/i2c-1/1-002c/setch_qn8027");

	public static boolean isFmTransmitOn(Context context) {
		boolean isFmTransmitOpen = false;
		String fmEnable = Settings.System.getString(
				context.getContentResolver(), "fm_transmitter_enable");
		if (fmEnable.trim().length() > 0) {
			if ("1".equals(fmEnable)) {
				isFmTransmitOpen = true;
			} else {
				isFmTransmitOpen = false;
			}
		}
		return isFmTransmitOpen;
	}

	/**
	 * Get FM Transmit Frequency from Setting
	 * 
	 * @return 8750-10800
	 */
	public static int getFmFrequceny(Context context) {
		String fmChannel = Settings.System.getString(
				context.getContentResolver(), "fm_transmitter_channel");

		return Integer.parseInt(fmChannel);
	}

	/**
	 * Set FM Transmit Frequency:8750-10800
	 * 
	 * @param frequency
	 */
	public static void setFmFrequency(Context context, int frequency) {
		if (frequency >= 8750 || frequency <= 10800) {
			Settings.System.putString(context.getContentResolver(),
					"fm_transmitter_channel", "" + frequency);

			SaveFileToNode(nodeFmChannel, String.valueOf(frequency));

		}
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
	 * Edog Status
	 * 
	 * 1-Open
	 * 
	 * 0-Close
	 */
	public static File fileEDogPower = new File(
			"/sys/devices/platform/mt-i2c.1/i2c-1/1-007f/edog_car_status");

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
