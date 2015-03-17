package seu.lab.matrix;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.opencv.core.Point;

import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.animation.DisplayAnimation;
import seu.lab.matrix.animation.LiveTileAnimation;
import seu.lab.matrix.animation.PickGroup;
import seu.lab.matrix.animation.ScaleAnimation;
import seu.lab.matrix.animation.SeqAnimation;
import seu.lab.matrix.animation.TranslationAnimation;

import android.R.integer;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection.ConnectionMode;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection.IDisplayConnectionCallback;
import com.idisplay.util.ServerItem;
import com.jbrush.ae.*;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.GLSLShader;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

public class SceneActivity extends Framework3DMatrixActivity {

	private static final String TAG = "SceneActivity";

	protected SimpleVector forward = new SimpleVector(-1, 0, 0);
	protected SimpleVector up = new SimpleVector(0, -1, 0);

	protected Object3D[] mCamViewspots = null;
	protected int mCamViewIndex = 0;

	protected Object3D[] mObjects = null;

	private static float BALL_DISTANCE = 2f;

	private int mFrameCounter = 0;

	private Camera cam;

	private Object3D ball1 = null;
	private Object3D ball2 = null;
	List<Point> mPoints = new LinkedList<Point>();

	List<Animatable> mAnimatables = new LinkedList<Animatable>();

	private LiveTileAnimation[] mBoardTiles = new LiveTileAnimation[] {
			new LiveTileAnimation("minecraft"), new LiveTileAnimation(""),
			new LiveTileAnimation(""), new LiveTileAnimation(""), };

	private LiveTileAnimation[] mListTiles = new LiveTileAnimation[] {
			new LiveTileAnimation("",false,null), new LiveTileAnimation("",false,null),
			new LiveTileAnimation("",false,null), new LiveTileAnimation("",false,null),
			new LiveTileAnimation("",false,null), new LiveTileAnimation("",false,null), };

	private Map<String, Object3D> clickableBoards = new HashMap<String, Object3D>();
	private Map<String, Object3D> clickableIcons = new HashMap<String, Object3D>();
	private Map<String, Object3D> clickableLists = new HashMap<String, Object3D>();

	PickGroup[] mPickGroupBoards = new PickGroup[11];
	PickGroup[] mPickGroupLists = new PickGroup[6 + 2];

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

		}

		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {

			Log.d(TAG, "x: " + distanceX + ", y: " + distanceY);

			double x = mPoints.get(0).x + -distanceX * 0.003;
			double y = mPoints.get(0).y + distanceY * 0.003;

			x = x > 1 ? 1 : x;
			x = x < -1 ? -1 : x;
			y = y > 1 ? 1 : y;
			y = y < -1 ? -1 : y;

			mPoints.get(0).x = x;
			mPoints.get(0).y = y;

			return false;
		}

	};

	private boolean test = true;

	private Object3D mSkype;

	private Object3D mList;

	private boolean isListShown = true;

	@Override
	public void onSurfaceChanged(int w, int h) {
		// TODO Auto-generated method stub
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		screens = new Object3D[3];
		islands = new Object3D[4];

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

		mPoints.add(new Point(0, 0));

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

			for (int i = 0; i < mBoardTiles.length; i++) {
				mAnimatables.add(mBoardTiles[i]);
			}

//			for (int i = 0; i < mListTiles.length; i++) {
//				mAnimatables.add(mListTiles[i]);
//			}

			// animatables.add(new ScaleAnimation(new Object3D[]{
			// mTiles[0].tile1, mTiles[0].tile2
			// }, "tile scale", 2f));

			// animatables.add(new DisplayAnimation(new Object3D[]{
			// mTiles[1].tile1, mTiles[1].tile2
			// }, "tile scale", true));

			mSkype = clickableBoards.get("b_skype");

			mList = clickableLists.get("l_l2");

		} else {

			// pick obj

			if (isLookingAt(cam, ball1, mSkype.getTransformedCenter()) > 0.75) {
//				Log.e(TAG, "pick skype");

				pickBoard();

			} else if (isLookingAt(cam, ball1,
					screens[0].getTransformedCenter()) > 0.8) {
				Log.e(TAG, "pick scr");

				pickScr();

			} else if (isListShown
					&& isLookingAt(cam, ball1, mList.getTransformedCenter()) > 0.8) {
				Log.e(TAG, "pick list");

				pickList();
			}

			// animations
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

		if (mPoints.size() > 0) {
			ball1.setRotationPivot(originInballView);
			ball1.rotateAxis(cam.getUpVector(),
					(float) (0.5f * mPoints.get(0).x));
			ball1.rotateAxis(cam.getSideVector(),
					(float) (-0.5f * mPoints.get(0).y));
		}

		ball2.clearTranslation();
		ball2.clearRotation();

		ball2.translate(camDir);
		ball2.setRotationPivot(originInballView);
		ball2.rotateAxis(cam.getUpVector(), 0.5f);
		ball2.rotateAxis(cam.getSideVector(), -0.5f);

	}

	private void pickList() {
		if(!isDoubleTapped)return;
		
		isDoubleTapped = false;
		
		PickGroup group = null;
		Object3D object3d = null;
		SimpleVector trans = new SimpleVector();
		for (int i = 6; i < mPickGroupLists.length; i++) {
			group = mPickGroupLists[i];
			object3d = group.group[0];

			if (isLookingAt(cam, ball1, object3d.getTransformedCenter()) > 0.99) {
				if(object3d.getName().startsWith("x_l_next")
					|| object3d.getName().startsWith("x_l_back")){
					
					for (int i1 = 0; i1 < mListTiles.length; i1++) {
						mListTiles[i1].reset();
//						mAnimatables.add(mListTiles[i1]);
					}
					
					mAnimatables.add(new SeqAnimation(mAnimatables, mListTiles));
					
				}
			}
		}
	}

	private void pickScr() {

	}

	private void pickBoard() {
		PickGroup group = null;
		SimpleVector trans = new SimpleVector();
		for (int i = 0; i < mPickGroupBoards.length; i++) {
			group = mPickGroupBoards[i];

			if (isLookingAt(cam, ball1, group.group[0].getTransformedCenter()) > 0.995) {
				
				Log.e(TAG, "trans:"+ group.group[0].getTranslation());
				
				if (group.state == 0) {
					group.state = 1;
					
					if (group.animation == null) {
						Log.e(TAG, "trans 0 -> 1 with null");

						trans = cam.getPosition()
								.calcSub(group.group[0].getTransformedCenter())
								.normalize();
						group.animation = new TranslationAnimation("",
								group.group, new SimpleVector(trans), group);
						mAnimatables.add(group.animation);
					} else {
						Log.e(TAG, "trans 0 -> 1 with animation");

						
						group.animation.stop();

						trans = cam.getPosition()
								.calcSub(group.group[0].getTransformedCenter())
								.normalize();

						SimpleVector hasTrns = new SimpleVector();
						group.group[0].getTranslation(hasTrns);

						group.animation = new TranslationAnimation("",
								group.group, trans.calcSub(hasTrns), group);
						mAnimatables.add(group.animation);
					}

				} else {

				}
			} else {
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
						group.animation = new TranslationAnimation("",
								group.group, trns, group);
						mAnimatables.add(group.animation);
					} else {
						Log.e(TAG, "trans 1 -> 0 with animation");

						group.animation.stop();
						SimpleVector trns = new SimpleVector();
						group.group[0].getTranslation(trns);
						trns.x = -trns.x;
						trns.y = -trns.y;
						trns.z = -trns.z;

						group.animation = new TranslationAnimation("",
								group.group, trns, group);
						mAnimatables.add(group.animation);
					}
				}
			}
		}
	}

	private void initIslands(String name, Object3D object3d) {
		if (name.contains("i_trea"))
			islands[3] = object3d;
		else if (name.contains("i_ship"))
			islands[2] = object3d;
		else if (name.contains("i_volcano"))
			islands[1] = object3d;
		else if (name.contains("i_green"))
			islands[0] = object3d;
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
			object3d.setVisibility(false);
			return;
		}
	}

	private void initScr(String name, Object3D object3d) {
		object3d.clearAdditionalColor();
		object3d.setShader(screenShaders[name.charAt(5) - '1']);
		object3d.calcTextureWrapSpherical();
		object3d.build();
		object3d.strip();
		screens[name.charAt(5) - '1'] = object3d;

//		object3d.setVisibility(false);
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
		if (name.contains("x_")) {
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

		if (tm.containsTexture(tname)) {
			object3d.setTexture(tname);
		} else {
			object3d.setTexture("dummy");
		}
		object3d.calcTextureWrapSpherical();
		object3d.strip();
		object3d.build();
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

	private void initIcons(String name, Object3D object3d) {
		TextureManager tm = TextureManager.getInstance();
		String tname = name.substring(2, name.indexOf("_Plane"));
		object3d.clearAdditionalColor();

		Log.e(TAG, "matching tname:" + tname);

		clickableIcons.put(tname, object3d);

		if (tm.containsTexture(tname)) {
			object3d.setTexture(tname);
		} else {
			object3d.setTexture("dummy");
		}
		object3d.calcTextureWrapSpherical();
		object3d.strip();
		object3d.build();
	}

	private void initLists(String name, Object3D object3d) {
		TextureManager tm = TextureManager.getInstance();

		String tname = name.substring(2, name.indexOf("_Plane"));
		Log.e(TAG, "matching tname:" + tname);

		if (tm.containsTexture(tname)) {
			object3d.setTexture(tname);
		} else {
			object3d.setTexture("dummy");
		}
		object3d.calcTextureWrapSpherical();
		object3d.strip();
		object3d.build();

		clickableLists.put(tname, object3d);
	}

	private void postInitList() {

		for (int i = 0; i < 6; i++) {
			mPickGroupLists[i] = new PickGroup(false);
		}
		for (int i = 6; i < mPickGroupLists.length; i++) {
			mPickGroupLists[i] = new PickGroup(true);
		}

		for (int i = 0; i < mListTiles.length; i++) {
			mListTiles[i].setFrames(40);
		}
		
		Object3D tmp;
		tmp = clickableLists.get("l_l1");
		mListTiles[0].setTile1(tmp, new SimpleVector(1.729, 1, 0));
		mPickGroupLists[0].group[0] = tmp;
		tmp = clickableLists.get("l_l2");
		mListTiles[1].setTile1(tmp, new SimpleVector(1.729, 1, 0));
		mPickGroupLists[1].group[0] = tmp;
		tmp = clickableLists.get("l_l3");
		mListTiles[2].setTile1(tmp, new SimpleVector(1.729, 1, 0));
		mPickGroupLists[2].group[0] = tmp;
		tmp = clickableLists.get("l_l4");
		mListTiles[3].setTile1(tmp, new SimpleVector(3.732, 1, 0));
		mPickGroupLists[3].group[0] = tmp;
		tmp = clickableLists.get("l_l5");
		mListTiles[4].setTile1(tmp, new SimpleVector(3.732, 1, 0));
		mPickGroupLists[4].group[0] = tmp;
		tmp = clickableLists.get("l_l6");
		mListTiles[5].setTile1(tmp, new SimpleVector(3.732, 1, 0));
		mPickGroupLists[5].group[0] = tmp;
		tmp = clickableLists.get("l_l7");
		mListTiles[0].setTile2(tmp, new SimpleVector(1.729, 1, 0));
		mPickGroupLists[0].group[1] = tmp;
		tmp = clickableLists.get("l_l8");
		mListTiles[1].setTile2(tmp, new SimpleVector(1.729, 1, 0));
		mPickGroupLists[1].group[1] = tmp;
		tmp = clickableLists.get("l_l9");
		mListTiles[2].setTile2(tmp, new SimpleVector(1.729, 1, 0));
		mPickGroupLists[2].group[1] = tmp;
		tmp = clickableLists.get("l_la");
		mListTiles[3].setTile2(tmp, new SimpleVector(3.732, 1, 0));
		mPickGroupLists[3].group[1] = tmp;
		tmp = clickableLists.get("l_lb");
		mListTiles[4].setTile2(tmp, new SimpleVector(3.732, 1, 0));
		mPickGroupLists[4].group[1] = tmp;
		tmp = clickableLists.get("l_lc");
		mListTiles[5].setTile2(tmp, new SimpleVector(3.732, 1, 0));
		mPickGroupLists[5].group[1] = tmp;
		
		tmp = clickableLists.get("l_next");
		mPickGroupLists[6].group[0] = tmp;

		tmp = clickableLists.get("l_back");
		mPickGroupLists[7].group[0] = tmp;

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
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
			if (isLookingAt(cam, island_center[i]) > 0.99) {
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
}
