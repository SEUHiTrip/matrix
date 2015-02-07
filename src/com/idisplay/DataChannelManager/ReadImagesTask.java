package com.idisplay.DataChannelManager;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import com.idisplay.DataChannelManager.DataChannelManager.Compression;
import com.idisplay.VirtualScreenDisplay.FPSCounter;
import com.idisplay.VirtualScreenDisplay.ThreadEvent;
import com.idisplay.util.ArrayImageContainer;
import com.idisplay.util.Logger;
import com.idisplay.util.RLEImage;
import com.idisplay.vp8.VP8Decoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.LinkedBlockingQueue;

import seu.lab.matrix.ScreenMatrixActivity;

public class ReadImagesTask extends Thread {
    static final int MAX_QUEUE_SIZE = 30;
    static final int NUM_SKIP_IMAGES = 15;
    static final int THRESHOLD_QUEUE_SIZE = 10;
    private ThreadEvent APPEND_QUEUE_WAIT;
    private volatile boolean bTooManyFrames;
    private String className;
    private CleanupThread cleanup;
    private LinkedBlockingQueue<VideoDetails> imageQueue;
    private volatile boolean m_stopProcess;
    private volatile boolean waiting_for_data;

    public ReadImagesTask(CleanupThread cleanupThread) {
        this.APPEND_QUEUE_WAIT = new ThreadEvent();
        this.bTooManyFrames = false;
        this.waiting_for_data = false;
        this.imageQueue = new LinkedBlockingQueue();
        this.m_stopProcess = false;
        this.cleanup = null;
        this.className = "ReadImagesTask";
        this.cleanup = cleanupThread;
    }

    private void addVideoDetails(VideoDetails videoDetails) {
        FPSCounter.getImageFromServer(videoDetails.hashCode());
        if (!this.m_stopProcess) {
            ByteBuffer wrap = ByteBuffer.wrap(videoDetails.m_videoData);
            wrap.order(ByteOrder.LITTLE_ENDIAN);
            wrap.getInt(videoDetails.mVideoDataOffset);
            this.imageQueue.add(videoDetails);
            if (this.imageQueue.size() >= 30) {
                this.bTooManyFrames = true;
            }
        }
    }

    private static Compression getCompression(int i) {
        switch (i) {
            case 0:
                return Compression.None;
            case 1:
                return Compression.RLE;
            case 2:
                return Compression.LZW;
            case 3:
                return Compression.TIFF;
            case 4:
                return Compression.PNG;
            case 5:
                return Compression.JPEG;
            case 6:
                return Compression.MTC_RLE_LZJB;
            case 7:
                return Compression.ME;
            case 8:
                return Compression.VP8;
            default:
                //Logger.e("Unsupported Format " + i);
                return Compression.VP8;
        }
    }

    private byte[] getUnpackedLZJB(VideoDetails videoDetails, boolean z) {
        if (z) {
            return LZJB.lzjb_decompress(videoDetails.m_videoData, videoDetails.mVideoDataOffset, videoDetails.m_decompressLength);
        }
        int length = videoDetails.m_videoData.length - videoDetails.mVideoDataOffset;
        videoDetails.m_decompressLength = length;
        byte[] obj = new byte[length];
        System.arraycopy(videoDetails.m_videoData, videoDetails.mVideoDataOffset, obj, 0, length);
        return obj;
    }

    @TargetApi(11)
    private void initBitmapOptionsToReuseBitmap(VideoDetails videoDetails, Options options) {

    }

    private void renderProcessedData(int i, Object obj) {
        ScreenMatrixActivity.onDataAvailable(i, obj);
    }

    public void clearImageQueue() {
        this.imageQueue.clear();
    }

    public void run() {
        Options options = new Options();
        boolean ok = false;
        int i = 0;
        Object obj = null;
        RLEImage rLEImage = null;
        while (!this.m_stopProcess) {
            try {
                VideoDetails videoDetails = (VideoDetails) this.imageQueue.take();
                if (!this.m_stopProcess && videoDetails != null) {
                    ok = false;
                    i=0;
                    switch (getCompression(videoDetails.m_compressionAlgo).ordinal()) {
                        case 5:
                            if (!this.bTooManyFrames || this.imageQueue.size() % 15 == 0) {
                                initBitmapOptionsToReuseBitmap(videoDetails, options);
                                options.inSampleSize = 1;
                                Bitmap decodeByteArray = BitmapFactory.decodeByteArray(videoDetails.m_videoData, videoDetails.mVideoDataOffset, videoDetails.getVideoDataLen(), options);
                                Bitmap bitmap;
                                if (decodeByteArray != null) {
                                	ok = true;
                                    bitmap = decodeByteArray;
                                    i = 1;
                                } else {
                                    Logger.w("Can't decode jpeg data, size: " + videoDetails.getVideoDataLen());
                                    bitmap = decodeByteArray;
                                    i = 1;
                                }
                                obj = bitmap;
                            }
                            break;
                        case 6:
                        	byte[] unpackedLZJB = getUnpackedLZJB(videoDetails, true);
                        	i = 2;
                            if (unpackedLZJB != null) {
                                rLEImage = new RLEImage(unpackedLZJB, videoDetails.m_decompressLength, videoDetails.m_imageWidth, videoDetails.m_imageHeight, videoDetails.m_rowBytes / 4);
                                i = 2;
                                //ok = videoDetails.m_imageWidth <= 0 && videoDetails.m_imageHeight > 0;
                                ok = true;
                                obj = rLEImage;
                            }
                            break;
//                        case 3:
//                            unpackedLZJB = getUnpackedLZJB(videoDetails, true);
//                            if (unpackedLZJB == null) {
//                            	ok = false;
//                            } else {
//                                rLEImage = new RLEImage(unpackedLZJB, videoDetails.m_decompressLength, videoDetails.m_imageWidth, videoDetails.m_imageHeight, videoDetails.m_rowBytes / 4);
//                                i = 2;
//                                obj = rLEImage;
//                                if (videoDetails.m_imageWidth <= 0) {
//                                }
//                            }
//                            break;
                        case 8:
                            ArrayImageContainer decode = VP8Decoder.getInstance().decode(videoDetails);
                            if (decode != null) {
                            	ok = true;
                                obj = decode;
                                i = 4;
                            }
                            break;
                        default:
                            if (this.bTooManyFrames || this.imageQueue.size() > 10) {
                                this.bTooManyFrames = false;
                                this.APPEND_QUEUE_WAIT.signal();
                                this.cleanup.cleanMemory();
                                Logger.w("ReadImagesTasksignalled to start append queue");
                            }
                    }
                                      
                    if (ok) {
                        renderProcessedData(i, obj);
					}
//                    ByteBufferPool.put(videoDetails.m_videoData);
//                    if (this.bTooManyFrames) {
//                    }
                }
            } catch (Throwable e) {
                Logger.e("InterruptedException in read task ", e);
                this.m_stopProcess = true;
            }
        }
    }

//    public void run() {
//        Options options = new Options();
//        boolean z = false;
//        Object obj = null;
//        boolean z2 = false;
//        while (!this.m_stopProcess) {
//            try {
//                VideoDetails videoDetails = (VideoDetails) this.imageQueue.take();
//                if (!this.m_stopProcess && videoDetails != null) {
//                    boolean z3;
//                    int i = 0;
//                    Object obj2 = null;
//                    int i2;
//                    boolean z4;
//                    byte[] unpackedLZJB;
//                    RLEImage rLEImage = null;
//                    RLEImage rLEImage2 = null;
//                    boolean z5;
//                    switch (AnonymousClass_1.$SwitchMap$com$idisplay$DataChannelManager$DataChannelManager$Compression[getCompression(videoDetails.m_compressionAlgo).ordinal()]) {
//                        case 1:
//                            if (!this.bTooManyFrames || this.imageQueue.size() % 15 == 0) {
//                                initBitmapOptionsToReuseBitmap(videoDetails, options);
//                                options.inSampleSize = 1;
//                                Bitmap decodeByteArray = BitmapFactory.decodeByteArray(videoDetails.m_videoData, videoDetails.mVideoDataOffset, videoDetails.getVideoDataLen(), options);
//                                z3 = decodeByteArray != null;
//                                Bitmap bitmap;
//                                if (z3) {
//                                    z2 = z3;
//                                    z3 = z;
//                                    bitmap = decodeByteArray;
//                                    i = 1;
//                                } else {
//                                    Logger.w("Can't decode jpeg data, size: " + videoDetails.getVideoDataLen());
//                                    z2 = z3;
//                                    z3 = z;
//                                    bitmap = decodeByteArray;
//                                    i = 1;
//                                }
//                                if (z2) {
//                                    renderProcessedData(i, bitmap);
//                                }
//                                ByteBufferPool.put(videoDetails.m_videoData);
//                                if (this.bTooManyFrames) {
//                                }
//                                i2 = i;
//                                obj = obj2;
//                                z = z3;
//                            }
//                        case 2:
//                            z4 = true;
//                            unpackedLZJB = getUnpackedLZJB(videoDetails, z4);
//                            if (unpackedLZJB == null) {
//                                rLEImage = new RLEImage(unpackedLZJB, videoDetails.m_decompressLength, videoDetails.m_imageWidth, videoDetails.m_imageHeight, videoDetails.m_rowBytes / 4);
//                                i = ErrorCode.FLUSH_FAILURE;
//                                z = videoDetails.m_imageWidth <= 0 && videoDetails.m_imageHeight > 0;
//                                z2 = z;
//                                rLEImage2 = rLEImage;
//                                z3 = z4;
//                            } else {
//                                z3 = z4;
//                                obj2 = obj;
//                                z5 = z2;
//                                z2 = false;
//                            }
//                            if (z2) {
//                                renderProcessedData(i, rLEImage);
//                            }
//                            ByteBufferPool.put(videoDetails.m_videoData);
//                            if (this.bTooManyFrames) {
//                            }
//                            i2 = i;
//                            obj = obj2;
//                            z = z3;
//                            break;
//                        case 3:
//                            z4 = z;
//                            unpackedLZJB = getUnpackedLZJB(videoDetails, z4);
//                            if (unpackedLZJB == null) {
//                                z3 = z4;
//                                obj2 = obj;
//                                z5 = z2;
//                                z2 = false;
//                            } else {
//                                rLEImage = new RLEImage(unpackedLZJB, videoDetails.m_decompressLength, videoDetails.m_imageWidth, videoDetails.m_imageHeight, videoDetails.m_rowBytes / 4);
//                                i = ErrorCode.FLUSH_FAILURE;
//                                if (videoDetails.m_imageWidth <= 0) {
//                                }
//                                z2 = z;
//                                rLEImage2 = rLEImage;
//                                z3 = z4;
//                            }
//                            if (z2) {
//                                renderProcessedData(i, obj2);
//                            }
//                            ByteBufferPool.put(videoDetails.m_videoData);
//                            if (this.bTooManyFrames) {
//                            }
//                            i2 = i;
//                            obj = obj2;
//                            z = z3;
//                            break;
//                        case 4:
//                            ArrayImageContainer decode = VP8Decoder.getInstance().decode(videoDetails);
//                            boolean z6 = decode != null;
//                            z3 = z;
//                            ArrayImageContainer arrayImageContainer = decode;
//                            i = 4;
//                            z2 = z6;
//                            if (z2) {
//                                renderProcessedData(i, obj2);
//                            }
//                            ByteBufferPool.put(videoDetails.m_videoData);
//                            if (this.bTooManyFrames) {
//                            }
//                            i2 = i;
//                            obj = obj2;
//                            z = z3;
//                            break;
//                        default:
//                            z2 = false;
//                            z3 = z;
//                            i = 0;
//                            obj2 = null;
//                            if (z2) {
//                                renderProcessedData(i, obj2);
//                            }
//                            ByteBufferPool.put(videoDetails.m_videoData);
//                            if (this.bTooManyFrames || this.imageQueue.size() > 10) {
//                                i2 = i;
//                                obj = obj2;
//                                z = z3;
//                            } else {
//                                this.bTooManyFrames = false;
//                                this.APPEND_QUEUE_WAIT.signal();
//                                this.cleanup.cleanMemory();
//                                Logger.w("ReadImagesTasksignalled to start append queue");
//                                i2 = i;
//                                obj = obj2;
//                                z = z3;
//                            }
//                            break;
//                    }
//                }
//            } catch (Throwable e) {
//                Logger.e("InterruptedException in read task ", e);
//                this.m_stopProcess = true;
//            }
//        }
//    }
    
    public void setVideoUpdate(byte[] bArr, int i, int i2, int i3, int i4, int i5, int i6) {
        addVideoDetails(new VideoDetails(bArr, i, i2, i3, i4, i5, i6));
        if (this.bTooManyFrames) {
            try {
                Logger.w("ReadImagesTask:waiting for renderer process to clear queue");
                this.APPEND_QUEUE_WAIT.await();
            } catch (Throwable e) {
                Logger.w(getClass().getName(), e);
            }
        }
    }

    public void stopProcess() {
        Logger.d("stop process");
        this.m_stopProcess = true;
        try {
            this.imageQueue.put(new VideoDetails(new byte[1], 0, 0, 0, 0, 0, 0));
        } catch (Exception e) {
            Logger.e("unable to stop the queue");
        }
    }
}
