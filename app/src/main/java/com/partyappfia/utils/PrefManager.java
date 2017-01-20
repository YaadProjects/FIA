package com.partyappfia.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.partyappfia.application.FiaApplication;

/**
 * @author zheng
 */
public class PrefManager {
	
	public static final int PREF_MODE 				= Context.MODE_PRIVATE;
	public static final String PREF_NAME 			= "PreferenceInfo";

    static final String	PREF_LAST_NOTIFY_ID			= "LastNotifyId";

    public static void savePrefString(Context context, String key, String val) {
        int mode = PREF_MODE;
        String name = PREF_NAME;
        SharedPreferences mySharedPreferences = context.getSharedPreferences(name, mode);
        if(mySharedPreferences != null){
            SharedPreferences.Editor editor = mySharedPreferences.edit();
            editor.putString(key, val);
            editor.commit();
        }
    }

    public static void savePrefString(String key, String val) {
        savePrefString(FiaApplication.getApp().getApplicationContext(), key, val);
    }

    public static void savePrefBoolean(Context context, String key, boolean val) {
        int mode = PREF_MODE;
        String name = PREF_NAME;
        SharedPreferences mySharedPreferences = context.getSharedPreferences(name, mode);
        if(mySharedPreferences != null){
            SharedPreferences.Editor editor = mySharedPreferences.edit();
            editor.putBoolean(key, val);
            editor.commit();
        }
    }

    public static void savePrefBoolean(String key, boolean val) {
        savePrefBoolean(FiaApplication.getApp().getApplicationContext(), key, val);
    }

    public static void savePrefInt(Context context, String key, int val) {
        int mode = PREF_MODE;
        String name = PREF_NAME;
        SharedPreferences mySharedPreferences = context.getSharedPreferences(name, mode);
        if(mySharedPreferences != null){
            SharedPreferences.Editor editor = mySharedPreferences.edit();
            editor.putInt(key, val);
            editor.commit();
        }
    }

    public static void savePrefInt(String key, int val) {
        savePrefInt(FiaApplication.getApp().getApplicationContext(), key, val);
    }

    public static void savePrefLong(Context context, String key, long val) {
        int mode = PREF_MODE;
        String name = PREF_NAME;
        SharedPreferences mySharedPreferences = context.getSharedPreferences(name, mode);
        if(mySharedPreferences != null){
            SharedPreferences.Editor editor = mySharedPreferences.edit();
            editor.putLong(key, val);
            editor.commit();
        }
    }

    public static void savePrefLong(String key, long val) {
        savePrefLong(FiaApplication.getApp().getApplicationContext(), key, val);
    }

    public static String readPrefString(Context context,String key, String defValue) {
        int mode = PREF_MODE;
        String name = PREF_NAME;
        String result = "";
        SharedPreferences mySharedPreferences =context.getSharedPreferences(name, mode);
        if(mySharedPreferences != null){
            result = mySharedPreferences.getString(key, defValue);
        }
        return result;
    }

    public static String readPrefString(Context context, String key) {
        return readPrefString(context, key, "");
    }

    public static String readPrefString(String key) {
        return readPrefString(FiaApplication.getApp().getApplicationContext(), key, "");
    }

    public static String readPrefString(String key, String defaultValue) {
        return readPrefString(FiaApplication.getApp().getApplicationContext(), key, defaultValue);
    }

    public static Boolean readPrefBoolean(Context context, String key, boolean defaultValue)
    {
        int mode = PREF_MODE;
        String name = PREF_NAME;
        Boolean result = false;
        SharedPreferences mySharedPreferences =context.getSharedPreferences(name, mode);
        if(mySharedPreferences != null){
            result = mySharedPreferences.getBoolean(key, defaultValue);
        }
        return result;
    }

    public static Boolean readPrefBoolean(Context context, String key) {
        return readPrefBoolean(context, key, false);
    }

    public static Boolean readPrefBoolean(String key) {
        return readPrefBoolean(FiaApplication.getApp().getApplicationContext(), key, false);
    }

    public static Boolean readPrefBoolean(String key, boolean defaultValue) {
        return readPrefBoolean(FiaApplication.getApp().getApplicationContext(), key, defaultValue);
    }

    public static int readPrefInt(Context context, String key, int defaultValue) {
        int mode = PREF_MODE;
        String name = PREF_NAME;
        int result = 0;
        SharedPreferences mySharedPreferences = context.getSharedPreferences(name, mode);
        if(mySharedPreferences != null){
            result = mySharedPreferences.getInt(key, defaultValue);
        }
        return result;
    }

    public static int readPrefInt(Context context, String key) {
        return readPrefInt(context, key, 0);
    }

    public static int readPrefInt(String key) {
        return readPrefInt(FiaApplication.getApp().getApplicationContext(), key, 0);
    }

    public static int readPrefInt(String key, int defaultValue) {
        return readPrefInt(FiaApplication.getApp().getApplicationContext(), key, defaultValue);
    }

    public static long readPrefLong(Context context, String key, long defaultValue) {
        int mode = PREF_MODE;
        String name = PREF_NAME;
        long result = 0;
        SharedPreferences mySharedPreferences = context.getSharedPreferences(name, mode);
        if(mySharedPreferences != null){
            result = mySharedPreferences.getLong(key, defaultValue);
        }
        return result;
    }

    public static long readPrefLong(Context context, String key) {
        return readPrefLong(context, key, 0);
    }

    public static long readPrefLong(String key) {
        return readPrefLong(FiaApplication.getApp().getApplicationContext(), key, 0);
    }

    public static long readPrefLong(String key, long defaultValue) {
        return readPrefLong(FiaApplication.getApp().getApplicationContext(), key, defaultValue);
    }

    public static int getNotifyId(Context context) {
        return readPrefInt(context, PREF_LAST_NOTIFY_ID, 0x88888);
    }

    public static void increaseNotifyId(Context context) {
        int notifyId = readPrefInt(context, PREF_LAST_NOTIFY_ID, 0x88888);

        notifyId++;
        if ( notifyId >= 0x100000) {
            notifyId = 0x88888;
        }

        savePrefInt(context, PREF_LAST_NOTIFY_ID, notifyId);
    }
}
