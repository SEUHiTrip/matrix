package seu.lab.matrix.obj;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import seu.lab.matrix.R;
import seu.lab.matrix.R.drawable;

import com.google.vrtoolkit.cardboard.Eye;
import com.learnopengles.android.common.TextureHelper;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class Cube extends Object3D {

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

	public Cube(float[] _COORDS, float[] _COLORS, float[] _NORMALS,
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

	}

	@Override
	public void initParams(Context mActivityContext) {
        
		mTextureDataHandle = TextureHelper.loadTexture(mActivityContext, R.drawable.bumpy_bricks_public_domain);
		// Object first appears directly in front of user.
		Matrix.setIdentityM(mModelCube, 0);
		Matrix.translateM(mModelCube, 0, 0, 0, -4f);// -mObjectDistance);
	}

	@Override
	public void draw(float[] mMVP, float[] mLightPosInEyeSpace,
			float[] mModelView, Eye eye) {
		GLES20.glUseProgram(mProgram);

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
		GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

		Matrix.rotateM(mModelCube, 0, 0.3f, 1.0f, 0.0f, 0.0f);
		
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

}
