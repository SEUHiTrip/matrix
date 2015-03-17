package seu.lab.matrix.red;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.LinkedList;
import java.util.List;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.JavaCameraView.JavaCameraSizeAccessor;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import seu.lab.matrix.red.RemoteManager.OnRemoteChangeListener;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class SimpleCameraBridge extends CameraView {

	static final String TAG = "SimpleCameraBridge";

	public class DefaultCvCameraViewListener2 implements CvCameraViewListener2 {

		private Mat mRgba;
		public ColorBlobDetector mDetector;
		public Mat mSpectrum;
		public Scalar mBlobColorRgba = new Scalar(255);
		
		private boolean mIsColorSelected;
		private Scalar mBlobColorHsv;
		
		public List<Point> points = new LinkedList<Point>();

		public void onCameraViewStarted(int width, int height) {
			mRgba = new Mat(height, width, CvType.CV_8UC4);
			mDetector = new ColorBlobDetector();

			mBlobColorHsv = new Scalar(211.5625, 36.5625, 246.125, 0.0);

			mIsColorSelected = mBlobColorHsv != null;
			
			mDetector.setHsvColor(mBlobColorHsv);
//			Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);
			mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
		}

		public void onCameraViewStopped() {
			mRgba.release();
		}

		public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
			mRgba = inputFrame.rgba();
			
			if (mIsColorSelected) {

				points.clear();

				mDetector.process(mRgba);

				List<MatOfPoint> contours = mDetector.getContours();

				for (int i = 0; i < contours.size(); i++) {
					MatOfPoint matOfPoint = contours.get(i);

					// Log.e(TAG, "width " + matOfPoint.size().width + "height "
					// + matOfPoint.size().height);

					if (matOfPoint.size().height < 3)
						continue;

					Mat mat = new Mat();
					matOfPoint.copyTo(mat);
					Moments mMoments = Imgproc.moments(mat);

					double x = mMoments.get_m10() / mMoments.get_m00();
					double y = mMoments.get_m01() / mMoments.get_m00();
//					double rx = (2 * x - mRgba.width()) / mRgba.width();
//					double ry = -(2 * y - mRgba.height()) / mRgba.height();

					Point center = new Point(x, y);
					points.add(center);

//					Log.e(TAG, "rx: " + rx + "  ry: " + ry);
				}

				rm.processData(points);

			}

			return mRgba;
		}
	}
	
	public RemoteManager rm = new RemoteManager();

	OnRemoteChangeListener callback;

	public SimpleCameraBridge(Context context, int cameraId,
			OnRemoteChangeListener _callback) {
		super(context, cameraId);
		callback = _callback;
		rm.registerListener(callback);
	}

	@Override
	protected void deliverAndDrawFrame(CvCameraViewFrame frame) {
		mListener.onCameraFrame(frame);
	}

	@Override
	protected boolean connectCamera(int width, int height) {
		return super.connectCamera(720, 480);
	}

	@Override
	protected boolean initializeCamera(int width, int height) {
		Log.d(TAG, "Initialize java camera");
		boolean result = true;
		synchronized (this) {
			mCamera = null;

			if (mCameraIndex == CAMERA_ID_ANY) {
				Log.d(TAG, "Trying to open camera with old open()");
				try {
					mCamera = Camera.open();
				} catch (Exception e) {
					Log.e(TAG,
							"Camera is not available (in use or does not exist): "
									+ e.getLocalizedMessage());
				}

				if (mCamera == null
						&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
					boolean connected = false;
					for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); ++camIdx) {
						Log.d(TAG, "Trying to open camera with new open("
								+ Integer.valueOf(camIdx) + ")");
						try {
							mCamera = Camera.open(camIdx);
							connected = true;
						} catch (RuntimeException e) {
							Log.e(TAG, "Camera #" + camIdx + "failed to open: "
									+ e.getLocalizedMessage());
						}
						if (connected)
							break;
					}
				}
			} else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
					int localCameraIndex = mCameraIndex;
					if (mCameraIndex == CAMERA_ID_BACK) {
						Log.i(TAG, "Trying to open back camera");
						Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
						for (int camIdx = 0; camIdx < Camera
								.getNumberOfCameras(); ++camIdx) {
							Camera.getCameraInfo(camIdx, cameraInfo);
							if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
								localCameraIndex = camIdx;
								break;
							}
						}
					} else if (mCameraIndex == CAMERA_ID_FRONT) {
						Log.i(TAG, "Trying to open front camera");
						Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
						for (int camIdx = 0; camIdx < Camera
								.getNumberOfCameras(); ++camIdx) {
							Camera.getCameraInfo(camIdx, cameraInfo);
							if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
								localCameraIndex = camIdx;
								break;
							}
						}
					}
					if (localCameraIndex == CAMERA_ID_BACK) {
						Log.e(TAG, "Back camera not found!");
					} else if (localCameraIndex == CAMERA_ID_FRONT) {
						Log.e(TAG, "Front camera not found!");
					} else {
						Log.d(TAG, "Trying to open camera with new open("
								+ Integer.valueOf(localCameraIndex) + ")");
						try {
							mCamera = Camera.open(localCameraIndex);
						} catch (RuntimeException e) {
							Log.e(TAG,
									"Camera #" + localCameraIndex
											+ "failed to open: "
											+ e.getLocalizedMessage());
						}
					}
				}
			}

			if (mCamera == null)
				return false;

			/* Now set camera parameters */
			try {
				Camera.Parameters params = mCamera.getParameters();
				Log.d(TAG, "getSupportedPreviewSizes()");
				List<android.hardware.Camera.Size> sizes = params
						.getSupportedPreviewSizes();

				if (sizes != null) {
					/*
					 * Select the size that fits surface considering maximum
					 * size allowed
					 */
					Size frameSize = calculateCameraFrameSize(sizes,
							new JavaCameraSizeAccessor(), width, height);

					params.setPreviewFormat(ImageFormat.NV21);
					Log.d(TAG,
							"Set preview size to "
									+ Integer.valueOf((int) frameSize.width)
									+ "x"
									+ Integer.valueOf((int) frameSize.height));
					params.setPreviewSize((int) frameSize.width,
							(int) frameSize.height);

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
							&& !android.os.Build.MODEL.equals("GT-I9100"))
						params.setRecordingHint(true);

					List<String> FocusModes = params.getSupportedFocusModes();
					if (FocusModes != null
							&& FocusModes
									.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
						params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
					}

					mCamera.setParameters(params);
					params = mCamera.getParameters();

					mFrameWidth = params.getPreviewSize().width;
					mFrameHeight = params.getPreviewSize().height;

					// if ((getLayoutParams().width ==
					// LayoutParams.MATCH_PARENT) && (getLayoutParams().height
					// == LayoutParams.MATCH_PARENT))
					// mScale = Math.min(((float)height)/mFrameHeight,
					// ((float)width)/mFrameWidth);
					// else
					mScale = 0;

					// if (mFpsMeter != null) {
					// mFpsMeter.setResolution(mFrameWidth, mFrameHeight);
					// }

					int size = mFrameWidth * mFrameHeight;
					size = size
							* ImageFormat.getBitsPerPixel(params
									.getPreviewFormat()) / 8;
					mBuffer = new byte[size];

					mCamera.addCallbackBuffer(mBuffer);
					mCamera.setPreviewCallbackWithBuffer(this);

					mFrameChain = new Mat[2];
					mFrameChain[0] = new Mat(mFrameHeight + (mFrameHeight / 2),
							mFrameWidth, CvType.CV_8UC1);
					mFrameChain[1] = new Mat(mFrameHeight + (mFrameHeight / 2),
							mFrameWidth, CvType.CV_8UC1);

					AllocateCache();

					mCameraFrame = new JavaCameraFrame[2];
					mCameraFrame[0] = new JavaCameraFrame(mFrameChain[0],
							mFrameWidth, mFrameHeight);
					mCameraFrame[1] = new JavaCameraFrame(mFrameChain[1],
							mFrameWidth, mFrameHeight);

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						mSurfaceTexture = new SurfaceTexture(MAGIC_TEXTURE_ID);
						mCamera.setPreviewTexture(mSurfaceTexture);
					} else
						mCamera.setPreviewDisplay(null);

					/* Finally we are ready to start the preview */
					Log.d(TAG, "startPreview");
					mCamera.startPreview();
				} else
					result = false;
			} catch (Exception e) {
				result = false;
				e.printStackTrace();
			}
		}

		return result;
	}

	private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
		Mat pointMatRgba = new Mat();
		Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
		Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL,
				4);

		return new Scalar(pointMatRgba.get(0, 0));
	}
	
}
