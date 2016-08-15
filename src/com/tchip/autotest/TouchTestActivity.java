package com.tchip.autotest;

import static android.view.WindowManager.LayoutParams.*;

import com.tchip.autotest.R;
import com.tchip.autotest.helper.ControlButtonUtil;
import com.tchip.autotest.view.PointerLocationView;
import com.tchip.autotest.view.TouchView;
import com.tchip.autotest.view.PointerLocationView.OnPointCountChangeListener;
import com.tchip.autotest.view.TouchView.OnRectangleChangeListener;

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
import android.widget.Toast;
import android.util.Log;

public class TouchTestActivity extends Activity implements OnClickListener {
	TextView mText;
	TextView mTitle;

	/** Multiple touch */
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
		// DeviceTest.lockScreenOrientation(this);
		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
				+ ")");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.touchtest);

		btns = (LinearLayout) findViewById(R.id.btns);
		btns.setVisibility(View.GONE);
		mTouchView = (TouchView) findViewById(R.id.touchview);
		mTouchView
				.setOnRectangleChangeListener(new OnRectangleChangeListener() {

					@Override
					public void onRectangleChange(int newRectangleCount) {
						Log.v("AZ", "countTouchRectangle:" + newRectangleCount);
						if (36 == newRectangleCount) { // Finish Rectangle Test,
														// Jump to Multiple
														// touch
							testTouchcell.setVisibility(View.GONE);
							testMaxpoint.setVisibility(View.VISIBLE);
							btns.setVisibility(View.VISIBLE);
						}
					}
				});
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

						if (newPointCount >= 5) {
							Toast.makeText(
									TouchTestActivity.this,
									getResources().getString(
											R.string.auto_touch_success),
									Toast.LENGTH_SHORT).show();
							ControlButtonUtil
									.autoVerifyPass(TouchTestActivity.this);
						}
					}
				});

		ControlButtonUtil.initControlButtonView(this);
		findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
		findViewById(R.id.btn_Fail).setVisibility(View.INVISIBLE);
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
