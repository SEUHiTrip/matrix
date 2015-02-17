package com.idisplay.ConnectionChannelManager;

import android.os.Build.VERSION;
import com.idisplay.Audio.AudioChannel;
import com.idisplay.ConnectionChannelManager.ServerInfo.HostType;
import com.idisplay.CoreFoundation.CFBaseTypes;
import com.idisplay.CoreFoundation.CFBaseTypes.CFTypeID;
import com.idisplay.CoreFoundation.CFDate;
import com.idisplay.CoreFoundation.CFDictionary;
import com.idisplay.CoreFoundation.CFNumber;
import com.idisplay.CoreFoundation.CFStringBaseC;
import com.idisplay.CoreFoundation.CFStringBaseT;
import com.idisplay.CoreFoundation.CNSDictionary;
import com.idisplay.DataChannelManager.DataChannelManager;
import com.idisplay.DataChannelManager.cursor.CursorSocket;
import com.idisplay.DataChannelManager.cursor.CursorSocketFactory;
import com.idisplay.ServerInteractionManager.AccessConfirmedListener;
import com.idisplay.ServerInteractionManager.ContentServiceAccessibleOnPortListener;
import com.idisplay.ServerInteractionManager.PingResponseListener;
import com.idisplay.ServerInteractionManager.ServerInteractionManager;
import com.idisplay.VirtualScreenDisplay.DataChannelConnectionListener;
import com.idisplay.VirtualScreenDisplay.FPSCounter;
import com.idisplay.VirtualScreenDisplay.ScreenConstants;
import com.idisplay.VirtualScreenDisplay.ServerDeniedListner;
import com.idisplay.VirtualScreenDisplay.UnexpectedErrorListner;
import com.idisplay.util.Logger;
import com.idisplay.util.SettingsManager;
import com.idisplay.util.Utils;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.jmdns.impl.constants.DNSConstants;
import org.apache.commons.lang.StringUtils;

import seu.lab.matrix.AbstractScreenMatrixActivity;
import seu.lab.matrix.MainActivity;

public class ConnectionChannelManager implements AccessConfirmedListener, ContentServiceAccessibleOnPortListener, PingResponseListener {
    private static CFDate datePingResponse;
    private static final CFStringBaseC oScreenHeight;
    private static final CFStringBaseC oScreenWidth;
    String className;
    ServerInteractionManager connectionSIM;
    private CursorSocket cursorChannel;
    private int cursorPort;
    private String deviceModel;
    private final CFStringBaseC disableUDPCursor;
    private boolean disableUdpCursor;
    private volatile boolean isHandshakeDone;
    private boolean isServerMac;
    private DataChannelManager m_dataChannelMangr;
    DataChannelConnectionListener m_dataConnectionListener;
    private int m_height;
    private DeviceOrientation m_orientation;
    private PingTask m_pingRequests;
    private Timer m_pingTimer;
    private ServerInfo m_serverInfo;
    private int m_width;
    private final CFStringBaseC moveWindowWithId;
    private final CFStringBaseC orientationChange;
    private final CFStringBaseC pictRender;
    private final CFStringBaseC resolutionChange;
    private final CFStringBaseC sendFrameReceived;
    private final CFStringBaseC sendRequestApplicationsList;
    private final CFStringBaseC sendRequestWindowsList;
    private ServerDeniedListner serverDeniedListner;
    private final CFStringBaseC startApplication;
    UnexpectedErrorListner unexpectedErrorListner;

    public enum DeviceOrientation {
        Portrait,
        Landscape,
        Square,
        Undefined
    }

    private class PingTask extends TimerTask {
        boolean START;
        private int countError;
        private final CFStringBaseC methodPing;
        private final CFStringBaseC paramPing;
        private final CFStringBaseC paramStart;

        private PingTask() {
            this.START = true;
            this.countError = 0;
            this.paramPing = new CFStringBaseC("PING");
            this.paramStart = new CFStringBaseC("START");
            this.methodPing = new CFStringBaseC("Ping");
        }

        public void run() {
            if (this.START) {
                ConnectionChannelManager.this.connectionSIM.sendMessage(this.paramStart, this.methodPing);
                this.START = false;
            } else if (datePingResponse == null) {
                Logger.d(ConnectionChannelManager.this.className + ":checkBeforePing false ::" + this.countError);
                int i = this.countError + 1;
                this.countError = i;
                if (i > 3) {
                    cancel();
                    ConnectionChannelManager.this.m_pingTimer.cancel();
                    ConnectionChannelManager.this.m_pingTimer.purge();
                    ConnectionChannelManager.this.m_pingTimer = null;
                    ConnectionChannelManager.this.unexpectedPingResult();
                    return;
                }
                ConnectionChannelManager.this.connectionSIM.sendMessage(this.paramPing, this.methodPing);
            } else {
                datePingResponse = null;
                this.countError = 0;
                ConnectionChannelManager.this.connectionSIM.sendMessage(this.paramPing, this.methodPing);
            }
        }
    }

    static {
        datePingResponse = null;
        oScreenWidth = new CFStringBaseC("screenWidth");
        oScreenHeight = new CFStringBaseC("screenHeight");
    }

    public ConnectionChannelManager() {
        this.connectionSIM = null;
        this.deviceModel = null;
        this.m_pingRequests = null;
        this.unexpectedErrorListner = null;
        this.m_dataConnectionListener = null;
        this.m_serverInfo = null;
        this.m_width = 0;
        this.m_height = 0;
        this.m_orientation = DeviceOrientation.Landscape;
        this.m_dataChannelMangr = null;
        this.isHandshakeDone = false;
        this.className = "CCMGr";
        this.cursorPort = -1;
        this.isServerMac = false;
        this.disableUdpCursor = false;
        this.pictRender = new CFStringBaseC("PictureRendered");
        this.sendFrameReceived = new CFStringBaseC("FrameReceived");
        this.sendRequestWindowsList = new CFStringBaseC("RequestWindowsList");
        this.sendRequestApplicationsList = new CFStringBaseC("ImportFavorites");
        this.moveWindowWithId = new CFStringBaseC("MoveWindowWithId");
        this.startApplication = new CFStringBaseC("StartApplication");
        this.orientationChange = new CFStringBaseC("OrientationChange");
        this.resolutionChange = new CFStringBaseC("ResolutionChange");
        this.disableUDPCursor = new CFStringBaseC("DisableUDPCursor");
        this.isHandshakeDone = false;
    }

    private void generatePing() {
        if (this.m_pingTimer != null) {
            this.m_pingTimer.cancel();
            this.m_pingTimer.purge();
            this.m_pingTimer = null;
        }
        this.m_pingTimer = new Timer("Connection ping timer");
        this.m_pingRequests = new PingTask();
        try {
            this.m_pingTimer.schedule(this.m_pingRequests, 10000, DNSConstants.CLOSE_TIMEOUT);
        } catch (Throwable e) {
            Logger.w(getClass().getName(), e);
        }
    }

    private CNSDictionary getHandshakeData() {
        CNSDictionary cNSDictionary = new CNSDictionary();
        if (this.cursorChannel != null) {
            cNSDictionary.getDict().insert("udpPort", new CFNumber(this.cursorPort));
        }
        cNSDictionary.getDict().insert("uniqueIdentifier", new CFStringBaseT(Utils.getImei()));
        cNSDictionary.getDict().insert("name", new CFStringBaseT(this.deviceModel));
        cNSDictionary.getDict().insert("systemName", new CFStringBaseT("Android OS"));
        cNSDictionary.getDict().insert("systemVersion", new CFStringBaseT(VERSION.RELEASE));
        cNSDictionary.getDict().insert("model", new CFStringBaseT("Android"));
        cNSDictionary.getDict().insert("localizedModel", new CFStringBaseT("Android"));
        cNSDictionary.getDict().insert("clientVersion", new CFStringBaseT(Utils.getApplicationVersion()));
        cNSDictionary.getDict().insert("clientDataProtocolVersionKey", new CFStringBaseT("4"));
        cNSDictionary.getDict().insert("clientHWModelKey", new CFNumber(9));
        cNSDictionary.getDict().insert("clientOrientationKey", new CFNumber(this.m_orientation.ordinal()));
        cNSDictionary.getDict().insert("MSMClientNumberPreferableCompressionKey", new CFNumber(10));
        if (!SettingsManager.getBoolean(SettingsManager.SMOOTH_VIDEO_KEY)) {
        	Logger.d("FloatingFps set to 1");
            cNSDictionary.getDict().insert("FloatingFps", new CFNumber(1));
        }else {
        	Logger.d("FloatingFps set to smooth");
//            cNSDictionary.getDict().insert("FloatingFps", new CFNumber(30));
		}
        if (this.m_orientation == DeviceOrientation.Landscape) {
            cNSDictionary.getDict().insert(oScreenWidth, new CFNumber(this.m_height));
            cNSDictionary.getDict().insert(oScreenHeight, new CFNumber(this.m_width));
        } else {
            cNSDictionary.getDict().insert(oScreenWidth, new CFNumber(this.m_width));
            cNSDictionary.getDict().insert(oScreenHeight, new CFNumber(this.m_height));
        }
        if (SettingsManager.getSoundEnabled()) {
            cNSDictionary.getDict().insert("AudioCodec", new CFStringBaseT("pcm=stereo,24000"));
        }
        return cNSDictionary;
    }

    private CNSDictionary getScreenResolutionData(int i, int i2) {
        CNSDictionary cNSDictionary = new CNSDictionary();
        cNSDictionary.getDict().insert(oScreenWidth, new CFNumber(i2));
        cNSDictionary.getDict().insert(oScreenHeight, new CFNumber(i));
        return cNSDictionary;
    }

    private void handshake() {
        Logger.d(this.className + ":In Handshake");
        this.connectionSIM.sendMessage(getHandshakeData(), new CFStringBaseT("Handshake"));
    }

    private void unexpectedPingResult() {
        Logger.e(this.className + ":unexpectedPingResult");
        this.unexpectedErrorListner.OnUnexpectedError(true, null);
    }

    public void OnAudioServiceAccessibleOnPort(int i) {
        Logger.d(this.className + ":received OnAudioServiceAccessibleOnPort...port = " + i);
        AudioChannel.getInstance().connectToPort(i);
    }

    public boolean OnRecivedAccessConfirmed(CFBaseTypes cFBaseTypes) {
        boolean z = false;
        if (cFBaseTypes.getType() != CFTypeID.kClassType) {
            return false;
        }
        CNSDictionary cNSDictionary = new CNSDictionary();
        cNSDictionary.initWithCustomClass(cFBaseTypes.customClass());
        CFDictionary dict = cNSDictionary.getDict();
        CFBaseTypes objectForKey = dict.getObjectForKey("isAccess");
        if (objectForKey.getType() == CFTypeID.kPlistBool) {
            z = objectForKey.boolType();
        } else {
            int asInt32 = objectForKey.getAsInt32();
            if (!(asInt32 == 0 || asInt32 == Integer.MIN_VALUE)) {
                z = true;
            }
        }
        this.isHandshakeDone = true;
        Logger.d("handshakeReceived(), accessOkByServer:" + z);
        this.serverDeniedListner.onServerDenied(z);
        CFBaseTypes objectForKey2 = dict.getObjectForKey("serverInfo");
        this.m_serverInfo = new ServerInfo();
        this.m_serverInfo.initWithObject(objectForKey2);

        if (!(processServerVersion(this.m_serverInfo.serverVersion(), this.m_serverInfo.OSName()) || AbstractScreenMatrixActivity.getScreenHandler() == null)) {
            AbstractScreenMatrixActivity.getScreenHandler().sendEmptyMessage(ScreenConstants.VERSION_MISMATCH);
        }
        if (this.m_serverInfo.getHostType() == HostType.MSMWindowCapture) {
            Logger.z("invalid type");
            long currentTimeMillis = System.currentTimeMillis();
            try {
                if (AbstractScreenMatrixActivity.getScreenHandlerLock() == null) {
                	AbstractScreenMatrixActivity.resetStartState();
                }
                AbstractScreenMatrixActivity.getScreenHandlerLock().await(30000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Logger.e("screenHandlerLock interrupted");
            }
            Logger.z("waited " + (System.currentTimeMillis() - currentTimeMillis));
            if (AbstractScreenMatrixActivity.getScreenHandler() != null) {
                Logger.z("screen handler not null");
                AbstractScreenMatrixActivity.getScreenHandler().sendEmptyMessage(ScreenConstants.SERVER_MODE_NOT_SUPPORTED);
            }
        }
        Logger.d(this.className + ":received AccessConfirmed...protocol version = " + this.m_serverInfo.hostDataProtocol());
        Logger.d(this.className + ": Host name: " + this.m_serverInfo.hostName() + " OS: " + this.m_serverInfo.OSName() + "(" + this.m_serverInfo.OSVersion() + ")");
        Logger.d(this.className + ": Server iDisplay version: " + this.m_serverInfo.serverVersion());
        return true;
    }

    public void OnRecivedPingResponse(CFDate cFDate) {
        datePingResponse = cFDate;
    }

    public void OnVideoServiceAccessibleOnPort(int i) {
        Logger.d(this.className + ":received OnVideoServiceAccessibleOnPort...port = " + i);
        this.m_dataChannelMangr = new DataChannelManager(this.m_width, this.m_height, this.m_orientation, this.m_serverInfo);
        this.m_dataConnectionListener.OnDataChannelConnected(this.m_dataChannelMangr);
        this.connectionSIM.setOnRecivedPingResponse(this);
        if (this.cursorChannel != null) {
            this.cursorChannel.start();
        }
        if (this.m_dataChannelMangr.connectToPort(i)) {
            generatePing();
        }
    }

    public boolean connectToServer(String str, int i) {
        this.connectionSIM = new ServerInteractionManager();
        this.connectionSIM.setAccessConfirmedListener(this);
        this.connectionSIM.setContentServiceAccessibleListener(this);
        boolean connectToServer = this.connectionSIM.connectToServer(str, i);
        Logger.d(this.className + ":connectToServer " + connectToServer);
        if (i == -1) {
            Logger.d(this.className + ": connect through usb");
        } else {
            Logger.d(this.className + ": connect to " + str + ":" + i);
        }
        if (connectToServer) {
            if (i != -1) {
                this.cursorPort = i;
                this.cursorChannel = CursorSocketFactory.getUDPSocket(this.cursorPort);
            }
            handshake();
        }
        return connectToServer;
    }

    public String getDeviceModel() {
        return this.deviceModel;
    }

    public String getServerOSName() {
        return this.m_serverInfo == null ? StringUtils.EMPTY : this.m_serverInfo.OSName() == null ? StringUtils.EMPTY : this.m_serverInfo.OSName().contains("MAC") ? "Mac OS " + this.m_serverInfo.OSVersion() : this.m_serverInfo.OSName();
    }

    public boolean isHandshakeDone() {
        return this.isHandshakeDone;
    }

    public void onOrientationChanged(int i, int i2, int i3) {
        if (this.connectionSIM != null) {
            Logger.d("ScreenResolution " + i2 + "x" + i3);
            this.connectionSIM.sendMessage(new CFNumber(i), this.orientationChange);
            this.connectionSIM.sendMessage(getScreenResolutionData(i2, i3), this.resolutionChange);
        }
    }

    public boolean processServerVersion(String str, String str2) {
        if (str2 == null || str == null) {
            return false;
        }
        String[] split = str.split("\\.");
        if (split == null || split.length == 0) {
            return false;
        }
        String str3 = StringUtils.EMPTY;
        int i = 0;
        while (i < split.length) {
            str3 = i == 0 ? str3 + Integer.parseInt(split[i]) + "." : str3 + Integer.parseInt(split[i]);
            i++;
        }
        float parseFloat = Float.parseFloat(str3);
        if (str2.contains("MAC")) {
            this.isServerMac = true;
            FPSCounter.setSimpleFpsAck(true);
            if (((double) parseFloat) >= 2.2d && Runtime.getRuntime().availableProcessors() == 1) {
                this.disableUdpCursor = true;
            }
            if (((double) Float.parseFloat(str3)) < 1.2d) {
                return false;
            }
        } else if (!str2.contains("Windows") && !str2.contains("Microsoft")) {
            return false;
        } else {
            this.isServerMac = false;
            if (((double) parseFloat) >= 2.2d && Runtime.getRuntime().availableProcessors() == 1) {
                this.disableUdpCursor = true;
            }
            FPSCounter.setSimpleFpsAck(((double) parseFloat) >= 2.2d);
            if (((double) parseFloat) < 1.29d) {
                return false;
            }
        }
        return true;
    }

    public void sendFPS(int i) {
        if (!this.isServerMac && this.connectionSIM != null && i == 0) {
            this.connectionSIM.sendMessageFps();
        }
    }

    public void sendFrameReceived() {
        if (this.connectionSIM != null && this.isServerMac) {
            this.connectionSIM.sendMessage(new CFNumber(0), this.sendFrameReceived);
        }
    }

    public void sendMoveWindowWithId(String str) {
        if (this.connectionSIM != null) {
            this.connectionSIM.sendMessage(new CFStringBaseT(str), this.moveWindowWithId);
        }
    }

    public void sendRequestApplicationsList() {
        if (this.connectionSIM != null) {
            this.connectionSIM.sendMessage(new CFNumber(0), this.sendRequestApplicationsList);
        }
    }

    public void sendRequestWindowsList() {
        if (this.connectionSIM != null) {
            this.connectionSIM.sendMessage(new CFNumber(0), this.sendRequestWindowsList);
        }
    }

    public void sendStartApplication(String str, String str2, String str3) {
    	CNSDictionary cNSDictionary = new CNSDictionary();
        cNSDictionary.add(new CFStringBaseT("appid"), new CFStringBaseT(str));
        cNSDictionary.add(new CFStringBaseT("workDir"), new CFStringBaseT(str2));
        cNSDictionary.add(new CFStringBaseT("cmdLine"), new CFStringBaseT(str3));
        this.connectionSIM.sendMessage(cNSDictionary, this.startApplication);
    }

    public void sendTurnOffUDP() {
        if (this.connectionSIM != null) {
            this.connectionSIM.sendMessage(new CFNumber(1), this.disableUDPCursor);
        }
    }

    public void setDataChannelConnectionListener(DataChannelConnectionListener dataChannelConnectionListener) {
        this.m_dataConnectionListener = dataChannelConnectionListener;
    }

    public void setDeviceModel(String str) {
        this.deviceModel = str;
    }

    public void setDisplayDetails(int i, int i2, DeviceOrientation deviceOrientation) {
        this.m_width = i;
        this.m_height = i2;
        Logger.d(this.className + ":Display width " + this.m_width + " height " + this.m_height);
        if (deviceOrientation == DeviceOrientation.Portrait && this.m_height > this.m_width) {
            this.m_orientation = deviceOrientation;
        } else if (deviceOrientation != DeviceOrientation.Undefined) {
            this.m_orientation = DeviceOrientation.Landscape;
        } else if (this.m_height > this.m_width) {
            this.m_orientation = DeviceOrientation.Portrait;
        } else {
            this.m_orientation = DeviceOrientation.Landscape;
        }
    }

    public void setServerDeniedListner(ServerDeniedListner serverDeniedListner) {
        this.serverDeniedListner = serverDeniedListner;
    }

    public void setUnexpectedErrorListner(UnexpectedErrorListner unexpectedErrorListner) {
        this.unexpectedErrorListner = unexpectedErrorListner;
    }

    public void setisHandshakeDone(boolean z) {
        this.isHandshakeDone = z;
    }

    public void signalVirtualScreenShown() {
        if (this.connectionSIM != null) {
            this.connectionSIM.signalVirtualScreenShown();
        }
        if (this.m_dataChannelMangr != null) {
            this.m_dataChannelMangr.signalVirtualScreenShown();
        }
    }

    public void startStream() {
        Logger.d(this.className + ":StartStream");
        if (this.connectionSIM != null) {
            this.connectionSIM.sendStartStream();
        }
    }

    public void stopProcesses() {
        Logger.d(this.className + ":In StopProcess");
        if (this.m_pingRequests != null) {
            this.m_pingRequests.cancel();
            Logger.d(this.className + ":Ping task cancelled");
            this.m_pingRequests = null;
        }
        if (this.connectionSIM != null) {
            this.connectionSIM.stopProcesses();
        }
        if (this.m_dataChannelMangr != null) {
            this.m_dataChannelMangr.stopProcesses();
        }
        if (this.cursorChannel != null) {
            this.cursorChannel.stop();
            this.cursorChannel = null;
        }
        AudioChannel.getInstance().stopProcesses();
    }

    public void stopStream() {
        Logger.d(this.className + ":stopStream");
        if (this.connectionSIM != null) {
            this.connectionSIM.sendStopStream();
        }
    }

    public void turnOffUdpCursorIfNeeds() {
        if (this.disableUdpCursor) {
            Logger.i("turning off udp cursor");
            sendTurnOffUDP();
            if (this.cursorChannel != null) {
                this.cursorChannel.stop();
                this.cursorChannel = null;
            }
        }
    }
}
