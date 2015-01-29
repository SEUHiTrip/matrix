package com.idisplay.VirtualScreenDisplay;

import com.idisplay.VirtualScreenDisplay.MiniMapProcessor.IKeyboardInfo;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.helpers.FileWatchdog;

public class ZoomState extends Observable implements IKeyboardInfo {
    private boolean mKeyboardOpened;
    private long mKeyboardShownTime;
    private boolean mKeyboardTimerFinished;
    private boolean mOnMeasureInvoked;
    private float mPanX;
    private float mPanY;
    private float mZoom;

    private float checkPan(float f, boolean z) {
        float f2 = (this.mZoom - 1.0f) / 2.0f;
        float f3 = f2 < -1.0f * f ? -1.0f * f2 : f;
        if (z) {
            if (f2 + 0.5f < f3) {
                f3 = f2 + 0.5f;
            }
        } else if (f2 < f3) {
            f3 = f2;
        }
        return this.mZoom + (2.0f * f3) < 0.0f ? -2.0f * this.mZoom : f3;
    }

    public float calculateZoomXOnZoomPercentage(float f, float f2) {
        return Math.min(f2, f2 * f);
    }

    public float calculateZoomYOnZoomPercentage(float f, float f2) {
        return Math.min(f2, f2 / f);
    }

    public boolean getKeyboardShown() {
        return this.mKeyboardOpened;
    }

    public float getPanX() {
        return this.mPanX;
    }

    public float getPanY() {
        return this.mPanY;
    }

    public float getZoom() {
        return this.mZoom;
    }

    public float getZoomX(float f) {
        return Math.min(this.mZoom, this.mZoom * f);
    }

    public float getZoomY(float f) {
        return Math.min(this.mZoom, this.mZoom / f);
    }

    public boolean isShown() {
        return getKeyboardShown();
    }

    public void setKeyboardShown() {
        this.mKeyboardShownTime = System.currentTimeMillis();
        this.mKeyboardOpened = true;
        this.mOnMeasureInvoked = false;
        this.mKeyboardTimerFinished = false;
        new Timer("keyboardWaiting").schedule(new TimerTask() {
            public void run() {
                ZoomState.this.mKeyboardTimerFinished = true;
                if (ZoomState.this.mOnMeasureInvoked) {
                    ZoomState.this.mKeyboardOpened = false;
                }
            }
        }, FileWatchdog.DEFAULT_DELAY);
    }

    public void setPanX(float f) {
        if (f != this.mPanX) {
            this.mPanX = checkPan(f, false);
            setChanged();
        }
    }

    public void setPanY(float f) {
        if (f != this.mPanY) {
            this.mPanY = checkPan(f, this.mKeyboardOpened);
            setChanged();
        }
    }

    public void setZoom(float f) {
        if (f < 1.0f) {
            f = 1.0f;
        }
        if (f == 1.0f) {
            this.mPanX = 0.0f;
            this.mPanY = 0.0f;
        }
        if (f != this.mZoom) {
            this.mZoom = f;
            this.mPanX = checkPan(this.mPanX, false);
            this.mPanY = checkPan(this.mPanY, this.mKeyboardOpened);
            setChanged();
        }
    }

    public void viewOnMeasure() {
        if (System.currentTimeMillis() - this.mKeyboardShownTime >= 500) {
            this.mOnMeasureInvoked = true;
            if (this.mKeyboardTimerFinished) {
                this.mKeyboardOpened = false;
            }
        }
    }
}
