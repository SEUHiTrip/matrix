package com.idisplay.VirtualScreenDisplay;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import com.idisplay.VirtualScreenDisplay.MiniMapProcessor.IMimiMapButtonListener;
import com.idisplay.VirtualScreenDisplay.MiniMapProcessor.IMiniMapRenderer;
import com.idisplay.util.Utils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.spi.ErrorCode;

import seu.lab.matrix.R;

public class MiniMapRenderer implements IMiniMapRenderer, IMimiMapButtonListener {
    private static int buttonMul;
    private int[] buffer;
    private FloatBuffer d;
    private FloatBuffer e;
    private String fragmentButtonShaderCode;
    private String fragmentRectangleShaderCode;
    private ByteBuffer indexBuffer;
    private byte[] indices;
    private FloatBuffer mButtonBuffer;
    private int mButtonProgram;
    private int mProgram;
    private float mRatio;
    private Resources mRes;
    private float mScreenBorderX;
    private float mScreenBorderY;
    private float mScreenIntBorderX;
    private float mScreenIntBorderY;
    private float mZoomBorderX;
    private float mZoomBorderY;
    private float mZoomIntBorderX;
    private float mZoomIntBorderY;
    private ZoomState mZoomState;
    private FloatBuffer mZoomedBuffer;
    private float maxSizeX;
    private float maxSizeY;
    private boolean needReloadButton;
    private float sizeX;
    private float sizeY;

    static {
        buttonMul = 6;
    }

    public MiniMapRenderer(FloatBuffer floatBuffer, String str, byte[] bArr, ByteBuffer byteBuffer, int i, int i2, ZoomState zoomState, int[] iArr, Resources resources) {
        this.fragmentRectangleShaderCode = "precision highp float;\nvarying highp vec2 textureCoordinate;\nuniform sampler2D frame;\nuniform float borderWidthX;\nuniform float borderWidthY;\nuniform float borderWidthX1;\nuniform float borderWidthY1;\nuniform float zoomed;\nvoid main(void) {\nif (textureCoordinate[0] > borderWidthX1 && textureCoordinate[0] < (1.0-borderWidthX1) && textureCoordinate[1] > borderWidthY1 && textureCoordinate[1] < (1.0-borderWidthY1)){\ngl_FragColor = vec4(0.0, 0.0, 0.0, zoomed);\n} else {\nif (textureCoordinate[0] < borderWidthX || textureCoordinate[1] < borderWidthY || textureCoordinate[0] > (1.0-borderWidthX) || textureCoordinate[1] > (1.0 - borderWidthY)){\ngl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);\n} else {\ngl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);\n}\n}\n}\n";
        this.fragmentButtonShaderCode = "precision highp float;\nvarying highp vec2 textureCoordinate;\nuniform sampler2D frame;\nvoid main(void) {\ngl_FragColor = (texture2D(frame, textureCoordinate));\n}";
        this.maxSizeX = 0.5f;
        this.maxSizeY = 0.5f;
        float integer = ((float) 5) / 10.0f;
        this.maxSizeX = integer;
        this.maxSizeY = integer;
        this.indices = bArr;
        this.indexBuffer = byteBuffer;
        this.buffer = iArr;
        this.mRes = resources;
        this.sizeX = this.maxSizeX;
        this.sizeY = this.maxSizeY;
        this.mZoomState = zoomState;
        loadButtonVertices();
        this.e = floatBuffer;
        this.mProgram = Programs.loadProgram(str, this.fragmentRectangleShaderCode);
        this.mButtonProgram = Programs.loadProgram(str, this.fragmentButtonShaderCode);
        this.mScreenBorderX = (1.0f / ((float) i)) * (2.0f / this.sizeX);
        this.mScreenBorderY = (1.0f / ((float) i2)) * (2.0f / this.sizeY);
        this.mScreenIntBorderX = this.mScreenBorderX * 2.0f;
        this.mScreenIntBorderY = this.mScreenBorderY * 2.0f;
        initSize();
        loadButtonTexture(false);
    }

    private FloatBuffer getZoomedFloatBuffer(float f, float f2, float f3) {
        float[] zoomedVertices = getZoomedVertices(f, f2, f3);
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(zoomedVertices.length * 4);
        allocateDirect.order(ByteOrder.nativeOrder());
        FloatBuffer asFloatBuffer = allocateDirect.asFloatBuffer();
        asFloatBuffer.put(zoomedVertices);
        asFloatBuffer.position(0);
        return asFloatBuffer;
    }

    private float[] getZoomedVertices(float f, float f2, float f3) {
        float[] translatePointToRef = translatePointToRef(SystemUtils.JAVA_VERSION_FLOAT, SystemUtils.JAVA_VERSION_FLOAT, f, f2, f3);
        float[] translatePointToRef2 = translatePointToRef(SystemUtils.JAVA_VERSION_FLOAT, 1.0f, f, f2, f3);
        float[] translatePointToRef3 = translatePointToRef(1.0f, SystemUtils.JAVA_VERSION_FLOAT, f, f2, f3);
        float[] translatePointToRef4 = translatePointToRef(1.0f, 1.0f, f, f2, f3);
        float[] translatePointToAbsolute = translatePointToAbsolute(translatePointToRef);
        float[] translatePointToAbsolute2 = translatePointToAbsolute(translatePointToRef2);
        float[] translatePointToAbsolute3 = translatePointToAbsolute(translatePointToRef3);
        translatePointToRef4 = translatePointToAbsolute(translatePointToRef4);
        return new float[]{translatePointToAbsolute2[0], translatePointToAbsolute2[1], translatePointToRef4[0], translatePointToRef4[1], translatePointToAbsolute[0], translatePointToAbsolute[1], translatePointToAbsolute3[0], translatePointToAbsolute3[1]};
    }

    private void initSize() {
        float[] fArr = new float[]{1.0f - this.sizeX, 1.0f - this.sizeY, 1.0f, 1.0f - this.sizeY, 1.0f - this.sizeX, 1.0f, 1.0f, 1.0f};
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(fArr.length * 4);
        allocateDirect.order(ByteOrder.nativeOrder());
        this.d = allocateDirect.asFloatBuffer();
        this.d.put(fArr);
        this.d.position(0);
        updatePanZoom();
    }

    private void loadButtonTexture(boolean z) {
//        GLES20.glActiveTexture(33986);
//        GLES20.glBindTexture(3553, this.buffer[2]);
//        Bitmap decodeResource = BitmapFactory.decodeResource(this.mRes, z ? R.drawable.btn_leftarrow : R.drawable.btn_rightarrow);
//        GLES20.glTexImage2D(3553, 0, 6408, decodeResource.getWidth(), decodeResource.getHeight(), 0, 6408, 5121, Utils.loadTextureFromBitmap(decodeResource));
    }

    private void loadButtonVertices() {
        float f = (this.sizeY - (this.mRatio * ((this.sizeY * 2.0f) / ((float) buttonMul)))) / 2.0f;
        float f2 = ((this.sizeY / ((float) buttonMul)) / this.mRatio) / 1.7f;
        float[] fArr = new float[]{1.0f - f2, (1.0f - this.sizeY) + f, 1.0f, (1.0f - this.sizeY) + f, 1.0f - f2, 1.0f - f, 1.0f, 1.0f - f};
        ByteBuffer allocateDirect = ByteBuffer.allocateDirect(fArr.length * 4);
        allocateDirect.order(ByteOrder.nativeOrder());
        this.mButtonBuffer = allocateDirect.asFloatBuffer();
        this.mButtonBuffer.put(fArr);
        this.mButtonBuffer.position(0);
    }

    private float[] translatePointToAbsolute(float[] fArr) {
        return new float[]{(1.0f - this.sizeX) + (fArr[0] * this.sizeX), 1.0f - (fArr[1] * this.sizeY)};
    }

    private float[] translatePointToRef(float f, float f2, float f3, float f4, float f5) {
        float f6 = (1.0f - (1.0f / f3)) / 2.0f;
        return new float[]{((f / f3) + f6) + (f4 / f3), (f6 + (f2 / f3)) + (f5 / f3)};
    }

    public boolean isShown() {
        return this.mZoomState.getKeyboardShown() || (((double) this.mZoomState.getZoom()) != 1.0d && this.sizeX >= this.maxSizeX);
    }

    public void onClick() {
        if (this.sizeX == this.maxSizeX && this.sizeY == this.maxSizeY) {
            this.sizeX = 0.0f;
            this.sizeY = 0.0f;
        } else {
            this.sizeX = this.maxSizeX;
            this.sizeY = this.maxSizeY;
        }
        this.needReloadButton = true;
        initSize();
    }

    public void onDrawFrame(GL10 gl10, float f) {
        if (this.mZoomState.getZoom() != 1.0f || this.mZoomState.getKeyboardShown()) {
            if (this.needReloadButton) {
                boolean z = (this.sizeX == this.maxSizeX || this.sizeY == this.maxSizeY) ? false : true;
                loadButtonTexture(z);
                this.needReloadButton = false;
            }
            GLES20.glUseProgram(this.mProgram);
            GLES20.glUniform1f(GLES20.glGetUniformLocation(this.mProgram, "borderWidthX"), this.mScreenBorderX);
            GLES20.glUniform1f(GLES20.glGetUniformLocation(this.mProgram, "borderWidthY"), this.mScreenBorderY);
            GLES20.glUniform1f(GLES20.glGetUniformLocation(this.mProgram, "borderWidthX1"), this.mScreenIntBorderX);
            GLES20.glUniform1f(GLES20.glGetUniformLocation(this.mProgram, "borderWidthY1"), this.mScreenIntBorderY);
            GLES20.glUniform1f(GLES20.glGetUniformLocation(this.mProgram, "zoomed"), SystemUtils.JAVA_VERSION_FLOAT);
            int glGetAttribLocation = GLES20.glGetAttribLocation(this.mProgram, "position");
            GLES20.glEnableVertexAttribArray(glGetAttribLocation);
            GLES20.glVertexAttribPointer(glGetAttribLocation, ErrorCode.FLUSH_FAILURE, 5126, false, 0, this.d);
            glGetAttribLocation = GLES20.glGetAttribLocation(this.mProgram, "inputTextureCoordinate");
            GLES20.glEnableVertexAttribArray(glGetAttribLocation);
            GLES20.glVertexAttribPointer(glGetAttribLocation, ErrorCode.FLUSH_FAILURE, 5126, false, 0, this.e);
            gl10.glDrawElements(ErrorCode.FILE_OPEN_FAILURE, this.indices.length, 5121, this.indexBuffer);
            int glGetAttribLocation2 = GLES20.glGetAttribLocation(this.mProgram, "position");
            GLES20.glUniform1f(GLES20.glGetUniformLocation(this.mProgram, "borderWidthX"), this.mZoomBorderX);
            GLES20.glUniform1f(GLES20.glGetUniformLocation(this.mProgram, "borderWidthY"), this.mZoomBorderY);
            GLES20.glUniform1f(GLES20.glGetUniformLocation(this.mProgram, "borderWidthX1"), this.mZoomIntBorderX);
            GLES20.glUniform1f(GLES20.glGetUniformLocation(this.mProgram, "borderWidthY1"), this.mZoomIntBorderY);
            GLES20.glUniform1f(GLES20.glGetUniformLocation(this.mProgram, "zoomed"), 0.2f);
            GLES20.glEnableVertexAttribArray(glGetAttribLocation2);
            GLES20.glVertexAttribPointer(glGetAttribLocation2, 2, 5126, false, 0, this.mZoomedBuffer);
            gl10.glDrawElements(ErrorCode.FILE_OPEN_FAILURE, this.indices.length, 5121, this.indexBuffer);
            GLES20.glUseProgram(this.mButtonProgram);
            if (this.mRatio != f) {
                this.mRatio = f;
                loadButtonVertices();
            }
            GLES20.glUseProgram(this.mButtonProgram);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(this.mButtonProgram, "frame"), ErrorCode.FLUSH_FAILURE);
            glGetAttribLocation2 = GLES20.glGetAttribLocation(this.mButtonProgram, "position");
            GLES20.glEnableVertexAttribArray(glGetAttribLocation2);
            GLES20.glVertexAttribPointer(glGetAttribLocation2, 2, 5126, false, 0, this.mButtonBuffer);
            GLES20.glEnableVertexAttribArray(glGetAttribLocation);
            GLES20.glVertexAttribPointer(glGetAttribLocation, ErrorCode.FLUSH_FAILURE, 5126, false, 0, this.e);
            glGetAttribLocation = GLES20.glGetAttribLocation(this.mButtonProgram, "inputTextureCoordinate");
            GLES20.glEnableVertexAttribArray(glGetAttribLocation);
            GLES20.glVertexAttribPointer(glGetAttribLocation, ErrorCode.FLUSH_FAILURE, 5126, false, 0, this.e);
            gl10.glDrawElements(ErrorCode.FILE_OPEN_FAILURE, this.indices.length, 5121, this.indexBuffer);
        }
    }

    public void updatePanZoom() {
        this.mZoomedBuffer = getZoomedFloatBuffer(this.mZoomState.getZoom(), this.mZoomState.getPanX(), this.mZoomState.getPanY());
        this.mZoomBorderX = (this.mScreenBorderX * this.mZoomState.getZoom()) * 1.01f;
        this.mZoomBorderY = (this.mScreenBorderY * this.mZoomState.getZoom()) * 1.01f;
        this.mZoomIntBorderX = this.mZoomBorderX * 2.01f;
        this.mZoomIntBorderY = this.mZoomBorderY * 2.01f;
    }
}
