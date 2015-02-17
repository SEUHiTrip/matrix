package com.idisplay.VirtualScreenDisplay;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.google.vrtoolkit.cardboard.Eye;
import com.idisplay.util.ArrayImageContainer;
import com.idisplay.util.Logger;
import com.learnopengles.android.common.RawResourceReader;
import com.learnopengles.android.common.ShaderHelper;

import java.nio.ByteBuffer;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.spi.ErrorCode;

import seu.lab.matrix.R;

public class OpenGlYuvViewRenderer implements IIdisplayViewRenderer {
	int cnt;
	long current;
	String fragmentShaderCode;
	private ArrayImageContainer mArrayImageContainer;
	IIdisplayViewRendererContainer mContainer;

	public OpenGlYuvViewRenderer(
			IIdisplayViewRendererContainer iIdisplayViewRendererContainer,
			Context mContext) {
		this.fragmentShaderCode = RawResourceReader.readTextFileFromRawResource(mContext, R.raw.idisplay_yuv_fragment);
		this.current = System.currentTimeMillis();
		this.cnt = 0;
		if (iIdisplayViewRendererContainer == null) {
			throw new IllegalArgumentException("container");
		}
		this.mContainer = iIdisplayViewRendererContainer;
	}

	public void fillTextures(int i, int[] iArr) {
		if (this.mArrayImageContainer != null) {
			int x = mArrayImageContainer.getStrideX();
			int y = mArrayImageContainer.getStrideY();
			simpleFillTextures(i, iArr, 0, x * y, x, y);
		}
	}

	public void fillTexturesWithEye(int i, int[] iArr, Eye eye) {
		if (this.mArrayImageContainer != null) {
			int x = mArrayImageContainer.getStrideX();
			int y = mArrayImageContainer.getStrideY();
			int len = x * y;

			if (ConnectionActivity.currentMode.type == ConnectionActivity.ConnectionType.Single) {
				simpleFillTextures(i, iArr, 0, len, x, y);
			} else {
				if (eye.getType() == Eye.Type.LEFT) {
					simpleFillTextures(i, iArr, 0, len >> 1, x, y >> 1);
				} else if (eye.getType() == Eye.Type.RIGHT) {
					simpleFillTextures(i, iArr, len >> 1, len >> 1, x, y >> 1);
				}
			}
		}
	}

	public void simpleFillTextures(int i, int[] iArr, int start, int count,
			int width, int height) {

		GLES20.glActiveTexture(33988);
		GLES20.glBindTexture(3553, iArr[4]);
		GLES20.glTexImage2D(3553, 0, 6409, width, height, 0, 6409, 5121,
				ByteBuffer.wrap(this.mArrayImageContainer.getDataY(), start,
						count));

		width = width >> 1;
		height = height >> 1;
		GLES20.glActiveTexture(33989);
		GLES20.glBindTexture(3553, iArr[5]);
		GLES20.glTexImage2D(3553, 0, 6409, width, height, 0, 6409, 5121,
				ByteBuffer.wrap(this.mArrayImageContainer.getDataU(),
						start >> 2, count >> 2));

		GLES20.glActiveTexture(33990);
		GLES20.glBindTexture(3553, iArr[6]);
		GLES20.glTexImage2D(3553, 0, 6409, width, height, 0, 6409, 5121,
				ByteBuffer.wrap(this.mArrayImageContainer.getDataV(),
						start >> 2, count >> 2));

		GLES20.glUniform1i(GLES20.glGetUniformLocation(i, "videoFrame"), 4);
		GLES20.glUniform1i(GLES20.glGetUniformLocation(i, "videoFrame2"), 5);
		GLES20.glUniform1i(GLES20.glGetUniformLocation(i, "videoFrame3"), 6);
	}

	public String getFragmentShader() {
		return fragmentShaderCode;
	}

	public int getNumOfTextures() {
		return 3;
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
		if (!(this.mArrayImageContainer != null
				&& this.mContainer.getDataStrideX() == arrayImageContainer
						.getStrideX()
				&& this.mContainer.getDataStrideY() == arrayImageContainer
						.getStrideY()
				&& this.mContainer.getDataWidth() == arrayImageContainer
						.getWidth() && this.mContainer.getDataHeight() == arrayImageContainer
				.getHeight())) {
			this.mArrayImageContainer = arrayImageContainer;
			this.mContainer.setDataStrideX(this.mArrayImageContainer
					.getStrideX());
			this.mContainer.setDataStrideY(this.mArrayImageContainer
					.getStrideY());
			this.mContainer.setDataWidth(this.mArrayImageContainer.getWidth());
			this.mContainer
					.setDataHeight(this.mArrayImageContainer.getHeight());
			z = true;
		}
		this.mArrayImageContainer = arrayImageContainer;
		this.mContainer.renderDataUpdated(z);
	}
}
