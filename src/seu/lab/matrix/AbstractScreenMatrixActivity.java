package seu.lab.matrix;

import java.util.concurrent.CountDownLatch;
import org.apache.log4j.spi.ErrorCode;
import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.idisplay.DataChannelManager.DataChannelManager;
import com.idisplay.VirtualScreenDisplay.ConnectionActivity;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection;
import com.idisplay.util.BitmapPool;
import com.idisplay.util.ImageContainer;
import com.idisplay.util.Logger;
import com.idisplay.util.RLEImage;
import com.idisplay.util.SettingsManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public abstract class AbstractScreenMatrixActivity extends CardboardActivity{
	
    protected static final String TAG = "ScreenMatrixActivity";

	public static boolean backPressedAndExiting = false;
	protected static AbstractScreenMatrixActivity instance;
	protected static long mTimeStartSession = -1;
	protected static float mZoom;
	protected static Handler screenHandler;
	protected static CountDownLatch screenHandlerLock;
	protected static int zoomCounter = 0;
	protected static final float zoomInRatio = (float) Math.pow(20.0d, 0.15d);
	protected static final float zoomOutRatio = (float) Math.pow(20.0d, -0.15d);
	
	public AbstractScreenMatrixActivity() {

	}
	
	protected void fillTheMenu(Menu menu) {
		menu.clear();
		menu.add(0, 1, 1, (int) R.string.menu_disconnect).setIcon(
				(int) R.drawable.disconnect);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Logger.d("onOptionsItemSelected: " + item.getItemId());
		return itemSelected(item.getItemId());
	}

	protected boolean itemSelected(int i) {
		Logger.d("itemSelected: " + i);
		Intent intent = new Intent();
		switch (i) {
		case 1:
			Logger.d(TAG + ":Reconnecting moving to connection screen");
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

	public static void onCursorImgChange(ImageContainer imageContainer){
		if (instance == null) {
			Logger.w(" is null, skiping new cursor image");
		} else {
			instance.onInstanceCursorImgChange(imageContainer);
		}
	}

	public abstract void onInstanceCursorImgChange(ImageContainer imageContainer);
	
	public static void onCursorPositionChange(int i, int i2){
		if (instance == null) {
			Logger.w(TAG+" is null, skiping cursor image");
		} else {
			instance.onInstanceCursorPositionChange(i, i2);
		}
	}

	public abstract void onInstanceCursorPositionChange(int i, int i2);
	
	public static void onDataAvailable(int i, Object obj){
		if (instance == null) {
			Logger.w(TAG+" is null, skiping new data " + i);
		} else {
			instance.onInstanceDataAvailable(i, obj);
		}
	}

	public abstract void onInstanceDataAvailable(int i, Object obj);

	
	protected static void onDataAvailableHandler(int i, Object obj){
		if (instance == null) {
			Logger.w(TAG+" is null, skiping new data handler " + i);
		} else {
			instance.onInstanceDataAvailableHandler(i, obj);
		}
	}
	
	protected abstract void onInstanceDataAvailableHandler(int i, Object obj);
	
	public static void resetStartState() {
		screenHandlerLock = new CountDownLatch(1);
	}

	public static void setDataChannelManager( DataChannelManager dataChannelManager){
		
	}

	protected abstract Bitmap setDiffImage(Bitmap bitmap, RLEImage rLEImage) throws Exception;

	public static void setSocketConnectionClosedFromRemote() {
		if (screenHandler != null && !backPressedAndExiting) {
			backPressedAndExiting = true;
			Logger.d(TAG
					+ ":Remote Socket closed handling unexpected error ");
			screenHandler.sendEmptyMessage(ErrorCode.ADDRESS_PARSE_FAILURE);
		}
	}

	public static Handler getScreenHandler(){
		return screenHandler;
	}
	
	public static CountDownLatch getScreenHandlerLock(){
		return screenHandlerLock;
	}
	
	public boolean onContextItemSelected(MenuItem menuItem) {
		return itemSelected(menuItem.getItemId()) ? true : super
				.onContextItemSelected(menuItem);
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	    	
    	super.onCreate(savedInstanceState);
		instance = this; 
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
		Logger.d(TAG + ":onDestroy");
	}

	public boolean onKeyDown(int i, KeyEvent keyEvent) {
		if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK)
			return true;
		return super.onKeyDown(i, keyEvent);
	}

	protected void onPause() {
		super.onPause();
		Logger.d(TAG + ":onPause");
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
		Logger.d(TAG + ":onRestart");
	}

	protected void onResume() {
		super.onResume();
		Logger.d(TAG + ":onResume");

		if (IDisplayConnection.ccMngr != null) {
			IDisplayConnection.ccMngr.startStream();
		} else {
			screenHandler.sendEmptyMessage(21);
		}
	}
}
