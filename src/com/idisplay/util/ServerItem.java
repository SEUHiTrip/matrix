package com.idisplay.util;

import android.app.Activity;
import com.idisplay.util.ServerItem.ServerType;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.Filter;

public class ServerItem {
    private boolean mEditable;
    private boolean mEmbedded;
    private String mHost;
    private int mImageResourceId;
    private int mPort;
    private String mServerName;
    private ServerType mServerType;

    public enum DeviceType {
        DESKTOP,
        NOTEBOOK,
        UNKNOWN
    }

    public enum ServerType {
        MAC,
        WINDOWS,
        UNKNOWN,
        USB
    }

    protected ServerItem(String str, String str2, int i, ServerType serverType, DeviceType deviceType, boolean z, boolean z2) {
        if (StringUtils.isEmpty(str)) {
            throw new IllegalArgumentException("the name is empty");
        } else if (Utils.validateIpV4(str2)) {
            this.mServerName = str;
            this.mHost = str2;
            this.mPort = i;
            this.mEditable = z;
            this.mEmbedded = z2;
            switch (serverType.ordinal()) {
                case Filter.ACCEPT:
                    if (deviceType == DeviceType.NOTEBOOK) {
                        this.mImageResourceId = 2130837674;
                    } else {
                        this.mImageResourceId = 2130837638;
                    }
                    break;
                case ErrorCode.FLUSH_FAILURE:
                    if (deviceType == DeviceType.NOTEBOOK) {
                        this.mImageResourceId = 2130837675;
                    } else {
                        this.mImageResourceId = 2130837648;
                    }
                    break;
                case ErrorCode.CLOSE_FAILURE:
                    this.mImageResourceId = 2130837646;
                    break;
                case ErrorCode.FILE_OPEN_FAILURE:
                    this.mImageResourceId = 2130837645;
                    break;
                default:
                    this.mImageResourceId = 2130837645;
                    break;
            }
            this.mServerType = serverType;
        } else {
            throw new IllegalArgumentException("invalid Ip format: " + str2);
        }
    }

    public static ServerItem CreateServerItem(String str, String str2, int i, ServerType serverType, DeviceType deviceType) {
        if (i > 0 && i <= 65535) {
            return new ServerItem(str, str2, i, serverType, deviceType, false, false);
        }
        throw new IllegalArgumentException("invalid port: " + i);
    }

    public static ServerItem CreateUsbItem(Activity activity) {
        return new ServerItem("Connect via USB", "1.1.1.1", -1, ServerType.USB, DeviceType.UNKNOWN, false, true);
    }

    public static ServerItem TryCreateServerItem(String str, String str2, int i) {
        try {
            return CreateServerItem("connect###", str2, i, ServerType.UNKNOWN, DeviceType.UNKNOWN);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean editable() {
        return this.mEditable;
    }

    public boolean embedded() {
        return this.mEmbedded;
    }

    public String getHost() {
        return this.mHost;
    }

    public int getImageResourceId() {
        return this.mImageResourceId;
    }

    public int getPort() {
        return this.mPort;
    }

    public String getServerName() {
        return this.mServerName;
    }

    public ServerType getServerType() {
        return this.mServerType;
    }

    public String toString() {
        return this.mServerName + "[" + this.mHost + ":" + this.mPort + "]";
    }
}
