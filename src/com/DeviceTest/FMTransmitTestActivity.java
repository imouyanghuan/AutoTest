package com.DeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import java.io.IOException;

import com.DeviceTest.helper.ControlButtonUtil;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.view.KeyEvent;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class FMTransmitTestActivity extends Activity {
	TextView mBattery;
	private BroadcastReceiver mBatteryInfoReceiver;
	TextView textFrequency;
	TextView mVoltage;
	TextView mCapacity;
	TextView mPlug;
	private static final String CURRENT_PATH = "/sys/class/power_supply/*battery/current_now";

	boolean stop = false;

	int mLastVoltage = -1;

	TextView pluginView;
	TextView unplugView;
	boolean pluginPass = false;
	boolean unplugPass = false;

	public FMTransmitTestActivity() {

	}

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		DeviceTest.lockScreenOrientation(this);
		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
				+ ")");
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_fmtransmittest);
		ControlButtonUtil.initControlButtonView(this);

		this.textFrequency = (TextView) findViewById(R.id.textFrequency);
		this.mVoltage = (TextView) findViewById(R.id.voltageText);
		this.mCapacity = (TextView) findViewById(R.id.capacityText);
		this.mPlug = (TextView) findViewById(R.id.plugText);

		// findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
		pluginView = (TextView) findViewById(R.id.pluginTest);
		unplugView = (TextView) findViewById(R.id.unplugTest);

		SeekBar fmSeekBar = (SeekBar) findViewById(R.id.fmSeekBar);
		// 875-1080
		// 0- 205
		fmSeekBar.setMax(205);
		int nowFrequency = SettingUtil.getFmFrequencyConfig(this);
		fmSeekBar.setProgress(nowFrequency / 10 - 875);

		textFrequency.setText(Html
				.fromHtml(getString(R.string.transmit_frequency) + ":"
						+ "<font color=yellow>" + nowFrequency / 100.0f
						+ "</font>" + " MHz"
						+ getString(R.string.open_fm_and_test)));

		fmSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				SettingUtil.setFmFrequencyNode(getApplicationContext(),
						(seekBar.getProgress() + 875) * 10);
				SettingUtil.setFmFrequencyConfig(getApplicationContext(),
						(seekBar.getProgress() + 875) * 10);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				float frequency = (progress + 875.0f) / 10;

				textFrequency.setText(Html
						.fromHtml(getString(R.string.transmit_frequency) + ":"
								+ "<font color=yellow>" + frequency + "</font>"
								+ " MHz" + getString(R.string.open_fm_and_test)));
			}
		});

	}

	/**
	 * FmFrequceny:7600~10800:8750-10800
	 */
	private void initialFmTransmit() {
		try {
			int freq = SettingUtil.getFmFrequencyConfig(this);

			if (freq >= 8750 && freq <= 10800) {
				SettingUtil.setFmFrequencyNode(this, freq);
			} else {
				SettingUtil.setFmFrequencyNode(this, 9600);
				freq = 9600;
			}
			textFrequency.setText(Html
					.fromHtml(getString(R.string.transmit_frequency) + ":"
							+ "<font color=yellow>" + freq / 100.0f + "</font>"
							+ " MHz" + getString(R.string.open_fm_and_test)));

			openFmTransmit(true);

		} catch (Exception e) {
		}
	}

	private void openFmTransmit(boolean isOpen) {
		Settings.System.putString(getContentResolver(),
				"fm_transmitter_enable", isOpen ? "1" : "0");
		SettingUtil.SaveFileToNode(SettingUtil.nodeFmEnable, (isOpen ? "1"
				: "0"));

		sendBroadcast(new Intent(isOpen ? "tchip.intent.action.FM_ON"
				: "tchip.intent.action.FM_OFF"));
	}

	private AudioManager mAudioManager;
	private MediaPlayer mPlayer;

	private void playMusic() {
		mAudioManager = (AudioManager) getSystemService("audio");
		mPlayer = new MediaPlayer();
		try {
			// mPlayer.setDataSource("/system/media/audio/ringtones/CrazyDream.ogg");
			AssetFileDescriptor fd = getAssets().openFd("test_music.mp3");
			mPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(),
					fd.getDeclaredLength());

			mPlayer.prepare();
			mPlayer.setLooping(true);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, true);

		int j = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, j, 0);
		this.mPlayer.start();

		mVoltage.setText(getString(R.string.music_playing));
	}

	private void stopMediaPlayBack() {
		Intent i = new Intent("com.android.music.musicservicecommand");
		i.putExtra("command", "pause");
		sendBroadcast(i);
	}

	protected void onResume() {
		super.onResume();
		stop = false;
		initialFmTransmit();

		stopMediaPlayBack();

		playMusic();
	}

	public void onPause() {
		super.onPause();
		stop = true;

		mPlayer.stop();
		if (this.mPlayer == null) {
			return;
		}
		this.mPlayer.release();
		this.mPlayer = null;

		openFmTransmit(false);

	}

	protected void onDestroy() {
		super.onDestroy();
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
}
