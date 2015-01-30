package com.idisplay.VirtualScreenDisplay;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ZoomButtonsController;
import com.idisplay.ConnectionChannelManager.ServerInfo;
import com.idisplay.CoreFoundation.MSMTouchObject;
import com.idisplay.CoreFoundation.MSMTouchObject.MSMTouchesPhase;
import com.idisplay.DataChannelManager.DataChannelManager;
import com.idisplay.VirtualScreenDisplay.IDisplayOpenGLView.OnMeasureListener;
import com.idisplay.VirtualScreenDisplay.PanMiniMapGestureDetector.OnPanGestureListener;
import com.idisplay.VirtualScreenDisplay.ScaleGestureDetector.OnScaleGestureListener;
import com.idisplay.VirtualScreenDisplay.ScrollGestureDetector.OnScrollGestureListener;
import com.idisplay.VirtualScreenDisplay.TwoFingersTapDetector.OnTwoFingersTapListener;
import com.idisplay.util.ArrayImageContainer;
import com.idisplay.util.BitmapPool;
import com.idisplay.util.ImageContainer;
import com.idisplay.util.Logger;
import com.idisplay.util.RLEImage;
import com.idisplay.util.SettingsManager;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;
import javax.jmdns.impl.constants.DNSConstants;
import javolution.xml.stream.XMLStreamConstants;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.Filter;

import seu.lab.matrix.MainActivity;
import seu.lab.matrix.R;

public class VirtualScreenActivity extends Activity {
	public static boolean backPressedAndExiting;
	static String className;
	public static OnTouchListener gestureListner;
	private static VirtualScreenActivity instance;
	private static boolean isMultiTouch;
	private static long mTimeStartSession;
	private static float mZoom;
	private static DataChannelManager m_dataChannelMangr;
	private static OnOrientationChangeListner orientationChangeListner;
	static boolean panningStart;
	public static Handler screenHandler;
	public static CountDownLatch screenHandlerLock;
	public static Dialog spinnerProgressDialog;
	static int zoomCounter;
	static final float zoomInRatio;
	static final float zoomOutRatio;
	Activity actContext;
	private volatile Bitmap bmpBgd;
	boolean dowmWasSend;
	float dx;
	float dy;
	private GestureDetector gestureDetector;
	private long lastPressTime;
	private MyGestureDetector listener;
	private Timer mExitTimer;
	private MiniMapProcessor mMiniMapProcessor;
	private float mRightClickX;
	private float mRightClickY;
	float mScaleFactor;
	private ScrollGestureDetector mScrollGestureDetector;
	private float mX;
	private float mY;
	private ZoomState mZoomState;
	private IDisplayOpenGLView mZoomView;
	private boolean m_activityWillBeClosed;
	double prevX;
	double prevY;
	private ScaleGestureDetector scaleGestureDetector;
	private TwoFingersTapDetector twoFingersTapDetector;
	ZoomButtonsController zoomButtons;

	class AnonymousClass_7 implements OnClickListener {
		final/* synthetic */Dialog val$dialog;

		AnonymousClass_7(Dialog dialog) {
			this.val$dialog = dialog;
		}

		public void onClick(View view) {
			if (VirtualScreenActivity.this.bmpBgd == null) {
				screenHandler.sendEmptyMessage(0);
			}
			try {
				this.val$dialog.dismiss();
			} catch (IllegalArgumentException e) {
			}
		}
	}

	class AnonymousClass_8 implements OnClickListener {
		final/* synthetic */Dialog val$dialog;

		AnonymousClass_8(Dialog dialog) {
			this.val$dialog = dialog;
		}

		public void onClick(View view) {
			if (!(VirtualScreenActivity.this.bmpBgd != null
					|| backPressedAndExiting || screenHandler == null)) {
				backPressedAndExiting = true;
			}
			screenHandler.sendEmptyMessage(ErrorCode.ADDRESS_PARSE_FAILURE);
			try {
				this.val$dialog.dismiss();
			} catch (IllegalArgumentException e) {
			}
		}
	}

	class AnonymousClass_9 implements OnClickListener {
		final/* synthetic */Dialog val$dialog;

		AnonymousClass_9(Dialog dialog) {
			this.val$dialog = dialog;
		}

		public void onClick(View view) {
			Logger.d(className + ":Version mismatch exiting..");
			try {
				this.val$dialog.dismiss();
			} catch (IllegalArgumentException e) {
			}
			if (ConnectionActivity.ccMngr != null) {
				ConnectionActivity.ccMngr.stopProcesses();
			}
			Intent intent = new Intent();
			intent.putExtra("DENY", true);
			intent.putExtra("WHERE", "ERROR");
			intent.putExtra("TIME", System.currentTimeMillis());
			intent.setClass(VirtualScreenActivity.this,
					ConnectionActivity.class);
			VirtualScreenActivity.this.startActivity(intent);
			VirtualScreenActivity.this.finish();
		}
	}

	private class MyGestureDetector implements OnGestureListener,
			OnDoubleTapListener, OnScaleGestureListener,
			OnTwoFingersTapListener, OnScrollGestureListener,
			OnPanGestureListener {
		private float startScale;

		private MyGestureDetector() {
			this.startScale = 1.0f;
		}

		public boolean onDoubleTap(MotionEvent motionEvent) {
			Logger.d("onDoubleTap");
			float access$1600 = VirtualScreenActivity.this.recalculatePoint(
					VirtualScreenActivity.this.mZoomView.getWidth(),
					VirtualScreenActivity.this.mZoomView.getServerWidth(),
					motionEvent.getX(),
					VirtualScreenActivity.this.mZoomState.getPanX(), mZoom,
					true);
			float access$16002 = VirtualScreenActivity.this.recalculatePoint(
					VirtualScreenActivity.this.mZoomView.getHeight(),
					VirtualScreenActivity.this.mZoomView.getServerHeight(),
					motionEvent.getY(),
					VirtualScreenActivity.this.mZoomState.getPanY(), mZoom,
					false);
			if (access$1600 < 0.0f || access$16002 < 0.0f) {
				return true;
			}
			if (!(panningStart || VirtualScreenActivity.this.scaleGestureDetector
					.isInProgress())) {
				Logger.d("sendTouchEvent onDoubleTap");
				m_dataChannelMangr.sendTouchEvent(new MSMTouchObject(
						(double) System.currentTimeMillis(),
						MSMTouchesPhase.Touches_began, 1, (double) access$1600,
						(double) access$16002, 0.0d, 0.0d));
				m_dataChannelMangr.sendTouchEvent(new MSMTouchObject(
						(double) System.currentTimeMillis(),
						MSMTouchesPhase.Touches_ended, 2, (double) access$1600,
						(double) access$16002, 0.0d, 0.0d));
				VirtualScreenActivity.this.dowmWasSend = true;
			}
			return true;
		}

		public boolean onDoubleTapEvent(MotionEvent motionEvent) {
			float access$1600 = VirtualScreenActivity.this.recalculatePoint(
					VirtualScreenActivity.this.mZoomView.getWidth(),
					VirtualScreenActivity.this.mZoomView.getServerWidth(),
					motionEvent.getX(),
					VirtualScreenActivity.this.mZoomState.getPanX(), mZoom,
					true);
			float access$16002 = VirtualScreenActivity.this.recalculatePoint(
					VirtualScreenActivity.this.mZoomView.getHeight(),
					VirtualScreenActivity.this.mZoomView.getServerHeight(),
					motionEvent.getY(),
					VirtualScreenActivity.this.mZoomState.getPanY(), mZoom,
					false);
			if (access$1600 < 0.0f || access$16002 < 0.0f) {
				return true;
			}
			new MSMTouchObject((double) System.currentTimeMillis(),
					MSMTouchesPhase.Touches_ended, 2, (double) access$1600,
					(double) access$16002, 0.0d, 0.0d);
			if (!(panningStart || VirtualScreenActivity.this.scaleGestureDetector
					.isInProgress())) {
				Logger.d("sendTouchEvent onDoubleTapEvent");
			}
			return true;
		}

		public boolean onDown(MotionEvent motionEvent) {
			return false;
		}

		public boolean onFling(MotionEvent motionEvent,
				MotionEvent motionEvent2, float f, float f2) {
			return false;
		}

		public void onLongPress(MotionEvent motionEvent) {
			Logger.d("onLongPress");
		}

		public void onPan(float f, float f2) {

		}

		public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
			return true;
		}

		public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
			return true;
		}

		public void onScaleEnd(ScaleGestureDetector scaleGestureDetector,
				boolean z) {
		}

		public void onScroll(int i) {
//			m_dataChannelMangr.sendWheelEvent(i);
		}

		public boolean onScroll(MotionEvent motionEvent,
				MotionEvent motionEvent2, float f, float f2) {
			return false;
		}

		public void onShowPress(MotionEvent motionEvent) {
		}

		public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
			return false;
		}

		public boolean onSingleTapUp(MotionEvent motionEvent) {
			return false;
		}

		public void onTwoFingersTap() {
//			VirtualScreenActivity.this.showAppropriateMenu();
		}
	}

	static {
		m_dataChannelMangr = null;
		gestureListner = null;
		spinnerProgressDialog = null;
		isMultiTouch = false;
		className = "VirtualScreen";
		mTimeStartSession = -1;
		panningStart = false;
		backPressedAndExiting = false;
		zoomOutRatio = (float) Math.pow(20.0d, -0.15d);
		zoomInRatio = (float) Math.pow(20.0d, 0.15d);
		zoomCounter = 0;
	}

	public VirtualScreenActivity() {
		this.bmpBgd = null;
		this.prevX = 0.0d;
		this.prevY = 0.0d;
		this.mScaleFactor = 1.0f;
		this.gestureDetector = null;
		this.scaleGestureDetector = null;
		this.twoFingersTapDetector = null;
		this.mScrollGestureDetector = null;
		this.mMiniMapProcessor = null;
		this.m_activityWillBeClosed = false;
		this.lastPressTime = 0;
		this.dowmWasSend = false;
	}

	private boolean checkForMultiTouch() {
		return getPackageManager().hasSystemFeature(
				"android.hardware.touchscreen.multitouch");
	}

	private void fillTheMenu(ContextMenu contextMenu) {
		contextMenu.clear();
		contextMenu.add(0, ErrorCode.FILE_OPEN_FAILURE, 1,
				R.string.menu_disconnect).setIcon(R.drawable.disconnect);
		contextMenu.add(1, ErrorCode.CLOSE_FAILURE, ErrorCode.FLUSH_FAILURE,
				R.string.menu_pan).setIcon(R.drawable.pan);
		contextMenu.add(1, ErrorCode.ADDRESS_PARSE_FAILURE,
				ErrorCode.CLOSE_FAILURE, R.string.menu_hints).setIcon(
				R.drawable.hints);

		contextMenu.add(1, XMLStreamConstants.END_DOCUMENT,
				ErrorCode.MISSING_LAYOUT, R.string.menu_keyboard).setIcon(
				R.drawable.keyboard);
		if (!DataChannelManager.isMACServer()) {
			contextMenu
					.add(1, XMLStreamConstants.NOTATION_DECLARATION,
							ErrorCode.ADDRESS_PARSE_FAILURE,
							R.string.menu_move_windows).setIcon(
							R.drawable.move_app);
			contextMenu.add(1, XMLStreamConstants.ENTITY_DECLARATION,
					XMLStreamConstants.START_DOCUMENT,
					R.string.menu_start_application).setIcon(
					R.drawable.start_app);
		}
		contextMenu.add(0, 1, XMLStreamConstants.END_DOCUMENT,
				R.string.menu_about).setIcon(R.drawable.about);
		if (!isMultiTouch) {
			contextMenu.add(1, ErrorCode.FLUSH_FAILURE,
					XMLStreamConstants.ENTITY_REFERENCE,
					R.string.menu_resolution_title);
		}
		contextMenu.add(1, XMLStreamConstants.ENTITY_REFERENCE,
				XMLStreamConstants.ATTRIBUTE, R.string.menu_settings).setIcon(
				R.drawable.settings);
		contextMenu.findItem(ErrorCode.CLOSE_FAILURE);

	}

	private void fillTheMenu(Menu menu) {
		menu.clear();
		menu.add(0, 1, 1, (int) R.string.menu_disconnect).setIcon(
				(int) R.drawable.disconnect);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Logger.d("onOptionsItemSelected: "+item.getItemId());

		return itemSelected(item.getItemId());
	}
	
	private boolean itemSelected(int i) {
		Logger.d("itemSelected: "+i);
		Intent intent = new Intent();
		switch (i) {
		case 1:
			Logger.d(className + ":Reconnecting moving to connection screen");
			if (ConnectionActivity.ccMngr != null) {
				ConnectionActivity.ccMngr.stopProcesses();
			}
			intent.putExtra("DENY", true);
			intent.putExtra("WHERE", "DISCONNECT");
			intent.putExtra("TIME", System.currentTimeMillis());
			intent.setClass(this, MainActivity.class);
			startActivity(intent);
			finish();
			break;
		default:
			return false;
		}
		return true;
	}

	public static void onCursorImgChange(ImageContainer imageContainer) {
		if (instance == null || instance.mZoomView == null) {
			Logger.w("VirtualScreenActivity is null, skiping new cursor image");
		} else {
			instance.mZoomView.setCursor(imageContainer);
		}
	}

	public static void onCursorPositionChange(int i, int i2) {
		if (instance == null || instance.mZoomView == null) {
			Logger.w("VirtualScreenActivity is null, skiping cursor image");
		} else {
			instance.mZoomView.setCursorPosition(i, i2);
		}
	}

	public static void onDataAvailable(int i, Object obj) {
		if (spinnerProgressDialog != null && spinnerProgressDialog.isShowing()) {
			mTimeStartSession = System.currentTimeMillis();
			screenHandler.sendEmptyMessage(1);
			if (i == 4) {
				ConnectionActivity.ccMngr.turnOffUdpCursorIfNeeds();
			}
		}
		if (instance == null || instance.mZoomView == null) {
			Logger.w("VirtualScreenActivity is null, skiping new data " + i);
		} else {
			instance.onDataAvailableHandler(i, obj);
		}
	}

	private void onDataAvailableHandler(int i, Object obj) {
		// Logger.d("onDataAvailableHandler: type:"+i+" obj:"+obj);
		switch (i) {
		case 0:
			Logger.e("data without type!");
			break;
		case 1:
			Bitmap bitmap = this.bmpBgd;
			this.bmpBgd = (Bitmap) obj;
			if (!(bitmap == null || bitmap.isRecycled() || bitmap
					.equals(this.bmpBgd))) {
				if (bitmap.getWidth() == this.bmpBgd.getWidth()
						&& bitmap.getHeight() == this.bmpBgd.getHeight()) {
					BitmapPool.putBitmap(bitmap);
				} else {
					bitmap.recycle();
				}
			}
			if (this.bmpBgd != null) {
				FPSCounter.imageRenderComplete(obj.hashCode(),
						this.bmpBgd.hashCode());
			} else {
				FPSCounter.removeImageData(obj.hashCode());
			}
			break;
		case 2:
			this.bmpBgd = setDiffImage(this.bmpBgd, (RLEImage) obj);
			if (this.bmpBgd != null) {
				FPSCounter.imageRenderComplete(obj.hashCode(),
						this.bmpBgd.hashCode());
			} else {
				FPSCounter.removeImageData(obj.hashCode());
			}
			break;
		case 4:
			ArrayImageContainer arrayImageContainer = (ArrayImageContainer) obj;
			if (!this.mZoomView.isYuvRenderer()) {
				this.mZoomView.setYuvRenderer();
			}
			this.mZoomView.setPixels(arrayImageContainer);
			return;
		default:
			Logger.d("Unknown format of picture " + i);
			break;
		}
		if (!this.mZoomView.isBitmapRenderer()) {
			this.mZoomView.setBitmapRenderer();
		}
		if (this.bmpBgd != null) {
			this.mZoomView.setBitmap(this.bmpBgd);
		}
	}

	private float recalculatePoint(int i, int i2, float f, float f2, float f3,
			boolean z) {
		float f4;
		float width = ((float) this.mZoomView.getWidth())
				/ ((float) this.mZoomView.getHeight());
		if (((float) this.mZoomView.getServerWidth())
				/ ((float) this.mZoomView.getServerHeight()) < width) {
			width = (width * ((float) this.mZoomView.getServerHeight()))
					/ ((float) this.mZoomView.getServerWidth());
			f4 = 1.0f;
		} else {
			f4 = (((float) this.mZoomView.getServerWidth()) / width)
					/ ((float) this.mZoomView.getServerHeight());
			width = 1.0f;
		}
		if (!z) {
			width = f4;
		}
		f4 = f >= ((float) (i / 2)) ? ((float) (i / 2))
				+ ((f - ((float) (i / 2))) * width) : ((float) (i / 2))
				- ((((float) (i / 2)) - f) * width);
		width *= f2;
		if (f4 < 0.0f || f4 > ((float) i2)) {
			return -1.0f;
		}
		return ((((((width * 2.0f) + (this.mZoomState.getZoom() - 1.0f)) * ((float) i)) + (f4 * 2.0f)) / (this.mZoomState
				.getZoom() * 2.0f)) / f3) * ((((float) i2) * f3) / ((float) i));
	}

	public static void resetStartState() {
		screenHandlerLock = new CountDownLatch(1);
	}

	private void resetZoomState() {
		this.mZoomState.setPanX(0.5f);
		this.mZoomState.setPanY(0.5f);
		this.mZoomState.setZoom(1.0f);
		zoomCounter = 0;
		this.mZoomState.notifyObservers();
	}

	private void sendCursorData(long j, MSMTouchesPhase mSMTouchesPhase,
			float f, float f2, boolean z) {
		if (m_dataChannelMangr != null) {
			MSMTouchObject mSMTouchObject;
			if (z) {
				mSMTouchObject = new MSMTouchObject((double) j,
						mSMTouchesPhase, 1, this.prevX, this.prevY, this.prevX,
						this.prevY);
			} else {
				mSMTouchObject = new MSMTouchObject((double) j,
						mSMTouchesPhase, 1, (double) f, (double) f2,
						this.prevX, this.prevY);
				this.prevX = (double) f;
				this.prevY = (double) f2;
			}
			m_dataChannelMangr.sendTouchEvent(mSMTouchObject);
		}
	}

	public static void setDataChannelManager(
			DataChannelManager dataChannelManager) {
		m_dataChannelMangr = dataChannelManager;
	}

	private Bitmap setDiffImage(Bitmap bitmap, RLEImage rLEImage) {
		Bitmap bitmap2, bitmap3 = null;
		boolean z;
		if (bitmap == null
				|| (bitmap.getWidth() == rLEImage.getWidth() && bitmap
						.getHeight() == rLEImage.getHeight())) {
			bitmap3 = bitmap;
		} else {

		}
		if (bitmap3 == null) {
			Logger.d("empty bitmap");
			bitmap2 = BitmapPool.getBitmap(rLEImage.getWidth(),
					rLEImage.getHeight());
			if (bitmap2 == null) {
				Logger.d("create image " + rLEImage.getWidth() + "x"
						+ rLEImage.getHeight());
				bitmap2 = Bitmap.createBitmap(rLEImage.getWidth(),
						rLEImage.getHeight(), Config.ARGB_8888);
			}
			bitmap2.eraseColor(-16777216);
			z = true;
		} else {
			bitmap2 = bitmap3;
			z = false;
		}
		try {
			if (rLEImage.patch(bitmap2, z)) {
				return bitmap2;
			}
			Logger.d("Can't process rle data");
			return null;
		} catch (Throwable e) {
			Logger.e("illegal state patching rle", e);
			return null;
		}
	}

	public static void setOrientationChangeListner(
			OnOrientationChangeListner onOrientationChangeListner) {
		orientationChangeListner = onOrientationChangeListner;
	}

	public static void setSocketConnectionClosedFromRemote() {
		if (screenHandler != null && !backPressedAndExiting) {
			backPressedAndExiting = true;
			Logger.d(className
					+ ":Remote Socket closed handling unexpected error ");
			screenHandler.sendEmptyMessage(ErrorCode.ADDRESS_PARSE_FAILURE);
		}
	}

	private void showAppropriateMenu() {
		openOptionsMenu();
	}

	public void onConfigurationChanged(Configuration configuration) {
		super.onConfigurationChanged(configuration);
		Logger.d("configuration changed");
		if (m_dataChannelMangr != null) {
			ServerInfo serverinfo = m_dataChannelMangr.getServerinfo();
			if (serverinfo != null) {
				if (serverinfo.hostDataProtocolInt() == 3
						|| serverinfo.hostDataProtocolInt() == 4
						|| serverinfo.hostDataProtocolInt() == 5) {
					DisplayMetrics displayMetrics = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(
							displayMetrics);
					Logger.d("ScreenResolution " + displayMetrics.heightPixels
							+ "x" + displayMetrics.widthPixels);
					if (configuration.orientation == 2) {
						ConnectionActivity.ccMngr
								.onOrientationChanged(
										1,
										(int) (((float) displayMetrics.heightPixels) / mZoom),
										(int) (((float) displayMetrics.widthPixels) / mZoom));
					} else if (configuration.orientation == 1) {
						ConnectionActivity.ccMngr
								.onOrientationChanged(
										0,
										(int) (((float) displayMetrics.heightPixels) / mZoom),
										(int) (((float) displayMetrics.widthPixels) / mZoom));
					}
				}
				orientationChangeListner.OnOrientationChange();
			}
		}
	}

	public boolean onContextItemSelected(MenuItem menuItem) {
		return itemSelected(menuItem.getItemId()) ? true : super
				.onContextItemSelected(menuItem);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		instance = this;
		mZoom = SettingsManager.getZoom();
		isMultiTouch = checkForMultiTouch();
		getWindow().setBackgroundDrawable(null);
		getActionBar().hide();
		getWindow().addFlags(DNSConstants.FLAGS_AA);

		backPressedAndExiting = false;
		LayoutParams layoutParams = new AbsListView.LayoutParams(-1, -1);
		this.listener = new MyGestureDetector();
		this.mZoomState = new ZoomState();
		this.mMiniMapProcessor = new MiniMapProcessor(this.listener,
				getResources(), this.mZoomState);
		this.mZoomView = new IDisplayOpenGLView(this, this.mMiniMapProcessor,
				new OnMeasureListener() {
					public void onMeasure() {
						VirtualScreenActivity.this.mZoomState.viewOnMeasure();
					}
				});
		this.mZoomView.setLayoutParams(layoutParams);
		this.mZoomView.setZoomState(this.mZoomState);
		setContentView(this.mZoomView);
		this.zoomButtons = new ZoomButtonsController(this.mZoomView);
		this.zoomButtons.setAutoDismissed(true);
		resetZoomState();
		this.actContext = this;
		this.mZoomView.setKeepScreenOn(true);
		this.scaleGestureDetector = new ScaleGestureDetector(this,
				this.listener);
		this.gestureDetector = new GestureDetector(this.listener);
		this.twoFingersTapDetector = new TwoFingersTapDetector(this.listener);
		this.mScrollGestureDetector = new ScrollGestureDetector(this.listener);

		gestureListner = new OnTouchListener() {
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (VirtualScreenActivity.this.mMiniMapProcessor.onTouchEvent(
						motionEvent,
						VirtualScreenActivity.this.mZoomState.getZoom(),
						VirtualScreenActivity.this.mZoomView.getWidth(),
						VirtualScreenActivity.this.mZoomView.getHeight())) {
					Logger.e("miniMap pan");
					return true;
				}
				int onTouchEvent = VirtualScreenActivity.this.mScrollGestureDetector
						.onTouchEvent(motionEvent);
				if ((ScrollGestureDetector.DETECTED & onTouchEvent) != 0) {
					if ((onTouchEvent & ScrollGestureDetector.RESET_SCALE) == 0) {
						return true;
					}
					VirtualScreenActivity.this.scaleGestureDetector
							.resetGesture();
					return true;
				} else if (VirtualScreenActivity.this.twoFingersTapDetector
						.onTouchEvent(motionEvent)) {
					Logger.d("2 fingers Tap");
					return true;
				} else if (VirtualScreenActivity.this.scaleGestureDetector
						.onTouchEvent(motionEvent)) {
					Logger.d(className + ":scaleGestureDetector called");
					return true;
				} else if (!VirtualScreenActivity.this.gestureDetector
						.onTouchEvent(motionEvent)) {
					return VirtualScreenActivity.this.onTouchEvent(motionEvent);
				} else {
					Logger.d(className + ":gestureDetector called");
					return true;
				}
			}
		};
		this.mZoomView.setOnTouchListener(gestureListner);
		this.mZoomView.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
			}
		});
		screenHandler = new Handler() {
			public void handleMessage(Message message) {
				Thread.currentThread()
						.setPriority(XMLStreamConstants.ATTRIBUTE);
				switch (message.what) {
				case ErrorCode.GENERIC_FAILURE:
					if (!VirtualScreenActivity.this.m_activityWillBeClosed) {
						Logger.e("!VirtualScreenActivity.this.m_activityWillBeClosed");
					}
					break;
				case Filter.ACCEPT:
					Logger.e("BigDebug - dismiss");
					if (spinnerProgressDialog != null
							&& spinnerProgressDialog.isShowing()) {
						try {
							Logger.d("BigDebug - real dismiss");
							spinnerProgressDialog.dismiss();
							spinnerProgressDialog = null;
						} catch (Throwable e) {
							Logger.w("close progress bar", e);
						}
					}
					break;
				case ErrorCode.ADDRESS_PARSE_FAILURE:
					Logger.d(className
							+ ":Unexpected Error occured moving to connection screen");
					// if (!VirtualScreenActivity.this.isFinishing()) {
					// screenHandler.sendEmptyMessage(1);
					// if (ConnectionActivity.ccMngr != null) {
					// ConnectionActivity.ccMngr.stopProcesses();
					// }
					// intent = new Intent();
					// intent.putExtra("DENY", true);
					// intent.putExtra("WHERE", "ERROR");
					// intent.putExtra("TIME", System.currentTimeMillis());
					// intent.setClass(VirtualScreenActivity.this,
					// ConnectionActivity.class);
					// VirtualScreenActivity.this.startActivity(intent);
					// VirtualScreenActivity.this.finish();
					// }
					break;
				case 17:
					Logger.d(className
							+ ":getString(R.string.LITE_VERSION_SERVER)");

					// VirtualScreenActivity.this.ShowErrorAlert(VirtualScreenActivity.this.getString(R.string.LITE_VERSION_SERVER),
					// "Error", R.drawable.warning, "Ok");
				case 21:
					Logger.d("ccMngr was destroy, need to restore");
					// intent = new Intent();
					// intent.putExtra("RESTORE", true);
					// intent.putExtra("WHERE", "RESTORE");
					// intent.putExtra("TIME", System.currentTimeMillis());
					// intent.setClass(VirtualScreenActivity.this,
					// ConnectionActivity.class);
					// VirtualScreenActivity.this.startActivity(intent);
					// VirtualScreenActivity.this.finish();
				case ScreenConstants.VERSION_MISMATCH:
					Logger.d(className + ":Server version mismatch error");
					// VirtualScreenActivity.this.showVersionMismatchDialog();
				case ScreenConstants.SERVER_MODE_NOT_SUPPORTED:
					Logger.d(className + ":Server mode not supported");
					// VirtualScreenActivity.this.showServerModeNotSupportedDialog();
				default:
					break;
				}
			}
		};
		if (screenHandlerLock == null) {
			resetStartState();
		}
		screenHandlerLock.countDown();
		if (SettingsManager.getShowHint()) {
			SettingsManager.setShowHint(false);

		} else if (this.bmpBgd == null) {
			Logger.d("BigDebug show, bmpBgd is null");
			screenHandler.sendEmptyMessage(0);
		}

		this.mZoomView.setFocusable(true);
		this.mZoomView.setFocusableInTouchMode(true);

	}

	public void onCreateContextMenu(ContextMenu contextMenu, View view,
			ContextMenuInfo contextMenuInfo) {
		super.onCreateContextMenu(contextMenu, view, contextMenuInfo);
		Logger.d("onCreateContextMenu");
		if (view == this.mZoomView) {
			fillTheMenu(contextMenu);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		Logger.d("onCreateContextMenu");
		fillTheMenu(menu);
		return true;
	}

	protected void onDestroy() {
		super.onDestroy();
		Logger.d(className + ":onDestroy");
		if (!(this.bmpBgd == null || this.bmpBgd.isRecycled())) {
			this.bmpBgd.recycle();
		}
		if (this.zoomButtons != null) {
			this.zoomButtons.setVisible(false);
			this.zoomButtons.setOnZoomListener(null);
		}
		if (this.mZoomView != null) {
			this.mZoomView.setOnTouchListener(null);
			this.mZoomState.deleteObservers();
		}
	}

	public boolean onKeyDown(int i, KeyEvent keyEvent) {
		if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK)
			return true;
		return super.onKeyDown(i, keyEvent);
	}

	protected void onPause() {
		super.onPause();
		Logger.d(className + ":onPause");
		if (ConnectionActivity.ccMngr != null) {
			ConnectionActivity.ccMngr.stopStream();
		}
		BitmapPool.clear();
		if (System.currentTimeMillis() - mTimeStartSession > 300000) {
			Logger.i("showing rate dialog");
			SettingsManager.setSuccessful5MinutesSettions(SettingsManager
					.getSuccessful5MinutesSettions() + 1);
		}
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		fillTheMenu(menu);
		return true;
	}

	protected void onRestart() {
		super.onRestart();
		Logger.d(className + ":onRestart");
	}

	protected void onResume() {
		super.onResume();
		Logger.d(className + ":onResume");
		if (this.mExitTimer != null) {
			this.mExitTimer.cancel();
			this.mExitTimer.purge();
			this.mExitTimer = null;
		}
		if (ConnectionActivity.ccMngr != null) {
			ConnectionActivity.ccMngr.startStream();
		} else {
			screenHandler.sendEmptyMessage(21);
		}
	}

	protected void onStart() {
		super.onStart();
		Logger.d(className + ":onStart");
	}

	protected void onStop() {
		super.onStop();
		Logger.d(className + ":onStop");
	}

	public boolean onTouchEvent(MotionEvent motionEvent) {
		long currentTimeMillis = System.currentTimeMillis();
		float x = motionEvent.getX() / mZoom;
		float y = motionEvent.getY() / mZoom;
		float recalculatePoint = recalculatePoint(this.mZoomView.getWidth(),
				this.mZoomView.getServerWidth(), motionEvent.getX(),
				this.mZoomState.getPanX(), mZoom, true);
		float recalculatePoint2 = recalculatePoint(this.mZoomView.getHeight(),
				this.mZoomView.getServerHeight(), motionEvent.getY(),
				this.mZoomState.getPanY(), mZoom, false);
		if (recalculatePoint < 0.0f || recalculatePoint2 < 0.0f) {
			return super.onTouchEvent(motionEvent);
		}
		boolean z = (this.lastPressTime == 0 || panningStart
				|| currentTimeMillis - this.lastPressTime >= 200
				|| Math.abs(this.mX - x) >= 100.0f || Math.abs(this.mY - y) >= 100.0f) ? false
				: true;
		MSMTouchesPhase mSMTouchesPhase;
		switch (motionEvent.getAction()) {
		case ErrorCode.GENERIC_FAILURE:
			this.mX = x;
			this.mY = y;
			this.lastPressTime = currentTimeMillis;
			this.dowmWasSend = false;
			return super.onTouchEvent(motionEvent);
		case Filter.ACCEPT:
			if (!(panningStart || this.dowmWasSend)) {
				sendCursorData(this.lastPressTime,
						MSMTouchesPhase.Touches_began, recalculatePoint,
						recalculatePoint2, false);
				this.dowmWasSend = true;
			}
			this.dowmWasSend = false;
			this.lastPressTime = 0;
			mSMTouchesPhase = MSMTouchesPhase.Touches_ended;
			if (!(panningStart || this.scaleGestureDetector.isInProgress())) {
				if (this.mRightClickX > 0.0f && this.mRightClickY > 0.0f) {
					Object obj = (Math
							.abs(this.mRightClickX - recalculatePoint) >= 30.0f || Math
							.abs(this.mRightClickY - recalculatePoint2) >= 30.0f) ? null
							: 1;
					if (obj != null) {
						m_dataChannelMangr.sendTouchEvent(new MSMTouchObject(
								(double) System.currentTimeMillis(),
								MSMTouchesPhase.Touches_ended, 1,
								(double) this.mRightClickX,
								(double) this.mRightClickY, 0.0d, 0.0d));
						m_dataChannelMangr
								.sendRightClickEvent(new MSMTouchObject(
										(double) System.currentTimeMillis(),
										MSMTouchesPhase.Touches_began, 1,
										(double) this.mRightClickX,
										(double) this.mRightClickY, 0.0d, 0.0d));
					}
					this.mRightClickX = 0.0f;
					this.mRightClickY = 0.0f;
				}
				sendCursorData(currentTimeMillis, mSMTouchesPhase,
						recalculatePoint, recalculatePoint2, z);
			}
			panningStart = false;
			return super.onTouchEvent(motionEvent);
		case ErrorCode.FLUSH_FAILURE:
			if (z) {
				return super.onTouchEvent(motionEvent);
			}
			if (!(panningStart || this.dowmWasSend)) {
				sendCursorData(this.lastPressTime,
						MSMTouchesPhase.Touches_began, recalculatePoint,
						recalculatePoint2, false);
				this.dowmWasSend = true;
			}
			mSMTouchesPhase = MSMTouchesPhase.Touch_moved;
			this.dx = (x - this.mX) / ((float) this.mZoomView.getWidth());
			this.dy = (y - this.mY) / ((float) this.mZoomView.getHeight());
			if (panningStart) {
				this.mZoomState.setPanX(this.mZoomState.getPanX() - this.dx);
				this.mZoomState.setPanY(this.mZoomState.getPanY() - this.dy);
				this.mZoomState.notifyObservers();
			}
			this.mX = x;
			this.mY = y;
			if (!(panningStart || this.scaleGestureDetector.isInProgress())) {
				sendCursorData(currentTimeMillis, mSMTouchesPhase,
						recalculatePoint, recalculatePoint2, z);
			}
			return super.onTouchEvent(motionEvent);
		case 261:
			return super.onTouchEvent(motionEvent);
		default:
			return super.onTouchEvent(motionEvent);
		}
	}

	public void onVisibilityChanged(boolean z) {
		this.mZoomView.setOnTouchListener(gestureListner);
	}

}
