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

public class SceneActivity extends Framework3DMatrixActivity {

	private static final String TAG = "SceneActivity";

	final static int PIC_COUNT_PER_PAGE = 6;
	final static int VIDEO_COUNT_PER_PAGE = 4;
	final static int WORKSPACE_COUNT = 3;

	final static boolean NEED_WORKSPACE = true;
	final static boolean NEED_SCENE = true;

	enum App {
		NULL(0), MINECRAFT(1), CAR(2), VIDEO(3), PIC(4), SKYPE(5), IE(6), FILE(
				7), WORD(8), EXCEL(9), PPT(10), CAM(11), FILE_OPEN(12);

		private int aa;

		private static Map<Integer, App> map = new HashMap<Integer, App>();

		static {
			for (App legEnum : App.values()) {
				map.put(legEnum.aa, legEnum);
			}
		}

		private App(final int a) {
			aa = a;
		}

		public static App valueOf(int a) {
			return map.get(a);
		}
	}

	final static int SINGLE_TAP = 0;
	final static int DOUBLE_TAP = 1;
	final static int TOGGLE_FULLSCREEN = 2;
	final static int LONG_PRESS = 3;
	final static int LEFT = 4;
	final static int RIGHT = 5;
	final static int UP = 6;
	final static int DOWN = 7;

	final static int GREEN = 0;
	final static int VOLCANO = 1;
	final static int SHIP = 2;
	final static int TREASURE = 3;
	final static int WORKSPACE = 4;

	final static int HORSE = 0;
	final static int DRONE = 1;

	private boolean NEO = false;

	class AppLaunchThead extends Thread {
		App app;

		AppLaunchThead(App app) {
			this.app = app;
		}

		@Override
		public void run() {
			try {
				sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Log.e(TAG, "thread run");

			hideList(0, 13);

			switch (app) {
			case PIC:
				drawText("l_opt", new String[] { "hello pic", "line2" });

				showList(0, 6);
				showList(10, 13);

				peopleAnimation.fadeout(mAnimatables);
				openPic(1);

				break;
			case VIDEO:
				drawText("l_opt", new String[] { "hello video", "line2" });

				showList(6, 13);

				peopleAnimation.fadeout(mAnimatables);
				screens[mWsIdx].translate(-5, 0, 0);

				mAnimatables.add(new TranslationAnimation("",
						new Object3D[] { screens[mWsIdx] }, new SimpleVector(5,
								0, 0), null));
				mAnimatables.add(new DisplayAnimation(
						new Object3D[] { screens[mWsIdx] }, "", false));

				break;
			case FILE:
				peopleAnimation.fadeout(mAnimatables);

				for (int i = 0; i < desksfiles.length; i++) {
					desksfiles[i].clearTranslation();
					desksfiles[i].translate(-5, 0, 0);
					desksfiles[i].setVisibility(true);
				}

				mAnimatables.add(new TranslationAnimation("", desksfiles,
						new SimpleVector(5, 0, 0), null));

				break;
			default:
				peopleAnimation.fadeout(mAnimatables);

				screens[mWsIdx].translate(-5, 0, 0);

				mAnimatables.add(new TranslationAnimation("",
						new Object3D[] { screens[mWsIdx] }, new SimpleVector(5,
								0, 0), null));
				mAnimatables.add(new DisplayAnimation(
						new Object3D[] { screens[mWsIdx] }, "", false));

				break;
			}
			ws.mState = 2;
			ws.mCurrentApp = app;
		}
	}

	protected SimpleVector forward = new SimpleVector(-1, 0, 0);
	protected SimpleVector backward = new SimpleVector(1, 0, 0);
	protected SimpleVector up = new SimpleVector(0, -1, 0);

	protected Object3D[] mCamViewspots = null;
	protected int mCamViewIndex = 4;

	protected Object3D[] mWorkspaceObjects = null;
	protected Object3D[] mSceneObjects = null;

	private static float BALL_DISTANCE = 2f;

	private int mFrameCounter = 0;

	private Camera cam;

	protected FrameBuffer fb = null;
	protected SkyBox sky;
	protected World world = null;
	protected Light sun = null;
	protected Light spot = null;
	protected Object3D[] screens = null;
	protected Object3D[] weathers = null;
	protected Object3D[] entrances = null;
	protected Object3D[] islands = null;
	protected Object3D[] desksfiles = null;
	protected Object3D[] desks = null;
	protected Object3D[] files = null;
	private boolean[] scrShown = new boolean[3];

	protected PeopleAnimation peopleAnimation;

	protected RGBColor back = new RGBColor(50, 50, 100);
	protected RGBColor wire = new RGBColor(100, 100, 100);

	protected int[] buffer;
	protected boolean canCamRotate = true;

	private Object3D ball1 = null;

	List<Animatable> mAnimatables = new LinkedList<Animatable>();

	private boolean test = true;

	private Object3D mSkype;

	private Object3D mList;

	private Object3D mPeoplePositionDummy;
	private Object3D[] picScrs;

	private Workspace[] workspaces;
	private Workspace ws;

	private int mWsIdx = 0;

	private long lastBallUpdate = 0;

	private boolean isAnimationOn = true;

	private LiveTileAnimation[] mBoardTiles = new LiveTileAnimation[] {
			new LiveTileAnimation("minecraft"), new LiveTileAnimation(""),
			new LiveTileAnimation(""), new LiveTileAnimation(""), };

	private LiveTileAnimation[] mPicListTiles = new LiveTileAnimation[] {
			new LiveTileAnimation("", false, null),
			new LiveTileAnimation("", false, null),
			new LiveTileAnimation("", false, null),
			new LiveTileAnimation("", false, null),
			new LiveTileAnimation("", false, null),
			new LiveTileAnimation("", false, null), };

	private LiveTileAnimation[] mVideoListTiles = new LiveTileAnimation[] {
			new LiveTileAnimation("", false, null),
			new LiveTileAnimation("", false, null),
			new LiveTileAnimation("", false, null),
			new LiveTileAnimation("", false, null), };

	private Map<String, Object3D> clickableBoards = new HashMap<String, Object3D>();
	private Map<String, Object3D> clickableIcons = new HashMap<String, Object3D>();
	private Map<String, Object3D> clickableLists = new HashMap<String, Object3D>();
	private Map<String, Object3D> clickableFiles = new HashMap<String, Object3D>();
	private Map<String, Object3D> clickableDesks = new HashMap<String, Object3D>();

	PickGroup[] mPickGroupBoards = new PickGroup[11];
	PickGroup[] mPickGroupLists = new PickGroup[6 + 4 + 2 + 1];
	PickGroup[] mPickGroupIcons = new PickGroup[2];
	PickGroup[] mPickGroupFiles = new PickGroup[3];
	PickGroup[] mPickGroupDesks = new PickGroup[4];

	GestureDetector mGestureDetector = null;

	boolean[] actionFired = new boolean[8];

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
				actionFired[RIGHT] = true;
			} else if (e.getRawX() > 1920 - 480) {
				actionFired[LEFT] = true;
			} else {
				if (e.getRawY() < 540) {
					actionFired[UP] = true;
				} else {
					actionFired[DOWN] = true;
				}
			}

			if (ws.mCurrentApp == App.PIC) {

			} else {
//				int ii = mCamViewIndex + 1 > 3 ? 0 : mCamViewIndex + 1;
//				switchIsland(ii);
				actionFired[TOGGLE_FULLSCREEN] = true;
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
			Log.e(TAG, "remote : onMove x:" + p.x + " y: " + p.y);
			point = p;
			lastBallUpdate = System.currentTimeMillis();
		}

		@Override
		public void onClick() {
			Log.e(TAG, "remote : onClick");
			actionFired[DOUBLE_TAP] = true;
			if (test) {
				test = false;
				ball1.setAdditionalColor(new RGBColor(0, 100, 0));
			} else {
				test = true;
				ball1.setAdditionalColor(new RGBColor(0, 0, 100));
			}
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
			double y = p.x - dragStartPoint.y;

			if (x < -100) {
				actionFired[LEFT] = true;
			} else if (x > 100) {
				actionFired[RIGHT] = true;
			} else if (y < -100) {
				actionFired[DOWN] = true;
			} else if (y > 100) {
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

			show3DToast(event.result);
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
						"" + GestureEvent.Gestures.SWIPE_BACK_LEFT_L.ordinal(),
						true);
				masks.put(
						"" + GestureEvent.Gestures.SWIPE_BACK_RIGHT_L.ordinal(),
						true);
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

		show3DToast("loading obj");

		try {
			if (NEED_WORKSPACE)
				mWorkspaceObjects = Loader.loadOBJ(
						getAssets().open("workspace.obj"),
						getAssets().open("workspace.mtl"), 1);
			if (NEED_SCENE)
				mSceneObjects = Loader.loadOBJ(getAssets().open("matnew.obj"),
						getAssets().open("matnew.mtl"), 1);

		} catch (IOException e) {
			e.printStackTrace();
		}

		cam = world.getCamera();

		cam.setPosition(0, 0, 0);

		if (NEED_SCENE) {
			islands = new Object3D[4];
			weathers = new Object3D[4];
			entrances = new Object3D[2];
			world.addObjects(mSceneObjects);
		}

		if (NEED_WORKSPACE) {
			screens = new Object3D[3];
			desksfiles = new Object3D[3 + 4];
			desks = new Object3D[4];
			files = new Object3D[3];
			workspaces = new Workspace[3];
			for (int i = 0; i < workspaces.length; i++) {
				workspaces[i] = new Workspace();
			}
			ws = workspaces[mWsIdx];

			peopleAnimation = new PeopleAnimation();
			picScrs = new Object3D[2];

			world.addObjects(mWorkspaceObjects);
		}

		world.setAmbientLight(150, 150, 150);

		sun = new Light(world);
		sun.setIntensity(250, 250, 250);

		spot = new Light(world);
		spot.setIntensity(50, 50, 50);

		ball1 = Primitives.getSphere(0.05f);
		ball1.translate(0, 0, -2);
		ball1.calcTextureWrapSpherical();
		ball1.setAdditionalColor(new RGBColor(100, 0, 0));
		ball1.strip();
		ball1.build();
		world.addObject(ball1);

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

		if (mFrameCounter == 0) {
			mFrameCounter++;
		} else if (mFrameCounter == 1) {
			mFrameCounter++;
			show3DToast("sceneInit");
			sceneInit();

		} else {

			sceneLoop();

		}

		headTransform.getEulerAngles(mAngles, 0);

		// if(mCamViewspots != null){
		// cam.setOrientation(forward, up);
		// }else {
		// cam.setOrientation(forward, up);
		// }

		if (canCamRotate) {
			cam.setOrientation(forward, up);
			cam.rotateZ(3.1415926f / 2);
			cam.rotateY(mAngles[1]);
			cam.rotateZ(-mAngles[2]);
			cam.rotateX(mAngles[0]);
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
				mCamViewspots[WORKSPACE].translate(-1000, -1000, -1000);
			}

			postInitBoard();
			postInitList();
			postInitScr();
			postInitFile();

			for (int i = 0; i < mBoardTiles.length; i++) {
				mAnimatables.add(mBoardTiles[i]);
			}

			mSkype = clickableBoards.get("b_skype");

			mList = clickableLists.get("l_l2");

			peopleAnimation.init(getAssets(), world, mCamViewspots[WORKSPACE],
					mPeoplePositionDummy);

			switchWorkspace(0);

			peopleAnimation.show(mAnimatables);

		}

		if (NEED_SCENE) {
			switchIsland(TREASURE);
		}
		// startIDisplay(currentMode);

	}

	private void sceneLoop() {

		// deal with input actions
		fireAction();

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
			ball1.setTransparency(0);
			ball1.rotateAxis(cam.getUpVector(), 0f);
			ball1.rotateAxis(cam.getSideVector(), 0f);
		} else {
			ball1.setTransparency(100);
			ball1.rotateAxis(cam.getUpVector(), (float) (0.5f * point.x));
			ball1.rotateAxis(cam.getSideVector(), (float) (0.5f * point.y));
		}

	}

	private void fireAction() {

		// consume first
		// pick obj
		if (NEED_WORKSPACE && mCamViewIndex != TREASURE) {
			if (SceneHelper.isLookingAt(cam, ball1,
					mSkype.getTransformedCenter()) > 0.75) {

				pickBoard();

			} else if (SceneHelper.isLookingAt(cam, ball1,
					screens[0].getTransformedCenter()) > 0.8) {

				pickScr();

			} else if ((ws.mCurrentApp == App.PIC || ws.mCurrentApp == App.VIDEO)
					&& SceneHelper.isLookingAt(cam, ball1,
							mList.getTransformedCenter()) > 0.8) {

				pickList();
			}
		}

		for (int i = 0; i < actionFired.length; i++) {
			if (!actionFired[i])
				continue;

			switch (i) {
			case LEFT:
				if (ws.mCurrentApp == App.PIC)
					slidePic(true);
				break;
			case RIGHT:
				if (ws.mCurrentApp == App.PIC)
					slidePic(false);
				break;
			case UP:
				if (ws.mCurrentApp == App.PIC)
					scalePic(true);
				break;
			case DOWN:
				if (ws.mCurrentApp == App.PIC)
					scalePic(false);
				break;

			case DOUBLE_TAP:

				if(NEED_SCENE){
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
					switch (mCamViewIndex) {
					case GREEN:
					case SHIP:
					case VOLCANO:
					case WORKSPACE:

						if (SceneHelper.isLookingAt(cam, ball1,
								ws.animal.getTransformedCenter()) > 0.995) {
							flyWorkspace();
							return;
						}
						break;
					case TREASURE:

						if (SceneHelper.isLookingAt(cam,
								entrances[HORSE].getTransformedCenter()) > 0.95) {
							flyIsland();
						} else if (SceneHelper.isLookingAt(cam,
								entrances[DRONE].getTransformedCenter()) > 0.95) {
							flyDrone();
						} else if (SceneHelper.isLookingAt(cam, backward) > 0.95) {
							NEO = NEO ? false : true;
							show3DToast("NEO");
						}

						break;
					default:
						break;
					}
				}

				break;

			case TOGGLE_FULLSCREEN:

				toggleFullscreen();

				break;
			default:
				break;
			}

			// consume action
			actionFired[i] = false;
		}
	}

	private void toggleFullscreen() {
		Log.e(TAG, "toggleFullscreen "+mCamViewIndex+" "+ws.isScrShown);
		
		if (mCamViewIndex != 3 && screens[mWsIdx].getVisibility()) {
			canCamRotate = canCamRotate ? false : true;
			Log.e(TAG, "ready to toggleFullscreen");

			if(!canCamRotate){
				cam.setOrientation(forward, up);
				cam.rotateZ(3.1415926f / 2);

				mAnimatables.add(new TranslationAnimation("",
						new Object3D[] { screens[mWsIdx] }, new SimpleVector(0.8, 0,
								0), null));
			}else {
				mAnimatables.add(new TranslationAnimation("",
						new Object3D[] { screens[mWsIdx] }, new SimpleVector(-0.8, 0,
								0), null){
					@Override
					public void onAnimateSuccess() {
						screens[mWsIdx].clearTranslation();
						super.onAnimateSuccess();
					}
				});
			}
			
		} else if (mCamViewIndex == 3) {
			canCamRotate = canCamRotate ? false : true;

			if(!canCamRotate){
				for (int i = 0; i < workspaces.length; i++) {
					if(workspaces[i].isScrShown){
						// TODO gjw switch screen
						
					}
				}
			}else {
				for (int i = 0; i < workspaces.length; i++) {
					if(workspaces[i].isScrShown){
						screens[i].setVisibility(true);
						screens[i].translate(new SimpleVector(5,1.8*(i-1),2.5).calcSub(screens[i].getTransformedCenter()));
					}
				}
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

		spot.setPosition(mCamViewspots[idx].getTransformedCenter());

		if (idx == TREASURE) {
			switchWorkspace(0);

			// hide the workspace
			mCamViewspots[WORKSPACE].translate(-1000, -1000, -1000);
			
			for (int i = 0; i < workspaces.length; i++) {
				if(workspaces[i].isScrShown){
					screens[i].setVisibility(true);
					screens[i].translate(new SimpleVector(5,1.8*(i-1),2.5).calcSub(screens[i].getTransformedCenter()));
				}
			}
			
		} else if (NEED_WORKSPACE) {
			
			for (int i = 0; i < screens.length; i++) {
				if(workspaces[i].isScrShown){
					screens[i].setVisibility(false);
					screens[i].clearTranslation();
				}
			}
			
			mCamViewspots[WORKSPACE].translate(mCamViewspots[idx]
					.getTransformedCenter().calcSub(
							mCamViewspots[WORKSPACE].getTransformedCenter()));
			switchWorkspace(idx);
		}
	}

	private void switchWorkspace(int idx) {
		
		for (int i = 0; i < workspaces.length; i++) {
			Log.e(TAG, "isScrShown"+i+":"+workspaces[i].isScrShown);
		}
		
		ws.animal.setVisibility(false);

		ws.isScrShown = screens[mWsIdx].getVisibility();

		screens[mWsIdx].setVisibility(false);
		picScrs[0].setVisibility(false);
		picScrs[1].setVisibility(false);

		hideList(0, 13);

		// closed =================
		ws = workspaces[idx];

		workspaces[idx].animal.setVisibility(true);
		// restore scr visibility

		screens[idx].setVisibility(ws.isScrShown);

		// restore pic && mov index
		if (ws.mPicPageIdx != workspaces[mWsIdx].mPicPageIdx) {
			flipPicList();
		}

		if (ws.mVideoPageIdx != workspaces[mWsIdx].mVideoPageIdx) {
			flipVideoList();
		}

		mWsIdx = idx;
		peopleAnimation.setWorkspace(mWsIdx);

		// restore app settings
		switch (ws.mCurrentApp) {
		case PIC:
			showList(0, 6);
			showList(10, 13);
			break;
		case VIDEO:
			showList(6, 13);
			break;
		default:
			break;
		}
	}

	private void showList(int from, int to) {
		Object3D[] group;
		for (int i = from; i < to; i++) {
			group = mPickGroupLists[i].group;
			for (int j = 0; j < group.length; j++) {
				group[j].setVisibility(true);
			}
		}

	}

	private void hideList(int from, int to) {
		Object3D[] group;
		for (int i = from; i < to; i++) {
			group = mPickGroupLists[i].group;
			for (int j = 0; j < group.length; j++) {
				group[j].setVisibility(false);
			}
		}
	}

	private void pickList() {

		PickGroup group = null;
		Object3D object3d = null;
		SimpleVector trans = new SimpleVector();

		switch (ws.mCurrentApp) {
		case PIC:
			for (int i = 0; i < 6; i++) {
				group = mPickGroupLists[i];
				if (SceneHelper.isLookingAt(cam, ball1,
						group.group[0].getTransformedCenter()) > 0.995) {
					activateTilesGroup(group);

					if (actionFired[DOUBLE_TAP]) {
						actionFired[DOUBLE_TAP] = false;
						openPic(i + PIC_COUNT_PER_PAGE * ws.mPicPageIdx + 1);
					}

				} else {
					deactivateTilesGroup(group);
				}
			}
			break;
		case VIDEO:
			for (int i = 6; i < 10; i++) {
				group = mPickGroupLists[i];
				if (SceneHelper.isLookingAt(cam, ball1,
						group.group[0].getTransformedCenter()) > 0.995) {
					activateTilesGroup(group);

					if (actionFired[DOUBLE_TAP]) {
						actionFired[DOUBLE_TAP] = false;
						openVideo(i + VIDEO_COUNT_PER_PAGE * ws.mVideoPageIdx
								+ 1);
					}
				} else {
					deactivateTilesGroup(group);
				}
			}
			break;
		default:
			break;
		}

		if (!actionFired[DOUBLE_TAP])
			return;

		actionFired[DOUBLE_TAP] = false;

		for (int i = 10; i < mPickGroupLists.length; i++) {
			group = mPickGroupLists[i];
			object3d = group.group[0];

			if (SceneHelper.isLookingAt(cam, ball1,
					object3d.getTransformedCenter()) > 0.995) {
				if (object3d.getName().startsWith("x_l_next")
						|| object3d.getName().startsWith("x_l_back")) {

					if (ws.mCurrentApp == App.VIDEO) {
						flipVideoList();
					} else if (ws.mCurrentApp == App.PIC) {
						flipPicList();
					}

				}
			}
		}
	}

	private void flipPicList() {

		for (int i1 = 0; i1 < mPicListTiles.length; i1++) {
			mPicListTiles[i1].reset();
			// mAnimatables.add(mListTiles[i1]);
		}

		mAnimatables.add(new SeqAnimation(mAnimatables, mPicListTiles));

		ws.mPicPageIdx = ws.mPicPageIdx + 1 % 2;
	}

	private void flipVideoList() {

		for (int i1 = 0; i1 < mVideoListTiles.length; i1++) {
			mVideoListTiles[i1].reset();
			// mAnimatables.add(mListTiles[i1]);
		}

		mAnimatables.add(new SeqAnimation(mAnimatables, mVideoListTiles));

		ws.mVideoPageIdx = ws.mVideoPageIdx + 1 % 2;
	}

	private void pickBoard() {
		PickGroup group;
		for (int i = 0; i < mPickGroupBoards.length; i++) {
			group = mPickGroupBoards[i];
			if (SceneHelper.isLookingAt(cam, ball1,
					group.group[0].getTransformedCenter()) > 0.995) {
				activateTilesGroup(group);

				if (actionFired[DOUBLE_TAP]) {
					actionFired[DOUBLE_TAP] = false;

					openApp(i);
				}

			} else {
				deactivateTilesGroup(group);
			}
		}
	}

	private void pickScr() {
		PickGroup group;
		for (int i = 0; i < mPickGroupIcons.length; i++) {
			group = mPickGroupIcons[i];
			if (SceneHelper.isLookingAt(cam, ball1,
					group.group[0].getTransformedCenter()) > 0.995) {
				activateTilesGroup(group);

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
				deactivateTilesGroup(group);
			}
		}
	}

	private void openVideo(int i) {

		// TODO gjw draw video desc

		
	}

	private void openPic(int i) {

		ws.mCurrentPic = i;

		picScrs[0].setVisibility(false);
		picScrs[1].setVisibility(false);
		picScrs[1].clearTranslation();
		picScrs[0].clearTranslation();

		if (i < 10) {
			picScrs[0].setTexture("p_" + i);
		} else {
			picScrs[0].setTexture("p_" + (char) ('a' + (i - 10)));
		}

		picScrs[0].translate(-3, 0, 0);

		mAnimatables
				.add(new TranslationAnimation("",
						new Object3D[] { picScrs[0] },
						new SimpleVector(3, 0, 0), null));

		mAnimatables.add(new DisplayAnimation(new Object3D[] { picScrs[0] },
				"", false));

		if (picScrs[0].getScale() != 1f) {
			mAnimatables.add(new ScaleAnimation(new Object3D[] { picScrs[0] },
					"", 1f));
		}

		// TODO gjw draw pic desc
	}

	private void slidePic(boolean slideLeft) {

		if (!(picScrs[0].getVisibility() || picScrs[1].getVisibility()))
			return;

		ws.mCurrentPic += slideLeft ? -1 : 1;
		ws.mCurrentPic = ws.mCurrentPic < 1 ? 12 : (ws.mCurrentPic > 12 ? 1
				: ws.mCurrentPic);

		Object3D pre = null, cur = null;
		SimpleVector moveDir;
		SimpleVector presetDir;

		if (slideLeft) {
			moveDir = new SimpleVector(0, -2, 0);
			presetDir = new SimpleVector(0, 2, 0);
		} else {
			moveDir = new SimpleVector(0, 2, 0);
			presetDir = new SimpleVector(0, -2, 0);
		}

		if (picScrs[0].getVisibility()) {
			pre = picScrs[0];
			cur = picScrs[1];
		} else if (picScrs[1].getVisibility()) {
			pre = picScrs[1];
			cur = picScrs[0];
		}

		if (ws.mCurrentPic < 10) {
			cur.setTexture("p_" + ws.mCurrentPic);
		} else {
			cur.setTexture("p_" + (char) ('a' + (ws.mCurrentPic - 10)));
		}
		pre.clearTranslation();
		cur.clearTranslation();
		cur.setScale(1f);

		cur.translate(presetDir);

		mAnimatables.add(new TranslationAnimation("", new Object3D[] { pre },
				moveDir, null));
		mAnimatables.add(new TranslationAnimation("", new Object3D[] { cur },
				moveDir, null));

		mAnimatables
				.add(new DisplayAnimation(new Object3D[] { pre }, "", true));
		mAnimatables
				.add(new DisplayAnimation(new Object3D[] { cur }, "", false));

		mAnimatables.add(new ScaleAnimation(new Object3D[] { pre }, "", 1f));

		// TODO gjw draw pic desc

	}

	private void scalePic(boolean up) {
		if (!(picScrs[0].getVisibility() || picScrs[1].getVisibility()))
			return;

		Object3D cur = null;
		if (picScrs[0].getVisibility()) {
			cur = picScrs[0];
		} else if (picScrs[1].getVisibility()) {
			cur = picScrs[1];
		}

		float scale = cur.getScale() + (up ? 0.2f : -0.2f);

		mAnimatables.add(new ScaleAnimation(new Object3D[] { cur }, "", scale));

	}

	private void openApp(final int idx) {
		// prevent from opening app while waiting
		if (ws.mState == 1)
			return;

		if (ws.mState != 0) {
			closeCurrentApp(new Runnable() {

				@Override
				public void run() {
					openApp(idx);
				}
			});

		} else {
			peopleAnimation.setPlayable();
			mAnimatables.add(peopleAnimation);

			ws.mState = 1;

			App app = App.valueOf(idx + 1);

			new AppLaunchThead(app).start();
		}
	}

	private void closeCurrentApp(final Runnable runnable) {
		if (ws.mState != 0) {
			// close the pic scr or idisplay scr

			Object3D[] cur = new Object3D[1];

			switch (ws.mCurrentApp) {
			case PIC:
				cur[0] = picScrs[0].getVisibility() ? picScrs[0] : null;
				cur[0] = picScrs[1].getVisibility() ? picScrs[1] : cur[0];
				break;
			case FILE:
				cur = desks[0].getVisibility() ? desks : cur;
				break;
			default:
				cur[0] = screens[mWsIdx].getVisibility() ? screens[mWsIdx]
						: null;
				break;
			}

			if (cur[0] != null) {

				hideList(0, 13);

				peopleAnimation.show(mAnimatables);

				mAnimatables.add(new TranslationAnimation("", cur,
						new SimpleVector(-40, 0, 0), null) {
					@Override
					public void onAnimateSuccess() {
						// object3ds[0].setVisibility(false);
						for (int i = 0; i < object3ds.length; i++) {
							object3ds[i].setVisibility(false);
							object3ds[0].clearTranslation();
						}

						ws.mState = 0;
						ws.mCurrentApp = App.NULL;

						if (runnable != null)
							runnable.run();

						super.onAnimateSuccess();
					}
				});

				if (cur.length == 1)
					mAnimatables.add(new DisplayAnimation(cur, "", true));

				return;
			}

		}

		ws.mState = 0;
		ws.mCurrentApp = App.NULL;

		if (runnable != null)
			runnable.run();
	}

	private void activateTilesGroup(PickGroup group) {
		SimpleVector trans = new SimpleVector();

		if (group.state == 0) {
			group.state = 1;

			if (group.animation == null) {
				Log.e(TAG, "trans 0 -> 1 with null");

				trans = cam.getPosition()
						.calcSub(group.group[0].getTransformedCenter())
						.normalize();
				group.animation = new TranslationAnimation("", group.group,
						new SimpleVector(trans), group);
				mAnimatables.add(group.animation);
			} else {
				Log.e(TAG, "trans 0 -> 1 with animation");

				group.animation.stop();

				trans = cam.getPosition()
						.calcSub(group.group[0].getTransformedCenter())
						.normalize();

				SimpleVector hasTrns = new SimpleVector();
				group.group[0].getTranslation(hasTrns);

				group.animation = new TranslationAnimation("", group.group,
						trans.calcSub(hasTrns), group);
				mAnimatables.add(group.animation);
			}

		} else {

		}
	}

	private void deactivateTilesGroup(PickGroup group) {
		if (group.state == 0) {

		} else {
			group.state = 0;

			if (group.animation == null) {
				Log.e(TAG, "trans 1 -> 0 with null");

				SimpleVector trns = new SimpleVector();
				group.group[0].getTranslation(trns);
				trns.x = -trns.x;
				trns.y = -trns.y;
				trns.z = -trns.z;
				group.animation = new TranslationAnimation("", group.group,
						trns, group);
				mAnimatables.add(group.animation);
			} else {
				Log.e(TAG, "trans 1 -> 0 with animation");

				group.animation.stop();
				SimpleVector trns = new SimpleVector();
				group.group[0].getTranslation(trns);
				trns.x = -trns.x;
				trns.y = -trns.y;
				trns.z = -trns.z;

				group.animation = new TranslationAnimation("", group.group,
						trns, group);
				mAnimatables.add(group.animation);
			}
		}
	}

	private void saveWorkspaceObject(String name, Object3D object3d) {

		if (name.startsWith("x_a")) {
			Log.e(TAG, "x_a");
			initAnimal(name, object3d);
			return;
		} else if (name.startsWith("x_b")) {
			Log.e(TAG, "x_b");
			initBoard(name, object3d);
			return;
		} else if (name.startsWith("x_i")) {
			Log.e(TAG, "x_i");
			initIcons(name, object3d);
			return;
		} else if (name.startsWith("x_l")) {
			Log.e(TAG, "x_l");
			initLists(name, object3d);
			return;
		} else if (name.startsWith("x_scr")) {
			Log.e(TAG, "x_scr");
			initScr(name, object3d);
			return;
		} else if (name.startsWith("x_p")) {
			Log.e(TAG, "x_p");
			initPeople(name, object3d);
			return;
		} else if (name.startsWith("x_f_")) {
			Log.e(TAG, "x_f");
			initFile(name, object3d);
			return;
		} else if (name.startsWith("x_c_works")) {
			object3d.setVisibility(false);
			mCamViewspots[4] = object3d;
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
		} else if (name.startsWith("e_drone")) {
			entrances[1] = object3d;
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

	private void initFile(String name, Object3D object3d) {
		object3d.setVisibility(false);
		// object3d.setAdditionalColor(new RGBColor(100, 100, 100));

		int end = -1;
		end = end == -1 ? name.indexOf("_Plane") : end;
		end = end == -1 ? name.indexOf("_Cube") : end;
		end = end == -1 ? name.indexOf("_Cylinder") : end;
		end = end == -1 ? name.indexOf("_OBJ") : end;

		String tname = name.substring(2, (end == -1 ? name.length() : end));

		Log.e(TAG, "nice: " + tname);

		if (tname.startsWith("f_b")) {
			clickableFiles.put(tname, object3d);
		} else {
			clickableDesks.put(tname, object3d);
		}
	}

	private void initIslands(String name, Object3D object3d) {
		if (name.startsWith("i_trea"))
			islands[3] = object3d;
		else if (name.startsWith("i_ship"))
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
		} else if (name.startsWith("x_p_s1lide")) {
			picScrs[0] = object3d;
			picScrs[0].setVisibility(false);
		} else if (name.startsWith("x_p_s2lide")) {
			picScrs[1] = object3d;
			picScrs[1].setVisibility(false);
		}
	}

	private void initScr(String name, Object3D object3d) {
		object3d.clearAdditionalColor();
		object3d.setShader(screenShaders[name.charAt(5) - '1']);
		object3d.calcTextureWrapSpherical();
		object3d.build();
		object3d.strip();
		screens[name.charAt(5) - '1'] = object3d;

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

	private void initBoard(String name, Object3D object3d) {

		TextureManager tm = TextureManager.getInstance();
		String tname = name.substring(2, name.indexOf("_Plane"));
		object3d.clearAdditionalColor();

		Log.e(TAG, "matching tname:" + tname);

		clickableBoards.put(tname, object3d);

		printTexture(tname, object3d);
	}

	private void initIcons(String name, Object3D object3d) {
		TextureManager tm = TextureManager.getInstance();
		String tname = name.substring(2, name.indexOf("_Plane"));
		object3d.clearAdditionalColor();

		Log.e(TAG, "matching tname:" + tname);

		clickableIcons.put(tname, object3d);

		printTexture(tname, object3d);
	}

	private void initLists(String name, Object3D object3d) {

		String tname = name.substring(2, name.indexOf("_Plane"));
		Log.e(TAG, "matching tname:" + tname);

		printTexture(tname, object3d);

		object3d.setVisibility(false);

		clickableLists.put(tname, object3d);
	}

	private void postInitBoard() {

		for (int i = 0; i < 4; i++) {
			mPickGroupBoards[i] = new PickGroup(2);
		}
		for (int i = 4; i < mPickGroupBoards.length; i++) {
			mPickGroupBoards[i] = new PickGroup(1);
		}

		Object3D tmp;
		tmp = clickableBoards.get("b_minecraft");
		mBoardTiles[0].setTile1(tmp, new SimpleVector(0.839, -1, 0));
		mPickGroupBoards[0].group[0] = tmp;

		tmp = clickableBoards.get("b_m2inecraft");
		mBoardTiles[0].setTile2(tmp, new SimpleVector(0.839, -1, 0));
		mPickGroupBoards[0].group[1] = tmp;

		tmp = clickableBoards.get("b_car");
		mBoardTiles[1].setTile1(tmp, new SimpleVector(0.839, -1, 0));
		mPickGroupBoards[1].group[0] = tmp;

		tmp = clickableBoards.get("b_c2ar");
		mBoardTiles[1].setTile2(tmp, new SimpleVector(0.839, -1, 0));
		mPickGroupBoards[1].group[1] = tmp;

		tmp = clickableBoards.get("b_video");
		mBoardTiles[2].setTile1(tmp, new SimpleVector(0.839, -1, 0));
		mPickGroupBoards[2].group[0] = tmp;

		tmp = clickableBoards.get("b_v2ideo");
		mBoardTiles[2].setTile2(tmp, new SimpleVector(0.839, -1, 0));
		mPickGroupBoards[2].group[1] = tmp;

		tmp = clickableBoards.get("b_pic");
		mBoardTiles[3].setTile1(tmp, new SimpleVector(1.732, -1, 0));
		mPickGroupBoards[3].group[0] = tmp;

		tmp = clickableBoards.get("b_p2ic");
		mBoardTiles[3].setTile2(tmp, new SimpleVector(1.732, -1, 0));
		mPickGroupBoards[3].group[1] = tmp;

		tmp = clickableBoards.get("b_skype");
		mPickGroupBoards[4].group[0] = tmp;
		tmp = clickableBoards.get("b_ie");
		mPickGroupBoards[5].group[0] = tmp;
		tmp = clickableBoards.get("b_file");
		mPickGroupBoards[6].group[0] = tmp;
		tmp = clickableBoards.get("b_word");
		mPickGroupBoards[7].group[0] = tmp;
		tmp = clickableBoards.get("b_excel");
		mPickGroupBoards[8].group[0] = tmp;
		tmp = clickableBoards.get("b_ppt");
		mPickGroupBoards[9].group[0] = tmp;
		tmp = clickableBoards.get("b_null");
		mPickGroupBoards[10].group[0] = tmp;
	}

	private void postInitFile() {
		for (int i = 0; i < mPickGroupFiles.length; i++) {
			mPickGroupFiles[i] = new PickGroup(1);
		}
		for (int i = 0; i < mPickGroupDesks.length; i++) {
			mPickGroupDesks[i] = new PickGroup(1);
		}

		Object3D tmp;
		tmp = clickableDesks.get("f_desk");
		mPickGroupDesks[0].group[0] = tmp;

		tmp = clickableDesks.get("f_trash");
		mPickGroupDesks[1].group[0] = tmp;

		tmp = clickableDesks.get("f_flower1");
		mPickGroupDesks[2].group[0] = tmp;

		tmp = clickableDesks.get("f_flower2");
		mPickGroupDesks[3].group[0] = tmp;

		tmp = clickableFiles.get("f_book1");
		mPickGroupFiles[0].group[0] = tmp;

		tmp = clickableFiles.get("f_book2");
		mPickGroupFiles[1].group[0] = tmp;

		tmp = clickableFiles.get("f_book3");
		mPickGroupFiles[2].group[0] = tmp;

		desks = clickableDesks.values().toArray(desks);
		files = clickableFiles.values().toArray(files);
		for (int i = 0; i < desks.length; i++) {
			desksfiles[i] = desks[i];
		}
		for (int i = 0; i < files.length; i++) {
			desksfiles[i + 4] = files[i];
		}

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

	private void postInitList() {

		for (int i = 0; i < 10; i++) {
			mPickGroupLists[i] = new PickGroup(2);
		}
		for (int i = 10; i < mPickGroupLists.length; i++) {
			mPickGroupLists[i] = new PickGroup(1);
		}

		for (int i = 0; i < mPicListTiles.length; i++) {
			mPicListTiles[i].setFrames(40);
		}

		for (int i = 0; i < mVideoListTiles.length; i++) {
			mVideoListTiles[i].setFrames(40);
		}

		Object3D tmp;
		tmp = clickableLists.get("l_l1");
		mPicListTiles[0].setTile1(tmp, new SimpleVector(1.729, 1, 0));
		mPickGroupLists[0].group[0] = tmp;
		tmp = clickableLists.get("l_l2");
		mPicListTiles[1].setTile1(tmp, new SimpleVector(1.729, 1, 0));
		mPickGroupLists[1].group[0] = tmp;
		tmp = clickableLists.get("l_l3");
		mPicListTiles[2].setTile1(tmp, new SimpleVector(1.729, 1, 0));
		mPickGroupLists[2].group[0] = tmp;
		tmp = clickableLists.get("l_l4");
		mPicListTiles[3].setTile1(tmp, new SimpleVector(3.732, 1, 0));
		mPickGroupLists[3].group[0] = tmp;
		tmp = clickableLists.get("l_l5");
		mPicListTiles[4].setTile1(tmp, new SimpleVector(3.732, 1, 0));
		mPickGroupLists[4].group[0] = tmp;
		tmp = clickableLists.get("l_l6");
		mPicListTiles[5].setTile1(tmp, new SimpleVector(3.732, 1, 0));
		mPickGroupLists[5].group[0] = tmp;
		tmp = clickableLists.get("l_l7");
		mPicListTiles[0].setTile2(tmp, new SimpleVector(1.729, 1, 0));
		mPickGroupLists[0].group[1] = tmp;
		tmp = clickableLists.get("l_l8");
		mPicListTiles[1].setTile2(tmp, new SimpleVector(1.729, 1, 0));
		mPickGroupLists[1].group[1] = tmp;
		tmp = clickableLists.get("l_l9");
		mPicListTiles[2].setTile2(tmp, new SimpleVector(1.729, 1, 0));
		mPickGroupLists[2].group[1] = tmp;
		tmp = clickableLists.get("l_la");
		mPicListTiles[3].setTile2(tmp, new SimpleVector(3.732, 1, 0));
		mPickGroupLists[3].group[1] = tmp;
		tmp = clickableLists.get("l_lb");
		mPicListTiles[4].setTile2(tmp, new SimpleVector(3.732, 1, 0));
		mPickGroupLists[4].group[1] = tmp;
		tmp = clickableLists.get("l_lc");
		mPicListTiles[5].setTile2(tmp, new SimpleVector(3.732, 1, 0));
		mPickGroupLists[5].group[1] = tmp;

		for (int i = 0; i < 8; i++) {
			tmp = clickableLists.get("l_m" + (i + 1));

			SimpleVector simpleVector;

			if (i % 2 == 0) {
				simpleVector = new SimpleVector(3.732, 1, 0);
			} else {
				simpleVector = new SimpleVector(0, 0, 1);
			}

			if (i / 4 == 0) {
				mVideoListTiles[i % 4].setTile1(tmp, simpleVector);
			} else {
				mVideoListTiles[i % 4].setTile2(tmp, simpleVector);
			}

			mPickGroupLists[6 + i % 4].group[(i / 4)] = tmp;
		}

		tmp = clickableLists.get("l_next");
		mPickGroupLists[10].group[0] = tmp;

		tmp = clickableLists.get("l_back");
		mPickGroupLists[11].group[0] = tmp;

		tmp = clickableLists.get("l_opt");
		mPickGroupLists[12].group[0] = tmp;
	}

	private void printTexture(String tname, Object3D object3d) {
		TextureManager tm = TextureManager.getInstance();

		if (tm.containsTexture(tname)) {
			object3d.setTexture(tname);
		} else {
			object3d.setTexture("dummy");
		}
		object3d.calcTextureWrapSpherical();
		object3d.strip();
		object3d.build();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGestureDetector = new GestureDetector(this, mGestureListener);
	}

	@Override
	public void onDrawEye(Eye eye) {

		fb.clear(back);
		world.renderScene(fb);
		if (NEO) {
			world.drawWireframe(fb, wire, 2, false);
		} else {
			world.draw(fb);
		}
		fb.display();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}

	public void drawText(String tex, String[] msg) {
		Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);

		canvas.drawColor(Color.TRANSPARENT);
		Paint p = new Paint();

		String fontType = "consolas";
		Typeface typeface = Typeface.create(fontType, Typeface.NORMAL);

		p.setAntiAlias(true);

		p.setColor(Color.WHITE);
		p.setTypeface(typeface);
		p.setTextSize(28);
		for (int i = 0; i < msg.length; i++) {
			canvas.drawText(msg[i], 0, 100 * i, p);
		}

		TextureManager tm = TextureManager.getInstance();
		Texture texture = new Texture(bitmap);
		if (tm.containsTexture(tex)) {
			tm.replaceTexture(tex, texture);
		} else {
			tm.addTexture(tex, texture);
		}
		bitmap.recycle();
	}

	private void flyWorkspace() {
		canCamRotate = false;
		SimpleVector ori = new SimpleVector();
		cam.getPosition(ori);

		final Animatable a3 = new CamTranslationAnimation("", ori.calcSub(cam
				.getPosition()), cam, forward, (float) Math.PI / 2) {

			public void onAnimateSuccess() {
				super.onAnimateSuccess();

				canCamRotate = true;

			}
		};

		final Animatable a2 = new CamAnimation(cam, ori, ori, 0, 2.5, 10, 3) {
			public void onAnimateSuccess() {
				// super.onAnimateSuccess();

				mAnimatables.add(a3);

			}
		};

		Animatable a1 = new CamTranslationAnimation("", new SimpleVector(0, 10,
				0), cam, screens[0].getTransformedCenter(), (float) Math.PI / 2) {
			public void onAnimateSuccess() {
				super.onAnimateSuccess();

				mAnimatables.add(a2);
			}
		};

		mAnimatables.add(a1);
	}

	private void flyIsland() {
		canCamRotate = false;
		SimpleVector ori = new SimpleVector();
		cam.getPosition(ori);

		SimpleVector volcano = islands[VOLCANO].getTransformedCenter();

		final Animatable a5 = new CamTranslationAnimation("",
				mCamViewspots[TREASURE].getTransformedCenter().calcSub(
						cam.getPosition()), cam,
				islands[VOLCANO].getTransformedCenter(), (float) Math.PI / 2) {
			@Override
			public void onAnimateSuccess() {
				canCamRotate = true;
				super.onAnimateSuccess();
			}
		};

		final Animatable a4 = new CamAnimation(cam, volcano, volcano,
				3 * Math.PI / 4, Math.PI / 4, 50, 10, false) {
			@Override
			public void onAnimateSuccess() {
				mAnimatables.add(a5);
			}
		};

		final Animatable a3 = new CamTranslationAnimation("", islands[VOLCANO]
				.getTransformedCenter()
				.calcAdd(
						new SimpleVector(50 / Math.sqrt(2), -50 / Math.sqrt(2),
								5)).calcSub(cam.getPosition()), cam, ori,
				(float) -Math.PI / 2) {

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
				0, 10, 0), cam, cam.getPosition(), (float) -Math.PI / 2) {
			public void onAnimateSuccess() {
				super.onAnimateSuccess();

				mAnimatables.add(a2);
			}
		};

		mAnimatables.add(a1);
	}

	private void flyDrone() {
		show3DToast("flyDrone");
	}

	@Override
	public void onCardboardTrigger() {
		show3DToast("onCardboardTrigger");

		actionFired[TOGGLE_FULLSCREEN] = true;
		super.onCardboardTrigger();
	}

	@Override
	public void onIDisplayConnected() {
		show3DToast("onIDisplayConnected");
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

}
