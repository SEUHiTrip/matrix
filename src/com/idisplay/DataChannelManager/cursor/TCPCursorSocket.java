//package com.idisplay.DataChannelManager.cursor;
//
//import com.idisplay.util.Logger;
//import com.idisplay.util.Utils;
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.ServerSocket;
//import java.net.Socket;
//import javolution.context.ArrayFactory;
//
//public class TCPCursorSocket extends CursorSocket {
//    private boolean listenStop;
//    private ServerSocket servSocket;
//
//    class AnonymousClass_1 extends Thread {
//        AnonymousClass_1(String str) {
//            super(str);
//        }
//
//        public void run() {
//            Socket socket;
//            InputStream inputStream = null;
//            byte[] bArr;
//            int TwoByteArrayToInt;
//            byte[] bArr2;
//            Object obj = null;
//            Logger.e("TCP - start");
//            try {
//                Socket accept = TCPCursorSocket.this.servSocket.accept();
//                try {
//                    accept.setTcpNoDelay(true);
//                    socket = accept;
//                    inputStream = accept.getInputStream();
//                } catch (IOException e) {
//                    Throwable e2 = e;
//                    Logger.e("TCP - Open InputStream EXC", e2);
//                    // TODO
////                    ConnectionActivity.ccMngr.sendTurnOffUDP();
//                    TCPCursorSocket.this.listenStop = true;
//                    socket = accept;
//                    //inputStream = r0;
//                    bArr = new byte[2];
//                    while (!TCPCursorSocket.this.listenStop) {
//                        if (inputStream.available() == 0) {
//                            Logger.e("TCP - ava1:" + inputStream.available());
//                            inputStream.read(bArr);
//                            Logger.e("TCP - ava2:" + inputStream.available());
//                            TwoByteArrayToInt = Utils.TwoByteArrayToInt(bArr);
//                            Logger.e("TCP - read" + TwoByteArrayToInt);
//                            bArr2 = (byte[]) ArrayFactory.BYTES_FACTORY.array(TwoByteArrayToInt);
//                            inputStream.read(bArr2, 0, TwoByteArrayToInt);
//                            Logger.e("TCP - ava3:" + inputStream.available());
//                            TCPCursorSocket.this.createCursorImage(bArr2, false);
//                            ArrayFactory.BYTES_FACTORY.recycle(bArr2);
//                        } else {
//                            Logger.e("TCP - empty");
//                            Thread.sleep(100);
//                        }
//                    }
//                    if (socket == null) {
//                        Logger.e("TCP - point2");
//                        socket.close();
//                    }
//                }
//            } catch (Throwable e3) {
//            	Throwable e2 = e3;
////                accept = null;
//                Logger.e("TCP - Open InputStream EXC", e2);
//                // TODO ConnectionActivity.ccMngr.sendTurnOffUDP();
//                TCPCursorSocket.this.listenStop = true;
//                socket = null;
////                inputStream = r0;
//                bArr = new byte[2];
//                while (!TCPCursorSocket.this.listenStop) {
//                    if (inputStream.available() == 0) {
//                        Logger.e("TCP - empty");
//                        Thread.sleep(100);
//                    } else {
//                        Logger.e("TCP - ava1:" + inputStream.available());
//                        inputStream.read(bArr);
//                        Logger.e("TCP - ava2:" + inputStream.available());
//                        TwoByteArrayToInt = Utils.TwoByteArrayToInt(bArr);
//                        Logger.e("TCP - read" + TwoByteArrayToInt);
//                        bArr2 = (byte[]) ArrayFactory.BYTES_FACTORY.array(TwoByteArrayToInt);
//                        inputStream.read(bArr2, 0, TwoByteArrayToInt);
//                        Logger.e("TCP - ava3:" + inputStream.available());
//                        TCPCursorSocket.this.createCursorImage(bArr2, false);
//                        ArrayFactory.BYTES_FACTORY.recycle(bArr2);
//                    }
//                }
//                if (socket == null) {
//                    Logger.e("TCP - point2");
//                    socket.close();
//                }
//            }
//            bArr = new byte[2];
//            while (!TCPCursorSocket.this.listenStop) {
//                try {
//                    if (inputStream.available() == 0) {
//                        Logger.e("TCP - empty");
//                        try {
//                            Thread.sleep(100);
//                        } catch (InterruptedException e4) {
//                        }
//                    } else {
//                        Logger.e("TCP - ava1:" + inputStream.available());
//                        inputStream.read(bArr);
//                        Logger.e("TCP - ava2:" + inputStream.available());
//                        TwoByteArrayToInt = Utils.TwoByteArrayToInt(bArr);
//                        Logger.e("TCP - read" + TwoByteArrayToInt);
//                        bArr2 = (byte[]) ArrayFactory.BYTES_FACTORY.array(TwoByteArrayToInt);
//                        inputStream.read(bArr2, 0, TwoByteArrayToInt);
//                        Logger.e("TCP - ava3:" + inputStream.available());
//                        TCPCursorSocket.this.createCursorImage(bArr2, false);
//                        ArrayFactory.BYTES_FACTORY.recycle(bArr2);
//                    }
//                } catch (Throwable e5) {
//                    Logger.e("TCP - read data EXC", e5);
//                    if (socket != null) {
//                        try {
//                            socket.close();
//                        } catch (IOException e6) {
//                        }
//                    }
//                    // TODO
////                    ConnectionActivity.ccMngr.sendTurnOffUDP();
//                    TCPCursorSocket.this.listenStop = true;
//                }
//            }
//            if (socket == null) {
//                try {
//                    Logger.e("TCP - point2");
//                    socket.close();
//                } catch (IOException e7) {
//                }
//            }
//        }
//    }
//
//    public TCPCursorSocket() {
//        this.listenStop = false;
//    }
//
//    public boolean openSocket(int i) {
//        try {
//            this.servSocket = new ServerSocket(i);
//            this.servSocket.setSoTimeout(this.TIMEOUT);
//            return true;
//        } catch (Throwable e) {
//            Logger.e("TCP cursor socket open EXC. port=" + i, e);
//            return false;
//        }
//    }
//
//    public void start() {
//        new AnonymousClass_1("TCP cursor thread").start();
//    }
//
//    public void stop() {
//        this.listenStop = true;
//    }
//}
