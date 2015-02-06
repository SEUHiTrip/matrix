package com.idisplay.DataChannelManager.cursor;

import android.util.Log;

import com.idisplay.util.ImageContainer;
import com.idisplay.util.Utils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.Inflater;

import seu.lab.matrix.ScreenMatrixActivity;

public abstract class CursorSocket {
    private static final byte COORDINATES = (byte) 1;
    private static final byte IMAGE = (byte) 2;
    protected int TIMEOUT;
    private boolean mHidden;
    private int prevX;
    private int prevY;

    public CursorSocket() {
        this.TIMEOUT = 20000;
        this.prevX = -1;
        this.prevY = -1;
        this.mHidden = false;
    }

    protected void createCursorImage(byte[] bArr, boolean z) {
        int i = -1;
        boolean z2 = false;
        if (bArr.length != 0) {
            int TwoByteArrayToInt;
            int i2;
            if (z) {
                TwoByteArrayToInt = Utils.TwoByteArrayToInt(bArr, 0);
                i2 = 2;
            } else {
                TwoByteArrayToInt = bArr.length;
                i2 = 0;
            }
            int i3 = i2 + 1;
            byte b = bArr[i2];
            i2 = i3 + 1;
            byte b2 = bArr[i3];
            i3 = Utils.TwoByteArrayToInt(bArr, i2);
            int i4 = i2 + 2;
            i2 = Utils.TwoByteArrayToInt(bArr, i4);
            i4 += 2;
            int i5 = i4 + 1;
            if (bArr[i4] == (byte) 1 || i3 < 0 || i2 < 0) {
                z2 = true;
            }
            if (i3 != this.prevX || i2 != this.prevY || this.mHidden != z2) {
                this.prevX = i3;
                this.prevY = i2;
                this.mHidden = z2;
                if (b2 == (byte) 2) {
                    int TwoByteArrayToInt2 = Utils.TwoByteArrayToInt(bArr, i5);
                    int i6 = i5 + 2;
                    int TwoByteArrayToInt3 = Utils.TwoByteArrayToInt(bArr, i6);
                    i6 += 2;
                    Utils.TwoByteArrayToInt(bArr, i6);
                    i6 += 2;
                    i4 = Utils.TwoByteArrayToInt(bArr, i6);
                    i6 += 2;
                    i5 = Utils.FourByteArrayToInt(bArr, i6);
                    i6 += 4;
                    if (i5 != 0) {
                        byte[] bArr2 = new byte[i5];
                        Inflater inflater = new Inflater();
                        inflater.setInput(bArr, i6, i4);
                        try {
                            i6 = inflater.inflate(bArr2);
                            if (i6 != i5) {
                                Log.e("UDP", "Decompress image error. width=" + TwoByteArrayToInt2 + " height=" + TwoByteArrayToInt3 + " compSize=" + i4 + " uncSize=" + i5 + " packSize" + TwoByteArrayToInt + "decompressResult=" + i6);
                                inflater.end();
                                return;
                            }
                            inflater.end();
                            ByteBuffer wrap = ByteBuffer.wrap(bArr2);
                            wrap.order(ByteOrder.LITTLE_ENDIAN);

                            ScreenMatrixActivity.onCursorImgChange(new ImageContainer(wrap, TwoByteArrayToInt2, TwoByteArrayToInt3));
                        } catch (Throwable e) {
                            Log.e("UDP", "Decompress image EXCEPTION. width=" + TwoByteArrayToInt2 + " height=" + TwoByteArrayToInt3 + " compSize=" + i4 + " uncSize=" + i5 + " packSize" + TwoByteArrayToInt, e);
                            inflater.end();
                        }
                    }
                }
                if (z2) {
                    i2 = -1;
                } else {
                    i = i3;
                }
                ScreenMatrixActivity.onCursorPositionChange(i, i2);
            }
        }
    }

    public abstract void start();

    public abstract void stop();
}
