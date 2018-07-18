package com.hexinnovation.flashlight;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;

public final class Preferences {
    private Preferences() {

    }
    private static SharedPreferences getPrefs() {
        return MyApplication.getContext().getSharedPreferences(PREFERENCES_NAME, Activity.MODE_PRIVATE);
    }

    public static final int NOTIFICATION_ALWAYS = 10;
    public static final int NOTIFICATION_SOMETIMES = 5;
    public static final int NOTIFICATION_NEVER = 0;

    private static final String PREFERENCES_NAME = "FlashlightPrefs";
    private static final String NOTIFICATION = "Notification";
    private static final String THEME = "Theme";
    private static final String CYCLE_LIGHT_ON_SCREEN_OFF = "CycleLightOnScreenOff";

    public static int getNotificationSetting() {
        return getPrefs().getInt(NOTIFICATION, Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? NOTIFICATION_SOMETIMES : NOTIFICATION_NEVER);
    }
    public static void setNotificationSetting(int value) {
        getPrefs().edit().putInt(NOTIFICATION, value).commit();
    }
    public static int getTheme() {
        return getPrefs().getInt(THEME, 0);
    }
    public static void setTheme(int value) {
        commitEditor(getPrefs().edit().putInt(THEME, value));
    }
    public static boolean getCycleLightOnScreenOff() {
        return getPrefs().getBoolean(CYCLE_LIGHT_ON_SCREEN_OFF, false);
    }
    public static void setCycleLightOnScreenOff(boolean value) {
        commitEditor(getPrefs().edit().putBoolean(CYCLE_LIGHT_ON_SCREEN_OFF, value));
    }
    private static void commitEditor(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            editor.apply();
        } else {
            editor.commit();
        }
    }
}
