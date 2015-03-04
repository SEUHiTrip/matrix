package seu.lab.matrix;

import java.nio.ByteBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import android.R.integer;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import com.idisplay.VirtualScreenDisplay.FPSCounter;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection;
import com.idisplay.VirtualScreenDisplay.IIdisplayViewRendererContainer;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection.ConnectionMode;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection.IDisplayConnectionCallback;
import com.idisplay.util.ArrayImageContainer;
import com.idisplay.util.ImageContainer;
import com.idisplay.util.RLEImage;
import com.idisplay.util.ServerItem;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.GLSLShader;
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Logger;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureInfo;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

public class Framework3DMatrixActivity extends AbstractScreenMatrixActivity
		implements CardboardView.StereoRenderer, IDisplayConnectionCallback {

	private static Activity master = null;
	private static final String TAG = "Framework3DMatrixActivity";

	private ServerItem usbServerItem;
	private IDisplayConnection iDisplayConnection;
	private ConnectionMode currentMode;
	
	private FrameBuffer fb = null;
	private World world = null;
	private Light sun = null;
	private Object3D[] screens = null;
	private Object3D[] islands = null;
	private GLSLShader[] screenShaders = null;

	private RGBColor back = new RGBColor(50, 50, 100);

	private SimpleVector forward = new SimpleVector(-1, 0, 0);


	private int mWidth = 1024;
	private int mHeight = 1024;
	private int mStrideX = 1024;
	private int mStrideY = 1024;

	private float[] mAngles = new float[3];

	ArrayImageContainer mArrayImageContainer;

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
			// if (z) {
			// reInitTextureBuffer();
			// }
			// isDirty = true;
			FPSCounter.imageRenderComplete();
			// TODO requestRender();
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

		private float mCursorMulX;
		private float mCursorMulY;

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
	private int[] buffer;
	private boolean canCamRotate = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		usbServerItem = ServerItem.CreateUsbItem(this);
		iDisplayConnection = new IDisplayConnection(this);

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
		CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
		cardboardView.setRenderer(this);
		setCardboardView(cardboardView);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "usbServerItem : " + usbServerItem);

		iDisplayConnection.connectToServer(usbServerItem, currentMode);
	}

	@Override
	protected void onPause() {
		super.onPause();
		iDisplayConnection.listScreenHandler.sendEmptyMessage(1);
	}

	@Override
	public void onIDisplayConnected() {

	}

	@Override
	public void onDrawEye(Eye eye) {

		screens[2].setVisibility(false);
		
		if (currentMode.type == IDisplayConnection.ConnectionType.Single) {

		} else if (currentMode.type == IDisplayConnection.ConnectionType.Duel) {
			if (eye.getType() == Eye.Type.LEFT) {
				screens[0].setVisibility(true);
				screens[1].setVisibility(false);
			} else {
				screens[0].setVisibility(false);
				screens[1].setVisibility(true);
			}
		} else {
			
		}

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
	public void onFinishFrame(Viewport viewport) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNewFrame(HeadTransform headTransform) {
		headTransform.getEulerAngles(mAngles, 0);

		Camera cam = world.getCamera();
		cam.lookAt(forward);
		if (canCamRotate) {
			cam.rotateY(mAngles[1]);
			cam.rotateZ(0 - mAngles[2]);
			cam.rotateX(mAngles[0]);
		}

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
			world.setAmbientLight(50, 50, 50);

			sun = new Light(world);
			sun.setIntensity(250, 250, 250);

			screens = new Object3D[3];
			islands = new Object3D[4];
			
			screens[0] = Primitives.getPlane(1, 10);
			screens[0].rotateY(4.71f);
			screens[0].translate(10, 5, 5);
			screens[0].setShader(screenShaders[0]);
			screens[0].calcTextureWrapSpherical();
			screens[0].build();
			screens[0].strip();
			world.addObject(screens[0]);

			screens[1] = Primitives.getPlane(1, 10);
			screens[1].rotateY(4.71f);
			screens[1].translate(10, 5, 5);
			screens[1].setShader(screenShaders[1]);
			screens[1].calcTextureWrapSpherical();
			screens[1].build();
			screens[1].strip();
			world.addObject(screens[1]);
			
			screens[2] = Primitives.getPlane(1, 10);
			screens[2].rotateY(4.71f);
			screens[2].translate(10, 5, 5);
			screens[2].setShader(screenShaders[2]);
			screens[2].calcTextureWrapSpherical();
			screens[2].build();
			screens[2].strip();
			world.addObject(screens[2]);
			screens[2].setVisibility(false);
			
			islands[0] = Object3D.mergeAll(Loader.load3DS(getResources().openRawResource(R.raw.island_green), 1f));
			islands[0].translate(-10, 0, -10);
			islands[0].rotateY(3.14f/2);
			islands[0].rotateZ(3.14f/4);
			world.addObjects(islands[0]);
			
			islands[1] = Object3D.mergeAll(Loader.load3DS(getResources().openRawResource(R.raw.island_volcano), 1f));
			islands[1].translate(-10, 0, 0);
			islands[1].rotateY(3.14f/2);
			islands[1].rotateZ(3.14f/4);
			world.addObjects(islands[1]);
			
			islands[2] = Object3D.mergeAll(Loader.load3DS(getResources().openRawResource(R.raw.island_ship), 0.25f));
			islands[2].translate(-10, 0, 10);
			islands[2].rotateY(3.14f/2);
			islands[2].rotateZ(3.14f/4);
			world.addObjects(islands[2]);
			
			if (currentMode.type == IDisplayConnection.ConnectionType.Single) {
				screens[1].setVisibility(false);
			} else if (currentMode.type == IDisplayConnection.ConnectionType.Duel) {

			} else {
				screens[1].translate(0,0,-12);
			}

			Camera cam = world.getCamera();

			SimpleVector sv = new SimpleVector(0, 0, 0);
			sv.y -= 100;
			sv.z -= 100;
			sun.setPosition(sv);
			MemoryHelper.compact();
		}

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
	public void onSurfaceCreated(EGLConfig config) {
		Resources res = getResources();
//		TextureManager tm = TextureManager.getInstance();

		// face = new Texture(res.openRawResource(R.raw.face));
		// video1 = new Texture(1024, 1024, new RGBColor(100, 0, 0));
		// video2 = new Texture(1024, 1024, new RGBColor(0, 100, 0));
		// video3 = new Texture(1024, 1024, new RGBColor(0, 0, 100));
		//
		// tm.addTexture("videoFrame",video1);
		// tm.addTexture("videoFrame2",video2);
		// tm.addTexture("videoFrame3",video3);
		//
		// Log.d(TAG,
		// "texid "+tm.getTextureID("videoFrame")+" "+tm.getTextureID("videoFrame2")+" "+tm.getTextureID("videoFrame3"));

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

	@Override
	public void onInstanceCursorImgChange(ImageContainer imageContainer) {

	}

	@Override
	public void onInstanceCursorPositionChange(int i, int i2) {
		// TODO Auto-generated method stub

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

	public void fillTexturesWithEye(int[] iArr, Eye eye) {
		if (mArrayImageContainer != null) {
			int x = mArrayImageContainer.getStrideX();
			int y = mArrayImageContainer.getStrideY();
			int len = x * y;

			if (currentMode.type == IDisplayConnection.ConnectionType.Single) {
				simpleFillTextures(iArr, 0, 0, len, x, y);
			} else {
				if (eye.getType() == Eye.Type.LEFT) {
					simpleFillTextures(iArr, 0, 0, len >> 1, x, y >> 1);
				} else if (eye.getType() == Eye.Type.RIGHT) {
					simpleFillTextures(iArr, 3, len >> 1, len >> 1, x, y >> 1);
				}
			}
		}
	}

	public void simpleFillTextures(int[] iArr, int offset, int start, int count, int width,
			int height) {

		GLES20.glActiveTexture(GLES20.GL_TEXTURE4+offset);
		GLES20.glBindTexture(3553, iArr[4+offset]);
		GLES20.glTexImage2D(3553, 0, 6409, width, height, 0, 6409, 5121,
				ByteBuffer.wrap(mArrayImageContainer.getDataY(), start, count));

		width = width >> 1;
		height = height >> 1;
		GLES20.glActiveTexture(GLES20.GL_TEXTURE5+offset);
		GLES20.glBindTexture(3553, iArr[5+offset]);
		GLES20.glTexImage2D(3553, 0, 6409, width, height, 0, 6409, 5121,
				ByteBuffer.wrap(mArrayImageContainer.getDataU(), start >> 2,
						count >> 2));

		GLES20.glActiveTexture(GLES20.GL_TEXTURE6+offset);
		GLES20.glBindTexture(3553, iArr[6+offset]);
		GLES20.glTexImage2D(3553, 0, 6409, width, height, 0, 6409, 5121,
				ByteBuffer.wrap(mArrayImageContainer.getDataV(), start >> 2,
						count >> 2));
	}

	Matrix rotationMatrix1 = new Matrix();
	Matrix translatMatrix1 = new Matrix();

	Matrix rotationMatrix2 = new Matrix();
	Matrix translatMatrix2 = new Matrix();
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!(event.getAction() == MotionEvent.ACTION_UP))
			return false;

		canCamRotate = canCamRotate ? false : true;

		if (!canCamRotate) {
			rotationMatrix1 = new Matrix(screens[0].getRotationMatrix());
			translatMatrix1 = new Matrix(screens[0].getTranslationMatrix());

			screens[0].clearRotation();
			screens[0].clearTranslation();

			screens[0].rotateY(4.71f);
			screens[0].translate(new SimpleVector(-8, 0, 0));
			
			rotationMatrix2 = new Matrix(screens[1].getRotationMatrix());
			translatMatrix2 = new Matrix(screens[1].getTranslationMatrix());

			screens[1].clearRotation();
			screens[1].clearTranslation();

			screens[1].rotateY(4.71f);
			screens[1].translate(new SimpleVector(-8, 0, 0));
		} else {
			screens[0].setTranslationMatrix(translatMatrix1);
			screens[0].setRotationMatrix(rotationMatrix1);
			
			screens[1].setTranslationMatrix(translatMatrix2);
			screens[1].setRotationMatrix(rotationMatrix2);
		}

		return false;
	}
}
