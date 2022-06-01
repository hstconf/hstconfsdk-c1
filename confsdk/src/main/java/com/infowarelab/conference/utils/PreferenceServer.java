package com.infowarelab.conference.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Always on 2017/9/29.
 */

public class PreferenceServer {

    public static final String SET_CAMID = "SET_CAMID";
    public static final String SET_CAMWIDTH = "SET_CAMWIDTH";
    public static final String SET_CAMHEIGHT = "SET_CAMHEIGHT";
    public static final String DEVICEID = "DEVICEID";
    public static final String MODE = "VIDEO_MODE";

    public static void saveSharedPreferences(Context context, String name, String key, int value) {
        SharedPreferences preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static void saveSharedPreferences(Context context, String name, String key, String value) {
        SharedPreferences preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static int readSharedPreferencesInt(Context context, String name, String key) {
        if (context == null) {
            return -1;
        }
        SharedPreferences preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return preferences.getInt(key, -1);
    }

    public static String readSharedPreferencesStirng(Context context, String name, String key) {
        if (context == null) {
            return "";
        }
        SharedPreferences preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        return preferences.getString(key, "");
    }
}
