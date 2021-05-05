package com.tyler.miniaudio;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class SavedPreferences {

    //apply() is asynchronous call to perform disk I/O where as commit() is synchronous. So avoid calling commit() from the UI thread


    public static String SNAP_TO_GRIP = "snap_to_grid";
    public static String ICON_NUMBER = "icon";
    public static String LIGHT_MODE = "light_mode";
    // static variable single_instance of type Singleton
    private static SavedPreferences single_instance = null;
    SharedPreferences pref = MainBottomNavActivity.contextOfApplication.getSharedPreferences("SavePreferences", MODE_PRIVATE);
    SharedPreferences.Editor editor = pref.edit();

    // private constructor restricted to this class itself
    private SavedPreferences() {
    }

    public void put(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public void put(String key, float value) {
        editor.putFloat(key, value);
        editor.apply();
    }

    public void put(String key, long value) {
        editor.putLong(key, value);
        editor.apply();
    }

    public void put(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    public void put(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }


    public String get(String key, String value) {
        return pref.getString(key, value);
    }

    public float get(String key, float value) {
        return pref.getFloat(key, value);
    }

    public long get(String key, long value) {
        return pref.getLong(key, value);
    }

    public int get(String key, int value) {
        return pref.getInt(key, value);
    }

    public boolean get(String key, boolean value) {
        return pref.getBoolean(key, value);
    }


    // static method to create instance of Singleton class
    public static SavedPreferences getInstance() {
        if (single_instance == null)
            single_instance = new SavedPreferences();

        return single_instance;
    }
}
