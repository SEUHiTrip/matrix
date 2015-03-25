package seu.lab.matrix;

import java.io.IOException;
import java.nio.ByteBuffer;
import javax.microedition.khronos.egl.EGLConfig;

import org.json.JSONException;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Point;

import seu.lab.dolphin.client.Dolphin;
import seu.lab.dolphin.client.DolphinException;
import seu.lab.dolphin.client.IDolphinStateCallback;
import seu.lab.dolphin.client.IGestureListener;
import seu.lab.matrix.controllers.AppController;
import seu.lab.matrix.controllers.FilesController;
import seu.lab.matrix.controllers.FolderController;
import seu.lab.matrix.controllers.VideoController;
import seu.lab.matrix.controllers.WindowController;
import seu.lab.matrix.red.RemoteManager.OnRemoteChangeListener;
import seu.lab.matrix.red.SimpleCameraBridge;
import seu.lab.matrix.red.SimpleCameraBridge.DefaultCvCameraViewListener2;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.Viewport;
import com.idisplay.VirtualScreenDisplay.FPSCounter;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection;
import com.idisplay.VirtualScreenDisplay.IIdisplayViewRendererContainer;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection.ConnectionMode;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection.IDisplayConnectionCallback;
import com.idisplay.util.ArrayImageContainer;
import com.idisplay.util.ImageContainer;
import com.idisplay.util.Logger;
import com.idisplay.util.RLEImage;
import com.idisplay.util.ServerItem;
import com.jme.scene.SwitchModel;
import com.threed.jpct.GLSLShader;
import com.threed.jpct.Loader;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.util.BitmapHelper;

// merge and manage dolphin, red, and IDisplay

public abstract class Framework3DMatrixActivity extends
		AbstractScreenMatrixActivity implements CardboardView.StereoRenderer,
		IDisplayConnectionCallback {

	public static Activity master = null;
	public static final String TAG = "Framework3DMatrixActivity";
	public final static boolean NEED_SKYBOX = true;
	public final static boolean NEED_IDISPLAY = false;
	public final static boolean NEED_RED = false;
	public final static boolean NEED_DOLPHIN = false;
	public final static boolean NEED_WORKSPACE = true;
	public final static boolean NEED_SCENE = false;
	
	protected ServerItem usbServerItem;
	protected IDisplayConnection iDisplayConnection;
	protected ConnectionMode currentMode;

	protected int mWidth = 1024;
	protected int mHeight = 1024;
	protected int mStrideX = 1024;
	protected int mStrideY = 1024;

	boolean mRedWorking = false;

	protected float[] mAngles = new float[3];

	protected GLSLShader[] screenShaders = null;

	ArrayImageContainer mArrayImageContainer;

	protected Point point = new Point();

	protected SimpleCameraBridge mOpenCvCameraView;

	Dolphin dolphin = null;

	protected CardboardOverlayView mOverlayView;

	protected Handler mHandler;

	protected RequestQueue mQueue;
	
	protected AppController appController;
	
	protected VideoController videoController;
	
	protected FilesController filesController;
	
	protected FolderController folderController;
	
	protected WindowController windowController;
		
	class IDisplayKeeper extends Thread{
		
		boolean needIdisplay = true;

		
		public IDisplayKeeper() {
			// TODO Auto-generated constructor stub
		}
		
		public void stopGracefully() {
			needIdisplay = false;
		}
		
		public void switchMode(ConnectionMode mode){
			currentMode = mode;
			stopIDisplay();
		}
		
		public void run() {
			while (needIdisplay) {
				startIDisplay(currentMode);
				try {
					synchronized (this) {
						this.wait(10*1000);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	
	IDisplayKeeper iDisplayKeeper = new IDisplayKeeper();
	
	public BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mRedWorking = true;
				mOpenCvCameraView.enableView();
			}
				break;
			default: {
				mRedWorking = false;
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	DefaultCvCameraViewListener2 cvCameraViewListener2 = null;

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
			FPSCounter.imageRenderComplete();
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

	IDisplayProgram iDisplayProgram = new IDisplayProgram() {

		int cnt;
		long current;

		protected float mCursorMulX;
		protected float mCursorMulY;

		float mCursorX = -2.0f;
		float mCursorY = -2.0f;

		@Override
		public void setPixels(ArrayImageContainer arrayImageContainer) {
			boolean z = false;
			if (System.currentTimeMillis() - current > 1000) {
				current = System.currentTimeMillis();
				Log.e(TAG, "cnt " + cnt);
				cnt = 0;
			}
			cnt++;
			if (!(mArrayImageContainer != null
					&& mContainer.getDataStrideX() == arrayImageContainer
							.getStrideX()
					&& mContainer.getDataStrideY() == arrayImageContainer
							.getStrideY()
					&& mContainer.getDataWidth() == arrayImageContainer
							.getWidth() && mContainer.getDataHeight() == arrayImageContainer
					.getHeight())) {
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

		@Override
		public void onInstanceCursorPositionChange(int i, int i2) {

		}

		@Override
		public void onInstanceCursorImgChange(ImageContainer imageContainer) {

		}

		public boolean isRenderDataAvaliable() {
			return mArrayImageContainer != null;
		}

	};

	IDolphinStateCallback stateCallback = new IDolphinStateCallback() {

		@Override
		public void onNormal() {
		}

		@Override
		public void onNoisy() {
		}

		@Override
		public void onCoreReady() {
			try {
				dolphin.start();
			} catch (DolphinException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void onCoreFail() {
		}
	};

	protected boolean mIDisplayConnected;
	protected OnRemoteChangeListener remoteListener;
	protected IGestureListener gestureListener;
	protected CardboardView cardboardView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		usbServerItem = ServerItem.CreateUsbItem(this);
		iDisplayConnection = new IDisplayConnection(this);
		mQueue = Volley.newRequestQueue(getApplicationContext());

		appController = new AppController(mQueue);
		videoController = new VideoController(mQueue);
		filesController = new FilesController(mQueue);
		folderController = new FolderController(mQueue);
		windowController = new WindowController(mQueue);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.isEmpty()) {
				Log.d(TAG, "start with: empty extract");
			} else {
				Log.d(TAG, "start with: " + extras.toString());
				currentMode = (ConnectionMode) extras.getParcelable("mode");
				Log.d(TAG, "currentMode: " + currentMode.width + "x"
						+ currentMode.height);
			}
		}

		setContentView(R.layout.common_ui);
		cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
		cardboardView.setRenderer(this);
		setCardboardView(cardboardView);

		mOverlayView = (CardboardOverlayView) findViewById(R.id.overlay);
		mOverlayView.show3DToast("Project Matrix\nInitializing");

		remoteListener = getRemoteListener();
		gestureListener = getDolphinGestureListener();

		mHandler = new Handler(getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 0)
					mOverlayView.show3DToast(msg.obj.toString());
				else if (msg.what == 1) {
					mOverlayView.show3DToastOnlyRight(msg.obj.toString());
				}
				super.handleMessage(msg);
			}
		};

		mOpenCvCameraView = new SimpleCameraBridge(getApplicationContext(), -1,
				remoteListener);
		cvCameraViewListener2 = mOpenCvCameraView.new DefaultCvCameraViewListener2();
		mOpenCvCameraView.setCvCameraViewListener(cvCameraViewListener2);
		mOpenCvCameraView.surfaceCreated(null);

		mIDisplayConnected = false;

		try {
			dolphin = Dolphin.getInstance(
					(AudioManager) getSystemService(Context.AUDIO_SERVICE),
					getContentResolver(), stateCallback, null, gestureListener);
		} catch (DolphinException e) {
			Log.e(TAG, e.toString());
		} catch (JSONException e) {
			Log.e(TAG, e.toString());
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onPause() {
		stopRed();
		iDisplayKeeper.stopGracefully();
		synchronized (iDisplayKeeper) {
			iDisplayKeeper.notifyAll();
		}
		try {
			dolphin.pause();
		} catch (DolphinException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		super.onPause();
	}

	@Override
	protected void onStop() {
		try {
			dolphin.stop();
		} catch (DolphinException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.surfaceDestroyed(null);

		super.onDestroy();
	}

	@Override
	public void onIDisplayConnected() {
		mIDisplayConnected = true;
	}
	
	@Override
	public void onIDisplayDenyed() {
		mIDisplayConnected = false;
	}
	
	@Override
	public void OnIDisplayUnexpectedError() {
		mIDisplayConnected = false;
	}

	protected void startIDisplay(ConnectionMode mode) {
		currentMode = mode;
		if (!mIDisplayConnected)
			IDisplayConnection.connectToServer(usbServerItem, currentMode);
	}

	protected void stopIDisplay() {
		if (mIDisplayConnected)
			IDisplayConnection.listScreenHandler.sendEmptyMessage(1);
		mIDisplayConnected = false;
	}

	protected void startRed() {
		if (!mRedWorking) {
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
					mLoaderCallback);

			mOpenCvCameraView.surfaceChanged(null, 0, 0, 0);
		}
	}

	protected void stopRed() {
		if (mRedWorking) {
			mRedWorking = false;
			if (mOpenCvCameraView != null)
				mOpenCvCameraView.disableView();
		}
	}

	protected void startDolphin() {
		try {
			dolphin.prepare(getApplicationContext());
		} catch (DolphinException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void stopDolphin() {
		try {
			dolphin.pause();
		} catch (DolphinException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	abstract OnRemoteChangeListener getRemoteListener();

	abstract IGestureListener getDolphinGestureListener();

	protected void show3DToast(String m) {
		Message message = new Message();
		message.obj = m;
		message.what = 0;
		mHandler.sendMessage(message);
	}

	protected void show3DToastOnlyRight(String m) {
		Message message = new Message();
		message.obj = m;
		message.what = 1;
		mHandler.sendMessage(message);
	}
	

	// @Override
	// public void onDrawEye(Eye eye) {
	//
	// screens[2].setVisibility(false);
	//
	// if (currentMode.type == IDisplayConnection.ConnectionType.Single) {
	//
	// } else if (currentMode.type == IDisplayConnection.ConnectionType.Duel) {
	// if (eye.getType() == Eye.Type.LEFT) {
	// screens[0].setVisibility(true);
	// screens[1].setVisibility(false);
	// } else {
	// screens[0].setVisibility(false);
	// screens[1].setVisibility(true);
	// }
	// } else {
	//
	// }
	//
	// fillTexturesWithEye(buffer, eye);
	//
	// screenShaders[0].setUniform("videoFrame", 4);
	// screenShaders[0].setUniform("videoFrame2", 5);
	// screenShaders[0].setUniform("videoFrame3", 6);
	//
	// screenShaders[1].setUniform("videoFrame", 7);
	// screenShaders[1].setUniform("videoFrame2", 8);
	// screenShaders[1].setUniform("videoFrame3", 9);
	//
	// screenShaders[2].setUniform("videoFrame", 10);
	// screenShaders[2].setUniform("videoFrame2", 11);
	// screenShaders[2].setUniform("videoFrame3", 12);
	//
	// fb.clear(back);
	//
	// world.renderScene(fb);
	// // sky.render(world, fb);
	//
	// if(false && eye.getType() == Eye.Type.LEFT){
	// world.drawWireframe(fb, wire, 2, false);
	// }else {
	// world.draw(fb);
	// }
	//
	// fb.display();
	// }

	// @Override
	// public void onNewFrame(HeadTransform headTransform) {
	// headTransform.getEulerAngles(mAngles, 0);
	//
	// Camera cam = world.getCamera();
	//
	// SimpleVector center_island_green = new SimpleVector();
	// SimpleVector center_island_volcano = new SimpleVector();
	// SimpleVector center_island_ship = new SimpleVector();
	//
	// islands[0].getTransformedCenter(center_island_green);
	// islands[1].getTransformedCenter(center_island_volcano);
	// islands[2].getTransformedCenter(center_island_ship);
	//
	// if( isLookingAt(cam, center_island_green) >0.99){
	// cam.setPosition(islands[0].getTranslation().x,islands[0].getTranslation().y-2,islands[0].getTranslation().z);
	// cam.rotateY(-3.14f/2);
	// }
	// //Log.e("cam", "island_green: " + dot);
	// // islands[0].setScale(1.2f);
	// // else
	// // islands[0].setScale(0.8f);
	// if( isLookingAt(cam, center_island_volcano) >0.99){
	// cam.setPosition(islands[1].getTranslation().x,islands[1].getTranslation().y-3,islands[1].getTranslation().z);
	// cam.lookAt(new
	// SimpleVector(center_island_volcano.x,center_island_volcano.y,center_island_volcano.z+5));
	// }//Log.e("cam", "island_volcano: " + dot);
	// // islands[1].setScale(1.2f);
	// // else
	// // islands[1].setScale(0.8f);
	// if(isLookingAt(cam, center_island_ship) >0.99){
	// cam.setPosition(islands[2].getTranslation().x,islands[2].getTranslation().y-1,islands[2].getTranslation().z);
	// cam.rotateY(3.14f/2);
	// }//Log.e("cam", "island_ship: " + dot);
	// // islands[2].setScale(1.2f);
	// // else
	// // islands[2].setScale(0.8f);
	//
	// // if(isLookingAt(cam, treasure.getTransformedCenter())>0.98)
	// // cam.setPosition(0,0,0);
	//
	// cam.lookAt(forward);
	// if (canCamRotate) {
	// cam.rotateY(mAngles[1]);
	// cam.rotateZ(0 - mAngles[2]);
	// cam.rotateX(mAngles[0]);
	// }
	//
	// // Log.e("cam", "transform_green: " + islands[0].getTransformedCenter());
	// // Log.e("cam", "transform_volcano: " +
	// islands[1].getTransformedCenter());
	// // Log.e("cam", "transform_ship: " + islands[2].getTransformedCenter());
	// Log.e("cam", "camDir: " + cam.getDirection());
	// }

	// @Override
	// public void onRendererShutdown() {
	// // TODO Auto-generated method stub
	//
	// }

	// @Override
	// public void onSurfaceChanged(int w, int h) {
	// if (fb != null) {
	// fb.dispose();
	// }
	//
	// fb = new FrameBuffer(w, h);
	//
	// if (master == null) {
	//
	// sky = new SkyBox("star_left", "star_forward", "star_left",
	// "star_right", "star_back", "star_bottom", 10000f);
	// sky.setCenter(new SimpleVector());
	//
	// world = new World();
	// world.setAmbientLight(120, 120, 120);
	//
	// sun = new Light(world);
	// sun.setIntensity(250, 250, 250);
	//
	// spot = new Light(world);
	// spot.setIntensity(150, 150, 150);
	//
	// screens = new Object3D[3];
	// islands = new Object3D[4];
	//
	// notice = Primitives.getPlane(1, 2);
	// notice.rotateY(4.71f);
	// notice.translate(-5, 0, 0);
	// notice.setTexture("font");
	// notice.setTransparencyMode(Object3D.TRANSPARENCY_MODE_ADD);
	// notice.build();
	// notice.strip();
	// world.addObject(notice);
	//
	// screens[0] = Primitives.getPlane(1, 10);
	// screens[0].rotateY(4.71f);
	// screens[0].translate(10, 5, 5);
	// screens[0].setShader(screenShaders[0]);
	// screens[0].calcTextureWrapSpherical();
	// screens[0].build();
	// screens[0].strip();
	// world.addObject(screens[0]);
	//
	// screens[1] = Primitives.getPlane(1, 10);
	// screens[1].rotateY(4.71f);
	// screens[1].translate(10, 5, 5);
	// screens[1].setShader(screenShaders[1]);
	// screens[1].calcTextureWrapSpherical();
	// screens[1].build();
	// screens[1].strip();
	// world.addObject(screens[1]);
	//
	// screens[2] = Primitives.getPlane(1, 10);
	// screens[2].rotateY(4.71f);
	// screens[2].translate(10, 5, 5);
	// screens[2].setShader(screenShaders[2]);
	// screens[2].calcTextureWrapSpherical();
	// screens[2].build();
	// screens[2].strip();
	// world.addObject(screens[2]);
	//
	// islands[0] =
	// Object3D.mergeAll(Loader.load3DS(getResources().openRawResource(R.raw.island_green),
	// 0.8f));
	// islands[0].setVisibility(false);
	// islands[0].translate(-10, 10, -5);
	// islands[0].rotateX(-3.14f/3f);
	// islands[0].rotateY(3.14f/2);
	// islands[0].rotateZ(3.14f/4);
	// world.addObjects(islands[0]);
	//
	// islands[1] =
	// Object3D.mergeAll(Loader.load3DS(getResources().openRawResource(R.raw.island_volcano),
	// 1.6f));
	// islands[1].setVisibility(false);
	// islands[1].translate(-15, 10, 0);
	// islands[1].rotateX(-3.14f/3f);
	// islands[1].rotateY(3.14f/2);
	// islands[1].rotateZ(3.14f/4);
	// world.addObjects(islands[1]);
	//
	// islands[2] =
	// Object3D.mergeAll(Loader.load3DS(getResources().openRawResource(R.raw.island_ship),
	// 0.2f));
	// islands[2].setVisibility(false);
	// islands[2].translate(-10, 10, 5);
	// islands[2].rotateX(-3.14f/3f);
	// islands[2].rotateY(3.14f/2);
	// islands[2].rotateZ(3.14f/4);
	//
	// world.addObjects(islands[2]);
	//
	// islands[3] = Object3D.mergeAll(Loader.load3DS(getResources()
	// .openRawResource(R.raw.treasure), 0.5f));
	// islands[3].translate(5, 1, 0);
	// islands[3].rotateY(3.14f / 2);
	// islands[3].rotateZ(3.14f / 2);
	// world.addObjects(islands[3]);
	//
	// if (currentMode.type == IDisplayConnection.ConnectionType.Single) {
	// screens[1].setVisibility(false);
	// } else if (currentMode.type == IDisplayConnection.ConnectionType.Duel) {
	// screens[1].setVisibility(false);
	// screens[2].setVisibility(false);
	// } else {
	// screens[1].translate(0, 0, -12);
	// }
	//
	// Camera cam = world.getCamera();
	//
	// SimpleVector sv = new SimpleVector(0, 0, 0);
	// sv.y -= 50;
	// // sv.z -= 50;
	// sun.setPosition(sv);
	//
	// spot.setPosition(new SimpleVector());
	// MemoryHelper.compact();
	// }
	//
	// if (buffer == null) {
	// // GLES20.glDeleteTextures(buffer.length, buffer, 0);
	//
	// int numOfTextures = 4 + 3 + 3 + 3;
	// buffer = new int[numOfTextures];
	// GLES20.glGenTextures(numOfTextures, buffer, 0);
	// for (int i = 0; i < numOfTextures; i++) {
	// int i2 = buffer[i];
	// Log.d("Texture", "Texture is at " + i2);
	// GLES20.glBindTexture(3553, i2);
	// GLES20.glTexParameteri(3553, 10242, 33071);
	// GLES20.glTexParameteri(3553, 10243, 33071);
	// GLES20.glTexParameteri(3553, 10240, 9729);
	// GLES20.glTexParameteri(3553, 10241, 9729);
	// }
	// }
	// }

	@Override
	public void onSurfaceCreated(EGLConfig config) {
		GLES20.glEnable(GLES20.GL_BLEND);
		// GLES20.glDisable(GLES20.GL_BLEND);
		// GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,
		// GLES20.GL_ONE_MINUS_SRC_ALPHA);

		Resources res = getResources();

		TextureManager tm = TextureManager.getInstance();

		if (!tm.containsTexture("dummy")) {

			Log.e(TAG, "addTexture dummy");

			Texture texture = new Texture(BitmapHelper.rescale(
					BitmapHelper.convert(getResources().getDrawable(
							R.drawable.icon)), 512, 512));
			tm.addTexture("dummy", texture);

			if (NEED_SKYBOX)
				loadSkyboxTexture(tm);

			loadBoardTexture(tm);
			try {
				loadPicTexture(tm);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		screenShaders = new GLSLShader[3];

		screenShaders[0] = new GLSLShader(Loader.loadTextFile(res
				.openRawResource(R.raw.vertexshader_offset)),
				Loader.loadTextFile(res
						.openRawResource(R.raw.fragmentshader_offset)));
		screenShaders[1] = new GLSLShader(Loader.loadTextFile(res
				.openRawResource(R.raw.vertexshader_offset)),
				Loader.loadTextFile(res
						.openRawResource(R.raw.fragmentshader_offset)));
		screenShaders[2] = new GLSLShader(Loader.loadTextFile(res
				.openRawResource(R.raw.vertexshader_offset)),
				Loader.loadTextFile(res
						.openRawResource(R.raw.fragmentshader_offset)));
	}

	protected void loadBoardTexture(TextureManager tm) {
		Texture b_c2ar = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.b_c2ar)), 512, 512));
		tm.addTexture("b_c2ar", b_c2ar);

		Texture b_car = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.b_car)), 512, 512));
		tm.addTexture("b_car", b_car);

		Texture b_excel = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.b_excel)), 256, 256));
		tm.addTexture("b_excel", b_excel);

		Texture b_file = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.b_file)), 512, 512));
		tm.addTexture("b_file", b_file);

		Texture b_ie = new Texture(
				BitmapHelper.rescale(
						BitmapHelper.convert(getResources().getDrawable(
								R.drawable.b_ie)), 512, 512));
		tm.addTexture("b_ie", b_ie);

		Texture b_m2inecraft = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.b_m2inecraft)), 512, 512));
		tm.addTexture("b_m2inecraft", b_m2inecraft);

		Texture b_v2ideo = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.b_v2ideo)), 512, 512));
		tm.addTexture("b_v2ideo", b_v2ideo);

		Texture b_v3ideo = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.b_v3ideo)), 512, 512));
		tm.addTexture("b_v3ideo", b_v3ideo);
		
		Texture b_minecraft = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.b_minecraft)), 512, 512));
		tm.addTexture("b_minecraft", b_minecraft);

		Texture b_p2ic = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.b_p2ic)), 512, 512));
		tm.addTexture("b_p2ic", b_p2ic);

		Texture b_pic = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.b_pic)), 512, 512));
		tm.addTexture("b_pic", b_pic);

		Texture b_ppt = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.b_ppt)), 256, 256));
		tm.addTexture("b_ppt", b_ppt);

		Texture b_skype = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.b_skype)), 512, 512));
		tm.addTexture("b_skype", b_skype);

		Texture b_video = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.b_video)), 512, 512));
		tm.addTexture("b_video", b_video);

		Texture b_word = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.b_word)), 256, 256));
		tm.addTexture("b_word", b_word);

		Texture b_null = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.b_null)), 256, 256));
		tm.addTexture("b_null", b_null);

		Texture i_close = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.i_close)), 64, 64));
		tm.addTexture("i_close", i_close);

		Texture i_fullscreen = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.i_fullscreen)), 64, 64));
		tm.addTexture("i_fullscreen", i_fullscreen);

		Texture l_back = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.i_back)), 64, 64));

		tm.addTexture("w_back", l_back);

		Texture l_next = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.i_next)), 64, 64));
		tm.addTexture("w_next", l_next);

		Texture l_opt = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.i_fullscreen)), 64, 64));
		tm.addTexture("w_opt", l_opt);
		
		Texture f_i_open = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.f_i_open)), 64, 64));
		tm.addTexture("f_i_open", f_i_open);
		
		Texture f_i_delete = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.f_i_delete)), 64, 64));
		tm.addTexture("f_i_delete", f_i_delete);
		
		Texture f_i_cut = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.f_i_cut)), 64, 64));
		tm.addTexture("f_i_cut", f_i_cut);
		
		Texture f_i_copy = new Texture(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.f_i_copy)), 64, 64));
		tm.addTexture("f_i_copy", f_i_copy);
	}

	protected void loadSkyboxTexture(TextureManager tm) {
		Texture star_back = new Texture(SceneHelper.RotateBitmap(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.star_back)), 256, 256), -90));
		tm.addTexture("star_back", star_back);

		Texture star_bottom = new Texture(SceneHelper.RotateBitmap(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.star_bottom)), 256, 256),90));
		tm.addTexture("star_bottom", star_bottom);

		Texture star_forward = new Texture(SceneHelper.RotateBitmap(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.star_forward)), 256, 256),90));
		tm.addTexture("star_forward", star_forward);

		Texture star_left = new Texture(SceneHelper.RotateBitmap(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.star_left)), 256, 256),90));
		tm.addTexture("star_left", star_left);

		Texture star_right = new Texture(SceneHelper.RotateBitmap(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.star_right)), 256, 256),90));
		tm.addTexture("star_right", star_right);

		Texture star_top = new Texture(SceneHelper.RotateBitmap(BitmapHelper.rescale(
				BitmapHelper.convert(getResources().getDrawable(
						R.drawable.star_top)), 256, 256),90));
		tm.addTexture("star_top", star_top);
	}

	protected void loadPicTexture(TextureManager tm) throws IOException {

		AssetManager am = getAssets();
		BitmapDrawable drawable;
		Texture texture;

		char[] c = new char[] { '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c' };

		for (int i = 0; i < c.length; i++) {
			drawable = new BitmapDrawable(am.open("thumbnails/l_l" + c[i]
					+ ".jpg"));
			texture = new Texture(BitmapHelper.rescale(
					BitmapHelper.convert(drawable), 256, 256));
			texture.removeAlpha();
			tm.addTexture("l_l" + c[i], texture);

			drawable = new BitmapDrawable(am.open("pic/p_" + c[i] + ".jpg"));
			texture = new Texture(BitmapHelper.rescale(
					BitmapHelper.convert(drawable), 1024, 1024));
			texture.removeAlpha();
			tm.addTexture("p_" + c[i], texture);
		}

		for (int i = 0; i < 8; i++) {
			drawable = new BitmapDrawable(am.open("video/l_m" + c[i] + ".jpg"));
			texture = new Texture(SceneHelper.RotateBitmap(BitmapHelper
					.rescale(BitmapHelper.convert(drawable), 512, 512), 90f));
			texture.removeAlpha();
			tm.addTexture("l_m" + c[i], texture);
		}
	}

	@Override
	public void onInstanceCursorImgChange(ImageContainer imageContainer) {

	}

	@Override
	public void onInstanceCursorPositionChange(int i, int i2) {

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
			iDisplayProgram.setPixels(arrayImageContainer);
			return;
		default:
			Log.d(TAG, "Unknown format of picture " + i);
			break;
		}
	}

	@Override
	protected Bitmap setDiffImage(Bitmap bitmap, RLEImage rLEImage)
			throws Exception {
		throw new Exception("setDiffImage not implemneted");
	}

	public void fillTextures(int[] iArr) {
		if (mArrayImageContainer != null) {
			int x = mArrayImageContainer.getStrideX();
			int y = mArrayImageContainer.getStrideY();
			simpleFillTextures(iArr, 0, 0, x * y, x, y);
		}
	}

	public void fillTexturesWithEye(int[] iArr, Eye eye, boolean[] shown) {
		if (mArrayImageContainer != null) {
			int x = mArrayImageContainer.getStrideX();
			int y = mArrayImageContainer.getStrideY();
			int len = x * y;

			Logger.d("len:" + len);

			if (currentMode.type == IDisplayConnection.ConnectionType.Single) {
				simpleFillTextures(iArr, 0, 0, len, x, y);
			} else {
				if (shown[0])
					simpleFillTextures(iArr, 0, 0, len / 4, x, y / 4);
				if (shown[1])
					simpleFillTextures(iArr, 3, len / 4, len / 4, x, y / 4);
				if (shown[2])
					simpleFillTextures(iArr, 6, len / 2, len / 4, x, y / 4);
				// if (eye.getType() == Eye.Type.LEFT) {
				// simpleFillTextures(iArr, 0, 0, len >> 1, x, y >> 1);
				// } else if (eye.getType() == Eye.Type.RIGHT) {
				// simpleFillTextures(iArr, 3, len >> 1, len >> 1, x, y >> 1);
				// }
			}
		}
	}

	public void simpleFillTextures(int[] iArr, int offset, int start,
			int count, int width, int height) {

		GLES20.glActiveTexture(GLES20.GL_TEXTURE4 + offset);
		GLES20.glBindTexture(3553, iArr[4 + offset]);
		GLES20.glTexImage2D(3553, 0, 6409, width, height, 0, 6409, 5121,
				ByteBuffer.wrap(mArrayImageContainer.getDataY(), start, count));

		width = width >> 1;
		height = height >> 1;
		GLES20.glActiveTexture(GLES20.GL_TEXTURE5 + offset);
		GLES20.glBindTexture(3553, iArr[5 + offset]);
		GLES20.glTexImage2D(3553, 0, 6409, width, height, 0, 6409, 5121,
				ByteBuffer.wrap(mArrayImageContainer.getDataU(), start >> 2,
						count >> 2));

		GLES20.glActiveTexture(GLES20.GL_TEXTURE6 + offset);
		GLES20.glBindTexture(3553, iArr[6 + offset]);
		GLES20.glTexImage2D(3553, 0, 6409, width, height, 0, 6409, 5121,
				ByteBuffer.wrap(mArrayImageContainer.getDataV(), start >> 2,
						count >> 2));
	}

	// Matrix rotationMatrix1 = new Matrix();
	// Matrix translatMatrix1 = new Matrix();
	//
	// Matrix rotationMatrix2 = new Matrix();
	// Matrix translatMatrix2 = new Matrix();
	//
	// Matrix rotationMatrix3 = new Matrix();
	// Matrix translatMatrix3 = new Matrix();
	//
	// @Override
	// public boolean onTouchEvent(MotionEvent event) {
	// if (!(event.getAction() == MotionEvent.ACTION_UP))
	// return false;
	//
	// canCamRotate = canCamRotate ? false : true;
	//
	// if (!canCamRotate) {
	// rotationMatrix1 = new Matrix(screens[0].getRotationMatrix());
	// translatMatrix1 = new Matrix(screens[0].getTranslationMatrix());
	//
	// screens[0].clearRotation();
	// screens[0].clearTranslation();
	//
	// screens[0].rotateY(4.71f);
	// screens[0].translate(new SimpleVector(-8, 0, 0));
	//
	// rotationMatrix2 = new Matrix(screens[1].getRotationMatrix());
	// translatMatrix2 = new Matrix(screens[1].getTranslationMatrix());
	//
	// screens[1].clearRotation();
	// screens[1].clearTranslation();
	//
	// screens[1].rotateY(4.71f);
	// screens[1].translate(new SimpleVector(-8, 0, 0));
	// } else {
	// screens[0].setTranslationMatrix(translatMatrix1);
	// screens[0].setRotationMatrix(rotationMatrix1);
	//
	// screens[1].setTranslationMatrix(translatMatrix2);
	// screens[1].setRotationMatrix(rotationMatrix2);
	// }
	//
	// return false;
	// }

	@Override
	public void onFinishFrame(Viewport arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRendererShutdown() {
		// TODO Auto-generated method stub

	}
}
