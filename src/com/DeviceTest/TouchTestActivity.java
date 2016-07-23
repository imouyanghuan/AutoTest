package com.DeviceTest;

import static android.view.WindowManager.LayoutParams.*;

import com.DeviceTest.helper.ControlButtonUtil;
import com.DeviceTest.view.PointerLocationView;
import com.DeviceTest.view.PointerLocationView.OnPointCountChangeListener;
import com.DeviceTest.view.TouchView;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.util.Log;

public class TouchTestActivity extends Activity implements OnClickListener {
	TextView mText;
	TextView mTitle;
	PointerLocationView mPointerView;
	private Button passButton;

	private LinearLayout btns;
	private TouchView mTouchView;
	private Button enterTestTouchcell;
	private Button enterTestMaxpoint;
	private Button reset;
	private RelativeLayout testTouchcell;
	private RelativeLayout testMaxpoint;

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		DeviceTest.lockScreenOrientation(this);
		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
				+ ")");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.touchtest);

		btns = (LinearLayout) findViewById(R.id.btns);
		mTouchView = (TouchView) findViewById(R.id.touchview);
		enterTestTouchcell = (Button) findViewById(R.id.enter_test_touchcell);
		enterTestMaxpoint = (Button) findViewById(R.id.enter_test_maxpoint);
		reset = (Button) findViewById(R.id.reset);
		testTouchcell = (RelativeLayout) findViewById(R.id.test_touchcell);
		testMaxpoint = (RelativeLayout) findViewById(R.id.test_maxpoint);

		enterTestTouchcell.setOnClickListener(this);
		enterTestMaxpoint.setOnClickListener(this);
		reset.setOnClickListener(this);
		mTouchView.setBtnsLinearLayout(btns);

		mPointerView = (PointerLocationView) findViewById(R.id.pointerview);
		mPointerView.setBackgroundColor(Color.TRANSPARENT);

		mPointerView
				.setOnPointCountChangeListener(new OnPointCountChangeListener() {

					public void onPointCountChange(int newPointCount) {
						Log.i("Jeffy", "Count:" + newPointCount);
						if (newPointCount >= 20) {
							// passButton.setVisibility(View.VISIBLE);
							passButton.performClick();
						}
					}
				});

		ControlButtonUtil.initControlButtonView(this);
		// passButton = (Button) findViewById(R.id.btn_Pass);
		// passButton.setVisibility(View.INVISIBLE);
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}

	public void onClick(View v) {
		if (v == reset) {
			mTouchView.reset();
		} else if (v == enterTestTouchcell) {
			testTouchcell.setVisibility(View.VISIBLE);
			testMaxpoint.setVisibility(View.GONE);
		} else if (v == enterTestMaxpoint) {
			testTouchcell.setVisibility(View.GONE);
			testMaxpoint.setVisibility(View.VISIBLE);
		}
	}

}
