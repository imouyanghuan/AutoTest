package com.DeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import java.io.IOException;

import com.DeviceTest.helper.ControlButtonUtil;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

/**
 * 新 听筒测试
 * 
 * @author zzp
 * 
 */
public class HandsetTestActivity extends Activity {

	private Context mContext;
	private boolean isStart = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DeviceTest.lockScreenOrientation(this);
		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS)
				+ ")");
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.handsettest_new);

		mediaPlayer = new MediaPlayer();
		mAudioManager = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		ControlButtonUtil.initControlButtonView(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mAudioManager.isSpeakerphoneOn()) {
			mAudioManager.setSpeakerphoneOn(false);
		}

		AssetFileDescriptor fd = null;
		try {
			fd = getAssets().openFd("Neptunium.ogg");
			Log.v("ZZP", ">>>>> fd: " + fd.getFileDescriptor());
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (fd != null) {
			mAudioManager = (AudioManager) this
					.getSystemService(Context.AUDIO_SERVICE);
			mAudioManager.setMode(AudioManager.MODE_IN_CALL);// 把模式调成听筒放音模式

			mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, false);
			mAudioManager
					.setStreamVolume(
							AudioManager.STREAM_VOICE_CALL,
							mAudioManager
									.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
							AudioManager.FLAG_PLAY_SOUND);

			playMusic(fd);

		}

	}

	@Override
	protected void onPause() {
		super.onPause();

		// this.mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL,
		// false);
		// this.mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
		// this.mOldVolume, AudioManager.STREAM_VOICE_CALL);

		if (mediaPlayer.isPlaying())
			mediaPlayer.stop();
		mAudioManager = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.setMode(AudioManager.MODE_NORMAL);
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();

		mediaPlayer.stop();
		if (mediaPlayer == null)
			return;
		mediaPlayer.release();
		mediaPlayer = null;

	}

	// //////////////////////////////////////////

	private MediaPlayer mediaPlayer;
	private AudioManager mAudioManager;
	private int mOldVolume;

	private void playMusic(AssetFileDescriptor fd) {
		Log.v("ZZP", ">>>>> playMusic <<<<<<");
		try {
			mediaPlayer.setDataSource(fd.getFileDescriptor(),
					fd.getStartOffset(), fd.getDeclaredLength());
			mediaPlayer.prepare();
			// 设置音频循环播放
			mediaPlayer.setLooping(true);
			mediaPlayer.setOnPreparedListener(new PrepareListener(0));
			Log.e("ZZP", ">>>>> playMusic <<<<<<");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private final class PrepareListener implements OnPreparedListener {
		private int position;

		public PrepareListener(int position) {
			this.position = position;
		}

		public void onPrepared(MediaPlayer mp) {
			mediaPlayer.start();// 开始播放
			if (position > 0)
				mediaPlayer.seekTo(position);
		}
	}

	// /////////////////////////////

	// 取消返回按钮
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}
}
