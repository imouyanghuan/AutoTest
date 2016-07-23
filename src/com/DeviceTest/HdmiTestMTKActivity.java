package com.DeviceTest;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.DeviceTest.helper.ControlButtonUtil;
import com.DeviceTest.view.LcdTestView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.os.SystemProperties;

public class HdmiTestMTKActivity extends Activity{
	private static final String HDMITX = "/sys/class/hdmitx/hdmitx/subsystem/hdmitx";
    private final static int CHANGE_COLOR = 1;
    private final static int HDMI_SCAN = 2;
	private static boolean isHdmiConnected = false;
    
	private TextView HDMIStatus = null;
    private TextView mResult = null;
	private boolean isStart = false;

	private final static String TAG = "HDMITEST_MTK";

    private int[] TestColor = {Color.RED, Color.GREEN, Color.BLUE };
    private LcdTestView mTestView;
    private TextView mTitle;
    private TextView mShowTime;
    private int mTestNo;
   
    // Action broadcast to HDMI settings and other App
    public static final String ACTION_CABLE_STATE_CHANGED = "com.mediatek.hdmi.localservice.action.CABLE_STATE_CHANGED";
    public static final String ACTION_EDID_UPDATED = "com.mediatek.hdmi.localservice.action.EDID_UPDATED";
    public static final String ACTION_IPO_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN_IPO";
    public static final String ACTION_IPO_BOOTUP = "android.intent.action.ACTION_BOOT_IPO";
    private HDMIServiceReceiver mReceiver = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DeviceTest.lockScreenOrientation(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(FLAG_FULLSCREEN | FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.hdmitest);

        mTestView = (LcdTestView) findViewById(R.id.lcdtestview);
        mResult = (TextView) findViewById(R.id.result);
        mShowTime = (TextView) findViewById(R.id.TimeShow);
        mTestNo = 0;

        ControlButtonUtil.initControlButtonView(this);
        ((Button) findViewById(R.id.btn_Pass)).setVisibility(View.INVISIBLE);
        
        if (mReceiver == null) {
            mReceiver = new HDMIServiceReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HDMI_PLUG);
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        filter.addAction(ACTION_IPO_BOOTUP);
        filter.addAction(ACTION_IPO_SHUTDOWN);
        registerReceiver(mReceiver, filter);

    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.sendEmptyMessageDelayed(HDMI_SCAN, 500);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeMessages(HDMI_SCAN);
        mHandler.removeMessages(CHANGE_COLOR);
    }
    
    
    
    @Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}



	private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CHANGE_COLOR:
                    if (mTestNo > TestColor.length - 1) {
                        finishHdmiTest();
                        return;
                    }
                    ControlButtonUtil.Hide();
                    mShowTime.setVisibility(View.VISIBLE);
                    mTestView.setVisibility(View.VISIBLE);
                    mResult.setText(R.string.HdmiStart);
                    mTestView.setBackgroundColor(TestColor[mTestNo++]);
                    sendEmptyMessageDelayed(CHANGE_COLOR, 1500);
                    break;
                case HDMI_SCAN:
                	this.removeMessages(HDMI_SCAN);
                    if (startHdmiTest()) {
                        mResult.setText(R.string.HdmiPrepare);
                        //setHdmiConfig(HdmiFile, true);
                        mTestNo = 0;
                        sendEmptyMessageDelayed(CHANGE_COLOR, 4000);
                    }else{
                        sendEmptyMessageDelayed(HDMI_SCAN, 500);
                    }
                    break;
                default:
                    break;
            }
        }
    };
    
    public boolean startHdmiTest() {
        if (!isStart && isHdmiConnected) {
            mResult.setText(R.string.HdmiPrepare);
            //setHdmiConfig(HdmiFile, true);
            mTestNo = 0;
            isStart = true;
            return true;
        }
        mResult.setText(R.string.HdmiNoInsert);
        
        Log.i(TAG, ">>>>>>> Hdmi no insert");
        return false;
    }
    
    public void finishHdmiTest() {
        ((Button) findViewById(R.id.btn_Pass)).setVisibility(View.VISIBLE);
        ControlButtonUtil.Show();
        isStart = false;
        mShowTime.setVisibility(View.GONE);
        mTestView.setVisibility(View.GONE);
        mResult.setText(R.string.HdmiResult);
//        setHdmiConfig(HdmiFile, false);
    }

//    protected boolean isHdmiConnected(File file) {
//        boolean isConnected = false;
//        if (file.exists()) {
//            try {
//                FileReader fread = new FileReader(file);
//                BufferedReader buffer = new BufferedReader(fread);
//                String strPlug = "plug=1";
//                String str = null;
//                Log.v(TAG, ">>>>>>>>>>>>>>>>>>  isHdmiConnected ");
//                while ((str = buffer.readLine()) != null) {
//                    int length = str.length();
//                    //if ((length == 6) && (str.equals(strPlug))) {
//                    Log.e(TAG, file + " :>>>>>>>>>>>>>>>>>>  str : " + str);
//                    if(str.equals("1")){
//                        isConnected = true;
//                        break;
//                    } else {
//                        isConnected = false;
//                    }
//                }
//            } catch (IOException e) {
//                Log.e(TAG, "IO Exception");
//            }
//        } else {
//            Log.e(TAG, file + "isHdmiConnected : file no exist");
//        }
//        return isConnected;
//    }

//    protected void setHdmiConfig(File file, boolean enable) {
//        if (SystemProperties.get("ro.board.platform", "none").equals("rk29xx")){
//            if (file.exists()) {
//                try {
//                    String strDouble = "2";
//                    String strChecked = "1";
//                    String strUnChecked = "0";
//                    RandomAccessFile rdf = null;
//                    rdf = new RandomAccessFile(file, "rw");
//
//                    if (enable) {
//                        rdf.writeBytes(strChecked);
//                    } else {
//                        rdf.writeBytes(strUnChecked);
//
//                    }
//
//                } catch (IOException re) {
//                    Log.e(TAG, "IO Exception");
//                }
//            } else {
//                Log.i(TAG, "The File " + file + " is not exists");
//            }
//        } else{
//            if (file.exists()) {
//                try {
//                    Log.d(TAG, "setHdmiConfig");
//                    String strChecked = "1";
//                    String strUnChecked = "0";
//
//                    RandomAccessFile rdf = null;
//                    rdf = new RandomAccessFile(file, "rw");
//                    if (enable) {
//                        rdf.writeBytes(strChecked);
//                    } else {
//                        rdf.writeBytes(strUnChecked);
//                    }
//                } catch (IOException re) {
//                    Log.e(TAG, "IO Exception");
//                    re.printStackTrace();
//                }
//            } else {
//                Log.i(TAG, "The File " + file + " is not exists");
//            }
//        }
//    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.dispatchKeyEvent(event);
    }
    
    public boolean onTouchEvent(MotionEvent paramMotionEvent) {
    	Log.v(TAG, ">>>>>>>>>>>>>>>>>>>>> onTouchEvent <<<<<<<<<<<<<<<<<<<<");

        if (paramMotionEvent.getAction() == MotionEvent.ACTION_DOWN && !isStart) {
        	Log.v(TAG,">>>>>>>>>>> ACTION_DOWN <<<<<<<<<<<<");
    		mHandler.sendEmptyMessageDelayed(HDMI_SCAN, 500);
        }
        return super.onTouchEvent(paramMotionEvent);
    }
	
	
	/**
	 * 广播更新HDMI状态
	 * @author zzp
	 *
	 */
    private class HDMIServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v(TAG,">>>>>>>>>>>>>>HDMIServiceReceiver action : " + action);
            if (Intent.ACTION_HDMI_PLUG.equals(action)) {
                int hdmiCableState = intent.getIntExtra("state", 0);
                if(hdmiCableState==1)
                	HdmiTestMTKActivity.isHdmiConnected = true;
                else
                	HdmiTestMTKActivity.isHdmiConnected = false;
                Log.e(TAG,">>>>>>>>>>>>> HDMIServiceReceiver hdmiCableState : " + hdmiCableState);
            } else if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                Log.e(TAG,">>>>>>>>>>> receive the headset plugin and do nothing");
               
            } else if (ACTION_IPO_BOOTUP.equals(action)) {
                Log.e(TAG,">>>>>>>>> HDMI local service receive IPO boot up broadcast");
               
            } else if (ACTION_IPO_SHUTDOWN.equals(action)) {
            	Log.e(TAG,">>>>>>>>> ACTION_IPO_SHUTDOWN");
            }
        }
    }

	
	
	
	private String ReadHDMIState() {
		String State = readFileByLines(HDMITX);

		return State;
	}

	/**
	 * 以行为单位读取文件，常用于读面向行的格式化文件
	 */
	public static String readFileByLines(String fileName) {
		File file = new File(fileName);
		BufferedReader reader = null;
		String tempString = null;
		try {
			System.out.println("以行为单位读取文件内容，一次读一整行：");
			reader = new BufferedReader(new FileReader(file));

			int line = 1;
			// 一次读入一行，直到读入null为文件结束
			/*while ((tempString = reader.readLine()) != null) {
                // 显示行号
                System.out.println("line " + line + ": " + tempString);
                line++;
            }*/
			tempString = reader.readLine(); // 默认读首行
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return tempString;
	}

}
