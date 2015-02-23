package com.idisplay.VirtualScreenDisplay;

import com.idisplay.util.Logger;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class FPSCounter {
    private static double currFPS;
    private static DecimalFormat fpsFormater;
    private static long frameCount;
    private static Map<Integer, Long> images;
    private static boolean sendFPSEnabled;
    private static boolean showFPSEnabled;
    private static boolean simpleFpsAck;
    private static long startTimeFPSCalc;

    static {
        fpsFormater = new DecimalFormat("FPS: #.##");
        images = new HashMap();
        sendFPSEnabled = true;
        showFPSEnabled = true;
        simpleFpsAck = true;
        startTimeFPSCalc = 0;
        frameCount = 0;
        currFPS = 0.0d;
    }

    public static String getCurrFormatedFPS() {
        return fpsFormater.format(currFPS);
    }

    public static void getImageFromServer(int i) {
        if (simpleFpsAck && sendFPSEnabled) {
            images.put(Integer.valueOf(i), Long.valueOf(System.currentTimeMillis()));
        }
    }

    public static void imageDrawComplete(int i) {
        if (sendFPSEnabled && !simpleFpsAck) {
            Long l = (Long) images.remove(Integer.valueOf(i));
            if (l != null) {
                sendFPStoServer(System.currentTimeMillis() - l.longValue());
            }
        }
        if (showFPSEnabled) {
            long currentTimeMillis = System.currentTimeMillis();
            if (startTimeFPSCalc == 0) {
                startTimeFPSCalc = currentTimeMillis;
            } else if (currentTimeMillis - startTimeFPSCalc < 5000) {
                frameCount = 1 + frameCount;
            } else {
                currFPS = (1000.0d * ((double) frameCount)) / ((double) (currentTimeMillis - startTimeFPSCalc));
                Logger.d(getCurrFormatedFPS());
                startTimeFPSCalc = currentTimeMillis;
                frameCount = 0;
            }
        }
    }

    public static void imageRenderComplete() {
        synchronized (FPSCounter.class) {
            try {
            } catch (Throwable th) {
                Class cls = FPSCounter.class;
            }
        }
        if (simpleFpsAck) {
            sendFPStoServer(0);
        } else {
            Logger.i("Old server. Image render completed already sent");
        }
    }

    public static void imageRenderComplete(int i, int i2) {
        synchronized (FPSCounter.class) {
            try {
            } catch (Throwable th) {
                Class cls = FPSCounter.class;
            }
        }
        if (simpleFpsAck) {
            sendFPStoServer(0);
        } else if (sendFPSEnabled) {
            Long valueOf = null;
            Long l = (Long) images.remove(Integer.valueOf(i));
            if (l == null) {
                valueOf = Long.valueOf(System.currentTimeMillis());
            }
            images.put(Integer.valueOf(i2), valueOf);
        }
    }

    public static boolean needToShowFPS() {
        return showFPSEnabled;
    }

    public static void removeImageData(int i) {
        if (simpleFpsAck) {
            images.remove(Integer.valueOf(i));
        }
    }

    private static void sendFPStoServer(long j) {
    	IDisplayConnection.ccMngr.sendFPS((int) j);
    }

    public static void setSimpleFpsAck(boolean z) {
        simpleFpsAck = z;
    }
}
