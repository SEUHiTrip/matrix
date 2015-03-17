package seu.lab.matrix.red;
//package org.opencv.samples.colorblobdetect;
//
//import java.util.List;
//import org.opencv.android.BaseLoaderCallback;
//import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
//import org.opencv.android.LoaderCallbackInterface;
//import org.opencv.android.OpenCVLoader;
//import org.opencv.core.Core;
//import org.opencv.core.CvType;
//import org.opencv.core.Mat;
//import org.opencv.core.MatOfPoint;
//import org.opencv.core.Point;
//import org.opencv.core.Rect;
//import org.opencv.core.Scalar;
//import org.opencv.core.Size;
//import org.opencv.android.CameraBridgeViewBase;
//import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
//import org.opencv.imgproc.Imgproc;
//import org.opencv.imgproc.Moments;
//import org.opencv.samples.colorblobdetect.RemoteManager.OnRemoteChangeListener;
//import org.opencv.samples.colorblobdetect.SimpleCameraBridge.DefaultCvCameraViewListener2;
//import org.opencv.samples.colorblobdetect.SimpleCameraBridge.SimpleCameraBridgeCallback;
//
//import seu.lab.matrix.R;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MotionEvent;
//import android.view.SurfaceView;
//import android.view.View;
//import android.view.Window;
//import android.view.WindowManager;
//import android.view.View.OnTouchListener;
//
//public class SimpleColorBlobDetectionActivity extends Activity implements
//		OnTouchListener, OnRemoteChangeListener {
//	private static final String TAG = "OCVSample::Activity";
//
//	List<android.hardware.Camera.Size> mResolutionList;
//
//	private SimpleCameraBridge mOpenCvCameraView;
//
//	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
//		@Override
//		public void onManagerConnected(int status) {
//			switch (status) {
//			case LoaderCallbackInterface.SUCCESS: {
//				Log.i(TAG, "OpenCV loaded successfully");
//				mOpenCvCameraView.enableView();
//				mOpenCvCameraView
//						.setOnTouchListener(SimpleColorBlobDetectionActivity.this);
//			}
//				break;
//			default: {
//				super.onManagerConnected(status);
//			}
//				break;
//			}
//		}
//	};
//
//	DefaultCvCameraViewListener2 cvCameraViewListener2 = null;
//	 
//	public SimpleColorBlobDetectionActivity() {
//		Log.i(TAG, "Instantiated new " + this.getClass());
//	}
//
//	/** Called when the activity is first created. */
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		Log.i(TAG, "called onCreate");
//		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//
//		setContentView(R.layout.dummy);
//
////		mOpenCvCameraView = (CameraView) findViewById(R.id.color_blob_detection_activity_surface_view);
////		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
//		
//		mOpenCvCameraView = new SimpleCameraBridge(getApplicationContext(), -1, this);
//		cvCameraViewListener2 = mOpenCvCameraView.new DefaultCvCameraViewListener2();
//		mOpenCvCameraView.setCvCameraViewListener(cvCameraViewListener2);
//		mOpenCvCameraView.surfaceCreated(null);
//	}
//
//	@Override
//	public void onPause() {
//		super.onPause();
//		if (mOpenCvCameraView != null)
//			mOpenCvCameraView.disableView();
//	}
//
//	@Override
//	public void onResume() {
//		super.onResume();
//		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
//				mLoaderCallback);
//		
//		mOpenCvCameraView.surfaceChanged(null, 0, 0, 0);
//	}
//
//	public void onDestroy() {
//		super.onDestroy();
//		if (mOpenCvCameraView != null)
//			mOpenCvCameraView.disableView();
//		
//		mOpenCvCameraView.surfaceDestroyed(null);
//	}
//	
//	public boolean onTouch(View v, MotionEvent event) {
////		mResolutionList = mOpenCvCameraView.getResolutionList();
//
////		 for (int i = 0; i < mResolutionList.size(); i++) {
////		 Log.e(TAG, ""+mResolutionList.get(i).height);
////		 }
//
////		mOpenCvCameraView.setResolution(mResolutionList.get(5));
//
//		Log.e(TAG, "getResolution " + mOpenCvCameraView.getResolution().width);
//		return false;
//	}
//
//	@Override
//	public void onUpdatePoints(List<Point> points) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onMove(Point p) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onClick() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onPress() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onRaise() {
//		// TODO Auto-generated method stub
//		
//	}
//
//}
