package com.DeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import com.DeviceTest.helper.ControlButtonUtil;
import com.DeviceTest.view.LcdTestView;

import android.R.color;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.Layout;
import android.util.Log;

public class LcdTestActivity extends Activity {
	public static final int MSG_LCD_TESTING = 2;
	public static final int MSG_LCD_TEST_START = 1;
	public static final int MSG_LCD_TES_END = 3;

	LcdTestView mLcdView;
	boolean mTestOn;
	TextView mText;
	TextView mTitle;
	LinearLayout accButtons;
	int mTestCount = 0;
	private static final int TEST_COLOR_COUNT = 5;
	private static final int TEST_COLORS[] = new int[TEST_COLOR_COUNT];

	private Canvas mCanvas = new Canvas();
	private Paint mPaint = new Paint();

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		TEST_COLORS[0] = Color.WHITE;
		TEST_COLORS[1] = Color.BLACK;
		TEST_COLORS[2] = Color.RED;
		TEST_COLORS[3] = Color.GREEN;
		TEST_COLORS[4] = Color.BLUE;
		DeviceTest.lockScreenOrientation(this);

		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
				+ ")");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
		getWindow().getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_FULLSCREEN);
		// getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_SHOW_FULLSCREEN);

		setContentView(R.layout.lcdtest);
		accButtons = (LinearLayout) findViewById(R.id.acc_buttons);

		mTitle = (TextView) findViewById(R.id.lcdtextTitle);
		mText = (TextView) findViewById(R.id.lcdtestresult);
		mLcdView = (LcdTestView) findViewById(R.id.lcdtestview);
		ControlButtonUtil.initControlButtonView(this);
		ControlButtonUtil.Hide();
	}

	protected void onPause() {
		super.onPause();
	}

	protected void onResume() {
		super.onResume();
	}

	public boolean onTouchEvent(MotionEvent paramMotionEvent) {
		if (paramMotionEvent.getAction() == MotionEvent.ACTION_DOWN) {
			mTestCount++;

			if (mTestCount >= 1 && mTestCount <= TEST_COLOR_COUNT) {
				mLcdView.setBackgroundColor(TEST_COLORS[mTestCount - 1]);
			}

			switch (mTestCount) {
			case 1:
				accButtons.setVisibility(View.GONE);
				mTitle.setVisibility(View.GONE);
				mText.setVisibility(View.GONE);
				ControlButtonUtil.Hide();
				mLcdView.setVisibility(View.VISIBLE);
				break;
			case TEST_COLOR_COUNT + 1:
				mLcdView.grayScale(true);
				mLcdView.paneBorder(false);
				mLcdView.postInvalidate();
				break;
			case TEST_COLOR_COUNT + 2:
				mLcdView.paneBorder(true);
				mLcdView.grayScale(false);
				mLcdView.postInvalidate();
				break;
			case TEST_COLOR_COUNT + 3:
				mLcdView.paneBorder(false);
				mLcdView.grayScale(false);
				mTestCount = 0;
				mLcdView.setVisibility(View.GONE);
				mTitle.setVisibility(View.VISIBLE);
				ControlButtonUtil.Show();
				accButtons.setVisibility(View.VISIBLE);
				;
				break;
			default:
				break;
			}
		}
		return super.onTouchEvent(paramMotionEvent);
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		} else if (event.getAction() == KeyEvent.ACTION_DOWN
				&& (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP || event
						.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)) {
			mTestCount++;

			if (mTestCount >= 1 && mTestCount <= TEST_COLOR_COUNT) {
				mLcdView.setBackgroundColor(TEST_COLORS[mTestCount - 1]);
			}
			switch (mTestCount) {
			case 1:
				mTitle.setVisibility(View.GONE);
				mText.setVisibility(View.GONE);
				ControlButtonUtil.Hide();
				mLcdView.setVisibility(View.VISIBLE);
				break;
			case TEST_COLOR_COUNT + 1:
				mLcdView.grayScale(true);
				mLcdView.paneBorder(false);
				mLcdView.postInvalidate();
				break;
			case TEST_COLOR_COUNT + 2:
				mLcdView.paneBorder(true);
				mLcdView.grayScale(false);
				mLcdView.postInvalidate();
				break;
			case TEST_COLOR_COUNT + 3:
				mLcdView.paneBorder(false);
				mLcdView.grayScale(false);
				mTestCount = 0;
				mLcdView.setVisibility(View.GONE);
				mTitle.setVisibility(View.VISIBLE);
				ControlButtonUtil.Show();
				break;
			default:
				break;
			}
		}
		return super.dispatchKeyEvent(event);
	}
}
