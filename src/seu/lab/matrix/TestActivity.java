package seu.lab.matrix;

import javax.microedition.khronos.egl.EGLConfig;

import android.R.anim;
import android.R.integer;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;

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

public class TestActivity extends CardboardActivity implements
		CardboardView.StereoRenderer {

	private static TestActivity master = null;

	private float[] mHeadView;
	private float[] mForwardVec;
	private float[] mRightVec;
	private float[] mUpVec;

	private FrameBuffer fb = null;
	private World world = null;
	private Light sun = null;
	private Object3D plane = null;

	private Object3D cube = null;
	private Object3D cube1 = null;
	private Object3D cube2 = null;
	private Object3D cube3 = null;

	private static final float CAMERA_Z = 0.01f;
	
	private RGBColor back = new RGBColor(50, 50, 100);

	Matrix mat = new Matrix();

	private float[] mCamera;

	private float[] mView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.common_ui);
		CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
		cardboardView.setRenderer(this);
		setCardboardView(cardboardView);

		// cardboardView.setDistortionCorrectionEnabled(false);
		mHeadView = new float[16];
		mForwardVec = new float[3];
		mRightVec = new float[3];
		mUpVec = new float[3];
		mCamera = new float[16];
		mView = new float[16];
		mat = new Matrix();
	}

	@Override
	public void onDrawEye(Eye eye) {

		android.opengl.Matrix.multiplyMM(mView, 0, eye.getEyeView(), 0, mCamera, 0);
		
//		for (int i = 0; i < 4; i++) {
//			for (int j = 0; j < 4; j++) {
//				System.out.print(mView[i*4+j]);
//			}
//			System.out.println();
//		}
//		System.out.println("=====================");
		Camera cam = world.getCamera();
		
        for (int i = 0; i < 3; i++) {
        	mForwardVec[i] = -mView[i + 8];
        	mUpVec[i] = -mView[i + 4];
            mRightVec[i] = mView[i];
        }
		
		SimpleVector simpleVector1 = new SimpleVector(mForwardVec[0], mForwardVec[1], mForwardVec[2]);
		SimpleVector simpleVector2 = new SimpleVector(mUpVec[0], mUpVec[1], mUpVec[2]);

//		cam.setOrientation(simpleVector1, simpleVector2);
//		cam.lookAt(cube.getTransformedCenter());
//		cam.moveCamera(Camera.CAMERA_MOVEOUT, 50);
//		cam.lookAt(cube.getTransformedCenter());
		
//		cam.rotateCameraY(0.01f);
//		System.out.println("=======================");

//		System.out.println(cam.getDirection());
//		System.out.println(cam.getUpVector());
		cube.setOrientation(simpleVector1, simpleVector2);

		int factor = 5;
		cube1.setTranslationMatrix(new Matrix());
		cube2.setTranslationMatrix(new Matrix());
		cube3.setTranslationMatrix(new Matrix());
		cube1.translate(new SimpleVector(-10+factor*mForwardVec[0],factor*mForwardVec[1],factor*mForwardVec[2]));
		cube2.translate(new SimpleVector(-10+factor*mRightVec[0],factor*mRightVec[1],factor*mRightVec[2]));
		cube3.translate(new SimpleVector(-10+factor*mUpVec[0],factor*mUpVec[1],factor*mUpVec[2]));
		
		fb.clear(back);
		world.renderScene(fb);
		world.draw(fb);
		fb.display();

	}

	@Override
	public void onFinishFrame(Viewport arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNewFrame(HeadTransform headTransform) {
		
		android.opengl.Matrix.setLookAtM(mCamera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f,
				0.0f, 1.0f, 0.0f);
		
//		headTransform.getHeadView(mHeadView, 0);
//		headTransform.getForwardVector(mForwardVec, 0);
//		headTransform.getRightVector(mRightVec, 0);
//		headTransform.getUpVector(mUpVec, 0);
		
//		System.out.println("============================");
//		System.out.println(new SimpleVector(mForwardVec));
//		System.out.println(new SimpleVector(mRightVec));
//		System.out.println(new SimpleVector(mUpVec));
//		System.out.println("============================");

//		for (int i = 0; i < mUpVec.length; i++) {
////			mForwardVec[i] = - mForwardVec[i];
//			mUpVec[i] = - mUpVec[i];
//			mRightVec[i] = - mRightVec[i];
//		}


//		mForwardVec[0] = mRightVec[1] * mUpVec[2] - mRightVec[2] * mUpVec[1];
//
//		mForwardVec[1] = mRightVec[2] * mUpVec[0] - mRightVec[0] * mUpVec[2];
//
//		mForwardVec[2] = mRightVec[0] * mUpVec[1] - mRightVec[1] * mUpVec[0];

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
			
			cube1 = Primitives.getCube(1);
			cube1.translate(-10, 0, 0);
			cube1.calcTextureWrapSpherical();
			cube1.setTexture("texture");
			cube1.strip();
			cube1.setAdditionalColor(new RGBColor(100, 0, 0));
			cube1.build();
			world.addObject(cube1);
			
			cube2 = Primitives.getCube(1);
			cube2.translate(-10, 0, 0);
			cube2.calcTextureWrapSpherical();
			cube2.setTexture("texture");
			cube2.strip();
			cube2.setAdditionalColor(new RGBColor(0, 100, 0));
			cube2.build();
			world.addObject(cube2);
			
			cube3 = Primitives.getCube(1);
			cube3.translate(-10, 0, 0);
			cube3.calcTextureWrapSpherical();
			cube3.setTexture("texture");
			cube3.strip();
			cube3.setAdditionalColor(new RGBColor(0, 0, 100));
			cube3.build();
			world.addObject(cube3);

			plane = Primitives.getCube(30);
			plane.translate(0, 32, 0);
			plane.calcTextureWrapSpherical();
			plane.setTexture("texture");
			plane.strip();
			plane.build();
			world.addObject(plane);
			
			Camera cam = world.getCamera();
			cam.lookAt(cube.getTransformedCenter());
//			cam.moveCamera(Camera.CAMERA_MOVEOUT, 50);
			//cam.setFOV(cam.getMinFOV());
			
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
		// TODO Auto-generated method stub
		world = new World();

	}
}