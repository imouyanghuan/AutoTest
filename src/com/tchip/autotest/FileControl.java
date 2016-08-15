package com.tchip.autotest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import java.io.RandomAccessFile;

import com.tchip.autotest.R;

public class FileControl {
	Context mContext;
	boolean sdCardExist;
	final public String SAVEFILE_DIR = ".DeviceTestResult";
	final public String FROMFILE_DIR = "/system/usr/data/DeviceTestResult";
	public final String TESTSAVE_DIR = "/system/usr/data/DeviceTestResult";

	public FileControl(Context context) {

		this.mContext = context;

	}

	public String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED); // 判断sd卡是否存在

		if (sdCardExist) // 如果SD卡存在，则获取跟目录
		{
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
			return sdDir.toString();
		} else {
			return "";
		}

	}

	public boolean isSdExist() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			this.sdCardExist = true;
			return true;
		} else {
			this.sdCardExist = false;
			Toast.makeText(mContext, R.string.not_sd, Toast.LENGTH_LONG).show();
			return false;
		}
	}

	public static String createLogFilename() { // 以当前时间创建保存文件名
		Date date = new Date();
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);

		DecimalFormat twoDigitDecimalFormat = new DecimalFormat("00");
		DecimalFormat fourDigitDecimalFormat = new DecimalFormat("0000");

		String year = fourDigitDecimalFormat
				.format(calendar.get(Calendar.YEAR));
		String month = twoDigitDecimalFormat.format(calendar
				.get(Calendar.MONTH) + 1);
		String day = twoDigitDecimalFormat.format(calendar
				.get(Calendar.DAY_OF_MONTH));
		String hour = twoDigitDecimalFormat.format(calendar
				.get(Calendar.HOUR_OF_DAY));
		String minute = twoDigitDecimalFormat.format(calendar
				.get(Calendar.MINUTE));
		String second = twoDigitDecimalFormat.format(calendar
				.get(Calendar.SECOND));

		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append(year).append("-").append(month).append("-")
				.append(day).append("-").append(hour).append("-")
				.append(minute).append("-").append(second).append("-full");

		stringBuilder.append(".txt");

		return stringBuilder.toString();
	}

	public void copyDataFile(boolean isforce) {

		// String SAVE_NAME =createLogFilename() ;
		String SDCARD_DIR = getSDPath() + "/";
		String TOFILE_PATH = SDCARD_DIR + SAVEFILE_DIR;

		Log.v("zxxcopy", ">>>>>>>DATABASES_DIR=" + SDCARD_DIR);
		File fromDir = new File(FROMFILE_DIR);
		String[] files = fromDir.list();
		if (files.length != 0) {
			for (int i = 0; i < files.length; i++) {
				copyFileToSd(FROMFILE_DIR, TOFILE_PATH, files[i], isforce);
			}
		}
		/*
		 * String
		 * out_success=mContext.getResources().getString(R.string.out_success,
		 * TOFILE_PATH); Toast.makeText(mContext, out_success,
		 * Toast.LENGTH_LONG).show();
		 */

	}

	public void copyFileToSd(String fromFilePath, String toFilePath,
			String fileName, boolean isforce) {

		File fromFile = new File(fromFilePath, fileName);
		File dir = new File(toFilePath);
		if (!dir.exists()) {
			try {
				dir.mkdir();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		File dest = new File(dir, fileName);
		if (dest.exists() && !isforce) {
			return;
		}

		try {
			/*
			 * if (dest.exists()) { dest.delete(); }
			 */
			dest.createNewFile();
			InputStream in = new FileInputStream(fromFile);
			int size = in.available();
			byte buf[] = new byte[size];
			in.read(buf);
			in.close();
			FileOutputStream out = new FileOutputStream(dest);
			out.write(buf);
			out.close();
			// Toast.makeText(mContext, R.string.out_success,
			// Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			e.printStackTrace();
			// Toast.makeText(mContext, R.string.out_fail_right,
			// Toast.LENGTH_LONG).show();
			Log.v("zxxcontrol", "copyfile>>>>>>e=" + e);
		}

	}

	public void deleteSaveData() {

		String SAVEFILE_PATH = getSDPath() + "/" + SAVEFILE_DIR;
		File saveFile = new File(SAVEFILE_PATH);
		if (saveFile.exists()) {
			RecursionDeleteFile(saveFile);
			Toast.makeText(mContext, R.string.delete_success, Toast.LENGTH_LONG)
					.show();
		} else {
			Toast.makeText(mContext, R.string.not_save, Toast.LENGTH_LONG)
					.show();
		}

	}

	public static void RecursionDeleteFile(File file) {
		if (file.isFile()) {
			file.delete();
			return;
		}
		if (file.isDirectory()) {
			File[] childFile = file.listFiles();
			if (childFile == null || childFile.length == 0) {
				file.delete();
				return;
			}
			for (File f : childFile) {
				RecursionDeleteFile(f);
			}
			file.delete();
		}
	}

	// 读取文本文件中的内容
	public String ReadTxtFile(String strFilePath) {
		String path = strFilePath;
		String content = ""; // 文件内容字符串
		// 打开文件
		File file = new File(path);
		// 如果path是传递过来的参数，可以做一个非目录的判断
		if (file.isDirectory()) {
			Log.v("zxxcontrol", "The File doesn't not exist.");
		} else {
			try {
				InputStream instream = new FileInputStream(file);
				if (instream != null) {
					InputStreamReader inputreader = new InputStreamReader(
							instream);
					BufferedReader buffreader = new BufferedReader(inputreader);
					String line;
					// 分行读取
					while ((line = buffreader.readLine()) != null) {
						Log.v("zxxcontrol", ">>>>" + line); // 空的也都读出来
						content += line + "\n";
						// break;

					}
					instream.close();
				}
			} catch (java.io.FileNotFoundException e) {
				Log.v("zxxcontrol", "The File doesn't not exist.");
			} catch (IOException e) {
				Log.v("zxxcontrol", "readtext>>>>>e=" + e.getMessage());
			}
		}
		return content;
	}

	// 将字符串写入到文本文件中
	public void WriteToFile(String strcontent, String fileName) {
		String SAVEFILE_PATH = getSDPath() + "/" + SAVEFILE_DIR;
		File saveDir = new File(SAVEFILE_PATH);
		if (!saveDir.exists()) {
			saveDir.mkdir();
		}
		String strFilePath = SAVEFILE_PATH + "/" + fileName;
		String strContent = strcontent;
		try {
			File file = new File(strFilePath);
			if (file.exists()) {
				file.delete();
			}
			Log.v("zxxwrite", "Create the file:" + strFilePath);
			file.createNewFile();
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			raf.seek(file.length());
			// Log.v("zxx", ">>>>>>>>length="+file.length());
			raf.write(strContent.getBytes());
			raf.close();
		} catch (Exception e) {
			Log.v("zxxwrite", "Error on write File.  e=" + e);
		}
	}

}
