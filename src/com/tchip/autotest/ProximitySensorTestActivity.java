package com.tchip.autotest;

import com.tchip.autotest.R;
import com.tchip.autotest.helper.ControlButtonUtil;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 距离传感器
 */
public class ProximitySensorTestActivity extends Activity {

	private SensorManager sensorManager;

	private Sensor illuminationSensor;

	private String TAG = "ProximitySensorTestActivity";

	private TextView tipTV = null;
	private TextView proximitysensorTV = null;

	private Button mPassBtn;
	boolean isSensorChanged = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.proximitysensortest);
		DeviceTest.lockScreenOrientation(this);
		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
				+ ")");

		tipTV = (TextView) findViewById(R.id.tip);
		proximitysensorTV = (TextView) findViewById(R.id.proximitysensor);
		ControlButtonUtil.initControlButtonView(this);
		mPassBtn = (Button) findViewById(R.id.btn_Pass);
		// mPassBtn.setVisibility(View.INVISIBLE);
		// mPassBtn.setEnabled(false);
		// proximitysensorTV.append(" 0 (数据没变化表示失败)");
		// init();
	}

	/**
	 * 对象的初始化
	 */
	private void init() {
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// proximity sensor
		illuminationSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_PROXIMITY);

		if (illuminationSensor == null) {
			Toast.makeText(this, "您的设备暂不支持该功能！", 0).show();
			tipTV.setText(getString(R.string.proximitysensor_tip_error));
			mPassBtn.setEnabled(false);
		} else {
			tipTV.setText(getString(R.string.proximitysensor_tip) + "\n"
					+ "0.0：表示靠近设备\n" + "1.0：表示离开设备\n");
			proximitysensorTV
					.setText(getString(R.string.proximitysensor_distance)
							+ "1.0");// （数据没变化表示失败）");

			String str = "\n名字：" + illuminationSensor.getName() + "\n电池："
					+ illuminationSensor.getPower() + "\n类型："
					+ illuminationSensor.getType() + "\nVendor:"
					+ illuminationSensor.getVendor() + "\n版本："
					+ illuminationSensor.getVersion() + "\n幅度："
					+ illuminationSensor.getMaximumRange();
			Log.v(TAG, ">>>>>>>> str : " + str);

		}
		sensorManager.registerListener(sensorEventListener, illuminationSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	/**
	 * 监听器
	 */
	private SensorEventListener sensorEventListener = new SensorEventListener() {

		public void onSensorChanged(SensorEvent event) {
			if (event.values == null)
				return;
			if (event.values.length == 0)
				return;

			if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {// proximity

				float[] values = event.values;

				// if(!isSensorChanged && ((int)values[0])==0){
				// mPassBtn.setEnabled(true);
				// isSensorChanged = true;
				// }

				// TODO Auto-generated method stub
				// float[] values=event.values;
				Log.v(TAG, ">>>>>>>> 设备与物体的距离为： : " + values[0]);
				// tv_g.setText("手机距离物体的距离为："+values[0]);
				proximitysensorTV
						.setText(getString(R.string.proximitysensor_distance)
								+ values[0]);
				// proximitysensorTV.setText(getString(R.string.proximitysensor_distance)
				// + (values[0]>0?"离开":"接近"));//
				// +"\n幅度："+illuminationSensor.getMaximumRange());
			}
		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}

	};

	protected void onResume() {
		super.onResume();
		init();
	}

	protected void onStop() {
		super.onStop();
		sensorManager.unregisterListener(sensorEventListener);
	}

	// 取消返回按钮
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
}
