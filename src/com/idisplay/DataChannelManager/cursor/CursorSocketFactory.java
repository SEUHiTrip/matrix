package com.idisplay.DataChannelManager.cursor;

import org.apache.commons.lang.NotImplementedException;

public class CursorSocketFactory {
    public static int DEFAULT_TCP_CURSOR_PORT;

    static {
        DEFAULT_TCP_CURSOR_PORT = 53423;
    }

    public static CursorSocket getTCPSocket(int i) {
    	throw new NotImplementedException("getTCPSocket");
//    	TCPCursorSocket tCPCursorSocket = new TCPCursorSocket();
//        return tCPCursorSocket.openSocket(i) ? tCPCursorSocket : null;
    }

    public static CursorSocket getUDPSocket(int i) {
    	UDPCursorSocket uDPCursorSocket = new UDPCursorSocket();
        return uDPCursorSocket.openSocket(i) ? uDPCursorSocket : null;
    }
}
