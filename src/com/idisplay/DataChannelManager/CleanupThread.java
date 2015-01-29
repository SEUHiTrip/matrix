package com.idisplay.DataChannelManager;

import com.idisplay.VirtualScreenDisplay.ThreadEvent;
import com.idisplay.util.Logger;

public class CleanupThread extends Thread {
    private ThreadEvent CLEANUP_WAIT;
    private boolean m_stopProcess;

    public CleanupThread() {
        super("CleanupThread");
        this.CLEANUP_WAIT = new ThreadEvent();
    }

    public void cleanMemory() {
        this.CLEANUP_WAIT.signal();
    }

    public void run() {
        while (!this.m_stopProcess) {
            try {
                this.CLEANUP_WAIT.await();
                Logger.d("CleanupThread: running GC");
            } catch (Throwable e) {
                Logger.w(getClass().getName(), e);
            }
            System.gc();
        }
    }

    public void stopProcess() {
        this.m_stopProcess = true;
        cleanMemory();
    }
}
