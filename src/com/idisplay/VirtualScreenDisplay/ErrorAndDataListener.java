package com.idisplay.VirtualScreenDisplay;

import com.idisplay.DataChannelManager.DataChannelManager;
import com.idisplay.util.Logger;
import org.apache.log4j.spi.ErrorCode;

import seu.lab.matrix.AbstractScreenMatrixActivity;
import seu.lab.matrix.MainActivity;
import seu.lab.matrix.test.ScreenMatrixActivity;

class ErrorAndDataListener implements UnexpectedErrorListner, DataChannelConnectionListener {
    String className;

    
    ErrorAndDataListener() {
        this.className = "ErrorAndDataListener";
    }

    public void OnDataChannelConnected(DataChannelManager dataChannelManager) {
    	AbstractScreenMatrixActivity.setDataChannelManager(dataChannelManager);
    }

    public void OnUnexpectedError(boolean z, String str) {
        Logger.e(this.className + ":Unexpected error");
//        if (!(ScreenMatrixActivity.backPressedAndExiting || ScreenMatrixActivity.screenHandler == null)) {
//            ScreenMatrixActivity.backPressedAndExiting = true;
//        }
        AbstractScreenMatrixActivity.getScreenHandler().sendEmptyMessage(ErrorCode.ADDRESS_PARSE_FAILURE);
    }
}
