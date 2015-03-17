package seu.lab.matrix;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import com.idisplay.VirtualScreenDisplay.ConnectionActivity;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection.ConnectionMode;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection.IDisplayConnectionCallback;
import com.idisplay.util.ArrayImageContainer;
import com.idisplay.util.ImageContainer;
import com.idisplay.util.Logger;
import com.idisplay.util.RLEImage;
import com.idisplay.util.ServerItem;
import com.learnopengles.android.common.GLCommon;
import com.learnopengles.android.common.RawResourceReader;
import com.learnopengles.android.common.ShaderHelper;
import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import javax.microedition.khronos.egl.EGLConfig;

import seu.lab.matrix.obj.Floor;
import seu.lab.matrix.obj.ScreenCube;
import seu.lab.matrix.obj.WorldLayoutData;

public class Screen3DMatrixActivity extends AbstractScreenMatrixActivity
		implements CardboardView.StereoRenderer, IDisplayConnectionCallback{

	private static final String TAG = "MatrixActivity";

	private ServerItem usbServerItem;
	private IDisplayConnection iDisplayConnection;
	private ConnectionMode currentMode;
	
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

	private ScreenCube cube;
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

		usbServerItem = ServerItem.CreateUsbItem(this);
		iDisplayConnection = new IDisplayConnection(this);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.isEmpty()) {
				Logger.d("start with: empty extract");
			} else {
				Logger.d("start with: " + extras.toString());
				currentMode = (ConnectionMode) extras.getParcelable("mode");
				Logger.d("currentMode: " + currentMode.width + "x"
						+ currentMode.height);
			}
		}
		
		setContentView(R.layout.common_ui);
		CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
		cardboardView.setRenderer(this);
		setCardboardView(cardboardView);

		// cardboardView.setDistortionCorrectionEnabled(false);

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
	protected void onStart() {
		super.onStart();
		Logger.d("usbServerItem : " + usbServerItem);

		iDisplayConnection.connectToServer(usbServerItem, currentMode);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		iDisplayConnection.listScreenHandler.sendEmptyMessage(1);
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
		
//		GLES20.glEnable(3042);
//
//		GLES20.glBlendFunc(1, 771);
		
		GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text
														// shows up well.
		int vertexShader = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER,
				RawResourceReader.readTextFileFromRawResource(
						getApplicationContext(), R.raw.light_vertex));
		int texVertexShader = ShaderHelper.compileShader(
				GLES20.GL_VERTEX_SHADER, RawResourceReader
						.readTextFileFromRawResource(getApplicationContext(),
								R.raw.tex_light_vertex));
		int gridShader = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER,
				RawResourceReader.readTextFileFromRawResource(
						getApplicationContext(), R.raw.grid_fragment));
		int passthroughShader = ShaderHelper.compileShader(
				GLES20.GL_FRAGMENT_SHADER, RawResourceReader
						.readTextFileFromRawResource(getApplicationContext(),
								R.raw.passthrough_fragment));

		int screenYUVShader = ShaderHelper.compileShader(
				GLES20.GL_FRAGMENT_SHADER, RawResourceReader
						.readTextFileFromRawResource(getApplicationContext(),
								R.raw.matrix_yuv_fragment));
		
		cube = new ScreenCube(WorldLayoutData.CUBE_COORDS,
				WorldLayoutData.CUBE_COLORS, WorldLayoutData.CUBE_NORMALS,
				WorldLayoutData.CUBE_FOUND_COLORS,
				WorldLayoutData.TEXTURE_COORDINATES, texVertexShader,
				screenYUVShader);

		GLCommon.checkGLError("Cube program");

		cube.initParams(getApplicationContext());

		GLCommon.checkGLError("Cube program params");

		floor = new Floor(WorldLayoutData.FLOOR_COORDS,
				WorldLayoutData.FLOOR_COLORS, WorldLayoutData.FLOOR_NORMALS,
				vertexShader, gridShader);

		GLCommon.checkGLError("Floor program");

		floor.initParams(getApplicationContext());

		GLCommon.checkGLError("Floor program params");

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

	@Override
	public void onInstanceCursorImgChange(ImageContainer imageContainer) {
		cube.onInstanceCursorImgChange(imageContainer);
	}

	@Override
	public void onInstanceCursorPositionChange(int i, int i2) {
		cube.onInstanceCursorPositionChange(i, i2);
	}

	@Override
	public void onInstanceDataAvailable(int i, Object obj) {
		onInstanceDataAvailableHandler(i, obj);
	}

	@Override
	protected void onInstanceDataAvailableHandler(int i, Object obj) {
		switch (i) {
		case 0:
			break;
		case 1:
			break;
		case 2:
			break;
		case 4:
			ArrayImageContainer arrayImageContainer = (ArrayImageContainer) obj;
			cube.setPixels(arrayImageContainer);
			return;
		default:
			Logger.d("Unknown format of picture " + i);
			break;
		}

	}

	@Override
	protected Bitmap setDiffImage(Bitmap bitmap, RLEImage rLEImage) throws Exception {
		throw new Exception("setDiffImage not implemneted");
	}

	@Override
	public void onIDisplayConnected() {
		// TODO Auto-generated method stub
		
	}


//	@Override
//	public void update(Observable observable, Object obj) {
//		updateScreenVerticles(mState.getZoom(), mState.getPanX(),
//				mState.getPanY());
//		setCursorPosition(mCursorX, mCursorY);
//		// requestRender();
//	}
}
