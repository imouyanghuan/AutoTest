package com.tchip.autotest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import com.tchip.autotest.R;
import com.tchip.autotest.helper.ControlButtonUtil;
import com.tchip.autotest.view.GsensorBall;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class GsensorTestActivity extends Activity {
	/** Called when the activity is first created. */
	private final static int MAX_NUM = 8;
	private SensorManager sensorManager;
	private SensorEventListener lsn = null;
	boolean stop = false;
	private int mHardwareRotation;

	private static enum TEST_AXIS {
		X, Y, Z, D
	};

	private TEST_AXIS testAxis;
	private GsensorBall mGsensorBall;

	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		mHardwareRotation = DeviceTest.lockScreenOrientation(this);
		setContentView(R.layout.gsensortest);
		stop = false;
		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
				+ ")");
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		ControlButtonUtil.initControlButtonView(this);
		sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		mGsensorBall = (GsensorBall) findViewById(R.id.gsensorball);
		setTestAxis(TEST_AXIS.X);
		// findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
	}

	TextView X_textView, Y_textView, Z_textView;

	protected void onResume() {
		super.onResume();

		stop = false;
		final TextView subTitle = (TextView) findViewById(R.id.Accelerometer);
		subTitle.setTextColor(Color.rgb(255, 0, 0));

		X_textView = (TextView) findViewById(R.id.gsensorTestX);
		X_textView.setTextColor(android.graphics.Color.GREEN);

		Y_textView = (TextView) findViewById(R.id.gsensorTestY);
		Y_textView.setTextColor(android.graphics.Color.GREEN);

		Z_textView = (TextView) findViewById(R.id.gsensorTestZ);
		Z_textView.setTextColor(android.graphics.Color.GREEN);

		lsn = new SensorEventListener() {

			public void onAccuracyChanged(Sensor sensor, int accuracy) {

			}

			public void onSensorChanged(SensorEvent e) {
				if (stop) {
					return;
				}
				subTitle.setText("x:" + (int) e.values[0] + ", y:"
						+ (int) e.values[1] + ", z:" + (int) e.values[2]);
				doTest(e);
				float x = e.values[0];
				float y = e.values[1];
				float z = e.values[2];
				if (mHardwareRotation == 0) {
					mGsensorBall.setXYZ(-1 * x, y, z);
				} else if (mHardwareRotation == 90) {
					mGsensorBall.setXYZ(y, x, z);
				} else if (mHardwareRotation == 180) {
					mGsensorBall.setXYZ(-1 * x, -1 * y, z);
				} else if (mHardwareRotation == 270) {
					mGsensorBall.setXYZ(y, -1 * x, z);
				} else {
					mGsensorBall.setXYZ(-1 * x, y, z);
				}
			}

		};

		Sensor sensors = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		sensorManager.registerListener(lsn, sensors,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	private void doTest(SensorEvent e) {
		switch (testAxis) {
		case X:
			if ((int) e.values[0] >= MAX_NUM && (int) e.values[1] == 0
					&& (int) e.values[2] == 0) {
				setTestAxis(TEST_AXIS.Y);
				X_textView.setText(X_textView.getText() + ":Pass");
			}
			break;
		case Y:
			if ((int) e.values[0] == 0 && (int) e.values[1] >= MAX_NUM
					&& (int) e.values[2] == 0) {
				setTestAxis(TEST_AXIS.Z);
				Y_textView.setText(Y_textView.getText() + ":Pass");
			}
			break;
		case Z:
			if ((int) e.values[0] == 0 && (int) e.values[1] == 0
					&& (int) e.values[2] >= MAX_NUM) {
				setTestAxis(TEST_AXIS.D);
				Z_textView.setText(Z_textView.getText() + ":Pass");
				// findViewById(R.id.btn_Pass).performClick();
			}
			break;
		default:
			break;
		}
	}

	private void setTestAxis(TEST_AXIS testAxis) {
		this.testAxis = testAxis;
		switch (testAxis) {
		case X:
			findViewById(R.id.gsensorTestX).setVisibility(View.VISIBLE);
			break;
		case Y:
			findViewById(R.id.gsensorTestY).setVisibility(View.VISIBLE);
			break;
		case Z:
			findViewById(R.id.gsensorTestZ).setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}

	//

	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(lsn);
		stop = true;
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
}
