package seu.lab.matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import com.google.vrtoolkit.cardboard.Eye;
import com.idisplay.VirtualScreenDisplay.ConnectionActivity;
import com.idisplay.VirtualScreenDisplay.FPSCounter;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection;
import com.idisplay.VirtualScreenDisplay.IIdisplayViewRendererContainer;
import com.idisplay.VirtualScreenDisplay.ZoomState;
import com.idisplay.util.ArrayImageContainer;
import com.idisplay.util.ImageContainer;
import com.idisplay.util.Logger;
import com.learnopengles.android.common.TextureHelper;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

public class ScreenCube extends Object3D implements IDisplayProgram {

	protected float[] FOUND_COLORS;
	protected FloatBuffer mFoundColors;
	protected FloatBuffer mCubeTextureCoordinates;
	private boolean isLookingAtObject = false;

	public float mObjectDistance = 8f;
	public float[] mModelCube = new float[16];

	protected float[] TEXTURE_COORDINATES;
	private int mTextureCoordinateHandle;
	private int mTextureUniformHandle;
	private final int mTextureCoordinateDataSize = 2;
	private int mTextureDataHandle;

	int cnt;
	long current;

	private int mWidth = 1024;
	private int mHeight = 1024;
	private int mStrideX = 1024;
	private int mStrideY = 1024;

	private ArrayImageContainer mArrayImageContainer;

	// ========================================

	private ImageContainer mCursorImage;

	private boolean mRendererChanged;

	private int mCursorWidth;

	private int mCursorHeigth;

	private boolean isCursorDirty = true;

	private boolean isDirty = false;

	private boolean mShowCursor;
	private float mCursorMulX;
	private float mCursorMulY;

	float mCursorX = -2.0f;
	float mCursorY = -2.0f;

	private ZoomState mState;

	private FloatBuffer a;
	private FloatBuffer b;
	// private FloatBuffer background;
	// private FloatBuffer backgroundTexture;
	private int[] buffer;
	private FloatBuffer c;
	private FloatBuffer d;
	private FloatBuffer e;

	private float mRightEdge;

	private boolean mNotNativeRatio = false;

	private float mTopEdge;

	// ========================================

	IIdisplayViewRendererContainer mContainer = new IIdisplayViewRendererContainer() {
		public int getDataHeight() {
			return 1024;
		}

		public int getDataStrideX() {
			return 1024;
		}

		public int getDataStrideY() {
			return 1024;
		}

		public int getDataWidth() {
			return 1024;
		}

		public void renderDataUpdated(boolean z) {
			if (z) {
				reInitTextureBuffer();
			}
			isDirty = true;
			FPSCounter.imageRenderComplete();
			// TODO requestRender();
		}

		public void setDataHeight(int i) {
			mHeight = 1024;
		}

		public void setDataStrideX(int i) {
			mStrideX = 1024;
		}

		public void setDataStrideY(int i) {
			mStrideY = 1024;
		}

		public void setDataWidth(int i) {
			mWidth = 1024;
		}
	};

	public ScreenCube(float[] _COORDS, float[] _COLORS, float[] _NORMALS,
			float[] _FOUND_COLORS, float[] _TEXTURE_COORDINATES,
			int _vertexShader, int _gridShader) {

		super(_COORDS, _COLORS, _NORMALS, _vertexShader, _gridShader);

		FOUND_COLORS = _FOUND_COLORS;
		TEXTURE_COORDINATES = _TEXTURE_COORDINATES;

		ByteBuffer bbFoundColors = ByteBuffer
				.allocateDirect(FOUND_COLORS.length * mBytesPerFloat);
		bbFoundColors.order(ByteOrder.nativeOrder());
		mFoundColors = bbFoundColors.asFloatBuffer();
		mFoundColors.put(FOUND_COLORS);
		mFoundColors.position(0);

		mCubeTextureCoordinates = ByteBuffer
				.allocateDirect(TEXTURE_COORDINATES.length * mBytesPerFloat)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mCubeTextureCoordinates.put(TEXTURE_COORDINATES).position(0);

		float[] fArr = new float[] { 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
				0.0f };
		ByteBuffer allocateDirect = ByteBuffer.allocateDirect(fArr.length * 4);
		allocateDirect.order(ByteOrder.nativeOrder());
		e = allocateDirect.asFloatBuffer();
		e.put(fArr);
		e.position(0);
	}

	@Override
	public void initParams(Context mActivityContext) {

		mTextureDataHandle = TextureHelper.loadTexture(mActivityContext,
				R.drawable.bumpy_bricks_public_domain);

		mShowCursor = false;
		mRendererChanged = true;
		isDirty = true;
		
		// Object first appears directly in front of user.
		Matrix.setIdentityM(mModelCube, 0);
		Matrix.translateM(mModelCube, 0, 0, 0, -4f);// -mObjectDistance);
	}

	@Override
	public void draw(float[] mMVP, float[] mLightPosInEyeSpace,
			float[] mModelView, Eye eye) {
		GLES20.glUseProgram(mProgram);

		rendererChanged();
		fillTextures(mProgram, buffer);

		mPositionParam = GLES20.glGetAttribLocation(mProgram, "a_Position");
		mNormalParam = GLES20.glGetAttribLocation(mProgram, "a_Normal");
		mColorParam = GLES20.glGetAttribLocation(mProgram, "a_Color");
		mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram,
				"a_TexCoordinate");
		mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram,
				"u_Texture");

		mModelParam = GLES20.glGetUniformLocation(mProgram, "u_Model");
		mModelViewParam = GLES20.glGetUniformLocation(mProgram, "u_MVMatrix");
		mModelViewProjectionParam = GLES20.glGetUniformLocation(mProgram,
				"u_MVP");
		mLightPosParam = GLES20.glGetUniformLocation(mProgram, "u_LightPos");

		// Set the active texture unit to texture unit 0.
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

		// Bind the texture to this unit.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

		// Tell the texture uniform sampler to use this texture in the shader by
		// binding to texture unit 0.
		GLES20.glUniform1i(mTextureUniformHandle, 0);

		GLES20.glEnableVertexAttribArray(mPositionParam);
		GLES20.glEnableVertexAttribArray(mNormalParam);
		GLES20.glEnableVertexAttribArray(mColorParam);
		// GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

		//Matrix.rotateM(mModelCube, 0, 0.3f, 1.0f, 0.0f, 0.0f);

		GLES20.glUniform3fv(mLightPosParam, 1, mLightPosInEyeSpace, 0);

		// Set the Model in the shader, used to calculate lighting
		GLES20.glUniformMatrix4fv(mModelParam, 1, false, mModelCube, 0);

		// Set the ModelView in the shader, used to calculate lighting
		GLES20.glUniformMatrix4fv(mModelViewParam, 1, false, mModelView, 0);

		// Set the position of the
		GLES20.glVertexAttribPointer(mPositionParam, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false, 0, mVertices);

		GLES20.glVertexAttribPointer(mTextureCoordinateHandle,
				mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0,
				mCubeTextureCoordinates);

		GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

		// Set the ModelViewProjection matrix in the shader.
		GLES20.glUniformMatrix4fv(mModelViewProjectionParam, 1, false, mMVP, 0);

		// Set the normal positions of the , again for shading
		GLES20.glVertexAttribPointer(mNormalParam, 3, GLES20.GL_FLOAT, false,
				0, mNormals);
		GLES20.glVertexAttribPointer(mColorParam, 4, GLES20.GL_FLOAT, false, 0,
				isLookingAtObject ? mFoundColors : mColors);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
	}

	/**
	 * Draw the cube.
	 * 
	 * We've set all of our transformation matrices. Now we simply pass them
	 * into the shader.
	 */

	public void setLookingAtObject(boolean b) {
		isLookingAtObject = b;
	}

	public void fillTextures(int i, int[] iArr) {
		if (mArrayImageContainer != null) {
			int x = mArrayImageContainer.getStrideX();
			int y = mArrayImageContainer.getStrideY();
			simpleFillTextures(i, iArr, 0, x * y, x, y);
		}
	}

	public void fillTexturesWithEye(int i, int[] iArr, Eye eye) {
		if (mArrayImageContainer != null) {
			int x = mArrayImageContainer.getStrideX();
			int y = mArrayImageContainer.getStrideY();
			int len = x * y;

			if (IDisplayConnection.currentMode.type == IDisplayConnection.ConnectionType.Single) {
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

	public void simpleFillTextures(int program, int[] iArr, int start,
			int count, int width, int height) {

		GLES20.glActiveTexture(GLES20.GL_TEXTURE4);
		GLES20.glBindTexture(3553, iArr[4]);
		GLES20.glTexImage2D(3553, 0, 6409, width, height, 0, 6409, 5121,
				ByteBuffer.wrap(mArrayImageContainer.getDataY(), start, count));

		width = width >> 1;
		height = height >> 1;
		GLES20.glActiveTexture(GLES20.GL_TEXTURE5);
		GLES20.glBindTexture(3553, iArr[5]);
		GLES20.glTexImage2D(3553, 0, 6409, width, height, 0, 6409, 5121,
				ByteBuffer.wrap(mArrayImageContainer.getDataU(), start >> 2,
						count >> 2));

		GLES20.glActiveTexture(GLES20.GL_TEXTURE6);
		GLES20.glBindTexture(3553, iArr[6]);
		GLES20.glTexImage2D(3553, 0, 6409, width, height, 0, 6409, 5121,
				ByteBuffer.wrap(mArrayImageContainer.getDataV(), start >> 2,
						count >> 2));

		GLES20.glUniform1i(
				GLES20.glGetUniformLocation(program, "u_videoFrame"), 4);
		GLES20.glUniform1i(
				GLES20.glGetUniformLocation(program, "u_videoFrame2"), 5);
		GLES20.glUniform1i(
				GLES20.glGetUniformLocation(program, "u_videoFrame3"), 6);

		GLES20.glUniform1f(
				GLES20.glGetUniformLocation(mProgram, "u_rightEdge"),
				mRightEdge);
		GLES20.glUniform1f(GLES20.glGetUniformLocation(mProgram, "u_topEdge"),
				mTopEdge);
	}

	public void setPixels(ArrayImageContainer arrayImageContainer) {
		boolean z = false;
		if (System.currentTimeMillis() - current > 1000) {
			current = System.currentTimeMillis();
			Logger.e("cnt " + cnt);
			cnt = 0;
		}
		cnt++;
		if (!(mArrayImageContainer != null
				&& mContainer.getDataStrideX() == arrayImageContainer
						.getStrideX()
				&& mContainer.getDataStrideY() == arrayImageContainer
						.getStrideY()
				&& mContainer.getDataWidth() == arrayImageContainer.getWidth() && mContainer
					.getDataHeight() == arrayImageContainer.getHeight())) {
			mArrayImageContainer = arrayImageContainer;
			mContainer.setDataStrideX(mArrayImageContainer.getStrideX());
			mContainer.setDataStrideY(mArrayImageContainer.getStrideY());
			mContainer.setDataWidth(mArrayImageContainer.getWidth());
			mContainer.setDataHeight(mArrayImageContainer.getHeight());
			z = true;
		}
		mArrayImageContainer = arrayImageContainer;
		mContainer.renderDataUpdated(z);
	}

	public boolean isRenderDataAvaliable() {
		return this.mArrayImageContainer != null;
	}

	// public void setZoomState(ZoomState zoomState) {
	// if (mState != null) {
	// mState.deleteObserver(this);
	// }
	// mState = zoomState;
	// mState.addObserver(this);
	// }

	public void setCursorPosition(float f, float f2) {
		if (isRenderDataAvaliable()) {
			float f3 = (((float) (mCursorWidth * 2)) / ((float) mWidth))
					/ mCursorMulX;
			float f4 = (((float) (mCursorHeigth * 2)) / ((float) mHeight))
					/ mCursorMulY;
			mCursorX = f;
			mCursorY = f2;
			float zoom = (mCursorX * mState.getZoom())
					- (mState.getPanX() * 2.0f);
			f3 = ((f3 + mCursorX) * mState.getZoom())
					- (mState.getPanX() * 2.0f);
			f4 = ((mCursorY - f4) * mState.getZoom())
					+ (mState.getPanY() * 2.0f);
			float zoom2 = (mCursorY * mState.getZoom())
					+ (mState.getPanY() * 2.0f);
			float[] fArr = new float[] { zoom, f4, f3, f4, zoom, zoom2, f3,
					zoom2 };
			ByteBuffer allocateDirect = ByteBuffer
					.allocateDirect(fArr.length * 4);
			allocateDirect.order(ByteOrder.nativeOrder());
			c = allocateDirect.asFloatBuffer();
			c.put(fArr);
			c.position(0);
			mShowCursor = true;
		}
	}

	private void updateScreenVerticles(float f, float f2, float f3) {
		float[] fArr = new float[] { (-1.0f * f) - (f2 * 2.0f),
				(-1.0f * f) + (f3 * 2.0f), (1.0f * f) - (f2 * 2.0f),
				(-1.0f * f) + (f3 * 2.0f), (-1.0f * f) - (f2 * 2.0f),
				(1.0f * f) + (f3 * 2.0f), (1.0f * f) - (f2 * 2.0f),
				(1.0f * f) + (f3 * 2.0f) };
		ByteBuffer allocateDirect = ByteBuffer.allocateDirect(fArr.length * 4);
		allocateDirect.order(ByteOrder.nativeOrder());
		a = allocateDirect.asFloatBuffer();
		a.put(fArr);
		a.position(0);
	}

	private void reInitCursorCalculation(float f, float f2) {
		float f3 = ((float) mWidth) / ((float) mHeight);
		float f4 = f / f2;
		mCursorMulX = 1.0f;
		mCursorMulY = 1.0f;
		if (f3 < f4) {
			mCursorMulX = (((float) mHeight) * f4) / ((float) mWidth);
		} else {
			mCursorMulY = (((float) mWidth) / f4) / ((float) mHeight);
		}
	}

	private void reInitTextureBuffer() {
		float width = (float) 1024;// TODO getWidth();
		float height = (float) 1024;// TODO getHeight();
		reInitTextureBuffer(width, height);
		reInitCursorCalculation(width, height);
	}

	private void reInitTextureBuffer(float f, float f2) {
		float f3;
		float f4 = 0.0f;
		float f5 = ((float) mWidth) / ((float) mStrideX);
		float f6 = ((float) mHeight) / ((float) mStrideY);
		float f7 = ((float) mWidth) / ((float) mHeight);
		float f8 = f / f2;
		if (f7 < f8) {
			f3 = ((float) mHeight) * f8;
			f5 = f3 / ((float) mStrideX);
			f3 = ((((float) mWidth) - f3) / ((float) mStrideX)) / 2.0f;
			f5 += f3;
		} else {
			f3 = ((float) mWidth) / f8;
			f6 = f3 / ((float) mStrideY);
			f3 = ((((float) mHeight) - f3) / ((float) mStrideY)) / 2.0f;
			f6 += f3;
			f4 = f3;
			f3 = 0.0f;
		}
		mNotNativeRatio = ((double) Math.abs(f7 - f8)) > 0.02d;
		mRightEdge = f5 + f3;
		mTopEdge = f6 + f4;
		float[] fArr = new float[] { f3, f6, f5, f6, f3, f4, f5, f4 };
		ByteBuffer allocateDirect = ByteBuffer.allocateDirect(fArr.length * 4);
		allocateDirect.order(ByteOrder.nativeOrder());
		b = allocateDirect.asFloatBuffer();
		b.put(fArr);
		b.position(0);
	}

	private void rendererChanged() {
		float integer = (10) / 10.0f;
		if (integer == 0.0f) {
			integer = 1.0f;
		}
//		float[] fArr = new float[] { ((0.01f * integer) / ratio) - 4.0f,
//				1.0f - ((0.042f * integer) * ratio),
//				((0.062f * integer) / ratio) - 4.0f,
//				1.0f - ((0.042f * integer) * ratio),
//				((0.01f * integer) / ratio) - 4.0f,
//				1.0f - ((0.01f * integer) * ratio),
//				((0.062f * integer) / ratio) - 4.0f,
//				1.0f - ((integer * 0.01f) * ratio) };
//		ByteBuffer allocateDirect = ByteBuffer.allocateDirect(fArr.length * 4);
//		allocateDirect.order(ByteOrder.nativeOrder());
//		d = allocateDirect.asFloatBuffer();
//		d.put(fArr);
//		d.position(0);
		if (buffer == null) {
			//GLES20.glDeleteTextures(buffer.length, buffer, 0);
			
			int numOfTextures = 3 + 4;
			buffer = new int[numOfTextures];
			GLES20.glGenTextures(numOfTextures, buffer, 0);
			for (int i = 0; i < numOfTextures; i++) {
				int i2 = buffer[i];
				Log.d("Texture", "Texture is at " + i2);
				GLES20.glBindTexture(3553, i2);
				GLES20.glTexParameteri(3553, 10242, 33071);
				GLES20.glTexParameteri(3553, 10243, 33071);
				GLES20.glTexParameteri(3553, 10240, 9729);
				GLES20.glTexParameteri(3553, 10241, 9729);
			}
		}

		isCursorDirty = true;

	}

	/**
	 * Find a new random position for the object.
	 * 
	 * We'll rotate it around the Y-axis so it's out of sight, and then up or
	 * down by a little bit.
	 */
	public void hide() {
		float[] rotationMatrix = new float[16];
		float[] posVec = new float[4];

		// First rotate in XZ plane, between 90 and 270 deg away, and scale so
		// that we vary
		// the object's distance from the user.
		float angleXZ = (float) Math.random() * 180 + 90;
		Matrix.setRotateM(rotationMatrix, 0, angleXZ, 0f, 1f, 0f);
		float oldObjectDistance = mObjectDistance;
		mObjectDistance = (float) Math.random() * 15 + 5;
		float objectScalingFactor = mObjectDistance / oldObjectDistance;
		Matrix.scaleM(rotationMatrix, 0, objectScalingFactor,
				objectScalingFactor, objectScalingFactor);
		Matrix.multiplyMV(posVec, 0, rotationMatrix, 0, mModelCube, 12);

		// Now get the up or down angle, between -20 and 20 degrees.
		float angleY = (float) Math.random() * 80 - 40; // Angle in Y plane,
														// between -40 and 40.
		angleY = (float) Math.toRadians(angleY);
		float newY = (float) Math.tan(angleY) * mObjectDistance;

		Matrix.setIdentityM(mModelCube, 0);
		Matrix.translateM(mModelCube, 0, posVec[0], newY, posVec[2]);
	}

	@Override
	public void onInstanceCursorPositionChange(int i, int i2) {
		if (!isRenderDataAvaliable()) {
			return;
		}
		if (i == -1 && i2 == -1) {
			mShowCursor = false;
			return;
		}
		setCursorPosition(((((float) (i * 2)) / ((float) mWidth)) - 1.0f)
				/ mCursorMulX,
				(1.0f - (((float) (i2 * 2)) / ((float) mHeight))) / mCursorMulY);
	}

	@Override
	public void onInstanceCursorImgChange(ImageContainer imageContainer) {
		mCursorImage = imageContainer;
		mCursorWidth = imageContainer.getWidth();
		mCursorHeigth = imageContainer.getHeight();
		isCursorDirty = true;
	}

}
