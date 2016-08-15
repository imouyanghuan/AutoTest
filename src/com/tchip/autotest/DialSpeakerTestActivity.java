package com.tchip.autotest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import com.tchip.autotest.R;
import com.tchip.autotest.helper.ControlButtonUtil;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.widget.TextView;

import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;

public class DialSpeakerTestActivity extends Activity {

	private Context mContext = DialSpeakerTestActivity.this;
	MediaPlayer mMediaPlayer;
	Vibrator mVibrator;
	TextView txtContent;

	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		DeviceTest.lockScreenOrientation(this);

		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
				+ ")");
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.spkrcv);
		TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
		txtContent = (TextView) findViewById(R.id.txtContent);
		// txtTitle.setText(R.string.SpeakerTitle);
		// xtContent.setText(getString(R.string.SpeakerTip));

		init();

		ControlButtonUtil.initControlButtonView(this);

	}

	protected void onDestroy() {
		super.onDestroy();
		mMediaPlayer.stop();
	}

	protected void onPause() {
		super.onPause();
		stopRing();
		stopVibrate();
	}

	protected void onResume() {
		super.onResume();
		AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		boolean b = mAudioManager.shouldVibrate(0);
		txtContent.setText(b ? R.string.spkrcv_now_r_v
				: R.string.spkrcv_now_r_nv);
		startRing();
		startVibrate();
	}

	private void startVibrate() {
		AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		boolean b = mAudioManager.shouldVibrate(0);
		if (b) {
			long[] pat = new long[] { 1000, 1500 };
			mVibrator.vibrate(pat, 0);
		}
	}

	private void stopVibrate() {
		mVibrator.cancel();
	}

	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}

	public void startRing() {

		AudioManager audioManager = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);
		int mode = AudioManager.MODE_IN_CALL;
		if (audioManager.getMode() != mode) {
			audioManager.setMode(mode);
		}
		audioManager.setSpeakerphoneOn(false);
	}

	public void stopRing() {
		AudioManager audioManager = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);
		audioManager.setSpeakerphoneOn(true);
	}

	private void init() {
		mMediaPlayer = MediaPlayer.create(mContext,
				getDefaultRingtoneUri(RingtoneManager.TYPE_RINGTONE));

		mMediaPlayer.setLooping(true);
		mMediaPlayer.start();

		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}

	public Ringtone getDefaultRingtone(int type) {

		return RingtoneManager.getRingtone(mContext,
				RingtoneManager.getActualDefaultRingtoneUri(mContext, type));

	}

	public Uri getDefaultRingtoneUri(int type) {

		return RingtoneManager.getActualDefaultRingtoneUri(mContext, type);

	}

	public List<Ringtone> getRingtoneList(int type) {

		List<Ringtone> resArr = new ArrayList<Ringtone>();

		RingtoneManager manager = new RingtoneManager(mContext);

		manager.setType(type);

		Cursor cursor = manager.getCursor();

		int count = cursor.getCount();

		for (int i = 0; i < count; i++) {

			resArr.add(manager.getRingtone(i));

		}

		return resArr;

	}

	public Ringtone getRingtone(int type, int pos) {

		RingtoneManager manager = new RingtoneManager(mContext);

		manager.setType(type);

		return manager.getRingtone(pos);

	}

	public List<String> getRingtoneTitleList(int type) {

		List<String> resArr = new ArrayList<String>();

		RingtoneManager manager = new RingtoneManager(mContext);

		manager.setType(type);

		Cursor cursor = manager.getCursor();

		if (cursor.moveToFirst()) {

			do {

				resArr.add(cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX));

			} while (cursor.moveToNext());

		}

		return resArr;

	}

	public String getRingtoneUriPath(int type, int pos, String def) {

		RingtoneManager manager = new RingtoneManager(mContext);

		manager.setType(type);

		Uri uri = manager.getRingtoneUri(pos);

		return uri == null ? def : uri.toString();

	}

	public Ringtone getRingtoneByUriPath(int type, String uriPath) {

		RingtoneManager manager = new RingtoneManager(mContext);

		manager.setType(type);

		Uri uri = Uri.parse(uriPath);

		return manager.getRingtone(mContext, uri);

	}
}
