package com.tchip.autotest;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Prefs { // 本地存储

	private SharedPreferences sharedPrefs = null;
	final String PREFS_NAME = "my_test";

	public Prefs(Context context) {
		sharedPrefs = context.getSharedPreferences(PREFS_NAME,
				Activity.MODE_WORLD_WRITEABLE);

	}

	public String getString(String key, String def) {
		String s = sharedPrefs.getString(key, def);
		return s;
	}

	public void setString(String key, String val) {
		Editor e = sharedPrefs.edit();
		e.putString(key, val);
		e.commit();
	}

	public boolean getBoolean(String key, boolean def) {
		boolean b = sharedPrefs.getBoolean(key, def);
		return b;
	}

	public void setBoolean(String key, boolean val) {
		Editor e = sharedPrefs.edit();
		e.putBoolean(key, val);
		e.commit();
	}

}
