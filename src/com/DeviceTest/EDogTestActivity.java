package com.DeviceTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.Queue;

import com.DeviceTest.helper.ControlButtonUtil;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Base64;
import android.widget.EditText;
import android_serialport_api.SerialPortFinder;

public class EDogTestActivity extends Activity {
	EditText editTextRecDisp;
	SerialControl ComA;// 4个串口
	DispQueueThread DispQueue;// 刷新显示线程
	SerialPortFinder mSerialPortFinder;// 串口设备搜索
	AssistBean AssistData;// 用于界面数据序列化和反序列化
	int iRecLines = 0;// 接收区行数

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DeviceTest.lockScreenOrientation(this);
		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
				+ ")");

		setContentView(R.layout.edogtest);

		SettingUtil.setEDogEnable(true);

		ComA = new SerialControl();
		DispQueue = new DispQueueThread();
		DispQueue.start();
		AssistData = getAssistData();
		setControls();

		// Open ComA
		ComA.setPort("/dev/ttyMT3");
		ComA.setBaudRate(9600);
		OpenComPort(ComA);

		ControlButtonUtil.initControlButtonView(this);
	}

	@Override
	public void onDestroy() {
		saveAssistData(AssistData);
		CloseComPort(ComA);
		SettingUtil.setEDogEnable(false);
		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		CloseComPort(ComA);
		setContentView(R.layout.edogtest);
		setControls();
	}

	private void setControls() {
		editTextRecDisp = (EditText) findViewById(R.id.editTextRecDisp);
	}

	// ----------------------------------------------------串口控制类
	private class SerialControl extends SerialHelper {

		public SerialControl() {
		}

		@Override
		protected void onDataReceived(final ComBean ComRecData) {
			// 数据接收量大或接收时弹出软键盘，界面会卡顿,可能和6410的显示性能有关
			// 直接刷新显示，接收数据量大时，卡顿明显，但接收与显示同步。
			// 用线程定时刷新显示可以获得较流畅的显示效果，但是接收数据速度快于显示速度时，显示会滞后。
			// 最终效果差不多-_-，线程定时刷新稍好一些。
			DispQueue.AddQueue(ComRecData);// 线程定时刷新显示(推荐)
			/*
			 * runOnUiThread(new Runnable()//直接刷新显示 { public void run() {
			 * DispRecData(ComRecData); } });
			 */
		}
	}

	// ----------------------------------------------------刷新显示线程
	private class DispQueueThread extends Thread {
		private Queue<ComBean> QueueList = new LinkedList<ComBean>();

		@Override
		public void run() {
			super.run();
			while (!isInterrupted()) {
				final ComBean ComData;
				while ((ComData = QueueList.poll()) != null) {
					runOnUiThread(new Runnable() {
						public void run() {
							DispRecData(ComData);
						}
					});
					try {
						Thread.sleep(100);// 显示性能高的话，可以把此数值调小。
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}

		public synchronized void AddQueue(ComBean ComData) {
			QueueList.add(ComData);
		}
	}

	// ----------------------------------------------------保存、获取界面数据
	private void saveAssistData(AssistBean AssistData) {
		AssistData.sTimeA = "500";// editTextTimeCOMA.getText().toString();
		SharedPreferences msharedPreferences = getSharedPreferences(
				"ComAssistant", Context.MODE_PRIVATE);
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(AssistData);
			String sBase64 = new String(Base64.encode(baos.toByteArray(), 0));
			SharedPreferences.Editor editor = msharedPreferences.edit();
			editor.putString("AssistData", sBase64);
			editor.commit();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ----------------------------------------------------
	private AssistBean getAssistData() {
		SharedPreferences msharedPreferences = getSharedPreferences(
				"ComAssistant", Context.MODE_PRIVATE);
		AssistBean AssistData = new AssistBean();
		try {
			String personBase64 = msharedPreferences
					.getString("AssistData", "");
			byte[] base64Bytes = Base64.decode(personBase64.getBytes(), 0);
			ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			AssistData = (AssistBean) ois.readObject();
			return AssistData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		AssistData.setTxtMode(false);
		return AssistData;
	}

	// ----------------------------------------------------显示接收数据
	private void DispRecData(ComBean ComRecData) {
		StringBuilder sMsg = new StringBuilder();
		sMsg.append(ComRecData.sRecTime);
		sMsg.append("[");
		sMsg.append(ComRecData.sComPort);
		sMsg.append("]");
		sMsg.append("[Hex] ");
		sMsg.append(MyFunc.ByteArrToHex(ComRecData.bRec));
		sMsg.append("\r\n");
		editTextRecDisp.append(sMsg);
		iRecLines++;
		if (iRecLines > 500)// Clear EditText
		{
			editTextRecDisp.setText("");
			iRecLines = 0;
		}
	}

	/**
	 * Close COM Port
	 * 
	 * @param ComPort
	 */
	private void CloseComPort(SerialHelper ComPort) {
		if (ComPort != null) {
			ComPort.stopSend();
			ComPort.close();
		}
	}

	/**
	 * Open COM Port
	 * 
	 * @param ComPort
	 */
	private void OpenComPort(SerialHelper ComPort) {
		try {
			ComPort.open();
		} catch (SecurityException e) {
			// No Permission
		} catch (IOException e) {
		} catch (InvalidParameterException e) {
		}
	}
}