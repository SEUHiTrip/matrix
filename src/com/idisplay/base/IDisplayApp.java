package com.idisplay.base;

import android.app.Application;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Printer;
import com.idisplay.util.Logger;
import com.idisplay.util.TopExceptionHandler;
import com.idisplay.util.Utils;
import org.apache.commons.lang.StringUtils;

public class IDisplayApp extends Application {
    public static final short FIRST_START_DATE = (short) 11;
    public static final short IS_RATED = (short) 12;
    public static final short O_DEVICE = (short) 8;
    public static final short O_IMEI = (short) 9;
    public static final short O_LAST_REGISTRATION_TIME = (short) 6;
    public static final short O_LICENSE_CODE = (short) 2;
    public static final short O_REGISTRATION_CODE = (short) 3;
    public static final short O_REGISTRATION_SKIP_COUNT = (short) 4;
    public static final short O_REGISTRATION_SKIP_ERROR_CONNECT_COUNT = (short) 5;
    public static final short O_SKIP_REGISTRATION = (short) 7;
    public static final short STARTS_APP_COUNT = (short) 10;
    private static IDisplayApp instance;

    public IDisplayApp() {
        instance = this;
    }

    private void dumpMainLooper(String str) {
        try {
            Looper mainLooper = getMainLooper();
            if (mainLooper != null) {
                Logger.d("Main Thread " + str + " Looper:");
                try {
                    mainLooper.dump(new Printer() {
                        public void println(String str) {
                            Logger.d(str);
                        }
                    }, StringUtils.EMPTY);
                    return;
                } catch (Throwable th) {
                    Logger.d("Main Thread " + str + " Looper error", th);
                }
            }
            Logger.e("Main Thread " + str + " Looper is NULL");
        } catch (Throwable th2) {
            Logger.d("Main Thread " + str + " get Looper error", th2);
        }
    }

    public static IDisplayApp getInstance() {
        if (instance == null) {
            synchronized (IDisplayApp.class) {
                try {
                } catch (Throwable th) {
                    while (true) {
                        break;
                    }
                    Class cls = IDisplayApp.class;
                }
            }
            if (instance == null) {
            	instance = new IDisplayApp();
            }
        }
        return instance;
    }

    public String getBranch() {
        return "Market";
    }

    public String getDPIAsString() {
        float f = (float) getResources().getDisplayMetrics().densityDpi;
        return f == 120.0f ? "ldpi" : f == 160.0f ? "mdpi" : f == 240.0f ? "hdpi" : f == 320.0f ? "xdpi" : "unknown: " + f;
    }

    public String getScreenSizeAsString() {
        StringBuilder stringBuilder = new StringBuilder();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        stringBuilder.append(displayMetrics.widthPixels).append("x").append(displayMetrics.heightPixels);
        return stringBuilder.toString();
    }

    public void onCreate() {
        super.onCreate();
        Logger.initLogger(this);
        Logger.i("---------------------------------\nStarting iDisplay app, v" + Utils.getApplicationVersion());
        dumpMainLooper("onCreate");

        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));
    }
}
