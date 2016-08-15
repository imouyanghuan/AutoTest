package com.tchip.autotest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.tchip.autotest.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class DSACopyActivity extends Activity {

	private final static String pathFrom1 = "/storage/sdcard1/dsa/";
	private final static String pathFrom2 = "/storage/sdcard2/dsa/";
	private final static String pathTo = "/storage/sdcard0/dsa";
	private int pathWhich = 1;

	private RelativeLayout layoutCoping;
	private Button btnFromSD1, btnFromSD2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dsa_copy);

		layoutCoping = (RelativeLayout) findViewById(R.id.layoutCoping);
		layoutCoping.setVisibility(View.INVISIBLE);

		btnFromSD1 = (Button) findViewById(R.id.btnFromSD1);
		btnFromSD1.setOnClickListener(new MyOnClickListener());

		btnFromSD2 = (Button) findViewById(R.id.btnFromSD2);
		btnFromSD2.setOnClickListener(new MyOnClickListener());
	}

	class MyOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnFromSD1:
				layoutCoping.setVisibility(View.VISIBLE);
				pathWhich = 1;
				new Thread(new CopyThread()).start();
				break;

			case R.id.btnFromSD2:
				layoutCoping.setVisibility(View.VISIBLE);
				pathWhich = 2;
				new Thread(new CopyThread()).start();
				break;

			default:
				break;
			}

		}

	}

	public class CopyThread implements Runnable {

		@Override
		public void run() {
			boolean isSuccess = false;
			if (pathWhich == 2) {
				isSuccess = copyFolder(pathFrom2, pathTo);
			} else {
				isSuccess = copyFolder(pathFrom1, pathTo);
			}

			Message messageResult = new Message();
			messageResult.what = isSuccess ? 1 : 0;
			copyHandler.sendMessage(messageResult);
		}
	}

	final Handler copyHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Toast.makeText(DSACopyActivity.this,
						getResources().getString(R.string.dsa_success),
						Toast.LENGTH_SHORT).show();
				layoutCoping.setVisibility(View.INVISIBLE);
				break;

			case 0:
			default:
				Toast.makeText(DSACopyActivity.this,
						getResources().getString(R.string.dsa_fail),
						Toast.LENGTH_SHORT).show();
				layoutCoping.setVisibility(View.INVISIBLE);
				break;
			}
			super.handleMessage(msg);
		}
	};

	/**
	 * 复制整个文件夹内容
	 * 
	 * @param oldPath
	 *            String 原文件路径
	 * @param newPath
	 *            String 复制后路径
	 * @return boolean
	 */
	public boolean copyFolder(String oldPath, String newPath) {
		boolean isok = true;

		try {
			(new File(newPath)).mkdirs(); // 如果文件夹不存在 则建立新文件夹
			File a = new File(oldPath);
			String[] file = a.list();

			File temp = null;
			for (int i = 0; i < file.length; i++) {
				if (oldPath.endsWith(File.separator)) {
					temp = new File(oldPath + file[i]);
				} else {
					temp = new File(oldPath + File.separator + file[i]);
				}

				if (temp.isFile()) {
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath
							+ "/" + (temp.getName()).toString());
					byte[] b = new byte[1024 * 5];
					int len;
					while ((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory()) {// 如果是子文件夹
					copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
				}
			}

			// 更新Media Database
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
					Uri.parse("file://" + pathTo)));
		} catch (Exception e) {
			MyLog.e("Copy DSA form SD " + oldPath + " Error:" + e.toString());
			isok = false;
		}

		return isok;
	}

}
