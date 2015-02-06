package com.idisplay.VirtualScreenDisplay;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import com.idisplay.util.ArrayImageContainer;
import com.idisplay.util.Utils;
import com.idisplay.vp8.NativeUtils;
import java.nio.ByteBuffer;
import javax.microedition.khronos.opengles.GL10;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.spi.ErrorCode;

public class OpenGlBitmapRenderer implements IIdisplayViewRenderer {
    String fragmentShaderCode;
    Bitmap mBitmap;
    IIdisplayViewRendererContainer mContainer;
    private byte[] mData;
    private boolean mDataAvaliable;
    private int[] mIntData;

    public OpenGlBitmapRenderer(IIdisplayViewRendererContainer iIdisplayViewRendererContainer) {
        this.fragmentShaderCode = "precision highp float;\nvarying highp vec2 textureCoordinate;\nuniform sampler2D videoFrame;\nuniform float rightEdge;\nuniform float topEdge;\nvoid main(void) {\nif(textureCoordinate.x < 0.0 || textureCoordinate.x > rightEdge || textureCoordinate.y < 0.0 || textureCoordinate.y > topEdge){\ngl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);\n} else {\nhighp vec4 argb;\nargb = texture2D(videoFrame, textureCoordinate);\ngl_FragColor=vec4(argb[2], argb[1], argb[0], 1.0);\n}\n}";
        this.mDataAvaliable = false;
        this.mIntData = new int[1];
        if (iIdisplayViewRendererContainer == null) {
            throw new IllegalArgumentException("container");
        }
        this.mContainer = iIdisplayViewRendererContainer;
    }

    private byte[] convertTextureData() {
        this.mBitmap.getPixels(this.mIntData, 0, this.mContainer.getDataStrideX(), 0, 0, this.mContainer.getDataWidth(), this.mContainer.getDataHeight());
        NativeUtils.convertArrays(this.mIntData, this.mData, this.mIntData.length);
        return this.mData;
    }

    public void fillTextures(int i, int[] iArr) {
        if (!this.mBitmap.isRecycled()) {
            convertTextureData();
            GLES20.glActiveTexture(33988);
            GLES20.glBindTexture(3553, iArr[4]);
            GLES20.glTexImage2D(3553, 0, 6408, this.mContainer.getDataStrideX(), this.mContainer.getDataStrideY(), 0, 6408, 5121, ByteBuffer.wrap(this.mData, 0, this.mData.length));
            GLES20.glUniform1i(GLES20.glGetUniformLocation(i, "videoFrame"), ErrorCode.FILE_OPEN_FAILURE);
        }
    }

    public String getFragmentShader() {
        return this.fragmentShaderCode;
    }

    public int getNumOfTextures() {
        return 1;
    }

    public boolean isBitmapRenderer() {
        return true;
    }

    public boolean isRenderDataAvaliable() {
        return this.mDataAvaliable;
    }

    public boolean isYuvRenderer() {
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setBitmap(android.graphics.Bitmap paramBitmap) {
		int i = paramBitmap.getWidth();
		int j = paramBitmap.getHeight();
		int k = this.mContainer.getDataHeight();

		boolean bool;
		if ((this.mDataAvaliable) && (this.mContainer.getDataWidth() == i) && (k!=j)) {
			bool = false;
		} else {
			this.mContainer.setDataWidth(i);
			this.mContainer.setDataHeight(j);
			this.mContainer.setDataStrideX(Utils.calculateStride(i));
			this.mContainer.setDataStrideY(Utils.calculateStride(j));
			this.mIntData = new int[this.mContainer.getDataStrideX()
					* this.mContainer.getDataStrideY()];
			this.mData = new byte[4 * this.mIntData.length];
			bool = true;
		}
		this.mBitmap = paramBitmap;
		this.mContainer.renderDataUpdated(bool);
		this.mDataAvaliable = true;
    }

    public void setPixels(ArrayImageContainer arrayImageContainer) {
        throw new NotImplementedException("set pixels not implemented");
    }
}
