package com.infowarelab.conference.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.infowarelab.conference.ConferenceApplication;

//import com.infowarelab.tvbox.base.BaseApplication;

/**
 * SharedPreferences存储数据方式工具类
 * 
 * @author lidongyao
 */
public class SharedPreferencesUrls {
	/**
	 * SP
	 */
	public static final String SP_NAME = "com.infowarelab.hongshantongphone";

	 private static SharedPreferencesUrls instance = new SharedPreferencesUrls();

	    public SharedPreferencesUrls() {

	    }

	    private static synchronized void syncInit() {
	        if (instance == null) {
	            instance = new SharedPreferencesUrls();
	        }
	    }

	    public static SharedPreferencesUrls getInstance() {
	        if (instance == null) {
	            syncInit();
	        }
	        return instance;
	    }

	    private static SharedPreferences getSp() {
	        return ConferenceApplication.getConferenceApp().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
	    }

	    public int getInt(String key, int def) {
	        try {
	            SharedPreferences sp = getSp();
	            if (sp != null)
	                def = sp.getInt(key, def);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return def;
	    }

	    public void putInt(String key, int val) {
	        try {
	            SharedPreferences sp = getSp();
	            if (sp != null) {
	                Editor e = sp.edit();
	                e.putInt(key, val);
	                e.commit();
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    public long getLong(String key, long def) {
	        try {
	            SharedPreferences sp = getSp();
	            if (sp != null)
	                def = sp.getLong(key, def);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return def;
	    }

	    public void putLong(String key, long val) {
	        try {
	            SharedPreferences sp = getSp();
	            if (sp != null) {
	                Editor e = sp.edit();
	                e.putLong(key, val);
	                e.commit();
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    public String getString(String key, String def) {
	        try {
	            SharedPreferences sp = getSp();
	            if (sp != null)
	                def = sp.getString(key, def);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return def;
	    }

	    public void putString(String key, String val) {
	        try {
	            SharedPreferences sp = getSp();
	            if (sp != null) {
	                Editor e = sp.edit();
	                e.putString(key, val);
	                e.commit();
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    public boolean getBoolean(String key, boolean def) {
	        try {
	            SharedPreferences sp = getSp();
	            if (sp != null)
	                def = sp.getBoolean(key, def);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return def;
	    }

	    public void putBoolean(String key, boolean val) {
	        try {
	            SharedPreferences sp = getSp();
	            if (sp != null) {
	                Editor e = sp.edit();
	                e.putBoolean(key, val);
	                e.commit();
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    public void remove(String key) {
	        try {
	            SharedPreferences sp = getSp();
	            if (sp != null) {
	                Editor e = sp.edit();
	                e.remove(key);
	                e.commit();
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
}
