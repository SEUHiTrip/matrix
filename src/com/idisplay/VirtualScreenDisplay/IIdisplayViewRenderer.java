package com.idisplay.VirtualScreenDisplay;

import android.graphics.Bitmap;
import com.idisplay.util.ArrayImageContainer;
import javax.microedition.khronos.opengles.GL10;

public interface IIdisplayViewRenderer {
    void fillTextures(GL10 gl10, int i, int[] iArr);

    String getFragmentShader();

    int getNumOfTextures();

    boolean isBitmapRenderer();

    boolean isRenderDataAvaliable();

    boolean isYuvRenderer();

    void setBitmap(Bitmap bitmap);

    void setPixels(ArrayImageContainer arrayImageContainer);
}
