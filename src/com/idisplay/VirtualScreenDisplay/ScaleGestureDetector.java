package com.idisplay.VirtualScreenDisplay;

import android.content.Context;
import android.util.FloatMath;
import android.view.MotionEvent;
import com.idisplay.VirtualScreenDisplay.ScaleGestureDetector.OnScaleGestureListener;
import com.idisplay.util.Logger;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.Filter;

public class ScaleGestureDetector {
    private static final float PRESSURE_THRESHOLD = 0.67f;
    private static final int ZOOM_PAN_TOLERANCE = 20;
    String className;
    private float mBottomFingerBeginX;
    private float mBottomFingerBeginY;
    private float mBottomFingerCurrX;
    private float mBottomFingerCurrY;
    private float mBottomPanX;
    private float mBottomPanY;
    private Context mContext;
    private MotionEvent mCurrEvent;
    private float mCurrFingerDiffX;
    private float mCurrFingerDiffY;
    private float mCurrLen;
    private float mCurrPressure;
    private float mFocusX;
    private float mFocusY;
    private boolean mGestureInProgress;
    private OnScaleGestureListener mListener;
    private float mPanX;
    private float mPanY;
    private boolean mPointerOneUp;
    private boolean mPointerTwoUp;
    private MotionEvent mPrevEvent;
    private float mPrevFingerDiffX;
    private float mPrevFingerDiffY;
    private float mPrevLen;
    private float mPrevPressure;
    private float mScaleFactor;
    private long mTimeDelta;
    private float mTopFingerBeginX;
    private float mTopFingerBeginY;
    private float mTopFingerCurrX;
    private float mTopFingerCurrY;
    private boolean mTopFingerIsPointer1;
    private float mTopPanX;
    private float mTopPanY;

    public static interface OnScaleGestureListener {
        boolean onScale(ScaleGestureDetector scaleGestureDetector);

        boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector);

        void onScaleEnd(ScaleGestureDetector scaleGestureDetector, boolean z);
    }

    public class SimpleOnScaleGestureListener implements OnScaleGestureListener {
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            return true;
        }

        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector, boolean z) {
        }
    }

    public ScaleGestureDetector(Context context, OnScaleGestureListener onScaleGestureListener) {
        this.className = "ScaleGestureDetector";
        this.mContext = context;
        this.mListener = onScaleGestureListener;
    }

    private void reset() {
        if (this.mPrevEvent != null) {
            this.mPrevEvent.recycle();
            this.mPrevEvent = null;
        }
        if (this.mCurrEvent != null) {
            this.mCurrEvent.recycle();
            this.mCurrEvent = null;
        }
    }

    private void setContext(MotionEvent motionEvent) {
        if (this.mCurrEvent != null) {
            this.mCurrEvent.recycle();
        }
        this.mCurrEvent = MotionEvent.obtain(motionEvent);
        this.mCurrLen = -1.0f;
        this.mPrevLen = -1.0f;
        this.mScaleFactor = -1.0f;
        MotionEvent motionEvent2 = this.mPrevEvent;
        float x = motionEvent2.getX(0);
        float y = motionEvent2.getY(0);
        float x2 = motionEvent2.getX(1);
        float y2 = motionEvent2.getY(1);
        float x3 = motionEvent.getX(0);
        float y3 = motionEvent.getY(0);
        float x4 = motionEvent.getX(1);
        float y4 = motionEvent.getY(1);
        x = x2 - x;
        y = y2 - y;
        x2 = x4 - x3;
        y2 = y4 - y3;
        this.mPrevFingerDiffX = x;
        this.mPrevFingerDiffY = y;
        this.mCurrFingerDiffX = x2;
        this.mCurrFingerDiffY = y2;
        this.mFocusX = (0.5f * x2) + x3;
        this.mFocusY = (0.5f * y2) + y3;
        this.mTimeDelta = motionEvent.getEventTime() - motionEvent2.getEventTime();
        this.mCurrPressure = motionEvent.getPressure(0) + motionEvent.getPressure(1);
        this.mPrevPressure = motionEvent2.getPressure(1) + motionEvent2.getPressure(0);
        this.mBottomFingerCurrX = x3;
        this.mBottomFingerCurrY = y3;
        this.mTopFingerCurrX = x4;
        this.mTopFingerCurrY = y4;
        this.mBottomPanX = getBottomFingerDeltaX();
        this.mBottomPanY = getBottomFingerDeltaY();
        this.mTopPanX = getTopFingerDeltaX();
        this.mTopPanY = getTopFingerDeltaY();
        if (this.mTopPanX - this.mBottomPanX <= 20.0f) {
            Logger.e(this.className + ":NOT ZOOM");
            this.mPanX = this.mBottomPanX;
        } else {
            Logger.e(this.className + ":ZOOM");
            this.mPanX = 0.0f;
        }
        if (this.mTopPanY - this.mBottomPanY <= 20.0f) {
            this.mPanY = this.mBottomPanY;
        } else {
            this.mPanY = 0.0f;
        }
    }

    public float getBottomFingerDeltaX() {
        return !this.mTopFingerIsPointer1 ? this.mTopFingerCurrX - this.mTopFingerBeginX : this.mBottomFingerCurrX - this.mBottomFingerBeginX;
    }

    public float getBottomFingerDeltaY() {
        return !this.mTopFingerIsPointer1 ? this.mTopFingerCurrY - this.mTopFingerBeginY : this.mBottomFingerCurrY - this.mBottomFingerBeginY;
    }

    public float getBottomFingerX() {
        return !this.mTopFingerIsPointer1 ? this.mTopFingerCurrX : this.mBottomFingerCurrX;
    }

    public float getBottomFingerY() {
        return !this.mTopFingerIsPointer1 ? this.mTopFingerCurrY : this.mBottomFingerCurrY;
    }

    public float getCurrentSpan() {
        if (this.mCurrLen == -1.0f) {
            float f = this.mCurrFingerDiffX;
            float f2 = this.mCurrFingerDiffY;
            this.mCurrLen = FloatMath.sqrt((f * f) + (f2 * f2));
        }
        return this.mCurrLen;
    }

    public long getEventTime() {
        return this.mCurrEvent.getEventTime();
    }

    public float getFocusX() {
        return this.mFocusX;
    }

    public float getFocusY() {
        return this.mFocusY;
    }

    public float getPanX() {
        return this.mPanX;
    }

    public float getPanY() {
        return this.mPanY;
    }

    public float getPreviousSpan() {
        if (this.mPrevLen == -1.0f) {
            float f = this.mPrevFingerDiffX;
            float f2 = this.mPrevFingerDiffY;
            this.mPrevLen = FloatMath.sqrt((f * f) + (f2 * f2));
        }
        return this.mPrevLen;
    }

    public float getScaleFactor() {
        if (this.mScaleFactor == -1.0f) {
            this.mScaleFactor = getCurrentSpan() / getPreviousSpan();
        }
        return this.mScaleFactor;
    }

    public long getTimeDelta() {
        return this.mTimeDelta;
    }

    public float getTopFingerDeltaX() {
        return this.mTopFingerIsPointer1 ? this.mTopFingerCurrX - this.mTopFingerBeginX : this.mBottomFingerCurrX - this.mBottomFingerBeginX;
    }

    public float getTopFingerDeltaY() {
        return this.mTopFingerIsPointer1 ? this.mTopFingerCurrY - this.mTopFingerBeginY : this.mBottomFingerCurrY - this.mBottomFingerBeginY;
    }

    public float getTopFingerX() {
        return this.mTopFingerIsPointer1 ? this.mTopFingerCurrX : this.mBottomFingerCurrX;
    }

    public float getTopFingerY() {
        return this.mTopFingerIsPointer1 ? this.mTopFingerCurrY : this.mBottomFingerCurrY;
    }

    public boolean isInProgress() {
        return this.mGestureInProgress;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean z = true;
        int action = motionEvent.getAction();
        if (this.mGestureInProgress) {
            boolean z2;
            int i = 0;
            switch (action) {
                case ErrorCode.GENERIC_FAILURE:
                    if (motionEvent.getPointerCount() == 1) {
                        this.mGestureInProgress = false;
                        return false;
                    }
                    this.mPointerOneUp = true;
                    this.mPointerTwoUp = true;
                    z2 = true;
                    if (this.mPointerOneUp) {
                        this.mPointerTwoUp = true;
                    }
                    this.mPointerOneUp = true;
                    if (motionEvent.getPointerCount() == 2) {
                        if (action == 262) {
                            if (this.mPointerTwoUp) {
                                this.mPointerOneUp = true;
                            }
                            this.mPointerTwoUp = true;
                        }
                        if (this.mPointerOneUp || this.mPointerTwoUp) {
                            setContext(motionEvent);
                            i = ((65280 & action) >> 8) != 0 ? 1 : 0;
                            this.mFocusX = motionEvent.getX(i);
                            this.mFocusY = motionEvent.getY(i);
                            this.mListener.onScaleEnd(this, false);
                            this.mGestureInProgress = false;
                            reset();
                            return true;
                        }
                    }
                    return z2;
                case Filter.ACCEPT:
                    this.mPointerOneUp = true;
                    this.mPointerTwoUp = true;
                    z2 = true;
                    if (this.mPointerOneUp) {
                        this.mPointerTwoUp = true;
                    }
                    this.mPointerOneUp = true;
                    if (motionEvent.getPointerCount() == 2) {
                        if (action == 262) {
                            if (this.mPointerTwoUp) {
                                this.mPointerOneUp = true;
                            }
                            this.mPointerTwoUp = true;
                        }
                        setContext(motionEvent);
                        if (((65280 & action) >> 8) != 0) {
                        }
                        this.mFocusX = motionEvent.getX(i);
                        this.mFocusY = motionEvent.getY(i);
                        this.mListener.onScaleEnd(this, false);
                        this.mGestureInProgress = false;
                        reset();
                        return true;
                    }
                    return z2;
                case ErrorCode.FLUSH_FAILURE:
                    if (motionEvent.getPointerCount() != 2) {
                        return false;
                    }
                    setContext(motionEvent);
                    if (this.mCurrPressure / this.mPrevPressure > 0.67f && this.mListener.onScale(this)) {
                        this.mPrevEvent.recycle();
                        this.mPrevEvent = MotionEvent.obtain(motionEvent);
                    }
                    return true;
                case ErrorCode.CLOSE_FAILURE:
                    this.mListener.onScaleEnd(this, true);
                    this.mGestureInProgress = false;
                    reset();
                    return true;
                case ErrorCode.ADDRESS_PARSE_FAILURE:
                    z2 = false;
                    if (this.mPointerOneUp) {
                        this.mPointerTwoUp = true;
                    }
                    this.mPointerOneUp = true;
                    if (motionEvent.getPointerCount() == 2) {
                        if (action == 262) {
                            if (this.mPointerTwoUp) {
                                this.mPointerOneUp = true;
                            }
                            this.mPointerTwoUp = true;
                        }
                        setContext(motionEvent);
                        if (((65280 & action) >> 8) != 0) {
                        }
                        this.mFocusX = motionEvent.getX(i);
                        this.mFocusY = motionEvent.getY(i);
                        this.mListener.onScaleEnd(this, false);
                        this.mGestureInProgress = false;
                        reset();
                        return true;
                    }
                    return z2;
                case 262:
                    z2 = false;
                    if (motionEvent.getPointerCount() == 2) {
                        if (action == 262) {
                            if (this.mPointerTwoUp) {
                                this.mPointerOneUp = true;
                            }
                            this.mPointerTwoUp = true;
                        }
                        setContext(motionEvent);
                        if (((65280 & action) >> 8) != 0) {
                        }
                        this.mFocusX = motionEvent.getX(i);
                        this.mFocusY = motionEvent.getY(i);
                        this.mListener.onScaleEnd(this, false);
                        this.mGestureInProgress = false;
                        reset();
                        return true;
                    }
                    return z2;
                default:
                    return false;
            }
        }
        if (action == 5) {
            Logger.e(this.className + ":ACTION_POINTER_1_DOWN");
        }
        if (action == 261) {
            Logger.e(this.className + ":ACTION_POINTER_2_DOWN");
        }
        if ((action != 5 && action != 261) || motionEvent.getPointerCount() < 2) {
            return false;
        }
        Logger.e(this.className + ":multi-finger gesture");
        this.mBottomFingerBeginX = motionEvent.getX(0);
        this.mBottomFingerBeginY = motionEvent.getY(0);
        this.mTopFingerBeginX = motionEvent.getX(1);
        this.mTopFingerBeginY = motionEvent.getY(1);
        this.mTopFingerCurrX = this.mTopFingerBeginX;
        this.mTopFingerCurrY = this.mTopFingerBeginY;
        this.mBottomFingerCurrX = this.mBottomFingerBeginX;
        this.mBottomFingerCurrY = this.mBottomFingerBeginY;
        this.mBottomPanX = getBottomFingerDeltaX();
        this.mBottomPanY = getBottomFingerDeltaX();
        this.mTopPanX = getTopFingerDeltaX();
        this.mTopPanY = getTopFingerDeltaY();
        if (this.mTopPanX - this.mBottomPanX <= 20.0f) {
            Logger.e(this.className + ":NOT ZOOM");
            this.mPanX = this.mBottomPanX;
        } else {
            Logger.e(this.className + ":ZOOM");
            this.mPanX = 0.0f;
        }
        if (this.mTopPanY - this.mBottomPanY <= 20.0f) {
            this.mPanY = this.mBottomPanY;
        } else {
            this.mPanY = 0.0f;
        }
        this.mPointerOneUp = false;
        this.mPointerTwoUp = false;
        reset();
        if (this.mTopFingerBeginY > this.mBottomFingerBeginY) {
            z = false;
        }
        this.mTopFingerIsPointer1 = z;
        this.mPrevEvent = MotionEvent.obtain(motionEvent);
        this.mTimeDelta = 0;
        setContext(motionEvent);
        this.mGestureInProgress = this.mListener.onScaleBegin(this);
        return false;
    }

    public void resetGesture() {
        this.mGestureInProgress = false;
    }
}
