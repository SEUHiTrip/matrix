package seu.lab.matrix;

import javax.microedition.khronos.egl.EGLConfig;

import org.json.JSONException;
import org.json.JSONObject;

import seu.lab.dolphin.client.ContinuousGestureEvent;
import seu.lab.dolphin.client.Dolphin;
import seu.lab.dolphin.client.DolphinException;
import seu.lab.dolphin.client.GestureEvent;
import seu.lab.dolphin.client.IDolphinStateCallback;
import seu.lab.dolphin.client.IGestureListener;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Logger;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.MemoryHelper;

public class BaseTestActivity extends CardboardActivity implements
		CardboardView.StereoRenderer {

	protected static String TAG = "TestActivity";
	protected static Activity master = null;

	protected float[] mAngles;

	protected FrameBuffer fb = null;
	protected World world = null;
	protected Light sun = null;
	protected Object3D plane = null;

	protected Object3D cube = null;

	protected static final float CAMERA_Z = 0.01f;

	protected RGBColor back = new RGBColor(0, 0, 0);

	Matrix mat = new Matrix();

	protected float[] mCamera;

	protected float[] mView;

	protected CardboardOverlayView mOverlayView;
	
	protected Handler mHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.common_ui);
		CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
		cardboardView.setRenderer(this);
		setCardboardView(cardboardView);

		mOverlayView = (CardboardOverlayView) findViewById(R.id.overlay);
		mOverlayView.show3DToast("Test start");

		mCamera = new float[16];
		mView = new float[16];
		mAngles = new float[3];

	}
	
	@Override
	public void onDrawEye(Eye eye) {
		Camera cam = world.getCamera();

		cam.lookAt(cube.getTransformedCenter());
		cam.rotateY(mAngles[1]);
		cam.rotateZ(0 - mAngles[2]);
		cam.rotateX(mAngles[0]);
	
		fb.clear(back);
		world.renderScene(fb);
		world.draw(fb);
		fb.display();
	}

	@Override
	public void onFinishFrame(Viewport arg0) {

	}

	@Override
	public void onNewFrame(HeadTransform headTransform) {
		headTransform.getEulerAngles(mAngles, 0);
	}

	@Override
	public void onRendererShutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSurfaceChanged(int w, int h) {
		if (fb != null) {
			fb.dispose();
		}

		fb = new FrameBuffer(w, h);

		if (master == null) {

			world = new World();
			world.setAmbientLight(20, 20, 20);

			sun = new Light(world);
			sun.setIntensity(250, 250, 250);

			// Create a texture out of the icon...:-)
			Texture texture = new Texture(BitmapHelper.rescale(
					BitmapHelper.convert(getResources().getDrawable(
							R.drawable.icon)), 64, 64));
			TextureManager.getInstance().addTexture("texture", texture);

			cube = Primitives.getCube(1);
			cube.translate(-10, 0, 0);
			cube.calcTextureWrapSpherical();
			cube.setTexture("texture");
			cube.strip();
			cube.build();
			world.addObject(cube);

			plane = Primitives.getPlane(1, 30);
			plane.translate(0, 32, 0);
			plane.calcTextureWrapSpherical();
			plane.setTexture("texture");
			plane.strip();
			plane.build();
			world.addObject(plane);
			
			Camera cam = world.getCamera();
			cam.lookAt(cube.getTransformedCenter());
			
			SimpleVector sv = new SimpleVector();
			sv.set(cube.getTransformedCenter());
			sv.y -= 100;
			sv.z -= 100;
			sun.setPosition(sv);
			MemoryHelper.compact();

			if (master == null) {
				Logger.log("Saving master Activity!");
				master = this;
			}
		}
	}

	@Override
	public void onSurfaceCreated(EGLConfig config) {
		world = new World();
	}

	@Override
	public void onCardboardTrigger() {
		super.onCardboardTrigger();
	}
}