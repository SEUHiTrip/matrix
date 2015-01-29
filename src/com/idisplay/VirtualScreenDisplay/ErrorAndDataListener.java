package com.idisplay.VirtualScreenDisplay;

import com.idisplay.DataChannelManager.DataChannelManager;
import com.idisplay.util.Logger;
import org.apache.log4j.spi.ErrorCode;

class ErrorAndDataListener implements UnexpectedErrorListner, DataChannelConnectionListener {
    String className;

    ErrorAndDataListener() {
        this.className = "ErrorAndDataListener";
    }

    public void OnDataChannelConnected(DataChannelManager dataChannelManager) {
        VirtualScreenActivity.setDataChannelManager(dataChannelManager);
    }

    public void OnUnexpectedError(boolean z, String str) {
        Logger.e(this.className + ":Unexpected error");
        if (!(VirtualScreenActivity.backPressedAndExiting || VirtualScreenActivity.screenHandler == null)) {
            VirtualScreenActivity.backPressedAndExiting = true;
        }
        VirtualScreenActivity.screenHandler.sendEmptyMessage(ErrorCode.ADDRESS_PARSE_FAILURE);
    }
}
