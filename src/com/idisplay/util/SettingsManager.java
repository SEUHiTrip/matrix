package com.idisplay.util;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import com.idisplay.base.IDisplayApp;
import org.apache.commons.lang.StringUtils;

import seu.lab.matrix.R;

public class SettingsManager {
    public static final String AUTOCONNECT_KEY = "settings_autoconnect";
    public static final String COMP_NAME = "compName";
    public static final String CONNECTION_DB = "connectionPref";
    public static final String CONN_TYPE_KEY = "CONNECTION_TYPE";
    public static final String DISCONNECT_ON_TIMER = "disconnectOnTimer";
    public static final String FISTS_START = "first_start";
    public static final String HINT_DB = "hintPref";
    public static final String MANUALLY_SERVERS = "manuallyServers";
    public static final String NEED_EXIT_BUTTON = "setting_show_exit_button";
    public static final String SERVER_IP = "serverIP";
    public static final String SERVER_PORT = "serverPort";
    public static final String SESSIONS_5MIN_CNT = "sessions_5min_cnt";
    public static final String SHOW_BATTERY = "show_battery";
    public static final String SHOW_HINT = "showhint";
    public static final String SMOOTH_VIDEO_KEY = "setting_smooth_video_key";
    public static final String SOUND_ENABLED = "sound_enabled";
    public static final String ZOOM = "settings_zoom";
    private static SharedPreferences prefs;

    static {
        prefs = null;
        prefs = PreferenceManager.getDefaultSharedPreferences(IDisplayApp.getInstance());
        PreferenceManager.setDefaultValues(IDisplayApp.getInstance(), R.xml.preferences, false);
    }

    public static void clearServerAutoconnectOptions() {
        setServerName(StringUtils.EMPTY);
        setServerIp(null);
        setServerPort(0);
    }

    public static void disableFirstStart() {
        Editor edit = prefs.edit();
        edit.putBoolean(FISTS_START, false);
        edit.commit();
    }

    public static boolean getBatteryWidgetEnabled() {
        return getBoolean(SHOW_BATTERY);
    }

    public static boolean getBoolean(String str) {
        return prefs.getBoolean(str, getDefaultBoolValueByKey(str));
    }

    public static boolean getConnectionType() {
        return getBoolean(CONN_TYPE_KEY);
    }

    private static boolean getDefaultBoolValueByKey(String str) {
        return AUTOCONNECT_KEY.equals(str) || SMOOTH_VIDEO_KEY.equals(str) || SHOW_BATTERY.equals(str) || SHOW_HINT.equals(str);
    }

    private static String getDefaultZoom() {
        return "1";
    }

    public static boolean getDisconnectOnTimer() {
        return getBoolean(DISCONNECT_ON_TIMER);
    }

    public static long getLong(String str) {
        return prefs.getLong(str, -1);
    }

    public static String getServerIp() {
        return getString(SERVER_IP);
    }

    public static String getServerName() {
        return getString(COMP_NAME);
    }

    public static long getServerPort() {
        return getLong(SERVER_PORT);
    }

    public static boolean getShowHint() {
        return getBoolean(SHOW_HINT);
    }

    public static boolean getSoundEnabled() {
        return getBoolean(SOUND_ENABLED);
    }

    public static String getString(String str) {
        return prefs.getString(str, StringUtils.EMPTY);
    }

    public static int getSuccessful5MinutesSettions() {
        return prefs.getInt(SESSIONS_5MIN_CNT, 0);
    }

    public static float getZoom() {
        String string = prefs.getString(ZOOM, getDefaultZoom());
        return string.equals("1.5") ? 1.5f : Float.valueOf(string).floatValue();
    }

    public static void initZoomValue() {
        Editor edit = prefs.edit();
        edit.putString(ZOOM, getDefaultZoom());
        edit.commit();
    }

    public static boolean isFirstStart() {
        return prefs.getBoolean(FISTS_START, true);
    }

    public static void setBoolean(String str, boolean z) {
        Editor edit = prefs.edit();
        edit.putBoolean(str, z);
        edit.commit();
    }

    public static void setConnectionType(boolean z) {
        setBoolean(CONN_TYPE_KEY, z);
    }

    public static void setDisconnectOnTimer(boolean z) {
        setBoolean(DISCONNECT_ON_TIMER, z);
    }

    public static void setLong(String str, long j) {
        Editor edit = prefs.edit();
        edit.putLong(str, j);
        edit.commit();
    }

    public static void setServerIp(String str) {
        setString(SERVER_IP, str);
    }

    public static void setServerName(String str) {
        setString(COMP_NAME, str);
    }

    public static void setServerPort(long j) {
        setLong(SERVER_PORT, j);
    }

    public static void setShowHint(boolean z) {
        setBoolean(SHOW_HINT, z);
    }

    public static void setString(String str, String str2) {
        Editor edit = prefs.edit();
        edit.putString(str, str2);
        edit.commit();
    }

    public static void setSuccessful5MinutesSettions(int i) {
        Editor edit = prefs.edit();
        edit.putInt(SESSIONS_5MIN_CNT, i);
        edit.commit();
    }
}
