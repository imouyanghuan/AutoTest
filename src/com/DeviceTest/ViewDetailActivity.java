package com.DeviceTest;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class ViewDetailActivity extends Activity {
	Context mContext;
	TextView myContentView;
	FileControl myFileControl = null;
	String myContent = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DeviceTest.lockScreenOrientation(this);
		setContentView(R.layout.view_detail);
		// getActionBar().setDisplayHomeAsUpEnabled(true);
		mContext = this;
		myContentView = (TextView) findViewById(R.id.view_content);
		myFileControl = new FileControl(mContext);
		Bundle bundle = this.getIntent().getExtras();
		String fileName = bundle.getString("fileName");
		setTitle(fileName);
		if ("latest_result.txt".equals(fileName.trim())) {
			Log.v("zxx", ">>>>>>>>>latest file");
			myContent = "";
			for (int i = 0; i < DeviceTestApplication.myItems.length; i++) {
				myContent += formatResult(DeviceTestApplication.myItems[i],
						DeviceTestApplication.myPrefs.getString("my" + i,
								"NOTEST"));
			}
			myContentView.setText(myContent);
		} else {
			Log.v("zxx", ">>>>>>>>>file");
			String saveFileStr = myFileControl.TESTSAVE_DIR + "/" + fileName;
			myContent = myFileControl.ReadTxtFile(saveFileStr.trim());
			Log.v("zxxview", ">>>fileName=" + fileName + ">>>>>>saveFileStr="
					+ saveFileStr + ">>>>>>>>>>>>>myContent=" + myContent);
			myContentView.setText(myContent);
		}

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
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
