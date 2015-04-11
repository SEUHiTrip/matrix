package com.idisplay.DataChannelManager.cursor;

import android.util.Log;

import com.idisplay.VirtualScreenDisplay.ConnectionActivity;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection;
import com.idisplay.util.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import seu.lab.matrix.Framework3DMatrixActivity;

public class UDPCursorSocket extends CursorSocket {
    private DatagramSocket socket;
    private boolean udpCursorReceived;
    private boolean udpStop;

    class AnonymousClass_1 extends Thread {
        AnonymousClass_1(String str) {
            super(str);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
//            throw new UnsupportedOperationException("Method not decompiled: com.idisplay.DataChannelManager.cursor.UDPCursorSocket.AnonymousClass_1.run():void");
            
        	int size = 0;
        	byte[] buffer;
        	DatagramPacket packet;
        	while (!udpStop) {
        		try {
    				size = socket.getReceiveBufferSize();
    				
    				buffer = new byte[size];
    				packet = new DatagramPacket(buffer, size);
    				socket.receive(packet);

    				if(udpCursorReceived == false){
    					udpCursorReceived = true;
    					socket.setSoTimeout(0);
    				}
    				createCursorImage(buffer, true);
				} catch (Exception e) {
		            com.idisplay.util.Logger.e("UDP cursor - run", e);
		            if(socket == null){
		            	// sendTurnOffUDP
		            	udpStop = true;
		            	continue;
		            }else {
						socket.close();
					}
		        }
				
			}
        	
        	socket.close();
        	return;
        	
        	/*
            this = this;
            r3 = 1;
            r0 = 0;
        L_0x0002:
            r1 = com.idisplay.DataChannelManager.cursor.UDPCursorSocket.this;
            r1 = r1.udpStop;
            if (r1 != 0) goto L_0x0075;
        L_0x000a:
            if (r0 == 0) goto L_0x0019;
        L_0x000c:
            r1 = r0.length;	 Catch:{ IOException -> 0x0053 }
            r2 = com.idisplay.DataChannelManager.cursor.UDPCursorSocket.this;	 Catch:{ IOException -> 0x0053 }
            r2 = r2.socket;	 Catch:{ IOException -> 0x0053 }
            r2 = r2.getReceiveBufferSize();	 Catch:{ IOException -> 0x0053 }
            if (r1 == r2) goto L_0x0025;
        L_0x0019:
            r1 = com.idisplay.DataChannelManager.cursor.UDPCursorSocket.this;	 Catch:{ IOException -> 0x0053 }
            r1 = r1.socket;	 Catch:{ IOException -> 0x0053 }
            r1 = r1.getReceiveBufferSize();	 Catch:{ IOException -> 0x0053 }
            r0 = new byte[r1];	 Catch:{ IOException -> 0x0053 }
        L_0x0025:
            r1 = new java.net.DatagramPacket;	 Catch:{ IOException -> 0x0053 }
            r2 = r0.length;	 Catch:{ IOException -> 0x0053 }
            r1.<init>(r0, r2);	 Catch:{ IOException -> 0x0053 }
            r2 = com.idisplay.DataChannelManager.cursor.UDPCursorSocket.this;	 Catch:{ IOException -> 0x0053 }
            r2 = r2.socket;	 Catch:{ IOException -> 0x0053 }
            r2.receive(r1);	 Catch:{ IOException -> 0x0053 }
            r1 = com.idisplay.DataChannelManager.cursor.UDPCursorSocket.this;	 Catch:{ IOException -> 0x0053 }
            r1 = r1.udpCursorReceived;	 Catch:{ IOException -> 0x0053 }
            if (r1 != 0) goto L_0x004c;
        L_0x003c:
            r1 = com.idisplay.DataChannelManager.cursor.UDPCursorSocket.this;	 Catch:{ IOException -> 0x0053 }
            r2 = 1;
            r1.udpCursorReceived = r2;	 Catch:{ IOException -> 0x0053 }
            r1 = com.idisplay.DataChannelManager.cursor.UDPCursorSocket.this;	 Catch:{ IOException -> 0x0053 }
            r1 = r1.socket;	 Catch:{ IOException -> 0x0053 }
            r2 = 0;
            r1.setSoTimeout(r2);	 Catch:{ IOException -> 0x0053 }
        L_0x004c:
            r1 = com.idisplay.DataChannelManager.cursor.UDPCursorSocket.this;	 Catch:{ IOException -> 0x0053 }
            r2 = 1;
            r1.createCursorImage(r0, r2);	 Catch:{ IOException -> 0x0053 }
            goto L_0x0002;
        L_0x0053:
            r1 = move-exception;
            r2 = "UDP cursor - run";
            com.idisplay.util.Logger.e(r2, r1);
            r1 = com.idisplay.DataChannelManager.cursor.UDPCursorSocket.this;
            r1 = r1.socket;
            if (r1 == 0) goto L_0x006a;
        L_0x0061:
            r1 = com.idisplay.DataChannelManager.cursor.UDPCursorSocket.this;
            r1 = r1.socket;
            r1.close();
        L_0x006a:
            r1 = com.idisplay.VirtualScreenDisplay.ConnectionActivity.ccMngr;
            r1.sendTurnOffUDP();
            r1 = com.idisplay.DataChannelManager.cursor.UDPCursorSocket.this;
            r1.udpStop = r3;
            goto L_0x0002;
        L_0x0075:
            r0 = com.idisplay.DataChannelManager.cursor.UDPCursorSocket.this;
            r0 = r0.socket;
            r0.close();
            return;
            */
        }
    }

    public UDPCursorSocket() {
        this.udpStop = false;
    }

    public boolean openSocket(int i) {
        boolean z = false;
        this.udpCursorReceived = false;
        try {
            this.socket = new DatagramSocket(i);
            this.socket.setSoTimeout(this.TIMEOUT);
            z = true;
            return true;
        } catch (Throwable e) {
            Log.e("UDP", "UDP cursor socket open EXC. port= " + i, e);
            return z;
        }
    }

    public void start() {
        this.udpStop = false;
        new AnonymousClass_1("UDP cursor thread").start();
    }

    public void stop() {
        try {
            this.socket.close();
        } catch (Throwable e) {
            Logger.w("Unable to close the socket ", e);
        }
        this.udpStop = true;
    }
}
