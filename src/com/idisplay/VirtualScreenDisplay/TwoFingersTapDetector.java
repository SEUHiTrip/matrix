package com.idisplay.VirtualScreenDisplay;

import android.view.MotionEvent;

public class TwoFingersTapDetector {
    private boolean mDownPressed;
    private OnTwoFingersTapListener mListener;
    private boolean mOneFingerUp;
    private long mOneFingerUpTime;
    private long mPrevTime;
    private float x1;
    private float x2;
    private float y1;
    private float y2;

    public static interface OnTwoFingersTapListener {
        void onTwoFingersTap();
    }

    public TwoFingersTapDetector(OnTwoFingersTapListener onTwoFingersTapListener) {
        this.mDownPressed = false;
        this.mPrevTime = System.currentTimeMillis();
        if (onTwoFingersTapListener == null) {
            throw new NullPointerException("listener");
        }
        this.mListener = onTwoFingersTapListener;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.mOneFingerUp && motionEvent.getPointerCount() == 1 && (motionEvent.getAction() & 1) == 1 && System.currentTimeMillis() - this.mOneFingerUpTime < 300) {
            this.mOneFingerUp = false;
            this.mListener.onTwoFingersTap();
            return true;
        }
        if (System.currentTimeMillis() - this.mOneFingerUpTime > 300) {
            this.mOneFingerUp = false;
        }
        if ((motionEvent.getAction() & 5) != 5 && (motionEvent.getAction() & 6) != 6) {
            return false;
        }
        if (motionEvent.getPointerCount() != 2) {
            this.mDownPressed = false;
            return false;
        } else if ((motionEvent.getAction() & 5) == 5) {
            this.mDownPressed = true;
            this.mPrevTime = System.currentTimeMillis();
            this.x1 = motionEvent.getX(0);
            this.x2 = motionEvent.getX(1);
            this.y1 = motionEvent.getY(0);
            this.y2 = motionEvent.getY(1);
            return false;
        } else if ((motionEvent.getAction() & 6) != 6 || !this.mDownPressed || System.currentTimeMillis() - this.mPrevTime >= 200 || Math.abs(motionEvent.getX(0) - this.x1) >= 5.0f || Math.abs(motionEvent.getX(1) - this.x2) >= 5.0f || Math.abs(motionEvent.getY(0) - this.y1) >= 5.0f || Math.abs(motionEvent.getY(1) - this.y2) >= 5.0f) {
            this.mDownPressed = false;
            return false;
        } else {
            this.mOneFingerUp = true;
            this.mOneFingerUpTime = System.currentTimeMillis();
            this.mDownPressed = false;
            return true;
        }
    }
}
