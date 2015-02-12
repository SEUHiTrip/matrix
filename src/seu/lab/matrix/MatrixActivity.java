package seu.lab.matrix;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
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
	 * Converts a raw text file, saved as a resource, into an OpenGL ES shader.
	 * 
	 * @param type
	 *            The type of shader we will be creating.
	 * @param resId
	 *            The resource ID of the raw text file about to be turned into a
	 *            shader.
	 * @return The shader object handler.
	 */
	private int loadGLShader(int type, int resId) {
		String code = readRawTextFile(resId);
		int shader = GLES20.glCreateShader(type);
		GLES20.glShaderSource(shader, code);
		GLES20.glCompileShader(shader);

		// Get the compilation status.
		final int[] compileStatus = new int[1];
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

		// If the compilation failed, delete the shader.
		if (compileStatus[0] == 0) {
			Log.e(TAG,
					"Error compiling shader: "
							+ GLES20.glGetShaderInfoLog(shader));
			GLES20.glDeleteShader(shader);
			shader = 0;
		}

		if (shader == 0) {
			throw new RuntimeException("Error creating shader.");
		}

		return shader;
	}

	/**
	 * Checks if we've had an error inside of OpenGL ES, and if so what that
	 * error is.
	 * 
	 * @param label
	 *            Label to report in case of error.
	 */
	private static void checkGLError(String label) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(TAG, label + ": glError " + error);
			throw new RuntimeException(label + ": glError " + error);
		}
	}

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
		GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text
														// shows up well.
		int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER,
				R.raw.light_vertex);
		int gridShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER,
				R.raw.grid_fragment);
		int passthroughShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER,
				R.raw.passthrough_fragment);

		cube = new Cube(WorldLayoutData.CUBE_COORDS,
				WorldLayoutData.CUBE_COLORS, WorldLayoutData.CUBE_NORMALS,
				WorldLayoutData.CUBE_FOUND_COLORS, vertexShader,
				passthroughShader);

		checkGLError("Cube program");

		cube.initParams();

		checkGLError("Cube program params");

		floor = new Floor(WorldLayoutData.FLOOR_COORDS,
				WorldLayoutData.FLOOR_COLORS, WorldLayoutData.FLOOR_NORMALS,
				vertexShader, gridShader);

		checkGLError("Floor program");

		floor.initParams();

		checkGLError("Floor program params");

		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		checkGLError("onSurfaceCreated");
	}

	/**
	 * Converts a raw text file into a string.
	 * 
	 * @param resId
	 *            The resource ID of the raw text file about to be turned into a
	 *            shader.
	 * @return The context of the text file, or null in case of error.
	 */
	private String readRawTextFile(int resId) {
		InputStream inputStream = getResources().openRawResource(resId);
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			reader.close();
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Prepares OpenGL ES before we draw a frame.
	 * 
	 * @param headTransform
	 *            The head transformation in the new frame.
	 */
	@Override
	public void onNewFrame(HeadTransform headTransform) {
		// Build the Model part of the ModelView matrix.
		Matrix.rotateM(cube.mModelCube, 0, TIME_DELTA, 0.5f, 0.5f, 1.0f);

		// Build the camera matrix and apply it to the ModelView.
		Matrix.setLookAtM(mCamera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f,
				0.0f, 1.0f, 0.0f);

		headTransform.getHeadView(mHeadView, 0);

		checkGLError("onReadyToDraw");
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

		checkGLError("mColorParam");

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

		cube.setLookingAtObject(isLookingAtObject());
		cube.draw(mModelViewProjection, mLightPosInEyeSpace, mModelView);
		checkGLError("cube.draw");

		// Set mModelView for the floor, so we draw floor in the correct
		// location
		Matrix.multiplyMM(mModelView, 0, mView, 0, floor.mModelFloor, 0);
		Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0, mModelView,
				0);

		floor.draw(mModelViewProjection, mLightPosInEyeSpace, mModelView);
		checkGLError("floor.draw");

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
