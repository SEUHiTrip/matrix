package seu.lab.matrix.red;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
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
import seu.lab.matrix.red.RemoteManager.OnRemoteChangeListener;

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

	public ColorBlobDetector mDetector;
	public Mat mSpectrum;
	public Scalar mBlobColorRgba = new Scalar(255);

	List<android.hardware.Camera.Size> mResolutionList;

	private CameraView mOpenCvCameraView;
	private RemoteManager rm;
	
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

	private boolean mIsColorSelected;

	private Scalar mBlobColorHsv;

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
		
		rm = new RemoteManager();
		rm.registerListener(new OnRemoteChangeListener() {

			@Override
			public void onMove(Point p) {
				Log.i("Remote", "X: " + p.x);
				Log.i("Remote", "Y: " + p.y);
			}

			@Override
			public void onClick() {
				Log.i("Remote", "OnClick!");			
			}

			@Override
			public void onPress() {
				Log.i("Remote", "onPress!");		
			}

			@Override
			public void onRaise() {
				Log.i("Remote", "onRaise!");		
			}
			
		});

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
		mDetector = new ColorBlobDetector();
		mSpectrum = new Mat();

		SPECTRUM_SIZE = new Size(200, 64);
		CONTOUR_COLOR = new Scalar(255, 0, 0, 255);
		
//		FileInputStream fStream = null;
//		try {
//			fStream = new FileInputStream("/sdcard/matrix/track_color");
//			ObjectInputStream oStream = new ObjectInputStream(fStream);
			 mBlobColorHsv = new Scalar(211.5625, 36.5625, 246.125, 0.0);
//			 mBlobColorHsv = new Scalar(255, 248, 255, 255);
			
//			mBlobColorHsv = new Scalar((double[]) oStream.readObject());
//			
//			for (int i=0; i<mBlobColorHsv.val.length; i++) {
//				Log.e("BlobColor", "BlobColor:"+mBlobColorHsv.val[i]);
//			}
//			 oStream.close();
//			 fStream.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (StreamCorruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		mIsColorSelected = mBlobColorHsv != null;
		
		if(mIsColorSelected){
			mDetector.setHsvColor(mBlobColorHsv);
			Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);
			mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
		}
		
	}

	public void onCameraViewStopped() {
		mRgba.release();
	}

	public boolean onTouch(View v, MotionEvent event) {
		mResolutionList = mOpenCvCameraView.getResolutionList();

		mOpenCvCameraView.setResolution(mResolutionList.get(5));

		// Log.e(TAG, "getResolution " + mOpenCvCameraView.getResolution().width);
		return false;
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		mRgba = inputFrame.rgba();

		if (mIsColorSelected) {

			mDetector.process(mRgba);

			List<MatOfPoint> contours = mDetector.getContours();
			
			ArrayList<Point> points = new ArrayList<Point>();
			// Log.i("Remote", ""+contours.size());
			
			for (int i = 0; i < contours.size(); i++) {
				MatOfPoint matOfPoint = contours.get(i);

//				Log.e(TAG, "width " + matOfPoint.size().width + "height "
//						+ matOfPoint.size().height);
				
				if (matOfPoint.size().height < 3)
					continue;

				Mat mat = new Mat();
				matOfPoint.copyTo(mat);
				Moments mMoments = Imgproc.moments(mat);

				double x = mMoments.get_m10() / mMoments.get_m00();
				double y = mMoments.get_m01() / mMoments.get_m00();
				Point center = new Point(x, y);
				
				
				points.add(center);
				
				Core.circle(mRgba, center, 3, new Scalar(0, 255, 0), 6, 8, 0);
			}
			
			rm.processData(points);
			
			
			Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

			Mat colorLabel = mRgba.submat(4, 68, 4, 68);
			colorLabel.setTo(mBlobColorRgba);

			Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70,
					70 + mSpectrum.cols());
			mSpectrum.copyTo(spectrumLabel);
		}
		
		return mRgba;
	}

	private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
		Mat pointMatRgba = new Mat();
		Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
		Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL,
				4);

		return new Scalar(pointMatRgba.get(0, 0));
	}
	
	
	
}
