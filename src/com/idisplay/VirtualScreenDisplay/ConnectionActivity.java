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

import seu.lab.matrix.CardboardScreenView;
import seu.lab.matrix.R;
import seu.lab.matrix.ScreenMatrixActivity;

public class ConnectionActivity extends Activity implements UnexpectedErrorListner, ServerDeniedListner {
    public static final int USB_PORT = -1;
    
    public static ConnectionChannelManager ccMngr = null;
    static String className = "ConnectionActivity";
    private static boolean isAccessConfirmed = false;
    private static boolean isVirtualScreenShown = false;
    static LaunchThread launchThread = null;
    public static ListScreenHandler listScreenHandler = null;
    static boolean serverListScreenShown = false;
    private boolean connected = false;
    public static ConnectionMode currentMode;
    protected ServiceInfo info;

    ServerItem usbServerItem = null;
    
    public CountDownLatch lounchLock;
    private boolean user_cancel_action;

    public enum ConnectionType{
    	Single, Duel
    }
    
    public static class ConnectionMode implements Parcelable{
    	ConnectionType type;
    	int width;
    	int height;
    	
    	public static final Parcelable.Creator<ConnectionMode> CREATOR = new Creator<ConnectionMode>(){

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
				height = 960;
				width = 960;
				break;
			case 1:
			default:
				this.type = ConnectionType.Duel;
				height = 1920;
				width = 960;
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
        final /* synthetic */ String val$compName;
        final /* synthetic */ String val$ipAddress;
        final /* synthetic */ long val$port;

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
            
            ConnectionActivity.this.lounchLock = new CountDownLatch(1);
            ScreenMatrixActivity.resetStartState(); // TODO VirtualScreenActivity.resetStartState();
            boolean connectToServer = ccMngr.connectToServer(strArr[0], Integer.parseInt(strArr[1]));
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
            ConnectionActivity.this.connected = false;
            Logger.z("user_cancel_action " + ConnectionActivity.this.user_cancel_action);
            ConnectionActivity.this.user_cancel_action = false;
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
                ConnectionActivity.this.lounchLock.await(30000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Logger.e("count down latch interrupted");
            }
            if (ccMngr.isHandshakeDone()) {
                Logger.i(className + ":Handshake Done");
                ccMngr.setisHandshakeDone(false);
                if (isAccessConfirmed) {
                    Logger.d("access confirmed launch thread");

                    ConnectionActivity.this.connected = true;
                    isAccessConfirmed = false;

                    listScreenHandler.sendEmptyMessage(1);
                    Logger.i(className + ":AccessConfirmed launching Vir Scr");
                    Intent intent = new Intent();
                    intent.setClass(ConnectionActivity.this, ScreenMatrixActivity.class); // TODO VirtualScreenActivity.class);
                    Logger.i(className + ":Before ActivityFinish");
                    ConnectionActivity.this.finish();
                    Logger.i(className + ":After ActivityFinish");
                    ConnectionActivity.this.startActivity(intent);
                    ccMngr.signalVirtualScreenShown();
                    isVirtualScreenShown = true;
                    return;
                }
                Logger.w(className + ":Server denied");

                ConnectionActivity.this.connected = false;
                listScreenHandler.sendEmptyMessage(XMLStreamConstants.END_DOCUMENT);
                listScreenHandler.sendEmptyMessage(1);
                return;
            }
            Logger.i(className + ":Handshake Error");

            if (!ConnectionActivity.this.user_cancel_action) {
                Logger.w(className + ":UNABLE TO CONNECT");
                ConnectionActivity.this.user_cancel_action = false;

                ConnectionActivity.this.connected = false;
                listScreenHandler.sendEmptyMessage(ErrorCode.MISSING_LAYOUT);
                listScreenHandler.sendEmptyMessage(1);
            }
            ConnectionActivity.this.user_cancel_action = false;
        }
    }

    public class ListScreenHandler extends Handler {

        class AnonymousClass_1 implements OnClickListener {
            final /* synthetic */ Dialog val$progress;

            AnonymousClass_1(Dialog dialog) {
                this.val$progress = dialog;
            }

            public void onClick(View view) {
                this.val$progress.dismiss();
                SocketChannelManager.cancelUSBConnect();
                ccMngr.stopProcesses();
                ConnectionActivity.this.user_cancel_action = true;
                ConnectionActivity.this.lounchLock.countDown();
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
//                            ConnectionActivity.this.notifyFound((ServerItem) message.obj);
                            break;
                        case 2:
                        	break;
//                            ConnectionActivity.this.notifyRemoved((ServerItem) message.obj);
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
                    ConnectionActivity.this.connected = false;
                    break;
                case 12:
                    ConnectionActivity.this.connected = false;
                    break;
                case 13:
                    ServerItem serverItem = (ServerItem) message.obj;
                    Logger.d(className + ":In HandleMessage CONNECT_TO_SERVER" + serverItem);
                    if(message.obj == null)break;
                    ConnectionActivity.this.connected = false;
                    ConnectionActivity.this.connectToServer(serverItem.getServerName(), serverItem.getHost(), (long) serverItem.getPort());
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

    public ConnectionActivity() {
        usbServerItem = ServerItem.CreateUsbItem(ConnectionActivity.this);
    }

    public static void connectToServer(ServerItem serverItem) {
    	
    	Logger.d("in static connectToServer: "+ serverItem);
    	
        Message message = new Message();
        message.what = 13;
        message.obj = serverItem;
        listScreenHandler.sendMessage(message);
    }

    private void connectToServer(String str, String str2, long j) {

        VP8Decoder.getInstance().unInitialize();
        ccMngr.setUnexpectedErrorListner(new ErrorAndDataListener());
        ccMngr.setDataChannelConnectionListener(new ErrorAndDataListener());
        
        if (this.connected || str2.equals(StringUtils.EMPTY) || j == 0 || j > 65535) {
            this.connected = false;
            Logger.e(className + ":IP AND PORT VALIDATION FAILED");
            listScreenHandler.sendEmptyMessage(XMLStreamConstants.CDATA);
            listScreenHandler.sendEmptyMessage(1);
            return;
        }
           
        if (Utils.isValidIP(str2)) {
            getWindowManager().getDefaultDisplay();
            SettingsManager.getZoom();
            ccMngr.setDisplayDetails(currentMode.width, currentMode.height, DeviceOrientation.Landscape);
            ccMngr.setServerDeniedListner(this);
            initCodecs();
            new ConnectTask(str2, str, j).execute(new String[]{str2, String.valueOf(j)});
            return;
        }
        
        this.connected = false;
    }

    private void initCodecs() {
    	Logger.d(className+": initCodes");
        VP8Decoder.getInstance().reInit();
    }

    public static boolean isVirtualScreenShown() {
        return isVirtualScreenShown;
    }

    public void OnUnexpectedError(boolean z, String str) {
    	Logger.e("OnUnexpectedError"+str);
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
                Logger.d("currentMode: "+currentMode.width+"x"+currentMode.height);
            }
        }
        if (extras == null || !extras.getBoolean("DENY")) {
        } else {
            Logger.d(className + ":OnCreate got Unexpected error on Virtual screen or reconnect");
            SettingsManager.clearServerAutoconnectOptions();
        }
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService("phone");
        ccMngr = new ConnectionChannelManager();
        Logger.d(className + ":telephonyManager.getDeviceId() " + telephonyManager.getDeviceId());
        ccMngr.setDeviceModel("Android: " + Build.BRAND + " " + Build.MODEL);
        serverListScreenShown = false;
        listScreenHandler = new ListScreenHandler();

    }

    protected void onDestroy() {
        super.onDestroy();
        Logger.d(className + ":OnDestroy");
    }

    protected void onPause() {
        super.onPause();
        Logger.d(className + ":On Pause");
        listScreenHandler.sendEmptyMessage(1);
    }

    protected void onRestart() {
        super.onRestart();
        Logger.d(className + ":onRestart");
    }

    protected void onResume() {
        super.onResume();
        Logger.d(className + ":OnResume");
    }

    public void onServerDenied(boolean z) {
        isAccessConfirmed = z;
        this.lounchLock.countDown();
    }

    protected void onStart() {
        super.onStart();
        Logger.d("onStart");

        if (this.connected) {
            finish();
        }
        
        Logger.d("usbServerItem : "+usbServerItem);
        
        connectToServer(usbServerItem);
    }

    protected void onStop() {
        super.onStop();
        Logger.d(className + ":OnStop");
    }

}
