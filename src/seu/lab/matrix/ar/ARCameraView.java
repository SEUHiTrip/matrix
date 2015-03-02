package seu.lab.matrix.ar;

import java.io.FileOutputStream;
import java.util.List;

import org.opencv.android.JavaCameraView;
import org.opencv.android.Utils;
import org.opencv.android.JavaCameraView.CameraWorker;
import org.opencv.android.JavaCameraView.JavaCameraFrame;
import org.opencv.android.JavaCameraView.JavaCameraSizeAccessor;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.ViewGroup.LayoutParams;

public class ARCameraView extends JavaCameraView implements PictureCallback {

	private static final String TAG = "Sample::Tutorial3View";
	private String mPictureFileName;

	public ARCameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public List<String> getEffectList() {
		return mCamera.getParameters().getSupportedColorEffects();
	}

	public boolean isEffectSupported() {
		return (mCamera.getParameters().getColorEffect() != null);
	}

	public String getEffect() {
		return mCamera.getParameters().getColorEffect();
	}

	public void setEffect(String effect) {
		Camera.Parameters params = mCamera.getParameters();
		params.setColorEffect(effect);
		mCamera.setParameters(params);
	}

	public List<Size> getResolutionList() {
		return mCamera.getParameters().getSupportedPreviewSizes();
	}

	public void setResolution(Size resolution) {
		disconnectCamera();
		mMaxHeight = resolution.height;
		mMaxWidth = resolution.width;
		connectCamera(getWidth(), getHeight());
	}

	public Size getResolution() {
		return mCamera.getParameters().getPreviewSize();
	}

	public void takePicture(final String fileName) {
		Log.i(TAG, "Taking picture");
		this.mPictureFileName = fileName;
		// Postview and jpeg are sent in the same buffers if the queue is not
		// empty when performing a capture.
		// Clear up buffers to avoid mCamera.takePicture to be stuck because of
		// a memory issue
		mCamera.setPreviewCallback(null);

		// PictureCallback is implemented by the current class
		mCamera.takePicture(null, null, this);
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		Log.i(TAG, "Saving a bitmap to file");
		// The camera preview was automatically stopped. Start it again.
		mCamera.startPreview();
		mCamera.setPreviewCallback(this);

		// Write the image in a file (in jpeg format)
		try {
			FileOutputStream fos = new FileOutputStream(mPictureFileName);

			fos.write(data);
			fos.close();

		} catch (java.io.IOException e) {
			Log.e("PictureDemo", "Exception in photoCallback", e);
		}

	}

	@Override
	protected void deliverAndDrawFrame(CvCameraViewFrame frame) {
		
		Mat modified;
		if (mListener != null) {
			modified = mListener.onCameraFrame(frame);
		} else {
			modified = frame.rgba();
		}

		boolean bmpValid = true;
		if (modified != null) {
			try {
				Utils.matToBitmap(modified, mCacheBitmap);
			} catch (Exception e) {
				Log.e(TAG, "Mat type: " + modified);
				Log.e(TAG, "Bitmap type: " + mCacheBitmap.getWidth() + "*"
						+ mCacheBitmap.getHeight());
				Log.e(TAG,
						"Utils.matToBitmap() throws an exception: "
								+ e.getMessage());
				bmpValid = false;
			}
		}

		if (bmpValid && mCacheBitmap != null) {
			Canvas canvas = getHolder().lockCanvas();

			int left, top, right, bottom;

			if (canvas != null) {
				canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
				Log.d(TAG, "mStretch value: " + mScale);

				if (mScale != 0) {

					left = (int) ((canvas.getWidth() - mScale
							* mCacheBitmap.getWidth()) / 2);
					top = (int) ((canvas.getHeight() - mScale
							* mCacheBitmap.getHeight()) / 2);
					right = (int) ((canvas.getWidth() - mScale
							* mCacheBitmap.getWidth()) / 2 + mScale
							* mCacheBitmap.getWidth());
					bottom = (int) ((canvas.getHeight() - mScale
							* mCacheBitmap.getHeight()) / 2 + mScale
							* mCacheBitmap.getHeight());

				} else {

					left = (canvas.getWidth() - mCacheBitmap.getWidth()) / 2;
					top = (canvas.getHeight() - mCacheBitmap.getHeight()) / 2;
					right = (canvas.getWidth() - mCacheBitmap.getWidth()) / 2
							+ mCacheBitmap.getWidth();
					bottom = (canvas.getHeight() - mCacheBitmap.getHeight())
							/ 2 + mCacheBitmap.getHeight();

				}

				left /= 2;
				top /= 2;
				right /= 2;
				bottom /= 2;
				
				top += bottom / 2;
				bottom += bottom / 2;

				canvas.drawBitmap(mCacheBitmap,
						new Rect(0, 0, mCacheBitmap.getWidth(),
								mCacheBitmap.getHeight()), new Rect(left,
								top, right, bottom), null);
				
				left += right;
				right += right;
				
				canvas.drawBitmap(mCacheBitmap,
						new Rect(0, 0, mCacheBitmap.getWidth(),
								mCacheBitmap.getHeight()), new Rect(left,
								top, right, bottom), null);
				
				if (mFpsMeter != null) {
					mFpsMeter.measure();
					mFpsMeter.draw(canvas, 20, 30);
				}
				getHolder().unlockCanvasAndPost(canvas);
			}
		}
	}

	@Override
	protected boolean connectCamera(int width, int height) {
		
		mMaxWidth = 1280;
		mMaxHeight = 720;
		
        /* 1. We need to instantiate camera
         * 2. We need to start thread which will be getting frames
         */
        /* First step - initialize camera connection */
        Log.d(TAG, "Connecting to camera");
        if (!initializeCamera(width, height))
            return false;

        return true;
    }

	@Override
	public void onPreviewFrame(byte[] frame, Camera arg1) {
        Log.d(TAG, "Preview Frame received. Frame size: " + frame.length);
        
        mFrameChain[0].put(0, 0, frame);

        if (mCamera != null)
            mCamera.addCallbackBuffer(mBuffer);
        
        deliverAndDrawFrame(mCameraFrame[0]);

	}
}
