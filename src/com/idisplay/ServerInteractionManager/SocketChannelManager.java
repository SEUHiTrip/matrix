package com.idisplay.ServerInteractionManager;

import com.idisplay.Audio.AudioChannel;
import com.idisplay.VirtualScreenDisplay.ConnectionActivity;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection;
import com.idisplay.VirtualScreenDisplay.ThreadEvent;
import com.idisplay.util.ByteBufferPool;
import com.idisplay.util.Logger;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import org.apache.log4j.Level;
import org.apache.log4j.spi.ErrorCode;

import seu.lab.matrix.AbstractScreenMatrixActivity;
import seu.lab.matrix.ScreenMatrixActivity;

public class SocketChannelManager {
    private static ServerSocket USBCommandsSocket;
    private static ServerSocket USBDataSocket;
    private static int USB_COMMANDS_PORT;
    private static int USB_COMMANDS_TIMEOUT;
    private static int USB_DATA_PORT;
    private static int USB_DATA_TIMEOUT;
    private static boolean connectWithUSb;
    private static String serverIpAddress;
    private AcceptSocketDataListener acceptSocketDataListener;
    private String className;
    private Socket dataSocket;
    private ListenToDataSocket listenerData;
    private ListenToTelemetrySocket listenerTelemetry;
    private SendDataThread mSendDataThread;
    private SendDataThread mSendTelemetryThread;
    private boolean remoteSocketClosedNotified;
    private Socket serverSocket;
    private volatile boolean stopProcess;

    private AbstractScreenMatrixActivity iDisplayer;

    private class ListenToDataSocket extends Thread {
        private final int INITIAL_DATA_LEN;
        private ThreadEvent VSCREEN_WAIT;
        private byte[] actualData;
        private InputStream mIn;

        public ListenToDataSocket(InputStream inputStream) {
            this.INITIAL_DATA_LEN = 10240;
            this.actualData = null;
            this.VSCREEN_WAIT = new ThreadEvent();
            this.mIn = null;
            this.mIn = inputStream;
            this.actualData = new byte[10240];
        }

        public void run() {
            byte[] bArr = new byte[4];
            DataInputStream dataInputStream = new DataInputStream(this.mIn);
            int access$100 = 0;
            while (!stopProcess) {
                try {
                    dataInputStream.readFully(bArr);
                    access$100 = SocketChannelManager.ByteArrayToInt(bArr);
                    if (access$100 > 10000000) {
                        Logger.e(getName() + ": Disconnect. Data length is to big. length = " + access$100);
                        if (!stopProcess) {

                            if (iDisplayer.getScreenHandler() != null) {
                                iDisplayer.getScreenHandler().sendEmptyMessage(ErrorCode.ADDRESS_PARSE_FAILURE);
                            }
                            stopProcess = true;
                            return;
                        }
                        return;
                    } else if (access$100 > 0) {
                        this.actualData = ByteBufferPool.get(access$100);
                        try {
                            dataInputStream.readFully(this.actualData, 0, access$100);
                            IDisplayConnection.ccMngr.sendFrameReceived();
                            onAcceptSocketData(access$100, this.actualData);
                        } catch (Throwable e) {
                            Logger.e(getName() + ": Can't read data", e);
                        }

                        if (!IDisplayConnection.isVirtualScreenShown()) {
                            try {
                                Logger.e(getName() + ": Waiting for Virtual screen to be shown");
                                this.VSCREEN_WAIT.await();
                            } catch (Throwable e2) {
                                Logger.w(getName() + ": Stop wait", e2);
                            }
                        }
                    } else {
                        Logger.e(getName() + ": Disconnect. Data length is <= 0. length = " + access$100);

                        if (iDisplayer.getScreenHandler() != null) {
                            iDisplayer.getScreenHandler().sendEmptyMessage(17);
                        }
                        remoteSocketClosedNotified = true;
                        stopProcess = true;
                        return;
                    }
                } catch (Throwable e22) {
                    if (!stopProcess) {
                        Logger.e(getName(), e22);
                        
                        if (iDisplayer.getScreenHandler() != null) {
                            iDisplayer.getScreenHandler().sendEmptyMessage(ErrorCode.ADDRESS_PARSE_FAILURE);
                        }
                        stopProcess = true;
                    }
                }
            }
            Logger.d(getName() + ": Stopping socket Listener 2");
        }
    }

    private class ListenToTelemetrySocket extends Thread {
        private byte[] actualData;
        private InputStream mIn;

        public ListenToTelemetrySocket(InputStream inputStream) {
            this.actualData = null;
            this.mIn = null;
            this.mIn = inputStream;
        }

        public void run() {
            byte[] bArr = new byte[4];
            DataInputStream dataInputStream = new DataInputStream(this.mIn);
            int access$100 = 0;
            while (!stopProcess) {
                try {
                    dataInputStream.readFully(bArr);
                    access$100 = SocketChannelManager.ByteArrayToInt(bArr);
                } catch (Throwable e) {
                    Logger.e(getName() + ": Can't read size", e);
                    if (!stopProcess) {
                        if (iDisplayer.getScreenHandler() != null) {
                            iDisplayer.getScreenHandler().sendEmptyMessage(ErrorCode.ADDRESS_PARSE_FAILURE);
                        }
                        stopProcess = true;
                    }
                }
                if (access$100 > 10000000) {
                    Logger.e(getName() + ": Disconnect. Data length is to big. length = " + access$100);
                    if (!stopProcess) {
                        if (iDisplayer.getScreenHandler() != null) {
                            iDisplayer.getScreenHandler().sendEmptyMessage(ErrorCode.ADDRESS_PARSE_FAILURE);
                        }
                        stopProcess = true;
                        return;
                    }
                    return;
                } else if (access$100 > 0) {
                    try {
                        this.actualData = new byte[access$100];                    
                        
                        try {
                            dataInputStream.readFully(this.actualData, 0, access$100);
                        } catch (Throwable e3) {
                            Logger.e(getName() + ": Can't read data", e3);
                            if (!remoteSocketClosedNotified) {
                            	iDisplayer.setSocketConnectionClosedFromRemote();
                                remoteSocketClosedNotified = true;
                                stopProcess = true;
                                break;
                            }
                        }
                        
                        onAcceptSocketTelemetry(access$100, this.actualData);
                    } catch (OutOfMemoryError e2) {
                        this.actualData = null;
                    }

                } else {
                    Logger.e(getName() + ": Disconnect. Data length is 0. length = " + access$100);
                    if (iDisplayer.getScreenHandler() != null) {
                        iDisplayer.getScreenHandler().sendEmptyMessage(17);
                    }

                    remoteSocketClosedNotified = true;
                    stopProcess = true;
                    return;
                }
            }
            Logger.d(getName() + ": Stopping socket Listener 2");
        }
    }

    static {
        serverIpAddress = null;
        USB_COMMANDS_TIMEOUT = 20000;
        USB_DATA_TIMEOUT = 20000;
        USB_COMMANDS_PORT = 53422;
        USB_DATA_PORT = 53421;
        connectWithUSb = false;
    }

    public SocketChannelManager() {
        this.stopProcess = false;
        this.remoteSocketClosedNotified = false;
        this.serverSocket = null;
        this.dataSocket = null;
        this.listenerTelemetry = null;
        this.listenerData = null;
        this.className = "SocketChannelManager";
        this.mSendTelemetryThread = null;
        this.mSendDataThread = null;
    }

    private static final int ByteArrayToInt(byte[] bArr) {
        return (((bArr[0] & 255) + ((bArr[1] & 255) << 8)) + ((bArr[2] & 255) << 16)) + ((bArr[3] & 255) << 24);
    }

    public static void cancelUSBConnect() {
        try {
            if (USBCommandsSocket != null) {
                USBCommandsSocket.close();
            }
            if (USBDataSocket != null) {
                USBDataSocket.close();
            }
        } catch (Throwable e) {
            Logger.d("Cancel USB connection", e);
        }
    }

    public boolean connectToPort(int i) {
        try {
            Logger.d(this.className + ": Inside connectToPort");
            if (!connectWithUSb) {
                SocketAddress inetSocketAddress = new InetSocketAddress(serverIpAddress, i);
                if (this.dataSocket != null && this.dataSocket.isConnected()) {
                    try {
                        this.dataSocket.close();
                    } catch (Exception e) {
                        Logger.e("Unable to close data socket");
                    }
                }
                this.dataSocket = new Socket();
                this.dataSocket.connect(inetSocketAddress, Level.TRACE_INT);
            } else if (USBDataSocket != null) {
                USBDataSocket.setSoTimeout(USB_DATA_TIMEOUT);
                this.dataSocket = USBDataSocket.accept();
            } else {
                Logger.e(this.className + ": connectToPort - USBDataSocket is NULL");
                return false;
            }
            this.dataSocket.setTcpNoDelay(true);
            InputStream inputStream = this.dataSocket.getInputStream();
            OutputStream outputStream = this.dataSocket.getOutputStream();
            this.listenerData = new ListenToDataSocket(inputStream);
            this.listenerData.setName("DataChannelThread");
            this.listenerData.setPriority(ErrorCode.MISSING_LAYOUT);
            Logger.d(this.className + ": Starting DataChannelThread");
            this.listenerData.start();
            this.mSendDataThread = new SendDataThread(outputStream, "DataChannelThread");
            return true;
        } catch (UnknownHostException e2) {
            Logger.e(this.className + ": Exception in connectToPort " + e2.toString());
            return false;
        } catch (SocketTimeoutException e3) {
            Logger.e(this.className + ": Exception in connectToPort " + e3.toString());
            return false;
        } catch (IOException e4) {
            Logger.e(this.className + ": Exception in connectToPort " + e4.toString());
            return false;
        }
    }

    public boolean connectToServer(String str, int i) {
        try {
            Logger.d(this.className + ": Inside ConnectToServer. IP: " + str + " port: " + i);
            connectWithUSb = i == -1;
            if (connectWithUSb) {
                try {
                    if (USBCommandsSocket != null) {
                        USBCommandsSocket.close();
                    }
                    if (USBDataSocket != null) {
                        USBDataSocket.close();
                    }
                } catch (Throwable e) {
                    Logger.e(this.className + ": Close usb socket exception", e);
                }
                USBCommandsSocket = new ServerSocket(USB_COMMANDS_PORT);
                USBCommandsSocket.setSoTimeout(USB_COMMANDS_TIMEOUT);
                this.serverSocket = USBCommandsSocket.accept();
                USBDataSocket = new ServerSocket(USB_DATA_PORT);
                AudioChannel.getInstance().createUsbSocket();
            } else {
                serverIpAddress = str;
                this.serverSocket = new Socket();
                this.serverSocket.connect(new InetSocketAddress(serverIpAddress, i), Level.TRACE_INT);
            }
            this.serverSocket.setTcpNoDelay(true);
            InputStream inputStream = this.serverSocket.getInputStream();
            OutputStream outputStream = this.serverSocket.getOutputStream();
            this.listenerTelemetry = new ListenToTelemetrySocket(inputStream);
            this.listenerTelemetry.setName("ConnectionChannelThread");
            this.listenerTelemetry.setPriority(1);
            Logger.d(this.className + ": Starting ConnectionChannelThread thread");
            this.listenerTelemetry.start();
            this.mSendTelemetryThread = new SendDataThread(outputStream, "ConnectionChannelThread");
            return true;
        } catch (UnknownHostException e22) {
            Logger.e(this.className + ": Exception in connectToServer " + e22.toString());
            return false;
        } catch (Throwable e32) {
            Logger.e(this.className + ": Exception in connectToServer ", e32);
            if (connectWithUSb) {
            	IDisplayConnection.listScreenHandler.sendEmptyMessage(1);
            }
            return false;
        }
    }

    public void onAcceptSocketData(int i, byte[] bArr) {
        this.acceptSocketDataListener.onAcceptSocketData(i, bArr);
    }

    public void onAcceptSocketTelemetry(int i, byte[] bArr) {
        this.acceptSocketDataListener.onAcceptSocketTelemetry(i, bArr);
    }

    public void sendData(byte[] bArr) {
        if (this.mSendTelemetryThread != null) {
            this.mSendTelemetryThread.send(bArr);
        } else {
            Logger.d("null pointer in sendData");
        }
    }

    public void sendDataData(byte[] bArr) {
        if (this.mSendDataThread != null) {
            this.mSendDataThread.send(bArr);
        } else {
            Logger.d("null pointer in sendDataData");
        }
    }

    public void setSocketDataAvailableListener(AcceptSocketDataListener acceptSocketDataListener) {
        this.acceptSocketDataListener = acceptSocketDataListener;
    }

    public void signalVirtualScreenShown() {
        if (this.listenerData != null) {
            this.listenerData.VSCREEN_WAIT.signal();
        }
    }

    public void stopProcesses() {
        try {
            this.stopProcess = true;
            Logger.d(this.className + ": In StopProcess ");
            if (this.mSendTelemetryThread != null) {
                this.mSendTelemetryThread.stop();
            }
            if (this.mSendDataThread != null) {
                this.mSendDataThread.stop();
            }
            if (this.listenerTelemetry != null) {
                this.listenerTelemetry.interrupt();
            }
            if (this.serverSocket != null) {
                this.serverSocket.shutdownInput();
                this.serverSocket.shutdownOutput();
                this.serverSocket.close();
                this.serverSocket = null;
            }
            if (this.listenerData != null) {
                this.listenerData.interrupt();
            }
            if (this.dataSocket != null) {
                this.dataSocket.shutdownInput();
                this.dataSocket.shutdownOutput();
                this.dataSocket.close();
                this.dataSocket = null;
            }
        } catch (IOException e) {
            Logger.e(this.className + ": Exception in stopProcess " + e.toString());
        } catch (NullPointerException e2) {
            Logger.e(this.className + ": Exception in stopProcess " + e2.toString());
        } catch (Exception e3) {
            Logger.e(this.className + ": Exception in stopProcess " + e3.toString());
        }
    }
}
