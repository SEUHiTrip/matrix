package com.idisplay.VirtualScreenDisplay;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import com.idisplay.ConnectionChannelManager.ConnectionChannelManager;
import com.idisplay.ConnectionChannelManager.ConnectionChannelManager.DeviceOrientation;
import com.idisplay.ServerInteractionManager.SocketChannelManager;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection.ConnectionMode;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection.IDisplayConnectionCallback;
import com.idisplay.util.Logger;
import com.idisplay.util.ServerItem;
import com.idisplay.util.SettingsManager;
import com.idisplay.util.Utils;
import com.idisplay.vp8.VP8Decoder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.jmdns.ServiceInfo;
import javolution.xml.stream.XMLStreamConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.spi.ErrorCode;

import seu.lab.matrix.AbstractScreenMatrixActivity;
import seu.lab.matrix.IdisplayCardboardScreenView;
import seu.lab.matrix.MainActivity;
import seu.lab.matrix.R;
import seu.lab.matrix.ScreenMatrixActivity;

public class ConnectionActivity extends Activity implements
		IDisplayConnectionCallback {
	static String className = "ConnectionActivity";
	private ServerItem usbServerItem;
	private IDisplayConnection iDisplayConnection;
	private ConnectionMode currentMode;
	
	public ConnectionActivity() {
		usbServerItem = ServerItem.CreateUsbItem(ConnectionActivity.this);
	}

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		Logger.d(className + " OnCreate of ConnectionActivity");
		setContentView(R.layout.connection_screen);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.isEmpty()) {
				Logger.d("start with: empty extract");
			} else {
				Logger.d("start with: " + extras.toString());
				currentMode = (ConnectionMode) extras.getParcelable("mode");
				Logger.d("currentMode: " + currentMode.width + "x"
						+ currentMode.height);
			}
		}
		if (extras == null || !extras.getBoolean("DENY")) {
		} else {
			Logger.d(className
					+ ":OnCreate got Unexpected error on Virtual screen or reconnect");
			SettingsManager.clearServerAutoconnectOptions();
		}
		
		iDisplayConnection = new IDisplayConnection(this);

	}

	protected void onDestroy() {
		super.onDestroy();
		Logger.d(className + ":OnDestroy");
	}

	protected void onPause() {
		super.onPause();
		Logger.d(className + ":On Pause");
		iDisplayConnection.listScreenHandler.sendEmptyMessage(1);
	}

	protected void onRestart() {
		super.onRestart();
		Logger.d(className + ":onRestart");
	}

	protected void onResume() {
		super.onResume();
		Logger.d(className + ":OnResume");
	}

	protected void onStart() {
		super.onStart();
		Logger.d("onStart");

		if (iDisplayConnection.isConnected()) {
			finish();
		}

		Logger.d("usbServerItem : " + usbServerItem);

		iDisplayConnection.connectToServer(usbServerItem);
	}

	protected void onStop() {
		super.onStop();
		Logger.d(className + ":OnStop");
	}

	@Override
	public void onIDisplayConnected() {
		Intent intent = new Intent();
		intent.setClass(ConnectionActivity.this, MainActivity.iDisplayerClass); // TODO
																				// VirtualScreenActivity.class);
		Logger.i(className + ":Before ActivityFinish");
		finish();
		Logger.i(className + ":After ActivityFinish");
		startActivity(intent);
	}

}
