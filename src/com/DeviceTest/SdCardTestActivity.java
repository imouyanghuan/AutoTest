package com.DeviceTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;

import com.DeviceTest.helper.ControlButtonUtil;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.text.format.Formatter;
import android.util.Log;
import android.os.storage.StorageManager;

public class SdCardTestActivity extends Activity {
    private static final String TAG = "SdCardTestActivity";
    private static final String TEST_STRING = "MTK_UsbHostTest_File";
    private static final int BACK_TIME = 1000;
    private static final int R_PASS = 1;
    private static final int R_FAIL = 2;
    private StringBuilder sBuilder;
    public String SUCCESS;
    public String FAIL;
    private boolean isFindSd = true;
    private StorageManager mStorageManager = null;
    TextView mResult;
	TextView mResult2;
    private Button mPassBtn;

	boolean testSd1 = false;
	boolean testSd2 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        DeviceTest.lockScreenOrientation(this);
		setTitle(getTitle() + "----("
				+ getIntent().getStringExtra(DeviceTest.EXTRA_TEST_PROGRESS) + ")");
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        // getWindow().addFlags(1152);
        setContentView(R.layout.sdcardtest);

		if (mStorageManager == null) {
            mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
		}
		mPassBtn = (Button) findViewById(R.id.btn_Pass);
        this.mResult = (TextView) findViewById(R.id.sdresultText);
        this.mResult.setVisibility(View.VISIBLE);
        this.mResult.setGravity(17);

        this.mResult2 = (TextView) findViewById(R.id.sd2resultText);
        this.mResult2.setVisibility(View.VISIBLE);
        this.mResult2.setGravity(17);


        ControlButtonUtil.initControlButtonView(this);
        //findViewById(R.id.btn_Pass).setVisibility(View.INVISIBLE);
        //findViewById(R.id.btn_Fail).setVisibility(View.INVISIBLE);
        SUCCESS = getString(R.string.success);
        FAIL = getString(R.string.fail);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mStorageManager.registerListener(mStorageListener);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addDataScheme("file");
        registerReceiver(sdcardReceiver,intentFilter );
        
        sBuilder = new StringBuilder();
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }

		sBuilder.delete(0, sBuilder.length());
        testSdcard(DeviceTest.sdcard_path);
        mResult.setText(sBuilder.toString());

		sBuilder.delete(0, sBuilder.length());
		testSdcard(DeviceTest.sdcard2_path);
		mResult2.setText(sBuilder.toString());


		
    }

    @Override
    protected void onStop() {
        super.onStop();
       /* if (mStorageManager != null && mStorageListener != null) {
            //mStorageManager.unregisterListener(mStorageListener);
        }*/
        unregisterReceiver(sdcardReceiver);
    }

    public boolean testSdcard(String sdcardpath) {
        try {
            String externalVolumeState = mStorageManager.getVolumeState(sdcardpath);
            if (!externalVolumeState.equals(Environment.MEDIA_MOUNTED)) {
                sBuilder.append(getString(R.string.SdCardFail)).append("\n");
                mHandler.sendEmptyMessageDelayed(R_FAIL, 3000);
                isFindSd = false;
                mPassBtn.setVisibility(View.INVISIBLE);
                return false;
            }
        } catch (Exception rex) {
            rex.printStackTrace();
            isFindSd = false;
            mHandler.sendEmptyMessageDelayed(R_FAIL, 3000);
            return false;
        }

        File pathFile = new File(sdcardpath);

        Log.d(TAG, ">>>>> pathFile = " + pathFile.toString());

        StatFs stat = new StatFs(pathFile.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();       

        long availableBlocks = stat.getAvailableBlocks();
        
        String totalSize = Formatter.formatFileSize(this, totalBlocks
                * blockSize);
        String availableSize = Formatter.formatFileSize(this, blockSize
                * availableBlocks);
        		
        String prix = getString(R.string.SdCardFind);
        String prix2 = getString(R.string.SdCardAvailable);
        sBuilder.append(prix + totalSize + prix2 + availableSize).append("\n");
		return true;
    }

    public void testReadAndWrite() {
        if (isFindSd && dotestReadAndWrite()) {
            sBuilder.append(getString(R.string.SdCardTitle) + SUCCESS);
            mHandler.sendEmptyMessageDelayed(R_PASS, BACK_TIME);
        } else {
            sBuilder.append(getString(R.string.SdCardTitle) + FAIL);
            mHandler.sendEmptyMessageDelayed(R_FAIL, BACK_TIME);
        }

        mResult.setText(sBuilder.toString());
    }

    private boolean dotestReadAndWrite() {
        String directoryName = Environment.getExternalStorageDirectory().toString()
                + "/test";

        File directory = new File(directoryName);
        if (!directory.isDirectory()) {
            if (!directory.mkdirs()) {
                sBuilder.append(getString(R.string.MakeDir) + FAIL).append("\n");
                return false;
            } else {
                sBuilder.append(getString(R.string.MakeDir) + SUCCESS).append(
                        "\n");
            }
        }
        File f = new File(directoryName, "SDCard.txt");
        try {
            // Remove stale file if any
            if (f.exists()) {
                f.delete();
            }
            if (!f.createNewFile()) {
                sBuilder.append(getString(R.string.CreateFile) + FAIL).append(
                        "\n");
                return false;
            } else {
                sBuilder.append(getString(R.string.CreateFile) + SUCCESS).append(
                        "\n");

                doWriteFile(f.getAbsoluteFile().toString());

                if (doReadFile(f.getAbsoluteFile().toString()).equals(
                        TEST_STRING)) {
                    sBuilder.append(getString(R.string.Compare)).append(SUCCESS).append(
                            "\n");
                } else {
                    sBuilder.append(getString(R.string.Compare)).append(FAIL).append(
                            "\n");
                    return false;
                }
            }

            sBuilder.append(getString(R.string.FileDel)).append(
                    (f.delete() ? SUCCESS : FAIL)).append("\n");
            sBuilder.append(getString(R.string.DirDel)).append(
                    (directory.delete() ? SUCCESS : FAIL)).append("\n");
            return true;
        } catch (IOException ex) {
            Log.e(TAG, "isWritable : false (IOException)!");
            return false;
        }
    }

    public void doWriteFile(String filename) {
        try {
            sBuilder.append(getString(R.string.WriteData)).append("\n");
            OutputStreamWriter osw = new OutputStreamWriter(
                                                            new FileOutputStream(
                                                                                 filename));
            osw.write(TEST_STRING, 0, TEST_STRING.length());
            osw.flush();
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String doReadFile(String filename) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader
                    (new FileInputStream(filename)));
            String data = null;
            StringBuilder temp = new StringBuilder();
            sBuilder.append(getString(R.string.ReadData)).append("\n");
            while ((data = br.readLine()) != null) {
                temp.append(data);
            }
            br.close();
            Log.e(TAG, "Readfile " + temp.toString());
            return temp.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private  BroadcastReceiver sdcardReceiver = new BroadcastReceiver() { 

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReveive ..... " + intent.getAction());
            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED) ||
                    intent.getAction().equals(Intent.ACTION_MEDIA_EJECT
                         ) ) {
                sBuilder.delete(0, sBuilder.length());
                testSd1 = testSdcard(DeviceTest.sdcard_path);
				mResult.setText(sBuilder.toString());
				
				sBuilder.delete(0, sBuilder.length());
				testSd2 = testSdcard(DeviceTest.sdcard2_path);
				mResult2.setText(sBuilder.toString());

				if(testSd1&&testSd2)
					mPassBtn.setVisibility(View.VISIBLE);
                //testReadAndWrite(); // XXX 下个版本修改
            }
        }
    };

   /* StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onStorageStateChanged(String path, String oldState, String newState) {
        	if (path.equals(DeviceTest.sdcard_path) && newState.equals(Environment.MEDIA_MOUNTED)) {
                testSdcard();
                testReadAndWrite();
        	}
        }
    };*/
    
    public void TestResult(int result) {
        if (result == R_PASS) {
            ((Button) findViewById(R.id.btn_Pass)).performClick();
        } else if (result == R_FAIL) {
            ((Button) findViewById(R.id.btn_Fail)).performClick();
        }
    }

    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case R_PASS:
                    //TestResult(R_PASS);
                    break;
                case R_FAIL:
                    //TestResult(R_FAIL);
                    break;
            }
        };
    };
    
	// 取消返回按钮
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.dispatchKeyEvent(event);
	}

}
