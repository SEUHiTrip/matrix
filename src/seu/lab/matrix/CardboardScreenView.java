package seu.lab.matrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import org.apache.log4j.spi.ErrorCode;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import com.idisplay.VirtualScreenDisplay.FPSCounter;
import com.idisplay.VirtualScreenDisplay.IIdisplayViewRenderer;
import com.idisplay.VirtualScreenDisplay.IIdisplayViewRendererContainer;
import com.idisplay.VirtualScreenDisplay.OpenGlBitmapRenderer;
import com.idisplay.VirtualScreenDisplay.OpenGlYuvView;
import com.idisplay.VirtualScreenDisplay.Programs;
import com.idisplay.VirtualScreenDisplay.ZoomState;
import com.idisplay.VirtualScreenDisplay.IDisplayOpenGLView.OnMeasureListener;
import com.idisplay.util.ArrayImageContainer;
import com.idisplay.util.ImageContainer;
import com.idisplay.util.Logger;
import com.idisplay.util.Utils;

public class CardboardScreenView extends CardboardView implements CardboardView.StereoRenderer,
		Observer {

	private final static String TAG = "CardboardScreenView";

	private FloatBuffer a;
	private FloatBuffer b;
	private FloatBuffer background;
	private FloatBuffer backgroundTexture;
	private int[] buffer;
	private FloatBuffer c;
	private FloatBuffer d;
	private FloatBuffer e;
	private String fragmentBackgroundShaderCode;
	private String fragmentBatteryShaderCode;
	private String fragmentCursorShaderCode;
	private ByteBuffer indexBuffer;
	private byte[] indices;
	private boolean isCursorDirty;
	private boolean isDirty;
	private int mBackgroundProgram;
	IIdisplayViewRendererContainer mContainer;
	private int mCursorHeigth;
	private ImageContainer mCursorImage;
	private float mCursorMulX;
	private float mCursorMulY;
	private int mCursorProgram;
	private int mCursorWidth;
	float mCursorX;
	float mCursorY;
	private int mHeight;
	private boolean mNotNativeRatio;
	private OnMeasureListener mOnMeasureListener;
	private int mProgram;
	IIdisplayViewRenderer mRenderer;
	private boolean mRendererChanged;
	private float mRightEdge;
	private boolean mShowCursor;
	private boolean mSingleCodeDevice;
	private ZoomState mState;
	private int mStrideX;
	private int mStrideY;
	private float mTopEdge;
	private int mWidth;
	private float ratio;
	String vertexShaderCode;
	
	public CardboardScreenView(Context context) {
		super(context);
	}

	public CardboardScreenView(Context context,
			OnMeasureListener onMeasureListener) {
		super(context);

		Logger.d(TAG ," CardboardScreenView init");
		
		boolean z = true;
		this.vertexShaderCode = "precision highp float;\nattribute vec4 position;\nattribute vec4 inputTextureCoordinate;\nvarying vec2 textureCoordinate;\nvoid main() {\ngl_Position = position;\ntextureCoordinate = inputTextureCoordinate.xy;\n}";
		this.fragmentCursorShaderCode = "precision highp float;\nvarying highp vec2 textureCoordinate;\nuniform sampler2D cursorFrame;\nvoid main(void) {\ngl_FragColor = (texture2D(cursorFrame, textureCoordinate));\n}";
		this.fragmentBackgroundShaderCode = "precision highp float;\nvarying highp vec2 textureCoordinate;\nuniform sampler2D cursorFrame;\nuniform float rightEdge;\nuniform float topEdge;\nvoid main(void) {\ngl_FragColor = (texture2D(cursorFrame, textureCoordinate));\n}";
		this.fragmentBatteryShaderCode = "precision highp float;\nvarying highp vec2 textureCoordinate;\nuniform float chargePos;\nuniform float rColor;\nuniform float gColor;\nuniform sampler2D batteryFrame;\nvoid main(void) {\nhighp vec4 bt = texture2D(batteryFrame, textureCoordinate); \nif (textureCoordinate[0] < chargePos && bt[3] < 0.8){\ngl_FragColor = vec4(rColor, gColor, 0.0, 0.3);\n} else {\nif (bt[3] > 0.1){\nbt[3] = 0.3;\n}\ngl_FragColor = bt;\n}\n}";
		this.mSingleCodeDevice = false;
		this.isDirty = false;
		this.isCursorDirty = true;
		this.mCursorX = -2.0f;
		this.mCursorY = -2.0f;
		this.mShowCursor = false;
		this.indices = new byte[] { (byte) 0, (byte) 1, (byte) 3, (byte) 0,
				(byte) 3, (byte) 2 };
		this.mNotNativeRatio = false;

		this.mOnMeasureListener = onMeasureListener;
		setEGLContextClientVersion(ErrorCode.FLUSH_FAILURE);
		if (Runtime.getRuntime().availableProcessors() != 1) {
			z = false;
		}
		this.mSingleCodeDevice = z;
		this.indexBuffer = ByteBuffer.allocateDirect(this.indices.length);
		this.indexBuffer.put(this.indices);
		this.indexBuffer.position(0);
		
		setRenderer(this);
		
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		
		this.mContainer = new IIdisplayViewRendererContainer() {
			public int getDataHeight() {
				return mHeight;
			}

			public int getDataStrideX() {
				return mStrideX;
			}

			public int getDataStrideY() {
				return mStrideY;
			}

			public int getDataWidth() {
				return mWidth;
			}

			public void renderDataUpdated(boolean z) {
				if (z) {
					reInitTextureBuffer();
				}
				isDirty = true;
				FPSCounter.imageRenderComplete();
				requestRender();
				if (mSingleCodeDevice) {
					Thread.yield();
				}
			}

			public void setDataHeight(int i) {
				mHeight = i;
			}

			public void setDataStrideX(int i) {
				mStrideX = i;
			}

			public void setDataStrideY(int i) {
				mStrideY = i;
			}

			public void setDataWidth(int i) {
				mWidth = i;
			}
		};
		float[] fArr = new float[] { 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
				0.0f };
		ByteBuffer allocateDirect = ByteBuffer.allocateDirect(fArr.length * 4);
		allocateDirect.order(ByteOrder.nativeOrder());
		this.e = allocateDirect.asFloatBuffer();
		this.e.put(fArr);
		this.e.position(0);
		fArr = new float[] { -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f };
		allocateDirect = ByteBuffer.allocateDirect(fArr.length * 4);
		allocateDirect.order(ByteOrder.nativeOrder());
		this.background = allocateDirect.asFloatBuffer();
		this.background.put(fArr);
		this.background.position(0);
		this.mRenderer = new OpenGlYuvView(this.mContainer);
		
	}


    @Override
    public void onNewFrame(HeadTransform headTransform) {

        checkGLError("onReadyToDraw");
    }


    @Override
    public void onDrawEye(Eye eye) {
    	Log.e(TAG, "on draw eye");
    	
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        checkGLError("mColorParam");
        
        onDrawFrame(null);
        
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
    	
    }

	@Override
	public void onRendererShutdown() {
		//Logger.d(TAG, "onRendererShutdown");
	}

	@Override
	public void onSurfaceChanged(int arg0, int arg1) {
		//Logger.d(TAG, "onSurfaceChanged");
		onSurfaceChanged(null, arg0, arg1);
	}

	@Override
	public void onSurfaceCreated(EGLConfig eGLConfig) {
		//Logger.d(TAG, "onSurfaceCreated");
		onSurfaceCreated(null, eGLConfig);
	}

	private void reInitCursorCalculation(float f, float f2) {
		float f3 = ((float) this.mWidth) / ((float) this.mHeight);
		float f4 = f / f2;
		this.mCursorMulX = 1.0f;
		this.mCursorMulY = 1.0f;
		if (f3 < f4) {
			this.mCursorMulX = (((float) this.mHeight) * f4)
					/ ((float) this.mWidth);
		} else {
			this.mCursorMulY = (((float) this.mWidth) / f4)
					/ ((float) this.mHeight);
		}
	}

	private void reInitTextureBuffer() {
		float width = (float) 960;//TODO getWidth();
		float height = (float) 960;//TODO getHeight();
		reInitTextureBuffer(width, height);
		reInitCursorCalculation(width, height);
		reinitBackgroundTexture(width, height);
	}

	private void reInitTextureBuffer(float f, float f2) {
		float f3;
		float f4 = 0.0f;
		float f5 = ((float) this.mWidth) / ((float) this.mStrideX);
		float f6 = ((float) this.mHeight) / ((float) this.mStrideY);
		float f7 = ((float) this.mWidth) / ((float) this.mHeight);
		float f8 = f / f2;
		if (f7 < f8) {
			f3 = ((float) this.mHeight) * f8;
			f5 = f3 / ((float) this.mStrideX);
			f3 = ((((float) this.mWidth) - f3) / ((float) this.mStrideX)) / 2.0f;
			f5 += f3;
		} else {
			f3 = ((float) this.mWidth) / f8;
			f6 = f3 / ((float) this.mStrideY);
			f3 = ((((float) this.mHeight) - f3) / ((float) this.mStrideY)) / 2.0f;
			f6 += f3;
			f4 = f3;
			f3 = 0.0f;
		}
		this.mNotNativeRatio = ((double) Math.abs(f7 - f8)) > 0.02d;
		this.mRightEdge = f5 + f3;
		this.mTopEdge = f6 + f4;
		float[] fArr = new float[] { f3, f6, f5, f6, f3, f4, f5, f4 };
		ByteBuffer allocateDirect = ByteBuffer.allocateDirect(fArr.length * 4);
		allocateDirect.order(ByteOrder.nativeOrder());
		this.b = allocateDirect.asFloatBuffer();
		this.b.put(fArr);
		this.b.position(0);
	}

	private void reinitBackgroundTexture(float f, float f2) {
		float f3 = 1.0f;
		float f4 = f / f2;
		float f5 = f > f2 ? 1.0f : f4;
		if (f2 <= f) {
			f3 = 1.0f / f4;
		}
		float[] fArr = new float[] { 0.0f, f3, f5, f3, 0.0f, 0.0f, f5, 0.0f };
		ByteBuffer allocateDirect = ByteBuffer.allocateDirect(fArr.length * 4);
		allocateDirect.order(ByteOrder.nativeOrder());
		this.backgroundTexture = allocateDirect.asFloatBuffer();
		this.backgroundTexture.put(fArr);
		this.backgroundTexture.position(0);
	}

	private void rendererChanged() {
		float integer = (10) / 10.0f;
		if (integer == 0.0f) {
			integer = 1.0f;
		}
		float[] fArr = new float[] { ((0.01f * integer) / this.ratio) - 4.0f,
				1.0f - ((0.042f * integer) * this.ratio),
				((0.062f * integer) / this.ratio) - 4.0f,
				1.0f - ((0.042f * integer) * this.ratio),
				((0.01f * integer) / this.ratio) - 4.0f,
				1.0f - ((0.01f * integer) * this.ratio),
				((0.062f * integer) / this.ratio) - 4.0f,
				1.0f - ((integer * 0.01f) * this.ratio) };
		ByteBuffer allocateDirect = ByteBuffer.allocateDirect(fArr.length * 4);
		allocateDirect.order(ByteOrder.nativeOrder());
		this.d = allocateDirect.asFloatBuffer();
		this.d.put(fArr);
		this.d.position(0);
		if (this.buffer != null) {
			GLES20.glDeleteTextures(this.buffer.length, this.buffer, 0);
		}
		int numOfTextures = this.mRenderer.getNumOfTextures() + 4;
		this.buffer = new int[numOfTextures];
		GLES20.glGenTextures(numOfTextures, this.buffer, 0);
		for (int i = 0; i < numOfTextures; i++) {
			int i2 = this.buffer[i];
			Log.v("Texture", "Texture is at " + i2);
			GLES20.glBindTexture(3553, i2);
			GLES20.glTexParameteri(3553, 10242, 33071);
			GLES20.glTexParameteri(3553, 10243, 33071);
			GLES20.glTexParameteri(3553, 10240, 9729);
			GLES20.glTexParameteri(3553, 10241, 9729);
		}
		this.mProgram = Programs.loadProgram(this.vertexShaderCode,
				this.mRenderer.getFragmentShader());
		this.mCursorProgram = Programs.loadProgram(this.vertexShaderCode,
				this.fragmentCursorShaderCode);
		this.mBackgroundProgram = Programs.loadProgram(this.vertexShaderCode,
				this.fragmentBackgroundShaderCode);
		GLES20.glActiveTexture(33985);
		GLES20.glBindTexture(3553, this.buffer[1]);
		Bitmap decodeResource = BitmapFactory.decodeResource(getResources(),
				R.drawable.battery);
		GLES20.glTexImage2D(3553, 0, 6408, decodeResource.getWidth(),
				decodeResource.getHeight(), 0, 6408, 5121,
				Utils.loadTextureFromBitmap(decodeResource));
		this.isCursorDirty = true;
		GLES20.glActiveTexture(33987);
		GLES20.glBindTexture(3553, this.buffer[3]);
		decodeResource = BitmapFactory.decodeResource(getResources(),
				R.drawable.cloth_texture);
		GLES20.glTexImage2D(3553, 0, 6408, decodeResource.getWidth(),
				decodeResource.getHeight(), 0, 6408, 5121,
				Utils.loadTextureFromBitmap(decodeResource));
	}

	private void updateScreenVerticles(float f, float f2, float f3) {
		float[] fArr = new float[] { (-1.0f * f) - (f2 * 2.0f),
				(-1.0f * f) + (f3 * 2.0f), (1.0f * f) - (f2 * 2.0f),
				(-1.0f * f) + (f3 * 2.0f), (-1.0f * f) - (f2 * 2.0f),
				(1.0f * f) + (f3 * 2.0f), (1.0f * f) - (f2 * 2.0f),
				(1.0f * f) + (f3 * 2.0f) };
		ByteBuffer allocateDirect = ByteBuffer.allocateDirect(fArr.length * 4);
		allocateDirect.order(ByteOrder.nativeOrder());
		this.a = allocateDirect.asFloatBuffer();
		this.a.put(fArr);
		this.a.position(0);
	}

	public int getServerHeight() {
		return this.mHeight;
	}

	public int getServerWidth() {
		return this.mWidth;
	}

	public boolean isBitmapRenderer() {
		return this.mRenderer.isBitmapRenderer();
	}

	public boolean isYuvRenderer() {
		return this.mRenderer.isYuvRenderer();
	}

	public void onDrawFrame(GL10 gl10) {
		if (this.mRenderer.isRenderDataAvaliable()) {
			int glGetAttribLocation;
			if (this.mRendererChanged) {
				rendererChanged();
				this.mRendererChanged = false;
			}
			if (this.isCursorDirty) {
				this.isCursorDirty = false;
				GLES20.glActiveTexture(33984);
				GLES20.glBindTexture(3553, this.buffer[0]);
				if (this.mCursorImage == null) {
					Bitmap decodeResource = BitmapFactory.decodeResource(
							getResources(), R.drawable.cursor);
					this.mCursorWidth = decodeResource.getWidth();
					this.mCursorHeigth = decodeResource.getHeight();
					GLES20.glTexImage2D(3553, 0, 6408,
							decodeResource.getWidth(),
							decodeResource.getHeight(), 0, 6408, 5121,
							Utils.loadTextureFromBitmap(decodeResource));
				} else {
					GLES20.glTexImage2D(3553, 0, 6408,
							this.mCursorImage.getWidth(),
							this.mCursorImage.getHeight(), 0, 6408, 5121,
							this.mCursorImage.GetByteBuffer());
				}
			}
			GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			GLES20.glClearDepthf(1.0f);
			GLES20.glClear(16640);
			if (this.mState.getKeyboardShown() || this.mNotNativeRatio) {
				GLES20.glUseProgram(this.mBackgroundProgram);
				GLES20.glUniform1i(GLES20.glGetUniformLocation(
						this.mBackgroundProgram, "cursorFrame"),
						ErrorCode.CLOSE_FAILURE);
				glGetAttribLocation = GLES20.glGetAttribLocation(
						this.mBackgroundProgram, "position");
				GLES20.glEnableVertexAttribArray(glGetAttribLocation);
				GLES20.glVertexAttribPointer(glGetAttribLocation,
						ErrorCode.FLUSH_FAILURE, 5126, false, 0,
						this.background);
				glGetAttribLocation = GLES20.glGetAttribLocation(
						this.mBackgroundProgram, "inputTextureCoordinate");
				GLES20.glEnableVertexAttribArray(glGetAttribLocation);
				GLES20.glVertexAttribPointer(glGetAttribLocation,
						ErrorCode.FLUSH_FAILURE, 5126, false, 0,
						this.backgroundTexture);
				GLES20.glDrawElements(ErrorCode.FILE_OPEN_FAILURE,
						this.indices.length, 5121, this.indexBuffer);
			}
			GLES20.glUseProgram(this.mProgram);
			if (this.isDirty) {
				this.mRenderer.fillTextures(this.mProgram, this.buffer);
			}
			this.isDirty = false;
			glGetAttribLocation = GLES20.glGetAttribLocation(this.mProgram,
					"position");
			int glGetAttribLocation2 = GLES20.glGetAttribLocation(
					this.mProgram, "inputTextureCoordinate");
			GLES20.glEnableVertexAttribArray(glGetAttribLocation);
			GLES20.glVertexAttribPointer(glGetAttribLocation,
					ErrorCode.FLUSH_FAILURE, 5126, false, 0, this.a);
			GLES20.glEnableVertexAttribArray(glGetAttribLocation2);
			GLES20.glVertexAttribPointer(glGetAttribLocation2,
					ErrorCode.FLUSH_FAILURE, 5126, false, 0, this.b);
			GLES20.glUniform1f(
					GLES20.glGetUniformLocation(this.mProgram, "rightEdge"),
					this.mRightEdge);
			GLES20.glUniform1f(
					GLES20.glGetUniformLocation(this.mProgram, "topEdge"),
					this.mTopEdge);
			GLES20.glDrawElements(ErrorCode.FILE_OPEN_FAILURE,
					this.indices.length, 5121, this.indexBuffer);
			if (this.mShowCursor) {
				GLES20.glUseProgram(this.mCursorProgram);
				GLES20.glUniform1i(GLES20.glGetUniformLocation(
						this.mCursorProgram, "cursorFrame"), 0);
				glGetAttribLocation = GLES20.glGetAttribLocation(
						this.mCursorProgram, "position");
				GLES20.glEnableVertexAttribArray(glGetAttribLocation);
				GLES20.glVertexAttribPointer(glGetAttribLocation,
						ErrorCode.FLUSH_FAILURE, 5126, false, 0, this.c);
				glGetAttribLocation = GLES20.glGetAttribLocation(
						this.mCursorProgram, "inputTextureCoordinate");
				GLES20.glEnableVertexAttribArray(glGetAttribLocation);
				GLES20.glVertexAttribPointer(glGetAttribLocation,
						ErrorCode.FLUSH_FAILURE, 5126, false, 0, this.e);
				GLES20.glDrawElements(ErrorCode.FILE_OPEN_FAILURE,
						this.indices.length, 5121, this.indexBuffer);
			}
		}
	}

	@SuppressLint("WrongCall")
	protected void onMeasure(int i, int i2) {
		super.onMeasure(i, i2);
		reInitTextureBuffer((float) MeasureSpec.getSize(i),
				(float) MeasureSpec.getSize(i2));
		reInitCursorCalculation((float) MeasureSpec.getSize(i),
				(float) MeasureSpec.getSize(i2));
		reinitBackgroundTexture((float) MeasureSpec.getSize(i),
				(float) MeasureSpec.getSize(i2));
		setCursorPosition(this.mCursorX, this.mCursorY);
		mOnMeasureListener.onMeasure();
	}

	public void onSurfaceChanged(GL10 gl10, int i, int i2) {
		this.ratio = (float) Math.sqrt(((double) i) / ((double) i2));
		GLES20.glViewport(0, 0, i, i2);
		GLES20.glEnable(3042);
		GLES20.glBlendFunc(1, 771);
		this.mRendererChanged = true;
		this.isDirty = true;
	}

	public void onSurfaceCreated(GL10 gl10, EGLConfig eGLConfig) {
		updateScreenVerticles(1.0f, 0.0f, 0.0f);
		this.mShowCursor = false;
	}

	public void setBitmap(Bitmap bitmap) {
		this.mRenderer.setBitmap(bitmap);
	}

	public void setBitmapRenderer() {
		this.mRenderer = new OpenGlBitmapRenderer(this.mContainer);
		this.mRendererChanged = true;
	}

	public void setCursor(ImageContainer imageContainer) {
		this.mCursorImage = imageContainer;
		this.mCursorWidth = imageContainer.getWidth();
		this.mCursorHeigth = imageContainer.getHeight();
		this.isCursorDirty = true;
		requestRender();
	}

	public void setCursorPosition(float f, float f2) {
		if (this.mRenderer.isRenderDataAvaliable()) {
			float f3 = (((float) (this.mCursorWidth * 2)) / ((float) this.mWidth))
					/ this.mCursorMulX;
			float f4 = (((float) (this.mCursorHeigth * 2)) / ((float) this.mHeight))
					/ this.mCursorMulY;
			this.mCursorX = f;
			this.mCursorY = f2;
			float zoom = (this.mCursorX * this.mState.getZoom())
					- (this.mState.getPanX() * 2.0f);
			f3 = ((f3 + this.mCursorX) * this.mState.getZoom())
					- (this.mState.getPanX() * 2.0f);
			f4 = ((this.mCursorY - f4) * this.mState.getZoom())
					+ (this.mState.getPanY() * 2.0f);
			float zoom2 = (this.mCursorY * this.mState.getZoom())
					+ (this.mState.getPanY() * 2.0f);
			float[] fArr = new float[] { zoom, f4, f3, f4, zoom, zoom2, f3,
					zoom2 };
			ByteBuffer allocateDirect = ByteBuffer
					.allocateDirect(fArr.length * 4);
			allocateDirect.order(ByteOrder.nativeOrder());
			this.c = allocateDirect.asFloatBuffer();
			this.c.put(fArr);
			this.c.position(0);
			requestRender();
			this.mShowCursor = true;
		}
	}

	public void setCursorPosition(int i, int i2) {
		if (!this.mRenderer.isRenderDataAvaliable()) {
			return;
		}
		if (i == -1 && i2 == -1) {
			this.mShowCursor = false;
			return;
		}
		setCursorPosition(((((float) (i * 2)) / ((float) this.mWidth)) - 1.0f)
				/ this.mCursorMulX,
				(1.0f - (((float) (i2 * 2)) / ((float) this.mHeight)))
						/ this.mCursorMulY);
	}

	public void setPixels(ArrayImageContainer arrayImageContainer) {
		this.mRenderer.setPixels(arrayImageContainer);
	}

	public void setYuvRenderer() {
		this.mRenderer = new OpenGlYuvView(this.mContainer);
		this.mRendererChanged = true;
	}

	public void setZoomState(ZoomState zoomState) {
		if (this.mState != null) {
			this.mState.deleteObserver(this);
		}
		this.mState = zoomState;
		this.mState.addObserver(this);
	}

	@Override
	public void update(Observable observable, Object obj) {
		updateScreenVerticles(this.mState.getZoom(), this.mState.getPanX(),
				this.mState.getPanY());
		setCursorPosition(this.mCursorX, this.mCursorY);
		requestRender();
	}
	
    private static void checkGLError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }
    
    private int loadGLShader(int type, int resId) {
        String code = readRawTextFile(resId);
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }
    
    private String readRawTextFile(int resId) {
        InputStream inputStream = getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
