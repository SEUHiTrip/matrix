package seu.lab.matrix.obj;

import com.google.vrtoolkit.cardboard.Eye;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class Floor extends Object3D {

	public float[] mModelFloor = new float[16];
	public float mFloorDepth = 20f;
	
	public Floor(float[] _COORDS, float[] _COLORS, float[] _NORMALS,
			int _vertexShader, int _fragShader) {
		super(_COORDS, _COLORS, _NORMALS, _vertexShader, _fragShader);
		
	}

	@Override
	public void initParams(Context context) {

	}

	@Override
	public void draw(float[] mMVP, float[] mLightPosInEyeSpace, float[] mModelView, Eye eye) {
        GLES20.glUseProgram(mProgram);

        mModelParam = GLES20.glGetUniformLocation(mProgram, "u_Model");
        mModelViewParam = GLES20.glGetUniformLocation(mProgram, "u_MVMatrix");
        mModelViewProjectionParam = GLES20.glGetUniformLocation(mProgram, "u_MVP");
        mLightPosParam = GLES20.glGetUniformLocation(mProgram, "u_LightPos");

        mPositionParam = GLES20.glGetAttribLocation(mProgram, "a_Position");
        mNormalParam = GLES20.glGetAttribLocation(mProgram, "a_Normal");
        mColorParam = GLES20.glGetAttribLocation(mProgram, "a_Color");

        GLES20.glEnableVertexAttribArray(mPositionParam);
        GLES20.glEnableVertexAttribArray(mNormalParam);
        GLES20.glEnableVertexAttribArray(mColorParam);
        
		Matrix.setIdentityM(mModelFloor, 0);
		Matrix.translateM(mModelFloor, 0, 0, -mFloorDepth, 0); // Floor appears
																// below user.
        
        // Set ModelView, MVP, position, normals, and color.
        GLES20.glUniform3fv(mLightPosParam, 1, mLightPosInEyeSpace, 0);
        GLES20.glUniformMatrix4fv(mModelParam, 1, false, mModelFloor, 0);
        GLES20.glUniformMatrix4fv(mModelViewParam, 1, false, mModelView, 0);
        GLES20.glUniformMatrix4fv(mModelViewProjectionParam, 1, false,
        		mMVP, 0);
        GLES20.glVertexAttribPointer(mPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, 0, mVertices);
        GLES20.glVertexAttribPointer(mNormalParam, 3, GLES20.GL_FLOAT, false, 0,
                mNormals);
        GLES20.glVertexAttribPointer(mColorParam, 4, GLES20.GL_FLOAT, false, 0, mColors);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
	}

    /**
     * Draw the floor.
     *
     * This feeds in data for the floor into the shader. Note that this doesn't feed in data about
     * position of the light, so if we rewrite our code to draw the floor first, the lighting might
     * look strange.
     */

}
