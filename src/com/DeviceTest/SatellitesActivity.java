package com.DeviceTest;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;

/*
 * 有时间单独将该模块提取出来
 */
public class SatellitesActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        // .detectDiskReads()
                // .detectDiskWrites()
                .detectNetwork() // or .detectAll() for all detectable problems
                // .penaltyLog()
                .build());
        
		setContentView(R.layout.ygps_layout_satellites);
	}
	
}
