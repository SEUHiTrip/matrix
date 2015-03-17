package seu.lab.matrix.test;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import com.learnopengles.android.common.GLCommon;
import com.learnopengles.android.common.RawResourceReader;
import com.learnopengles.android.common.ShaderHelper;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;

import seu.lab.matrix.CardboardOverlayView;
import seu.lab.matrix.R;
import seu.lab.matrix.R.id;
import seu.lab.matrix.R.layout;
import seu.lab.matrix.R.raw;
import seu.lab.matrix.obj.Cube;
import seu.lab.matrix.obj.Floor;
import seu.lab.matrix.obj.WorldLayoutData;

public class MatrixActivity extends CardboardActivity implements
		CardboardView.StereoRenderer {

	private static final String TAG = "MatrixActivity";

	private static final float Z_NEAR = 0.1f;
	private static final float Z_FAR = 100.0f;

	private static final float CAMERA_Z = 0.01f;
	private static final float TIME_DELTA = 0.3f;

	private static final float YAW_LIMIT = 0.12f;
	private static final float PITCH_LIMIT = 0.12f;

	// We keep the light always position just above the user.
	private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[] { 0.0f,
			2.0f, 0.0f, 1.0f };

	private final float[] mLightPosInEyeSpace = new float[4];

	private Cube cube;
	private Floor floor;

	private float[] mCamera;
	private float[] mView;
	private float[] mHeadView;
	private float[] mModelViewProjection;
	private float[] mModelView;

	private float[] initVec = { 0, 0, 0, 1.0f };
	private float[] objPositionVec = new float[4];

	private int mScore = 0;

	private Vibrator mVibrator;
	private CardboardOverlayView mOverlayView;

	/**
	 * Sets the view to our CardboardView and initializes the transformation
	 * matrices we will use to render our scene.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.common_ui);
		CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
		cardboardView.setRenderer(this);
		setCardboardView(cardboardView);
		
		//cardboardView.setDistortionCorrectionEnabled(false);
		
		mCamera = new float[16];
		mView = new float[16];
		mModelViewProjection = new float[16];
		mModelView = new float[16];
		mHeadView = new float[16];
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		mOverlayView = (CardboardOverlayView) findViewById(R.id.overlay);
		mOverlayView.show3DToast("Pull the magnet when you find an object.");
	}

	@Override
	public void onRendererShutdown() {
		Log.i(TAG, "onRendererShutdown");
	}

	@Override
	public void onSurfaceChanged(int width, int height) {
		Log.i(TAG, "onSurfaceChanged");
	}

	/**
	 * Creates the buffers we use to store information about the 3D world.
	 * 
	 * OpenGL doesn't use Java arrays, but rather needs data in a format it can
	 * understand. Hence we use ByteBuffers.
	 * 
	 * @param config
	 *            The EGL configuration used when creating the surface.
	 */
	@Override
	public void onSurfaceCreated(EGLConfig config) {
		Log.i(TAG, "onSurfaceCreated");
		
		// Use culling to remove back faces.
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		
		// Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		
		GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text
														// shows up well.
		int vertexShader = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER,
				RawResourceReader.readTextFileFromRawResource(
						getApplicationContext(), R.raw.light_vertex));
		int texVertexShader = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER,
				RawResourceReader.readTextFileFromRawResource(
						getApplicationContext(), R.raw.tex_light_vertex));
		int gridShader = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER,
				RawResourceReader.readTextFileFromRawResource(
						getApplicationContext(), R.raw.grid_fragment));
		int passthroughShader = ShaderHelper.compileShader(
				GLES20.GL_FRAGMENT_SHADER, RawResourceReader
						.readTextFileFromRawResource(getApplicationContext(),
								R.raw.passthrough_fragment));

		cube = new Cube(WorldLayoutData.CUBE_COORDS,
				WorldLayoutData.CUBE_COLORS, WorldLayoutData.CUBE_NORMALS,
				WorldLayoutData.CUBE_FOUND_COLORS,
				WorldLayoutData.TEXTURE_COORDINATES, texVertexShader,
				passthroughShader);

		GLCommon.checkGLError("Cube program");

		cube.initParams(getApplicationContext());

		GLCommon.checkGLError("Cube program params");

		floor = new Floor(WorldLayoutData.FLOOR_COORDS,
				WorldLayoutData.FLOOR_COLORS, WorldLayoutData.FLOOR_NORMALS,
				vertexShader, gridShader);

		GLCommon.checkGLError("Floor program");

		floor.initParams(getApplicationContext());

		GLCommon.checkGLError("Floor program params");

		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		GLCommon.checkGLError("onSurfaceCreated");
	}

	/**
	 * Prepares OpenGL ES before we draw a frame.
	 * 
	 * @param headTransform
	 *            The head transformation in the new frame.
	 */
	@Override
	public void onNewFrame(HeadTransform headTransform) {

		// Build the camera matrix and apply it to the ModelView.
		Matrix.setLookAtM(mCamera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f,
				0.0f, 1.0f, 0.0f);

		headTransform.getHeadView(mHeadView, 0);

		GLCommon.checkGLError("onReadyToDraw");
	}

	/**
	 * Draws a frame for an eye.
	 * 
	 * @param eye
	 *            The eye to render. Includes all required transformations.
	 */
	@Override
	public void onDrawEye(Eye eye) {
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		GLCommon.checkGLError("mColorParam");

		// Apply the eye transformation to the camera.
		Matrix.multiplyMM(mView, 0, eye.getEyeView(), 0, mCamera, 0);

		// Set the position of the light
		Matrix.multiplyMV(mLightPosInEyeSpace, 0, mView, 0,
				LIGHT_POS_IN_WORLD_SPACE, 0);

		// Build the ModelView and ModelViewProjection matrices
		// for calculating cube position and light.
		float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
		Matrix.multiplyMM(mModelView, 0, mView, 0, cube.mModelCube, 0);
		Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0, mModelView,
				0);

		cube.setLookingAtObject(false);
		cube.draw(mModelViewProjection, mLightPosInEyeSpace, mModelView, eye);
		GLCommon.checkGLError("cube.draw");

		// Set mModelView for the floor, so we draw floor in the correct
		// location
		Matrix.multiplyMM(mModelView, 0, mView, 0, floor.mModelFloor, 0);
		Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0, mModelView,
				0);

		floor.draw(mModelViewProjection, mLightPosInEyeSpace, mModelView, eye);
		GLCommon.checkGLError("floor.draw");

	}

	@Override
	public void onFinishFrame(Viewport viewport) {
	}

	/**
	 * Called when the Cardboard trigger is pulled.
	 */
	@Override
	public void onCardboardTrigger() {
		Log.i(TAG, "onCardboardTrigger");

		if (isLookingAtObject()) {
			mScore++;
			mOverlayView
					.show3DToast("Found it! Look around for another one.\nScore = "
							+ mScore);
			cube.hide();
		} else {
			mOverlayView.show3DToast("Look around to find the object!");
		}

		// Always give user feedback.
		mVibrator.vibrate(50);
	}

	/**
	 * Check if user is looking at object by calculating where the object is in
	 * eye-space.
	 * 
	 * @return true if the user is looking at the object.
	 */
	private boolean isLookingAtObject() {
		// Convert object space to camera space. Use the headView from
		// onNewFrame.
		Matrix.multiplyMM(mModelView, 0, mHeadView, 0, cube.mModelCube, 0);
		Matrix.multiplyMV(objPositionVec, 0, mModelView, 0, initVec, 0);

		float pitch = (float) Math.atan2(objPositionVec[1], -objPositionVec[2]);
		float yaw = (float) Math.atan2(objPositionVec[0], -objPositionVec[2]);

		return Math.abs(pitch) < PITCH_LIMIT && Math.abs(yaw) < YAW_LIMIT;
	}

}
