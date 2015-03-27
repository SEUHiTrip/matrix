package seu.lab.matrix;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Point;
import seu.lab.dolphin.client.ContinuousGestureEvent;
import seu.lab.dolphin.client.GestureEvent;
import seu.lab.dolphin.client.GestureEvent.Gestures;
import seu.lab.dolphin.client.IGestureListener;
import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.animation.CamAnimation;
import seu.lab.matrix.animation.CamTranslationAnimation;
import seu.lab.matrix.animation.DisplayAnimation;
import seu.lab.matrix.animation.LiveTileAnimation;
import seu.lab.matrix.animation.PeopleAnimation;
import seu.lab.matrix.animation.PickGroup;
import seu.lab.matrix.animation.ScaleAnimation;
import seu.lab.matrix.animation.SeqAnimation;
import seu.lab.matrix.animation.TranslationAnimation;
import seu.lab.matrix.animation.WeatherAnimation;
import seu.lab.matrix.app.AbstractApp;
import seu.lab.matrix.app.AppType;
import seu.lab.matrix.app.CamApp;
import seu.lab.matrix.app.CarApp;
import seu.lab.matrix.app.DroneApp;
import seu.lab.matrix.app.ExcelApp;
import seu.lab.matrix.app.FileApp;
import seu.lab.matrix.app.FileOpenApp;
import seu.lab.matrix.app.IEApp;
import seu.lab.matrix.app.LauncherApp;
import seu.lab.matrix.app.MinecraftApp;
import seu.lab.matrix.app.NullApp;
import seu.lab.matrix.app.PPTApp;
import seu.lab.matrix.app.PicApp;
import seu.lab.matrix.app.SceneCallback;
import seu.lab.matrix.app.SkypeApp;
import seu.lab.matrix.app.VideoApp;
import seu.lab.matrix.app.WordApp;
import seu.lab.matrix.controllers.AppController.app_name;
import seu.lab.matrix.red.RemoteManager.OnRemoteChangeListener;
import android.R.integer;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection.ConnectionMode;
import com.threed.jpct.Camera;
import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;
import com.threed.jpct.util.SkyBox;

// control the 3D scene

public class SceneActivity extends Framework3DMatrixActivity implements
		SceneCallback {

	private static final String TAG = "SceneActivity";

	final static int WORKSPACE_COUNT = 3;

	final static int SINGLE_TAP = 0;
	final static int DOUBLE_TAP = 1;
	final static int TOGGLE_FULLSCREEN = 2;
	final static int LONG_PRESS = 3;
	final static int LEFT = 4;
	final static int RIGHT = 5;
	final static int UP = 6;
	final static int DOWN = 7;
	final static int BACK = 8;

	final static int GREEN = 0;
	final static int VOLCANO = 1;
	final static int SHIP = 2;
	final static int TREASURE = 3;
	final static int WORKSPACE = 4;

	final static int HORSE = 0;

	AbstractApp[] apps = new AbstractApp[11 + 1 + 1 + 1 + 1];

	class AppLaunchThead extends Thread {
		private AppType appType;
		private Bundle bundle;

		AppLaunchThead(AppType appType, Bundle bundle) {
			this.bundle = bundle;
			this.appType = appType;
		}

		@Override
		public void run() {

			switch (appType) {
			case CAM:
			case DRONE:
			case MINECRAFT:
			case CAR:
			case SKYPE:
				break;

			default:
				try {
					sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				break;
			}
			try {
				sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Log.e(TAG, "thread run");

			ws.mState = 2;
			ws.mCurrentAppType = appType;
			ws.mCurrentApp = apps[appType.ordinal()];

			ws.mCurrentApp.onOpen(bundle);

			Log.e(TAG, "ws open : " + appType.toString());
		}

	}

	private boolean NEO = false;
	private boolean isCamFlying = false;

	protected SimpleVector forward = new SimpleVector(-1, 0, 0);
	protected SimpleVector backward = new SimpleVector(1, 0, 0);
	protected SimpleVector up = new SimpleVector(0, -1, 0);

	private SimpleVector launcherDir = new SimpleVector(-1, -2.5, 0);
	private SimpleVector scrDir = new SimpleVector(-1, 0, 0);
	private SimpleVector listDir = new SimpleVector(-1, 3, 0);

	protected Object3D[] mCamViewspots = null;
	protected int mCamViewIndex = NEED_SCENE ? TREASURE : WORKSPACE;

	protected Object3D[] mWorkspaceObjects = null;
	protected Object3D[] mSceneObjects = null;

	private static float BALL_DISTANCE = 2f;

	private int mFrameCounter = 0;

	private Camera cam;
	protected FrameBuffer fb = null;
	protected SkyBox sky;
	protected World world = null;
	protected Light sun = null;
	protected Light sun2 = null;
	protected Light spot = null;
	protected Light treasureLight = null;
	protected Object3D[] screens = null;
	protected Object3D curtain = null;
	protected Object3D[] weathers = null;
	protected Object3D[] entrances = null;
	protected Object3D[] islands = null;

	private boolean[] scrShown = new boolean[3];

	protected PeopleAnimation peopleAnimation;

	protected RGBColor black = new RGBColor(0, 0, 0);
	protected RGBColor back = new RGBColor(50, 50, 100);
	protected RGBColor wire = new RGBColor(100, 100, 100);

	protected int[] buffer;
	protected boolean canCamRotate = true;

	private Object3D ball1 = null;

	List<Animatable> mAnimatables = new LinkedList<Animatable>();

	private boolean test = true;

	private Object3D mPeoplePositionDummy;

	private Workspace[] workspaces;
	private Workspace ws;

	private int mWsIdx = 0;

	private long lastBallUpdate = 0;

	private boolean isAnimationOn = true;

	private Map<String, Object3D> clickableIcons = new HashMap<String, Object3D>();

	PickGroup[] mPickGroupIcons = new PickGroup[2];

	GestureDetector mGestureDetector = null;

	boolean[] actionFired = new boolean[8 + 1];

	float headAngle = 0f;

	int headDir = 0;
	
	private boolean singleEye = false;

	SimpleOnGestureListener mGestureListener = new SimpleOnGestureListener() {

		public boolean onDoubleTap(MotionEvent e) {
			actionFired[DOUBLE_TAP] = true;
			return super.onDoubleTap(e);
		}

		public boolean onSingleTapConfirmed(MotionEvent e) {
			actionFired[SINGLE_TAP] = true;
			return super.onSingleTapConfirmed(e);
		}

		public void onLongPress(MotionEvent e) {
			if (e.getRawX() < 480) {
				// actionFired[LEFT] = true;
			} else if (e.getRawX() > 1920 - 480) {
				// actionFired[RIGHT] = true;
			} else {
				if (e.getRawY() < 540) {
					// actionFired[UP] = true;
					actionFired[TOGGLE_FULLSCREEN] = true;

				} else {
					// actionFired[DOWN] = true;
					actionFired[BACK] = true;
				}
			}

		}

		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			lastBallUpdate = System.currentTimeMillis();

			double x = point.x + -distanceX * 0.003;
			double y = point.y + -distanceY * 0.003;

			x = x > 1 ? 1 : x;
			x = x < -1 ? -1 : x;
			y = y > 1 ? 1 : y;
			y = y < -1 ? -1 : y;

			point.x = x;
			point.y = y;

			return false;
		}

		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {

			Log.e(TAG, "vx: " + velocityX + " vy:" + velocityY);

			if (velocityX < -2000) {
				actionFired[LEFT] = true;
			} else if (velocityX > 2000) {
				actionFired[RIGHT] = true;
			} else if (velocityY < -2000) {
				actionFired[DOWN] = true;
			} else if (velocityY > 2000) {
				actionFired[UP] = true;
			}

			return true;
		}

	};

	OnRemoteChangeListener remoteChangeListener = new OnRemoteChangeListener() {

		Point dragStartPoint;

		@Override
		public void onMove(Point p) {
			// Log.e(TAG, "remote : onMove x:" + p.x + " y: " + p.y);
			point = p;
			lastBallUpdate = System.currentTimeMillis();
		}

		@Override
		public void onClick() {
			Log.e(TAG, "remote : onClick");
			actionFired[DOUBLE_TAP] = true;
			// if (test) {
			// test = false;
			// ball1.setAdditionalColor(new RGBColor(0, 100, 0));
			// } else {
			// test = true;
			// ball1.setAdditionalColor(new RGBColor(0, 0, 100));
			// }
		}

		@Override
		public void onPress(Point p) {
			Log.e(TAG, "remote : onPress");
			dragStartPoint = p;
			ball1.setAdditionalColor(new RGBColor(0, 100, 0));

		}

		@Override
		public void onRaise(Point p) {
			Log.e(TAG, "remote : onRaise");

			// remote drag
			double x = p.x - dragStartPoint.x;
			double y = p.y - dragStartPoint.y;

			Log.e(TAG, "remote : drag  " + x + "  " + y);

			if (x < -0.5) {
				actionFired[LEFT] = true;
			} else if (x > 0.5) {
				actionFired[RIGHT] = true;
			} else if (y < -0.5) {
				actionFired[DOWN] = true;
			} else if (y > 0.5) {
				actionFired[UP] = true;
			}

			ball1.setAdditionalColor(new RGBColor(0, 0, 100));

		}
	};

	IGestureListener gestureListener = new IGestureListener() {

		@Override
		public void onGesture(final GestureEvent event) {
			if (!event.isConclusion)
				return;

			if (event.type == Gestures.PUSH_PULL_PUSH.ordinal()
					|| event.type == Gestures.PUSH_PULL_PUSH_PULL.ordinal()) {
				actionFired[DOUBLE_TAP] = true;
			} else if (event.type == Gestures.SWIPE_LEFT_P.ordinal()
					|| event.type == Gestures.SWIPE_LEFT_L.ordinal()) {
				actionFired[LEFT] = true;
			} else if (event.type == Gestures.SWIPE_RIGHT_P.ordinal()
					|| event.type == Gestures.SWIPE_RIGHT_L.ordinal()) {
				actionFired[RIGHT] = true;
			} else if (event.type == Gestures.PUSH.ordinal() && event.speed > 8) {
				actionFired[DOWN] = true;
			} else if (event.type == Gestures.PULL.ordinal() && event.speed > 8) {
				actionFired[UP] = true;
			} else if (event.type == Gestures.PULL_PUSH_PULL.ordinal()) {
				actionFired[BACK] = true;
			}

			show3DToastOnlyRight(event.result);
		}

		@Override
		public void onContinuousGestureUpdate(ContinuousGestureEvent event) {
		}

		@Override
		public void onContinuousGestureStart(ContinuousGestureEvent event) {
		}

		@Override
		public void onContinuousGestureEnd() {
		}

		@Override
		public JSONObject getGestureConfig() {
			// claim the gesture you need to be true

			JSONObject config = new JSONObject();
			JSONObject masks = new JSONObject();

			try {
				masks.put("" + GestureEvent.Gestures.SWIPE_LEFT_L.ordinal(),
						true);
				masks.put("" + GestureEvent.Gestures.SWIPE_RIGHT_L.ordinal(),
						true);
				masks.put("" + GestureEvent.Gestures.PULL_PUSH_PULL.ordinal(),
						true);
				masks.put("" + GestureEvent.Gestures.PUSH_PULL_PUSH.ordinal(),
						true);
				masks.put(
						""
								+ GestureEvent.Gestures.PUSH_PULL_PUSH_PULL
										.ordinal(), true);
				masks.put(
						""
								+ GestureEvent.Gestures.PULL_PUSH_PULL_PUSH
										.ordinal(), true);
				config.put("masks", masks);
			} catch (JSONException e) {
				Log.e(TAG, e.toString());
			}

			return config;
		}
	};

	@Override
	public void onSurfaceChanged(int w, int h) {
		Config.maxAnimationSubSequences = 100;

		if (fb != null) {
			fb.dispose();
		}
		fb = new FrameBuffer(w, h);
		world = new World();

		try {
			if (NEED_WORKSPACE) {
				show3DToast("loading\nworkspace");

				mWorkspaceObjects = Loader.loadOBJ(
						getAssets().open("workspace.obj"),
						getAssets().open("workspace.mtl"), 1);
			}

			if (NEED_SCENE) {
				show3DToast("loading\nmatrix scene");
				mSceneObjects = Loader.loadOBJ(getAssets().open("matnew.obj"),
						getAssets().open("matnew.mtl"), 1);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		show3DToast("creating\nyour world");

		cam = world.getCamera();

		cam.setPosition(0, 0, 0);

		world.setAmbientLight(130, 130, 130);

		sun = new Light(world);
		sun.setIntensity(250, 250, 250);

		sun2 = new Light(world);
		sun2.setIntensity(250, 250, 250);
		sun2.setPosition(new SimpleVector(0, -50, 10));

		spot = new Light(world);
		spot.setIntensity(50, 50, 50);
		spot.setPosition(new SimpleVector(0, 0, 5));

		treasureLight = new Light(world);
		treasureLight.setIntensity(0, 0, 0);
		treasureLight.setPosition(new SimpleVector(0, 0, 5));

		ball1 = Primitives.getSphere(0.05f);
		ball1.translate(0, 0, -2);
		ball1.calcTextureWrapSpherical();
		ball1.setAdditionalColor(new RGBColor(100, 0, 0));
		ball1.strip();
		ball1.build();
		world.addObject(ball1);

		if (NEED_SCENE) {
			islands = new Object3D[4];
			weathers = new Object3D[4];
			entrances = new Object3D[2];
			world.addObjects(mSceneObjects);
		}

		if (NEED_WORKSPACE) {
			screens = new Object3D[3];
			workspaces = new Workspace[4];

			peopleAnimation = new PeopleAnimation();

			AbstractApp.appController = appController;
			AbstractApp.videoController = videoController;
			AbstractApp.filesController = filesController;
			AbstractApp.folderController = folderController;
			AbstractApp.windowController = windowController;

			apps[AppType.NULL.ordinal()] = new NullApp(mAnimatables, this, cam,
					ball1);
			apps[AppType.MINECRAFT.ordinal()] = new MinecraftApp(cardboardView,
					mAnimatables, this, cam, ball1);
			apps[AppType.CAR.ordinal()] = new CarApp(cardboardView,
					mAnimatables, this, cam, ball1);
			apps[AppType.VIDEO.ordinal()] = new VideoApp(mAnimatables, this,
					cam, ball1);
			apps[AppType.PIC.ordinal()] = new PicApp(mAnimatables, this, cam,
					ball1);
			apps[AppType.SKYPE.ordinal()] = new SkypeApp(mAnimatables, this,
					cam, ball1);
			apps[AppType.IE.ordinal()] = new IEApp(mAnimatables, this, cam,
					ball1);
			apps[AppType.FILE.ordinal()] = new FileApp(mAnimatables, this, cam,
					ball1, world);
			apps[AppType.WORD.ordinal()] = new WordApp(mAnimatables, this, cam,
					ball1);
			apps[AppType.EXCEL.ordinal()] = new ExcelApp(mAnimatables, this,
					cam, ball1);
			apps[AppType.PPT.ordinal()] = new PPTApp(mAnimatables, this, cam,
					ball1);
			apps[AppType.CAM.ordinal()] = new CamApp(cardboardView,
					mAnimatables, this, cam, ball1);
			apps[AppType.FILE_OPEN.ordinal()] = new FileOpenApp(mAnimatables,
					this, cam, ball1);
			apps[AppType.LAUNCHER.ordinal()] = new LauncherApp(mAnimatables,
					this, cam, ball1);
			apps[AppType.DRONE.ordinal()] = new DroneApp(cardboardView,
					mAnimatables, this, cam, ball1);

			for (int i = 0; i < workspaces.length; i++) {
				workspaces[i] = new Workspace(i);
				workspaces[i].mCurrentApp = apps[AppType.NULL.ordinal()];
			}
			mWsIdx = TREASURE;
			ws = workspaces[mWsIdx];

			world.addObjects(mWorkspaceObjects);
		}

		if (NEED_SKYBOX) {
			// sky = new SkyBox("star_left", "star_forward", "star_right",
			// "star_back", "star_top", "star_bottom", 10000f);
			sky = new SkyBox("star_forward", "star_top", "star_back",
					"star_bottom", "star_left", "star_right", 10000f);
			sky.setCenter(new SimpleVector());
		}

		MemoryHelper.compact();

		if (buffer == null) {
			// GLES20.glDeleteTextures(buffer.length, buffer, 0);

			int numOfTextures = 3 + 4 + 3 + 3;
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

	}

	@Override
	public void onNewFrame(HeadTransform headTransform) {
		headTransform.getEulerAngles(mAngles, 0);

		if (mFrameCounter == 0) {
			mFrameCounter++;
		} else if (mFrameCounter == 1) {
			mFrameCounter++;
			show3DToast("sceneInit");
			sceneInit();

		} else {
			sceneLoop();
		}

		if (canCamRotate) {
			cam.setOrientation(forward, up);
			cam.rotateZ(3.1415926f / 2);
			cam.rotateY(mAngles[1]);
			cam.rotateZ(-mAngles[2]);
			cam.rotateX(mAngles[0]);
		} else if (mCamViewIndex == TREASURE && !isCamFlying) {
			cam.setOrientation(forward, up);
			cam.rotateZ(3.1415926f / 2);

			if (mAngles[1] > 0) {
				headAngle = (float) (Math.log(mAngles[1] + 1) / Math.log(2.2));
			} else {
				headAngle = -(float) (Math.log(-mAngles[1] + 1) / Math.log(2.2));
			}

			int newDir = 0;
			if (Math.abs(headAngle) < 0.75) {
				newDir = 1;
			} else if (headAngle < 0) {
				newDir = 2;
			} else {
				newDir = 0;
			}

			if (newDir != headDir) {
				headDir = newDir;
				Log.e(TAG, "headDir change to " + headDir);
				try {
					windowController.setMouse(headDir);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			cam.rotateY(headAngle);

		}

		updateGestureBall();
	}

	private void sceneInit() {
		String name = null;
		mCamViewspots = new Object3D[5];

		cam.setPosition(0, 0, 0);
		if (NEED_SCENE) {
			for (int i = 0; i < mSceneObjects.length; i++) {
				name = mSceneObjects[i].getName();
				saveSceneObject(name, mSceneObjects[i]);
			}
			cam.setPosition(mCamViewspots[TREASURE].getTransformedCenter());
			mAnimatables.add(new WeatherAnimation(weathers[3], weathers[1],
					mAnimatables));

			treasureLight.setIntensity(250, 250, 250);
			treasureLight.enable();
			treasureLight.setPosition(mCamViewspots[TREASURE]
					.getTransformedCenter());
		}

		if (NEED_WORKSPACE) {
			for (int i = 0; i < mWorkspaceObjects.length; i++) {
				name = mWorkspaceObjects[i].getName();
				saveWorkspaceObject(name, mWorkspaceObjects[i]);
			}
			for (int i = 0; i < mWorkspaceObjects.length; i++) {
				name = mWorkspaceObjects[i].getName();
				getScreen(name, mWorkspaceObjects[i]);
			}

			if (NEED_SCENE) {
				mCamViewspots[WORKSPACE].translate(0, 0, -1000);
			}

			for (int i = 0; i < apps.length; i++) {
				apps[i].postInitObj();
			}

			postInitScr();

			peopleAnimation.init(getAssets(), world, mCamViewspots[WORKSPACE],
					mPeoplePositionDummy);

			switchWorkspace(0);

			peopleAnimation.show(mAnimatables);

		}

		if (NEED_SCENE) {
			switchIsland(TREASURE);
		}

		if (NEED_RED)
			startRed();
		if (NEED_DOLPHIN)
			startDolphin();
		synchronized (iDisplayKeeper) {
			if (NEED_IDISPLAY)
				iDisplayKeeper.start();
		}

	}

	private void sceneLoop() {

		if (!isCamFlying) {
			pickAction();

			// deal with input actions
			fireAction();

		}

		// animation driver
		if (isAnimationOn) {
			for (int i = 0; i < mAnimatables.size();) {
				Animatable a = mAnimatables.get(i);
				if (a.isOver()) {
					a.onAnimateSuccess();
					mAnimatables.remove(i);

				} else {
					a.animate();
					i++;
				}
			}
		}

		for (int i = 0; i < scrShown.length; i++) {
			scrShown[i] = screens[i].getVisibility();
		}

		fillTexturesWithEye(buffer, null, scrShown);

		if (scrShown[0]) {
			screenShaders[0].setUniform("videoFrame", 4);
			screenShaders[0].setUniform("videoFrame2", 5);
			screenShaders[0].setUniform("videoFrame3", 6);
		}
		if (scrShown[1]) {
			screenShaders[1].setUniform("videoFrame", 7);
			screenShaders[1].setUniform("videoFrame2", 8);
			screenShaders[1].setUniform("videoFrame3", 9);
		}
		if (scrShown[2]) {
			screenShaders[2].setUniform("videoFrame", 10);
			screenShaders[2].setUniform("videoFrame2", 11);
			screenShaders[2].setUniform("videoFrame3", 12);
		}
	}

	private void updateGestureBall() {

		SimpleVector camDir = new SimpleVector();
		cam.getDirection(camDir);

		camDir.x = camDir.x * BALL_DISTANCE;
		camDir.y = camDir.y * BALL_DISTANCE;
		camDir.z = camDir.z * BALL_DISTANCE;

		SimpleVector originInballView = new SimpleVector(camDir);
		originInballView.x = -originInballView.x;
		originInballView.y = -originInballView.y;
		originInballView.z = -originInballView.z;

		ball1.clearTranslation();
		ball1.clearRotation();

		ball1.translate(cam.getPosition());
		ball1.translate(camDir);

		ball1.setRotationPivot(originInballView);

		if (System.currentTimeMillis() - lastBallUpdate > 5000) {
			ball1.setVisibility(false);
			ball1.rotateAxis(cam.getUpVector(), 0f);
			ball1.rotateAxis(cam.getSideVector(), 0f);
		} else {
			ball1.setVisibility(true);
			ball1.rotateAxis(cam.getUpVector(), (float) (0.5f * point.x));
			ball1.rotateAxis(cam.getSideVector(), (float) (0.5f * point.y));
		}

	}

	private void pickAction() {
		// pick obj
		if (NEED_WORKSPACE && mWsIdx != TREASURE) {

			if (SceneHelper.isLookingDir(cam, ball1, launcherDir) > 0.75) {

				apps[AppType.LAUNCHER.ordinal()].onPick();

			} else if (SceneHelper.isLookingDir(cam, ball1, scrDir) > 0.8) {

				pickScr();
				if (ws.mCurrentAppType == AppType.FILE)
					ws.mCurrentApp.onPick();

			} else if ((ws.mCurrentAppType == AppType.PIC || ws.mCurrentAppType == AppType.VIDEO)
					&& SceneHelper.isLookingDir(cam, ball1, listDir) > 0.8) {

				ws.mCurrentApp.onPick();
			}
		}
	}

	private void fireAction() {

		// consume first

		for (int i = 0; i < actionFired.length; i++) {
			if (!actionFired[i])
				continue;

			switch (i) {
			case LEFT:
				ws.mCurrentApp.onLeft();
				break;
			case RIGHT:
				ws.mCurrentApp.onRight();
				break;
			case UP:
				ws.mCurrentApp.onUp();
				break;
			case DOWN:
				ws.mCurrentApp.onDown();
				break;
			case DOUBLE_TAP:

				if (NEED_SCENE && ws.mCurrentAppType != AppType.PIC
						&& ws.mCurrentAppType != AppType.VIDEO) {
					for (int i1 = 0; i1 < 4; i1++) {
						if (i1 == mCamViewIndex)
							continue;
						if (SceneHelper.isLookingAt(cam,
								islands[i1].getTransformedCenter()) > 0.99) {
							switchIsland(i1);
							actionFired[DOUBLE_TAP] = false;
							break;
						}
					}
				}

				if (actionFired[DOUBLE_TAP]) {
					if (mCamViewIndex == TREASURE) {
						if (SceneHelper.isLookingAt(cam,
								entrances[HORSE].getTransformedCenter()) > 0.95) {
							flyIsland();
							actionFired[DOUBLE_TAP] = false;
						} else if (SceneHelper.isLookingAt(cam,
								islands[TREASURE].getTransformedCenter()) > 0.95) {
							NEO = NEO ? false : true;
							show3DToastOnlyRight("NEO");
							actionFired[DOUBLE_TAP] = false;
						}
					} else {
						if (SceneHelper.isLookingAt(cam, ball1,
								ws.animal.getTransformedCenter()) > 0.995) {
							flyWorkspace();
							actionFired[DOUBLE_TAP] = false;
						}
					}
				}

				if (actionFired[DOUBLE_TAP]) {
					Log.e(TAG, "ws.mCurrentApp.onDoubleTap");

					if (SceneHelper.isLookingDir(cam, ball1, launcherDir) > 0.75) {

						apps[AppType.LAUNCHER.ordinal()].onDoubleTap();

					} else if (SceneHelper.isLookingDir(cam, ball1, scrDir) > 0.8) {

						pickScr();
						ws.mCurrentApp.onDoubleTap();

					} else if ((ws.mCurrentAppType == AppType.PIC || ws.mCurrentAppType == AppType.VIDEO)
							&& SceneHelper.isLookingDir(cam, ball1, listDir) > 0.75) {

						ws.mCurrentApp.onDoubleTap();
					}

				}

				break;

			case SINGLE_TAP:
				ws.mCurrentApp.onSingleTap();
				break;
			case LONG_PRESS:
				ws.mCurrentApp.onLongPress();
				break;
			case TOGGLE_FULLSCREEN:
				if (!ws.mCurrentApp.onToggleFullscreen())
					toggleFullscreen();
				break;
			case BACK:

				if (isLookingAtScreen() && mCamViewIndex != TREASURE
						&& NEED_SCENE) {
					switchIsland(TREASURE);
				}

				break;
			default:
				break;
			}
			// consume action
			actionFired[i] = false;
		}
	}

	private void toggleFullscreen() {

		if (mCamViewIndex == TREASURE) {
			canCamRotate = canCamRotate ? false : true;

			if (!canCamRotate) {

				for (int i = 0; i < screens.length; i++) {
					screens[i].setVisibility(true);
					// screens[i]
					// .translate(new SimpleVector(6+Math.abs(i-1)*0.6, 1.8 * (i
					// - 1), 1.45)
					// .calcSub(screens[i].getTransformedCenter()));
					screens[i].rotateZ((float) ((i - 1) * Math.PI / 4));

					mAnimatables.add(new TranslationAnimation("",
							new Object3D[] { screens[i] }, new SimpleVector(
									6 + Math.abs(i - 1) * 0.7, 1.5 * (i - 1),
									1.45).calcSub(screens[i]
									.getTransformedCenter()), null));
				}
			} else {
				for (int i = 0; i < screens.length; i++) {
					screens[i].setVisibility(false);
					screens[i].clearRotation();
					if (workspaces[i].isScrShown) {
						screens[i].setVisibility(true);
						mAnimatables
								.add(new TranslationAnimation(
										"",
										new Object3D[] { screens[i] },
										new SimpleVector(5, 1.8 * (i - 1), 2.5).calcSub(screens[i]
												.getTransformedCenter()), null));
						// screens[i]
						// .translate(new SimpleVector(5, 1.8 * (i - 1),
						// 2.5).calcSub(screens[i]
						// .getTransformedCenter()));
					}
				}
			}
			return;
		}

		Log.e(TAG,
				"toggleFullscreen " + mCamViewIndex + " "
						+ screens[mWsIdx].getVisibility());

		if (mCamViewIndex != TREASURE && screens[mWsIdx].getVisibility()) {
			canCamRotate = canCamRotate ? false : true;
			Log.e(TAG, "ready to toggleFullscreen");

			if (!canCamRotate) {
				cam.setOrientation(forward, up);
				cam.rotateZ(3.1415926f / 2);

				mAnimatables.add(new TranslationAnimation("",
						new Object3D[] { screens[mWsIdx] }, new SimpleVector(
								0.85, 0, -0.1), null));
			} else {
				mAnimatables.add(new TranslationAnimation("",
						new Object3D[] { screens[mWsIdx] }, new SimpleVector(
								-0.85, 0, 0.1), null) {
					@Override
					public void onAnimateSuccess() {
						screens[mWsIdx].clearTranslation();
						super.onAnimateSuccess();
					}
				});
			}

		}

	}

	private void switchIsland(final int idx) {
		if (idx == WORKSPACE)
			return;

		mAnimatables
				.add(new CamTranslationAnimation("", mCamViewspots[idx]
						.getTransformedCenter().calcSub(cam.getPosition()),
						cam, mCamViewspots[idx].getTransformedCenter(),
						(float) -Math.PI / 2) {
					@Override
					public void onAnimateSuccess() {
						mCamViewIndex = idx;
						super.onAnimateSuccess();
					}
				});

		spot.setPosition(mCamViewspots[idx].getTransformedCenter().calcAdd(
				new SimpleVector(0, 0, 5)));

		if (idx == TREASURE) {
			exitWorkspace();

			// hide the workspace
			mCamViewspots[WORKSPACE].translate(0, 0, -1000);

			for (int i = 0; i < workspaces.length; i++) {
				if (workspaces[i].isScrShown) {
					screens[i].setVisibility(true);
					screens[i]
							.translate(new SimpleVector(5, 1.8 * (i - 1), 2.5)
									.calcSub(screens[i].getTransformedCenter()));
				}
			}

		} else if (NEED_WORKSPACE) {

			mCamViewspots[WORKSPACE].translate(mCamViewspots[idx]
					.getTransformedCenter().calcSub(
							mCamViewspots[WORKSPACE].getTransformedCenter()));
			switchWorkspace(idx);
		}
	}

	private void enterWorkspace() {
		for (int i = 0; i < screens.length; i++) {
			if (workspaces[i].isScrShown) {
				screens[i].setVisibility(false);
				screens[i].clearTranslation();
			}
		}
	}

	private void exitWorkspace() {

		ws.animal.setVisibility(false);

		ws.isScrShown = screens[mWsIdx].getVisibility();

		screens[mWsIdx].setVisibility(false);

		ws.mCurrentApp.onHide();

		ws = workspaces[TREASURE];
		mWsIdx = TREASURE;

		for (int i = 0; i < workspaces.length; i++) {
			Log.e(TAG, "isScrShown" + i + ":" + workspaces[i].isScrShown);
		}
	}

	private void switchWorkspace(int idx) {

		if (mWsIdx != TREASURE) {
			exitWorkspace();
		} else {
			enterWorkspace();
		}

		// closed =================
		ws = workspaces[idx];

		workspaces[idx].animal.setVisibility(true);
		// restore scr visibility

		screens[idx].setVisibility(ws.isScrShown);

		mWsIdx = idx;
		peopleAnimation.setWorkspace(mWsIdx);

		ws.mCurrentApp.onShown();

	}

	private void pickScr() {
		PickGroup group;
		for (int i = 0; i < mPickGroupIcons.length; i++) {
			group = mPickGroupIcons[i];
			if (SceneHelper.isLookingAt(cam, ball1,
					group.group[0].getTransformedCenter()) > 0.995) {
				onActivateTilesGroup(group);

				if (actionFired[DOUBLE_TAP]) {
					if (i == 0) {
						actionFired[DOUBLE_TAP] = false;

						closeCurrentApp(null);
					} else if (i == 1) {
						actionFired[DOUBLE_TAP] = false;

						actionFired[TOGGLE_FULLSCREEN] = true;
					}
				}

			} else {
				onDeactivateTilesGroup(group);
			}
		}
	}

	public void openApp(final int idx, final Bundle bundle) {
		Log.e(TAG, ws.mState + " ==> going to open " + AppType.valueOf(idx + 1));

		// prevent from opening app while waiting
		if (ws.mState == 1)
			return;

		if (ws.mState != 0) {
			closeCurrentApp(new Runnable() {

				@Override
				public void run() {
					openApp(idx, bundle);
				}
			});

		} else {
			peopleAnimation.setPlayable();
			mAnimatables.add(peopleAnimation);

			ws.mState = 1;

			AppType app = AppType.valueOf(idx + 1);

			new AppLaunchThead(app, bundle).start();
		}
	}

	private void closeCurrentApp(final Runnable runnable) {
		if (ws.mState != 0) {
			// close the pic scr or idisplay scr
			ws.mCurrentApp.onClose(runnable);

		} else {
			onAppClosed();
			if (runnable != null)
				runnable.run();
		}
	}

	private void saveWorkspaceObject(String name, Object3D object3d) {

		if (name.startsWith("x_a")) {
			Log.e(TAG, "x_a");
			initAnimal(name, object3d);
			return;
		} else if (name.startsWith("x_b")) {
			Log.e(TAG, "x_b");
			apps[AppType.LAUNCHER.ordinal()].initObj(name, object3d);
			return;
		} else if (name.startsWith("x_i")) {
			Log.e(TAG, "x_i");
			initIcons(name, object3d);
			return;
		} else if (name.startsWith("x_l_l")) {
			Log.e(TAG, "x_l_l");
			apps[AppType.PIC.ordinal()].initObj(name, object3d);
			return;
		} else if (name.startsWith("x_l_m")) {
			Log.e(TAG, "x_l_m");
			apps[AppType.VIDEO.ordinal()].initObj(name, object3d);
			return;
		} else if (name.startsWith("x_w")) {
			Log.e(TAG, "x_l_m");
			apps[AppType.VIDEO.ordinal()].initObj(name, object3d);
			apps[AppType.PIC.ordinal()].initObj(name, object3d);
			return;
		} else if (name.startsWith("x_scr")) {
			Log.e(TAG, "x_scr");
			initScr(name, object3d);
			return;
		} else if (name.startsWith("x_p_p")) {
			Log.e(TAG, "x_p_people");
			initPeople(name, object3d);
			return;
		} else if (name.startsWith("x_p_s")) {
			Log.e(TAG, "x_p_people");
			apps[AppType.PIC.ordinal()].initObj(name, object3d);
			return;
		} else if (name.startsWith("x_f_")) {
			Log.e(TAG, "x_f");
			apps[AppType.FILE.ordinal()].initObj(name, object3d);
			return;
		} else if (name.startsWith("x_c_works")) {
			object3d.setVisibility(false);
			mCamViewspots[4] = object3d;
			return;
		} else if (name.startsWith("x_drone")) {
			apps[AppType.LAUNCHER.ordinal()].initObj(name, object3d);
			return;
		}
	}

	private void saveSceneObject(String name, Object3D object3d) {
		Log.e(TAG, "saveSceneObject " + name);
		if (name.startsWith("c_")) {
			initCamera(name, object3d);
		} else if (name.startsWith("w_")) {
			initWeather(name, object3d);
			return;
		} else if (name.startsWith("i_")) {
			Log.e(TAG, "i_");
			initIslands(name, object3d);
			return;
		} else if (name.startsWith("e_")) {
			Log.e(TAG, "e_");
			initEntrance(name, object3d);
			return;
		}
	}

	private void initEntrance(String name, Object3D object3d) {
		if (name.startsWith("e_horse")) {
			entrances[0] = object3d;
			return;
		}
	}

	private void initWeather(String name, Object3D object3d) {
		if (name.startsWith("w_forecast")) {
			weathers[0] = object3d;
			return;
		} else if (name.startsWith("w_location")) {
			weathers[1] = object3d;
			return;
		} else if (name.startsWith("w_sun")) {
			SimpleVector sv = new SimpleVector(object3d.getTransformedCenter());
			sv.y -= 10;
			sun.setPosition(sv);
			weathers[2] = object3d;
			return;
		} else if (name.startsWith("w_temp")) {
			weathers[3] = object3d;
			return;
		}

	}

	private void initAnimal(String name, Object3D object3d) {
		object3d.setVisibility(false);
		if (name.startsWith("x_a_dog"))
			workspaces[2].animal = object3d;
		else if (name.startsWith("x_a_raptor"))
			workspaces[1].animal = object3d;
		else if (name.startsWith("x_a_giraffe"))
			workspaces[0].animal = object3d;
	}

	private void initIslands(String name, Object3D object3d) {
		if (name.startsWith("i_trea")) {
			islands[3] = object3d;
			object3d.setAdditionalColor(new RGBColor(255, 255, 255));
		} else if (name.startsWith("i_ship"))
			islands[2] = object3d;
		else if (name.startsWith("i_volcano"))
			islands[1] = object3d;
		else if (name.startsWith("i_green"))
			islands[0] = object3d;
	}

	private void initPeople(String name, Object3D object3d) {
		if (name.startsWith("x_p_people")) {
			mPeoplePositionDummy = object3d;
			object3d.setVisibility(false);
		}
	}

	private void initScr(String name, Object3D object3d) {
		object3d.clearAdditionalColor();
		if (name.charAt(5) == '4') {
			curtain = object3d;
		} else {
			object3d.setShader(screenShaders[name.charAt(5) - '1']);
			object3d.calcTextureWrapSpherical();
			object3d.build();
			object3d.strip();
			screens[name.charAt(5) - '1'] = object3d;
		}

		object3d.setVisibility(false);
	}

	private void initCamera(String name, Object3D object3d) {
		Log.e(TAG, "initCamera " + name);
		if (name.startsWith("c_green")) {
			object3d.setVisibility(false);
			mCamViewspots[GREEN] = object3d;
			return;
		} else if (name.startsWith("c_ship")) {
			object3d.setVisibility(false);
			mCamViewspots[SHIP] = object3d;
			return;
		} else if (name.startsWith("c_volcano")) {
			object3d.setVisibility(false);
			mCamViewspots[VOLCANO] = object3d;
			return;
		} else if (name.startsWith("c_treasure")) {
			object3d.setVisibility(false);
			mCamViewspots[TREASURE] = object3d;
			return;
		}
	}

	private void getScreen(String name, Object3D object3d) {
		if (name.startsWith("x_")) {
			Log.e(TAG, "getScreen: " + name);
			if (mCamViewspots[WORKSPACE] != object3d)
				mCamViewspots[WORKSPACE].addChild(object3d);
		}
	}

	private void initIcons(String name, Object3D object3d) {
		TextureManager tm = TextureManager.getInstance();
		String tname = name.substring(2, name.indexOf("_Plane"));
		object3d.clearAdditionalColor();

		Log.e(TAG, "matching tname:" + tname);

		clickableIcons.put(tname, object3d);

		SceneHelper.printTexture(tname, object3d);
	}

	private void postInitScr() {
		for (int i = 0; i < 2; i++) {
			mPickGroupIcons[i] = new PickGroup(1);
		}
		Object3D tmp;
		tmp = clickableIcons.get("i_close");
		mPickGroupIcons[0].group[0] = tmp;

		tmp = clickableIcons.get("i_fullscreen");
		mPickGroupIcons[1].group[0] = tmp;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGestureDetector = new GestureDetector(this, mGestureListener);
	}

	@Override
	public void onDrawEye(Eye eye) {

		fb.clear(back);

		if(eye.getType() == Eye.Type.LEFT){
			if (NEED_SKYBOX) {
				world.renderScene(fb);
				sky.render(world, fb);
			} else {
				world.renderScene(fb);
			}
			if (NEO) {
				world.drawWireframe(fb, wire, 2, false);
			} else {
				world.draw(fb);
			}
		}else {
			if(singleEye){
				fb.clear(black);
			}else {
				if (NEED_SKYBOX) {
					world.renderScene(fb);
					sky.render(world, fb);
				} else {
					world.renderScene(fb);
				}
				if (NEO) {
					world.drawWireframe(fb, wire, 2, false);
				} else {
					world.draw(fb);
				}
			}
		}
		
		fb.display();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}

	private void flyWorkspace() {
		canCamRotate = false;
		isCamFlying = true;
		SimpleVector ori = new SimpleVector();
		cam.getPosition(ori);

		final Animatable a2 = new CamAnimation(cam, ori, ori, 0, 2.5, 10, 3) {
			public void onAnimateSuccess() {
				// super.onAnimateSuccess();

				final Animatable a3 = new CamTranslationAnimation("",
						ori.calcSub(cam.getPosition()), cam,
						ori.calcAdd(forward), (float) Math.PI / 2) {

					public void onAnimateSuccess() {
						super.onAnimateSuccess();

						isCamFlying = false;
						canCamRotate = true;
					}
				};

				mAnimatables.add(a3);

			}
		};

		Animatable a1 = new CamTranslationAnimation("", new SimpleVector(0, 10,
				0), cam, ori, -(float) Math.PI / 2) {
			public void onAnimateSuccess() {
				super.onAnimateSuccess();

				mAnimatables.add(a2);
			}
		};

		mAnimatables.add(a1);
	}

	private void flyIsland() {
		canCamRotate = false;
		isCamFlying = true;
		SimpleVector ori = new SimpleVector();
		cam.getPosition(ori);

		final SimpleVector volcano = islands[VOLCANO].getTransformedCenter();

		final Animatable a4 = new CamAnimation(cam, volcano, volcano,
				3 * Math.PI / 4, Math.PI / 4, 50, 30, false) {
			@Override
			public void onAnimateSuccess() {

				final Animatable a5 = new CamTranslationAnimation("",
						mCamViewspots[TREASURE].getTransformedCenter().calcSub(
								cam.getPosition()), cam, volcano,
						(float) Math.PI / 2) {
					@Override
					public void onAnimateSuccess() {
						canCamRotate = true;
						isCamFlying = false;
						singleEye = true;
						super.onAnimateSuccess();
					}
				};

				mAnimatables.add(a5);
			}
		};

		final Animatable a3 = new CamTranslationAnimation("", islands[VOLCANO]
				.getTransformedCenter()
				.calcAdd(
						new SimpleVector(50 / Math.sqrt(2), -50 / Math.sqrt(2),
								30)).calcSub(cam.getPosition()), cam, volcano,
				(float) Math.PI / 2) {

			public void onAnimateSuccess() {
				super.onAnimateSuccess();

				mAnimatables.add(a4);

			}
		};

		final Animatable a2 = new CamAnimation(cam, ori, ori, 0, 2.8, 10, 3) {
			public void onAnimateSuccess() {
				// super.onAnimateSuccess();

				mAnimatables.add(a3);

			}
		};

		final Animatable a1 = new CamTranslationAnimation("", new SimpleVector(
				0, 10, 0), cam, ori, (float) -Math.PI / 2) {
			public void onAnimateSuccess() {
				super.onAnimateSuccess();

				mAnimatables.add(a2);
			}
		};

		mAnimatables.add(a1);
	}

	@Override
	public void onCardboardTrigger() {
		show3DToastOnlyRight("onCardboardTrigger");

		actionFired[TOGGLE_FULLSCREEN] = true;
		super.onCardboardTrigger();
	}

	@Override
	public void onIDisplayConnected() {
		show3DToastOnlyRight("onIDisplayConnected");
		synchronized (iDisplayKeeper) {
			iDisplayKeeper.notifyAll();
		}
		super.onIDisplayConnected();
	}

	@Override
	OnRemoteChangeListener getRemoteListener() {
		return remoteChangeListener;
	}

	@Override
	IGestureListener getDolphinGestureListener() {
		return gestureListener;
	}

	@Override
	public void onAppReady() {
		peopleAnimation.fadeout(mAnimatables);
	}

	@Override
	public void onAppClosed() {
		peopleAnimation.show(mAnimatables);
		ws.mState = 0;
		ws.mCurrentAppType = AppType.NULL;
	}

	public void onCallObj(Object3D[] cur, boolean display) {
		for (Object3D object3d : cur) {
			object3d.translate(-5, 0, 0);
		}
		mAnimatables.add(new TranslationAnimation("", cur, new SimpleVector(5,
				0, 0), null));
		mAnimatables.add(new DisplayAnimation(cur, "", false));
	}

	@Override
	public void onCallScreen() {
		if (screens[mWsIdx].getVisibility())
			return;

		onCallObj(new Object3D[] { screens[mWsIdx] }, true);
	}

	@Override
	public void onHideScreen(final Runnable runnable) {
		if (!screens[mWsIdx].getVisibility())
			return;

		Log.e(TAG, "onHideScreen");

		Object3D[] cur = new Object3D[] { screens[mWsIdx] };

		onHideObj(cur, true, runnable);
	}

	@Override
	public void onHideObj(Object3D[] cur, boolean displayAnimation,
			final Runnable runnable) {

		mAnimatables.add(new TranslationAnimation("", cur, new SimpleVector(
				-40, 0, 0), null) {
			@Override
			public void onAnimateSuccess() {
				for (int i = 0; i < object3ds.length; i++) {
					object3ds[i].setVisibility(false);
					object3ds[i].clearTranslation();
				}

				if (runnable != null)
					runnable.run();
				// TODO scene.

				super.onAnimateSuccess();
			}
		});
		if (displayAnimation)
			mAnimatables.add(new DisplayAnimation(cur, "", true));
	}

	@Override
	public void onAppFail() {
		abandon();
	}

	void abandon() {
		peopleAnimation.stop();
	}

	public void onActivateTilesGroup(PickGroup group) {
		SimpleVector trans = new SimpleVector();
		SimpleVector ori;

		if (group.oriPos[0] == null) {
			ori = group.group[0].getTransformedCenter();
		} else {
			ori = group.oriPos[0];
		}

		if (group.state == 0) {
			group.state = 1;

			if (group.animation == null) {
				Log.e(TAG, "trans 0 -> 1 with null");

				trans = cam.getPosition().calcSub(ori).normalize();
				group.animation = new TranslationAnimation("", group.group,
						new SimpleVector(trans), group);
				mAnimatables.add(group.animation);
			} else {
				Log.e(TAG, "trans 0 -> 1 with animation");

				group.animation.stop();

				trans = cam.getPosition().calcSub(ori).normalize();

				SimpleVector hasTrns = new SimpleVector();
				group.group[0].getTranslation(hasTrns);

				group.animation = new TranslationAnimation("", group.group,
						trans.calcSub(hasTrns), group);
				mAnimatables.add(group.animation);
			}

		} else {

		}
	}

	public void onDeactivateTilesGroup(PickGroup group) {

		SimpleVector trns;

		if (group.oriPos[0] == null) {
			trns = new SimpleVector().calcSub(group.group[0].getTranslation());
		} else {
			trns = group.oriPos[0].calcSub(group.group[0]
					.getTransformedCenter());
		}

		if (group.state == 0) {

		} else {
			group.state = 0;

			if (group.animation == null) {
				Log.e(TAG, "trans 1 -> 0 with null");

				group.animation = new TranslationAnimation("", group.group,
						trns, group);
				mAnimatables.add(group.animation);
			} else {
				Log.e(TAG, "trans 1 -> 0 with animation");

				group.animation.stop();

				group.animation = new TranslationAnimation("", group.group,
						trns, group);
				mAnimatables.add(group.animation);

			}
		}
	}

	@Override
	public void onOpenApp(int idx, Bundle bundle) {
		openApp(idx, bundle);
	}

	@Override
	public int getScreenIdx() {
		return mWsIdx;
	}

	@Override
	public boolean isLookingAtScreen() {
		return SceneHelper.isLookingDir(cam, ball1, scrDir) > 0.95;
	}

	@Override
	public void onIDisplayDenyed() {
		show3DToastOnlyRight("onIDisplayDenyed");
		synchronized (iDisplayKeeper) {
			iDisplayKeeper.notifyAll();
		}
		super.onIDisplayDenyed();
	}

	@Override
	public void OnIDisplayUnexpectedError() {
		show3DToastOnlyRight("OnIDisplayUnexpectedError");
		synchronized (iDisplayKeeper) {
			iDisplayKeeper.notifyAll();
		}
		super.OnIDisplayUnexpectedError();
	}

	@Override
	public void onCallCurtain(String tex) {
		if (curtain.getVisibility())
			return;
		curtain.setTexture(tex);
		onCallObj(new Object3D[] { curtain }, true);
	}

	@Override
	public void onHideCurtain() {
		if (!curtain.getVisibility())
			return;
		onHideObj(new Object3D[] { curtain }, true, null);
	}

	@Override
	public void onSwitchMode(ConnectionMode mode) {
		iDisplayKeeper.switchMode(mode);
	}

	@Override
	public void onSceneToggleFullscreen() {
		toggleFullscreen();
	}

}
