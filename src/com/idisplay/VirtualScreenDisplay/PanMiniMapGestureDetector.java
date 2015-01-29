package com.idisplay.VirtualScreenDisplay;

import android.content.res.Resources;
import android.view.MotionEvent;
import com.idisplay.VirtualScreenDisplay.MiniMapProcessor.IKeyboardInfo;
import com.idisplay.VirtualScreenDisplay.MiniMapProcessor.IMimiMapButtonListener;
import com.idisplay.VirtualScreenDisplay.MiniMapProcessor.IMiniMapRenderer;
import org.apache.commons.lang.SystemUtils;

import seu.lab.matrix.R;

public class PanMiniMapGestureDetector {
    private static int buttonMul;
    private IMimiMapButtonListener buttonListener;
    private OnPanGestureListener listener;
    private IKeyboardInfo mKeyboardInfo;
    private boolean mMiniMapGesture;
    float maxSizeX;
    float maxSizeY;
    private IMiniMapRenderer renderer;

    public static interface OnPanGestureListener {
        void onPan(float f, float f2);
    }

    static {
        buttonMul = 6;
    }

    PanMiniMapGestureDetector(OnPanGestureListener onPanGestureListener, Resources resources) {
        this.listener = onPanGestureListener;
        float integer = ((float) resources.getInteger(R.integer.minimap_size_10x)) / 10.0f;
        this.maxSizeX = integer;
        this.maxSizeY = integer;
    }

    public boolean onTouchEvent(MotionEvent motionEvent, float f, int i, int i2) {
        float x = motionEvent.getX(0);
        float y = motionEvent.getY(0);
        float f2 = ((float) i) - ((((float) i) / 2.0f) * this.maxSizeX);
        float f3 = this.maxSizeY * (((float) i2) / 2.0f);
        float f4 = this.mKeyboardInfo.isShown() ? ((((float) i2) / 2.0f) * this.maxSizeY) / (2.0f * f) : SystemUtils.JAVA_VERSION_FLOAT;
        float f5 = (f3 - (2.0f * ((((float) i2) / 2.0f) * (this.maxSizeY / ((float) buttonMul))))) / 2.0f;
        if ((motionEvent.getAction() & 1) == 1 && this.mMiniMapGesture) {
            this.mMiniMapGesture = false;
            return true;
        } else if (x <= ((float) i) - ((((float) i) / 2.0f) * ((this.maxSizeX / ((float) buttonMul)) / 1.7f)) || y >= f3 - f5 || y <= f5) {
            if (this.renderer == null) {
                return false;
            }
            if (motionEvent.getAction() != 0 || (!this.renderer.isShown() || x <= f2 || y >= f3 + f4)) {
                this.mMiniMapGesture = false;
                return false;
            }
            this.mMiniMapGesture = true;
            if (!this.renderer.isShown()) {
                return false;
            }
            if (motionEvent.getAction() != 2 && motionEvent.getAction() != 0) {
                return (x > f2 && y < f4 + f3) || this.mMiniMapGesture;
            } else {
                if (!this.mMiniMapGesture) {
                    return false;
                }
                if (x < f2 || y > f4 + f3) {
                    return true;
                }
                if (!this.renderer.isShown()) {
                    return this.mMiniMapGesture;
                } else {
                    this.listener.onPan((((x - f2) * (((float) i) / (((float) i) - f2))) - ((float) (i / 2))) / (((float) i) / f), (((((float) i2) / f3) * y) - ((float) (i2 / 2))) / (((float) i2) / f));
                    return true;
                }
            }
        } else if (motionEvent.getAction() != 0) {
            return true;
        } else {
            this.mMiniMapGesture = false;
            this.buttonListener.onClick();
            return true;
        }
    }

    public void setButtonClickListener(IMimiMapButtonListener iMimiMapButtonListener) {
        this.buttonListener = iMimiMapButtonListener;
    }

    public void setKeyboardInfo(IKeyboardInfo iKeyboardInfo) {
        this.mKeyboardInfo = iKeyboardInfo;
    }

    public void setRenderer(IMiniMapRenderer iMiniMapRenderer) {
        this.renderer = iMiniMapRenderer;
    }
}
