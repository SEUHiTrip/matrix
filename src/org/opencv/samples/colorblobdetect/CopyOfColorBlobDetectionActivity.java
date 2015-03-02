package org.opencv.samples.colorblobdetect;

import java.util.List;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import seu.lab.matrix.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;

public class CopyOfColorBlobDetectionActivity extends Activity implements
		OnTouchListener, CvCameraViewListener2 {
	private static final String TAG = "OCVSample::Activity";

	private Mat mRgba;
	private Size SPECTRUM_SIZE;
	private Scalar CONTOUR_COLOR;

	List<android.hardware.Camera.Size> mResolutionList;

	private CameraView mOpenCvCameraView;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				mOpenCvCameraView.enableView();
				mOpenCvCameraView
						.setOnTouchListener(CopyOfColorBlobDetectionActivity.this);
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	public CopyOfColorBlobDetectionActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.color_track_view);

		mOpenCvCameraView = (CameraView) findViewById(R.id.color_blob_detection_activity_surface_view);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);

	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		ColorBlobDetectionActivity.mDetector = new ColorBlobDetector();

		SPECTRUM_SIZE = new Size(200, 64);
		CONTOUR_COLOR = new Scalar(255, 0, 0, 255);

		ColorBlobDetectionActivity.mDetector
				.setHsvColor(ColorBlobDetectionActivity.mBlobColorHsv);

	}

	public void onCameraViewStopped() {
		mRgba.release();
	}

	public boolean onTouch(View v, MotionEvent event) {
		mResolutionList = mOpenCvCameraView.getResolutionList();

		// for (int i = 0; i < mResolutionList.size(); i++) {
		// Log.e(TAG, ""+mResolutionList.get(i).height);
		// }

		mOpenCvCameraView.setResolution(mResolutionList.get(5));

		Log.e(TAG, "getResolution " + mOpenCvCameraView.getResolution().width);
		return false;
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mRgba = inputFrame.rgba();

		if (ColorBlobDetectionActivity.mIsColorSelected) {

			ColorBlobDetectionActivity.mDetector.process(mRgba);

			List<MatOfPoint> contours = ColorBlobDetectionActivity.mDetector
					.getContours();

			for (int i = 0; i < contours.size(); i++) {
				MatOfPoint matOfPoint = contours.get(i);

				Log.e(TAG, "width " + matOfPoint.size().width + "height "
						+ matOfPoint.size().height);

				if(matOfPoint.size().height < 3)continue;
				
				Mat mat = new Mat();
				matOfPoint.copyTo(mat);
				Moments mMoments = Imgproc.moments(mat);

				double x = mMoments.get_m10() / mMoments.get_m00();
				double y = mMoments.get_m01() / mMoments.get_m00();
				Point center = new Point(x, y);
				Core.circle(mRgba, center, 10, new Scalar(0, 255, 0), 20, 8, 0);
			}

			Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

			Mat colorLabel = mRgba.submat(4, 68, 4, 68);
			colorLabel.setTo(ColorBlobDetectionActivity.mBlobColorRgba);

			Mat spectrumLabel = mRgba.submat(4,
					4 + ColorBlobDetectionActivity.mSpectrum.rows(), 70,
					70 + ColorBlobDetectionActivity.mSpectrum.cols());
			ColorBlobDetectionActivity.mSpectrum.copyTo(spectrumLabel);
		}

		return mRgba;
	}

}
