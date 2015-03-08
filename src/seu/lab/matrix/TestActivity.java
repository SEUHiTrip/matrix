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

import android.R.anim;
import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;

import com.arwave.skywriter.objects.Rectangle;
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

public class TestActivity extends BaseTestActivity {

	private static String TAG = "TestActivity";
	private static TestActivity master = null;

	private float[] mHeadView;
	private float[] mForwardVec;
	private float[] mRightVec;
	private float[] mUpVec;

	private Object3D cube1 = null;
	private Object3D cube2 = null;
	private Object3D cube3 = null;

	private static final float CAMERA_Z = 0.01f;

	private float[] mCamera;

	private float[] mView;
	
	private Handler mHandler;
	
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
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onContinuousGestureStart(ContinuousGestureEvent event) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onContinuousGestureEnd() {
			// TODO Auto-generated method stub
			
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
				masks.put("" + GestureEvent.Gestures.SWIPE_BACK_LEFT_L.ordinal(),
						true);
				masks.put("" + GestureEvent.Gestures.SWIPE_BACK_RIGHT_L.ordinal(),
						true);
				config.put("masks", masks);
			} catch (JSONException e) {
				Log.e(TAG, e.toString());
			}

			return config;
		}
	}; 
	
	IDolphinStateCallback stateCallback = new IDolphinStateCallback() {
		
		@Override
		public void onNormal() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onNoisy() {
			// TODO Auto-generated method stub
			
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
			// TODO Auto-generated method stub
			
		}
	};
	
	Dolphin dolphin = null;
	private Object3D notice;
	private Bitmap fontBitmap;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.common_ui);
		CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
		cardboardView.getHolder().setFormat(PixelFormat.RGBA_8888);
		cardboardView.setRenderer(this);
		setCardboardView(cardboardView);

		mOverlayView = (CardboardOverlayView) findViewById(R.id.overlay);
		mOverlayView.show3DToast("Test start");
		
		// cardboardView.setDistortionCorrectionEnabled(false);
		mHeadView = new float[16];
		mForwardVec = new float[3];
		mRightVec = new float[3];
		mUpVec = new float[3];
		mCamera = new float[16];
		mView = new float[16];
		mAngles = new float[3];
		mat = new Matrix();
		
		try {
            dolphin = Dolphin.getInstance(
                    (AudioManager)getSystemService(Context.AUDIO_SERVICE), 
                    getContentResolver(),
                    stateCallback,
                    null,
                    gestureListener);
        } catch (DolphinException e) {
            Log.e(TAG, e.toString());
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
		
		mHandler = new Handler(getMainLooper());
		
		initFontBitmap();
		TextureManager tm = TextureManager.getInstance();

		if (!tm.containsTexture("font")) {
			Texture fontTexture = new Texture(fontBitmap,true);	
			tm.addTexture("font", fontTexture);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		
//		try {
//			dolphin.prepare(getApplicationContext());
//		} catch (DolphinException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	@Override
	protected void onPause() {
//		try {
//			dolphin.pause();
//		} catch (DolphinException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		super.onPause();
	}
	
	@Override
	protected void onStop() {
//		try {
//			dolphin.stop();
//		} catch (DolphinException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		super.onStop();
	}
	
	@Override
	public void onDrawEye(Eye eye) {

		// android.opengl.Matrix.multiplyMM(mView, 0, eye.getEyeView(), 0,
		// mCamera, 0);

		// for (int i = 0; i < 4; i++) {
		// for (int j = 0; j < 4; j++) {
		// System.out.print(mView[i*4+j]);
		// }
		// System.out.println();
		// }
		// System.out.println("=====================");
		Camera cam = world.getCamera();

		// for (int i = 0; i < 3; i++) {
		// mForwardVec[i] = mView[i];
		// mUpVec[i] = mView[i + 4];
		// mRightVec[i] = mView[i + 8];
		// }

		SimpleVector simpleVector1 = new SimpleVector(1, 0, 0);
		SimpleVector simpleVector2 = new SimpleVector(0, 0, 1);

		// cube.setOrientation(simpleVector1, simpleVector2);
		// cam.setOrientation(simpleVector1, simpleVector2);

		// cam.moveCamera(Camera.CAMERA_MOVEOUT, 50);
		cam.lookAt(cube.getTransformedCenter());
		cam.rotateY(mAngles[1]);
		cam.rotateZ(0 - mAngles[2]);
		cam.rotateX(mAngles[0]);

//		SimpleVector cubeCenter = new SimpleVector();
//		SimpleVector camDir = new SimpleVector();
//
//		cube.getTransformedCenter(cubeCenter);
//		cam.getDirection(camDir);
//
//		double sum = Math.pow(cubeCenter.x, 2d) + Math.pow(cubeCenter.y, 2d)
//				+ Math.pow(cubeCenter.z, 2d);
//		sum = Math.sqrt(sum);
//		cubeCenter.x = (float) (cubeCenter.x / sum);
//		cubeCenter.y = (float) (cubeCenter.y / sum);
//		cubeCenter.z = (float) (cubeCenter.z / sum);
//
//		double dot = camDir.x * cubeCenter.x + camDir.y * cubeCenter.y
//				+ camDir.z * cubeCenter.z;
//		
//		Log.d("cam", "dot: " + dot);

		// cam.rotateX(mAngles[2]);//roll 2
		// cam.rotateY(mAngles[1]);//pitch 0
		// cam.rotateZ(mAngles[0]);//yaw 1

		int factor = 5;
		cube1.setTranslationMatrix(new Matrix());
		cube2.setTranslationMatrix(new Matrix());
		cube3.setTranslationMatrix(new Matrix());
		cube1.translate(new SimpleVector(-10 + factor * mForwardVec[0], factor
				* mForwardVec[1], factor * mForwardVec[2]));
		cube2.translate(new SimpleVector(-10 + factor * mRightVec[0], factor
				* mRightVec[1], factor * mRightVec[2]));
		cube3.translate(new SimpleVector(-10 + factor * mUpVec[0], factor
				* mUpVec[1], factor * mUpVec[2]));
	
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

		android.opengl.Matrix.setLookAtM(mCamera, 0, 0.0f, 0.0f, CAMERA_Z,
				0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

		headTransform.getEulerAngles(mAngles, 0);

		// Log.d("angles", mAngles[0] + " " + mAngles[1] + " " + mAngles[2]);
		headTransform.getHeadView(mHeadView, 0);
		headTransform.getForwardVector(mForwardVec, 0);
		headTransform.getRightVector(mRightVec, 0);
		headTransform.getUpVector(mUpVec, 0);

		// System.out.println("============================");
		// System.out.println(new SimpleVector(mForwardVec));
		// System.out.println(new SimpleVector(mRightVec));
		// System.out.println(new SimpleVector(mUpVec));
		// System.out.println("============================");

		for (int i = 0; i < mUpVec.length; i++) {
			// mForwardVec[i] = - mForwardVec[i];
			mUpVec[i] = -mUpVec[i];
			mRightVec[i] = -mRightVec[i];
		}

		// mForwardVec[0] = mRightVec[1] * mUpVec[2] - mRightVec[2] * mUpVec[1];

		// mForwardVec[1] = mRightVec[2] * mUpVec[0] - mRightVec[0] * mUpVec[2];

		// mForwardVec[2] = mRightVec[0] * mUpVec[1] - mRightVec[1] * mUpVec[0];

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
			world.setAmbientLight(120, 120, 120);

			sun = new Light(world);
			sun.setIntensity(250, 250, 250);

			// Create a texture out of the icon...:-)
			Texture texture = new Texture(BitmapHelper.rescale(
					BitmapHelper.convert(getResources().getDrawable(
							R.drawable.icon)), 64, 64),true);
			TextureManager.getInstance().addTexture("texture", texture);

			cube = Primitives.getCube(1);
			cube.translate(-10, 0, 0);
			cube.calcTextureWrapSpherical();
			cube.setTexture("texture");
			cube.strip();
			cube.build();
			world.addObject(cube);
			cube.setVisibility(false);
			
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

			cube1.setVisibility(false);
			cube2.setVisibility(false);
			cube3.setVisibility(false);

			plane = Primitives.getCube(30);
			plane.translate(0, 32, 0);
			plane.calcTextureWrapSpherical();
			plane.setTexture("texture");
			plane.strip();
			plane.build();
			world.addObject(plane);

			plane.setVisibility(false);
			
			notice = new Rectangle(1, 2, 1);
			notice.rotateY(4.71f);
			notice.translate(-5, 0, 0);
			notice.setTexture("font");
			notice.build();
			notice.strip();
			world.addObject(notice);
			
			Camera cam = world.getCamera();
			cam.lookAt(cube.getTransformedCenter());
			// cam.moveCamera(Camera.CAMERA_MOVEOUT, 50);
			// cam.setFOV(cam.getMinFOV());
			
			SimpleVector sv = new SimpleVector();
			sv.set(cube.getTransformedCenter());
			sv.y -= 50;
			sv.z -= 50;
			sun.setPosition(sv);
			MemoryHelper.compact();

			if (master == null) {
				Logger.log("Saving master Activity!");
				master = this;
			}
		}
	}

	public void initFontBitmap(){  
        String font = "Fonts to be tested!";
        fontBitmap = Bitmap.createBitmap(256, 128, Bitmap.Config.ARGB_8888);  
        
        Canvas canvas = new Canvas(fontBitmap);  
        
        //背景颜色  
        canvas.drawColor(Color.TRANSPARENT);  
        Paint p = new Paint();  
        //字体设置  
        String fontType = "Calibri";  
        Typeface typeface = Typeface.create(fontType, Typeface.BOLD);  
        //消除锯齿  
        p.setAntiAlias(true);  
        //字体为红色  
        p.setColor(Color.RED);  
        p.setTypeface(typeface);  
        p.setTextSize(28);  
        //绘制字体  
        canvas.drawText(font, 0, 100, p);  
    }
	
	@Override
	public void onSurfaceCreated(EGLConfig config) {
		
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		world = new World();
	}

	@Override
	public void onCardboardTrigger() {
		// TODO Auto-generated method stub
		super.onCardboardTrigger();
	}
}

