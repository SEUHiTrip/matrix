package com.idisplay.VirtualScreenDisplay;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import com.idisplay.util.ArrayImageContainer;
import com.idisplay.util.Logger;
import java.nio.ByteBuffer;
import javax.microedition.khronos.opengles.GL10;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.spi.ErrorCode;

public class OpenGlYuvView implements IIdisplayViewRenderer {
    int cnt;
    long current;
    String fragmentShaderCode;
    private ArrayImageContainer mArrayImageContainer;
    IIdisplayViewRendererContainer mContainer;

    public OpenGlYuvView(IIdisplayViewRendererContainer iIdisplayViewRendererContainer) {
        this.fragmentShaderCode = "precision highp float;\nvarying highp vec2 textureCoordinate;\nuniform sampler2D videoFrame;\nuniform sampler2D videoFrame2;\nuniform sampler2D videoFrame3;\nuniform float rightEdge;\nuniform float topEdge;\nvoid main(void) {\nif(textureCoordinate.x < 0.0 || textureCoordinate.x > rightEdge || textureCoordinate.y < 0.0 || textureCoordinate.y > topEdge){\ngl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);\n} else {\nhighp vec3 yuv;\nhighp vec3 rgb; \nhighp float nx,ny; \nnx=(textureCoordinate.x);\nny=(textureCoordinate.y);\nyuv.x = texture2D( videoFrame, textureCoordinate ).r; \nyuv.y = texture2D( videoFrame2, vec2(nx,ny)).r-0.5; \nyuv.z = texture2D( videoFrame3, vec2(nx,ny)).r-0.5; \nrgb = mat3(1, 1, 1, 0, -.34414, 1.772, 1.402, -.71414, 0) * yuv; \ngl_FragColor = vec4(rgb, 1); \n}\n}";
        this.current = System.currentTimeMillis();
        this.cnt = 0;
        if (iIdisplayViewRendererContainer == null) {
            throw new IllegalArgumentException("container");
        }
        this.mContainer = iIdisplayViewRendererContainer;
    }

    public void fillTextures(GL10 gl10, int i, int[] iArr) {
        if (this.mArrayImageContainer != null) {
            int strideX = this.mArrayImageContainer.getStrideX() * this.mArrayImageContainer.getStrideY();
            GLES20.glActiveTexture(33988);
            GLES20.glBindTexture(3553, iArr[4]);
            GLES20.glTexImage2D(3553, 0, 6409, this.mArrayImageContainer.getStrideX(), this.mArrayImageContainer.getStrideY(), 0, 6409, 5121, ByteBuffer.wrap(this.mArrayImageContainer.getDataY(), 0, strideX));
            GLES20.glActiveTexture(33989);
            GLES20.glBindTexture(3553, iArr[5]);
            GLES20.glTexImage2D(3553, 0, 6409, this.mArrayImageContainer.getStrideX() >> 1, this.mArrayImageContainer.getStrideY() >> 1, 0, 6409, 5121, ByteBuffer.wrap(this.mArrayImageContainer.getDataU(), 0, strideX >> 2));
            GLES20.glActiveTexture(33990);
            GLES20.glBindTexture(3553, iArr[6]);
            GLES20.glTexImage2D(3553, 0, 6409, this.mArrayImageContainer.getStrideX() >> 1, this.mArrayImageContainer.getStrideY() >> 1, 0, 6409, 5121, ByteBuffer.wrap(this.mArrayImageContainer.getDataV(), 0, strideX >> 2));
            GLES20.glUniform1i(GLES20.glGetUniformLocation(i, "videoFrame"), ErrorCode.FILE_OPEN_FAILURE);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(i, "videoFrame2"), ErrorCode.MISSING_LAYOUT);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(i, "videoFrame3"), ErrorCode.ADDRESS_PARSE_FAILURE);
        }
    }

    public String getFragmentShader() {
        return this.fragmentShaderCode;
    }

    public int getNumOfTextures() {
        return ErrorCode.CLOSE_FAILURE;
    }

    public boolean isBitmapRenderer() {
        return false;
    }

    public boolean isRenderDataAvaliable() {
        return this.mArrayImageContainer != null;
    }

    public boolean isYuvRenderer() {
        return true;
    }

    public void setBitmap(Bitmap bitmap) {
        throw new NotImplementedException("setBitmap not implemented");
    }

    public void setPixels(ArrayImageContainer arrayImageContainer) {
        boolean z = false;
        if (System.currentTimeMillis() - this.current > 1000) {
            this.current = System.currentTimeMillis();
            Logger.e("cnt " + this.cnt);
            this.cnt = 0;
        }
        this.cnt++;
        if (!(this.mArrayImageContainer != null && this.mContainer.getDataStrideX() == arrayImageContainer.getStrideX() && this.mContainer.getDataStrideY() == arrayImageContainer.getStrideY() && this.mContainer.getDataWidth() == arrayImageContainer.getWidth() && this.mContainer.getDataHeight() == arrayImageContainer.getHeight())) {
            this.mArrayImageContainer = arrayImageContainer;
            this.mContainer.setDataStrideX(this.mArrayImageContainer.getStrideX());
            this.mContainer.setDataStrideY(this.mArrayImageContainer.getStrideY());
            this.mContainer.setDataWidth(this.mArrayImageContainer.getWidth());
            this.mContainer.setDataHeight(this.mArrayImageContainer.getHeight());
            z = true;
        }
        this.mArrayImageContainer = arrayImageContainer;
        this.mContainer.renderDataUpdated(z);
    }
}
