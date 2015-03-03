package com.idisplay.VirtualScreenDisplay;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.jmdns.ServiceInfo;
import javolution.xml.stream.XMLStreamConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.spi.ErrorCode;
import seu.lab.matrix.AbstractScreenMatrixActivity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.View.OnClickListener;
import com.idisplay.ConnectionChannelManager.ConnectionChannelManager;
import com.idisplay.ConnectionChannelManager.ConnectionChannelManager.DeviceOrientation;
import com.idisplay.ServerInteractionManager.SocketChannelManager;
import com.idisplay.util.Logger;
import com.idisplay.util.ServerItem;
import com.idisplay.util.SettingsManager;
import com.idisplay.util.Utils;
import com.idisplay.vp8.VP8Decoder;

public class IDisplayConnection implements UnexpectedErrorListner,
		ServerDeniedListner {
	static String className = "IDisplayConnection";

	public static final int USB_PORT = -1;

	public static ConnectionChannelManager ccMngr = null;
	private static boolean isAccessConfirmed = false;
	private static boolean isVirtualScreenShown = false;
	static LaunchThread launchThread = null;
	public static ListScreenHandler listScreenHandler = null;
	static boolean serverListScreenShown = false;
	private boolean connected = false;
	public static ConnectionMode currentMode = new ConnectionMode(
			ConnectionType.Single.ordinal());
	protected ServiceInfo info;

	public CountDownLatch lounchLock;
	private boolean user_cancel_action;

	private IDisplayConnectionCallback callback;

	public enum ConnectionType {
		Single, Duel, SideBySide
	}

	public static interface IDisplayConnectionCallback {
		void onIDisplayConnected();
	}

	public static class ConnectionMode implements Parcelable {
		public ConnectionType type;
		public int width;
		public int height;

		public static final Parcelable.Creator<ConnectionMode> CREATOR = new Creator<ConnectionMode>() {

			@Override
			public ConnectionMode createFromParcel(Parcel source) {
				// 必须按成员变量声明的顺序读取数据，不然会出现获取数据出错
				ConnectionMode m = new ConnectionMode(source.readInt());
				return m;
			}

			@Override
			public ConnectionMode[] newArray(int arg0) {
				return new ConnectionMode[arg0];
			}
		};

		public ConnectionMode(int type) {
			switch (type) {
			case 0:
				this.type = ConnectionType.Single;
				height = 1024;
				width = 1024;
				break;
			case 1:
				this.type = ConnectionType.Duel;
				height = 2048;
				width = 1024;
				break;
			case 2:
			default:
				this.type = ConnectionType.SideBySide;
				height = 2048;
				width = 1024;
				break;
			}
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(type.ordinal());
		}
	}

	class ConnectTask extends AsyncTask<String, Void, Boolean> {
		final/* synthetic */String val$compName;
		final/* synthetic */String val$ipAddress;
		final/* synthetic */long val$port;

		ConnectTask(String str, String str2, long j) {
			this.val$ipAddress = str;
			this.val$compName = str2;
			this.val$port = j;
		}

		protected Boolean doInBackground(String... strArr) {
			if (strArr.length < 2) {
				return Boolean.valueOf(false);
			}
			Logger.d("ConnectTask: start doInBackground");

			lounchLock = new CountDownLatch(1);
			AbstractScreenMatrixActivity.resetStartState(); // TODO
															// VirtualScreenActivity.resetStartState();
			boolean connectToServer = ccMngr.connectToServer(strArr[0],
					Integer.parseInt(strArr[1]));
			Logger.d("connect to server " + connectToServer);

			return Boolean.valueOf(connectToServer);
		}

		protected void onPostExecute(Boolean bool) {
			if (bool.booleanValue()) {
				Logger.d("ConnectTask: onPostExecute : connected");

				SettingsManager.setServerIp(this.val$ipAddress);
				SettingsManager.setServerName(this.val$compName);
				SettingsManager.setServerPort(this.val$port);
				launchThread = new LaunchThread();
				launchThread.start();
				return;
			}
			connected = false;
			Logger.z("user_cancel_action " + user_cancel_action);
			user_cancel_action = false;
		}
	}

	class LaunchThread extends Thread {
		final int HANDSHAKE_TIMEOUT;

		public LaunchThread() {
			super("Launch Thread");
			this.HANDSHAKE_TIMEOUT = 30000;
			setPriority(XMLStreamConstants.ATTRIBUTE);
		}

		public void run() {
			Logger.d(className + ":In launch thread to launch Virtual screen");
			listScreenHandler.sendEmptyMessage(0);
			try {
				lounchLock.await(30000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				Logger.e("count down latch interrupted");
			}
			if (ccMngr.isHandshakeDone()) {
				Logger.i(className + ":Handshake Done");
				ccMngr.setisHandshakeDone(false);
				if (isAccessConfirmed) {
					Logger.d("access confirmed launch thread");

					connected = true;
					isAccessConfirmed = false;

					listScreenHandler.sendEmptyMessage(1);
					Logger.i(className + ":AccessConfirmed launching Vir Scr");

					callback.onIDisplayConnected();

					ccMngr.signalVirtualScreenShown();
					isVirtualScreenShown = true;
					return;
				}
				Logger.w(className + ":Server denied");

				connected = false;
				listScreenHandler
						.sendEmptyMessage(XMLStreamConstants.END_DOCUMENT);
				listScreenHandler.sendEmptyMessage(1);
				return;
			}
			Logger.i(className + ":Handshake Error");

			if (!user_cancel_action) {
				Logger.w(className + ":UNABLE TO CONNECT");
				user_cancel_action = false;

				connected = false;
				listScreenHandler.sendEmptyMessage(ErrorCode.MISSING_LAYOUT);
				listScreenHandler.sendEmptyMessage(1);
			}
			user_cancel_action = false;
		}
	}

	public class ListScreenHandler extends Handler {

		class AnonymousClass_1 implements OnClickListener {
			final/* synthetic */Dialog val$progress;

			AnonymousClass_1(Dialog dialog) {
				this.val$progress = dialog;
			}

			public void onClick(View view) {
				this.val$progress.dismiss();
				SocketChannelManager.cancelUSBConnect();
				ccMngr.stopProcesses();
				user_cancel_action = true;
				lounchLock.countDown();
			}
		}

		public void handleMessage(Message message) {
			switch (message.what) {
			case 0:
				
				break;
			case 1:
				
				break;
			case 2:
				switch (message.arg1) {
				case 1:
					serverListScreenShown = true;
					// notifyFound((ServerItem) message.obj);
					break;
				case 2:
					break;
				// notifyRemoved((ServerItem) message.obj);
				default:
					break;
				}
			case 3:
				break;
			case 4:
				break;
			case 5:
				Logger.d("SHOW_UNABLE_TO_CONNECT_ERROR");
				SettingsManager.clearServerAutoconnectOptions();
				Logger.e(className + ":Unable TO Connect calling stopProcess");
				ccMngr.stopProcesses();
				break;
			case 8:
				Logger.d("SERVER_DENIED");
				SettingsManager.clearServerAutoconnectOptions();

				Logger.e(className + ":Server Denied calling stopProcess");
				ccMngr.stopProcesses();
				break;
			case 10:
				Logger.e(className + ":Connection Failed calling stopProcess");
				ccMngr.stopProcesses();
				break;
			case 11:
				connected = false;
				break;
			case 12:
				connected = false;
				break;
			case 13:
				ServerItem serverItem = (ServerItem) message.obj;
				Logger.d(className + ":In HandleMessage CONNECT_TO_SERVER"
						+ serverItem);
				if (message.obj == null)
					break;
				connected = false;
				connectToServer(serverItem.getServerName(),
						serverItem.getHost(), (long) serverItem.getPort());
				break;
			case 16:
				break;
			case 18:
				Logger.e(className + ":Connection Failed calling stopProcess");
				ccMngr.stopProcesses();
				break;
			default:
				break;
			}
		}
	}

	public IDisplayConnection(IDisplayConnectionCallback _callback) {
		callback = _callback;
        ccMngr = new ConnectionChannelManager();
		ccMngr.setDeviceModel("Android: " + Build.BRAND + " " + Build.MODEL);
		serverListScreenShown = false;
		listScreenHandler = new ListScreenHandler();
	}

	public static void connectToServer(ServerItem serverItem, ConnectionMode mode) {
		Logger.d("in static connectToServer: " + serverItem);
		currentMode = mode;
		Message message = new Message();
		message.what = 13;
		message.obj = serverItem;
		listScreenHandler.sendMessage(message);
	}

	private void connectToServer(String str, String str2, long j) {
		
		VP8Decoder.getInstance().unInitialize();
		ccMngr.setUnexpectedErrorListner(new ErrorAndDataListener());
		ccMngr.setDataChannelConnectionListener(new ErrorAndDataListener());

		if (this.connected || str2.equals(StringUtils.EMPTY) || j == 0
				|| j > 65535) {
			this.connected = false;
			Logger.e(className + ":IP AND PORT VALIDATION FAILED");
			listScreenHandler.sendEmptyMessage(XMLStreamConstants.CDATA);
			listScreenHandler.sendEmptyMessage(1);
			return;
		}

		if (Utils.isValidIP(str2)) {
			SettingsManager.getZoom();
			ccMngr.setDisplayDetails(currentMode.width, currentMode.height,
					DeviceOrientation.Landscape);
			ccMngr.setServerDeniedListner(this);
			initCodecs();
			new ConnectTask(str2, str, j).execute(new String[] { str2,
					String.valueOf(j) });
			return;
		}

		this.connected = false;
	}

	private void initCodecs() {
		Logger.d(className + ": initCodes");
		VP8Decoder.getInstance().reInit();
	}

	public static boolean isVirtualScreenShown() {
		return isVirtualScreenShown;
	}

	public void OnUnexpectedError(boolean z, String str) {
		Logger.e("OnUnexpectedError" + str);
	}

	public void onServerDenied(boolean z) {
		isAccessConfirmed = z;
		this.lounchLock.countDown();
	}

	public boolean isConnected(){
		return connected;
	}
	
}
