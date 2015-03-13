package seu.lab.matrix;

import java.nio.ByteBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import android.R.integer;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
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
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.LensFlare;
import com.threed.jpct.util.MemoryHelper;
import com.threed.jpct.util.SkyBox;

public class Framework3DMatrixActivity extends AbstractScreenMatrixActivity
		implements CardboardView.StereoRenderer, IDisplayConnectionCallback {

	protected static Activity master = null;
	protected static final String TAG = "Framework3DMatrixActivity";

	protected ServerItem usbServerItem;
	protected IDisplayConnection iDisplayConnection;
	protected ConnectionMode currentMode;

	protected FrameBuffer fb = null;
	protected SkyBox sky;
	protected World world = null;
	protected Light sun = null;
	protected Light spot = null;
	protected Object3D[] screens = null;
	protected Object3D[] islands = null;
	protected Object3D treasure = null;
	protected Object3D notice = null;
	protected GLSLShader[] screenShaders = null;

	protected RGBColor back = new RGBColor(50, 50, 100);
	protected RGBColor wire = new RGBColor(100, 100, 100);

	protected SimpleVector forward = new SimpleVector(-1, 0, 0);

	protected int[] buffer;
	protected boolean canCamRotate = true;

	protected int mWidth = 1024;
	protected int mHeight = 1024;
	protected int mStrideX = 1024;
	protected int mStrideY = 1024;

	protected float[] mAngles = new float[3];

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

		protected float mCursorMulX;
		protected float mCursorMulY;

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
	protected Bitmap fontBitmap;

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
		
		initFontBitmap();
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
		sky.render(world, fb);

		if(false && eye.getType() == Eye.Type.LEFT){
			world.drawWireframe(fb, wire, 2, false);
		}else {
			world.draw(fb);
		}
		
		fb.display();
	}

	@Override
	public void onFinishFrame(Viewport viewport) {
	}
	
	double isLookingAt(Camera cam, SimpleVector center){
		SimpleVector camDir = new SimpleVector();
		cam.getDirection(camDir);
		SimpleVector camPos=cam.getPosition();
		
		//double sum = Math.pow(center.x, 2d) + Math.pow(center.y, 2d) + Math.pow(center.z, 2d);
		double sum = Math.pow(center.x-camPos.x, 2d) + Math.pow(center.y-camPos.y, 2d) + Math.pow(center.z-camPos.z, 2d);
		sum = Math.sqrt(sum);
//		center.x = (float) (center.x / sum);
//		center.y = (float) (center.y / sum);
//		center.z = (float) (center.z / sum);

		//double dot = camDir.x * center.x + camDir.y * center.y + camDir.z * center.z;
		double dot = (camDir.x * (center.x-camPos.x) + camDir.y * (center.y-camPos.y) + camDir.z * (center.z-camPos.z))/sum;
		return dot;
	}
	
	@Override
	public void onNewFrame(HeadTransform headTransform) {
		headTransform.getEulerAngles(mAngles, 0);

		Camera cam = world.getCamera();
		
		SimpleVector center_island_green = new SimpleVector();
		SimpleVector center_island_volcano = new SimpleVector();
		SimpleVector center_island_ship = new SimpleVector();	
		
		islands[0].getTransformedCenter(center_island_green);
		islands[1].getTransformedCenter(center_island_volcano);
		islands[2].getTransformedCenter(center_island_ship);
			
		if( isLookingAt(cam, center_island_green) >0.99){
			cam.setPosition(islands[0].getTranslation().x,islands[0].getTranslation().y-2,islands[0].getTranslation().z);
			cam.rotateY(-3.14f/2);
		}
		//Log.e("cam", "island_green: " + dot);
		//			islands[0].setScale(1.2f);
		//		else
		//			islands[0].setScale(0.8f);
		if( isLookingAt(cam, center_island_volcano) >0.99){
			cam.setPosition(islands[1].getTranslation().x,islands[1].getTranslation().y-3,islands[1].getTranslation().z);
			cam.lookAt(new SimpleVector(center_island_volcano.x,center_island_volcano.y,center_island_volcano.z+5));
		}//Log.e("cam", "island_volcano: " + dot);
//			islands[1].setScale(1.2f);
//		else
//			islands[1].setScale(0.8f);
		if(isLookingAt(cam, center_island_ship) >0.99){
			cam.setPosition(islands[2].getTranslation().x,islands[2].getTranslation().y-1,islands[2].getTranslation().z);
			cam.rotateY(3.14f/2);
		}//Log.e("cam", "island_ship: " + dot);
//			islands[2].setScale(1.2f);
//		else
//			islands[2].setScale(0.8f);
		
//		if(isLookingAt(cam, treasure.getTransformedCenter())>0.98)
//			cam.setPosition(0,0,0);
		
		cam.lookAt(forward);
		if (canCamRotate) {
			cam.rotateY(mAngles[1]);
			cam.rotateZ(0 - mAngles[2]);
			cam.rotateX(mAngles[0]);
		}
		
//		Log.e("cam", "transform_green: " + islands[0].getTransformedCenter());
//		Log.e("cam", "transform_volcano: " + islands[1].getTransformedCenter());
//		Log.e("cam", "transform_ship: " + islands[2].getTransformedCenter());
		Log.e("cam", "camDir: " + cam.getDirection());
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

			sky = new SkyBox("star_left", "star_forward", "star_left",
					"star_right", "star_back", "star_bottom", 10000f);
			sky.setCenter(new SimpleVector());

			world = new World();
			world.setAmbientLight(120, 120, 120);

			sun = new Light(world);
			sun.setIntensity(250, 250, 250);

			spot = new Light(world);
			spot.setIntensity(150, 150, 150);

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
			
			notice = Primitives.getPlane(1, 2);
			notice.rotateY(4.71f);
			notice.translate(-5, 0, 0);
			notice.setTexture("font");
			notice.setTransparencyMode(Object3D.TRANSPARENCY_MODE_ADD);
			notice.build();
			notice.strip();
			world.addObject(notice);

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
			
			islands[0] = Object3D.mergeAll(Loader.load3DS(getResources().openRawResource(R.raw.island_green), 0.8f));
			//islands[0].translate(-10, 0, -10);
			islands[0].translate(-10, 10, -5);
			islands[0].rotateX(-3.14f/3f);
			islands[0].rotateY(3.14f/2);
			islands[0].rotateZ(3.14f/4);
			world.addObjects(islands[0]);
			
			islands[1] = Object3D.mergeAll(Loader.load3DS(getResources().openRawResource(R.raw.island_volcano), 1.6f));
			//islands[1].translate(-10, 0, 0);
			islands[1].translate(-15, 10, 0);
			islands[1].rotateX(-3.14f/3f);
			islands[1].rotateY(3.14f/2);
			islands[1].rotateZ(3.14f/4);
			world.addObjects(islands[1]);
			
			islands[2] = Object3D.mergeAll(Loader.load3DS(getResources().openRawResource(R.raw.island_ship), 0.2f));
			//islands[2].translate(-10, 0, 10);
			islands[2].translate(-10, 10, 5);
			islands[2].rotateX(-3.14f/3f);
			islands[2].rotateY(3.14f/2);
			islands[2].rotateZ(3.14f/4);

			world.addObjects(islands[2]);

			treasure = Object3D.mergeAll(Loader.load3DS(getResources()
					.openRawResource(R.raw.treasure), 0.5f));
			treasure.translate(5, 1, 0);
			treasure.rotateY(3.14f / 2);
			treasure.rotateZ(3.14f / 2);
			world.addObjects(treasure);

			if (currentMode.type == IDisplayConnection.ConnectionType.Single) {
				screens[1].setVisibility(false);
			} else if (currentMode.type == IDisplayConnection.ConnectionType.Duel) {

			} else {
				screens[1].translate(0, 0, -12);
			}

			Camera cam = world.getCamera();

			SimpleVector sv = new SimpleVector(0, 0, 0);
			sv.y -= 50;
			// sv.z -= 50;
			sun.setPosition(sv);

			spot.setPosition(new SimpleVector());
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
//		GLES20.glEnable(GLES20.GL_BLEND);
//		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		
		Resources res = getResources();

		TextureManager tm = TextureManager.getInstance();

		if (!tm.containsTexture("dummy")) {
			Texture texture = new Texture(BitmapHelper.rescale(
					BitmapHelper.convert(getResources().getDrawable(
							R.drawable.icon)), 512, 512));
			tm.addTexture("dummy", texture);

			Texture fontTexture = new Texture(fontBitmap);
			tm.addTexture("font", fontTexture);
			
			Texture star_back = new Texture(BitmapHelper.rescale(
					BitmapHelper.convert(getResources().getDrawable(
							R.drawable.star_back)), 512, 512));
			tm.addTexture("star_back", star_back);

			Texture star_bottom = new Texture(BitmapHelper.rescale(
					BitmapHelper.convert(getResources().getDrawable(
							R.drawable.star_bottom)), 512, 512));
			tm.addTexture("star_bottom", star_bottom);

			Texture star_forward = new Texture(BitmapHelper.rescale(
					BitmapHelper.convert(getResources().getDrawable(
							R.drawable.star_forward)), 512, 512));
			tm.addTexture("star_forward", star_forward);

			Texture star_left = new Texture(BitmapHelper.rescale(
					BitmapHelper.convert(getResources().getDrawable(
							R.drawable.star_left)), 512, 512));
			tm.addTexture("star_left", star_left);

			Texture star_right = new Texture(BitmapHelper.rescale(
					BitmapHelper.convert(getResources().getDrawable(
							R.drawable.star_right)), 512, 512));
			tm.addTexture("star_right", star_right);

			Texture star_top = new Texture(BitmapHelper.rescale(
					BitmapHelper.convert(getResources().getDrawable(
							R.drawable.star_top)), 512, 512));
			tm.addTexture("star_top", star_top);
		}

		// TextureManager tm = TextureManager.getInstance();

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

	public void simpleFillTextures(int[] iArr, int offset, int start,
			int count, int width, int height) {

		GLES20.glActiveTexture(GLES20.GL_TEXTURE4 + offset);
		GLES20.glBindTexture(3553, iArr[4 + offset]);
		GLES20.glTexImage2D(3553, 0, 6409, width, height, 0, 6409, 5121,
				ByteBuffer.wrap(mArrayImageContainer.getDataY(), start, count));
		
		width = width >> 1;
		height = height >> 1;
		GLES20.glActiveTexture(GLES20.GL_TEXTURE5 + offset);
		GLES20.glBindTexture(3553, iArr[5 + offset]);
		GLES20.glTexImage2D(3553, 0, 6409, width, height, 0, 6409, 5121,
				ByteBuffer.wrap(mArrayImageContainer.getDataU(), start >> 2,
						count >> 2));

		GLES20.glActiveTexture(GLES20.GL_TEXTURE6 + offset);
		GLES20.glBindTexture(3553, iArr[6 + offset]);
		GLES20.glTexImage2D(3553, 0, 6409, width, height, 0, 6409, 5121,
				ByteBuffer.wrap(mArrayImageContainer.getDataV(), start >> 2,
						count >> 2));
	}
	
	public void initFontBitmap(){  
        String font = "闇�娓叉煋鐨勬枃瀛楁祴璇曪紒";
        fontBitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);  
        Canvas canvas = new Canvas(fontBitmap);  
        //鑳屾櫙棰滆壊  
        canvas.drawColor(Color.TRANSPARENT);  
        Paint p = new Paint();  
        //瀛椾綋璁剧疆  
        String fontType = "瀹嬩綋";  
        Typeface typeface = Typeface.create(fontType, Typeface.BOLD);  
        //娑堥櫎閿娇  
        p.setAntiAlias(true);  
        //瀛椾綋涓虹孩鑹� 
        p.setColor(Color.RED);  
        p.setTypeface(typeface);  
        p.setTextSize(28);  
        //缁樺埗瀛椾綋  
        canvas.drawText(font, 0, 100, p);  
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
