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
import seu.lab.matrix.animation.DisplayAnimation;
import seu.lab.matrix.animation.LiveTileAnimation;
import seu.lab.matrix.animation.PeopleAnimation;
import seu.lab.matrix.animation.PickGroup;
import seu.lab.matrix.animation.ScaleAnimation;
import seu.lab.matrix.animation.SeqAnimation;
import seu.lab.matrix.animation.TranslationAnimation;
import seu.lab.matrix.red.RemoteManager.OnRemoteChangeListener;
import android.R.integer;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

import com.arwave.skywriter.objects.Rectangle;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.threed.jpct.Camera;
import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
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

	enum App {
		NULL(0), MINECRAFT(1), CAR(2), VIDEO(3), PIC(4), SKYPE(5), IE(6), FILE(7), WORD(8), EXCEL(9), PPT(10), CAM(11);
		
		private int aa;
		
	    private static Map<Integer, App> map = new HashMap<Integer, App>();

	    static {
	        for (App legEnum : App.values()) {
	            map.put(legEnum.aa, legEnum);
	        }
	    }

	    private App(final int a) { aa = a; }

	    public static App valueOf(int a) {
	        return map.get(a);
	    }
	}

	protected SimpleVector forward = new SimpleVector(-1, 0, 0);
	protected SimpleVector up = new SimpleVector(0, -1, 0);

	protected Object3D[] mCamViewspots = null;
	protected int mCamViewIndex = 0;

	protected Object3D[] mObjects = null;

	private static float BALL_DISTANCE = 2f;

	private int mFrameCounter = 0;

	private Camera cam;

	protected FrameBuffer fb = null;
	protected SkyBox sky;
	protected World world = null;
	protected Light sun = null;
	protected Light spot = null;
	protected Object3D[] screens = null;
	protected Object3D[] islands = null;
	protected Object3D notice = null;

	protected PeopleAnimation peopleAnimation;

	protected RGBColor back = new RGBColor(50, 50, 100);
	protected RGBColor wire = new RGBColor(100, 100, 100);

	protected Bitmap fontBitmap;

	protected int[] buffer;
	protected boolean canCamRotate = true;

	private Object3D ball1 = null;
	private Object3D ball2 = null;

	List<Animatable> mAnimatables = new LinkedList<Animatable>();

	private boolean test = true;

	private Object3D mSkype;

	private Object3D mList;

	private Object3D mPeoplePositionDummy;
	private Object3D[] picScrs;

	private Workspace[] workspaces;
	private Workspace ws;

	private int mWsIdx = 0;

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

	PickGroup[] mPickGroupBoards = new PickGroup[11];
	PickGroup[] mPickGroupLists = new PickGroup[6 + 4 + 2];

	PickGroup[] mPickGroupIcons = new PickGroup[2];

	GestureDetector mGestureDetector = null;

	boolean isDoubleTapped = false;

	SimpleOnGestureListener mGestureListener = new SimpleOnGestureListener() {

		public boolean onDoubleTap(MotionEvent e) {
			isDoubleTapped = true;
			return super.onDoubleTap(e);
		}

		public boolean onSingleTapConfirmed(MotionEvent e) {
			return super.onSingleTapConfirmed(e);
		}

		public void onLongPress(MotionEvent e) {

			if (ws.mCurrentApp == App.PIC) {
				if (e.getRawX() < 480) {
					slidePic(false);
				} else if (e.getRawX() > 1920 - 480) {
					slidePic(true);
				} else {
					if (e.getRawY() < 540) {
						scalePic(true);
					} else {
						scalePic(false);
					}
				}
			} else {
				int workspace = mWsIdx + 1 > 2 ? 0 : mWsIdx + 1;
				switchWorkspace(workspace);
			}
		}

		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {

			Log.d(TAG, "x: " + distanceX + ", y: " + distanceY);

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

	};

	OnRemoteChangeListener remoteChangeListener = new OnRemoteChangeListener() {
		@Override
		public void onMove(Point p) {
			Log.e(TAG, "remote : onMove x:" + p.x + " y: " + p.y);
			point = p;
		}

		@Override
		public void onClick() {
			Log.e(TAG, "remote : onClick");
			isDoubleTapped = true;
			if (test) {
				test = false;
				ball1.setAdditionalColor(new RGBColor(0, 100, 0));
			} else {
				test = true;
				ball1.setAdditionalColor(new RGBColor(0, 0, 100));
			}
		}

		@Override
		public void onPress() {
			Log.e(TAG, "remote : onPress");

			ball1.setAdditionalColor(new RGBColor(0, 100, 0));

		}

		@Override
		public void onRaise() {
			Log.e(TAG, "remote : onRaise");

			ball1.setAdditionalColor(new RGBColor(0, 0, 100));

		}
	};

	IGestureListener gestureListener = new IGestureListener() {

		@Override
		public void onGesture(final GestureEvent event) {
			if (!event.isConclusion)
				return;

			mHandler.post(new Runnable() {

				@Override
				public void run() {
					mOverlayView.show3DToast(event.result);
				}
			});

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

		world.getCamera().setPosition(0, 0, 0);

		try {
			// mObjects = Loader.load3DS(getAssets().open("tex.3ds"), 1);

			mObjects = Loader.loadOBJ(getAssets().open("workspace.obj"),
					getAssets().open("workspace.mtl"), 1);

		} catch (IOException e) {
			e.printStackTrace();
		}

		screens = new Object3D[3];
		islands = new Object3D[4];
		workspaces = new Workspace[3];
		for (int i = 0; i < workspaces.length; i++) {
			workspaces[i] = new Workspace();
		}
		ws = workspaces[mWsIdx];

		peopleAnimation = new PeopleAnimation();
		picScrs = new Object3D[2];

		world.addObjects(mObjects);
		world.setAmbientLight(120, 120, 120);

		sun = new Light(world);
		sun.setIntensity(250, 250, 250);

		spot = new Light(world);
		spot.setIntensity(10, 10, 10);

		ball1 = Primitives.getSphere(0.05f);
		ball1.translate(0, 0, -2);
		ball1.calcTextureWrapSpherical();
		ball1.setAdditionalColor(new RGBColor(100, 0, 0));
		ball1.strip();
		ball1.build();
		world.addObject(ball1);

		ball2 = Primitives.getSphere(0.05f);
		ball2.translate(0, 0, -2);
		ball2.calcTextureWrapSpherical();
		ball2.setAdditionalColor(new RGBColor(0, 100, 0));
		ball2.strip();
		ball2.build();
		world.addObject(ball2);

		// notice = Primitives.getPlane(1, 2);
		// notice.rotateY(4.71f);
		// notice.translate(-5, 0, 0);
		// notice.setTexture("dummy");
		// notice.build();
		// notice.strip();
		// world.addObject(notice);

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
			cam = world.getCamera();
		} else if (mFrameCounter == 1) {
			mFrameCounter++;

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
		cam.setOrientation(forward, up);

		if (canCamRotate) {
			cam.rotateZ(3.1415926f / 2);

			cam.rotateY(mAngles[1]);
			cam.rotateZ(-mAngles[2]);
			cam.rotateX(mAngles[0]);

		}

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

		ball1.translate(camDir);

		ball1.setRotationPivot(originInballView);
		ball1.rotateAxis(cam.getUpVector(), (float) (0.5f * point.x));
		ball1.rotateAxis(cam.getSideVector(), (float) (0.5f * point.y));

		ball2.clearTranslation();
		ball2.clearRotation();

		ball2.translate(camDir);
		ball2.setRotationPivot(originInballView);
		ball2.rotateAxis(cam.getUpVector(), 0.5f);
		ball2.rotateAxis(cam.getSideVector(), -0.5f);

	}

	private void sceneInit() {
		mCamViewspots = new Object3D[5];

		String name = null;
		for (int i = 0; i < mObjects.length; i++) {
			name = mObjects[i].getName();
			Log.e(TAG, "name: " + name);
			saveObject(name, mObjects[i]);
		}
		for (int i = 0; i < mObjects.length; i++) {
			name = mObjects[i].getName();
			getScreen(name, mObjects[i]);
		}

		postInitBoard();
		postInitList();
		postInitScr();

		for (int i = 0; i < mBoardTiles.length; i++) {
			mAnimatables.add(mBoardTiles[i]);
		}

		// for (int i = 0; i < mListTiles.length; i++) {
		// mAnimatables.add(mListTiles[i]);
		// }

		// animatables.add(new ScaleAnimation(new Object3D[]{
		// mTiles[0].tile1, mTiles[0].tile2
		// }, "tile scale", 2f));

		// animatables.add(new DisplayAnimation(new Object3D[]{
		// mTiles[1].tile1, mTiles[1].tile2
		// }, "tile scale", true));

		mSkype = clickableBoards.get("b_skype");

		mList = clickableLists.get("l_l2");

		peopleAnimation.init(getAssets(), world, mCamViewspots[4],
				mPeoplePositionDummy);

		// mAnimatables.add(peopleAnimation);

		// startIDisplay(currentMode);
		
		switchWorkspace(0);

		peopleAnimation.show(mAnimatables);

	}

	private void sceneLoop() {
		// pick obj
		if (SceneHelper.isLookingAt(cam, ball1, mSkype.getTransformedCenter()) > 0.75) {

			pickBoard();

		} else if (SceneHelper.isLookingAt(cam, ball1, forward) > 0.8) {

			pickScr();

		} else if ((ws.mCurrentApp == App.PIC || ws.mCurrentApp == App.VIDEO)
				&& SceneHelper.isLookingAt(cam, ball1,
						mList.getTransformedCenter()) > 0.8) {

			pickList();
		}

		// animation driver
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

	private void switchWorkspace(int idx) {
		
		ws.isScrShown = screens[mWsIdx].getVisibility();
		
		screens[mWsIdx].setVisibility(false);		
		picScrs[0].setVisibility(false);
		picScrs[1].setVisibility(false);
		
		hideList(0, 12);

		// closed =================
		ws = workspaces[idx];

		// restore scr visibility

		screens[idx].setVisibility(ws.isScrShown);

		// TODO restore animation

		// restore pic && mov index
		if(ws.mPicPageIdx != workspaces[mWsIdx].mPicPageIdx){
			flipPicList();
		}
		
		if(ws.mVideoPageIdx != workspaces[mWsIdx].mVideoPageIdx){
			flipVideoList();
		}
		
		mWsIdx = idx;
		peopleAnimation.setWorkspace(mWsIdx);
				
		// restore app settings
		switch (ws.mCurrentApp) {
		case PIC :
			showList(0, 6);
			showList(10, 12);
			break;
		case VIDEO :
			showList(6, 12);
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

					if (isDoubleTapped) {
						isDoubleTapped = false;
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

					if (isDoubleTapped) {
						isDoubleTapped = false;
						openVideo(i + VIDEO_COUNT_PER_PAGE * ws.mVideoPageIdx + 1);
					}
				} else {
					deactivateTilesGroup(group);
				}
			}
			break;
		default:
			break;
		}

		if (!isDoubleTapped)
			return;

		isDoubleTapped = false;

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

		mAnimatables.add(new SeqAnimation(mAnimatables,
				mPicListTiles));

		ws.mPicPageIdx = ws.mPicPageIdx + 1 % 2;
	}

	private void flipVideoList() {
		
		for (int i1 = 0; i1 < mVideoListTiles.length; i1++) {
			mVideoListTiles[i1].reset();
			// mAnimatables.add(mListTiles[i1]);
		}

		mAnimatables.add(new SeqAnimation(mAnimatables,
				mVideoListTiles));

		ws.mVideoPageIdx = ws.mVideoPageIdx + 1 % 2;		
	}

	private void pickBoard() {
		PickGroup group;
		for (int i = 0; i < mPickGroupBoards.length; i++) {
			group = mPickGroupBoards[i];
			if (SceneHelper.isLookingAt(cam, ball1,
					group.group[0].getTransformedCenter()) > 0.995) {
				activateTilesGroup(group);

				if (isDoubleTapped) {
					isDoubleTapped = false;

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

				if (i == 0 && isDoubleTapped) {
					isDoubleTapped = false;

					closeCurrentApp(null);
				}

			} else {
				deactivateTilesGroup(group);
			}
		}
	}

	private void openVideo(int i) {

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

			// TODO open app

			App app = App.valueOf(idx+1);
			
			switch (app) {
			case PIC:
				new Thread() {
					public void run() {
						try {
							sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						Log.e(TAG, "thread run");

						hideList(0, 12);
						showList(0, 6);
						showList(10, 12);
						
						peopleAnimation.fadeout(mAnimatables);
						openPic(1);

						ws.mState = 2;
					}
				}.start();
				break;

			case VIDEO:
				new Thread() {
					public void run() {
						try {
							sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						Log.e(TAG, "thread run");

						hideList(0, 12);
						showList(6, 12);
						
						peopleAnimation.fadeout(mAnimatables);
						screens[mWsIdx].translate(-5, 0, 0);

						mAnimatables.add(new TranslationAnimation("",
								new Object3D[] { screens[mWsIdx] },
								new SimpleVector(5, 0, 0), null));
						mAnimatables.add(new DisplayAnimation(
								new Object3D[] { screens[mWsIdx] }, "", false));
						
						ws.mState = 2;
					}
				}.start();

				break;
			default:
				new Thread() {
					public void run() {
						try {
							sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						Log.e(TAG, "thread run");

						peopleAnimation.fadeout(mAnimatables);

						screens[mWsIdx].translate(-5, 0, 0);

						mAnimatables.add(new TranslationAnimation("",
								new Object3D[] { screens[mWsIdx] },
								new SimpleVector(5, 0, 0), null));
						mAnimatables.add(new DisplayAnimation(
								new Object3D[] { screens[mWsIdx] }, "", false));

						ws.mState = 2;
					}
				}.start();
				break;
			}
			
			ws.mCurrentApp = app;
		}
	}

	private void closeCurrentApp(final Runnable runnable) {
		if (ws.mState != 0) {
			// close the pic scr or idisplay scr

			Object3D cur;

			if (ws.mCurrentApp == App.PIC) {
				cur = picScrs[0].getVisibility() ? picScrs[0] : null;
				cur = picScrs[1].getVisibility() ? picScrs[1] : cur;
			} else {
				cur = screens[mWsIdx].getVisibility() ? screens[mWsIdx] : null;
			}

			if (cur != null) {

				hideList(0, 12);
				
				peopleAnimation.show(mAnimatables);

				mAnimatables.add(new TranslationAnimation("",
						new Object3D[] { cur }, new SimpleVector(-40, 0, 0),
						null) {
					@Override
					public void onAnimateSuccess() {
						// object3ds[0].setVisibility(false);
						object3ds[0].clearTranslation();

						ws.mState = 0;
						ws.mCurrentApp = App.NULL;

						if (runnable != null)
							runnable.run();

						super.onAnimateSuccess();
					}
				});

				mAnimatables.add(new DisplayAnimation(new Object3D[] { cur },
						"", true));

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

	private void saveObject(String name, Object3D object3d) {

		if (name.startsWith("c_")) {
			initCamera(name, object3d);
		} else if (name.startsWith("x_c_works")) {
			object3d.setVisibility(false);
			mCamViewspots[4] = object3d;
			return;
		} else if (name.startsWith("weather")) {
			SimpleVector sv = new SimpleVector(object3d.getTransformedCenter());
			sv.y -= 10;
			sun.setPosition(sv);
			return;
		} else if (name.startsWith("i_")) {
			Log.e(TAG, "i_");
			initIslands(name, object3d);
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
			object3d.setVisibility(false);
			return;
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
		if (name.startsWith("c_green")) {
			object3d.setVisibility(false);
			mCamViewspots[0] = object3d;
			return;
		} else if (name.startsWith("c_ship")) {
			object3d.setVisibility(false);
			mCamViewspots[2] = object3d;
			return;
		} else if (name.startsWith("c_volcano")) {
			object3d.setVisibility(false);
			mCamViewspots[1] = object3d;
			return;
		} else if (name.startsWith("c_treasure")) {
			object3d.setVisibility(false);
			mCamViewspots[3] = object3d;
			return;
		}
	}

	private void getScreen(String name, Object3D object3d) {
		if (name.startsWith("x_")) {
			Log.e(TAG, "getScreen: " + name);
			if (mCamViewspots[4] != object3d)
				mCamViewspots[4].addChild(object3d);
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

		// if (tname.startsWith("l_l") || tname.startsWith("l_m")) {
		object3d.setVisibility(false);
		// }

		clickableLists.put(tname, object3d);
	}

	private void postInitBoard() {

		for (int i = 0; i < 4; i++) {
			mPickGroupBoards[i] = new PickGroup(false);
		}
		for (int i = 4; i < mPickGroupBoards.length; i++) {
			mPickGroupBoards[i] = new PickGroup(true);
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

	private void postInitScr() {
		for (int i = 0; i < 2; i++) {
			mPickGroupIcons[i] = new PickGroup(true);
		}
		Object3D tmp;
		tmp = clickableIcons.get("i_close");
		mPickGroupIcons[0].group[0] = tmp;

		tmp = clickableIcons.get("i_fullscreen");
		mPickGroupIcons[1].group[0] = tmp;
	}

	private void postInitList() {

		for (int i = 0; i < 10; i++) {
			mPickGroupLists[i] = new PickGroup(false);
		}
		for (int i = 10; i < mPickGroupLists.length; i++) {
			mPickGroupLists[i] = new PickGroup(true);
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
		fillTexturesWithEye(buffer, eye);

		screenShaders[0].setUniform("videoFrame", 4);
		screenShaders[0].setUniform("videoFrame2", 5);
		screenShaders[0].setUniform("videoFrame3", 6);

		screenShaders[1].setUniform("videoFrame", 7);
		screenShaders[1].setUniform("videoFrame2", 8);
		screenShaders[1].setUniform("videoFrame3", 9);

		screenShaders[2].setUniform("videoFrame", 10);
		screenShaders[2].setUniform("videoFrame2", 11);
		screenShaders[2].setUniform("videoFrame3", 12);

		fb.clear(back);
		world.renderScene(fb);
		world.draw(fb);
		fb.display();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (true)
			return mGestureDetector.onTouchEvent(event);

		if (!(event.getAction() == MotionEvent.ACTION_UP))
			return false;

		if (mCamViewspots == null)
			return false;

		SimpleVector[] island_center = new SimpleVector[4];
		for (int i = 0; i < island_center.length; i++) {
			island_center[i] = new SimpleVector();
			islands[i].getTransformedCenter(island_center[i]);
		}

		for (int i = 0; i < 3; i++) {
			if (SceneHelper.isLookingAt(cam, island_center[i]) > 0.99) {
				cam.setPosition(mCamViewspots[i].getTransformedCenter());
				SimpleVector center_screen = mCamViewspots[4]
						.getTransformedCenter();
				SimpleVector target = island_center[i];
				target.z += 1;
				SimpleVector distance = new SimpleVector(target.x
						- center_screen.x, target.y - center_screen.y, target.z
						- center_screen.z);
				mCamViewspots[4].translate(distance);
				spot.setPosition(mCamViewspots[i].getTransformedCenter());
			}
		}

		return false;
	}

	public void initFontBitmap() {
		String font = "words to test";
		fontBitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(fontBitmap);

		canvas.drawColor(Color.TRANSPARENT);
		Paint p = new Paint();

		String fontType = "consolas";
		Typeface typeface = Typeface.create(fontType, Typeface.BOLD);

		p.setAntiAlias(true);

		p.setColor(Color.RED);
		p.setTypeface(typeface);
		p.setTextSize(28);
		canvas.drawText(font, 0, 100, p);
	}

	@Override
	public void onCardboardTrigger() {
		mOverlayView.show3DToast("onCardboardTrigger");
		super.onCardboardTrigger();
	}

	@Override
	public void onIDisplayConnected() {

		mHandler.post(new Runnable() {

			@Override
			public void run() {
				mOverlayView.show3DToast("onIDisplayConnected");
			}
		});

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
