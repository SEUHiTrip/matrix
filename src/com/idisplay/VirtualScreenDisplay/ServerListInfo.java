package com.idisplay.VirtualScreenDisplay;

public class ServerListInfo {
    private String ip;
    private String name;
    private int port;

    public ServerListInfo(String str, String str2, int i) {
        this.ip = str;
        this.name = str2;
        this.port = i;
    }

    public boolean equals(Object obj) {
        if (obj instanceof ServerListInfo) {
            ServerListInfo serverListInfo = (ServerListInfo) obj;
            if (this.ip.equals(serverListInfo.ip) && this.port == serverListInfo.port) {
                return true;
            }
        }
        return false;
    }

    public String getIp() {
        return this.ip;
    }

    public String getName() {
        return this.name;
    }

    public int getPort() {
        return this.port;
    }

    public String toString() {
        return this.port == -1 ? this.name : this.name + " : " + this.ip;
    }
}
