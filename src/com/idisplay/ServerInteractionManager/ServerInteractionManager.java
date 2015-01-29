package com.idisplay.ServerInteractionManager;

import com.idisplay.CoreFoundation.CFBaseTypes;
import com.idisplay.CoreFoundation.CFBaseTypes.CFTypeID;
import com.idisplay.CoreFoundation.BinaryPList;
import com.idisplay.CoreFoundation.CFArray;
import com.idisplay.CoreFoundation.CFCustomClass;
import com.idisplay.CoreFoundation.CFDate;
import com.idisplay.CoreFoundation.CFDictionary;
import com.idisplay.CoreFoundation.CFStringBaseT;
import com.idisplay.CoreFoundation.CKeyedArchiver;
import com.idisplay.CoreFoundation.CKeyedUnarchiver;
import com.idisplay.CoreFoundation.CMSMMessagePacket;
import com.idisplay.CoreFoundation.Tester;
import com.idisplay.CoreFoundation.CMSMMessagePacket.MSMMessageType;
import com.idisplay.CoreFoundation.MSMKeyboardEventPacket;
import com.idisplay.CoreFoundation.MSMTouchEventPacket;
import com.idisplay.CoreFoundation.MSMWheelEventPacket;
import com.idisplay.GlobalCommunicationStructures.KeyboardEventStructure;
import com.idisplay.GlobalCommunicationStructures.MSMTouchEvent;
import com.idisplay.util.Logger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerInteractionManager implements AcceptSocketDataListener {
    private static final byte[] mFpsArray;
    private AccessConfirmedListener accessConfirmedListener;
    String className;
    private ContentServiceAccessibleOnPortListener contentServiceAccessibleListener;
    private PingResponseListener pingResponseListener;
    private SocketChannelManager socketChannelManager;
    ExecutorService telemetryQueue;
    private VideoDataReceivedListener videoDataAvailableListener;

    class AnonymousClass_1 implements Runnable {
        final /* synthetic */ byte[] val$actualData;
        final /* synthetic */ int val$incomingDataLen;

        AnonymousClass_1(int i, byte[] bArr) {
            this.val$incomingDataLen = i;
            this.val$actualData = bArr;
        }

        public void run() {
            ServerInteractionManager.this.processTelemetry(this.val$incomingDataLen, this.val$actualData);
        }
    }

    static {
        mFpsArray = new byte[]{(byte) 74, (byte) 2, (byte) 0, (byte) 0, (byte) 98, (byte) 112, (byte) 108, (byte) 105, (byte) 115, (byte) 116, (byte) 48, (byte) 48, (byte) -44, (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 52, (byte) 55, (byte) 89, (byte) 36, (byte) 97, (byte) 114, (byte) 99, (byte) 104, (byte) 105, (byte) 118, (byte) 101, (byte) 114, (byte) 88, (byte) 36, (byte) 111, (byte) 98, (byte) 106, (byte) 101, (byte) 99, (byte) 116, (byte) 115, (byte) 84, (byte) 36, (byte) 116, (byte) 111, (byte) 112, (byte) 88, (byte) 36, (byte) 118, (byte) 101, (byte) 114, (byte) 115, (byte) 105, (byte) 111, (byte) 110, (byte) 95, (byte) 16, (byte) 15, (byte) 78, (byte) 83, (byte) 75, (byte) 101, (byte) 121, (byte) 101, (byte) 100, (byte) 65, (byte) 114, (byte) 99, (byte) 104, (byte) 105, (byte) 118, (byte) 101, (byte) 114, (byte) -87, (byte) 51, (byte) 8, (byte) 25, (byte) 32, (byte) 37, (byte) 44, (byte) 49, (byte) 50, (byte) 51, (byte) 85, (byte) 36, (byte) 110, (byte) 117, (byte) 108, (byte) 108, (byte) -40, (byte) 45, (byte) 10, (byte) 11, (byte) 12, (byte) 13, (byte) 14, (byte) 15, (byte) 16, (byte) 17, (byte) 18, (byte) 19, (byte) 20, (byte) 21, (byte) 22, (byte) 23, (byte) 24, (byte) 86, (byte) 36, (byte) 99, (byte) 108, (byte) 97, (byte) 115, (byte) 115, (byte) 95, (byte) 16, (byte) 16, (byte) 100, (byte) 97, (byte) 116, (byte) 101, (byte) 67, (byte) 114, (byte) 101, (byte) 97, (byte) 116, (byte) 101, (byte) 80, (byte) 97, (byte) 99, (byte) 107, (byte) 101, (byte) 116, (byte) 95, (byte) 16, (byte) 18, (byte) 100, (byte) 97, (byte) 116, (byte) 101, (byte) 69, (byte) 110, (byte) 100, (byte) 87, (byte) 97, (byte) 105, (byte) 116, (byte) 82, (byte) 101, (byte) 113, (byte) 117, (byte) 101, (byte) 115, (byte) 116, (byte) 90, (byte) 110, (byte) 97, (byte) 109, (byte) 101, (byte) 77, (byte) 101, (byte) 116, (byte) 104, (byte) 111, (byte) 100, (byte) 91, (byte) 112, (byte) 97, (byte) 114, (byte) 97, (byte) 109, (byte) 79, (byte) 98, (byte) 106, (byte) 101, (byte) 99, (byte) 116, (byte) 92, (byte) 114, (byte) 101, (byte) 115, (byte) 117, (byte) 108, (byte) 116, (byte) 79, (byte) 98, (byte) 106, (byte) 101, (byte) 99, (byte) 116, (byte) 87, (byte) 115, (byte) 117, (byte) 98, (byte) 116, (byte) 121, (byte) 112, (byte) 101, (byte) 84, (byte) 116, (byte) 121, (byte) 112, (byte) 101, Byte.MIN_VALUE, (byte) 2, Byte.MIN_VALUE, (byte) 3, Byte.MIN_VALUE, (byte) 5, Byte.MIN_VALUE, (byte) 6, Byte.MIN_VALUE, (byte) 7, Byte.MIN_VALUE, (byte) 8, (byte) 16, (byte) 0, (byte) 16, (byte) 0, (byte) -46, (byte) 38, (byte) 39, (byte) 28, (byte) 31, (byte) 88, (byte) 36, (byte) 99, (byte) 108, (byte) 97, (byte) 115, (byte) 115, (byte) 101, (byte) 115, (byte) 90, (byte) 36, (byte) 99, (byte) 108, (byte) 97, (byte) 115, (byte) 115, (byte) 110, (byte) 97, (byte) 109, (byte) 101, (byte) -94, (byte) 29, (byte) 42, (byte) 95, (byte) 16, (byte) 16, (byte) 77, (byte) 83, (byte) 77, (byte) 77, (byte) 101, (byte) 115, (byte) 115, (byte) 97, (byte) 103, (byte) 101, (byte) 80, (byte) 97, (byte) 99, (byte) 107, (byte) 101, (byte) 116, (byte) 88, (byte) 78, (byte) 83, (byte) 79, (byte) 98, (byte) 106, (byte) 101, (byte) 99, (byte) 116, (byte) 95, (byte) 16, (byte) 16, (byte) 77, (byte) 83, (byte) 77, (byte) 77, (byte) 101, (byte) 115, (byte) 115, (byte) 97, (byte) 103, (byte) 101, (byte) 80, (byte) 97, (byte) 99, (byte) 107, (byte) 101, (byte) 116, (byte) -46, (byte) 45, (byte) 34, (byte) 47, (byte) 36, (byte) 86, (byte) 36, (byte) 99, (byte) 108, (byte) 97, (byte) 115, (byte) 115, (byte) 87, (byte) 78, (byte) 83, (byte) 46, (byte) 116, (byte) 105, (byte) 109, (byte) 101, Byte.MIN_VALUE, (byte) 4, (byte) 35, (byte) 66, (byte) 115, (byte) -91, (byte) 75, (byte) 29, (byte) -8, (byte) 96, (byte) 0, (byte) -46, (byte) 38, (byte) 39, (byte) 40, (byte) 43, (byte) 88, (byte) 36, (byte) 99, (byte) 108, (byte) 97, (byte) 115, (byte) 115, (byte) 101, (byte) 115, (byte) 90, (byte) 36, (byte) 99, (byte) 108, (byte) 97, (byte) 115, (byte) 115, (byte) 110, (byte) 97, (byte) 109, (byte) 101, (byte) -94, (byte) 41, (byte) 42, (byte) 86, (byte) 78, (byte) 83, (byte) 68, (byte) 97, (byte) 116, (byte) 101, (byte) 88, (byte) 78, (byte) 83, (byte) 79, (byte) 98, (byte) 106, (byte) 101, (byte) 99, (byte) 116, (byte) 86, (byte) 78, (byte) 83, (byte) 68, (byte) 97, (byte) 116, (byte) 101, (byte) -46, (byte) 45, (byte) 46, (byte) 47, (byte) 48, (byte) 86, (byte) 36, (byte) 99, (byte) 108, (byte) 97, (byte) 115, (byte) 115, (byte) 87, (byte) 78, (byte) 83, (byte) 46, (byte) 116, (byte) 105, (byte) 109, (byte) 101, Byte.MIN_VALUE, (byte) 4, (byte) 35, (byte) 66, (byte) 115, (byte) -91, (byte) 75, (byte) 29, (byte) -8, (byte) 96, (byte) 0, (byte) 95, (byte) 16, (byte) 15, (byte) 80, (byte) 105, (byte) 99, (byte) 116, (byte) 117, (byte) 114, (byte) 101, (byte) 82, (byte) 101, (byte) 110, (byte) 100, (byte) 101, (byte) 114, (byte) 101, (byte) 100, (byte) 16, (byte) 0, (byte) 85, (byte) 36, (byte) 110, (byte) 117, (byte) 108, (byte) 108, (byte) -47, (byte) 53, (byte) 54, (byte) 84, (byte) 114, (byte) 111, (byte) 111, (byte) 116, Byte.MIN_VALUE, (byte) 1, (byte) 18, (byte) 0, (byte) 1, (byte) -122, (byte) -96, (byte) 0, (byte) 8, (byte) 0, (byte) 17, (byte) 0, (byte) 27, (byte) 0, (byte) 36, (byte) 0, (byte) 41, (byte) 0, (byte) 50, (byte) 0, (byte) 68, (byte) 0, (byte) 78, (byte) 0, (byte) 84, (byte) 0, (byte) 101, (byte) 0, (byte) 108, (byte) 0, Byte.MAX_VALUE, (byte) 0, (byte) -108, (byte) 0, (byte) -97, (byte) 0, (byte) -85, (byte) 0, (byte) -72, (byte) 0, (byte) -64, (byte) 0, (byte) -59, (byte) 0, (byte) -57, (byte) 0, (byte) -55, (byte) 0, (byte) -53, (byte) 0, (byte) -51, (byte) 0, (byte) -49, (byte) 0, (byte) -47, (byte) 0, (byte) -45, (byte) 0, (byte) -43, (byte) 0, (byte) -38, (byte) 0, (byte) -29, (byte) 0, (byte) -18, (byte) 0, (byte) -15, (byte) 1, (byte) 4, (byte) 1, (byte) 13, (byte) 1, (byte) 32, (byte) 1, (byte) 37, (byte) 1, (byte) 44, (byte) 1, (byte) 52, (byte) 1, (byte) 54, (byte) 1, (byte) 63, (byte) 1, (byte) 68, (byte) 1, (byte) 77, (byte) 1, (byte) 88, (byte) 1, (byte) 91, (byte) 1, (byte) 98, (byte) 1, (byte) 107, (byte) 1, (byte) 114, (byte) 1, (byte) 119, (byte) 1, (byte) 126, (byte) 1, (byte) -122, (byte) 1, (byte) -120, (byte) 1, (byte) -111, (byte) 1, (byte) -93, (byte) 1, (byte) -91, (byte) 1, (byte) -85, (byte) 1, (byte) -82, (byte) 1, (byte) -77, (byte) 1, (byte) -75, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 2, (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 56, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) -70};
    }

    public ServerInteractionManager() {
        this.socketChannelManager = null;
        this.telemetryQueue = Executors.newSingleThreadExecutor();
        this.className = "ServerInteractionMgr";
    }

    private void OnApplicationsListReceived(CFBaseTypes cFBaseTypes, CFBaseTypes cFBaseTypes2) {
        Logger.e("zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz onApplications received");
        new WindowsManager().onApplicationsLoaded(cFBaseTypes);
    }

    private void OnAudioServiceAccessibleOnPort(int i) {
        this.contentServiceAccessibleListener.OnAudioServiceAccessibleOnPort(i);
    }

    private void OnReceivedVideoData(int i, byte[] bArr) {
        this.videoDataAvailableListener.OnVideoDataAvailable(i, bArr);
    }

    private void OnRecivedAccessConfirmed(CFBaseTypes cFBaseTypes) {
        this.accessConfirmedListener.OnRecivedAccessConfirmed(cFBaseTypes);
    }

    private void OnRecivedPingResponse(CFDate cFDate) {
        if (this.pingResponseListener != null) {
            this.pingResponseListener.OnRecivedPingResponse(cFDate);
        }
    }

    private void OnVideoServiceAccessibleOnPort(int i) {
        this.contentServiceAccessibleListener.OnVideoServiceAccessibleOnPort(i);
    }

    private void OnWindowsListReceived(CFBaseTypes cFBaseTypes, CFBaseTypes cFBaseTypes2) {
        new WindowsManager().onWindowsLoaded(cFBaseTypes);
    }

    private void callMethod(String str, CFBaseTypes cFBaseTypes, CFBaseTypes cFBaseTypes2) {
        if (str.equalsIgnoreCase("accessConfirmed")) {
            Logger.d(this.className + ":callMethod accessConfirmed");
            OnRecivedAccessConfirmed(cFBaseTypes);
        } else if (str.equalsIgnoreCase("videoServiceAccessibleOnPort")) {
            Logger.d(this.className + ":callMethod videoServiceAccessibleOnPort");
            OnVideoServiceAccessibleOnPort(cFBaseTypes.getAsInt32());
        } else if (str.equalsIgnoreCase("AudioServiceAccessibleOnPort")) {
            Logger.d(this.className + ":callMethod audioServiceAccessibleOnPort");
            OnAudioServiceAccessibleOnPort(cFBaseTypes.getAsInt32());
        } else if (str.equalsIgnoreCase("PING")) {
            OnRecivedPingResponse(new CFDate(cFBaseTypes2.getAsDouble()));
        } else if (str.equalsIgnoreCase("WindowsList")) {
            OnWindowsListReceived(cFBaseTypes, cFBaseTypes2);
        } else if (str.equalsIgnoreCase("ImportFavorites")) {
            OnApplicationsListReceived(cFBaseTypes, cFBaseTypes2);
        } else {
            Logger.w("unknown method called " + str + "   " + cFBaseTypes);
        }
    }

    private CMSMMessagePacket decodeData(int i, byte[] bArr) {
        CKeyedUnarchiver cKeyedUnarchiver = new CKeyedUnarchiver();
        if (cKeyedUnarchiver.initWithData(bArr, (long) i)) {
            CFBaseTypes decodeRootObject = cKeyedUnarchiver.decodeRootObject();
            if (decodeRootObject.getType() != CFTypeID.kClassType) {
                Logger.d("object = " + decodeRootObject.getType());
                Logger.d("object = " + decodeRootObject.toString());
                return null;
            }
            CFCustomClass customClass = decodeRootObject.customClass();
            CMSMMessagePacket cMSMMessagePacket = new CMSMMessagePacket();
            boolean initWithCustomClass = cMSMMessagePacket.initWithCustomClass(customClass);
            Logger.d(this.className + ":InitWithClass " + initWithCustomClass);
            return initWithCustomClass ? cMSMMessagePacket : null;
        } else {
            Logger.d("object =  can't init");
            return null;
        }
    }

    private void processTelemetry(int i, byte[] bArr) {
        CMSMMessagePacket decodeData = decodeData(i, bArr);
        if (decodeData != null) {
            Logger.d(this.className + ":methodToCall " + decodeData.getMethodName());
            callMethod(decodeData.getMethodName(), decodeData.getParam(), decodeData.getResultObject());
        }else {
			Logger.d("processTelemetry null data");
		}
    }

    public boolean connectToPort(int i) {
        Logger.d(this.className + ":ConnectToPort " + i);
        this.socketChannelManager = new SocketChannelManager();
        this.socketChannelManager.setSocketDataAvailableListener(this);
        return this.socketChannelManager.connectToPort(i);
    }

    public boolean connectToServer(String str, int i) {
        Logger.d(this.className + ":ConnectToServer " + str + ":" + i);
        this.socketChannelManager = new SocketChannelManager();
        this.socketChannelManager.setSocketDataAvailableListener(this);
        return this.socketChannelManager.connectToServer(str, i);
    }

    public void onAcceptSocketData(int i, byte[] bArr) {
        if (this.videoDataAvailableListener != null) {
            OnReceivedVideoData(i, bArr);
        }
    }

    public void onAcceptSocketTelemetry(int i, byte[] bArr) {
    	
    	Logger.d("onAcceptSocketTelemetry: i:"+i+" barr.length"+bArr.length);
    	
        if (i > 5000) {
            this.telemetryQueue.submit(new AnonymousClass_1(i, bArr));
        } else {
            processTelemetry(i, bArr);
        }
    }

    public void sendEvent(KeyboardEventStructure keyboardEventStructure) {
        CFBaseTypes mSMKeyboardEventPacket = new MSMKeyboardEventPacket(keyboardEventStructure);
        CKeyedArchiver cKeyedArchiver = new CKeyedArchiver();
        cKeyedArchiver.encodeRootObject(mSMKeyboardEventPacket);
        byte[] encodeToBinary = cKeyedArchiver.encodeToBinary();
        Logger.d("data = sendEvent, " + encodeToBinary.length);
        this.socketChannelManager.sendDataData(encodeToBinary);
    }

    public void sendLog(String str) {
        sendMessage(new CFStringBaseT(str), new CFStringBaseT("Log"));
    }

    public void sendMessage(CFBaseTypes cFBaseTypes, CFStringBaseT cFStringBaseT) {
        if (this.socketChannelManager == null) {
            Logger.e("trying to send data before init");
            return;
        }
        
        Logger.d("ServerInteractionManager: sendMessage");
        CFBaseTypes cMSMMessagePacket = cFStringBaseT.nativeStr().equals("Ping") ? new CMSMMessagePacket(MSMMessageType.MSMMessageTypeRequest, cFStringBaseT, cFBaseTypes) : new CMSMMessagePacket(MSMMessageType.MSMMessageTypeSend, cFStringBaseT, cFBaseTypes);
        CKeyedArchiver cKeyedArchiver = new CKeyedArchiver();
        cKeyedArchiver.encodeRootObject(cMSMMessagePacket);
        this.socketChannelManager.sendData(cKeyedArchiver.encodeToBinary());
    }

    public void sendMessageFps() {
        if (this.socketChannelManager == null) {
            Logger.e("trying to send data before init");
        } else {
            this.socketChannelManager.sendData(mFpsArray);
        }
    }

    public void sendStartStream() {
        Logger.d(this.className + ":Inside sendStartStream");
        CFStringBaseT cFStringBaseT = new CFStringBaseT("StartStream");
        sendMessage(cFStringBaseT, cFStringBaseT);
    }

    public void sendStopStream() {
        Logger.d(this.className + ":Inside sendStopStream");
        CFStringBaseT cFStringBaseT = new CFStringBaseT("StopStream");
        sendMessage(cFStringBaseT, cFStringBaseT);
    }

    public void sendTouchEvent(MSMTouchEvent mSMTouchEvent) {
        CFBaseTypes mSMTouchEventPacket = new MSMTouchEventPacket(mSMTouchEvent);
        CKeyedArchiver cKeyedArchiver = new CKeyedArchiver();
        cKeyedArchiver.encodeRootObject(mSMTouchEventPacket);
        byte[] encodeToBinary = cKeyedArchiver.encodeToBinary();
        Logger.d("data = sendTouchEvent (" + mSMTouchEvent + "),  " + encodeToBinary.length);
        this.socketChannelManager.sendDataData(encodeToBinary);
    }

    public void sendWheelEvent(int i, boolean z) {
        CFBaseTypes mSMWheelEventPacket = new MSMWheelEventPacket(i, z);
        CKeyedArchiver cKeyedArchiver = new CKeyedArchiver();
        cKeyedArchiver.encodeRootObject(mSMWheelEventPacket);
        this.socketChannelManager.sendDataData(cKeyedArchiver.encodeToBinary());
    }

    public void setAccessConfirmedListener(AccessConfirmedListener accessConfirmedListener) {
        this.accessConfirmedListener = accessConfirmedListener;
    }

    public void setContentServiceAccessibleListener(ContentServiceAccessibleOnPortListener contentServiceAccessibleOnPortListener) {
        this.contentServiceAccessibleListener = contentServiceAccessibleOnPortListener;
    }

    public void setOnRecivedPingResponse(PingResponseListener pingResponseListener) {
        this.pingResponseListener = pingResponseListener;
    }

    public void setVideoDataAvailableListener(VideoDataReceivedListener videoDataReceivedListener) {
        this.videoDataAvailableListener = videoDataReceivedListener;
    }

    public void signalVirtualScreenShown() {
        if (this.socketChannelManager != null) {
            this.socketChannelManager.signalVirtualScreenShown();
        }
    }

    public void stopProcesses() {
        if (this.socketChannelManager != null) {
            this.socketChannelManager.stopProcesses();
        }
    }
}
