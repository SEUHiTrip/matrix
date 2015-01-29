package com.idisplay.DataChannelManager;

import com.idisplay.ConnectionChannelManager.ConnectionChannelManager.DeviceOrientation;
import com.idisplay.ConnectionChannelManager.ServerInfo;
import com.idisplay.CoreFoundation.CNSSet;
import com.idisplay.CoreFoundation.MSMTouchObject;
import com.idisplay.GlobalCommunicationStructures.KeyboardEventStructure;
import com.idisplay.GlobalCommunicationStructures.MSMTouchEvent;
import com.idisplay.GlobalCommunicationStructures.MSMTouchEvent.MSMEventSubtype;
import com.idisplay.GlobalCommunicationStructures.MSMTouchEvent.MSMEventType;
import com.idisplay.ServerInteractionManager.ServerInteractionManager;
import com.idisplay.ServerInteractionManager.VideoDataReceivedListener;
import com.idisplay.VirtualScreenDisplay.OnOrientationChangeListner;
import com.idisplay.util.BitmapPool;
import com.idisplay.util.Logger;
import javolution.xml.stream.XMLStreamConstants;
import org.apache.log4j.spi.ErrorCode;

public class DataChannelManager implements VideoDataReceivedListener, OnOrientationChangeListner, TouchEventSendListener, KeyEventSendListener {
    private static boolean m_MAC_server;
    private String className;
    private CleanupThread cleanup;
    ServerInteractionManager dataSIM;
    private int m_height;
    KeyEventTask m_keyEventTask;
    public boolean m_orientationChange;
    ReadImagesTask m_readImage;
    private ServerInfo m_serverInfo;
    TouchEventTask m_touchTask;
    private int m_width;

    public enum Compression {
        None,
        RLE,
        LZW,
        TIFF,
        PNG,
        JPEG,
        MTC_RLE_LZJB,
        ME,
        VP8
    }

    private class MSMImageHeader {
        private byte compression;
        private int height;
        private int imageOffset;
        private int rowBytes;
        private int width;

        public MSMImageHeader(byte[] bArr, int i) {
            this.imageOffset = ByteArrayUtilities.byteArrayToInt(bArr, 0);
            this.width = ByteArrayUtilities.byteArrayToInt(bArr, ErrorCode.FILE_OPEN_FAILURE);
            this.height = ByteArrayUtilities.byteArrayToInt(bArr, XMLStreamConstants.END_DOCUMENT);
            this.compression = bArr[12];
            if (i == 3) {
                this.rowBytes = (this.width + 32) * 4;
            } else if (i == 4 || i == 5) {
                this.rowBytes = ByteArrayUtilities.byteArrayToInt(bArr, XMLStreamConstants.NAMESPACE);
            }
        }
    }

    static {
        m_MAC_server = false;
    }

    public DataChannelManager(int i, int i2, DeviceOrientation deviceOrientation, ServerInfo serverInfo) {
        String str = null;
        this.dataSIM = null;
        this.m_readImage = null;
        this.m_touchTask = null;
        this.m_keyEventTask = null;
        this.m_width = 0;
        this.m_height = 0;
        this.m_serverInfo = null;
        this.cleanup = null;
        this.m_orientationChange = false;
        this.className = getClass().getName();
        this.m_width = i;
        this.m_height = i2;
        this.m_serverInfo = serverInfo;
        if (this.m_serverInfo != null) {
            str = this.m_serverInfo.OSName();
        } else {
            Logger.e(this.className + ": Server Info is NULL at constructor!");
        }
        if (str != null) {
            m_MAC_server = str.contains("MAC");
        } else {
            m_MAC_server = false;
            Logger.e("OS Name is empty. Strange.");
        }
        this.cleanup = new CleanupThread();
        this.cleanup.start();
        this.cleanup.setPriority(XMLStreamConstants.ATTRIBUTE);
        this.m_readImage = new ReadImagesTask(this.cleanup);
        this.m_readImage.setName("ReadImageTask");
        this.m_readImage.start();
        this.m_readImage.setPriority(ErrorCode.MISSING_LAYOUT);
        this.m_touchTask = new TouchEventTask();
        this.m_touchTask.setTouchEventSendListener(this);
        this.m_touchTask.setName("touchTask");
        this.m_touchTask.start();
        this.m_touchTask.setPriority(XMLStreamConstants.ATTRIBUTE);
        this.m_keyEventTask = new KeyEventTask();
        this.m_keyEventTask.setKeyEventSendListener(this);
        this.m_keyEventTask.setName("keyEventTask");
        this.m_keyEventTask.start();
        this.m_keyEventTask.setPriority(XMLStreamConstants.ATTRIBUTE);
        Logger.i(this.className + ":width = " + this.m_width + " :height: " + this.m_height);
    }

    public static boolean isMACServer() {
        return m_MAC_server;
    }

    public void OnOrientationChange() {
        Logger.d(this.className + ":On Orientation change");
        BitmapPool.clear();
    }

    public void OnVideoDataAvailable(int i, byte[] bArr) {
        int i2;
        byte[] bArr2;
        int i3 = 0;
        int i4 = -1;
        int i5 = XMLStreamConstants.ATTRIBUTE;
        if (this.m_serverInfo == null || this.m_serverInfo.hostDataProtocolInt() == 1) {
            i5 = 1;
            i2 = 0;
        } else if (this.m_serverInfo.hostDataProtocolInt() == 2) {
            i5 = bArr[0] & 255;
            Logger.e(this.className + ":OnVideoDataAvailable len = " + bArr.length);
            i2 = 1;
        } else if (this.m_serverInfo.hostDataProtocolInt() == 3 || this.m_serverInfo.hostDataProtocolInt() == 4 || this.m_serverInfo.hostDataProtocolInt() == 5) {
            MSMImageHeader mSMImageHeader = new MSMImageHeader(bArr, this.m_serverInfo.hostDataProtocolInt());
            i5 = mSMImageHeader.compression & 255;
            i3 = mSMImageHeader.rowBytes;
            i2 = mSMImageHeader.imageOffset;
            if (!(this.m_width == mSMImageHeader.width && this.m_height == mSMImageHeader.height)) {
                this.m_width = mSMImageHeader.width;
                this.m_height = mSMImageHeader.height;
            }
            if (i5 == Compression.MTC_RLE_LZJB.ordinal()) {
                i4 = ByteArrayUtilities.byteArrayToInt(bArr, i2);
                i2 += 4;
            }
        } else {
            Logger.e("Unsupported protocol version - " + this.m_serverInfo.hostDataProtocol());
            i2 = 0;
        }
        if (i2 == 0) {
            bArr2 = bArr;
        } else if (i2 <= 0) {
            Logger.e(this.className + ":Invalid data");
            return;
        } else if (i - i2 <= 0) {
            Logger.e("ERROR. incommingDataLen < imageOffset ::  incommingDataLen " + i + " imageOffset " + i2 + "WxH" + this.m_width + "x" + this.m_height);
            bArr2 = null;
        } else {
            bArr2 = bArr;
        }
        if (i3 == 0) {
            i3 = (this.m_width + 32) * 4;
        }
        if (bArr2 != null) {
            this.m_readImage.setVideoUpdate(bArr2, i2, i4, this.m_width, this.m_height, i3, i5);
        }
    }

    public boolean connectToPort(int i) {
        Logger.d(this.className + ":connectToPort");
        this.dataSIM = new ServerInteractionManager();
        this.dataSIM.setVideoDataAvailableListener(this);
//        VirtualScreenActivity.setOrientationChangeListner(this);
        return this.dataSIM.connectToPort(i);
    }

    public ServerInfo getServerinfo() {
        return this.m_serverInfo;
    }

    public void sendKeyEventListener(KeyboardEventStructure keyboardEventStructure) {
        this.dataSIM.sendEvent(keyboardEventStructure);
    }

    public void sendRightClickEvent(MSMTouchObject mSMTouchObject) {
        CNSSet cNSSet = new CNSSet();
        CNSSet cNSSet2 = new CNSSet();
        cNSSet.getSet().add(mSMTouchObject);
        MSMTouchEvent mSMTouchEvent = new MSMTouchEvent(MSMEventType.touch, MSMEventSubtype.right_click, mSMTouchObject._phase, cNSSet, cNSSet2);
        Logger.d(this.className + ":sendRightClickEvent");
        this.m_touchTask.setTouchEvent(mSMTouchEvent);
    }

    public void sendTouchEvent(MSMTouchObject mSMTouchObject) {
        Logger.d(this.className + ":Send Touch Event");
        CNSSet cNSSet = new CNSSet();
        CNSSet cNSSet2 = new CNSSet();
        cNSSet.getSet().add(mSMTouchObject);
        this.m_touchTask.setTouchEvent(new MSMTouchEvent(MSMEventType.touch, MSMEventSubtype.none, mSMTouchObject._phase, cNSSet, cNSSet2));
    }

    public void sendTouchEventTaskListener(MSMTouchEvent mSMTouchEvent) {
        this.dataSIM.sendTouchEvent(mSMTouchEvent);
    }

    public void sendWheelEvent(int i) {
        this.dataSIM.sendWheelEvent(i, m_MAC_server);
    }

    public void sendkeyboardEvent(KeyboardEventStructure keyboardEventStructure) {
        this.m_keyEventTask.setKeyEvent(keyboardEventStructure);
    }

    public void signalVirtualScreenShown() {
        if (this.dataSIM != null) {
            this.dataSIM.signalVirtualScreenShown();
        }
    }

    public void stopProcesses() {
        Logger.d(this.className + ":Stop Process");
        if (this.dataSIM != null) {
            this.dataSIM.stopProcesses();
        }
        if (this.cleanup != null) {
            this.cleanup.stopProcess();
        }
        if (this.m_readImage != null) {
            this.m_readImage.stopProcess();
        }
        if (this.m_touchTask != null) {
            this.m_touchTask.stopProcess();
        }
        if (this.m_keyEventTask != null) {
            this.m_keyEventTask.stopProcess();
        }
    }
}
