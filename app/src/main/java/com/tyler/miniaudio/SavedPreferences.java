package com.tyler.miniaudio;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class SavedPreferences {

    //apply() is asynchronous call to perform disk I/O where as commit() is synchronous. So avoid calling commit() from the UI thread


    public static String SNAP_TO_GRIP = "snap_to_grid";
    public static String ICON_NUMBER = "icon";
    public static String LIGHT_MODE = "light_mode";
    public static String PERMISION_TO_STORAGE = "Permission_To_Storage";
    // static variable single_instance of type Singleton
    private static SavedPreferences single_instance = null;
    private Context mainContext;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    // private constructor restricted to this class itself
    private SavedPreferences(Context context) {
        if (mainContext != null)
            return;
        mainContext = context;
        pref = mainContext.getSharedPreferences("SavePreferences", MODE_PRIVATE);
        editor = pref.edit();
    }

    public void put(String key, String value) {
        if (mainContext == null)
            return;
        editor.putString(key, value);
        editor.apply();
    }

    public void put(String key, float value) {
        if (mainContext == null)
            return;
        editor.putFloat(key, value);
        editor.apply();
    }

    public void put(String key, long value) {
        if (mainContext == null)
            return;
        editor.putLong(key, value);
        editor.apply();
    }

    public void put(String key, int value) {
        if (mainContext == null)
            return;
        editor.putInt(key, value);
        editor.apply();
    }

    public void put(String key, boolean value) {
        if (mainContext == null)
            return;
        editor.putBoolean(key, value);
        editor.apply();
    }


    public String get(String key, String value) {
        if (mainContext == null)
            return "";
        return pref.getString(key, value);
    }

    public float get(String key, float value) {
        if (mainContext == null)
            return 0;
        return pref.getFloat(key, value);
    }

    public long get(String key, long value) {
        if (mainContext == null)
            return 0;
        return pref.getLong(key, value);
    }

    public int get(String key, int value) {
        if (mainContext == null)
            return 0;
        return pref.getInt(key, value);
    }

    public boolean get(String key, boolean value) {
        if (mainContext == null)
            return true;
        return pref.getBoolean(key, value);
    }

    public static void init(Context context){
        single_instance = new SavedPreferences(context);
    }
    // static method to create instance of Singleton class
    public static SavedPreferences getInstance() {
        if (single_instance == null)
            return null;
        return single_instance;
    }
}
