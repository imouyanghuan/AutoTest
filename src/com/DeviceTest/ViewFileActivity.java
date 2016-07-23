package com.DeviceTest;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.View;

public class ViewFileActivity extends Activity {
	FileControl myControl = null;
	Context mContext;
	private ArrayList<ViewFileEntry> myFileList = new ArrayList<ViewFileEntry>();
	ListView myList;
	ViewFileAdapter myViewFileAdapter;
	static boolean isFirst = true;
	Button viewLastestBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DeviceTest.lockScreenOrientation(this);
		setContentView(R.layout.view_file_list);
		isFirst = true;
		mContext = this;
		myControl = new FileControl(mContext);
		myList = (ListView) findViewById(R.id.mylistvew);
		readFileName(myControl.TESTSAVE_DIR);
		myViewFileAdapter = new ViewFileAdapter(mContext, myFileList);
		myList.setAdapter(myViewFileAdapter);
		myList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.v("zxxview",
						">>>>>>click fileName="
								+ myFileList.get(position).fileName);
				Intent intent = new Intent(mContext, ViewDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("fileName", myFileList.get(position).fileName);
				intent.putExtras(bundle);
				startActivity(intent);
				overridePendingTransition(0, 0);
			}

		});
		viewLastestBtn = (Button) findViewById(R.id.view_lastest);
		viewLastestBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(mContext, ViewDetailActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("fileName", "latest_result.txt");
				intent.putExtras(bundle);
				startActivity(intent);
				overridePendingTransition(0, 0);

			}
		});

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.v("zxxview", ">>>>resume");
		if (!isFirst) {
			readFileName(myControl.TESTSAVE_DIR);
			myViewFileAdapter = new ViewFileAdapter(mContext, myFileList);
			myList.setAdapter(myViewFileAdapter);
			myViewFileAdapter.notifyDataSetChanged();
		}
		isFirst = false;

	}

	public static class ViewFileEntry implements Serializable {
		public String fileName;

	}

	void readFileName(String saveFileDir) {
		myFileList.clear();
		File myDir = new File(saveFileDir);
		if (myDir.exists()) {
			File[] allFiles = myDir.listFiles();
			if (allFiles.length != 0) {
				// Log.v("zxxview", ">>>>>>>>>>>files="+allFiles.toString());
				for (int i = 0; i < allFiles.length; i++) {
					File myFile = allFiles[i];
					// Log.v("zxxview",
					// ">>>>>>>>>>>fileName="+myFile.toString());
					if (myFile.isFile()) {
						ViewFileEntry myViewFileEntry = new ViewFileEntry();
						myViewFileEntry.fileName = myFile.getName();
						myFileList.add(myViewFileEntry);
					}
				}
			}

		}

	}

}
