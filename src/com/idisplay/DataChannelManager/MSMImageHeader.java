package com.idisplay.DataChannelManager;

import javolution.xml.stream.XMLStreamConstants;
import org.apache.log4j.spi.ErrorCode;

public class MSMImageHeader {
    private byte compression;
    private int height;
    private int imageOffset;
    private int width;

    public MSMImageHeader(byte[] bArr) {
        this.imageOffset = ByteArrayUtilities.byteArrayToInt(bArr, 0);
        if (DataChannelManager.isMACServer()) {
            this.width = ByteArrayUtilities.byteArrayToInt(bArr, ErrorCode.FILE_OPEN_FAILURE);
        } else {
            this.width = ByteArrayUtilities.byteArrayToInt(bArr, ErrorCode.FILE_OPEN_FAILURE);
        }
        this.height = ByteArrayUtilities.byteArrayToInt(bArr, XMLStreamConstants.END_DOCUMENT);
        this.compression = bArr[12];
    }
}
