package com.DeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import com.DeviceTest.helper.ControlButtonUtil;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * 听筒测试
 * @author zzp
 *
 */
public class HandsetTestActivity_bak extends Activity implements OnClickListener{

	private Button handsetStart;
	//private Button handsetStop;

	private Context mContext;
	private RecordThread rec;
	private boolean isStart = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DeviceTest.lockScreenOrientation(this);
		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS) + ")");
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.handsettest);

		handsetStart = (Button) findViewById(R.id.headset_start);
		//handsetStop = (Button) findViewById(R.id.headset_stop);

		handsetStart.setOnClickListener(this);
		//handsetStop.setOnClickListener(this);
		
		ControlButtonUtil.initControlButtonView(this);
		rec = new RecordThread();
	}

	public void onClick(View v) {
		if(v == handsetStart){
			if(isStart){
				isStart = false;
				rec.stopThread();
				handsetStart.setText(getString(R.string.handset_start));
			}else{
				isStart = true;
				rec = new RecordThread();
				rec.start();
				handsetStart.setText(getString(R.string.handset_stop));
			}
		} 

	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		if(isStart)
			rec.stopThread();
	}

		//打开扬声器
		public void OpenSpeaker() {

			try{
				AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
				audioManager.setMode(AudioManager.ROUTE_SPEAKER);
				// currVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);

				if(!audioManager.isSpeakerphoneOn()) {
					audioManager.setSpeakerphoneOn(true);

					audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
							audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL ),
							AudioManager.STREAM_VOICE_CALL);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
}


class RecordThread extends Thread{
	static final int frequency = 44100;
	// static final int frequency = 11025;
	static final int channelConfiguration = AudioFormat.CHANNEL_IN_STEREO;
	static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

	AudioRecord audioRecord;
	AudioTrack audioTrack;
	static boolean isStart = true;

	@Override
	public void run() {
		isStart = true;
		// TODO Auto-generated method stub
		int recBufSize = AudioRecord.getMinBufferSize(frequency,
				channelConfiguration, audioEncoding);
		int plyBufSize = AudioTrack.getMinBufferSize(frequency,
				channelConfiguration, audioEncoding);

		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
				channelConfiguration, audioEncoding, recBufSize);

		audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, frequency,
				channelConfiguration, audioEncoding, plyBufSize, AudioTrack.MODE_STREAM);

		byte[] recBuf = new byte[recBufSize];
		audioRecord.startRecording();
		audioTrack.play();
		Log.v("",">>>>>>>>>>>>>>>. start <<<<<<<<<<<<<<<<");
		while(isStart){
			int readLen = audioRecord.read(recBuf, 0, recBufSize);
			audioTrack.write(recBuf, 0, readLen);
		}
		Log.v("",">>>>>>>>>>>>>>>. end <<<<<<<<<<<<<<<<");
	}

	public void stopThread(){
		try{
			audioRecord.stop();
			audioTrack.stop();
			isStart = false;
			Log.v("",">>>>>>>>>>>>>>> stopThread <<<<<<<<<<<<<<<<");
		}catch(Exception e){
			e.printStackTrace();
		}

	}
}
