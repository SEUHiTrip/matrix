package com.idisplay.VirtualScreenDisplay;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import com.idisplay.DataChannelManager.DataChannelManager;
import com.idisplay.VirtualScreenDisplay.IDisplayOpenGLView.OnMeasureListener;
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
	private static VirtualScreenActivity instance;
	private static long mTimeStartSession;
	private static float mZoom;
	public static Handler screenHandler;
	public static CountDownLatch screenHandlerLock;
	static int zoomCounter;
	static final float zoomInRatio;
	static final float zoomOutRatio;
	Activity actContext;
	private volatile Bitmap bmpBgd;
	boolean dowmWasSend;
	float dx;
	float dy;
	private Timer mExitTimer;
	float mScaleFactor;
	private ZoomState mZoomState;
	private IDisplayOpenGLView mZoomView;
	private boolean m_activityWillBeClosed;
	double prevX;
	double prevY;

	static {
		className = "VirtualScreen";
		mTimeStartSession = -1;
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
		this.m_activityWillBeClosed = false;
		this.dowmWasSend = false;
	}

	private void fillTheMenu(Menu menu) {
		menu.clear();
		menu.add(0, 1, 1, (int) R.string.menu_disconnect).setIcon(
				(int) R.drawable.disconnect);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Logger.d("onOptionsItemSelected: " + item.getItemId());
		return itemSelected(item.getItemId());
	}

	private boolean itemSelected(int i) {
		Logger.d("itemSelected: " + i);
		Intent intent = new Intent();
		switch (i) {
		case 1:
			Logger.d(className + ":Reconnecting moving to connection screen");
			if (IDisplayConnection.ccMngr != null) {
				IDisplayConnection.ccMngr.stopProcesses();
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
		if (instance == null || instance.mZoomView == null) {
			Logger.w("VirtualScreenActivity is null, skiping new data " + i);
		} else {
			instance.onDataAvailableHandler(i, obj);
		}
	}

	private void onDataAvailableHandler(int i, Object obj) {
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

	public static void setDataChannelManager(
			DataChannelManager dataChannelManager) {
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

	public static void setSocketConnectionClosedFromRemote() {
		if (screenHandler != null && !backPressedAndExiting) {
			backPressedAndExiting = true;
			Logger.d(className
					+ ":Remote Socket closed handling unexpected error ");
			screenHandler.sendEmptyMessage(ErrorCode.ADDRESS_PARSE_FAILURE);
		}
	}

	public boolean onContextItemSelected(MenuItem menuItem) {
		return itemSelected(menuItem.getItemId()) ? true : super
				.onContextItemSelected(menuItem);
	}

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		instance = this;
		mZoom = SettingsManager.getZoom();
		getWindow().setBackgroundDrawable(null);
		getActionBar().hide();
		getWindow().addFlags(DNSConstants.FLAGS_AA);

		LayoutParams layoutParams = new AbsListView.LayoutParams(-1, -1);

		this.mZoomState = new ZoomState();

		this.mZoomView = new IDisplayOpenGLView(this, null,
				new OnMeasureListener() {
					public void onMeasure() {
						VirtualScreenActivity.this.mZoomState.viewOnMeasure();
					}
				});
		this.mZoomView.setLayoutParams(layoutParams);
		this.mZoomView.setZoomState(this.mZoomState);
		setContentView(this.mZoomView);

		resetZoomState();
		this.actContext = this;
		this.mZoomView.setKeepScreenOn(true);

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
					break;
				case ErrorCode.ADDRESS_PARSE_FAILURE:
					Logger.d(className
							+ ":Unexpected Error occured moving to connection screen");
					break;
				case 17:
					Logger.d(className
							+ ":getString(R.string.LITE_VERSION_SERVER)");
				case 21:
					Logger.d("ccMngr was destroy, need to restore");

				case ScreenConstants.VERSION_MISMATCH:
					Logger.d(className + ":Server version mismatch error");

				case ScreenConstants.SERVER_MODE_NOT_SUPPORTED:
					Logger.d(className + ":Server mode not supported");

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		Logger.d("onCreateOptionsMenu");
		fillTheMenu(menu);
		return true;
	}

	protected void onDestroy() {
		super.onDestroy();
		Logger.d(className + ":onDestroy");
		if (!(this.bmpBgd == null || this.bmpBgd.isRecycled())) {
			this.bmpBgd.recycle();
		}
		if (this.mZoomView != null) {
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
		if (IDisplayConnection.ccMngr != null) {
			IDisplayConnection.ccMngr.stopStream();
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
		if (IDisplayConnection.ccMngr != null) {
			IDisplayConnection.ccMngr.startStream();
		} else {
			screenHandler.sendEmptyMessage(21);
		}
	}
}
