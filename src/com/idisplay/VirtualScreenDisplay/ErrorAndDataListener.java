package com.idisplay.VirtualScreenDisplay;

import com.idisplay.DataChannelManager.DataChannelManager;
import com.idisplay.util.Logger;
import org.apache.log4j.spi.ErrorCode;

import seu.lab.matrix.ScreenMatrixActivity;

class ErrorAndDataListener implements UnexpectedErrorListner, DataChannelConnectionListener {
    String className;

    ErrorAndDataListener() {
        this.className = "ErrorAndDataListener";
    }

    public void OnDataChannelConnected(DataChannelManager dataChannelManager) {
        ScreenMatrixActivity.setDataChannelManager(dataChannelManager);
    }

    public void OnUnexpectedError(boolean z, String str) {
        Logger.e(this.className + ":Unexpected error");
        if (!(ScreenMatrixActivity.backPressedAndExiting || ScreenMatrixActivity.screenHandler == null)) {
            ScreenMatrixActivity.backPressedAndExiting = true;
        }
        ScreenMatrixActivity.screenHandler.sendEmptyMessage(ErrorCode.ADDRESS_PARSE_FAILURE);
    }
}
