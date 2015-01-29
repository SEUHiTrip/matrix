package com.idisplay.VirtualScreenDisplay;

import android.view.MotionEvent;

public class ScrollGestureDetector {
    public static int DETECTED;
    private static float EDGE_LENGTH;
    public static int NOT_DETECTED;
    public static int RESET_SCALE;
    private static float SCROLL_LENGTH;
    private static float START_LENGTH;
    private boolean mGestureAcknowledged;
    private boolean mGestureStarted;
    private OnScrollGestureListener mListener;
    private float prX1;
    private float prX2;
    private float prY1;
    private float prY2;

    public static interface OnScrollGestureListener {
        void onScroll(int i);
    }

    static {
        NOT_DETECTED = 0;
        DETECTED = 1;
        RESET_SCALE = 2;
        START_LENGTH = 10.0f;
        EDGE_LENGTH = 7.0f;
        SCROLL_LENGTH = 12.0f;
    }

    public ScrollGestureDetector(OnScrollGestureListener onScrollGestureListener) {
        this.mGestureStarted = false;
        this.mGestureAcknowledged = false;
        if (onScrollGestureListener == null) {
            throw new NullPointerException("listener");
        }
        this.mListener = onScrollGestureListener;
    }

    private float getAngle(float f, float f2, float f3, float f4) {
        float atan = 57.3f * ((float) Math.atan((double) ((f2 - f4) / (f - f3))));
        if (!Float.isNaN(atan)) {
            return atan;
        }
        return ((f2 > f4 ? 1 : (f2 == f4 ? 0 : -1)) > 0 ? 1 : null) != null ? 90.0f : -90.0f;
    }

    private float getLength(float f, float f2, float f3, float f4) {
        float f5 = f - f3;
        float f6 = f2 - f4;
        return (float) Math.sqrt((double) ((f5 * f5) + (f6 * f6)));
    }

    private boolean isScrollGesture(float f, float f2, float f3, float f4) {
        float length = getLength((f + f2) / 2.0f, (f3 + f4) / 2.0f, (this.prX1 + this.prX2) / 2.0f, (this.prY1 + this.prY2) / 2.0f);
        float abs = Math.abs(getLength(f, f3, f2, f4) - getLength(this.prX1, this.prY1, this.prX2, this.prY2));
        if (length >= abs) {
            return true;
        }
        float abs2 = Math.abs(getAngle(f, f3, this.prX1, this.prY1) - getAngle(f2, f4, this.prX2, this.prY2));
        return abs2 > 30.0f ? false : ((double) (Math.max(length, abs) - Math.min(length, abs))) < 6.0d + (1.3d * ((double) Math.min(length, abs))) ? true : abs2 < 10.0f && Math.max(length, abs) - Math.min(length, abs) < 20.0f;
    }

    private void storePrevValues(MotionEvent motionEvent) {
        if (motionEvent.getPointerCount() != 2) {
            throw new IllegalArgumentException("pointers count should be 2");
        }
        this.prX1 = motionEvent.getX(0);
        this.prY1 = motionEvent.getY(0);
        this.prX2 = motionEvent.getX(1);
        this.prY2 = motionEvent.getY(1);
    }

    public int onTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getPointerCount() == 2 && (motionEvent.getAction() & 261) == 261) {
            this.mGestureStarted = true;
            this.mGestureAcknowledged = false;
            storePrevValues(motionEvent);
            return NOT_DETECTED;
        } else if (motionEvent.getPointerCount() != 2 || (motionEvent.getAction() & 262) == 262) {
            this.mGestureStarted = false;
            this.mGestureAcknowledged = false;
            return NOT_DETECTED;
        } else if (!this.mGestureStarted) {
            return NOT_DETECTED;
        } else {
            float x = motionEvent.getX(0);
            float y = motionEvent.getY(0);
            float x2 = motionEvent.getX(1);
            float y2 = motionEvent.getY(1);
            if (!this.mGestureAcknowledged) {
                if (getLength(x, y, this.prX1, this.prY1) > START_LENGTH || getLength(x2, y2, this.prX2, this.prY2) > START_LENGTH) {
                    if (isScrollGesture(x, x2, y, y2)) {
                        this.mGestureAcknowledged = true;
                    } else {
                        this.mGestureAcknowledged = false;
                        this.mGestureStarted = false;
                        return NOT_DETECTED;
                    }
                }
            }
            if (!this.mGestureAcknowledged) {
                return NOT_DETECTED;
            }
            if (Math.abs(y - this.prY1) <= SCROLL_LENGTH || Math.abs(y2 - this.prY2) <= SCROLL_LENGTH) {
                return RESET_SCALE;
            }
            this.mListener.onScroll((int) ((((y + y2) / 2.0f) - ((this.prY1 + this.prY2) / 2.0f)) / 10.0f));
            storePrevValues(motionEvent);
            return RESET_SCALE | DETECTED;
        }
    }
}
