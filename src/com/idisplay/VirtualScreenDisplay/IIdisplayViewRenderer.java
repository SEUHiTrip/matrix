package com.idisplay.VirtualScreenDisplay;

import android.graphics.Bitmap;

import com.google.vrtoolkit.cardboard.Eye;
import com.idisplay.util.ArrayImageContainer;
import javax.microedition.khronos.opengles.GL10;

public interface IIdisplayViewRenderer {
    void fillTextures(int i, int[] iArr);
    void fillTexturesWithEye(int i, int[] iArr, Eye eye);

    String getFragmentShader();

    int getNumOfTextures();

    boolean isBitmapRenderer();

    boolean isRenderDataAvaliable();

    boolean isYuvRenderer();

    void setBitmap(Bitmap bitmap);

    void setPixels(ArrayImageContainer arrayImageContainer);
}
