package seu.lab.matrix;

import java.nio.FloatBuffer;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;

import javax.jmdns.impl.constants.DNSConstants;

import org.apache.log4j.spi.ErrorCode;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.idisplay.DataChannelManager.DataChannelManager;
import com.idisplay.VirtualScreenDisplay.ConnectionActivity;
import com.idisplay.VirtualScreenDisplay.FPSCounter;
import com.idisplay.VirtualScreenDisplay.ZoomState;
import com.idisplay.VirtualScreenDisplay.IDisplayOpenGLView.OnMeasureListener;
import com.idisplay.util.ArrayImageContainer;
import com.idisplay.util.BitmapPool;
import com.idisplay.util.ImageContainer;
import com.idisplay.util.Logger;
import com.idisplay.util.RLEImage;
import com.idisplay.util.SettingsManager;

public class ScreenMatrixActivity extends AbstractScreenMatrixActivity{

	protected volatile Bitmap bmpBgd = null;
	boolean dowmWasSend = false;
	float dx;
	float dy;
	protected Timer mExitTimer;
	float mScaleFactor = 1.0f;
	protected ZoomState mZoomState;
	protected IdisplayCardboardScreenView mZoomView;
	protected boolean m_activityWillBeClosed = false;
	double prevX = 0.0d;
	double prevY = 0.0d;
	
	@Override
	public void onInstanceCursorImgChange(ImageContainer imageContainer) {
		if (mZoomView == null) {
			Logger.w(" is null, skiping new cursor image");
		} else {
			mZoomView.setCursor(imageContainer);
		}
	}

	@Override
	public void onInstanceCursorPositionChange(int i, int i2) {
		if (mZoomView == null) {
			Logger.w(TAG+" is null, skiping cursor image");
		} else {
			mZoomView.setCursorPosition(i, i2);
		}
	}

	@Override
	public void onInstanceDataAvailable(int i, Object obj) {
		if (mZoomView == null) {
			Logger.w(TAG+" is null, skiping new data " + i);
		} else {
			onInstanceDataAvailableHandler(i, obj);
		}
	}

	@Override
	protected void onInstanceDataAvailableHandler(int i, Object obj) {
		switch (i) {
		case 0:
			Logger.e(TAG+" data without type!");
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

	@Override
	protected Bitmap setDiffImage(Bitmap bitmap, RLEImage rLEImage) {
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mZoom = SettingsManager.getZoom();
		getWindow().setBackgroundDrawable(null);
		if(getActionBar() != null)
			getActionBar().hide();
		getWindow().addFlags(DNSConstants.FLAGS_AA);
		
		LayoutParams layoutParams = new AbsListView.LayoutParams(-1, -1);

		this.mZoomState = new ZoomState();
		
		this.mZoomView = new IdisplayCardboardScreenView(this,
				new OnMeasureListener() {
					public void onMeasure() {
						mZoomState.viewOnMeasure();
					}
				});
		this.mZoomView.setLayoutParams(layoutParams);
		this.mZoomView.setZoomState(this.mZoomState);
        setCardboardView(mZoomView);
        setContentView(mZoomView);
	}
	
	@Override
	protected void onDestroy() {
		if (!(this.bmpBgd == null || this.bmpBgd.isRecycled())) {
			this.bmpBgd.recycle();
		}
		if (this.mZoomView != null) {
			this.mZoomState.deleteObservers();
		}
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (this.mExitTimer != null) {
			this.mExitTimer.cancel();
			this.mExitTimer.purge();
			this.mExitTimer = null;
		}
	}
	
}
