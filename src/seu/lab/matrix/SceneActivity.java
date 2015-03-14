package seu.lab.matrix;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.opencv.core.Point;

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

	private static float ballDistance = 2f;

	private int frameCounter = 0;

	private Camera cam;

	private Object3D ball1 = null;
	private Object3D ball2 = null;
	List<Point> points = new LinkedList<Point>();

	List<Animatable> animatables = new LinkedList<Animatable>();

	private LiveTile[] mTiles = new LiveTile[]{
		new LiveTile(), new LiveTile(),
		new LiveTile(), new LiveTile()
	};
	
	GestureDetector gestureDetector = null;

	SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
		public boolean onDoubleTap(MotionEvent e) {
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

			double x = points.get(0).x + -distanceX * 0.003;
			double y = points.get(0).y + distanceY * 0.003;

			x = x > 1 ? 1 : x;
			x = x < -1 ? -1 : x;
			y = y > 1 ? 1 : y;
			y = y < -1 ? -1 : y;

			points.get(0).x = x;
			points.get(0).y = y;
			
			return false;
		}

	};

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

		points.add(new Point(0, 0));

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

		if (frameCounter == 0) {
			frameCounter++;

			cam = world.getCamera();

		} else if (frameCounter == 1) {
			frameCounter++;

			mCamViewspots = new Object3D[5];
			
			String name = null;
			for (int i = 0; i < mObjects.length; i++) {
				name = mObjects[i].getName();
				Log.e(TAG, "name: " + name);
				saveObject(name, mObjects[i]);
				getIslands(name, mObjects[i]);
			}
			for (int i = 0; i < mObjects.length; i++) {
				name = mObjects[i].getName();
				getScreen(name, mObjects[i]);
			}
			
			for (int i = 0; i < mTiles.length; i++) {
				animatables.add(mTiles[i]);
			}
		}else {
			// animations
			for (Animatable a : animatables) {
				if(a.isOver()){
					animatables.remove(a);
				}else {
					a.animate();
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

		camDir.x = camDir.x * ballDistance;
		camDir.y = camDir.y * ballDistance;
		camDir.z = camDir.z * ballDistance;

		SimpleVector originInballView = new SimpleVector(camDir);
		originInballView.x = -originInballView.x;
		originInballView.y = -originInballView.y;
		originInballView.z = -originInballView.z;

		ball1.clearTranslation();
		ball1.clearRotation();

		ball1.translate(camDir);

		if (points.size() > 0) {
			ball1.setRotationPivot(originInballView);
			ball1.rotateAxis(cam.getUpVector(),
					(float) (0.5f * points.get(0).x));
			ball1.rotateAxis(cam.getSideVector(),
					(float) (-0.5f * points.get(0).y));
		}

		ball2.clearTranslation();
		ball2.clearRotation();

		ball2.translate(camDir);
		ball2.setRotationPivot(originInballView);
		ball2.rotateAxis(cam.getUpVector(), 0.5f);
		ball2.rotateAxis(cam.getSideVector(), -0.5f);

	}

	private void getIslands(String name, Object3D object3d) {
		if (name.contains("i_trea"))
			islands[3] = object3d;
		else if (name.contains("i_ship"))
			islands[2] = object3d;
		else if (name.contains("i_volcano"))
			islands[1] = object3d;
		else if (name.contains("i_green"))
			islands[0] = object3d;
	}

	private void getScreen(String name, Object3D object3d) {
		if (name.contains("x_")) {
			Log.e(TAG, "getScreen: " + name);
			if(mCamViewspots[4] != object3d)
				mCamViewspots[4].addChild(object3d);
		}
	}
	
	private void getLiveTile(String name, Object3D object3d){
		
		if(name.startsWith("x_b_mine")){
			mTiles[0].setTile1(object3d, new SimpleVector(0.839, -1, 0));
		}else if (name.startsWith("x_b_m2ine")) {
			mTiles[0].setTile2(object3d, new SimpleVector(0.839, -1, 0));
		}else if (name.startsWith("x_b_car")) {
			mTiles[1].setTile1(object3d, new SimpleVector(0.839, -1, 0));
		}else if (name.startsWith("x_b_c2ar")) {
			mTiles[1].setTile2(object3d, new SimpleVector(0.839, -1, 0));
		}else if (name.startsWith("x_b_video")) {
			mTiles[2].setTile1(object3d, new SimpleVector(0.839, -1, 0));
		}else if (name.startsWith("x_b_v2ideo")) {
			mTiles[2].setTile2(object3d, new SimpleVector(0.839, -1, 0));
		}else if (name.startsWith("x_b_pic")) {
			mTiles[3].setTile1(object3d, new SimpleVector(1.729, -1, 0));
		}else if (name.startsWith("x_b_p2ic")) {
			mTiles[3].setTile2(object3d, new SimpleVector(1.729, -1, 0));
		}
		
	}
	
	private void setBoardTexture(String name, Object3D object3d) {
		TextureManager tm = TextureManager.getInstance();
		String tname = name.substring(2, name.indexOf("_Plane"));
		object3d.clearAdditionalColor();

		Log.e(TAG, "matching tname:"+tname);
		
		if (tm.containsTexture(tname)) {
			object3d.setTexture(tname);
		}else {
			object3d.setTexture("dummy");
		}
		object3d.calcTextureWrapSpherical();
		object3d.strip();
		object3d.build();
	}

	private void saveObject(String name, Object3D object3d) {
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

			getIslands(name, object3d);

			return;
		} else if (name.startsWith("x_b")) {

			Log.e(TAG, "x_b");

			setBoardTexture(name, object3d);
			getLiveTile(name, object3d);

			return;
		} else if (name.startsWith("x_scr")) {

			Log.e(TAG, "x_scr");

			object3d.clearAdditionalColor();
			object3d.setShader(screenShaders[name.charAt(5) - '1']);
			object3d.calcTextureWrapSpherical();
			object3d.build();
			object3d.strip();

			screens[name.charAt(5) - '1'] = object3d;

			return;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		gestureDetector = new GestureDetector(this, gestureListener);
	}

	@Override
	public void onDrawEye(Eye eye) {
		fillTexturesWithEye(buffer, eye);

		screenShaders[0].setUniform("videoFrame", 4);
		screenShaders[0].setUniform("videoFrame2", 5);
		screenShaders[0].setUniform("videoFrame3", 6);

		fb.clear(back);
		world.renderScene(fb);
		world.draw(fb);
		fb.display();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (true)
			return gestureDetector.onTouchEvent(event);
		
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
