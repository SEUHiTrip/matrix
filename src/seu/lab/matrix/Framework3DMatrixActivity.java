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
	private Object3D screen1 = null;
	private Object3D screen2 = null;
	private RGBColor back = new RGBColor(50, 50, 100);

	private SimpleVector forward = new SimpleVector(-1, 0, 0);

	private GLSLShader screen1Shader = null;
	private GLSLShader screen2Shader = null;

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

		iDisplayConnection.connectToServer(usbServerItem);
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

		if (currentMode.type == IDisplayConnection.ConnectionType.Single) {

		} else if (currentMode.type == IDisplayConnection.ConnectionType.Duel) {
			if (eye.getType() == Eye.Type.LEFT) {
				screen1.setVisibility(true);
				screen2.setVisibility(false);
			} else {
				screen1.setVisibility(false);
				screen2.setVisibility(true);
			}
		} else {
			
		}

		fillTexturesWithEye(buffer, eye);

		screen1Shader.setUniform("videoFrame", 4);
		screen1Shader.setUniform("videoFrame2", 5);
		screen1Shader.setUniform("videoFrame3", 6);

		screen2Shader.setUniform("videoFrame", 7);
		screen2Shader.setUniform("videoFrame2", 8);
		screen2Shader.setUniform("videoFrame3", 9);

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
			world.setAmbientLight(20, 20, 20);

			sun = new Light(world);
			sun.setIntensity(250, 250, 250);

			screen1 = Primitives.getPlane(1, 10);
			screen1.rotateY(4.71f);
			screen1.translate(-10, 5, 5);
			screen1.setShader(screen1Shader);
			screen1.calcTextureWrapSpherical();
			screen1.build();
			screen1.strip();
			world.addObject(screen1);

			screen2 = Primitives.getPlane(1, 10);
			screen2.rotateY(4.71f);
			screen2.translate(-10, 5, 5);
			screen2.setShader(screen2Shader);
			screen2.calcTextureWrapSpherical();
			screen2.build();
			screen2.strip();
			world.addObject(screen2);

			if (currentMode.type == IDisplayConnection.ConnectionType.Single) {
				screen2.setVisibility(false);
			} else if (currentMode.type == IDisplayConnection.ConnectionType.Duel) {

			} else {
				screen2.translate(0,0,-12);
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

			int numOfTextures = 3 + 4 + 3;
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

		screen1Shader = new GLSLShader(Loader.loadTextFile(res
				.openRawResource(R.raw.vertexshader_offset)),
				Loader.loadTextFile(res
						.openRawResource(R.raw.fragmentshader_offset)));
		screen2Shader = new GLSLShader(Loader.loadTextFile(res
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
			rotationMatrix1 = new Matrix(screen1.getRotationMatrix());
			translatMatrix1 = new Matrix(screen1.getTranslationMatrix());

			screen1.clearRotation();
			screen1.clearTranslation();

			screen1.rotateY(4.71f);
			screen1.translate(new SimpleVector(-8, 0, 0));
			
			rotationMatrix2 = new Matrix(screen2.getRotationMatrix());
			translatMatrix2 = new Matrix(screen2.getTranslationMatrix());

			screen2.clearRotation();
			screen2.clearTranslation();

			screen2.rotateY(4.71f);
			screen2.translate(new SimpleVector(-8, 0, 0));
		} else {
			screen1.setTranslationMatrix(translatMatrix1);
			screen1.setRotationMatrix(rotationMatrix1);
			
			screen2.setTranslationMatrix(translatMatrix2);
			screen2.setRotationMatrix(rotationMatrix2);
		}

		return false;
	}
}
