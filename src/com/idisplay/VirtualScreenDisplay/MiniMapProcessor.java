package com.idisplay.VirtualScreenDisplay;

import android.content.res.Resources;
import android.view.MotionEvent;
import com.idisplay.VirtualScreenDisplay.PanMiniMapGestureDetector.OnPanGestureListener;

public class MiniMapProcessor {
    private MiniMapRenderer miniMapRenderer;
    private PanMiniMapGestureDetector panMiniMapGestureDetector;

    public static interface IKeyboardInfo {
        boolean isShown();
    }

    public static interface IMimiMapButtonListener {
        void onClick();
    }

    public static interface IMiniMapRenderer {
        boolean isShown();
    }

    public MiniMapProcessor(OnPanGestureListener onPanGestureListener, Resources resources, IKeyboardInfo iKeyboardInfo) {
        this.panMiniMapGestureDetector = new PanMiniMapGestureDetector(onPanGestureListener, resources);
        this.panMiniMapGestureDetector.setKeyboardInfo(iKeyboardInfo);
    }

    public MiniMapRenderer getRenderer() {
        return this.miniMapRenderer;
    }

    public boolean onTouchEvent(MotionEvent motionEvent, float f, int i, int i2) {
        return this.panMiniMapGestureDetector.onTouchEvent(motionEvent, f, i, i2);
    }

    public void updateRenderer(MiniMapRenderer miniMapRenderer) {
        if (miniMapRenderer == null) {
            throw new IllegalArgumentException("renderer is null");
        }
        this.miniMapRenderer = miniMapRenderer;
        if (this.panMiniMapGestureDetector != null) {
            this.panMiniMapGestureDetector.setRenderer(this.miniMapRenderer);
            this.panMiniMapGestureDetector.setButtonClickListener(this.miniMapRenderer);
        }
    }
}
